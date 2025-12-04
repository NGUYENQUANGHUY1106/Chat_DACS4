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
    
    public UserListCellRenderer() {
        setLayout(new BorderLayout(12, 0));
        setBorder(new javax.swing.border.EmptyBorder(10, 15, 10, 15));
        
        lblIcon = new JLabel();
        lblName = new JLabel();
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        lblStatus = new JLabel();
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        statusIcon = new StatusIconPanel();
        
        // Panel chứa tên và trạng thái
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(lblName);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        infoPanel.add(lblStatus);
        
        // Panel bên phải chứa status icon
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
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
        
        if (isSelected) {
            setBackground(Constants.HOVER_COLOR);
            lblName.setForeground(Color.WHITE);
        } else {
            setBackground(Constants.SIDEBAR_BG_COLOR);
            lblName.setForeground(Constants.SIDEBAR_TEXT_COLOR);
        }
        
        setEnabled(list.isEnabled());
        setOpaque(true);
        
        return this;
    }
}
