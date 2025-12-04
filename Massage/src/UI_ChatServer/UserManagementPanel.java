package UI_ChatServer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities; // <-- Import
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class UserManagementPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTable userTable;
	private JTextField txtSearch;
    private DefaultTableModel userTableModel; // Biến thành viên

	public UserManagementPanel() {
		setLayout(new BorderLayout(15, 15));
		setBorder(new EmptyBorder(15, 15, 15, 15));
		setBackground(Color.WHITE);
		
		// === 1. TOP PANEL (NORTH) ===
		JPanel topPanel = new JPanel(new BorderLayout(10, 10));
		topPanel.setOpaque(false);
		
		JLabel lblUserTitle = new JLabel("Quản lý Người dùng");
		lblUserTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
		topPanel.add(lblUserTitle, BorderLayout.WEST);
		
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		searchPanel.setOpaque(false);
		JLabel lblSearch = new JLabel("🔍 Tìm kiếm:");
		lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		txtSearch = new JTextField(20);
		txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		JButton btnSearch = new JButton("Tìm");
		btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
		searchPanel.add(lblSearch);
		searchPanel.add(txtSearch);
		searchPanel.add(btnSearch);
		topPanel.add(searchPanel, BorderLayout.EAST);
		
		add(topPanel, BorderLayout.NORTH);

		// === 2. TABLE (CENTER) ===
		String[] userColumnNames = {"Username (SĐT)", "Trạng thái", "Hành động"};
		
		userTableModel = new DefaultTableModel(userColumnNames, 0);
		
		userTable = new JTable(userTableModel);
		userTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		userTable.setRowHeight(25);
		userTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		userTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		userTable.getColumnModel().getColumn(2).setPreferredWidth(120);
		
		JScrollPane userTableScrollPane = new JScrollPane(userTable);
		
		add(userTableScrollPane, BorderLayout.CENTER);

		// === 3. PAGINATION (SOUTH) ===
		JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		paginationPanel.setOpaque(false);
		// (Các nút phân trang chưa có logic)
		
		add(paginationPanel, BorderLayout.SOUTH);
	}

    // =============================================================
    // CÁC PHƯTNG THỨC CÔNG KHAI ĐỂ CẬP NHẬT BẢNG
    // =============================================================

    /**
     * Thêm một hàng mới vào bảng user khi có người kết nối
     */
    public void addUser(String username) {
        SwingUtilities.invokeLater(() -> {
            userTableModel.addRow(new Object[]{username, "🟢 Online", "[Chi tiết] [Khóa]"});
        });
    }

    /**
     * Xóa một hàng khỏi bảng user khi có người ngắt kết nối
     */
    public void removeUser(String username) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < userTableModel.getRowCount(); i++) {
                if (userTableModel.getValueAt(i, 0).equals(username)) {
                    userTableModel.removeRow(i);
                    return; // Thoát sau khi tìm thấy và xóa
                }
            }
        });
    }
}