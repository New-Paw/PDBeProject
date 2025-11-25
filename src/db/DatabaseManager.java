
import java.sql.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import oracle.jdbc.OracleResultSet;
import oracle.ord.im.OrdImage;
import ui.*;

public class DatabaseManager {
    private Connection conn;

    public DatabaseManager() throws SQLException {
        conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe",
                "your_user",
                "your_password");
    }

    public MapMedia loadMediaByName(String name) throws Exception {
        String sql =
            "SELECT m.entity_id, m.entity_name, m.entity_type, " +
            "       SDO_GEOM.SDO_CENTROID(m.geometry, 0.005).sdo_point.x AS x, " +
            "       SDO_GEOM.SDO_CENTROID(m.geometry, 0.005).sdo_point.y AS y, " +
            "       e.tokentime, e.image " +
            "FROM map m JOIN MEntities e ON e.SID_ref = m.entity_id " +
            "WHERE LOWER(m.entity_name) = LOWER(?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (OracleResultSet rs = (OracleResultSet) ps.executeQuery()) {
                if (!rs.next()) return null;

                MapMedia mm = new MapMedia();
                mm.setId(rs.getInt("entity_id"));
                mm.setName(rs.getString("entity_name"));
                mm.setType(rs.getString("entity_type"));
                mm.setX(rs.getDouble("x"));
                mm.setY(rs.getDouble("y"));

                Timestamp ts = rs.getTimestamp("tokentime");
                if (ts != null) {
                    mm.setTokenTime(new java.util.Date(ts.getTime()));
                }

                // 读取 ORDSYS.ORDImage
                OrdImage ordImg = (OrdImage) rs.getORAData("image", OrdImage.getORADataFactory());
                if (ordImg != null && ordImg.getContentLength() > 0) {
                    try (java.io.InputStream is = ordImg.getDataInStream()) {
                        BufferedImage bi = ImageIO.read(is);
                        mm.setImage(bi);
                    }
                }
                return mm;
            }
        }
    }
}

