package UI_ChatClient.view.dialogs;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog hiển thị danh sách thành viên trong nhóm
 * Thiết kế hiện đại với giao diện đẹp mắt
 */
public class ViewGroupMembersDialog extends JDialog {
    
    private static final Color PRIMARY_COLOR = new Color(94, 234, 212);
    private static final Color TEXT_COLOR = new Color(19, 78, 74);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    
    public ViewGroupMembersDialog(JFrame owner, String groupName, String[] members) {
        super(owner, "Thành viên nhóm", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        
        // Panel chính với hiệu ứng đổ bóng
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ đổ bóng
                for (int i = 0; i < 10; i++) {
                    g2.setColor(new Color(0, 0, 0, 12 - i));
                    g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 16 - i, 16 - i);
                }
                
                // Vẽ nền trắng
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 12, 12);
                
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
        
        JLabel lblIcon = new JLabel("👥");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Thành viên nhóm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_COLOR);
        
        JLabel lblGroupName = new JLabel(groupName);
        lblGroupName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblGroupName.setForeground(new Color(107, 114, 128));
        
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(lblGroupName);
        
        headerPanel.add(lblIcon, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Members list
        JPanel membersContainer = new JPanel();
        membersContainer.setLayout(new BoxLayout(membersContainer, BoxLayout.Y_AXIS));
        membersContainer.setOpaque(false);
        
        if (members != null && members.length > 0) {
            for (int i = 0; i < members.length; i++) {
                String member = members[i];
                JPanel memberPanel = createMemberPanel(member, i + 1);
                membersContainer.add(memberPanel);
                
                if (i < members.length - 1) {
                    membersContainer.add(Box.createVerticalStrut(5));
                }
            }
        } else {
            JLabel lblNoMembers = new JLabel("Không có thành viên nào");
            lblNoMembers.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblNoMembers.setForeground(new Color(156, 163, 175));
            lblNoMembers.setAlignmentX(Component.CENTER_ALIGNMENT);
            membersContainer.add(lblNoMembers);
        }
        
        JScrollPane scrollPane = new JScrollPane(membersContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        scrollPane.setPreferredSize(new Dimension(0, 250));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Footer với nút đóng
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        JButton btnClose = new JButton("Đóng") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_COLOR,
                    getWidth(), 0, new Color(45, 212, 191)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setForeground(Color.WHITE);
        btnClose.setOpaque(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        
        footerPanel.add(btnClose);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
        setSize(400, 480);
        setLocationRelativeTo(owner);
    }
    
    /**
     * Tạo panel cho một thành viên
     */
    private JPanel createMemberPanel(String memberName, int index) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(true);
        panel.setBackground(new Color(249, 250, 251));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        // Avatar placeholder với số thứ tự
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_COLOR.brighter(),
                    getWidth(), getHeight(), PRIMARY_COLOR
                );
                g2.setPaint(gradient);
                g2.fillOval(0, 0, getWidth(), getHeight());
                
                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(40, 40));
        avatarPanel.setLayout(new GridBagLayout());
        
        JLabel lblIndex = new JLabel(String.valueOf(index));
        lblIndex.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblIndex.setForeground(Color.WHITE);
        avatarPanel.add(lblIndex);
        
        // Member name
        JLabel lblName = new JLabel(memberName);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblName.setForeground(TEXT_COLOR);
        
        // Online indicator (optional - có thể mở rộng sau)
        JLabel lblStatus = new JLabel("●");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(34, 197, 94)); // Green for online
        lblStatus.setToolTipText("Đang hoạt động");
        
        panel.add(avatarPanel, BorderLayout.WEST);
        panel.add(lblName, BorderLayout.CENTER);
        panel.add(lblStatus, BorderLayout.EAST);
        
        return panel;
    }
}
