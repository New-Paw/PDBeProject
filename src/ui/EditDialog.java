package ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditDialog extends JDialog {

    private final DatabaseManager db;
    private final MapRecord rec;

    private JLabel imageLabel;
    private JTextField nameField;
    private JSpinner dateSpinner;

    // 记录当前选择的图片路径
    private String currentImagePath;

    public EditDialog(Frame owner, DatabaseManager db, MapRecord rec) {
        super(owner, "Edit position info - " + rec.getName(), true);
        this.db = db;
        this.rec = rec;
        this.currentImagePath = rec.getImagePath();

        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setContentPane(content);

        // ===== 1. Select the picture =====
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(400, 200));
        refreshImagePreview();

        JButton chooseImageBtn = new JButton("选择图片...");
        chooseImageBtn.addActionListener(e -> onChooseImage());

        topPanel.add(imageLabel, BorderLayout.CENTER);
        topPanel.add(chooseImageBtn, BorderLayout.SOUTH);

        content.add(topPanel, BorderLayout.NORTH);

        // ===== 2. Information and time =====
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Posi-Info: "));
        nameField = new JTextField(rec.getName(), 20);
        namePanel.add(nameField);
        centerPanel.add(namePanel);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new JLabel("Time stamp: "));

        Date initialDate = (rec.getVisitTime() != null) ? rec.getVisitTime() : new Date();
        SpinnerDateModel dateModel = new SpinnerDateModel(initialDate, null, null, java.util.Calendar.MINUTE);
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd HH:mm");
        dateSpinner.setEditor(editor);

        timePanel.add(dateSpinner);
        centerPanel.add(timePanel);

        content.add(centerPanel, BorderLayout.CENTER);

        // ===== 3.Cancel / Confirm buttons=====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        JButton confirmBtn = new JButton("Confirm");

        bottomPanel.add(cancelBtn);
        bottomPanel.add(confirmBtn);

        content.add(bottomPanel, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dispose());
        confirmBtn.addActionListener(e -> onConfirm());
    }

    // Set the picture
    private void onChooseImage() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            currentImagePath = file.getAbsolutePath();
            refreshImagePreview();
        }
    }

   // Refresh the picture
    private void refreshImagePreview() {
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            ImageIcon icon = new ImageIcon(currentImagePath);
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

   // Update the database
    private void onConfirm() {
        try {
            rec.setName(nameField.getText().trim());
            rec.setImagePath(currentImagePath);
            rec.setVisitTime((Date) dateSpinner.getValue());

            db.updateRecord(rec);

            JOptionPane.showMessageDialog(this, "Success！");
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Fail update: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
