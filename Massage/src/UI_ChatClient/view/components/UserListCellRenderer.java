package UI_ChatClient.view.components;

import javax.swing.*;
import java.awt.*;
import UI_ChatClient.model.UserDisplay;
import UI_ChatClient.model.Constants;
import UI_ChatClient.view.utils.IconUtils;

/**
 * Renderer tùy chỉnh cho JList hiển thị danh sách người dùng
 */
public class UserListCellRenderer extends JPanel implements ListCellRenderer<UserDisplay> {
    private JLabel lblIcon;
    private JLabel lblName;
    private JLabel lblStatus;
    private StatusIconPanel statusIcon;
    private JLabel lblUnreadBadge;
    
    public UserListCellRenderer() {
        setLayout(new BorderLayout(12, 0));
        setBorder(new javax.swing.border.EmptyBorder(10, 15, 10, 15));
        
        lblIcon = new JLabel();
        lblName = new JLabel();
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        lblStatus = new JLabel();
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        statusIcon = new StatusIconPanel();
        
        // Badge hiển thị số tin nhắn chưa đọc với bo viền tròn
        lblUnreadBadge = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (isVisible()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Vẽ nền badge bo tròn
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        lblUnreadBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblUnreadBadge.setForeground(Color.WHITE);
        lblUnreadBadge.setOpaque(false); // Đặt opaque false để vẽ custom background
        lblUnreadBadge.setBackground(new Color(239, 68, 68)); // Màu đỏ
        lblUnreadBadge.setHorizontalAlignment(SwingConstants.CENTER);
        lblUnreadBadge.setBorder(new javax.swing.border.EmptyBorder(3, 7, 3, 7));
        lblUnreadBadge.setVisible(false);
        
        // Panel chứa tên và trạng thái
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(lblName);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        infoPanel.add(lblStatus);
        
        // Panel bên phải chứa status icon và badge
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(lblUnreadBadge);
        rightPanel.add(statusIcon);
        
        add(lblIcon, BorderLayout.WEST);
        add(infoPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends UserDisplay> list, UserDisplay user, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        
        lblName.setText(user.getFullName());
        
        if (user.isGroup()) {
            lblIcon.setIcon(IconUtils.loadAndScaleIcon("user.png", 40, 40));
            lblStatus.setText("Nhóm chat");
            lblStatus.setForeground(new Color(156, 163, 175));
        } else if (user.getUsername().equals("Server (Admin)")) {
            lblIcon.setIcon(IconUtils.loadAndScaleIcon("avatar.jpg", 40, 40));
            lblStatus.setText("Quản trị viên");
            lblStatus.setForeground(new Color(251, 191, 36));
        } else {
            lblIcon.setIcon(IconUtils.loadAndScaleIcon("avatar.jpg", 40, 40));
            lblStatus.setText(user.isOnline() ? "Đang hoạt động" : "Không hoạt động");
            lblStatus.setForeground(user.isOnline() ? Constants.ONLINE_COLOR : new Color(107, 114, 128));
        }
        statusIcon.setOnline(!user.isGroup() && user.isOnline());
        
        // Hiển thị badge số tin nhắn chưa đọc
        if (user.getUnreadCount() > 0) {
            lblUnreadBadge.setText(String.valueOf(user.getUnreadCount()));
            lblUnreadBadge.setVisible(true);
        } else {
            lblUnreadBadge.setVisible(false);
        }
        
        if (isSelected) {
            setBackground(Constants.HOVER_COLOR);
            lblName.setForeground(Constants.PRIMARY_DARK);
        } else {
            setBackground(Constants.SIDEBAR_BG_COLOR);
            lblName.setForeground(Constants.SIDEBAR_TEXT_COLOR);
        }
        
        setEnabled(list.isEnabled());
        setOpaque(true);
        
        return this;
    }
}
