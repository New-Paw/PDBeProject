package ui;
import java.sql.*;

public class DatabaseManager {

    private Connection conn;

    public DatabaseManager() throws SQLException {
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String user = "YOUR_USER";
        String password = "YOUR_PASSWORD";

        conn = DriverManager.getConnection(url, user, password);
    }

  // Get the information from database
    public MapRecord getRecordByName(String name) throws SQLException {
        String sql =
            "SELECT entity_id, entity_name, entity_type, " +
            "       SDO_GEOM.SDO_CENTROID(geometry, 0.005).sdo_point.x AS x, " +
            "       SDO_GEOM.SDO_CENTROID(geometry, 0.005).sdo_point.y AS y, " +
            "       image_path, visit_time " +
            "FROM map " +
            "WHERE LOWER(entity_name) = LOWER(?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                MapRecord rec = new MapRecord();
                rec.setId(rs.getInt("entity_id"));
                rec.setName(rs.getString("entity_name"));
                rec.setType(rs.getString("entity_type"));
                rec.setX(rs.getDouble("x"));
                rec.setY(rs.getDouble("y"));
                rec.setImagePath(rs.getString("image_path"));

                Timestamp ts = rs.getTimestamp("visit_time");
                if (ts != null) {
                    rec.setVisitTime(new java.util.Date(ts.getTime()));
                }
                return rec;
            }
        }
    }

    public void updateRecord(MapRecord rec) throws SQLException {
    String sql = "UPDATE map SET entity_name = ?, image_path = ?, visit_time = ? " +
                 "WHERE entity_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, rec.getName());
        ps.setString(2, rec.getImagePath());
        if (rec.getVisitTime() != null) {
            ps.setTimestamp(3, new Timestamp(rec.getVisitTime().getTime()));
        } else {
            ps.setNull(3, Types.TIMESTAMP);
        }
        ps.setInt(4, rec.getId());
        ps.executeUpdate();
        }
    }

}

