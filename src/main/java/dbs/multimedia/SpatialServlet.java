package dbs.multimedia;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

@WebServlet("/api/Sentities")
public class SpatialServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
//        req.getRequestDispatcher("/map.jsp").forward(req, resp);
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // 先写死两条数据，测试前端绘制是否正常
        String json = "[" +
                "{\"sid\":1,\"name\":\"Panda House\",\"type\":\"building\",\"lat\":34.001,\"lon\":113.001}," +
                "{\"sid\":2,\"name\":\"Aquarium\",\"type\":\"building\",\"lat\":3.002,\"lon\":113.002}" +
                "]";

        out.print(json);
        out.flush();
    }
}



//public class SpatialServlet extends HttpServlet {
//
//    private static final String SQL_FETCH =
//            "SELECT SID, entity_Name, entity_Type, " +
//                    "       geometry.sdo_point.y AS lat, " +
//                    "       geometry.sdo_point.x AS lon " +
//                    "FROM SEntities";
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException {
//
//        resp.setContentType("application/json; charset=UTF-8");
//
//        try (PrintWriter out = resp.getWriter()) {
//            try (Connection conn = DbUtil.getConnection();
//                 PreparedStatement ps = conn.prepareStatement(SQL_FETCH);
//                 ResultSet rs = ps.executeQuery()) {
//
//                StringBuilder json = new StringBuilder();
//                json.append("[");
//
//                boolean first = true;
//                while (rs.next()) {
//                    if (!first) {
//                        json.append(",");
//                    }
//                    first = false;
//
//                    int sid = rs.getInt("SID");
//                    String name = rs.getString("entity_Name");
//                    String type = rs.getString("entity_Type");
//                    double lat = rs.getDouble("lat");
//                    double lon = rs.getDouble("lon");
//
//                    json.append("{")
//                            .append("\"sid\":").append(sid).append(",")
//                            .append("\"name\":\"").append(escape(name)).append("\",")
//                            .append("\"type\":\"").append(escape(type)).append("\",")
//                            .append("\"lat\":").append(lat).append(",")
//                            .append("\"lon\":").append(lon)
//                            .append("}");
//                }
//
//                json.append("]");
//                out.print(json.toString());
//            } catch (SQLException e) {
//                e.printStackTrace();
//                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                out.print("{\"error\":\"database error\"}");
//            }
//        }
//    }
//
//    private String escape(String s) {
//        if (s == null) return "";
//        return s.replace("\"", "\\\"");
//    }
//}
