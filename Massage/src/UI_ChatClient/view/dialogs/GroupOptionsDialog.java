package UI_ChatClient.view.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog hiển thị menu tùy chọn cho nhóm chat
 * Thiết kế hiện đại với gradient và hiệu ứng hover
 */
public class GroupOptionsDialog extends JDialog {
    
    private static final Color PRIMARY_COLOR = new Color(94, 234, 212);
    private static final Color SECONDARY_COLOR = new Color(45, 212, 191);
    private static final Color HOVER_COLOR = new Color(240, 253, 250);
    private static final Color TEXT_COLOR = new Color(19, 78, 74);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color DANGER_HOVER = new Color(254, 242, 242);
    
    private int selectedOption = -1; // 0 = Xem thành viên, 1 = Rời nhóm, -1 = Không chọn
    
    public GroupOptionsDialog(JFrame owner, String groupName) {
        super(owner, "", true);
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
                for (int i = 0; i < 8; i++) {
                    g2.setColor(new Color(0, 0, 0, 10 - i));
                    g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 16 - i, 16 - i);
                }
                
                // Vẽ nền trắng
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 12, 12);
                
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Header với tên nhóm
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 15, 10));
        
        JLabel lblGroupIcon = new JLabel("👥");
        lblGroupIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        JLabel lblTitle = new JLabel("Tùy chọn nhóm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_COLOR);
        
        JLabel lblGroupName = new JLabel(groupName);
        lblGroupName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGroupName.setForeground(new Color(107, 114, 128));
        
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblGroupName);
        
        headerPanel.add(lblGroupIcon, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        contentPanel.add(headerPanel);
        
        // Divider
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(229, 231, 235));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        contentPanel.add(separator);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Option 1: Xem thành viên
        JPanel viewMembersOption = createOptionPanel(
            "👤", 
            "Xem thành viên nhóm",
            "Xem danh sách tất cả thành viên trong nhóm",
            PRIMARY_COLOR,
            HOVER_COLOR,
            false
        );
        viewMembersOption.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedOption = 0;
                dispose();
            }
        });
        contentPanel.add(viewMembersOption);
        contentPanel.add(Box.createVerticalStrut(8));
        
        // Option 2: Rời nhóm
        JPanel leaveGroupOption = createOptionPanel(
            "🚪",
            "Rời khỏi nhóm",
            "Bạn sẽ không thể nhận tin nhắn từ nhóm nữa",
            DANGER_COLOR,
            DANGER_HOVER,
            true
        );
        leaveGroupOption.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedOption = 1;
                dispose();
            }
        });
        contentPanel.add(leaveGroupOption);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Đóng khi click bên ngoài
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dispose();
            }
        });
        
        setContentPane(mainPanel);
        setSize(320, 240);
        setLocationRelativeTo(owner);
    }
    
    /**
     * Tạo panel cho một tùy chọn với hiệu ứng hover đẹp mắt
     */
    private JPanel createOptionPanel(String icon, String title, String description, 
                                     Color accentColor, Color hoverColor, boolean isDanger) {
        JPanel panel = new JPanel(new BorderLayout(12, 0)) {
            private boolean isHovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isHovered) {
                    g2.setColor(hoverColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                
                g2.dispose();
            }
            
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                        repaint();
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        repaint();
                    }
                });
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        // Icon panel với gradient background
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isDanger) {
                    g2.setColor(new Color(254, 226, 226));
                } else {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, accentColor.brighter(),
                        getWidth(), getHeight(), accentColor
                    );
                    g2.setPaint(gradient);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(45, 45));
        iconPanel.setLayout(new GridBagLayout());
        
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconPanel.add(lblIcon);
        
        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(isDanger ? DANGER_COLOR : TEXT_COLOR);
        
        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDescription.setForeground(new Color(107, 114, 128));
        
        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lblDescription);
        
        panel.add(iconPanel, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Lấy lựa chọn của người dùng
     * @return 0 = Xem thành viên, 1 = Rời nhóm, -1 = Không chọn gì
     */
    public int getSelectedOption() {
        return selectedOption;
    }
}
