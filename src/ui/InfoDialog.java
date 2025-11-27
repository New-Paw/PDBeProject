package ui;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class InfoDialog extends JDialog {

    private final DatabaseManager db;
    private final MapRecord rec;

    private JLabel imageLabel;
    private JLabel locLabel;
    private JLabel timeLabel;

    public InfoDialog(Frame owner, DatabaseManager db, MapRecord rec) {
        super(owner, "Position name" + rec.getName(), true);
        this.db = db;
        this.rec = rec;
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setContentPane(content);

        // ===== 1. Picture =====
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(400, 200));
        refreshImage();

        content.add(imageLabel, BorderLayout.NORTH);

        // ===== 2. Text =====
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        locLabel = new JLabel();
        timeLabel = new JLabel();

        refreshText();

        infoPanel.add(locLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(timeLabel);

        content.add(infoPanel, BorderLayout.CENTER);

        // ===== 3. Buttons =====
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editBtn = new JButton("Edit");
        JButton quitBtn = new JButton("退出");

        btnPanel.add(editBtn);
        btnPanel.add(quitBtn);
        content.add(btnPanel, BorderLayout.SOUTH);

        quitBtn.addActionListener(e -> dispose());

        // When open the edit
        editBtn.addActionListener(e -> {
            EditDialog ed = new EditDialog((Frame) getOwner(), db, rec);
            ed.setVisible(true);
            // Refresh
            refreshImage();
            refreshText();
            setTitle("地点信息 - " + rec.getName());
        });
    }

    private void refreshImage() {
        String imagePath = rec.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            ImageIcon icon = new ImageIcon(imagePath);
            Image img = icon.getImage();
            int newWidth = 400;
            int newHeight = img.getHeight(null) * newWidth / img.getWidth(null);
            Image scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
            imageLabel.setText(null);
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("NO PICTURE");
        }
    }

    private void refreshText() {
        String locText = String.format("Info: %s  (position: %.1f, %.1f)",
                rec.getName(), rec.getX(), rec.getY());
        locLabel.setText(locText);

        String timeText = "Time: ";
        if (rec.getVisitTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            timeText += sdf.format(rec.getVisitTime());
        } else {
            timeText += "NO RECORD";
        }
        timeLabel.setText(timeText);
    }

    // Just for test
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseManager db = new DatabaseManager();
                MapRecord rec = db.getRecordByName("panda_House");
                if (rec == null) {
                    JOptionPane.showMessageDialog(null, "未找到 panda_House 记录");
                    return;
                }
                InfoDialog dialog = new InfoDialog(null, db, rec);
                dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                dialog.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
