package UI_ChatServer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities; // <-- Import
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class DashboardPanel extends JPanel {

	private static final long serialVersionUID = 1L;
    private JTextArea logTextArea; // Khung log
    private JLabel lblUserOnline; // Label để cập nhật số người online
    private JLabel lblMessageCount; // Label để cập nhật số tin nhắn

	private JPanel createStatCard(String icon, String title, String value, Color bgColor) {
		JPanel cardPanel = new JPanel(new BorderLayout(10, 0));
		cardPanel.setBackground(bgColor);
		cardPanel.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(bgColor.darker(), 1, true),
				new EmptyBorder(15, 15, 15, 15)
		));
		
		JLabel lblIcon = new JLabel(icon);
		lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 24));
		cardPanel.add(lblIcon, BorderLayout.WEST);
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setOpaque(false);
		
		JLabel lblTitle = new JLabel(title);
		lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		lblTitle.setForeground(Color.DARK_GRAY);
		
        // Gán các label cho các biến thành viên
        if (title.equals("Người dùng online")) {
            lblUserOnline = new JLabel(value);
            lblUserOnline.setFont(new Font("Segoe UI", Font.BOLD, 20));
            textPanel.add(lblTitle);
            textPanel.add(lblUserOnline);
        } else if (title.equals("Tin nhắn hôm nay")) {
            lblMessageCount = new JLabel(value);
            lblMessageCount.setFont(new Font("Segoe UI", Font.BOLD, 20));
            textPanel.add(lblTitle);
            textPanel.add(lblMessageCount);
        } else {
            JLabel lblValue = new JLabel(value);
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 20));
            textPanel.add(lblTitle);
            textPanel.add(lblValue);
        }
		
		cardPanel.add(textPanel, BorderLayout.CENTER);
		return cardPanel;
	}

	public DashboardPanel() {
		setLayout(new BorderLayout(15, 15));
		setBorder(new EmptyBorder(15, 15, 15, 15));
		setBackground(Color.WHITE);
		
		JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 15));
		statsPanel.setOpaque(false);
		statsPanel.add(createStatCard("👤", "Người dùng online", "0", new Color(232, 245, 254)));
		statsPanel.add(createStatCard("💬", "Tin nhắn hôm nay", "0", new Color(230, 250, 233)));
		statsPanel.add(createStatCard("🕓", "Lượt đăng nhập", "0", new Color(254, 248, 231)));
		
		add(statsPanel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logTextArea.setForeground(Color.DARK_GRAY);
        logTextArea.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Nhật ký hệ thống (System Log)", 
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), Color.DARK_GRAY
        ));
        
        centerPanel.add(logScrollPane, BorderLayout.CENTER);
		add(centerPanel, BorderLayout.CENTER);
		
		JPanel alertsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		alertsPanel.setBackground(new Color(255, 248, 225));
		alertsPanel.setBorder(new LineBorder(new Color(255, 224, 130), 1, true));
		JLabel lblAlertIcon = new JLabel("⚠️");
		lblAlertIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		
		alertsPanel.add(lblAlertIcon);
	
		add(alertsPanel, BorderLayout.SOUTH);
	}

    /**
     * Phương thức công khai để thêm log
     */
    public void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            if (logTextArea.getDocument().getLength() > 50000) {
                logTextArea.setText("");
            }
            logTextArea.append(message + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }
    
    /**
     * Phương thức công khai để cập nhật số người online
     */
    public void updateUserCount(int count) {
        SwingUtilities.invokeLater(() -> {
            if (lblUserOnline != null) {
                lblUserOnline.setText(String.valueOf(count));
            }
        });
    }
    
    /**
     * Phương thức công khai để tăng số tin nhắn
     */
    public void incrementMessageCount() {
        SwingUtilities.invokeLater(() -> {
            if (lblMessageCount != null) {
                int currentCount = Integer.parseInt(lblMessageCount.getText().replace(",", ""));
                lblMessageCount.setText(String.valueOf(currentCount + 1));
            }
        });
    }
}