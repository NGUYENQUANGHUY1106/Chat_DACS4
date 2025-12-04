package UI_Login_Register;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.*;

// Import database
import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Import Client.java từ package UI_ChatClient
import UI_ChatClient.Client; 

public class Login extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JButton btnLogin;
	private JLabel lblRegister;
	

	private final Color gradientTop = new Color(255, 253, 250);      
	private final Color gradientMid = new Color(178, 236, 225);      
	private final Color gradientBottom = new Color(64, 185, 181);    
	private final Color cardBackground = new Color(255, 255, 255);
	private final Color textPrimary = new Color(33, 37, 41);
	private final Color textSecondary = new Color(108, 117, 125);
	private final Color inputBackground = new Color(248, 249, 250);
	private final Color inputBorder = new Color(206, 212, 218);
	private final Color inputFocusBorder = new Color(64, 185, 181);
	private final Color buttonColor = new Color(64, 185, 181);       
	private final Color buttonHover = new Color(45, 160, 155);       
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
				Login frame = new Login();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public Login() {
		setTitle("Đăng nhập - ChatSphere");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 600);
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
		cardPanel.setPreferredSize(new Dimension(400, 450));
		cardPanel.setBorder(new EmptyBorder(40, 45, 40, 45));

		// Content panel inside card
		JPanel innerContent = new JPanel();
		innerContent.setLayout(new BoxLayout(innerContent, BoxLayout.Y_AXIS));
		innerContent.setOpaque(false);
		innerContent.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Title
		JLabel lblTitle = new JLabel("ChatSphere");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
		lblTitle.setForeground(textPrimary);
		lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		innerContent.add(lblTitle);

		// Subtitle
		JLabel lblSubtitle = new JLabel("Chào mừng trở lại!");
		lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		lblSubtitle.setForeground(textSecondary);
		lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		innerContent.add(lblSubtitle);
		innerContent.add(Box.createVerticalStrut(30));

		// Username field with icon
		JPanel usernamePanel = createInputPanel("👤", "Số điện thoại hoặc Email");
		usernamePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		txtUsername = (JTextField) ((JPanel) usernamePanel.getComponent(0)).getComponent(1);
		innerContent.add(usernamePanel);
		innerContent.add(Box.createVerticalStrut(12));

		// Password field with icon
		JPanel passwordPanel = createPasswordPanel("🔒", "Mật khẩu");
		passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		txtPassword = (JPasswordField) ((JPanel) passwordPanel.getComponent(0)).getComponent(1);
		innerContent.add(passwordPanel);
		innerContent.add(Box.createVerticalStrut(20));

		// Login button
		btnLogin = createModernButton("Đăng nhập");
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		buttonPanel.setOpaque(false);
		buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		buttonPanel.setPreferredSize(new Dimension(280, 50));
		buttonPanel.add(btnLogin);
		innerContent.add(buttonPanel);
		innerContent.add(Box.createVerticalStrut(18));

		// Divider
		JPanel dividerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		dividerPanel.setOpaque(false);
		dividerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		dividerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
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

		// Register link
		JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		registerPanel.setOpaque(false);
		registerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		JLabel lblNoAccount = new JLabel("Chưa có tài khoản?");
		lblNoAccount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		lblNoAccount.setForeground(textSecondary);
		lblRegister = new JLabel("Đăng ký ngay");
		lblRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lblRegister.setForeground(linkColor);
		lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
		registerPanel.add(lblNoAccount);
		registerPanel.add(lblRegister);
		innerContent.add(registerPanel);

		cardPanel.add(innerContent, BorderLayout.CENTER);
		contentPane.add(cardPanel);
		
		addActions();
	}

	private JPanel createInputPanel(String icon, String placeholder) {
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setOpaque(false);
		wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

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
		inputPanel.setBorder(new EmptyBorder(12, 15, 12, 15));

		JLabel iconLabel = new JLabel(icon);
		iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
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
		wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

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
		inputPanel.setBorder(new EmptyBorder(12, 15, 12, 15));

		JLabel iconLabel = new JLabel(icon);
		iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
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
		// Click nút đăng nhập
		btnLogin.addActionListener(e -> handleLogin());
		// Bấm Enter để đăng nhập
		txtUsername.addActionListener(e -> handleLogin());
		txtPassword.addActionListener(e -> handleLogin());
		
		// Hover và Click cho link đăng ký
		lblRegister.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				lblRegister.setText("<html><u>Đăng ký ngay</u></html>");
			}
			@Override
			public void mouseExited(MouseEvent e) {
				lblRegister.setText("Đăng ký ngay");
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				new Register().setVisible(true);
				Login.this.dispose();
			}
		});
	}

    private void handleLogin() {
        String userInput = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (userInput.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, 
                "Vui lòng nhập đầy đủ tài khoản và mật khẩu!", 
                "Lỗi đăng nhập", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT * FROM users WHERE (phone = ? OR email = ?) AND password = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Không thể kết nối đến cơ sở dữ liệu!", "Lỗi DB", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userInput);
                ps.setString(2, userInput);
                ps.setString(3, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String username = rs.getString("username"); 
                        String fullName = rs.getString("full_name");

                        JOptionPane.showMessageDialog(this, "Đăng nhập thành công! Chào mừng " + fullName);

                        // Khởi chạy Client và truyền cả 2 thông tin
                        Client clientFrame = new Client(username, fullName);
                        clientFrame.setVisible(true);
                        
                        this.dispose();
                        
                    } else {
                        JOptionPane.showMessageDialog(this, "Sai SĐT/Email hoặc mật khẩu!", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}