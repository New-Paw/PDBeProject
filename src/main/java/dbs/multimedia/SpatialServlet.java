package dbs.multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

@WebServlet("/api/Sentities")
public class SpatialServlet extends HttpServlet {

    private static final String SQL_FETCH =
            "SELECT " +
                    "  SID, " +
                    "  entity_Name AS NAME, " +
                    "  entity_Type AS TYPE, " +
                    "  SDO_UTIL.TO_WKTGEOMETRY(geometry) AS WKT, " +
                    "  SDO_GEOM.SDO_CENTROID(geometry, 0.005).SDO_POINT.Y AS LAT, " +
                    "  SDO_GEOM.SDO_CENTROID(geometry, 0.005).SDO_POINT.X AS LON " +
                    "FROM SEntities";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FETCH);
             ResultSet rs = ps.executeQuery()) {

            StringBuilder json = new StringBuilder();
            json.append("[");

            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    json.append(",");
                } else {
                    first = false;
                }

                int sid = rs.getInt("SID");
                String name = rs.getString("NAME");   // entity_Name AS NAME
                String type = rs.getString("TYPE");   // entity_Type AS TYPE
                String wkt = rs.getString("WKT");
                double lat = rs.getDouble("LAT");
                double lon = rs.getDouble("LON");

                json.append("{")
                        .append("\"sid\":").append(sid).append(",")
                        .append("\"name\":\"").append(escape(name)).append("\",")
                        .append("\"type\":\"").append(escape(type)).append("\",")
                        .append("\"lat\":").append(lat).append(",")
                        .append("\"lon\":").append(lon).append(",")
                        .append("\"wkt\":\"").append(escape(wkt)).append("\"")
                        .append("}");
            }

            json.append("]");
            out.print(json.toString());
            out.flush();

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"database error\"}");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")   // \
                .replace("\"", "\\\""); // "
    }
}
