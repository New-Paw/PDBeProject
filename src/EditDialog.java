import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class EditDialog extends JDialog {

    private final MapRecord rec;
    private final Runnable onSaved;

    private JTextField nameField;
    private JTextField imageField;
    private JSpinner dateSpinner;

    public EditDialog(Frame owner, MapRecord rec, Runnable onSaved) {
        super(owner, "编辑地点信息", true);
        this.rec = rec;
        this.onSaved = onSaved;
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(new Color(248, 247, 255));
        setContentPane(root);

        JPanel card = new RoundedPanel(20, new Color(250, 248, 255));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));
        root.add(card, BorderLayout.CENTER);

        // 顶部标题
        JLabel title = new JLabel("编辑地点信息");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(new Color(111, 61, 188));
        card.add(title, BorderLayout.NORTH);

        // 中部表单
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 0, 6, 0);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.weightx = 1.0;

        // 地点名称
        nameField = new JTextField(rec.getName(), 20);
        addFormRow(form, gc, "地点名称", nameField);

        // 图片信息
        imageField = new JTextField(
                rec.getImagePath() == null ? "" : rec.getImagePath(), 20);
        imageField.setToolTipText("可填写图片说明或文件名");
        addFormRow(form, gc, "图片信息", imageField);

        // 时间
        Date initial = rec.getVisitTime() != null ? rec.getVisitTime() : new Date();
        SpinnerDateModel model = new SpinnerDateModel(initial, null, null,
                java.util.Calendar.MINUTE);
        dateSpinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner,
                "yyyy-MM-dd HH:mm");
        dateSpinner.setEditor(editor);
        addFormRow(form, gc, "时间戳", dateSpinner);

        card.add(form, BorderLayout.CENTER);

        // 底部按钮
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        JButton cancelBtn = createGhostButton("取消");
        JButton confirmBtn = createPrimaryButton("Confirm");
        bottom.add(cancelBtn);
        bottom.add(confirmBtn);
        card.add(bottom, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dispose());
        confirmBtn.addActionListener(e -> onConfirm());
    }

    private void addFormRow(JPanel panel, GridBagConstraints gc,
                            String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(120, 120, 150));
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));

        gc.gridy++;
        gc.gridx = 0;
        gc.weightx = 0;
        panel.add(label, gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        gc.insets = new Insets(4, 16, 8, 0);
        panel.add(field, gc);
        gc.insets = new Insets(6, 0, 6, 0);
    }

    private void onConfirm() {
        rec.setName(nameField.getText().trim());
        String img = imageField.getText().trim();
        rec.setImagePath(img.isEmpty() ? null : img);
        rec.setVisitTime((Date) dateSpinner.getValue());

        if (onSaved != null) {
            onSaved.run();
        }
        dispose();
    }

    // ===== 与 InfoDialog 一致的样式辅助 =====

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(118, 99, 255));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        return btn;
    }

    private JButton createGhostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(new Color(118, 99, 255));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(210, 205, 255)),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)));
        return btn;
    }

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
