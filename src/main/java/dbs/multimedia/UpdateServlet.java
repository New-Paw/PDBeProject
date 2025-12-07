package dbs.multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/updateImageTitle")
public class UpdateServlet extends HttpServlet {

    // Update the title of an image record
    private static final String SQL =
            "UPDATE MEntities SET Title = ? WHERE MID = ?";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String midStr = req.getParameter("mid");
        String title  = req.getParameter("title");

        // Required parameters
        if (midStr == null || title == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("missing mid or title");
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

            ps.setString(1, title);
            ps.setInt(2, mid);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                // MID not found
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("not found");
            } else {
                out.println("ok");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("error");
        }
    }
}
