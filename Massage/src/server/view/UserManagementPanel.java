package server.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class UserManagementPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTable userTable;
    private JTextField txtSearch;
    private DefaultTableModel userTableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    public UserManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        // ================= TOP (TIÊU ĐỀ + TÌM KIẾM) =================
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel lblUserTitle = new JLabel("Quản lý Người dùng");
        lblUserTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topPanel.add(lblUserTitle, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);

        // Icon tìm kiếm
        ImageIcon iconsearch = new ImageIcon(getClass().getResource("/icons/search.png"));
        Image img = iconsearch.getImage();
        Image imgScaled = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        ImageIcon iconResized = new ImageIcon(imgScaled);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setIcon(iconResized);
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

        // ================== TABLE NGƯỜI DÙNG ==================
        String[] userColumnNames = {"Username (SĐT)", "Trạng thái", "Hành động"};
        userTableModel = new DefaultTableModel(userColumnNames, 0);
        userTable = new JTable(userTableModel);

        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userTable.setRowHeight(25);

        // Sorter + filter cho tìm kiếm
        rowSorter = new TableRowSorter<>(userTableModel);
        userTable.setRowSorter(rowSorter);

        // Renderer cho cột trạng thái (hình tròn + chữ Online)
        userTable.getColumnModel().getColumn(1)
                 .setCellRenderer(new StatusCellRenderer());

        JScrollPane userTableScrollPane = new JScrollPane(userTable);
        add(userTableScrollPane, BorderLayout.CENTER);

        // ================= SỰ KIỆN NÚT TÌM =================
        btnSearch.addActionListener(e -> filterUsers());
    }

    // =================== TÌM KIẾM NGƯỜI DÙNG ===================
    private void filterUsers() {
        String text = txtSearch.getText().trim();

        if (text.isEmpty()) {
            rowSorter.setRowFilter(null); // hiện tất cả
        } else {
            RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(text), 0 // lọc theo cột Username (cột 0)
            );
            rowSorter.setRowFilter(rf);
        }
    }

    // =================== CẬP NHẬT BẢNG USER ===================

    // Thêm user mới (Online)
    public void addUser(String username) {
        SwingUtilities.invokeLater(() -> {
            userTableModel.addRow(new Object[]{username, "Online", "[Chi tiết] [Khóa]"});
        });
    }

    // Xoá user khỏi bảng
    public void removeUser(String username) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < userTableModel.getRowCount(); i++) {
                if (userTableModel.getValueAt(i, 0).equals(username)) {
                    userTableModel.removeRow(i);
                    return;
                }
            }
        });
    }

    // =========================================================
    //      CÁC CLASS PHỤ – GỘP CHUNG TRONG FILE NÀY
    // =========================================================

    // Icon hình tròn màu
    private static class CircleIcon implements Icon {

        private final int size;
        private final Color color;

        public CircleIcon(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillOval(x, y, size, size);
        }
    }

    
    private static class StatusCellRenderer extends DefaultTableCellRenderer {

        private final Icon greenIcon = new CircleIcon(new Color(0, 180, 0), 12);
        private final Icon redIcon   = new CircleIcon(Color.RED, 12);

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            String status = value == null ? "" : value.toString();

            if (status.equalsIgnoreCase("Online")) {
                label.setIcon(greenIcon);
                label.setText(" Online");
                label.setForeground(new Color(0, 150, 0));
            } else if (status.equalsIgnoreCase("Offline")) {
                label.setIcon(redIcon);
                label.setText(" Offline");
                label.setForeground(Color.RED);
            } else {
                label.setIcon(null);
                label.setForeground(Color.BLACK);
            }

            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setOpaque(false);

            if (isSelected) {
                label.setForeground(Color.WHITE);
            }

            return label;
        }
    }
}
