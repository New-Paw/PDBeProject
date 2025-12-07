package dbs.multimedia;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

@WebServlet("/api/similar")
public class SimilarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String midParam = req.getParameter("mid");

        // Optional weight parameters for similarity scoring
        String wACParam = req.getParameter("wAC");
        String wCHParam = req.getParameter("wCH");
        String wPCParam = req.getParameter("wPC");
        String wTXParam = req.getParameter("wTX");

        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        if (midParam == null) {
            out.print("[]");
            return;
        }

        int mid;
        try {
            mid = Integer.parseInt(midParam);
        } catch (NumberFormatException e) {
            out.print("[]");
            return;
        }

        // Default similarity weights (1.0 each)
        double wAC = parseOrDefault(wACParam, 1.0);
        double wCH = parseOrDefault(wCHParam, 1.0);
        double wPC = parseOrDefault(wPCParam, 1.0);
        double wTX = parseOrDefault(wTXParam, 1.0);

        try (Connection conn = DbUtil.getConnection()) {

            // Build entity for the source MID
            Mentities src = new Mentities(conn, mid);

            // Query the most similar image
            Mentities similar = src.findTheMostSimilar(conn, wAC, wCH, wPC, wTX);

            int simMid = similar.getMID();
            String title = similar.getTitle();
            Date tokentime = similar.getTokentime();

            StringBuilder json = new StringBuilder();
            json.append("[");

            json.append("{")
                    .append("\"id\":").append(simMid).append(",")
                    .append("\"title\":\"").append(escape(title)).append("\"");

            if (tokentime != null) {
                json.append(",\"tokentime\":\"").append(tokentime.toString()).append("\"");
            }

            json.append("}");
            json.append("]");

            out.print(json.toString());

        } catch (Mentities.NotFoundException e) {
            // No similar image found
            out.print("[]");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
        }
    }

    // Parses a double or returns default value
    private double parseOrDefault(String s, double def) {
        if (s == null || s.isEmpty()) return def;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    // JSON escape
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
