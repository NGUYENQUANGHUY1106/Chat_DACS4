package UI_ChatClient.view.dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import UI_ChatClient.model.UserDisplay;
import UI_ChatClient.model.Constants;

/**
 * Dialog thêm thành viên vào nhóm
 */
public class AddMemberDialog extends JDialog {
    private JList<UserDisplay> potentialMembersList;
    private JButton btnAdd;
    private JButton btnCancel;
    
    private boolean succeeded = false;
    private List<UserDisplay> selectedUsers;

    public AddMemberDialog(JFrame owner, String groupName, DefaultListModel<UserDisplay> model) {
        super(owner, "Thêm thành viên vào: " + groupName, true);
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
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 20, 0));
        
        JLabel lblIcon = new JLabel("➕");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new javax.swing.border.EmptyBorder(0, 15, 0, 0));
        
        JLabel lblTitle = new JLabel("Thêm thành viên");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(31, 41, 55));
        
        JLabel lblSubtitle = new JLabel("Vào nhóm: " + groupName);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(107, 114, 128));
        
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        
        headerPanel.add(lblIcon, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Danh sách
        JPanel listPanel = new JPanel(new BorderLayout(0, 8));
        listPanel.setOpaque(false);
        
        JLabel lblSelect = new JLabel("Chọn người dùng (giữ Ctrl để chọn nhiều)");
        lblSelect.setFont(Constants.UI_FONT);
        lblSelect.setForeground(new Color(55, 65, 81));
        
        potentialMembersList = new JList<>(model);
        potentialMembersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        potentialMembersList.setBackground(new Color(249, 250, 251));
        potentialMembersList.setFixedCellHeight(50);
        potentialMembersList.setSelectionBackground(new Color(34, 197, 94, 50));
        potentialMembersList.setSelectionForeground(new Color(31, 41, 55));
        
        JScrollPane scrollPane = new JScrollPane(potentialMembersList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        
        listPanel.add(lblSelect, BorderLayout.NORTH);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(listPanel, BorderLayout.CENTER);
        
        // Buttons
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

        btnAdd = new JButton("Thêm thành viên");
        btnAdd.setFont(Constants.UI_FONT_BOLD);
        btnAdd.setBackground(new Color(34, 197, 94));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setBorder(new javax.swing.border.EmptyBorder(12, 24, 12, 24));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnAdd);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> onAdd());
        btnCancel.addActionListener(e -> onCancel());

        setContentPane(mainPanel);
        setSize(400, 480);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void onAdd() {
        List<UserDisplay> users = potentialMembersList.getSelectedValuesList();
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bạn phải chọn ít nhất một thành viên để thêm.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.selectedUsers = users;
        this.succeeded = true;
        dispose();
    }

    private void onCancel() {
        this.succeeded = false;
        dispose();
    }

    public boolean isSucceeded() { return succeeded; }
    public List<UserDisplay> getSelectedUsers() { return selectedUsers; }
}
