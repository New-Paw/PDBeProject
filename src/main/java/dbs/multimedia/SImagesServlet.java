package dbs.multimedia;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/Simages")
public class SImagesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // Optional filters
        String sidParam  = req.getParameter("sid");
        String fromParam = req.getParameter("from"); // yyyy-MM-dd
        String toParam   = req.getParameter("to");   // yyyy-MM-dd

        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // Parse SID (nullable)
        Integer sid = null;
        if (sidParam != null && !sidParam.isEmpty()) {
            try {
                sid = Integer.valueOf(sidParam);
            } catch (NumberFormatException e) {
                sid = null;  // ignore invalid SID
            }
        }

        // Parse date filters (nullable)
        Date fromDate = null;
        Date toDate = null;
        try {
            if (fromParam != null && !fromParam.isEmpty()) {
                fromDate = Date.valueOf(fromParam);
            }
            if (toParam != null && !toParam.isEmpty()) {
                toDate = Date.valueOf(toParam);
            }
        } catch (IllegalArgumentException e) {
            // ignore invalid dates
            fromDate = null;
            toDate = null;
        }

        try (Connection conn = DbUtil.getConnection()) {

            // Build dynamic SQL
            StringBuilder sql = new StringBuilder(
                    "SELECT MID, SID_ref, Title, Tokentime FROM MEntities WHERE 1=1"
            );

            List<Object> params = new ArrayList<>();

            if (sid != null) {
                sql.append(" AND SID_ref = ?");
                params.add(sid);
            }
            if (fromDate != null) {
                sql.append(" AND Tokentime >= ?");
                params.add(fromDate);
            }
            if (toDate != null) {
                sql.append(" AND Tokentime <= ?");
                params.add(toDate);
            }

            sql.append(" ORDER BY Tokentime DESC");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {

                // Bind parameters
                int index = 1;
                for (Object p : params) {
                    if (p instanceof Integer) {
                        ps.setInt(index++, (Integer) p);
                    } else if (p instanceof Date) {
                        ps.setDate(index++, (Date) p);
                    }
                }

                // Generate JSON output
                try (ResultSet rs = ps.executeQuery()) {

                    StringBuilder json = new StringBuilder();
                    json.append("[");

                    boolean first = true;
                    while (rs.next()) {
                        if (!first) json.append(",");
                        first = false;

                        int mid = rs.getInt("MID");
                        String title = rs.getString("Title");
                        Date tokentime = rs.getDate("Tokentime");

                        json.append("{")
                                .append("\"id\":").append(mid).append(",")
                                .append("\"title\":\"").append(escape(title)).append("\"");

                        if (tokentime != null) {
                            json.append(",\"tokentime\":\"").append(tokentime.toString()).append("\"");
                        }

                        json.append("}");
                    }

                    json.append("]");
                    out.print(json.toString());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
        }
    }

    // JSON escape helper
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
