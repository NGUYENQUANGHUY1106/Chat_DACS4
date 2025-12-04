package UI_ChatClient.view.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import UI_ChatClient.view.components.RoundButton;
import UI_ChatClient.view.utils.IconUtils;

/**
 * Dialog hiển thị khi đang gọi đi (chờ người nhận)
 */
public class OutgoingCallDialog extends JDialog {
    private boolean cancelled = false;
    private javax.swing.Timer pulseTimer;
    private int pulseAlpha = 0;
    private boolean pulseIncreasing = true;
    
    public OutgoingCallDialog(JFrame owner, String calleeName, boolean isVideo) {
        super(owner, "Đang gọi...", false);
        setResizable(true);
        setMinimumSize(new Dimension(300, 400));
        
        // Panel chính với gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient từ tím đậm sang xanh indigo
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(99, 102, 241),
                    0, getHeight(), new Color(79, 70, 229)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                
                // Hiệu ứng pulse xung quanh
                g2.setColor(new Color(255, 255, 255, pulseAlpha));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(5, 5, getWidth()-10, getHeight()-10, 30, 30);
                
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new javax.swing.border.EmptyBorder(30, 30, 30, 30));
        
        // Hiệu ứng pulse
        pulseTimer = new javax.swing.Timer(30, e -> {
            if (pulseIncreasing) {
                pulseAlpha += 5;
                if (pulseAlpha >= 100) pulseIncreasing = false;
            } else {
                pulseAlpha -= 5;
                if (pulseAlpha <= 20) pulseIncreasing = true;
            }
            mainPanel.repaint();
        });
        pulseTimer.start();
        
        // Phần chính: Thông tin người được gọi
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new javax.swing.border.EmptyBorder(20, 0, 20, 0));
        
        // Avatar
        JPanel avatarContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vòng tròn phát sáng
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                g2.setColor(new Color(255, 255, 255, pulseAlpha / 2));
                g2.fillOval(centerX - 55, centerY - 55, 110, 110);
                
                g2.dispose();
            }
        };
        avatarContainer.setOpaque(false);
        avatarContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        avatarContainer.setPreferredSize(new Dimension(120, 120));
        avatarContainer.setMaximumSize(new Dimension(120, 120));
        avatarContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblAvatar = new JLabel();
        lblAvatar.setIcon(IconUtils.loadAndScaleIcon("avatar.jpg", 100, 100));
        avatarContainer.add(lblAvatar);
        
        JLabel lblName = new JLabel(calleeName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblName.setForeground(Color.WHITE);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblName.setBorder(new javax.swing.border.EmptyBorder(20, 0, 8, 0));
        
        JLabel lblStatus = new JLabel("Đang đổ chuông...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblStatus.setForeground(new Color(200, 200, 255));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Icon loại cuộc gọi
        JLabel lblCallType = new JLabel();
        lblCallType.setIcon(IconUtils.loadAndScaleIcon(isVideo ? "video.png" : "call.png", 24, 24));
        lblCallType.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblCallType.setBorder(new javax.swing.border.EmptyBorder(15, 0, 0, 0));
        
        infoPanel.add(avatarContainer);
        infoPanel.add(lblName);
        infoPanel.add(lblStatus);
        infoPanel.add(lblCallType);
        
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Nút Hủy cuộc gọi
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new javax.swing.border.EmptyBorder(10, 0, 10, 0));
        
        RoundButton btnCancel = new RoundButton(IconUtils.loadAndScaleIcon("hangup.png", 32, 32), new Color(231, 76, 60));
        btnCancel.setPreferredSize(new Dimension(70, 70));
        btnCancel.setToolTipText("Hủy cuộc gọi");
        btnCancel.addActionListener(e -> {
            cancelled = true;
            closeDialog();
        });
        
        buttonPanel.add(btnCancel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        setSize(380, 480);
        setLocationRelativeTo(owner);
        
        // Đóng dialog khi đóng cửa sổ
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelled = true;
                closeDialog();
            }
        });
    }
    
    public void closeDialog() {
        if (pulseTimer != null) pulseTimer.stop();
        dispose();
    }
    
    public boolean isCancelled() { return cancelled; }
}
