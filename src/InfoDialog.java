import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class InfoDialog extends JDialog {

    private final MapRecord rec;
    private JLabel imageArea;
    private JLabel nameLabel;
    private JLabel coordLabel;
    private JLabel timeLabel;

    // Title
    public InfoDialog(Frame owner, MapRecord rec) {
        super(owner, "PositionInformation", true);
        this.rec = rec;
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    // The information UI
    private void initUI() {
        setUndecorated(false);
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(new Color(248, 247, 255));
        setContentPane(root);

        // Main panel
        JPanel card = new RoundedPanel(20, new Color(250, 248, 255));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.add(card, BorderLayout.CENTER);

        // ===== 顶部：图片占位区域 =====
        imageArea = new JLabel("", SwingConstants.CENTER);
        imageArea.setPreferredSize(new Dimension(380, 150));
        imageArea.setOpaque(false);
        imageArea.setFont(imageArea.getFont().deriveFont(Font.PLAIN, 14f));
        imageArea.setForeground(new Color(120, 120, 150));

        // 再包一层圆角背景
        JPanel imageWrapper = new RoundedPanel(18, new Color(240, 237, 255));
        imageWrapper.setLayout(new BorderLayout());
        imageWrapper.add(imageArea, BorderLayout.CENTER);
        card.add(imageWrapper, BorderLayout.NORTH);

        // ===== 中部：文字信息 =====
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        card.add(center, BorderLayout.CENTER);

        nameLabel = new JLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 15f));
        nameLabel.setForeground(new Color(68, 44, 121));

        coordLabel = new JLabel();
        coordLabel.setFont(coordLabel.getFont().deriveFont(Font.PLAIN, 13f));
        coordLabel.setForeground(new Color(120, 120, 150));

        timeLabel = new JLabel();
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, 13f));
        timeLabel.setForeground(new Color(120, 120, 150));

        center.add(nameLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(coordLabel);
        center.add(Box.createVerticalStrut(4));
        center.add(timeLabel);

        // ===== 底部：按钮区 =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        JButton editBtn = createPrimaryButton("Edit");
        JButton closeBtn = createGhostButton("关闭");
        bottom.add(closeBtn);
        bottom.add(editBtn);
        card.add(bottom, BorderLayout.SOUTH);

        // ===== 填充文本内容 =====
        refreshContent();

        // 事件
        closeBtn.addActionListener(e -> dispose());
        editBtn.addActionListener(e -> {
            EditDialog ed = new EditDialog((Frame) getOwner(), rec, () -> {
                refreshContent();                  // 编辑保存后刷新信息
                setTitle("地点信息 - " + rec.getName());
            });
            ed.setVisible(true);
        });
    }

    private void refreshContent() {
        // “图片信息”用文字占位
        String imgText = rec.getImagePath() == null || rec.getImagePath().isEmpty()
                ? "暂无图片信息（可在 Edit 中添加说明或文件名）"
                : "图片信息: " + rec.getImagePath();
        imageArea.setText(imgText);

        nameLabel.setText("地点: " + rec.getName());

        coordLabel.setText(String.format("坐标:  x = %.1f,  y = %.1f",
                rec.getX(), rec.getY()));

        String timeText = "时间: ";
        if (rec.getVisitTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            timeText += sdf.format(rec.getVisitTime());
        } else {
            timeText += "无记录";
        }
        timeLabel.setText(timeText);
    }

    // ===== 辅助：统一按钮样式 & 圆角面板 =====

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(118, 99, 255));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        return btn;
    }

    private JButton createGhostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(new Color(118, 99, 255));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(210, 205, 255)),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        return btn;
    }

    // 用于卡片背景的圆角 panel
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

    // 边框：用于浅色描边按钮
    private static class RoundedBorder implements javax.swing.border.Border {
        private final Color color;

        public RoundedBorder(Color color) {
            this.color = color;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 10, 4, 10);
        }

        @Override
        public boolean isBorderOpaque() { return false; }

        @Override
        public void paintBorder(Component c, Graphics g,
                                int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, 14, 14);
            g2.dispose();
        }
    }
}
