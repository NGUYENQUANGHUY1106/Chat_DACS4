package UI_ChatClient.view.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog để chọn nguồn chia sẻ màn hình (toàn màn hình hoặc cửa sổ cụ thể)
 */
public class ScreenShareDialog extends JDialog {
    private GraphicsDevice selectedScreen = null;
    private Window selectedWindow = null;
    private boolean shareFullScreen = false;
    private boolean confirmed = false;
    
    private final Color darkBgColor = new Color(240, 253, 250);
    private final Color cardBgColor = new Color(255, 255, 255);
    private final Color accentGreen = new Color(16, 185, 129);
    private final Color textPrimary = new Color(19, 78, 74);
    
    public ScreenShareDialog(JFrame parent) {
        super(parent, "Chọn nguồn chia sẻ màn hình", true);
        setSize(600, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(darkBgColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Chọn nội dung để chia sẻ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(textPrimary);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Content panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Tab 1: Toàn màn hình
        JPanel screenPanel = createScreenPanel();
        tabbedPane.addTab("🖥️ Màn hình", screenPanel);
        
        // Tab 2: Cửa sổ
        JPanel windowPanel = createWindowPanel();
        tabbedPane.addTab("🪟 Cửa sổ", windowPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(darkBgColor);
        
        JButton btnCancel = createStyledButton("Hủy", Color.GRAY);
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        JButton btnShare = createStyledButton("Chia sẻ", accentGreen);
        btnShare.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnShare);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createScreenPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(cardBgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel infoLabel = new JLabel("Chọn màn hình để chia sẻ:");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(textPrimary);
        panel.add(infoLabel, BorderLayout.NORTH);
        
        // List các màn hình
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        
        JPanel screensPanel = new JPanel();
        screensPanel.setLayout(new BoxLayout(screensPanel, BoxLayout.Y_AXIS));
        screensPanel.setBackground(cardBgColor);
        
        ButtonGroup group = new ButtonGroup();
        
        for (int i = 0; i < screens.length; i++) {
            GraphicsDevice screen = screens[i];
            DisplayMode mode = screen.getDisplayMode();
            String screenInfo = String.format("Màn hình %d - %dx%d", 
                i + 1, mode.getWidth(), mode.getHeight());
            
            JRadioButton radioBtn = new JRadioButton(screenInfo);
            radioBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            radioBtn.setBackground(cardBgColor);
            radioBtn.setForeground(textPrimary);
            
            final GraphicsDevice selectedDevice = screen;
            radioBtn.addActionListener(e -> {
                selectedScreen = selectedDevice;
                shareFullScreen = true;
                selectedWindow = null;
            });
            
            if (i == 0) {
                radioBtn.setSelected(true);
                selectedScreen = screen;
                shareFullScreen = true;
            }
            
            group.add(radioBtn);
            screensPanel.add(radioBtn);
            screensPanel.add(Box.createVerticalStrut(8));
        }
        
        JScrollPane scrollPane = new JScrollPane(screensPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(204, 251, 241), 1));
        scrollPane.getViewport().setBackground(cardBgColor);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createWindowPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(cardBgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel infoLabel = new JLabel("Chọn cửa sổ để chia sẻ:");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(textPrimary);
        panel.add(infoLabel, BorderLayout.NORTH);
        
        // List các cửa sổ đang mở
        Window[] windows = Window.getWindows();
        List<Window> visibleWindows = new ArrayList<>();
        
        for (Window window : windows) {
            if (window.isVisible() && window instanceof Frame) {
                Frame frame = (Frame) window;
                if (frame.getTitle() != null && !frame.getTitle().isEmpty()) {
                    visibleWindows.add(window);
                }
            }
        }
        
        JPanel windowsPanel = new JPanel();
        windowsPanel.setLayout(new BoxLayout(windowsPanel, BoxLayout.Y_AXIS));
        windowsPanel.setBackground(cardBgColor);
        
        ButtonGroup group = new ButtonGroup();
        
        if (visibleWindows.isEmpty()) {
            JLabel noWindowLabel = new JLabel("Không tìm thấy cửa sổ nào");
            noWindowLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noWindowLabel.setForeground(Color.GRAY);
            windowsPanel.add(noWindowLabel);
        } else {
            for (Window window : visibleWindows) {
                String windowTitle = "";
                if (window instanceof Frame) {
                    windowTitle = ((Frame) window).getTitle();
                } else if (window instanceof Dialog) {
                    windowTitle = ((Dialog) window).getTitle();
                }
                
                if (windowTitle.isEmpty()) continue;
                
                JRadioButton radioBtn = new JRadioButton(windowTitle);
                radioBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                radioBtn.setBackground(cardBgColor);
                radioBtn.setForeground(textPrimary);
                
                final Window selectedWin = window;
                radioBtn.addActionListener(e -> {
                    selectedWindow = selectedWin;
                    shareFullScreen = false;
                    selectedScreen = null;
                });
                
                group.add(radioBtn);
                windowsPanel.add(radioBtn);
                windowsPanel.add(Box.createVerticalStrut(8));
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(windowsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(204, 251, 241), 1));
        scrollPane.getViewport().setBackground(cardBgColor);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(100, 35));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    // Getters
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public boolean isShareFullScreen() {
        return shareFullScreen;
    }
    
    public GraphicsDevice getSelectedScreen() {
        return selectedScreen;
    }
    
    public Window getSelectedWindow() {
        return selectedWindow;
    }
}
