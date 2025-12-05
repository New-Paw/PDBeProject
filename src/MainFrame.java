import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private JTextField searchField;
    private MapPanel mapPanel;
    private List<MapRecord> allRecords;
    private List<MapRecord> filtered;

    public MainFrame() {
        setLookAndFeel();
        initData();
        initUI();
    }

    // Set the appearance.
    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}
    }

    // Initialize the local data.(Just used to test)
    private void initData() {
        allRecords = new ArrayList<>();
        allRecords.add(new MapRecord(1, "Panda House", "building", 100, 400, "panda.jpg", new Date()));
        allRecords.add(new MapRecord(2, "Aquarium", "building", 320, 420, "fish.png", null));
        allRecords.add(new MapRecord(3, "Fountain", "fountain", 240, 320, "", null));
        allRecords.add(new MapRecord(4, "Bridge", "bridge", 280, 260, null, null));
        allRecords.add(new MapRecord(5, "Flower Garden", "garden", 160, 200, "", null));
        allRecords.add(new MapRecord(6, "Entrance", "gate", 80, 140, "", null));

        filtered = new ArrayList<>(allRecords);
    }

    // Build the main window interface.
    private void initUI() {
        setTitle("Zoo Map");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 247, 255));

        // Top title.
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        header.setOpaque(false);

        JLabel title = new JLabel(" 🐼 Zoo Map");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(new Color(111, 61, 188));
        header.add(title, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // Search bar.
        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setOpaque(false);
        searchWrapper.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));

        // Wrap the container of the search bar, leaving left and right space.
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(244, 240, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JLabel icon = new JLabel("🔍");
        searchPanel.add(icon, BorderLayout.WEST);

        // The setting of the searchBar.
        searchField = new JTextField();
        searchField.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        searchField.setOpaque(false);
        searchField.setFont(searchField.getFont().deriveFont(13f));
        searchField.setToolTipText("Please enter text to filter.");
        searchPanel.add(searchField, BorderLayout.CENTER);

        // Clear button.
        JButton clearBtn = new JButton("×");
        clearBtn.setMargin(new Insets(1, 6, 1, 6));
        clearBtn.setFocusable(false);
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            onSearch(null);
        });
        searchPanel.add(clearBtn, BorderLayout.EAST);

        searchWrapper.add(searchPanel, BorderLayout.CENTER);
        add(searchWrapper, BorderLayout.BEFORE_FIRST_LINE);

        searchField.addActionListener(this::onSearch);

        // Map panel.
        mapPanel = new MapPanel(filtered);
        mapPanel.setPreferredSize(new Dimension(400, 600));
        mapPanel.setMarkerClickListener(rec -> {
            InfoDialog dialog = new InfoDialog(this, rec);
            dialog.setVisible(true);
        });

        // Edge wrapping.
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(4, 10, 16, 10));
        centerWrapper.add(mapPanel, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);
    }

    // Search filtering logic.
    private void onSearch(ActionEvent e) {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            filtered = new ArrayList<>(allRecords);
        } else {
            filtered = allRecords.stream()
                    .filter(r -> r.getName() != null &&
                                 r.getName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }
        mapPanel.setRecords(filtered);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
