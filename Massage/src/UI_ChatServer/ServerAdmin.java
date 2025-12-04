package UI_ChatServer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultListModel;
import java.io.*;
// ================================================
// IMPORTS CHO UDP
// ================================================
import java.net.DatagramPacket; // <-- MỚI
import java.net.DatagramSocket; // <-- MỚI
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress; // <-- MỚI
import java.net.URI;
import java.util.Set;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashSet;
import javax.sound.sampled.*;
import java.awt.Desktop;
import java.awt.geom.RoundRectangle2D;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JSplitPane;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServerAdmin extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel mainContentPanel;
	private CardLayout cardLayout;
	private final Color sidebarColor = new Color(44, 62, 80);
	private final Color sidebarHoverColor = new Color(52, 73, 94);
	private final Color sidebarTextColor = Color.WHITE;
	private final Color headerColor = Color.WHITE;

	private JButton btnDashboard, btnUsers, btnMessages, btnRooms, btnFiles, btnReports, btnSettings;
	private JButton[] sidebarButtons;
	// Các biến static để cập nhật GUI
private static DashboardPanel dashboardPanel;
	private static UserManagementPanel userManagementPanel;
	private static FileManagementPanel fileManagementPanel;
	private static JPanel roomPanel;
	private static DefaultListModel<String> groupListModel;
	private static JList<String> groupList;
	private static DefaultListModel<UserDisplay> memberListModel;
	private static JList<UserDisplay> memberList;
	private static DefaultListModel<String> chatClientListModel;
	private static JList<String> chatClientList;
	private static JPanel serverChatWindowsPanel;
	private static CardLayout serverChatCardLayout;

	// ================================================
	// CÁC BIẾN LOGIC TỪ CHAT_SERVER
	// ================================================
	private static final int TYPE_FILE_TRANSFER = 2;
	private static final int TYPE_LOCATION_SHARE = 3;
	private static final int TYPE_VOICE_MESSAGE = 4;
	private static final int TYPE_REGISTER_USERNAME = 5;
	private static final int TYPE_USER_LIST_UPDATE = 6;
	private static final int TYPE_PRIVATE_MESSAGE = 7;
	private static final int TYPE_GROUP_MESSAGE = 8;
	private static final int TYPE_CREATE_GROUP_REQUEST = 9;
	private static final int TYPE_RECEIVE_PRIVATE_MESSAGE = 10;
	private static final int TYPE_RECEIVE_GROUP_MESSAGE = 11;
	private static final int TYPE_SYSTEM_MESSAGE = 12;
	private static final int TYPE_ADD_MEMBERS_TO_GROUP = 13;

	private static final int TYPE_VOICE_CALL_REQUEST = 14;
	private static final int TYPE_VOICE_CALL_INCOMING = 15;
	private static final int TYPE_VOICE_CALL_ACCEPT = 16;
	private static final int TYPE_VOICE_CALL_DECLINE = 17;
	private static final int TYPE_VOICE_CALL_ACCEPTED = 18;
	private static final int TYPE_VOICE_CALL_DECLINED = 19;
	private static final int TYPE_VOICE_CALL_HANGUP = 20;
	private static final int TYPE_VOICE_CALL_ENDED = 21;
	private static final int TYPE_VOICE_CALL_DATA = 22; // Sẽ được xử lý bởi UDP

	private static final int TYPE_VIDEO_CALL_REQUEST = 23;
	private static final int TYPE_VIDEO_CALL_INCOMING = 24;
	private static final int TYPE_VIDEO_CALL_ACCEPT = 25;
	private static final int TYPE_VIDEO_CALL_DECLINE = 26;
	private static final int TYPE_VIDEO_CALL_ACCEPTED = 27;
	private static final int TYPE_VIDEO_CALL_DECLINED = 28;
	private static final int TYPE_VIDEO_CALL_HANGUP = 29;
	private static final int TYPE_VIDEO_CALL_ENDED = 30;
	private static final int TYPE_VIDEO_CALL_DATA = 31; // Sẽ được xử lý bởi UDP
	private static final int TYPE_INVITE_TO_CALL_REQUEST = 32;
	private static final int TYPE_JOIN_CALL_REQUEST = 33;
	private static final int TYPE_CALL_STATUS_UPDATE = 34; // Cập nhật trạng thái cuộc gọi nhóm
	private static final int TYPE_CALL_JOINED_SUCCESS = 35;

	// ================================================
	// (THÊM MỚI - UDP) - CÁC HẰNG SỐ VÀ BIẾN CHO UDP
	// ================================================
	private static final int TCP_PORT = 1234;
	private static final int UDP_PORT = 1235;
	private static final int UDP_TYPE_REGISTER_CLIENT = 99; // Gói tin UDP client gửi để đăng ký

	private static DatagramSocket udpSocket; // Socket UDP lắng nghe

	// Map để theo dõi địa chỉ UDP của client
// (IP:Port) -> "clientId" (VD: "096...")
	private static final ConcurrentHashMap<SocketAddress, String> udpClientMap = new ConcurrentHashMap<>();
	// "clientId" -> (IP:Port)
	private static final ConcurrentHashMap<String, SocketAddress> udpAddressBook = new ConcurrentHashMap<>();
	// ================================================

	private static final String DATA_TYPE_FILE = "FILE";
	private static final String DATA_TYPE_LOCATION = "LOCATION";
	private static final String DATA_TYPE_VOICE = "VOICE";

	private static final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, GroupInfo> groups = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, String> activeCalls = new ConcurrentHashMap<>();

	private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private static final Font UI_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
	private static final Color MY_MESSAGE_COLOR = new Color(0, 132, 255);
	private static final Color OTHER_MESSAGE_COLOR = new Color(225, 225, 225);
	private static final Color SYSTEM_MESSAGE_COLOR = new Color(225, 225, 225);

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(() -> {
			try {
				ServerAdmin frame = new ServerAdmin();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	// ... (Hàm loadAndScaleIcon và Constructor ServerAdmin giữ nguyên từ dòng 190
	// đến 720) ...

	public ServerAdmin() {
		setTitle("ChatSphere - Bảng điều khiển Máy chủ");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1200, 700);
		setLocationRelativeTo(null);

		contentPane = new JPanel(new BorderLayout(0, 0));
		contentPane.setBackground(Color.WHITE);
		setContentPane(contentPane);

		// === 1. HEADER (NORTH) ===
		JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
		headerPanel.setBackground(headerColor);
		headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
		headerPanel.setPreferredSize(new Dimension(0, 60));
		headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
		JLabel lblTitle = new JLabel("ChatSphere Admin");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
		lblTitle.setForeground(new Color(52, 152, 219));
		headerPanel.add(lblTitle, BorderLayout.WEST);
		JPanel headerRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
		headerRightPanel.setOpaque(false);
		JLabel lblAdmin = new JLabel("Xin chào, Admin");
		lblAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		JButton btnLogout = new JButton("Logout");
		btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
		btnLogout.setIcon(loadAndScaleIcon("logout.png", 16, 16));
btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnLogout.setFocusPainted(false);
		headerRightPanel.add(lblAdmin);
		headerRightPanel.add(btnLogout);
		headerPanel.add(headerRightPanel, BorderLayout.EAST);
		contentPane.add(headerPanel, BorderLayout.NORTH);

		// === 2. SIDEBAR (WEST) ===
		JPanel sidebarPanel = new JPanel();
		sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
		sidebarPanel.setBackground(sidebarColor);
		sidebarPanel.setPreferredSize(new Dimension(230, 0));
		sidebarButtons = new JButton[7];
		btnDashboard = new JButton("📊 Dashboard");
		sidebarButtons[0] = btnDashboard;
		styleSidebarButton(btnDashboard);
		btnUsers = new JButton("▪ Người dùng");
		sidebarButtons[1] = btnUsers;
		styleSidebarButton(btnUsers);
		btnMessages = new JButton("▪ Tin nhắn");
		sidebarButtons[2] = btnMessages;
		styleSidebarButton(btnMessages);
		btnRooms = new JButton("▪ Phòng chat");
		sidebarButtons[3] = btnRooms;
		styleSidebarButton(btnRooms);
		btnFiles = new JButton("▪ File / Ảnh");
		sidebarButtons[4] = btnFiles;
		styleSidebarButton(btnFiles);
		btnReports = new JButton("▪ Báo cáo");
		sidebarButtons[5] = btnReports;
		styleSidebarButton(btnReports);
		btnSettings = new JButton("⚙️ Cấu hình hệ thống");
		sidebarButtons[6] = btnSettings;
		styleSidebarButton(btnSettings);
		sidebarPanel.add(btnDashboard);
		sidebarPanel.add(btnUsers);
		sidebarPanel.add(btnMessages);
		sidebarPanel.add(btnRooms);
		sidebarPanel.add(btnFiles);
		sidebarPanel.add(btnReports);
		sidebarPanel.add(Box.createVerticalGlue());
		sidebarPanel.add(btnSettings);
		setActiveSidebarButton(btnDashboard);
		contentPane.add(sidebarPanel, BorderLayout.WEST);

		// === 3. FOOTER (SOUTH) ===
		JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		footerPanel.setBackground(Color.WHITE);
		footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
		footerPanel.setPreferredSize(new Dimension(0, 30));
		JLabel lblFooter = new JLabel("© 2025 ChatSphere. All rights reserved.");
		lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblFooter.setForeground(Color.GRAY);
		footerPanel.add(lblFooter);
		contentPane.add(footerPanel, BorderLayout.SOUTH);

		// === 4. MAIN CONTENT (CENTER) VỚI CARDLAYOUT ===
		cardLayout = new CardLayout();
		mainContentPanel = new JPanel(cardLayout);
		mainContentPanel.setBackground(Color.WHITE);

		dashboardPanel = new DashboardPanel();
		userManagementPanel = new UserManagementPanel();
		fileManagementPanel = new FileManagementPanel();

		// ========================================================
		// --- Panel 3: Tin nhắn (ĐÃ NÂNG CẤP) ---
		// ========================================================
		JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
		messagePanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		messagePanel.setBackground(Color.WHITE);
// 3.1: Danh sách client (Bên trái)
		chatClientListModel = new DefaultListModel<>();
		chatClientList = new JList<>(chatClientListModel);
		chatClientList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		chatClientList.setFixedCellHeight(30);
		JScrollPane clientListScrollPane = new JScrollPane(chatClientList);
		clientListScrollPane.setPreferredSize(new Dimension(250, 0));
		clientListScrollPane.setBorder(BorderFactory.createTitledBorder("Clients đang Online"));

		// 3.2: Các cửa sổ chat (Bên phải)
		serverChatCardLayout = new CardLayout();
		serverChatWindowsPanel = new JPanel(serverChatCardLayout);

		JPanel welcomeChatPanel = new JPanel(new GridBagLayout());
		welcomeChatPanel.setBackground(Color.WHITE);
		welcomeChatPanel.add(new JLabel("Chọn một client để xem tin nhắn."));
		serverChatWindowsPanel.add(welcomeChatPanel, "WELCOME_CHAT");
		serverChatCardLayout.show(serverChatWindowsPanel, "WELCOME_CHAT");

		// 3.3: Split Pane
		JSplitPane chatSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, clientListScrollPane,
				serverChatWindowsPanel);
		chatSplitPane.setDividerLocation(250);
		chatSplitPane.setBorder(null);
		messagePanel.add(chatSplitPane, BorderLayout.CENTER);

		// 3.4: Khung nhập liệu (Bên dưới)
		JPanel serverInputPanel = new JPanel(new BorderLayout(10, 10));
		serverInputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		serverInputPanel.setOpaque(false);

		JTextField serverMessageField = new JTextField("Gửi tin nhắn với tư cách Server...");
		serverMessageField.setFont(UI_FONT);
		serverMessageField.setForeground(Color.GRAY);

		serverMessageField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (serverMessageField.getText().equals("Gửi tin nhắn với tư cách Server...")) {
					serverMessageField.setText("");
					serverMessageField.setForeground(Color.BLACK);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (serverMessageField.getText().isEmpty()) {
					serverMessageField.setForeground(Color.GRAY);
					serverMessageField.setText("Gửi tin nhắn với tư cách Server...");
				}
			}
		});

		JButton serverSendButton = new JButton("Gửi");
		serverSendButton.setFont(UI_FONT_BOLD);
		serverSendButton.setBackground(new Color(52, 152, 219));
		serverSendButton.setForeground(Color.WHITE);

		serverInputPanel.add(serverMessageField, BorderLayout.CENTER);
		serverInputPanel.add(serverSendButton, BorderLayout.EAST);
		messagePanel.add(serverInputPanel, BorderLayout.SOUTH);
// 3.5: Thêm Listener cho JList và Nút Gửi
		chatClientList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && chatClientList.getSelectedValue() != null) {
				String selectedValue = chatClientList.getSelectedValue();
				String targetClientId = selectedValue.substring(selectedValue.lastIndexOf("(") + 1,
						selectedValue.lastIndexOf(")"));
serverChatCardLayout.show(serverChatWindowsPanel, targetClientId);
			}
		});

		ActionListener sendAction = e -> {
			String selectedValue = chatClientList.getSelectedValue();
			String message = serverMessageField.getText().trim();
			if (selectedValue == null) {
				JOptionPane.showMessageDialog(this, "Vui lòng chọn một client để gửi tin nhắn!");
				return;
			}
			if (message.isEmpty() || message.equals("Gửi tin nhắn với tư cách Server..."))
				return;

			String targetClientId = selectedValue.substring(selectedValue.lastIndexOf("(") + 1,
					selectedValue.lastIndexOf(")"));
			ClientHandler targetHandler = clients.get(targetClientId);

			if (targetHandler != null) {
				String formattedMessage = "Server (Admin): " + message;
				targetHandler.sendPrivateMessage("Server (Admin)", formattedMessage);
				targetHandler.addMyMessageToServerGUI(formattedMessage);
				serverMessageField.setText("");
				serverMessageField.setForeground(Color.GRAY);
				serverMessageField.setText("Gửi tin nhắn với tư cách Server...");
				getRootPane().requestFocus();
			}
		};

		serverSendButton.addActionListener(sendAction);
		serverMessageField.addActionListener(sendAction);

		// ========================================================

		// --- Panel 4: Quản lý Phòng chat ---
		// ========================================================
		roomPanel = new JPanel(new BorderLayout(10, 10));
		roomPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		roomPanel.setBackground(Color.WHITE);

		JLabel lblRoomTitle = new JLabel("Quản lý Phòng chat / Nhóm");
		lblRoomTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
		roomPanel.add(lblRoomTitle, BorderLayout.NORTH);

		// 4.1: Danh sách Nhóm (Bên trái)
		groupListModel = new DefaultListModel<>();
		groupList = new JList<>(groupListModel); // <-- Sửa (dùng biến static)
		groupList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		JScrollPane groupListScrollPane = new JScrollPane(groupList);
		groupListScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách Nhóm"));

		// 4.2: Danh sách Thành viên (Ở giữa)
		memberListModel = new DefaultListModel<>();
		memberList = new JList<>(memberListModel);
		memberList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		JScrollPane memberListScrollPane = new JScrollPane(memberList);
		memberListScrollPane.setBorder(BorderFactory.createTitledBorder("Thành viên trong Nhóm"));

		// 4.3: Các nút hành động (Bên phải)
		JPanel roomActionPanel = new JPanel(new GridBagLayout());
		roomActionPanel.setBackground(Color.WHITE);
		roomActionPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL; // Các nút rộng bằng nhau

		// Nút Xóa Thành Viên MỚI
		gbc.gridy++;
		JButton btnRemoveMember = new JButton("Xóa Thành Viên");
btnRemoveMember.setForeground(Color.RED.darker());
		btnRemoveMember.setFont(new Font("Segoe UI", Font.BOLD, 12));
		roomActionPanel.add(btnRemoveMember, gbc);

		gbc.gridy++;
		gbc.weighty = 1.0; // Đẩy các nút lên trên
		roomActionPanel.add(Box.createVerticalGlue(), gbc);

		// 4.4: Bố cục chính của Panel
		JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
		centerPanel.setOpaque(false);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groupListScrollPane, memberListScrollPane);
		splitPane.setDividerLocation(280); // Kích thước ban đầu cho danh sách nhóm
		splitPane.setOpaque(false);

		centerPanel.add(splitPane, BorderLayout.CENTER);
		centerPanel.add(roomActionPanel, BorderLayout.EAST);

		roomPanel.add(centerPanel, BorderLayout.CENTER);

		// 4.5: Thêm Sự kiện (Logic)

		// Sự kiện khi bấm vào 1 nhóm
		groupList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				String selectedGroupFullName = groupList.getSelectedValue();
				if (selectedGroupFullName == null) {
					memberListModel.clear();
				} else {
					updateMemberPanel(selectedGroupFullName);
				}
			}
		});

		// Sự kiện khi bấm nút Xóa Thành Viên
		btnRemoveMember.addActionListener(e -> {
			String selectedGroupFullName = groupList.getSelectedValue();
			UserDisplay selectedMember = memberList.getSelectedValue();

			if (selectedGroupFullName == null || selectedMember == null) {
				JOptionPane.showMessageDialog(roomPanel, "Vui lòng chọn một nhóm VÀ một thành viên để xóa.", "Lỗi",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			GroupInfo group = findGroupByName(selectedGroupFullName);
			String memberIdToRemove = selectedMember.username;

			if (group == null) {
				JOptionPane.showMessageDialog(roomPanel, "Lỗi: Không tìm thấy thông tin nhóm.", "Lỗi",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// Xác nhận
			int confirm = JOptionPane.showConfirmDialog(roomPanel,
					"Bạn có chắc muốn xóa " + selectedMember.fullName + " khỏi nhóm " + group.groupFullName + "?",
					"Xác nhận Xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (confirm != JOptionPane.YES_OPTION) {
				return;
			}

			// 1. Xóa thành viên khỏi Set
			if (group.members.remove(memberIdToRemove)) {
				addSystemLog("ADMIN: Đã xóa " + memberIdToRemove + " khỏi nhóm " + group.groupName);

				// 2. Cập nhật JList trên GUI Server
				memberListModel.removeElement(selectedMember);

				// 3. Gửi tin nhắn cho người bị xóa (nếu họ online)
				ClientHandler removedHandler = clients.get(memberIdToRemove);
				if (removedHandler != null) {
					removedHandler.sendSystemMessage(
							"Hệ thống: Bạn đã bị admin xóa khỏi nhóm '" + group.groupFullName + "'.");
				}

				// 4. Thông báo cho các thành viên còn lại (nếu họ online)
				String notification = "Hệ thống: " + selectedMember.fullName + " đã bị admin xóa khỏi nhóm.";
for (String memberId : group.members) {
					ClientHandler handler = clients.get(memberId);
					if (handler != null) {
						handler.sendSystemMessage(notification);
					}
				}

				// 5. Broadcast update (CỰC KỲ QUAN TRỌNG)
				broadcastUserListUpdate();

			} else {
				addSystemLog("ADMIN: Lỗi, không tìm thấy " + memberIdToRemove + " trong nhóm " + group.groupName);
			}
		});

		// ========================================================

		// --- Các Panel placeholder khác ---
		JPanel reportPanel = createPlaceholderPanel("▪ Báo cáo", "Xem báo cáo hệ thống");
		JPanel settingPanel = createPlaceholderPanel("⚙️ Cấu hình", "Cấu hình hệ thống");

		// Thêm các panel vào CardLayout
		mainContentPanel.add(dashboardPanel, "DASHBOARD");
		mainContentPanel.add(userManagementPanel, "USERS");
		mainContentPanel.add(messagePanel, "MESSAGES");
		mainContentPanel.add(roomPanel, "ROOMS");
		mainContentPanel.add(fileManagementPanel, "FILES");
		mainContentPanel.add(reportPanel, "REPORTS");
		mainContentPanel.add(settingPanel, "SETTINGS");

		contentPane.add(mainContentPanel, BorderLayout.CENTER);

		// === 5. ADD ACTIONS (Kết nối Sidebar với CardLayout) ===
		btnDashboard.addActionListener(e -> {
			cardLayout.show(mainContentPanel, "DASHBOARD");
			setActiveSidebarButton(btnDashboard);
		});
		btnUsers.addActionListener(e -> {
			cardLayout.show(mainContentPanel, "USERS");
			setActiveSidebarButton(btnUsers);
		});
		btnMessages.addActionListener(e -> {
			cardLayout.show(mainContentPanel, "MESSAGES");
			setActiveSidebarButton(btnMessages);
		});
		btnRooms.addActionListener(e -> {
			cardLayout.show(mainContentPanel, "ROOMS");
			setActiveSidebarButton(btnRooms);
		});
		btnFiles.addActionListener(e -> {
			cardLayout.show(mainContentPanel, "FILES");
			setActiveSidebarButton(btnFiles);
		});
		btnReports.addActionListener(e -> {
			cardLayout.show(mainContentPanel, "REPORTS");
			setActiveSidebarButton(btnReports);
		});
		btnSettings.addActionListener(e -> {
			cardLayout.show(mainContentPanel, "SETTINGS");
			setActiveSidebarButton(btnSettings);
		});

		// ================================================
		// KHỞI ĐỘNG SERVER LOGIC
		// ================================================
		new Thread(ServerAdmin::startServer).start();
	}

	// ... (Các hàm style GUI và placeholder giữ nguyên từ 723 đến 788) ...
	private ImageIcon loadAndScaleIcon(String fileName, int width, int height) {
		URL resourceUrl = getClass().getResource("/icons/" + fileName);
		if (resourceUrl == null) {
			System.err.println("Không thể tìm thấy icon: /icons/" + fileName);
			return new ImageIcon(
					new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB));
		}
		ImageIcon icon = new ImageIcon(resourceUrl);
		Image img = icon.getImage();
		Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
return new ImageIcon(scaledImg);
	}

	private JPanel createPlaceholderPanel(String title, String subtitle) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setOpaque(false);
		JLabel lblTitle = new JLabel(title);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
		lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		JLabel lblSubtitle = new JLabel(subtitle);
		lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		lblSubtitle.setForeground(Color.GRAY);
		lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		content.add(lblTitle);
		content.add(Box.createRigidArea(new Dimension(0, 10)));
		content.add(lblSubtitle);
		panel.add(content, new GridBagConstraints());
		return panel;
	}

	private void styleSidebarButton(JButton button) {
		button.setFont(new Font("Segoe UI", Font.BOLD, 15));
		button.setForeground(sidebarTextColor);
		button.setBackground(sidebarColor);
		button.setOpaque(false);
		button.setFocusPainted(false);
		button.setBorder(new EmptyBorder(15, 25, 15, 25));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (!button.isOpaque()) {
					button.setBackground(sidebarHoverColor);
					button.setOpaque(true);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!button.getBackground().equals(sidebarHoverColor.darker())) {
					button.setBackground(sidebarColor);
					button.setOpaque(false);
				}
			}
		});
	}

	private void setActiveSidebarButton(JButton activeButton) {
		for (JButton button : sidebarButtons) {
			if (button != null) {
				button.setBackground(sidebarColor);
				button.setOpaque(false);
			}
		}
		activeButton.setBackground(sidebarHoverColor.darker());
		activeButton.setOpaque(true);
	}

	// =================================================================
	// LOGIC TỪ CHAT_SERVER.JAVA (ĐÃ SỬA ĐỔI CHO HYBRID)
	// =================================================================

	private static void addSystemLog(String message) {
		System.out.println("LOG: " + message);
		SwingUtilities.invokeLater(() -> {
			if (dashboardPanel != null) {
				dashboardPanel.addLog(message);
			}
		});
	}

	private static void updateDashboardCounts() {
		if (dashboardPanel != null) {
			dashboardPanel.updateUserCount(clients.size());
		}
	}

	private static void startServer() {
		try {
			Files.createDirectories(Paths.get("server_downloads"));
			addSystemLog("Thư mục 'server_downloads' đã sẵn sàng.");
		} catch (IOException e) {
addSystemLog("LỖI: Không thể tạo thư mục 'server_downloads': " + e.getMessage());
		}

		ExecutorService executor = Executors.newCachedThreadPool();

		// ================================================
		// (THÊM MỚI - UDP) - KHỞI ĐỘNG LUỒNG UDP RELAY
		// ================================================
		try {
			udpSocket = new DatagramSocket(UDP_PORT);
			UdpRelayThread udpRelay = new UdpRelayThread();
			executor.execute(udpRelay);
			addSystemLog("UDP Relay started... Listening for data on port " + UDP_PORT);
		} catch (IOException e) {
			addSystemLog("SERVER ERROR: Không thể khởi động UDP socket trên port " + UDP_PORT + ": " + e.getMessage());
			e.printStackTrace();
			return; // Không thể tiếp tục nếu UDP thất bại
		}
		// ================================================

		// (GIỮ NGUYÊN) - KHỞI ĐỘNG LUỒNG TCP
		try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
			addSystemLog("TCP Server started... Waiting for clients on port " + TCP_PORT);
			while (true) {
				Socket socket = serverSocket.accept();
				ClientHandler clientHandler = new ClientHandler(socket);
				executor.execute(clientHandler);
			}
		} catch (IOException e) {
			addSystemLog("SERVER ERROR (TCP): " + e.getMessage());
			e.printStackTrace();
		}
	}

	// (SỬA ĐỔI) - LỌC DANH SÁCH NHÓM CHO TỪNG CLIENT
	private static void broadcastUserListUpdate() {
		for (ClientHandler handler : clients.values()) {
			// Lọc danh sách nhóm MÀ HANDLER NÁY LÀ THÀNH VIÊN
			List<GroupInfo> filteredGroups = new ArrayList<>();
			for (GroupInfo group : groups.values()) {
				if (group.members.contains(handler.clientId)) {
					filteredGroups.add(group);
				}
			}
			// Gửi danh sách đã lọc (gồm tất cả user và chỉ các nhóm của handler)
			handler.sendUserListUpdate(clients, filteredGroups);
		}
	}

	private static void broadcastSystemMessage(String message, String exceptUser) {
		for (ClientHandler handler : clients.values()) {
			if (exceptUser == null || !handler.clientId.equals(exceptUser)) {
				handler.sendSystemMessage(message);
			}
		}
	}

	// (GIỮ LẠI) - HÀM KẾT THÚC CUỘC GỌI
	private static synchronized void endCall(String userA, String userB) {
		if (userA == null || userB == null)
			return;

		// Kiểm tra xem cuộc gọi có tồn tại không
		if (activeCalls.remove(userA) == null) {
			activeCalls.remove(userB); // Cũng xóa userB cho chắc
			return; // Thoát vì cuộc gọi có thể đã được xử lý
		}
		activeCalls.remove(userB);
		addSystemLog("Đã kết thúc cuộc gọi (TCP/UDP) giữa: " + userA + " và " + userB);

		// Gửi tin nhắn kết thúc cuộc gọi (VOICE) qua TCP
		ClientHandler handlerB = clients.get(userB);
		if (handlerB != null) {
			try {
				handlerB.dos.writeInt(TYPE_VOICE_CALL_ENDED);
				handlerB.dos.writeUTF(userA);
				handlerB.dos.flush();
			} catch (IOException e) {
				/* Bỏ qua */ }
		}

		// Gửi tin nhắn kết thúc cuộc gọi (VIDEO) qua TCP
ClientHandler handlerA = clients.get(userA);
		if (handlerA != null) {
			try {
				handlerA.dos.writeInt(TYPE_VOICE_CALL_ENDED);
				handlerA.dos.writeUTF(userB);
				handlerA.dos.flush();
			} catch (IOException e) {
				/* Bỏ qua */ }
		}
	}

	// =========================================================================
	// (SỬA ĐỔI) - LỚP LƯU THÔNG TIN NHÓM
	// =========================================================================
	static class GroupInfo {
		String groupName; // ID của nhóm (ví dụ: "laptrinh")
		String groupFullName; // Tên hiển thị (ví dụ: "Nhóm: Lập trình")
		Set<String> members = new HashSet<>();
	}

// =========================================================================
	// (THÊM MỚI) - LỚP HELPER ĐỂ HIỂN THỊ TÊN + ID TRONG JLIST
// =========================================================================
	static class UserDisplay {
		String username; // ID (SĐT)
		String fullName; // Tên hiển thị

		public UserDisplay(String username, String fullName) {
			this.username = username;
			this.fullName = fullName;
		}

		@Override
		public String toString() {
			// JList sẽ hiển thị cái này
			return fullName + " (" + username + ")";
		}
	}

	// =========================================================================
	// (MỚI) - LUỒNG CHUYỂN TIẾP UDP
	// =========================================================================
	// =========================================================================
	// (CẬP NHẬT) - LUỒNG CHUYỂN TIẾP UDP (HỖ TRỢ GROUP CALL)
	// =========================================================================
	static class UdpRelayThread implements Runnable {

		@Override
		public void run() {
			byte[] buffer = new byte[65507];

			while (true) {
				try {
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					udpSocket.receive(packet);

					SocketAddress senderAddress = packet.getSocketAddress();
					int length = packet.getLength();

					String fromId = udpClientMap.get(senderAddress);

					if (fromId != null) {
						String targetId = activeCalls.get(fromId); // Lấy ID người nhận/nhóm nhận

						if (targetId != null) {
							// 1. Kiểm tra xem targetId có phải là một NHÓM không
							if (groups.containsKey(targetId)) {
								GroupInfo group = groups.get(targetId);
								// Gửi cho tất cả thành viên trong nhóm (trừ người gửi)
								for (String memberId : group.members) {
									if (memberId.equals(fromId))
										continue; // Bỏ qua chính mình

									// Kiểm tra xem thành viên này có đang trong cuộc gọi với nhóm không
									// (Tùy chọn: Hoặc cứ gửi nếu họ đã đăng ký UDP)
									SocketAddress targetAddress = udpAddressBook.get(memberId);
									if (targetAddress != null) {
										packet.setSocketAddress(targetAddress);
										udpSocket.send(packet);
									}
								}
							}
							// 2. Nếu không phải nhóm, gửi 1-1 như cũ
else {
								SocketAddress targetAddress = udpAddressBook.get(targetId);
								if (targetAddress != null) {
									packet.setSocketAddress(targetAddress);
									udpSocket.send(packet);
								}
							}
							continue;
						}
					}

					// ... (Phần xử lý gói đăng ký giữ nguyên) ...
					try (DataInputStream dis = new DataInputStream(
							new ByteArrayInputStream(packet.getData(), 0, length))) {
						int dataType = dis.readInt();
						if (dataType == UDP_TYPE_REGISTER_CLIENT) {
							String clientId = dis.readUTF();
							udpClientMap.put(senderAddress, clientId);
							udpAddressBook.put(clientId, senderAddress);
							addSystemLog("UDP: Client " + clientId + " đã đăng ký địa chỉ " + senderAddress);
						}
					} catch (IOException e) {
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// =========================================================================
	// ClientHandler (Lớp nội static - XỬ LÝ TCP)
	// =========================================================================
	static class ClientHandler implements Runnable {
		private final Socket socket;
		private String clientId; // Username (SĐT)
		private String fullName; // Họ và Tên
		private DataInputStream dis;
		private DataOutputStream dos;

		private final JPanel chatPanel;
		private final JScrollPane scrollPane;

		public ClientHandler(Socket socket) {
			this.socket = socket;
			this.chatPanel = new JPanel();
			this.chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
			this.chatPanel.setBackground(Color.WHITE);
			this.chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
			this.scrollPane = new JScrollPane(chatPanel);
			this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
		}

		private void addMessageToPanel(JPanel bubble) {
			SwingUtilities.invokeLater(() -> {
				this.chatPanel.add(bubble);
				this.chatPanel.add(Box.createRigidArea(new Dimension(0, 3)));
				this.chatPanel.revalidate();
				this.chatPanel.repaint();
				scrollToBottom(this.scrollPane);
			});
		}

		public void addClientMessageToServerGUI(String m) {
			addMessageToPanel(createMessageBubble(m, OTHER_MESSAGE_COLOR, false, false));
		}

		public void addMyMessageToServerGUI(String m) {
			addMessageToPanel(createMessageBubble(m, MY_MESSAGE_COLOR, true, false));
		}

		@Override
		public void run() {
			try {
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());

				while (true) {
					int dataType = dis.readInt();
					switch (dataType) {
					case TYPE_REGISTER_USERNAME:
						handleRegisterUsername();
						break;
					case TYPE_PRIVATE_MESSAGE:
						handlePrivateMessage();
						break;
					case TYPE_GROUP_MESSAGE:
						handleGroupMessage();
						break;
					case TYPE_CREATE_GROUP_REQUEST:
handleCreateGroup();
						break;
					case TYPE_ADD_MEMBERS_TO_GROUP:
						handleAddMembersToGroup();
						break;
					case TYPE_FILE_TRANSFER:
						handleDataTransfer(DATA_TYPE_FILE);
						break;
					case TYPE_LOCATION_SHARE:
						handleDataTransfer(DATA_TYPE_LOCATION);
						break;
					case TYPE_VOICE_MESSAGE:
						handleDataTransfer(DATA_TYPE_VOICE);
						break;

					// CÁC LỆNH ĐIỀU KHIỂN VẪN QUA TCP
					case TYPE_VOICE_CALL_REQUEST:
						handleVoiceCallRequest();
						break;
					case TYPE_VOICE_CALL_ACCEPT:
						handleVoiceCallAccept();
						break;
					case TYPE_VOICE_CALL_DECLINE:
						handleVoiceCallDecline();
						break;
					case TYPE_VOICE_CALL_HANGUP:
						handleVoiceCallHangup();
						break;

					case TYPE_VIDEO_CALL_REQUEST:
						handleVideoCallRequest();
						break;
					case TYPE_VIDEO_CALL_ACCEPT:
						handleVideoCallAccept();
						break;
					case TYPE_VIDEO_CALL_DECLINE:
						handleVideoCallDecline();
						break;
					case TYPE_VIDEO_CALL_HANGUP:
						handleVoiceCallHangup();
						break; 
					case TYPE_INVITE_TO_CALL_REQUEST:
						handleInviteToCall();
						break;
					case TYPE_JOIN_CALL_REQUEST:
						handleJoinCallRequest();
						break;

					default:
						addSystemLog("Nhận được gói tin TCP không rõ: " + dataType + " từ " + this.clientId);
					}
				}
			} catch (IOException e) {
				addSystemLog("Client " + this.clientId + " (" + this.fullName + ") disconnected: " + e.getMessage());
			} finally {
				if (this.clientId != null) {
					// Tự động gác máy (cho cả voice/video)
					if (activeCalls.containsKey(this.clientId)) {
						String partnerId = activeCalls.get(this.clientId);
						endCall(this.clientId, partnerId);
					}

					// ================================================
// (THÊM MỚI - UDP) - DỌN DẸP MAP UDP
					// ================================================
					SocketAddress myUdpAddress = udpAddressBook.remove(this.clientId);
					if (myUdpAddress != null) {
						udpClientMap.remove(myUdpAddress);
						addSystemLog("UDP: Đã hủy đăng ký " + this.clientId + " khỏi " + myUdpAddress);
					}
					// ================================================

					clients.remove(clientId);
					final String userDisplayName = this.fullName + " (" + this.clientId + ")";

					SwingUtilities.invokeLater(() -> {
						if (userManagementPanel != null) {
							userManagementPanel.removeUser(userDisplayName);
						}
						if (chatClientListModel != null) {
							chatClientListModel.removeElement(userDisplayName);
						}
						if (serverChatWindowsPanel != null) {
							serverChatCardLayout.removeLayoutComponent(this.scrollPane);
							serverChatWindowsPanel.remove(this.scrollPane);
						}
						if (groupList != null && groupList.getSelectedValue() != null) {
updateMemberPanel(groupList.getSelectedValue());
						}
					});

					updateDashboardCounts();
					broadcastUserListUpdate();
				}
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Xử lý yêu cầu mời người khác vào cuộc gọi hiện tại (nâng cấp 1-1 lên nhóm)
		 * Dữ liệu nhận: String: targetUserId (người được mời), String: callType (VOICE/VIDEO)
		 */
		private void handleInviteToCall() throws IOException {
			String invitedId = dis.readUTF();
			String callType = dis.readUTF();
			
			// 1. Lấy ID Cuộc gọi hiện tại (có thể là ID của người khác nếu là 1-1, hoặc ID nhóm nếu là nhóm)
			String currentContextId = activeCalls.get(this.clientId); 
			
			if (currentContextId == null) {
				sendSystemMessage("Lỗi: Bạn không đang trong cuộc gọi nào để mời.");
				return;
			}
			
			// 2. TÌM HOẶC TẠO NHÓM GỌI TẠM THỜI
			GroupInfo callGroup;
			String groupName;

			if (groups.containsKey(currentContextId)) {
				// Đang trong Group Call
				groupName = currentContextId;
				callGroup = groups.get(groupName);
				addSystemLog("SERVER: " + this.clientId + " mời " + invitedId + " vào Nhóm GỌI " + groupName);

			} else if (clients.containsKey(currentContextId)) {
				// Đang trong cuộc gọi 1-1 (currentContextId là ID của đối tác)
				String partnerId = currentContextId;
				
				// Tạo ID nhóm tạm thời duy nhất, ví dụ: "CALL_SĐT1_SĐT2"
				groupName = "CALL_" + Math.min(this.clientId.hashCode(), partnerId.hashCode()) + "_" + Math.max(this.clientId.hashCode(), partnerId.hashCode());
				
				// Kiểm tra nếu nhóm tạm đã được tạo
				if (groups.containsKey(groupName)) {
					callGroup = groups.get(groupName);
				} else {
					// LẦN ĐẦU TIÊN NÂNG CẤP LÊN NHÓM
					callGroup = new GroupInfo();
					callGroup.groupName = groupName;
					callGroup.groupFullName = (callType.equals("VIDEO") ? "Phòng Video" : "Phòng Voice") + " Tạm thời";
					callGroup.members.add(this.clientId);
					callGroup.members.add(partnerId);
					groups.put(groupName, callGroup);
					addSystemLog("SERVER: Nâng cấp cuộc gọi 1-1 (" + this.clientId + ", " + partnerId + ") lên nhóm tạm " + groupName);

					// Cập nhật activeCalls của người gọi và người nhận sang ID nhóm
					activeCalls.put(this.clientId, groupName);
					activeCalls.put(partnerId, groupName);

					// Gửi thông báo cập nhật trạng thái cuộc gọi cho 2 người
					ClientHandler partnerHandler = clients.get(partnerId);
					if(partnerHandler != null) {
						partnerHandler.sendCallStatusUpdate(groupName, callGroup.groupFullName, "CREATED");
					}
					this.sendCallStatusUpdate(groupName, callGroup.groupFullName, "CREATED");
					
					// Gửi update list để clients biết có nhóm mới (Group Tạm)
					broadcastUserListUpdate();
				}
			} else {
				sendSystemMessage("Lỗi: Không tìm thấy ngữ cảnh cuộc gọi.");
				return;
			}
			
			// 3. THÊM NGƯỜI ĐƯỢC MỜI VÀO NHÓM VÀ GỬI LỜI MỜI
			ClientHandler invitedHandler = clients.get(invitedId);
			if (invitedHandler == null) {
				sendSystemMessage("Hệ thống: Người được mời (" + invitedId + ") không online.");
				return;
			}
			
			// Kiểm tra và thêm vào GroupInfo
			if (!callGroup.members.contains(invitedId)) {
				callGroup.members.add(invitedId);
				addSystemLog("SERVER: Thêm thành viên " + invitedId + " vào nhóm " + groupName);

				// 4. Gửi lời mời đến người được mời
				if (callType.equals("VIDEO")) {
					invitedHandler.sendVideoCallIncoming(groupName, this.fullName + " (Mời vào Video Nhóm)");
				} else {
					invitedHandler.sendVoiceCallIncoming(groupName, this.fullName + " (Mời vào Voice Nhóm)");
				}
				
				// 5. Thông báo cho các thành viên khác trong nhóm về lời mời
				String notification = "Hệ thống: " + invitedHandler.fullName + " đang được mời tham gia.";
				for (String memberId : callGroup.members) {
					if (memberId.equals(this.clientId) || memberId.equals(invitedId)) continue;
					ClientHandler memberHandler = clients.get(memberId);
					if (memberHandler != null) {
						memberHandler.sendSystemMessage(notification);
					}
				}
				
				// 6. Cập nhật danh sách nhóm cho tất cả
				broadcastUserListUpdate(); 
			} else {
				sendSystemMessage("Hệ thống: Người này đã ở trong cuộc gọi rồi.");
			}
		}

		/**
		 * Xử lý yêu cầu tham gia cuộc gọi bằng ID nhóm (Mã phòng)
		 * Dữ liệu nhận: String: groupId (Mã phòng)
		 */
		private void handleJoinCallRequest() throws IOException {
			String groupId = dis.readUTF();
			
			GroupInfo group = groups.get(groupId);
			
			if (group == null || !activeCalls.containsKey(group.groupName) && group.members.size() < 2) {
				// Kiểm tra group có tồn tại VÀ phải có ít nhất 2 thành viên đang gọi (hoặc đã được đánh dấu trong activeCalls)
				sendSystemMessage("Lỗi: Mã phòng không tồn tại hoặc cuộc gọi đã kết thúc.");
				return;
			}
			
			// Kiểm tra xem người dùng đã ở trong cuộc gọi khác chưa
			if (activeCalls.containsKey(this.clientId)) {
				sendSystemMessage("Lỗi: Bạn đang trong cuộc gọi khác. Vui lòng gác máy trước.");
				return;
			}
			
			// 1. Thêm user vào GroupInfo (nếu chưa có)
			if (group.members.add(this.clientId)) {
				addSystemLog("SERVER: User " + this.clientId + " đã tự tham gia vào nhóm " + groupId + " bằng mã phòng.");
			}
			
			// 2. Cập nhật activeCalls của người tham gia
			activeCalls.put(this.clientId, groupId);
			
			// 3. Gửi xác nhận cho client để client bắt đầu kết nối UDP và Mic/Cam
			this.sendCallJoinedSuccess(groupId, group.groupFullName); 
			
			// 4. Thông báo cho các thành viên khác
			String notification = "Hệ thống: " + this.fullName + " đã tham gia cuộc gọi.";
			for (String memberId : group.members) {
				if (memberId.equals(this.clientId)) continue;
				ClientHandler memberHandler = clients.get(memberId);
				if (memberHandler != null) {
					memberHandler.sendSystemMessage(notification);
				}
			}

			// 5. Cập nhật danh sách nhóm cho tất cả
			broadcastUserListUpdate();
		}
		private static synchronized void endCall(String userA, String userB) {
			if (userA == null || userB == null)
				return;

			// 1. Kiểm tra xem userA/userB có đang trong cuộc gọi nhóm tạm thời không
			String contextId = activeCalls.get(userA); // Lấy contextId (có thể là ID người hoặc ID nhóm)
			
			if (contextId != null && groups.containsKey(contextId)) {
				GroupInfo group = groups.get(contextId);
				if (group.members.contains(userA)) {
					group.members.remove(userA); // Xóa người này khỏi danh sách thành viên nhóm
					addSystemLog("Đã xóa " + userA + " khỏi Group Call " + contextId);
					
					// Gửi thông báo cho client đã gác máy
					ClientHandler handlerA = clients.get(userA);
					if (handlerA != null) {
						try {
							handlerA.dos.writeInt(TYPE_VOICE_CALL_ENDED); // Dùng lại hằng số này
							handlerA.dos.writeUTF(contextId);
							handlerA.dos.flush();
						} catch (IOException e) {/* Bỏ qua */}
					}
					
					// Kiểm tra nếu nhóm tạm rỗng, thì xóa nhóm
					if (group.members.isEmpty()) {
						groups.remove(contextId);
						addSystemLog("Đã xóa Nhóm Tạm thời: " + contextId);
					} else {
						// Thông báo cho các thành viên còn lại
						String notification = "Hệ thống: " + (clients.containsKey(userA) ? clients.get(userA).fullName : userA) + " đã rời cuộc gọi.";
						for (String memberId : group.members) {
							ClientHandler handler = clients.get(memberId);
							if (handler != null) {
								handler.sendSystemMessage(notification);
							}
						}
					}
				}
			}
			
			// 2. Xử lý xóa activeCalls và gửi ENDED cho người còn lại (như cũ)
			if (activeCalls.remove(userA) == null) {
				activeCalls.remove(userB); // Cũng xóa userB cho chắc
				return;
			}
			activeCalls.remove(userB);
			addSystemLog("Đã kết thúc cuộc gọi (TCP/UDP) giữa: " + userA + " và " + userB);

			// Gửi tin nhắn kết thúc cuộc gọi (VOICE/VIDEO) qua TCP cho user B (bị gác máy)
			ClientHandler handlerB = clients.get(userB);
			if (handlerB != null) {
				try {
					handlerB.dos.writeInt(TYPE_VOICE_CALL_ENDED); // Dùng chung
					handlerB.dos.writeUTF(userA);
					handlerB.dos.flush();
				} catch (IOException e) {
					/* Bỏ qua */ }
			}

			// Gửi tin nhắn kết thúc cuộc gọi (VOICE/VIDEO) qua TCP cho user A (người gác máy)
			// ... (logic gửi ENDED cho handlerA đã được chuyển lên trên nếu là group) ...
		}
		// CÁC HÀM GỬI LỆNH ĐIỀU KHIỂN MỚI
		
				/**
				 * Gửi cập nhật trạng thái cuộc gọi (ví dụ: 1-1 đã chuyển thành nhóm)
				 */
				public void sendCallStatusUpdate(String groupId, String groupFullName, String status) {
					try {
						dos.writeInt(TYPE_CALL_STATUS_UPDATE);
						dos.writeUTF(groupId);
						dos.writeUTF(groupFullName);
						dos.writeUTF(status);
						dos.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				/**
				 * Gửi xác nhận tham gia thành công bằng mã phòng
				 */
				public void sendCallJoinedSuccess(String groupId, String groupFullName) {
					try {
						dos.writeInt(TYPE_CALL_JOINED_SUCCESS);
						dos.writeUTF(groupId);
						dos.writeUTF(groupFullName);
						dos.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		private void handleRegisterUsername() throws IOException {
			String username = dis.readUTF(); // SĐT
			if (username == null || username.trim().isEmpty() || clients.containsKey(username)) {
				sendSystemMessage("Lỗi: Tên đăng nhập không hợp lệ hoặc đã tồn tại.");
				socket.close();
				return;
			}

			String foundFullName = username;
			String sql = "SELECT full_name FROM users WHERE username = ?";
			try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

				ps.setString(1, username);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						foundFullName = rs.getString("full_name");
					} else {
						addSystemLog("Lỗi: Không tìm thấy user " + username + " trong DB.");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				addSystemLog("Lỗi DB: Không thể lấy fullName cho " + username);
			}

			this.clientId = username;
			this.fullName = foundFullName;
			clients.put(this.clientId, this);
			final String userDisplayName = this.fullName + " (" + this.clientId + ")";

			SwingUtilities.invokeLater(() -> {
				if (userManagementPanel != null) {
					userManagementPanel.addUser(userDisplayName);
				}
				if (serverChatWindowsPanel != null) {
					serverChatWindowsPanel.add(this.scrollPane, this.clientId);
				}
				if (chatClientListModel != null) {
					chatClientListModel.addElement(userDisplayName);
				}
			});
			addSystemLog("Client " + this.clientId + " (" + this.fullName + ") connected (TCP).");
			// Ghi chú: Client PHẢI gửi gói UDP_TYPE_REGISTER_CLIENT ngay sau đây.
			updateDashboardCounts();
			broadcastSystemMessage("Hệ thống: " + this.fullName + " đã tham gia.", this.clientId);
			sendSystemMessage("Chào mừng, " + this.fullName + "!");
			broadcastUserListUpdate();
		}

		private void handlePrivateMessage() throws IOException {
			String targetUsername = dis.readUTF();
			String message = dis.readUTF();
			ClientHandler targetHandler = clients.get(targetUsername);

			if (targetHandler != null) {
				String formattedMessageForClient = this.fullName + ": " + message;
				targetHandler.sendPrivateMessage(this.clientId, formattedMessageForClient);

				String formattedMessageForServer = this.fullName + " -> " + targetHandler.fullName + ": " + message;

				this.addClientMessageToServerGUI(formattedMessageForServer);
				targetHandler.addClientMessageToServerGUI(formattedMessageForServer);

				if (dashboardPanel != null)
					dashboardPanel.incrementMessageCount();
			} else {
				sendSystemMessage("Hệ thống: Không tìm thấy user '" + targetUsername + "'.");
			}
		}
		private void handleGroupMessage() throws IOException {
		    String groupName = dis.readUTF();
		    String message = dis.readUTF();

		    GroupInfo group = groups.get(groupName);
		    if (group == null) {
		        return;
		    }
		    Set<String> members = group.members;
		    if (members == null) {
		        return;
		    }
		    // Kiểm tra xem người gửi có trong nhóm không (bảo mật)
		    if (!members.contains(this.clientId)) {
		        return;
		    }

		    String formattedMessage = "[" + group.groupFullName + "] " + this.fullName + ": " + message;
		    String formattedMessageForServer = this.fullName + " -> [" + group.groupFullName + "]: " + message;
		    
		    // --- ĐOẠN CẦN SỬA Ở ĐÂY ---
		    for (String memberName : members) {
	
		        if (memberName.equals(this.clientId)) {
		            continue; 
		        }

		        ClientHandler memberHandler = clients.get(memberName);
		        if (memberHandler != null) {
		            memberHandler.sendGroupMessage(group.groupName, formattedMessage);
		            // Chỉ cần 1 lần log server là đủ, không cần log trong vòng lặp
		        }
		    }
		    // ---------------------------

		    // Log tin nhắn lên Server GUI (chuyển ra ngoài vòng lặp để tránh log nhiều lần)
		    this.addClientMessageToServerGUI(formattedMessageForServer);
		    
		    if (dashboardPanel != null)
		        dashboardPanel.incrementMessageCount();
		}

		private void handleCreateGroup() throws IOException {
			String groupName = dis.readUTF(); // Tên Nhóm (ID)
			String groupFullName = dis.readUTF(); // Tên Nhóm Hiển thị
			int memberCount = dis.readInt();

			GroupInfo newGroup = new GroupInfo();
			newGroup.groupName = groupName;
			newGroup.groupFullName = groupFullName;

			for (int i = 0; i < memberCount; i++) {
				newGroup.members.add(dis.readUTF());
			}

			if (groups.containsKey(groupName) || clients.containsKey(groupName)) {
				sendSystemMessage("Hệ thống: Tên nhóm '" + groupName + "' đã tồn tại hoặc trùng tên user.");
				return;
			}
			groups.put(groupName, newGroup); // Lưu đối tượng GroupInfo

			SwingUtilities.invokeLater(() -> {
				if (groupListModel != null) {
					groupListModel.addElement(groupFullName); // Chỉ lưu FullName vào JList
				}
			});

			
			broadcastUserListUpdate();
		}

		private void handleAddMembersToGroup() throws IOException {
			String groupName = dis.readUTF(); // ID nhóm
			int newMemberCount = dis.readInt();

			GroupInfo group = groups.get(groupName);
			if (group == null) {
				sendSystemMessage("Lỗi: Không tìm thấy nhóm '" + groupName + "'.");
				for (int i = 0; i < newMemberCount; i++)
					dis.readUTF(); // Đọc bỏ qua
				return;
			}

			Set<String> newMembers = new HashSet<>();
			for (int i = 0; i < newMemberCount; i++) {
				String newMemberId = dis.readUTF();
				if (group.members.add(newMemberId)) { // Dùng .add() để thêm và kiểm tra
					newMembers.add(newMemberId);
				}
			}

			if (newMembers.isEmpty()) {
				sendSystemMessage("Hệ thống: Không có thành viên mới nào được thêm (có thể họ đã ở trong nhóm).");
				return;
			}
String joinMsg = "Hệ thống: Bạn vừa được " + this.fullName + " thêm vào nhóm '" + group.groupFullName
					+ "'.";
			for (String memberId : newMembers) {
				ClientHandler handler = clients.get(memberId);
				if (handler != null) {
					handler.sendSystemMessage(joinMsg);
				}
			}

			String addedMsg = "Hệ thống: " + this.fullName + " vừa thêm " + newMembers.size()
					+ " thành viên mới vào nhóm.";
			for (String memberId : group.members) {
				if (!newMembers.contains(memberId)) {
					ClientHandler handler = clients.get(memberId);
					if (handler != null) {
						handler.sendSystemMessage(addedMsg);
					}
				}
			}

			addSystemLog(this.fullName + " đã thêm " + newMembers.size() + " thành viên vào nhóm " + group.groupName);
			broadcastUserListUpdate();
		}

		// CÁC HÀM XỬ LÝ ĐIỀU KHIỂN VOIP (VẪN DÙNG TCP)
		// [ServerAdmin.java] - Bên trong class ClientHandler

		// [File: ServerAdmin.java] -> Class ClientHandler

		private void handleVoiceCallRequest() throws IOException {
		    String targetId = dis.readUTF(); // ID người nhận hoặc ID nhóm
		    
		    // 1. KIỂM TRA XEM CÓ PHẢI GỌI NHÓM KHÔNG?
		    if (groups.containsKey(targetId)) {
		        GroupInfo group = groups.get(targetId);
		        addSystemLog("SERVER: Yêu cầu gọi NHÓM (Voice): " + this.clientId + " -> Nhóm " + group.groupFullName);
		        
		        // Đánh dấu người gọi đang bận
		        activeCalls.put(this.clientId, targetId); 
		        
		        // Gửi lời mời cho TẤT CẢ thành viên trong nhóm (trừ người gọi)
		        for (String memberId : group.members) {
		            if (memberId.equals(this.clientId)) continue; // Bỏ qua chính mình
		            
		            ClientHandler memberHandler = clients.get(memberId);
		            if (memberHandler != null) {
		                // Gửi thông báo đến thành viên
		                // Tham số 1: targetId (ID Nhóm) -> Để client bên kia biết là cuộc gọi từ nhóm
		                // Tham số 2: Tên hiển thị
		                memberHandler.sendVoiceCallIncoming(targetId, this.fullName + " (Nhóm: " + group.groupFullName + ")");
		            }
		        }
		    } 
		    // 2. NẾU KHÔNG PHẢI NHÓM -> XỬ LÝ GỌI 1-1 NHƯ CŨ
		    else {
		        ClientHandler targetHandler = clients.get(targetId);
		        addSystemLog("SERVER: Yêu cầu gọi 1-1: " + this.clientId + " -> " + targetId);
		        
		        if (targetHandler != null && !activeCalls.containsKey(targetId)) {
		            targetHandler.sendVoiceCallIncoming(this.clientId, this.fullName);
		        } else {
		            this.sendVoiceCallDeclined(targetId);
		            if (targetHandler == null) addSystemLog("Server: Từ chối do " + targetId + " offline.");
		            else addSystemLog("Server: Từ chối do " + targetId + " đang bận.");
		        }
		    }
		}

		// [ServerAdmin.java] - Bên trong class ClientHandler
// [File: ServerAdmin.java] -> Trong class ClientHandler

		// [File: ServerAdmin.java] -> Trong class ClientHandler

		private void handleVoiceCallAccept() throws IOException {
		    String contextId = dis.readUTF(); // Đây là User ID (nếu 1-1) hoặc Group ID (nếu gọi nhóm)
		    
		    // 1. XỬ LÝ CHẤP NHẬN GỌI NHÓM
		    if (groups.containsKey(contextId)) {
		        addSystemLog("SERVER: User " + this.clientId + " đã chấp nhận tham gia Voice Nhóm " + contextId);
		        
		        // Đưa người chấp nhận vào trạng thái gọi với nhóm
		        activeCalls.put(this.clientId, contextId);
		        
		        // Gửi xác nhận ngược lại cho Client để bắt đầu thu âm/phát loa
		        this.sendVoiceCallAccepted(contextId);
		    } 
		    // 2. XỬ LÝ CHẤP NHẬN GỌI 1-1
		    else {
		        String callerUsername = contextId;
		        ClientHandler callerHandler = clients.get(callerUsername);
		        addSystemLog("SERVER: Chấp nhận Voice Call 1-1: " + this.clientId + " -> " + callerUsername);
		        
		        if (callerHandler != null) {
		            activeCalls.put(this.clientId, callerUsername);
		            activeCalls.put(callerUsername, this.clientId);
		            callerHandler.sendVoiceCallAccepted(this.clientId);
		        } else {
		            addSystemLog("Lỗi chấp nhận: " + callerUsername + " đã offline.");
		        }
		    }
		}

		private void handleVoiceCallDecline() throws IOException {
			String callerUsername = dis.readUTF();
			ClientHandler callerHandler = clients.get(callerUsername);
			addSystemLog("Từ chối gọi (Voice): " + this.clientId + " -> " + callerUsername);
			if (callerHandler != null) {
				callerHandler.sendVoiceCallDeclined(this.clientId);
			}
		}

		private void handleVoiceCallHangup() throws IOException {
			String partnerUsername = dis.readUTF();
			addSystemLog("Gác máy (Chung): " + this.clientId + " -> " + partnerUsername);
			endCall(this.clientId, partnerUsername);
		}
// === HÀM handleVoiceCallData() ĐÃ BỊ XÓA ===

		// CÁC HÀM XỬ LÝ ĐIỀU KHIỂN VIDEO (VẪN DÙNG TCP)
		// [File: ServerAdmin.java] -> Trong class ClientHandler

		private void handleVideoCallRequest() throws IOException {
		    String targetId = dis.readUTF(); // Đọc ID người nhận hoặc ID nhóm
		    
		    // 1. KIỂM TRA XEM CÓ PHẢI GỌI NHÓM VIDEO KHÔNG?
		    if (groups.containsKey(targetId)) {
		        GroupInfo group = groups.get(targetId);
		        addSystemLog("SERVER: Yêu cầu VIDEO CALL NHÓM: " + this.clientId + " -> Nhóm " + group.groupFullName);
		        
		        // Đánh dấu người gọi đang bận trong nhóm này
		        activeCalls.put(this.clientId, targetId); 
		        
		        // Gửi lời mời video cho TẤT CẢ thành viên trong nhóm (trừ người gọi)
		        for (String memberId : group.members) {
		            if (memberId.equals(this.clientId)) continue; // Bỏ qua chính mình
ClientHandler memberHandler = clients.get(memberId);
		            if (memberHandler != null) {
		                // Gửi thông báo cuộc gọi video đến
		                // Tham số 1: targetId (ID Nhóm) -> Để client biết đây là gọi nhóm
		                // Tham số 2: Tên hiển thị kèm tên nhóm
		                memberHandler.sendVideoCallIncoming(targetId, this.fullName + " (Video Nhóm: " + group.groupFullName + ")");
		            }
		        }
		    } 
		    // 2. NẾU KHÔNG PHẢI NHÓM -> XỬ LÝ GỌI VIDEO 1-1 NHƯ CŨ
		    else {
		        ClientHandler targetHandler = clients.get(targetId);
		        addSystemLog("SERVER: Yêu cầu VIDEO CALL 1-1: " + this.clientId + " -> " + targetId);
		        
		        if (targetHandler != null && !activeCalls.containsKey(targetId)) {
		            targetHandler.sendVideoCallIncoming(this.clientId, this.fullName);
		        } else {
		            this.sendVideoCallDeclined(targetId); 
		            if (targetHandler == null) addSystemLog("Server: Từ chối video do " + targetId + " offline.");
		            else addSystemLog("Server: Từ chối video do " + targetId + " đang bận.");
		        }
		    }
		}

		// [File: ServerAdmin.java] -> Trong class ClientHandler

		private void handleVideoCallAccept() throws IOException {
		    String contextId = dis.readUTF(); // Đây là User ID (nếu 1-1) hoặc Group ID (nếu gọi nhóm)
		    
		    // 1. XỬ LÝ CHẤP NHẬN GỌI NHÓM
		    if (groups.containsKey(contextId)) {
		        addSystemLog("SERVER: User " + this.clientId + " đã chấp nhận tham gia Video Nhóm " + contextId);
		        
		        // Đưa người chấp nhận vào trạng thái gọi với nhóm
		        activeCalls.put(this.clientId, contextId);
		        
		        // Gửi xác nhận để Client bắt đầu bật Camera/Mic
		        this.sendVideoCallAccepted(contextId);
		    } 
		    // 2. XỬ LÝ CHẤP NHẬN GỌI 1-1
		    else {
		        String callerUsername = contextId;
		        ClientHandler callerHandler = clients.get(callerUsername);
		        addSystemLog("SERVER: Chấp nhận VIDEO CALL 1-1: " + this.clientId + " -> " + callerUsername);
		        
		        if (callerHandler != null) {
		            activeCalls.put(this.clientId, callerUsername);
		            activeCalls.put(callerUsername, this.clientId);
		            callerHandler.sendVideoCallAccepted(this.clientId);
		        } else {
		            addSystemLog("Lỗi chấp nhận: " + callerUsername + " đã offline.");
		        }
		    }
		}

		private void handleVideoCallDecline() throws IOException {
			String callerUsername = dis.readUTF();
			ClientHandler callerHandler = clients.get(callerUsername);
			addSystemLog("Từ chối VIDEO CALL: " + this.clientId + " -> " + callerUsername);
			if (callerHandler != null) {
				callerHandler.sendVideoCallDeclined(this.clientId);
			}
		}

		// === HÀM handleVideoCallData() ĐÃ BỊ XÓA ===

		// Xử lý gửi file/location/voice (vẫn dùng TCP)
private void handleDataTransfer(String subDataType) throws IOException {
			String targetType = dis.readUTF();
			String targetName = dis.readUTF();

			if (subDataType.equals(DATA_TYPE_FILE) || subDataType.equals(DATA_TYPE_VOICE)) {
				String fileName = dis.readUTF();
				long fileSize = dis.readLong();
				File dir = new File("server_downloads");
				File savedFile = new File(dir, System.currentTimeMillis() + "_" + fileName);
				try (FileOutputStream fos = new FileOutputStream(savedFile)) {
					byte[] buffer = new byte[8192];
					long remaining = fileSize;
					while (remaining > 0) {
						int bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining));
						if (bytesRead == -1)
							break;
						fos.write(buffer, 0, bytesRead);
						remaining -= bytesRead;
					}
				}

				if (fileManagementPanel != null && subDataType.equals(DATA_TYPE_FILE)) {
					String targetDisplayName = targetName;
					if (targetType.equals("USER") && clients.containsKey(targetName)) {
						targetDisplayName = clients.get(targetName).fullName;
					} else if (targetType.equals("GROUP") && groups.containsKey(targetName)) {
						targetDisplayName = groups.get(targetName).groupFullName;
					}
					fileManagementPanel.addFile(this.fullName, targetDisplayName, fileName);
				}

				if (targetType.equals("USER")) {
					ClientHandler targetHandler = clients.get(targetName);
					if (targetHandler != null) {
						addSystemLog(this.fullName + " -> " + targetHandler.fullName + " (gửi " + fileName + ")");

						// =========================================================
						// (*** ĐÂY LÀ CHỖ SỬA LỖI ***)
						// Tham số thứ 3 (chatTargetId) phải là ID của người gửi (this.clientId)
						// =========================================================
						forwardFileToHandler(targetHandler, this.clientId, this.clientId, this.fullName, savedFile,
								fileName, subDataType);
					}
				} else if (targetType.equals("GROUP")) {
					GroupInfo group = groups.get(targetName);
					if (group != null) {
						addSystemLog(this.fullName + " -> [" + group.groupFullName + "] (gửi " + fileName + ")");
						for (String memberName : group.members) {
							if (memberName.equals(this.clientId))
								continue;
							ClientHandler memberHandler = clients.get(memberName);
							if (memberHandler != null) {
								// Đối với nhóm, chatTargetId (tham số 3) là chính groupName (targetName)
								forwardFileToHandler(memberHandler, targetName, targetName, this.fullName, savedFile,
										fileName, subDataType);
							}
						}
					}
				}

			} else if (subDataType.equals(DATA_TYPE_LOCATION)) {
				String lat = dis.readUTF();
				String lon = dis.readUTF();
				String time = dis.readUTF();
				String weather = dis.readUTF();

				if (targetType.equals("USER")) {
					ClientHandler targetHandler = clients.get(targetName);
					if (targetHandler != null) {
// =========================================================
						// (*** ĐÂY LÀ CHỖ SỬA LỖI ***)
						// Tham số thứ 3 (chatTargetId) phải là ID của người gửi (this.clientId)
						// =========================================================
						forwardLocationToHandler(targetHandler, this.clientId, this.clientId, this.fullName, lat, lon,
								time, weather);
					}
				} else if (targetType.equals("GROUP")) {
					GroupInfo group = groups.get(targetName);
					if (group != null) {
						for (String memberName : group.members) {
							if (memberName.equals(this.clientId))
								continue;
							ClientHandler memberHandler = clients.get(memberName);
							if (memberHandler != null) {
								// Đối với nhóm, chatTargetId (tham số 3) là chính groupName (targetName)
								forwardLocationToHandler(memberHandler, targetName, targetName, this.fullName, lat, lon,
										time, weather);
							}
						}
					}
				}
			}
		}

		private void forwardFileToHandler(ClientHandler targetHandler, String fromContext, String chatTargetId,
				String senderFullName, File savedFile, String originalFileName, String subDataType) throws IOException {
			synchronized (targetHandler.dos) {
				targetHandler.dos
						.writeInt(subDataType.equals(DATA_TYPE_FILE) ? TYPE_FILE_TRANSFER : TYPE_VOICE_MESSAGE);
				targetHandler.dos.writeUTF(fromContext);
				targetHandler.dos.writeUTF(originalFileName);
				targetHandler.dos.writeLong(savedFile.length());

				try (FileInputStream fis = new FileInputStream(savedFile)) {
					byte[] buffer = new byte[8192];
					int bytesRead;
					while ((bytesRead = fis.read(buffer)) != -1) {
						targetHandler.dos.write(buffer, 0, bytesRead);
					}
				}
				targetHandler.dos.writeUTF(senderFullName);
				targetHandler.dos.writeUTF(chatTargetId);
				targetHandler.dos.flush();
			}
		}

		private void forwardLocationToHandler(ClientHandler targetHandler, String fromContext, String chatTargetId,
				String senderFullName, String lat, String lon, String time, String weather) throws IOException {
			synchronized (targetHandler.dos) {
				targetHandler.dos.writeInt(TYPE_LOCATION_SHARE);
				targetHandler.dos.writeUTF(fromContext);
				targetHandler.dos.writeUTF(lat);
				targetHandler.dos.writeUTF(lon);
				targetHandler.dos.writeUTF(time);
				targetHandler.dos.writeUTF(weather);
				targetHandler.dos.writeUTF(senderFullName);
				targetHandler.dos.writeUTF(chatTargetId);
				targetHandler.dos.flush();
			}
		}

		// (*** THAY ĐỔI Ở ĐÂY ***)
		// Gửi danh sách user/group (vẫn dùng TCP)
		public void sendUserListUpdate(ConcurrentHashMap<String, ClientHandler> userMap,
				Collection<GroupInfo> filteredGroups) {
			try {
				dos.writeInt(TYPE_USER_LIST_UPDATE);

				// Đếm số user (không bao gồm chính mình)
				dos.writeInt(userMap.size() - 1);
				for (ClientHandler handler : userMap.values()) {
					if (handler.clientId.equals(this.clientId))
continue; // Bỏ qua chính mình
					dos.writeUTF(handler.clientId); // SĐT (ID)
					dos.writeUTF(handler.fullName); // Họ Tên (Hiển thị)

					// (THAY ĐỔI) - Gửi trạng thái online
					// Vì map 'clients' chỉ chứa user online, ta luôn gửi 'true'
					dos.writeBoolean(true);
				}

				dos.writeInt(filteredGroups.size());
				for (GroupInfo group : filteredGroups) {
					dos.writeUTF(group.groupName); // Gửi ID
					dos.writeUTF(group.groupFullName); // Gửi Tên Hiển Thị
				}
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void sendPrivateMessage(String fromChatContext, String message) {
			try {
				dos.writeInt(TYPE_RECEIVE_PRIVATE_MESSAGE);
				dos.writeUTF(fromChatContext);
				dos.writeUTF(message);
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void sendGroupMessage(String groupName, String message) {
			try {
				dos.writeInt(TYPE_RECEIVE_GROUP_MESSAGE);
				dos.writeUTF(groupName);
				dos.writeUTF(message);
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void sendSystemMessage(String message) {
			try {
				dos.writeInt(TYPE_SYSTEM_MESSAGE);
				dos.writeUTF(message);
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// CÁC HÀM GỬI LỆNH ĐIỀU KHIỂN VOIP (VẪN DÙNG TCP)
		public void sendVoiceCallIncoming(String fromId, String fromFullName) {
			try {
				dos.writeInt(TYPE_VOICE_CALL_INCOMING);
				dos.writeUTF(fromId);
				dos.writeUTF(fromFullName);
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void sendVoiceCallAccepted(String fromId) {
			try {
				dos.writeInt(TYPE_VOICE_CALL_ACCEPTED);
				dos.writeUTF(fromId);
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void sendVoiceCallDeclined(String fromId) {
			try {
				dos.writeInt(TYPE_VOICE_CALL_DECLINED);
				dos.writeUTF(fromId);
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// === HÀM sendVoiceCallData() ĐÃ BỊ XÓA ===

		// CÁC HÀM GỬI LỆNH ĐIỀU KHIỂN VIDEO (VẪN DÙNG TCP)
		public void sendVideoCallIncoming(String fromId, String fromFullName) {
			try {
				dos.writeInt(TYPE_VIDEO_CALL_INCOMING);
				dos.writeUTF(fromId);
				dos.writeUTF(fromFullName);
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void sendVideoCallAccepted(String fromId) {
			try {
				dos.writeInt(TYPE_VIDEO_CALL_ACCEPTED);
				dos.writeUTF(fromId);
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void sendVideoCallDeclined(String fromId) {
			try {
				dos.writeInt(TYPE_VIDEO_CALL_DECLINED);
				dos.writeUTF(fromId);
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// === HÀM sendVideoCallData() ĐÃ BỊ XÓA ===
} // Hết class ClientHandler

	// =================================================================================
	// CÁC HÀM HỖ TRỢ GIAO DIỆN (Giữ nguyên)
	// =================================================================================
	static class RoundedPanel extends JPanel {
		private final Color backgroundColor;
		private final int cornerRadius = 20;

		public RoundedPanel(LayoutManager layout, Color backgroundColor) {
			super(layout);
			this.backgroundColor = backgroundColor;
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(backgroundColor);
			g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
			g2d.dispose();
		}
	}

	private static JPanel createMessageBubble(String message, Color bgColor, boolean alignRight, boolean isSystem) {
		JPanel alignmentPanel = new JPanel(
				new FlowLayout(alignRight ? FlowLayout.RIGHT : (isSystem ? FlowLayout.CENTER : FlowLayout.LEFT), 0, 0));
		alignmentPanel.setOpaque(false);
		alignmentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		RoundedPanel bubble = new RoundedPanel(new BorderLayout(), bgColor);

		String safeMessage = message.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
		JLabel label = new JLabel("<html><body>" + safeMessage + "</body></html>");
		label.setFont(isSystem ? UI_FONT_BOLD : UI_FONT);
		label.setForeground(alignRight && !isSystem ? Color.WHITE : Color.BLACK);
		label.setBorder(new EmptyBorder(8, 12, 8, 12));
		bubble.add(label, BorderLayout.CENTER);
		alignmentPanel.add(bubble);
		alignmentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, bubble.getPreferredSize().height));
		return alignmentPanel;
	}

	private static void scrollToBottom(JScrollPane scrollPane) {
		SwingUtilities.invokeLater(() -> {
			if (scrollPane != null) {
				JScrollBar vertical = scrollPane.getVerticalScrollBar();
				vertical.setValue(vertical.getMaximum());
			}
		});
	}

	// =========================================================================
	// (Giữ nguyên) - CÁC HÀM HELPER CHO LOGIC PHÒNG CHAT
	// =========================================================================

	/**
	 * Tìm một GroupInfo bằng Tên Hiển Thị (groupFullName)
	 */
	private static GroupInfo findGroupByName(String fullName) {
		if (fullName == null)
			return null;
		for (GroupInfo group : groups.values()) {
			if (fullName.equals(group.groupFullName)) {
				return group;
			}
		}
		return null;
	}

	/**
	 * Cập nhật JList thành viên dựa trên nhóm được chọn
	 */
	private static void updateMemberPanel(String selectedGroupFullName) {
		// Luôn chạy trên luồng EDT
		SwingUtilities.invokeLater(() -> {
			memberListModel.clear(); // Xóa danh sách cũ
GroupInfo group = findGroupByName(selectedGroupFullName);

			if (group != null) {
				// Lặp qua danh sách ID thành viên
				for (String memberId : group.members) {
					// Tìm fullName từ ID
					ClientHandler handler = clients.get(memberId);
					String fullName = memberId; // Mặc định là ID
					if (handler != null) {
						fullName = handler.fullName; // Cập nhật tên nếu online
					} else {
						// (Nâng cao): Bạn có thể truy vấn DB ở đây để lấy tên
// người offline, nhưng sẽ chậm. Tạm thời dùng ID.
						fullName = memberId + " (Offline)";
					}

					// Thêm vào JList thành viên
					memberListModel.addElement(new UserDisplay(memberId, fullName));
				}
			}
		});
	}

} // Hết class ServerAdmin
 