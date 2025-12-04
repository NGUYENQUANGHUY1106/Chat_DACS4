package UI_Login_Register;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.*;

// Import database connection
import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Register extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtFullName;
	private JTextField txtPhone;
	private JTextField txtEmail;
	private JPasswordField txtPassword;
	private JPasswordField txtConfirmPassword;
	private JButton btnRegister;
	private JLabel lblLoginLink;

	// Modern color palette - Gradient: Trắng kem -> Xanh ngọc nhạt -> Turquoise đậm
	private final Color gradientTop = new Color(255, 253, 250);      // Trắng kem nhạt
	private final Color gradientMid = new Color(178, 236, 225);      // Xanh ngọc mint pastel
	private final Color gradientBottom = new Color(64, 185, 181);    // Turquoise đậm
	private final Color cardBackground = new Color(255, 255, 255);
	private final Color textPrimary = new Color(33, 37, 41);
	private final Color textSecondary = new Color(108, 117, 125);
	private final Color inputBackground = new Color(248, 249, 250);
	private final Color inputBorder = new Color(206, 212, 218);
	private final Color buttonColor = new Color(64, 185, 181);       // Turquoise
	private final Color buttonHover = new Color(45, 160, 155);       // Turquoise đậm hơn
	private final Color linkColor = new Color(64, 185, 181);

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			System.err.println("Không thể áp dụng Nimbus Look and Feel.");
			e.printStackTrace();
		}

		EventQueue.invokeLater(() -> {
			try {
				Register frame = new Register();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public Register() {
		setTitle("Đăng ký - ChatSphere");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 700);
		setLocationRelativeTo(null);
		setUndecorated(false);
		
		// Main panel with 3-color gradient background
		contentPane = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				
				int height = getHeight();
				int width = getWidth();
				
				// Gradient từ trắng kem -> xanh ngọc nhạt (nửa trên)
				GradientPaint gradient1 = new GradientPaint(0, 0, gradientTop, 0, height / 2, gradientMid);
				g2d.setPaint(gradient1);
				g2d.fillRect(0, 0, width, height / 2);
				
				// Gradient từ xanh ngọc nhạt -> turquoise đậm (nửa dưới)
				GradientPaint gradient2 = new GradientPaint(0, height / 2, gradientMid, 0, height, gradientBottom);
				g2d.setPaint(gradient2);
				g2d.fillRect(0, height / 2, width, height / 2);
			}
		};
		contentPane.setLayout(new GridBagLayout());
		setContentPane(contentPane);

		// Card panel
		JPanel cardPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				// Shadow
				g2d.setColor(new Color(0, 0, 0, 30));
				g2d.fill(new RoundRectangle2D.Double(5, 5, getWidth() - 6, getHeight() - 6, 30, 30));
				
				// Card background
				g2d.setColor(cardBackground);
				g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 6, getHeight() - 6, 30, 30));
			}
		};
		cardPanel.setLayout(new BorderLayout());
		cardPanel.setOpaque(false);
		cardPanel.setPreferredSize(new Dimension(420, 580));
		cardPanel.setBorder(new EmptyBorder(35, 45, 35, 45));

		// Content panel inside card
		JPanel innerContent = new JPanel();
		innerContent.setLayout(new BoxLayout(innerContent, BoxLayout.Y_AXIS));
		innerContent.setOpaque(false);

		// Title
		JLabel lblTitle = new JLabel("Tạo tài khoản");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
		lblTitle.setForeground(textPrimary);
		lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		innerContent.add(lblTitle);

		// Subtitle
		JLabel lblSubtitle = new JLabel("Tham gia ChatSphere ngay hôm nay!");
		lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		lblSubtitle.setForeground(textSecondary);
		lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		innerContent.add(lblSubtitle);
		innerContent.add(Box.createVerticalStrut(25));

		// Full name field
		JPanel fullNamePanel = createInputPanel("👤", "Họ và tên");
		txtFullName = (JTextField) ((JPanel) fullNamePanel.getComponent(0)).getComponent(1);
		innerContent.add(fullNamePanel);
		innerContent.add(Box.createVerticalStrut(12));

		// Phone field
		JPanel phonePanel = createInputPanel("📱", "Số điện thoại");
		txtPhone = (JTextField) ((JPanel) phonePanel.getComponent(0)).getComponent(1);
		innerContent.add(phonePanel);
		innerContent.add(Box.createVerticalStrut(12));

		// Email field
		JPanel emailPanel = createInputPanel("📧", "Email");
		txtEmail = (JTextField) ((JPanel) emailPanel.getComponent(0)).getComponent(1);
		innerContent.add(emailPanel);
		innerContent.add(Box.createVerticalStrut(12));

		// Password field
		JPanel passwordPanel = createPasswordPanel("🔒", "Mật khẩu");
		txtPassword = (JPasswordField) ((JPanel) passwordPanel.getComponent(0)).getComponent(1);
		innerContent.add(passwordPanel);
		innerContent.add(Box.createVerticalStrut(12));

		// Confirm password field
		JPanel confirmPasswordPanel = createPasswordPanel("🔐", "Xác nhận mật khẩu");
		txtConfirmPassword = (JPasswordField) ((JPanel) confirmPasswordPanel.getComponent(0)).getComponent(1);
		innerContent.add(confirmPasswordPanel);
		innerContent.add(Box.createVerticalStrut(22));

		// Register button
		btnRegister = createModernButton("Đăng ký");
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		buttonPanel.setOpaque(false);
		buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		buttonPanel.setPreferredSize(new Dimension(280, 50));
		buttonPanel.add(btnRegister);
		innerContent.add(buttonPanel);
		innerContent.add(Box.createVerticalStrut(20));

		// Divider
		JPanel dividerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		dividerPanel.setOpaque(false);
		JSeparator leftSep = new JSeparator();
		leftSep.setPreferredSize(new Dimension(80, 1));
		leftSep.setForeground(inputBorder);
		JLabel lblOr = new JLabel("hoặc");
		lblOr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblOr.setForeground(textSecondary);
		JSeparator rightSep = new JSeparator();
		rightSep.setPreferredSize(new Dimension(80, 1));
		rightSep.setForeground(inputBorder);
		dividerPanel.add(leftSep);
		dividerPanel.add(lblOr);
		dividerPanel.add(rightSep);
		innerContent.add(dividerPanel);
		innerContent.add(Box.createVerticalStrut(15));

		// Login link
		JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		loginPanel.setOpaque(false);
		JLabel lblHaveAccount = new JLabel("Đã có tài khoản?");
		lblHaveAccount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		lblHaveAccount.setForeground(textSecondary);
		lblLoginLink = new JLabel("Đăng nhập ngay");
		lblLoginLink.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lblLoginLink.setForeground(linkColor);
		lblLoginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
		loginPanel.add(lblHaveAccount);
		loginPanel.add(lblLoginLink);
		innerContent.add(loginPanel);

		cardPanel.add(innerContent, BorderLayout.CENTER);
		contentPane.add(cardPanel);
		
		addActions();
	}

	private JPanel createInputPanel(String icon, String placeholder) {
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setOpaque(false);
		wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

		JPanel inputPanel = new JPanel(new BorderLayout(10, 0)) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setColor(inputBackground);
				g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
				g2d.setColor(inputBorder);
				g2d.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
			}
		};
		inputPanel.setOpaque(false);
		inputPanel.setBorder(new EmptyBorder(11, 15, 11, 15));

		JLabel iconLabel = new JLabel(icon);
		iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
		inputPanel.add(iconLabel, BorderLayout.WEST);

		JTextField textField = new JTextField() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (getText().isEmpty() && !hasFocus()) {
					Graphics2D g2d = (Graphics2D) g;
					g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					g2d.setColor(textSecondary);
					g2d.setFont(getFont());
					g2d.drawString(placeholder, 5, getHeight() / 2 + 5);
				}
			}
		};
		textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		textField.setBorder(null);
		textField.setOpaque(false);
		textField.setForeground(textPrimary);
		inputPanel.add(textField, BorderLayout.CENTER);

		wrapper.add(inputPanel);
		return wrapper;
	}

	private JPanel createPasswordPanel(String icon, String placeholder) {
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setOpaque(false);
		wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

		JPanel inputPanel = new JPanel(new BorderLayout(10, 0)) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setColor(inputBackground);
				g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
				g2d.setColor(inputBorder);
				g2d.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
			}
		};
		inputPanel.setOpaque(false);
		inputPanel.setBorder(new EmptyBorder(11, 15, 11, 15));

		JLabel iconLabel = new JLabel(icon);
		iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
		inputPanel.add(iconLabel, BorderLayout.WEST);

		JPasswordField passwordField = new JPasswordField() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (getPassword().length == 0 && !hasFocus()) {
					Graphics2D g2d = (Graphics2D) g;
					g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					g2d.setColor(textSecondary);
					g2d.setFont(getFont());
					g2d.drawString(placeholder, 5, getHeight() / 2 + 5);
				}
			}
		};
		passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		passwordField.setBorder(null);
		passwordField.setOpaque(false);
		passwordField.setForeground(textPrimary);
		inputPanel.add(passwordField, BorderLayout.CENTER);

		// Toggle password visibility
		JLabel toggleLabel = new JLabel("👁");
		toggleLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
		toggleLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		toggleLabel.addMouseListener(new MouseAdapter() {
			boolean visible = false;
			@Override
			public void mouseClicked(MouseEvent e) {
				visible = !visible;
				passwordField.setEchoChar(visible ? (char) 0 : '•');
				toggleLabel.setText(visible ? "🙈" : "👁");
			}
		});
		inputPanel.add(toggleLabel, BorderLayout.EAST);

		wrapper.add(inputPanel);
		return wrapper;
	}

	private JButton createModernButton(String text) {
		JButton button = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				if (getModel().isPressed()) {
					g2d.setColor(buttonHover.darker());
				} else if (getModel().isRollover()) {
					g2d.setColor(buttonHover);
				} else {
					g2d.setColor(buttonColor);
				}
				g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
				
				g2d.setColor(Color.WHITE);
				g2d.setFont(getFont());
				FontMetrics fm = g2d.getFontMetrics();
				int x = (getWidth() - fm.stringWidth(getText())) / 2;
				int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
				g2d.drawString(getText(), x, y);
			}
			
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(280, 45);
			}
			
			@Override
			public Dimension getMinimumSize() {
				return new Dimension(280, 45);
			}
			
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(280, 45);
			}
		};
		button.setFont(new Font("Segoe UI", Font.BOLD, 15));
		button.setPreferredSize(new Dimension(280, 45));
		button.setMinimumSize(new Dimension(280, 45));
		button.setMaximumSize(new Dimension(280, 45));
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setOpaque(false);
		return button;
	}

	private void addActions() {
		btnRegister.addActionListener(e -> handleRegister());
		
		lblLoginLink.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				lblLoginLink.setText("<html><u>Đăng nhập ngay</u></html>");
			}
			@Override
			public void mouseExited(MouseEvent e) {
				lblLoginLink.setText("Đăng nhập ngay");
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				new Login().setVisible(true);
				Register.this.dispose();
			}
		});
	}
	
	private void handleRegister() {
		String fullName = txtFullName.getText().trim();
		String phone = txtPhone.getText().trim();
		String email = txtEmail.getText().trim();
		char[] password = txtPassword.getPassword();
		char[] confirmPassword = txtConfirmPassword.getPassword();
		
		if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.length == 0 || confirmPassword.length == 0) {
			JOptionPane.showMessageDialog(contentPane, "Vui lòng nhập đầy đủ thông tin!", "Lỗi đăng ký", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		if (!Arrays.equals(password, confirmPassword)) {
			JOptionPane.showMessageDialog(contentPane, "Mật khẩu và xác nhận mật khẩu không khớp!", "Lỗi đăng ký", JOptionPane.WARNING_MESSAGE);
			return;
		}

        // Logic đăng ký với Database
        String sqlCheck = "SELECT * FROM users WHERE phone = ? OR email = ?";
        String sqlInsert = "INSERT INTO users (username, password, full_name, phone, email) VALUES (?, ?, ?, ?, ?)";
        
        // Chúng ta sẽ dùng SĐT làm username cho logic chat
        String username = phone;
        String passString = new String(password);

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Không thể kết nối đến cơ sở dữ liệu!", "Lỗi DB", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 1. Kiểm tra SĐT hoặc Email đã tồn tại chưa
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, phone);
                psCheck.setString(2, email);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this, "Số điện thoại hoặc Email đã tồn tại!", "Lỗi đăng ký", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            // 2. Nếu chưa, tiến hành INSERT
            try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                psInsert.setString(1, username);
                psInsert.setString(2, passString); // !!! Cần mã hóa mật khẩu trong dự án thực tế
                psInsert.setString(3, fullName);
                psInsert.setString(4, phone);
                psInsert.setString(5, email);

                int rowsAffected = psInsert.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    // Chuyển về trang Login
                    new Login().setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Đăng ký thất bại. Vui lòng thử lại.", "Lỗi đăng ký", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            // Xóa mảng char để bảo mật
            Arrays.fill(password, '0');
            Arrays.fill(confirmPassword, '0');
        }
	}
}