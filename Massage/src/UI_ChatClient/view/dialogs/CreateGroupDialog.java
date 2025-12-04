package UI_ChatClient.view.dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import UI_ChatClient.model.UserDisplay;
import UI_ChatClient.model.Constants;
import UI_ChatClient.view.components.RoundTextField;

/**
 * Dialog tạo nhóm mới
 */
public class CreateGroupDialog extends JDialog {
    private JList<UserDisplay> potentialMembersList;
    private RoundTextField groupNameField;
    private JButton btnCreate;
    private JButton btnCancel;
    
    private boolean succeeded = false;
    private List<UserDisplay> selectedUsers;
    private String groupName;

    public CreateGroupDialog(JFrame owner, DefaultListModel<UserDisplay> model) {
        super(owner, "Tạo Nhóm Mới", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        
        // Panel chính với bo tròn
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(229, 231, 235));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new javax.swing.border.EmptyBorder(25, 25, 25, 25));
        
        // Header với icon và tiêu đề
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 20, 0));
        
        JLabel lblIcon = new JLabel("👥");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new javax.swing.border.EmptyBorder(0, 15, 0, 0));
        
        JLabel lblTitle = new JLabel("Tạo nhóm mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(31, 41, 55));
        
        JLabel lblSubtitle = new JLabel("Thêm thành viên và bắt đầu trò chuyện");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(107, 114, 128));
        
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        
        headerPanel.add(lblIcon, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setOpaque(false);
        
        // Tên nhóm
        JPanel groupNamePanel = new JPanel(new BorderLayout(0, 8));
        groupNamePanel.setOpaque(false);
        JLabel lblGroupName = new JLabel("Tên nhóm");
        lblGroupName.setFont(Constants.UI_FONT_BOLD);
        lblGroupName.setForeground(new Color(55, 65, 81));
        groupNameField = new RoundTextField(0);
        groupNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        groupNameField.setBackground(new Color(249, 250, 251));
        groupNamePanel.add(lblGroupName, BorderLayout.NORTH);
        groupNamePanel.add(groupNameField, BorderLayout.CENTER);
        contentPanel.add(groupNamePanel, BorderLayout.NORTH);
        
        // Danh sách thành viên
        JPanel membersPanel = new JPanel(new BorderLayout(0, 8));
        membersPanel.setOpaque(false);
        JLabel lblMembers = new JLabel("Chọn thành viên (giữ Ctrl để chọn nhiều)");
        lblMembers.setFont(Constants.UI_FONT_BOLD);
        lblMembers.setForeground(new Color(55, 65, 81));
        
        potentialMembersList = new JList<>(model);
        potentialMembersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        potentialMembersList.setBackground(new Color(249, 250, 251));
        potentialMembersList.setFixedCellHeight(50);
        potentialMembersList.setSelectionBackground(new Color(99, 102, 241, 50));
        potentialMembersList.setSelectionForeground(new Color(31, 41, 55));
        
        JScrollPane scrollPane = new JScrollPane(potentialMembersList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        scrollPane.setPreferredSize(new Dimension(0, 200));
        
        membersPanel.add(lblMembers, BorderLayout.NORTH);
        membersPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(membersPanel, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new javax.swing.border.EmptyBorder(20, 0, 0, 0));
        
        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(Constants.UI_FONT_BOLD);
        btnCancel.setBackground(new Color(243, 244, 246));
        btnCancel.setForeground(new Color(55, 65, 81));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(new javax.swing.border.EmptyBorder(12, 24, 12, 24));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnCreate = new JButton("Tạo nhóm");
        btnCreate.setFont(Constants.UI_FONT_BOLD);
        btnCreate.setBackground(new Color(99, 102, 241));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setFocusPainted(false);
        btnCreate.setBorder(new javax.swing.border.EmptyBorder(12, 24, 12, 24));
        btnCreate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnCreate);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        btnCreate.addActionListener(e -> onCreate());
        btnCancel.addActionListener(e -> onCancel());
        
        setContentPane(mainPanel);
        setSize(420, 520);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void onCreate() {
        String name = groupNameField.getText().trim();
        List<UserDisplay> users = potentialMembersList.getSelectedValuesList();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên nhóm không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bạn phải chọn ít nhất một thành viên khác.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.groupName = name;
        this.selectedUsers = users;
        this.succeeded = true;
        dispose();
    }

    private void onCancel() {
        this.succeeded = false;
        dispose();
    }

    public boolean isSucceeded() { return succeeded; }
    public String getGroupName() { return groupName; }
    public List<UserDisplay> getSelectedUsers() { return selectedUsers; }
}
