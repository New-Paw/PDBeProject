package dbs.multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/createSpatial")
public class CreateSpatialServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String sidStr   = trim(request.getParameter("sid"));
        String name     = trim(request.getParameter("entityName"));
        String type     = trim(request.getParameter("entityType"));
        String shape    = trim(request.getParameter("shape")); // "point" or "rectangle"

        String xStr     = trim(request.getParameter("x"));
        String yStr     = trim(request.getParameter("y"));

        String x1Str    = trim(request.getParameter("x1"));
        String y1Str    = trim(request.getParameter("y1"));
        String x2Str    = trim(request.getParameter("x2"));
        String y2Str    = trim(request.getParameter("y2"));

        // Verification of submitted content.
        if (sidStr == null || name == null || type == null || shape == null ||
                sidStr.isEmpty() || name.isEmpty() || type.isEmpty()) {

            setErrorAndForward(request, response,
                    "SID, name, type and shape must not be empty.");
            return;
        }

        // Standardize data content.
        int sid;
        try {
            sid = Integer.parseInt(sidStr);
        } catch (NumberFormatException e) {
            setErrorAndForward(request, response, "SID must be an integer.");
            return;
        }

        boolean isPoint = "point".equalsIgnoreCase(shape);
        String sql;

        if (isPoint) {
            // Point geometry requires x,y
            if (xStr == null || yStr == null || xStr.isEmpty() || yStr.isEmpty()) {
                setErrorAndForward(request, response,
                        "Point geometry requires X and Y coordinates.");
                return;
            }

            Double x = parseAndValidateCoordinate(xStr, "X");
            Double y = parseAndValidateCoordinate(yStr, "Y");
            if (x == null || y == null) {
                setErrorAndForward(request, response,
                        "Invalid point coordinates (must be numeric in range 0–500).");
                return;
            }

            // Insert the data to database.
            sql = "INSERT INTO SEntities (SID, entity_Name, entity_Type, geometry) " +
                    "VALUES (?, ?, ?, " +
                    "SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE(?, ?, NULL), NULL, NULL))";

            // Connect to the database.
            try (Connection conn = DbUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, sid);
                ps.setString(2, name);
                ps.setString(3, type);
                ps.setDouble(4, x);
                ps.setDouble(5, y);

                ps.executeUpdate();
                conn.commit();

                // Refresh the map.
                response.sendRedirect(request.getContextPath() + "/map.jsp");
            } catch (SQLException ex) {
                ex.printStackTrace();
                setErrorAndForward(request, response,
                        "Failed to create spatial entity (point): " + ex.getMessage());
            }

        }

        // Operations if it is a rectangle.
        else if ("rectangle".equalsIgnoreCase(shape)) {
            // Rectangle geometry requires x1,y1,x2,y2
            if (x1Str == null || y1Str == null || x2Str == null || y2Str == null ||
                    x1Str.isEmpty() || y1Str.isEmpty() || x2Str.isEmpty() || y2Str.isEmpty()) {

                setErrorAndForward(request, response,
                        "Rectangle geometry requires X1, Y1, X2 and Y2 coordinates.");
                return;
            }

            Double x1 = parseAndValidateCoordinate(x1Str, "X1");
            Double y1 = parseAndValidateCoordinate(y1Str, "Y1");
            Double x2 = parseAndValidateCoordinate(x2Str, "X2");
            Double y2 = parseAndValidateCoordinate(y2Str, "Y2");
            if (x1 == null || y1 == null || x2 == null || y2 == null) {
                setErrorAndForward(request, response,
                        "Invalid rectangle coordinates (must be numeric in range 0–500).");
                return;
            }

            sql = "INSERT INTO SEntities (SID, entity_Name, entity_Type, geometry) " +
                    "VALUES (?, ?, ?, " +
                    "SDO_GEOMETRY(2003, NULL, NULL, " +
                    "SDO_ELEM_INFO_ARRAY(1,1003,3), " +
                    "SDO_ORDINATE_ARRAY(?, ?, ?, ?)))";

            try (Connection conn = DbUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, sid);
                ps.setString(2, name);
                ps.setString(3, type);
                ps.setDouble(4, x1);
                ps.setDouble(5, y1);
                ps.setDouble(6, x2);
                ps.setDouble(7, y2);

                ps.executeUpdate();
                conn.commit();

                response.sendRedirect(request.getContextPath() + "/map.jsp");
            } catch (SQLException ex) {
                ex.printStackTrace();
                setErrorAndForward(request, response,
                        "Failed to create spatial entity (rectangle): " + ex.getMessage());
            }

        } else {
            setErrorAndForward(request, response,
                    "Unsupported shape: " + shape + ". Use 'point' or 'rectangle'.");
        }
    }

    // Trim helper.
    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    // Parse and ensure coordinate is numeric and in range [0,500].
    private Double parseAndValidateCoordinate(String text, String label) {
        try {
            double v = Double.parseDouble(text);
            if (v < 0.0 || v > 500.0) {
                System.err.println("Coordinate " + label + " out of range: " + v);
                return null;
            }
            return v;
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse coordinate " + label + ": " + text);
            return null;
        }
    }

    // Set error message and forward back to map.jsp.
    private void setErrorAndForward(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String msg) throws ServletException, IOException {
        request.setAttribute("error", msg);
        request.getRequestDispatcher("map.jsp").forward(request, response);
    }
}
