package dbs.oracle_lab_multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/image")
public class ImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String codeParam = req.getParameter("code");
        if (codeParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing code");
            return;
        }

        int code = Integer.parseInt(codeParam);

        try (Connection conn = DbUtil.getConnection()) {
            Product product = new Product(conn, code); // 先确认存在
            resp.setContentType("image/gif"); // 你存的是 GIF，如有需要可以改
            product.writeImageToStream(conn, resp.getOutputStream());
        } catch (Product.NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
