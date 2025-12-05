import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class InfoDialog extends JDialog {

    private final MapRecord rec;
    private JLabel imageArea;
    private JLabel nameLabel;
    private JLabel coordLabel;
    private JLabel timeLabel;

    // Constructor
    public InfoDialog(Frame owner, MapRecord rec) {
        super(owner, "PositionInformation", true);
        this.rec = rec;
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    // Construct the UI.
    private void initUI() {
        setUndecorated(false);
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(new Color(248, 247, 255));
        setContentPane(root);

        // Main panel.
        JPanel card = new RoundedPanel(20, new Color(250, 248, 255));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.add(card, BorderLayout.CENTER);

        // The image place.
        imageArea = new JLabel("", SwingConstants.CENTER);
        imageArea.setPreferredSize(new Dimension(380, 150));
        imageArea.setOpaque(false);
        imageArea.setFont(imageArea.getFont().deriveFont(Font.PLAIN, 14f));
        imageArea.setForeground(new Color(120, 120, 150));

        JPanel imageWrapper = new RoundedPanel(18, new Color(240, 237, 255));
        imageWrapper.setLayout(new BorderLayout());
        imageWrapper.add(imageArea, BorderLayout.CENTER);
        card.add(imageWrapper, BorderLayout.NORTH);

        // The text information part.
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        card.add(center, BorderLayout.CENTER);

        nameLabel = new JLabel();   // Name part.
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 15f));
        nameLabel.setForeground(new Color(68, 44, 121));

        coordLabel = new JLabel();  // Coordinate part.
        coordLabel.setFont(coordLabel.getFont().deriveFont(Font.PLAIN, 13f));
        coordLabel.setForeground(new Color(120, 120, 150));

        timeLabel = new JLabel();   // Data part.
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, 13f));
        timeLabel.setForeground(new Color(120, 120, 150));

        center.add(nameLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(coordLabel);
        center.add(Box.createVerticalStrut(4));
        center.add(timeLabel);

        // Button part.
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        JButton editBtn = createPrimaryButton("Edit");
        JButton closeBtn = createPrimaryButton("Close");
        bottom.add(editBtn);
        bottom.add(closeBtn);
        card.add(bottom, BorderLayout.SOUTH);

        refreshContent();   // Keep refresh the panel.

        closeBtn.addActionListener(e -> dispose());
        editBtn.addActionListener(e -> {
            EditDialog ed = new EditDialog((Frame) getOwner(), rec, () -> {
                refreshContent();   // Refresh the panel after edit.         
                setTitle("Name - " + rec.getName());
            });
            ed.setVisible(true);
        });
    }

    // Define the method to refresh the text.
    private void refreshContent() {
        // "Image information" is occupied by text.
        String imgText = rec.getImagePath() == null || rec.getImagePath().isEmpty()
                ? "No image（You can add image by editing）"
                : "Image Information: " + rec.getImagePath();
        imageArea.setText(imgText);

        nameLabel.setText("Location: " + rec.getName());

        coordLabel.setText(String.format("Coordinate:  x = %.1f,  y = %.1f",
                rec.getX(), rec.getY()));

        String timeText = "Data: ";
        if (rec.getVisitTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            timeText += sdf.format(rec.getVisitTime());
        } else {
            timeText += "No recored";
        }
        timeLabel.setText(timeText);
    }

    // Make a method to create a clearer button.
    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(118, 99, 255));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        return btn;
    }

    // Define a class to make the panel softer.
    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}