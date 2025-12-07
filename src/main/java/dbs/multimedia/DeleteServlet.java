package dbs.multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/deleteImage")
public class DeleteServlet extends HttpServlet {

    // Delete a single image row by MID
    private static final String SQL =
            "DELETE FROM MEntities WHERE MID = ?";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String midStr = req.getParameter("mid");
        if (midStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("missing mid");
            return;
        }

        int mid;
        try {
            mid = Integer.parseInt(midStr);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("invalid mid");
            return;
        }

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {

            ps.setInt(1, mid);
            int rows = ps.executeUpdate();

            if (rows == 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("not found");
            } else {
                out.println("ok");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Foreign key constraints or other DB errors end up here
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("error");
        }
    }
}
