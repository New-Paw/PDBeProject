package dbs.multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

        import java.io.*;

        import java.sql.Connection;
import java.sql.Date;

@WebServlet("/api/Mentities")
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

        int MID = Integer.parseInt(req.getParameter("MID"));

        int SID_ref = Integer.parseInt(req.getParameter("SID_ref"));

        String Title = req.getParameter("title");

        String tokStr = req.getParameter("Tokentime");

        Date Tokentime = Date.valueOf(tokStr);  // 格式必须是 yyyy-MM-dd

        Part filePart = req.getPart("image"); // 表单里的 name="image"

        // 把上传的文件写到一个临时文件（复用你现有的 saveImageToDbFromFile）
        File tmp = File.createTempFile("upload_", ".img");
        try (InputStream in = filePart.getInputStream();
             OutputStream out = new FileOutputStream(tmp)) {
            in.transferTo(out);
        }

        try (Connection conn = DbUtil.getConnection()) {
            Mentities product = new Mentities(MID, SID_ref,Title, Tokentime);
            product.saveToDb(conn); // 确保基本记录存在
            product.saveImageToDbFromFile(conn, tmp.getAbsolutePath());
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            tmp.delete();
        }

        resp.sendRedirect(req.getContextPath() + "/view?code=" + MID);
    }
}


