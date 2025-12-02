package dbs.oracle_lab_multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

        import java.io.*;

        import java.sql.Connection;

@WebServlet("/upload_image")
@MultipartConfig
public class UploadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 转发到 JSP 表单页面
        req.getRequestDispatcher("/upload.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int code = Integer.parseInt(req.getParameter("code"));
        String title = req.getParameter("title");
        Part filePart = req.getPart("image"); // 表单里的 name="image"

        // 把上传的文件写到一个临时文件（复用你现有的 saveImageToDbFromFile）
        File tmp = File.createTempFile("upload_", ".img");
        try (InputStream in = filePart.getInputStream();
             OutputStream out = new FileOutputStream(tmp)) {
            in.transferTo(out);
        }

        try (Connection conn = DbUtil.getConnection()) {
            Product product = new Product(code, title);
            product.saveToDb(conn); // 确保基本记录存在
            product.saveImageToDbFromFile(conn, tmp.getAbsolutePath());
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            tmp.delete();
        }

        // 跳转到预览页面
        resp.sendRedirect(req.getContextPath() + "/view?code=" + code);
    }
}
