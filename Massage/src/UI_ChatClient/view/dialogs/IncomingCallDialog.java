package UI_ChatClient.view.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import UI_ChatClient.view.components.RoundButton;
import UI_ChatClient.view.utils.IconUtils;

/**
 * Dialog hiển thị khi có cuộc gọi đến
 */
public class IncomingCallDialog extends JDialog {
    private boolean accepted = false;
    private javax.swing.Timer waveTimer;
    private int waveOffset = 0;
    
    public IncomingCallDialog(JFrame owner, String callerName, boolean isVideo) {
        super(owner, "Cuộc gọi đến", true);
        setResizable(true);
        setMinimumSize(new Dimension(300, 400));
        
        // Panel chính với gradient xanh ngọc nhạt
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient từ trắng sang xanh ngọc nhạt
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255),
                    0, getHeight(), new Color(94, 234, 212)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                
                // Hiệu ứng sóng nhẹ - vòng tròn xanh ngọc
                g2.setColor(new Color(94, 234, 212, 60));
                for (int i = 0; i < 3; i++) {
                    int offset = (waveOffset + i * 20) % 60;
                    g2.drawRoundRect(offset/2, offset/2, getWidth() - offset, getHeight() - offset, 30, 30);
                }
                
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new javax.swing.border.EmptyBorder(30, 30, 30, 30));
        
        // Hiệu ứng sóng
        waveTimer = new javax.swing.Timer(100, e -> {
            waveOffset = (waveOffset + 5) % 60;
            mainPanel.repaint();
        });
        waveTimer.start();
        
        // Header: Loại cuộc gọi
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        
        JLabel lblCallType = new JLabel(isVideo ? "📹 Incomming video call" : "📞 Incomming call");
        lblCallType.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        lblCallType.setForeground(new Color(19, 78, 74));
        headerPanel.add(lblCallType);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Phần chính: Thông tin người gọi
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new javax.swing.border.EmptyBorder(15, 0, 15, 0));
        
        // Avatar
        JPanel avatarContainer = new JPanel();
        avatarContainer.setOpaque(false);
        avatarContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        avatarContainer.setPreferredSize(new Dimension(120, 120));
        avatarContainer.setMaximumSize(new Dimension(120, 120));
        avatarContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblAvatar = new JLabel();
        lblAvatar.setIcon(IconUtils.loadAndScaleIcon("avatar.jpg", 100, 100));
        avatarContainer.add(lblAvatar);
        
        JLabel lblName = new JLabel(callerName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblName.setForeground(new Color(19, 78, 74));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblName.setBorder(new javax.swing.border.EmptyBorder(15, 0, 5, 0));
        
        JLabel lblStatus = new JLabel("đang gọi cho bạn...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 15));
        lblStatus.setForeground(new Color(45, 212, 191));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        infoPanel.add(avatarContainer);
        infoPanel.add(lblName);
        infoPanel.add(lblStatus);
        
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Nút trả lời / từ chối
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new javax.swing.border.EmptyBorder(10, 0, 15, 0));
        
        // Nút Từ Chối
        JPanel declinePanel = new JPanel();
        declinePanel.setLayout(new BoxLayout(declinePanel, BoxLayout.Y_AXIS));
        declinePanel.setOpaque(false);
        
        RoundButton btnDecline = new RoundButton(IconUtils.loadAndScaleIcon("hangup.png", 32, 32), new Color(239, 68, 68));
        btnDecline.setPreferredSize(new Dimension(65, 65));
        btnDecline.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDecline.addActionListener(e -> {
            accepted = false;
            closeDialog();
        });
        
        JLabel lblDecline = new JLabel("Từ chối");
        lblDecline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDecline.setForeground(new Color(19, 78, 74));
        lblDecline.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDecline.setBorder(new javax.swing.border.EmptyBorder(8, 0, 0, 0));
        
        declinePanel.add(btnDecline);
        declinePanel.add(lblDecline);
        
        // Nút Trả Lời
        JPanel acceptPanel = new JPanel();
        acceptPanel.setLayout(new BoxLayout(acceptPanel, BoxLayout.Y_AXIS));
        acceptPanel.setOpaque(false);
        
        String acceptIconName = isVideo ? "video.png" : "call.png";
        RoundButton btnAccept = new RoundButton(IconUtils.loadAndScaleIcon(acceptIconName, 32, 32), new Color(20, 184, 166));
        btnAccept.setPreferredSize(new Dimension(65, 65));
        btnAccept.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAccept.addActionListener(e -> {
            accepted = true;
            closeDialog();
        });
        
        JLabel lblAccept = new JLabel("Trả lời");
        lblAccept.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblAccept.setForeground(new Color(19, 78, 74));
        lblAccept.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAccept.setBorder(new javax.swing.border.EmptyBorder(8, 0, 0, 0));
        
        acceptPanel.add(btnAccept);
        acceptPanel.add(lblAccept);
        
        buttonPanel.add(declinePanel);
        buttonPanel.add(acceptPanel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        setSize(380, 480);
        setLocationRelativeTo(owner);
    }
    
    private void closeDialog() {
        if (waveTimer != null) waveTimer.stop();
        dispose();
    }
    
    public boolean isAccepted() { return accepted; }
}
