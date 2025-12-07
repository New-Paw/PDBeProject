package dbs.multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.sql.*;

@WebServlet("/uploadImage")
@MultipartConfig
public class UploadServlet extends HttpServlet {

    // Generate next MID: MAX(MID) + 1
    private int getNextMid(Connection conn) throws SQLException {
        String sql = "SELECT NVL(MAX(MID),0) + 1 FROM MEntities";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // Parse SID reference
        String sidStr = req.getParameter("sid");
        if (sidStr == null || sidStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/map.jsp");
            return;
        }
        int SID_ref = Integer.parseInt(sidStr);

        // Parse title
        String title = req.getParameter("title");
        if (title == null) title = "";

        // Parse Tokentime (date provided by user)
        String timeStr = req.getParameter("tokentime");
        Date tokenTime;

        if (timeStr == null || timeStr.isEmpty()) {
            // Default to current date when user does not specify a date
            tokenTime = new Date(System.currentTimeMillis());
        } else {
            try {
                tokenTime = Date.valueOf(timeStr); // yyyy-MM-dd
            } catch (IllegalArgumentException ex) {
                // Fallback to current date if format is invalid
                tokenTime = new Date(System.currentTimeMillis());
            }
        }

        // Parse image file
        Part filePart = req.getPart("image");
        if (filePart == null || filePart.getSize() == 0) {
            resp.sendRedirect(req.getContextPath() + "/map.jsp");
            return;
        }

        // Write uploaded image to a temporary file
        File tmp = File.createTempFile("upload_", ".img");
        try (InputStream in = filePart.getInputStream();
             OutputStream out = new FileOutputStream(tmp)) {
            in.transferTo(out);
        }

        try (Connection conn = DbUtil.getConnection()) {

            // Allocate new MID
            int MID = getNextMid(conn);

            // Create metadata record
            Mentities entity = new Mentities(MID, SID_ref, title, tokenTime);
            entity.saveToDb(conn);

            // Store the binary image into ORDImage column
            entity.saveImageToDbFromFile(conn, tmp.getAbsolutePath());

        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            tmp.delete();
        }

        // Redirect back to map page after upload
        resp.sendRedirect(req.getContextPath() + "/map.jsp");
    }
}
