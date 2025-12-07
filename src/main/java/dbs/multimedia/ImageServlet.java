package dbs.multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;

import oracle.ord.im.OrdImage;
import oracle.sql.STRUCT;
import oracle.jdbc.OracleTypes;

@WebServlet("/image")
public class ImageServlet extends HttpServlet {

    // Retrieve ORDImage object by MID
    private static final String SQL =
            "SELECT Image FROM MEntities WHERE MID = ?";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String midParam = req.getParameter("mid");
        if (midParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int mid = Integer.parseInt(midParam);

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {

            ps.setInt(1, mid);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                // Required for modern Oracle drivers: retrieve ORDImage via STRUCT
                STRUCT struct = (STRUCT) rs.getObject(1);
                if (struct == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                OrdImage ordImg =
                        (OrdImage) OrdImage.getORADataFactory()
                                .create(struct, OracleTypes.OTHER);

                if (ordImg == null || ordImg.getDataInByteArray() == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                // Set correct MIME type (PNG / JPEG / etc.)
                String mime = ordImg.getMimeType();
                if (mime == null || mime.isEmpty()) {
                    mime = "application/octet-stream";
                }
                resp.setContentType(mime);

                byte[] data = ordImg.getDataInByteArray();
                resp.setContentLength(data.length);

                OutputStream out = resp.getOutputStream();
                out.write(data);
                out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
