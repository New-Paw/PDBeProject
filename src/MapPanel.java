import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.List;

public class MapPanel extends JPanel {

    // The listener to call the InforDialog.
    public interface MarkerClickListener {
        void onMarkerClicked(MapRecord rec);
    }

    private List<MapRecord> records;
    private MarkerClickListener listener;

    // The map range
    private static final double DATA_MIN = 0.0;
    private static final double DATA_MAX = 500.0;
    private MapRecord selected;

    // Constructor
    public MapPanel(List<MapRecord> records) {
        this.records = records;
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    // Connect to the MainFrame.
    public void setMarkerClickListener(MarkerClickListener listener) {
        this.listener = listener;
    }

    // Refresh the map.
    public void setRecords(List<MapRecord> records) {
        this.records = records;
        selected = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (records == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background
        GradientPaint bg = new GradientPaint(0, 0,
                new Color(245, 242, 255),
                0, h,
                new Color(232, 234, 252));
        g2.setPaint(bg);
        g2.fillRoundRect(10, 10, w - 20, h - 20, 30, 30);

        // The map.
        Shape inner = new Rectangle(30, 30, w - 60, h - 80);
        g2.setColor(new Color(255, 255, 255, 210));
        g2.fill(inner);

        // Just used to local show, not usefull to the database.
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(220, 220, 235));
        g2.drawLine(60, h - 150, w - 100, 80);
        g2.drawLine(80, 80, 80, h - 140);
        g2.drawLine(w - 120, 120, w - 120, h - 120);

        // The position with some colorful attributes.
        int r = 9; 

        for (MapRecord rec : records) {
            Point p = dataToScreen(rec.getX(), rec.getY(), w, h);

            Color color = getColorByType(rec.getType());
            boolean isSelected = (selected != null && selected.getId() == rec.getId());
            drawPin(g2, p.x, p.y, color, isSelected);

            // The lable of name.
            String name = rec.getName();
            if (name != null && !name.isEmpty()) {
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(name);
                int tx = p.x - textWidth / 2;
                int ty = p.y + r + fm.getAscent() + 2;

                g2.setColor(new Color(255, 255, 255, 230));
                g2.fillRoundRect(tx - 4, ty - fm.getAscent(),
                        textWidth + 8, fm.getHeight(), 12, 12);
                g2.setColor(new Color(120, 120, 150));
                g2.drawString(name, tx, ty);
            }
        }

        g2.dispose();
    }

    // Determine which pin the user has clicked.
    private void handleClick(int mx, int my) {
        if (records == null) return;

        int width = getWidth();
        int height = getHeight();
        int clickRadius = 12;

        for (MapRecord rec : records) {
            Point p = dataToScreen(rec.getX(), rec.getY(), width, height);
            if (p.distance(mx, my) <= clickRadius) {
                selected = rec;
                repaint();
                if (listener != null) {
                    listener.onMarkerClicked(rec);
                }
                break;
            }
        }
    }

    // Display the data on the screen.
    private Point dataToScreen(double x, double y, int width, int height) {
        int left = 40, right = 40, top = 50, bottom = 80;
        double usableW = width - left - right;
        double usableH = height - top - bottom;

        double scaleX = usableW / (DATA_MAX - DATA_MIN);
        double scaleY = usableH / (DATA_MAX - DATA_MIN);
        double scale = Math.min(scaleX, scaleY);

        int sx = (int) Math.round(left + (x - DATA_MIN) * scale);
        int sy = (int) Math.round(height - bottom - (y - DATA_MIN) * scale); // 倒置 Y

        return new Point(sx, sy);
    }

    // Give each pin a different color to distinguish the locations.
    private Color getColorByType(String type) {
        if (type == null) return new Color(118, 99, 255);
        String t = type.toLowerCase();
        if (t.contains("building") || t.contains("house")) {
            return new Color(118, 99, 255); // Purple
        } else if (t.contains("fountain") || t.contains("water")) {
            return new Color(76, 178, 255); // Blue
        } else if (t.contains("garden") || t.contains("flower")) {
            return new Color(122, 201, 67); // Green
        } else if (t.contains("bridge")) {
            return new Color(255, 171, 64); // Orange
        } else {
            return new Color(140, 158, 255); // Default
        }
    }

    // The method to draw the pin.
    private void drawPin(Graphics2D g2, int x, int y, Color base, boolean selected) {
        int size = selected ? 14 : 11;
        int r = size / 2;

        // Create shadow.
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(x - r, y - r + 6, size, size / 2);

        // Pin
        GeneralPath path = new GeneralPath();
        path.moveTo(x, y - r);
        path.curveTo(x + r, y - r, x + r, y, x, y + r + 2);
        path.curveTo(x - r, y, x - r, y - r, x, y - r);
        g2.setColor(base);
        g2.fill(path);

        // The white point in the pin.
        g2.setColor(Color.WHITE);
        g2.fillOval(x - r / 2, y - r / 2, r, r);
    }
}
