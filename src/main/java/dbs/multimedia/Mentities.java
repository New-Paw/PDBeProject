package dbs.multimedia;

import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.ord.im.OrdImage;

import java.io.IOException;
import java.sql.*;

public class Mentities {

    // --- SQL statements ---
    private static final String SQL_SELECT_DATA =
            "SELECT SID_ref, Title, Tokentime FROM mentities WHERE MID = ?";

    private static final String SQL_SELECT_IMAGE =
            "SELECT Image FROM mentities WHERE MID = ?";

    private static final String SQL_SELECT_IMAGE_FOR_UPDATE =
            "SELECT Image FROM mentities WHERE MID = ? FOR UPDATE";

    private static final String SQL_INSERT_NEW =
            "INSERT INTO mentities (MID, Title, SID_ref, Image, Tokentime) " +
                    "VALUES (?, ?, ?, ordsys.ordImage.init(), ?)";

    private static final String SQL_UPDATE_DATA =
            "UPDATE mentities SET SID_ref = ?, Title = ?, Tokentime = ? WHERE MID = ?";

    private static final String SQL_UPDATE_IMAGE =
            "UPDATE mentities SET Image = ? WHERE MID = ?";

    // Populate SI_StillImage (the internal extracted feature blob)
    private static final String SQL_UPDATE_STILLIMAGE =
            "UPDATE mentities p SET p.Image_si = SI_StillImage(p.Image.getContent()) " +
                    "WHERE p.MID = ?";

    // Populate SI feature components (AC, CH, PC, TX)
    private static final String SQL_UPDATE_STILLIMAGE_META =
            "UPDATE mentities SET " +
                    "Image_ac = SI_AverageColor(Image_si), " +
                    "Image_ch = SI_ColorHistogram(Image_si), " +
                    "Image_pc = SI_PositionalColor(Image_si), " +
                    "Image_tx = SI_Texture(Image_si) " +
                    "WHERE MID = ?";

    // Similarity search based on weighted feature list
    private static final String SQL_SIMILAR_IMAGE =
            "SELECT dst.MID, SI_ScoreByFtrList(" +
                    "new SI_FeatureList(src.Image_ac, ?, src.Image_ch, ?, src.Image_pc, ?, src.Image_tx, ?)," +
                    " dst.Image_si) AS similarity " +
                    "FROM mentities src, mentities dst " +
                    "WHERE src.MID = ? AND src.MID <> dst.MID " +
                    "ORDER BY similarity ASC";

    private int MID;
    private int SID_ref;
    private String Title;
    private Date Tokentime;

    // Manual constructor
    public Mentities(int MID, int SID_ref, String Title, Date Tokentime) {
        this.MID = MID;
        this.SID_ref = SID_ref;
        this.Title = Title;
        this.Tokentime = Tokentime;
    }

    // Constructor that loads from DB
    public Mentities(Connection connection, int MID) throws SQLException, NotFoundException {
        this.MID = MID;
        loadFromDb(connection);
    }

    /**
     * Load Title, SID_ref, Tokentime from the database.
     */
    public void loadFromDb(Connection connection)
            throws SQLException, NotFoundException {

        try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_DATA)) {
            ps.setInt(1, MID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.SID_ref = rs.getInt(1);
                    this.Title = rs.getString(2);
                    this.Tokentime = rs.getDate(3);
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }

    /**
     * Insert or update metadata (SID_ref, Title, Tokentime).
     */
    public void saveToDb(Connection connection) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(SQL_INSERT_NEW)) {
            insert.setInt(1, MID);
            insert.setString(2, Title);
            insert.setInt(3, SID_ref);
            insert.setDate(4, Tokentime);

            try {
                insert.executeUpdate(); // attempt insert
            } catch (SQLException ex) {

                // If insert fails -> update instead
                try (PreparedStatement update = connection.prepareStatement(SQL_UPDATE_DATA)) {
                    update.setInt(1, SID_ref);
                    update.setString(2, Title);
                    update.setDate(3, Tokentime);
                    update.setInt(4, MID);
                    update.executeUpdate();
                }
            }
        }
    }

    /**
     * Retrieve ORDImage for update (required for writing image data into Oracle).
     */
    private OrdImage selectOrdImageForUpdate(Connection connection)
            throws SQLException, NotFoundException {

        try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_IMAGE_FOR_UPDATE)) {
            ps.setInt(1, MID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OracleResultSet ors = (OracleResultSet) rs;
                    return (OrdImage) ors.getORAData(1, OrdImage.getORADataFactory());
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }

    /**
     * Update SI_StillImage and its derived feature metadata.
     */
    private void recreateStillImageData(Connection connection) throws SQLException {
        try (PreparedStatement ps1 = connection.prepareStatement(SQL_UPDATE_STILLIMAGE)) {
            ps1.setInt(1, MID);
            ps1.executeUpdate();
        }

        try (PreparedStatement ps2 = connection.prepareStatement(SQL_UPDATE_STILLIMAGE_META)) {
            ps2.setInt(1, MID);
            ps2.executeUpdate();
        }
    }

    /**
     * Save an image from a local file into Oracle ORDImage column.
     */
    public void saveImageToDbFromFile(Connection connection, String filename)
            throws SQLException, NotFoundException, IOException {

        boolean prevAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            OrdImage img;

            // Try reading existing row; if not found, insert a new one
            try {
                img = selectOrdImageForUpdate(connection);
            } catch (Exception ex) {

                try (PreparedStatement insert = connection.prepareStatement(SQL_INSERT_NEW)) {
                    insert.setInt(1, MID);
                    insert.setString(2, Title);
                    insert.setInt(3, SID_ref);
                    insert.setDate(4, Tokentime);
                    insert.executeUpdate();
                }

                img = selectOrdImageForUpdate(connection);
            }

            // Load image binary
            img.loadDataFromFile(filename);
            img.setProperties();

            // Write image to DB
            try (PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_IMAGE)) {
                OraclePreparedStatement ops = (OraclePreparedStatement) ps;
                ops.setORAData(1, img);
                ps.setInt(2, MID);
                ps.executeUpdate();
            }

            // Update SI feature metadata
            recreateStillImageData(connection);

        } finally {
            connection.setAutoCommit(prevAutoCommit);
        }
    }

    /**
     * Find the most similar image based on SI feature weights.
     */
    public Mentities findTheMostSimilar(
            Connection connection, double wAC, double wCH, double wPC, double wTX)
            throws SQLException, NotFoundException {

        try (PreparedStatement ps = connection.prepareStatement(SQL_SIMILAR_IMAGE)) {
            ps.setDouble(1, wAC);
            ps.setDouble(2, wCH);
            ps.setDouble(3, wPC);
            ps.setDouble(4, wTX);
            ps.setInt(5, MID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int similarMID = rs.getInt(1);
                    return new Mentities(connection, similarMID);
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }

    /**
     * Stream image data to an OutputStream (e.g., servlet response).
     */
    public void writeImageToStream(Connection connection, java.io.OutputStream out)
            throws SQLException, NotFoundException, IOException {

        try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_IMAGE)) {
            ps.setInt(1, MID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OracleResultSet ors = (OracleResultSet) rs;
                    OrdImage img =
                            (OrdImage) ors.getORAData(1, OrdImage.getORADataFactory());

                    try (java.io.InputStream in = img.getDataInStream()) {
                        in.transferTo(out);
                    }
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }

    // --- Utility getters/setters ---
    public static class NotFoundException extends Exception {}

    public int getSID_ref() { return SID_ref; }
    public void setSID_ref(int value) { SID_ref = value; }

    public int getMID() { return MID; }

    public String getTitle() { return Title; }
    public void setTitle(String title) { Title = title; }

    public Date getTokentime() { return Tokentime; }
    public void setTokentime(Date d) { Tokentime = d; }
}
