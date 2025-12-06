package dbs.multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
        import java.io.*;
        import java.sql.*;

@WebServlet("/api/Simages")
public class SImagesServlet extends HttpServlet {

    private static final String SQL =
            "SELECT MID, Title " +
                    "FROM MEntities " +
                    "WHERE SID_ref = ? " +
                    "ORDER BY Tokentime DESC";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String sidParam = req.getParameter("sid");

        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        if (sidParam == null) {
            out.print("[]");
            return;
        }

        int sid;
        try {
            sid = Integer.parseInt(sidParam);
        } catch (NumberFormatException e) {
            out.print("[]");
            return;
        }

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {

            ps.setInt(1, sid);

            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder json = new StringBuilder();
                json.append("[");

                boolean first = true;
                while (rs.next()) {
                    if (!first) {
                        json.append(",");
                    } else {
                        first = false;
                    }

                    int mid = rs.getInt("MID");
                    String title = rs.getString("Title");

                    json.append("{")
                            .append("\"id\":").append(mid).append(",")
                            .append("\"title\":\"").append(escape(title)).append("\"")
                            .append("}");
                }

                json.append("]");
                out.print(json.toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
