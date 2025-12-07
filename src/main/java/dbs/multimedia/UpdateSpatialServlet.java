package dbs.multimedia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/updateSpatial")
public class UpdateSpatialServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String sidStr   = trim(request.getParameter("sid"));
        String name     = trim(request.getParameter("entityName"));
        String type     = trim(request.getParameter("entityType"));
        String shape    = trim(request.getParameter("shape")); // may be "", "point", "rectangle"

        String xStr     = trim(request.getParameter("x"));
        String yStr     = trim(request.getParameter("y"));
        String x1Str    = trim(request.getParameter("x1"));
        String y1Str    = trim(request.getParameter("y1"));
        String x2Str    = trim(request.getParameter("x2"));
        String y2Str    = trim(request.getParameter("y2"));

        // Check the input is correct or not.
        if (sidStr == null || sidStr.isEmpty()) {
            setErrorAndForward(request, response, "SID is required.");
            return;
        }

        int sid;
        try {
            sid = Integer.parseInt(sidStr);
        } catch (NumberFormatException e) {
            setErrorAndForward(request, response, "SID must be an integer.");
            return;
        }

        if (name == null || name.isEmpty() || type == null || type.isEmpty()) {
            setErrorAndForward(request, response, "Name and type must not be empty.");
            return;
        }

        boolean updateGeometry = (shape != null && !shape.isEmpty());
        StringBuilder sql = new StringBuilder("UPDATE SEntities SET ");
        sql.append("entity_Name = ?, entity_Type = ?");

        // Build geometry update part.
        if (updateGeometry) {
            // For point part to update.
            if ("point".equalsIgnoreCase(shape)) {
                // Point geometry
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
                sql.append(", geometry = SDO_GEOMETRY(2001, NULL, ");
                sql.append("SDO_POINT_TYPE(?, ?, NULL), NULL, NULL)");

            }
            // For rectangle part to update.
            else if ("rectangle".equalsIgnoreCase(shape)) {
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
                sql.append(", geometry = SDO_GEOMETRY(2003, NULL, NULL, ");
                sql.append("SDO_ELEM_INFO_ARRAY(1,1003,3), ");
                sql.append("SDO_ORDINATE_ARRAY(?, ?, ?, ?))");

            } else {
                setErrorAndForward(request, response,
                        "Unsupported shape: " + shape + ". Use empty, 'point' or 'rectangle'.");
                return;
            }
        }

        sql.append(" WHERE SID = ?");

        try (Connection conn = DbUtil.getConnection();  // Update the database.
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            // name and type
            ps.setString(idx++, name);
            ps.setString(idx++, type);

            if (updateGeometry) {
                if ("point".equalsIgnoreCase(shape)) {
                    double x = Double.parseDouble(xStr);
                    double y = Double.parseDouble(yStr);
                    ps.setDouble(idx++, x);
                    ps.setDouble(idx++, y);
                } else if ("rectangle".equalsIgnoreCase(shape)) {
                    double x1 = Double.parseDouble(x1Str);
                    double y1 = Double.parseDouble(y1Str);
                    double x2 = Double.parseDouble(x2Str);
                    double y2 = Double.parseDouble(y2Str);

                    ps.setDouble(idx++, x1);
                    ps.setDouble(idx++, y1);
                    ps.setDouble(idx++, x2);
                    ps.setDouble(idx++, y2);
                }
            }

            ps.setInt(idx, sid);

            int updated = ps.executeUpdate();
            conn.commit();

            // Result handling and exception handling.
            if (updated == 0) {
                setErrorAndForward(request, response,
                        "No spatial entity found with SID = " + sid);
            } else {
                response.sendRedirect(request.getContextPath() + "/map.jsp");
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            setErrorAndForward(request, response,
                    "Failed to update spatial entity: " + e.getMessage());
        }
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    // Check whether the coordinates are in the correct format.
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
