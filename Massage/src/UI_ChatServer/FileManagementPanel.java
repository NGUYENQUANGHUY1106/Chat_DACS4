package UI_ChatServer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileManagementPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable fileTable;
    private DefaultTableModel fileTableModel;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    public FileManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Quản lý File và Ảnh");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(lblTitle, BorderLayout.NORTH);

        // Bảng hiển thị file
        String[] columnNames = {"Thời gian", "Người gửi", "Người nhận (User/Nhóm)", "Tên file"};
        fileTableModel = new DefaultTableModel(columnNames, 0);
        
        fileTable = new JTable(fileTableModel);
        fileTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fileTable.setRowHeight(25);
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(fileTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Phương thức công khai để thêm thông tin file vào bảng
     */
    public void addFile(String sender, String receiver, String fileName) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = sdf.format(new Date());
            fileTableModel.addRow(new Object[]{timestamp, sender, receiver, fileName});
        });
    }
}