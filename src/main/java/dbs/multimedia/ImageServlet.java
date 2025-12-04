package dbs.multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/api/Image")
public class ImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String MIDParam = req.getParameter("MID");
        if (MIDParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing MID");
            return;
        }

        int MID = Integer.parseInt(MIDParam);

        try (Connection conn = DbUtil.getConnection()) {
            Mentities mentities = new Mentities(conn, MID);
            resp.setContentType("image/*");
            mentities.writeImageToStream(conn, resp.getOutputStream());
        } catch (Mentities.NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
        } catch (Exception e) {
            throw new ServletException(e);
        }

    }
}
