import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class EditDialog extends JDialog {

    private final MapRecord rec;    // Get data from local file.
    private final Runnable onSaved;
    private JTextField nameField;
    private JTextField imageField;
    private JSpinner dateSpinner;

    // Constructor
    public EditDialog(Frame owner, MapRecord rec, Runnable onSaved) {
        super(owner, "Edit location information", true);
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

        // Top title.
        JLabel title = new JLabel("Edit location information");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(new Color(111, 61, 188));
        card.add(title, BorderLayout.NORTH);

        // The middle part.
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 0, 6, 0);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.weightx = 1.0;

        // The name of the location.
        nameField = new JTextField(rec.getName(), 20);
        addFormRow(form, gc, "Location Name", nameField);

        // Information of the image.
        imageField = new JTextField(
                rec.getImagePath() == null ? "" : rec.getImagePath(), 20);
        imageField.setToolTipText("You can fill in the picture description or file name");
        addFormRow(form, gc, "Picture Information", imageField);

        // Data of time.
        Date initial = rec.getVisitTime() != null ? rec.getVisitTime() : new Date();
        SpinnerDateModel model = new SpinnerDateModel(initial, null, null,
                java.util.Calendar.MINUTE);
        dateSpinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner,
                "yyyy-MM-dd HH:mm");
        dateSpinner.setEditor(editor);
        addFormRow(form, gc, "Datastamp", dateSpinner);

        card.add(form, BorderLayout.CENTER);

        // The button.
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        JButton cancelBtn = createPrimaryButton("Cancel");
        JButton confirmBtn = createPrimaryButton("Confirm");
        bottom.add(cancelBtn);
        bottom.add(confirmBtn);
        card.add(bottom, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dispose());
        confirmBtn.addActionListener(e -> onConfirm());
    }

    // Control boundary range.
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

    // The method to update the data.
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

    // Same method like in InforDialog.
    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(118, 99, 255));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        return btn;
    }

    // Same like in InforDialog to define a class to make the panel softer.
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
