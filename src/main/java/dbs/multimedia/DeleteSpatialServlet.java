package dbs.multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/deleteSpatial")
public class DeleteSpatialServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Make sure the sid is correct.
        String sidStr = request.getParameter("sid");
        if (sidStr == null || sidStr.trim().isEmpty()) {
            setErrorAndForward(request, response, "SID is required for delete.");
            return;
        }

        int sid;
        try {
            sid = Integer.parseInt(sidStr.trim());
        } catch (NumberFormatException e) {
            setErrorAndForward(request, response, "SID must be an integer.");
            return;
        }

        String sql = "DELETE FROM SEntities WHERE SID = ?";

        try (Connection conn = DbUtil.getConnection();  // Connect to the database to delete.
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sid);
            int deleted = ps.executeUpdate();
            conn.commit();

            if (deleted == 0) {
                setErrorAndForward(request, response,
                        "No spatial entity found with SID = " + sid);
            } else {
                response.sendRedirect(request.getContextPath() + "/map.jsp");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setErrorAndForward(request, response,
                    "Failed to delete spatial entity: " + e.getMessage());
        }
    }

    private void setErrorAndForward(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String msg) throws ServletException, IOException {
        request.setAttribute("error", msg);
        request.getRequestDispatcher("map.jsp").forward(request, response);
    }
}
