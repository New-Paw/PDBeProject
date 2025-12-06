package dbs.multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

@WebServlet("/image")
public class ImageServlet extends HttpServlet {

    // 从 ORDImage 中取出本地 BLOB 数据
    private static final String SQL =
            "SELECT m.Image.localdata " +
                    "FROM MEntities m " +
                    "WHERE m.MID = ?";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String midParam = req.getParameter("mid");
        if (midParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int mid;
        try {
            mid = Integer.parseInt(midParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 默认按 JPEG 处理，如果你存的是 PNG，可以改成 image/png 或根据内容判断
        resp.setContentType("image/jpeg");

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {

            ps.setInt(1, mid);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                Blob blob = rs.getBlob(1);
                if (blob == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                try (InputStream in = blob.getBinaryStream();
                     OutputStream out = resp.getOutputStream()) {

                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
