package ui;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class InfoDialog extends JDialog {
    
    public InfoDialog(Frame owner, MapMedia media) {
        super(owner, "地点信息 - " + media.getName(), true);
        initUI(media);
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI(MapMedia media) {
        JPanel content = new JPanel(new BorderLayout(10,10));
        content.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        setContentPane(content);

        // ===== 图片 =====
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(400, 200));

        if (media.getImage() != null) {
            Image img = media.getImage();
            int newWidth = 400;
            int newHeight = img.getHeight(null) * newWidth / img.getWidth(null);
            Image scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
        } else {
            imageLabel.setText("暂无图片");
        }
        content.add(imageLabel, BorderLayout.NORTH);

        // ===== 地点+时间 =====
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        String locText = String.format("地点坐标: (%.1f, %.1f)", media.getX(), media.getY());
        JLabel locLabel = new JLabel(locText);

        String timeText = "时间: ";
        if (media.getTokenTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            timeText += sdf.format(media.getTokenTime());
        } else {
            timeText += "无记录";
        }
        JLabel timeLabel = new JLabel(timeText);

        infoPanel.add(locLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(timeLabel);
        content.add(infoPanel, BorderLayout.CENTER);

        // ===== 底部按钮 =====
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editBtn = new JButton("Edit");
        JButton quitBtn = new JButton("退出");
        btnPanel.add(editBtn);
        btnPanel.add(quitBtn);
        content.add(btnPanel, BorderLayout.SOUTH);

        quitBtn.addActionListener(e -> dispose());
        editBtn.addActionListener(e -> {
            // TODO: 打开编辑窗口，把 media 传过去
        });
    }

    // 简单测试：根据名字从 DB 取出 panda_House 并显示
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseManager db = new DatabaseManager();
                MapMedia media = db.loadMediaByName("panda_House");
                if (media == null) {
                    JOptionPane.showMessageDialog(null, "找不到该地点的记录");
                    return;
                }
                InfoDialog dialog = new InfoDialog(null, media);
                dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                dialog.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}

