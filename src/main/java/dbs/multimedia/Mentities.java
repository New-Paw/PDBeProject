package dbs.multimedia;

import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.ord.im.OrdImage;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.*;

public class Mentities {
    private static final String SQL_SELECT_DATA = "SELECT SID_ref, Title, Tokentime FROM mentities WHERE MID = ?";
    private static final String SQL_SELECT_SID_REF = "SELECT SID_ref FROM mentities WHERE MID = ?";
    private static final String SQL_SELECT_TITLE = "SELECT Title FROM mentities WHERE MID = ?";
    private static final String SQL_SELECT_IMAGE = "SELECT Image FROM mentities WHERE MID = ?";
    private static final String SQL_SELECT_TIME = "SELECT Tokentime FROM mentities WHERE MID = ?";
    private static final String SQL_SELECT_IMAGE_FOR_UPDATE =
            "SELECT Image FROM mentities WHERE MID = ? FOR UPDATE";
    private static final String SQL_INSERT_NEW =
            "INSERT INTO mentities (MID, Title, Image, Tokentime) VALUES (?, ?, ordsys.ordImage.init(),?)";
    private static final String SQL_UPDATE_DATA = "UPDATE mentities SET SID_ref = ?, Title = ?, Tokentime = ? WHERE MID = ?";
    private static final String SQL_UPDATE_SID_REF = "UPDATE mentities SET SID_ref = ? WHERE MID = ?";
    private static final String SQL_UPDATE_TITLE = "UPDATE mentities SET Title = ? WHERE MID = ?";
    private static final String SQL_UPDATE_IMAGE = "UPDATE mentities SET Image = ? WHERE MID = ?";
    private static final String SQL_UPDATE_TIME = "UPDATE mentities SET Tokentime = ? WHERE MID = ?";
    private static final String SQL_UPDATE_STILLIMAGE =
            "UPDATE mentities p SET"
                    + " p.Image_si ="
                    + " SI_StillImage(p.Image.getContent())"
                    + " WHERE"
                    + " p.MID"
                    + " = ?"; // an SQL method call needs to be on table.column, not just column
    private static final String SQL_UPDATE_STILLIMAGE_META =
            "UPDATE mentities SET Image_ac = SI_AverageColor(Image_si), Image_ch ="
                    + " SI_ColorHistogram(Image_si), Image_pc = SI_PositionalColor(Image_si), Image_tx ="
                    + " SI_Texture(Image_si) WHERE MID = ?";
    private static final String SQL_SIMILAR_IMAGE =
            "SELECT dst.MID, SI_ScoreByFtrList(new"
                    + " SI_FeatureList(src.Image_ac,?,src.Image_ch,?,src.Image_pc,?,src.Image_tx,?),dst.Image_si)"
                    + " AS similarity FROM mentities src, mentities dst WHERE (src.MID = ?) AND (src.MID <>"
                    + " dst.MID) ORDER BY similarity ASC";
    private int MID;
    private int SID_ref;
    private String Title;
    private Date Tokentime;

    public Mentities(int MID, int SID_ref, String Title, Date Tokentime) throws Mentities.NotFoundException, SQLException {
        this.MID=MID;
        this.SID_ref=SID_ref;
        this.Title=Title;
        this.Tokentime=Tokentime;
    }

    public Mentities(Connection connection, int MID) throws Mentities.NotFoundException, SQLException {
        this.MID=MID;
        // load the rest of properties from the database
        loadFromDb(connection);

    }

    /**
     * Load properties of the Mentities based on its MID from a database.
     *
     * @param connection database connection
     * @throws SQLException SQL error
     * @throws Mentities.NotFoundException the Mentities of this particular MID is not in the database
     * "SELECT SID_ref, Title, Tokentime FROM mentities WHERE MID = ?"
     */
    public void loadFromDb(Connection connection) throws SQLException, Mentities.NotFoundException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_DATA)) {
            preparedStatement.setInt(1, MID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    this.SID_ref = resultSet.getInt(1);
                    this.Title = resultSet.getString(2);
                    this.Tokentime = resultSet.getDate(3);
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }

    /**
     * Save properties of the Mentities to a database.
     *
     * @param connection database connection
     * @throws SQLException SQL error
     *  "INSERT INTO mentities (MID, Title, Image, ) VALUES (?, ?, ordsys.ordImage.init())"
     *  "UPDATE mentities SET SID_ref = ?, Title = ?, Tokentime = ? WHERE MID = ?"
     */
    public void saveToDb(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatementInsert = connection.prepareStatement(SQL_INSERT_NEW)) {
            preparedStatementInsert.setInt(1, MID);
            preparedStatementInsert.setString(2, Title);
            preparedStatementInsert.setDate(3, Tokentime);
            try {
                // try insert before update
                preparedStatementInsert.executeUpdate();
            } catch (SQLException sqlException) {
                try (PreparedStatement preparedStatementUpdate =
                             connection.prepareStatement(SQL_UPDATE_DATA)) {
                    preparedStatementUpdate.setInt(1, SID_ref);
                    preparedStatementUpdate.setString(2, Title);
                    preparedStatementUpdate.setDate(3, Tokentime);
                    preparedStatementUpdate.setInt(4, MID);
                    // try the update id the insert failed
                    preparedStatementUpdate.executeUpdate();
                }
            }
        }
    }

    /**
     * Load an image of the Mentities from a database and save it to a local file.
     *
     * @param connection database connection
     * @param filename file title where to save the image
     * @throws SQLException SQL error
     * @throws Mentities.NotFoundException the Mentities of this particular MID is not in the database
     * @throws IOException I/O error
     * "SELECT Image FROM mentities WHERE MID = ?"
     */
    public void loadImageFromDbToFile(Connection connection, String filename)
            throws SQLException, Mentities.NotFoundException, IOException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_IMAGE)) {
            preparedStatement.setInt(1, MID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final OracleResultSet oracleResultSet = (OracleResultSet) resultSet;
                    final OrdImage ordImage =
                            (OrdImage) oracleResultSet.getORAData(1, OrdImage.getORADataFactory());
                    ordImage.getDataInFile(filename);
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }

    /**
     * "SELECT Image FROM mentities WHERE MID = ? FOR UPDATE"
     */
    private OrdImage selectOrdImageForUpdate(Connection connection)
            throws SQLException, Mentities.NotFoundException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(SQL_SELECT_IMAGE_FOR_UPDATE)) {
            preparedStatement.setInt(1, MID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final OracleResultSet oracleResultSet = (OracleResultSet) resultSet;
                    return (OrdImage) oracleResultSet.getORAData(1, OrdImage.getORADataFactory());
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }


    private void recreateStillImageData(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatementSi =
                     connection.prepareStatement(SQL_UPDATE_STILLIMAGE)) {
            preparedStatementSi.setInt(1, MID);
            preparedStatementSi.executeUpdate();
        }
        try (PreparedStatement preparedStatementSiMeta =
                     connection.prepareStatement(SQL_UPDATE_STILLIMAGE_META)) {
            preparedStatementSiMeta.setInt(1, MID);
            preparedStatementSiMeta.executeUpdate();
        }
    }

    /**
     * Load an image of the Mentities from a local file and save it in a database.
     *
     * @param connection database connection
     * @param filename file title where to load the image from
     * @throws SQLException SQL error
     * @throws Mentities.NotFoundException the Mentities of this particular MID is not in the database
     * @throws IOException I/O error
     * "INSERT INTO mentities (MID, Title, Image, Tokentime) VALUES (?, ?, ordsys.ordImage.init(),?)"
     * "UPDATE mentities SET Image = ? WHERE MID = ?"
     */
    public void saveImageToDbFromFile(Connection connection, String filename)
            throws SQLException, Mentities.NotFoundException, IOException {
        final boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            OrdImage ordImage;
            try {
                // at first, try to get the image from an existing row
                ordImage = selectOrdImageForUpdate(connection);
            } catch (SQLException | Mentities.NotFoundException ex) {
                try (PreparedStatement preparedStatementInsert =
                             connection.prepareStatement(SQL_INSERT_NEW)) {
                    preparedStatementInsert.setInt(1, MID);
                    preparedStatementInsert.setString(2, Title);
                    preparedStatementInsert.setDate(3, Tokentime);
                    // insert a new row if the suitable row does not exist
                    preparedStatementInsert.executeUpdate();
                }
                // get the image from the previously inserted row
                ordImage = selectOrdImageForUpdate(connection);
            }
            ordImage.loadDataFromFile(filename);
            ordImage.setProperties();
            try (PreparedStatement preparedStatementUpdate =
                         connection.prepareStatement(SQL_UPDATE_IMAGE)) {
                final OraclePreparedStatement oraclePreparedStatement =
                        (OraclePreparedStatement) preparedStatementUpdate;
                oraclePreparedStatement.setORAData(1, ordImage);
                preparedStatementUpdate.setInt(2, MID);
                preparedStatementUpdate.executeUpdate();
            }
            recreateStillImageData(connection);
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    /**
     * Find a Mentities with the most similar image to the current Mentities based on several criteria.
     *
     * @param connection database connection
     * @param weightAC average color criteria
     * @param weightCH color histogram criteria
     * @param weightPC positional color criteria
     * @param weightTX texture criteria
     * @return object of the found Mentities
     * @throws SQLException SQL error
     * @throws Mentities.NotFoundException the suitable Mentities is not in the database
     */
    public Mentities findTheMostSimilar(
            Connection connection, double weightAC, double weightCH, double weightPC, double weightTX)
            throws SQLException, NotFoundException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_SIMILAR_IMAGE)) {
            preparedStatement.setDouble(1, weightAC);
            preparedStatement.setDouble(2, weightCH);
            preparedStatement.setDouble(3, weightPC);
            preparedStatement.setDouble(4, weightTX);
            preparedStatement.setInt(5, MID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final int MID = resultSet.getInt(1);
                    return new Mentities(connection, MID);
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }



    /**
     * 从数据库读取图片并写入到输出流（用于Servlet返回给浏览器）
     */
    public void writeImageToStream(Connection connection, java.io.OutputStream out)
            throws SQLException, Mentities.NotFoundException, java.io.IOException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_IMAGE)) {
            preparedStatement.setInt(1, MID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final OracleResultSet oracleResultSet = (OracleResultSet) resultSet;
                    final OrdImage ordImage =
                            (OrdImage) oracleResultSet.getORAData(1, OrdImage.getORADataFactory());
                    try (java.io.InputStream in = ordImage.getDataInStream()) {
                        in.transferTo(out);
                    }
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }
    public class NotFoundException extends Exception {
        // nothing to extend
    }

    public int getSID_ref() {
        return SID_ref;
    }

    public void setSID_ref(int SID_ref) {
        this.SID_ref = SID_ref;
    }

    public int getMID() {
        return MID;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public Date getTokentime() {
        return Tokentime;
    }

    public void setTokentime(Date tokentime) {
        Tokentime = tokentime;
    }
}
