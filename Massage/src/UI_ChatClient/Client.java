package UI_ChatClient;

import UI_ChatClient.controller.*;
import UI_ChatClient.model.*;
import UI_ChatClient.view.components.*;
import UI_ChatClient.view.dialogs.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Client Chat Application - Main Frame
 * Theo mô hình MVC
 */
public class Client extends JFrame {

    private static final long serialVersionUID = 1L;
    
    // === STATE ===
    private ChatState chatState;
    
    // === CONTROLLERS ===
    private NetworkController networkController;
    private AudioController audioController;
    private LocationController locationController;
    
    // === UI COMPONENTS ===
    private JPanel contentPane;
    private JTextField txtMessageInput;
    private JList<UserDisplay> userList;
    private DefaultListModel<UserDisplay> userListModel;
    
    private JButton btnFile, btnMic, btnLocation, btnSend;
    private JButton btnUser, btnCall, btnVideo;
    private JLabel lblChattingWith;
    
    private JPanel chatWindowsPanel;
    private CardLayout cardLayout;
    private Map<String, JPanel> chatPanes;
    private Map<String, JScrollPane> chatScrollPanes;
    
    // === MAIN ===
    public static void main(String[] args) {
        try { 
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        
        EventQueue.invokeLater(() -> {
            try {
                String username = JOptionPane.showInputDialog(null, "Nhập SĐT để test:", "Test Client", JOptionPane.PLAIN_MESSAGE);
                String fullname = JOptionPane.showInputDialog(null, "Nhập Tên để test:", "Test Client", JOptionPane.PLAIN_MESSAGE);
                if (username != null && !username.trim().isEmpty()) {
                    Client frame = new Client(username.trim(), fullname.trim());
                    frame.setVisible(true);
                } else {
                    System.exit(0);
                }
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
        });
    }
    
    public Client(String username, String fullName) {
        // Initialize state
        this.chatState = new ChatState(username, fullName);
        this.chatPanes = new HashMap<>();
        this.chatScrollPanes = new HashMap<>();
        
        // Initialize controllers
        initControllers();
        
        // Initialize UI
        initUI(fullName);
        
        // Create initial tabs
        createNewChatTab("WELCOME", "Hệ thống");
        createNewChatTab("Server (Admin)", "Server (Admin)");
        
        addSystemMessage_Safe("WELCOME", "Đang kết nối đến server...");
        
        // Connect to server
        connectToServer(username);
        
        // Add actions
addActions();
    }
    
    private void initControllers() {
        // Audio Controller
        audioController = new AudioController();
        audioController.setCallback(new AudioController.RecordingCallback() {
            @Override
            public void onRecordingStarted() {
                // Just change icon, no system message
                btnMic.setIcon(loadIcon("stop.png", 22, 22));
            }
            
            @Override
            public void onRecordingStopped(File audioFile) {
                btnMic.setIcon(loadIcon("mic.png", 22, 22));
                JPanel voicePanel = createVoiceMessagePanel("Tin nhắn thoại:", audioFile, true);
                addComponentToChat(chatState.getCurrentChatTarget(), voicePanel);
                sendVoiceMessage(audioFile);
            }
            
            @Override
            public void onRecordingError(String message) {
                addSystemMessage_Safe(chatState.getCurrentChatTarget(), message);
            }
        });
        
        // Location Controller
        locationController = new LocationController();
        
        // Network Controller
        networkController = new NetworkController(chatState, new NetworkController.MessageHandler() {
            @Override
            public void onUserListUpdate(DataInputStream dis) throws IOException {
                handleUserListUpdate(dis);
            }
            
            @Override
            public void onPrivateMessage(DataInputStream dis) throws IOException {
                handleReceivePrivateMessage(dis);
            }
            
            @Override
            public void onGroupMessage(DataInputStream dis) throws IOException {
                handleReceiveGroupMessage(dis);
            }
            
            @Override
            public void onSystemMessage(String message) {
                String target = (chatState.getCurrentChatTarget() != null) ? chatState.getCurrentChatTarget() : "WELCOME";
                addSystemMessage_Safe(target, message);
            }
            
            @Override
            public void onFileReceived(DataInputStream dis) throws IOException {
                receiveFile(dis);
            }
            
            @Override
            public void onLocationReceived(DataInputStream dis) throws IOException {
                receiveLocation(dis);
            }
            
            @Override
            public void onVoiceMessageReceived(DataInputStream dis) throws IOException {
                receiveVoiceMessage(dis);
            }
            
            @Override
            public void onCallIncoming(String fromUser, String fromFullName, boolean isVideo) {
                SwingUtilities.invokeLater(() -> {
                    IncomingCallDialog callDialog = new IncomingCallDialog(Client.this, fromFullName, isVideo);
                    callDialog.setVisible(true);
                    
                    try {
if (callDialog.isAccepted()) {
                            if (isVideo) {
                                networkController.sendVideoCallAccept(fromUser);
                                networkController.startVideoCallSession(fromUser, fromFullName, Client.this);
                            } else {
                                networkController.sendVoiceCallAccept(fromUser);
                                networkController.startCallSession(fromUser, fromFullName, Client.this);
                            }
                        } else {
                            if (isVideo) {
                                networkController.sendVideoCallDecline(fromUser);
                            } else {
                                networkController.sendVoiceCallDecline(fromUser);
                            }
                        }
                    } catch (IOException e) {
                        addSystemMessage_Safe(fromUser, "Lỗi xử lý cuộc gọi: " + e.getMessage());
                    }
                });
            }
            
            @Override
            public void onCallAccepted(String targetUser, boolean isVideo) {
                SwingUtilities.invokeLater(() -> {
                    networkController.closeOutgoingCallDialog();
                });
                String targetFullName = getFullNameForUser(targetUser);
                if (isVideo) {
                    networkController.startVideoCallSession(targetUser, targetFullName, Client.this);
                } else {
                    networkController.startCallSession(targetUser, targetFullName, Client.this);
                }
            }
            
            @Override
            public void onCallDeclined(String targetUser, boolean isVideo) {
                SwingUtilities.invokeLater(() -> {
                    // Đóng dialog và dừng cuộc gọi nếu đang có (không gửi hangup vì server đã từ chối)
                    networkController.closeOutgoingCallDialog();
                    networkController.stopCallWithoutSignal();
                    resetCallUI();
                    JOptionPane.showMessageDialog(Client.this, 
                        (isVideo ? "Video call" : "Cuộc gọi") + " đã bị từ chối", 
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                });
            }
            
            @Override
            public void onCallEnded(String fromUser, boolean isVideo) {
                SwingUtilities.invokeLater(() -> {
                    // Cuộc gọi kết thúc từ server, không cần gửi hangup
                    networkController.stopCallWithoutSignal();
                    resetCallUI();
                });
            }
            
            @Override
            public void onCallStoppedLocally() {
                // Được gọi khi cuộc gọi kết thúc từ ActiveCallWindow (bấm nút kết thúc)
                resetCallUI();
            }
        });
    }
    
    private ImageIcon loadIcon(String name, int w, int h) {
        URL url = getClass().getResource("../icons/" + name);
        if (url == null) {
            return new ImageIcon(new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB));
        }
        ImageIcon icon = new ImageIcon(url);
        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
    
    private void initUI(String fullName) {
        setTitle("ChatSphere - " + fullName);
setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                networkController.close();
                System.exit(0);
            }
        });
        
        setBounds(100, 100, 900, 633);
        setLocationRelativeTo(null);
        
        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(Color.WHITE);
        setContentPane(contentPane);
        
        // === 1. SIDEBAR ===
        JPanel sidebarPanel = createSidebarPanel(fullName);
        contentPane.add(sidebarPanel, BorderLayout.WEST);
        
        // === 2. MAIN CHAT PANEL ===
        JPanel mainPanel = createMainChatPanel();
        contentPane.add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createSidebarPanel(String fullName) {
        JPanel sidebarPanel = new JPanel(new BorderLayout(0, 0));
        sidebarPanel.setPreferredSize(new Dimension(280, 0));
        sidebarPanel.setBackground(Constants.SIDEBAR_BG_COLOR);
        sidebarPanel.setBorder(null);
        
        // Header
        JPanel sidebarHeaderPanel = new JPanel(new BorderLayout());
        sidebarHeaderPanel.setBackground(Constants.SIDEBAR_BG_COLOR);
        sidebarHeaderPanel.setBorder(new EmptyBorder(20, 20, 15, 20));
        
        JLabel lblAppName = new JLabel("ChatSphere");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAppName.setForeground(Color.WHITE);
        
        JButton btnNewGroup = new JButton("+");
        btnNewGroup.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnNewGroup.setForeground(Color.WHITE);
        btnNewGroup.setBackground(Constants.PRIMARY_COLOR);
        btnNewGroup.setBorder(new EmptyBorder(5, 12, 5, 12));
        btnNewGroup.setFocusPainted(false);
        btnNewGroup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNewGroup.setToolTipText("Tạo nhóm mới");
        btnNewGroup.addActionListener(e -> showCreateGroupDialog());
        
        sidebarHeaderPanel.add(lblAppName, BorderLayout.WEST);
        sidebarHeaderPanel.add(btnNewGroup, BorderLayout.EAST);
        sidebarPanel.add(sidebarHeaderPanel, BorderLayout.NORTH);
        
        // User list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(Constants.SIDEBAR_BG_COLOR);
        userList.setSelectionBackground(Constants.HOVER_COLOR);
        userList.setSelectionForeground(Color.WHITE);
        userList.setFixedCellHeight(65);
        userList.setBorder(new EmptyBorder(5, 0, 5, 0));
        userList.setCellRenderer(new UserListCellRenderer());
        
        JScrollPane userListScrollPane = new JScrollPane(userList);
        userListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        userListScrollPane.getViewport().setBackground(Constants.SIDEBAR_BG_COLOR);
sidebarPanel.add(userListScrollPane, BorderLayout.CENTER);
        
        UserDisplay serverUser = new UserDisplay("Server (Admin)", "Server (Admin)", false, true);
        userListModel.addElement(serverUser);
        
        // Footer
        JPanel sidebarFooterPanel = new JPanel(new BorderLayout(10, 0));
        sidebarFooterPanel.setBackground(new Color(31, 41, 55));
        sidebarFooterPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel lblCurrentUser = new JLabel(" " + fullName);
        lblCurrentUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCurrentUser.setForeground(Constants.SIDEBAR_TEXT_COLOR);
        lblCurrentUser.setIcon(loadIcon("avatar.jpg", 36, 36));
        
        JButton btnLogout = new JButton();
        btnLogout.setIcon(loadIcon("logout.png", 24, 24));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(null);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setPreferredSize(new Dimension(34, 34));
        btnLogout.setToolTipText("Đăng xuất");
        btnLogout.addActionListener(e -> {
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        
        sidebarFooterPanel.add(lblCurrentUser, BorderLayout.CENTER);
        sidebarFooterPanel.add(btnLogout, BorderLayout.EAST);
        sidebarPanel.add(sidebarFooterPanel, BorderLayout.SOUTH);
        
        return sidebarPanel;
    }
    
    private JPanel createMainChatPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);
        
        // Header
        JPanel chatHeaderPanel = new JPanel(new BorderLayout());
        chatHeaderPanel.setBackground(Color.WHITE);
        chatHeaderPanel.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        chatHeaderPanel.setPreferredSize(new Dimension(0, 55));
        
        lblChattingWith = new JLabel(" Chọn 1 người để chat");
        lblChattingWith.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblChattingWith.setIcon(loadIcon("avatar.jpg", 32, 32));
        lblChattingWith.setBorder(new EmptyBorder(0, 15, 0, 0));
        
        JPanel chatHeaderIconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        chatHeaderIconsPanel.setOpaque(false);
        chatHeaderIconsPanel.setBorder(new EmptyBorder(0, 0, 0, 15));
        
        btnCall = createHeaderButton("call.png", "Bắt đầu cuộc gọi");
        btnVideo = createHeaderButton("video.png", "Bắt đầu video call");
        JButton btnCaution = createHeaderButton("caution.png", "Thông tin");
        btnUser = createHeaderButton("user.png", "Tạo nhóm mới");
        
        chatHeaderIconsPanel.add(btnCall);
        chatHeaderIconsPanel.add(btnVideo);
        chatHeaderIconsPanel.add(btnCaution);
        chatHeaderIconsPanel.add(btnUser);
chatHeaderPanel.add(lblChattingWith, BorderLayout.WEST);
        chatHeaderPanel.add(chatHeaderIconsPanel, BorderLayout.EAST);
        
        mainPanel.add(chatHeaderPanel, BorderLayout.NORTH);
        
        // Chat windows
        cardLayout = new CardLayout();
        chatWindowsPanel = new JPanel(cardLayout);
        chatWindowsPanel.setBackground(Constants.CHAT_BG_COLOR);
        
        // Welcome Panel
        JPanel welcomePanel = createWelcomePanel();
        chatWindowsPanel.add(welcomePanel, "WELCOME_PANEL");
        mainPanel.add(chatWindowsPanel, BorderLayout.CENTER);
        cardLayout.show(chatWindowsPanel, "WELCOME_PANEL");
        
        // Input panel
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private JButton createHeaderButton(String iconName, String tooltip) {
        JButton btn = new JButton();
        btn.setIcon(loadIcon(iconName, 24, 24));
        btn.setPreferredSize(new Dimension(34, 34));
        btn.setToolTipText(tooltip);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(Constants.CHAT_BG_COLOR);
        
        JPanel welcomeContent = new JPanel();
        welcomeContent.setLayout(new BoxLayout(welcomeContent, BoxLayout.Y_AXIS));
        welcomeContent.setOpaque(false);
        
        JLabel welcomeEmoji = new JLabel("👋", SwingConstants.CENTER);
        welcomeEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        welcomeEmoji.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel welcomeTitle = new JLabel("Chào mừng, " + chatState.getMyFullName() + "!");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeTitle.setForeground(new Color(31, 41, 55));
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel welcomeSubtitle = new JLabel("Chọn một cuộc trò chuyện ở thanh bên trái để bắt đầu");
        welcomeSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeSubtitle.setForeground(new Color(107, 114, 128));
        welcomeSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        welcomeContent.add(welcomeEmoji);
        welcomeContent.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomeContent.add(welcomeTitle);
        welcomeContent.add(Box.createRigidArea(new Dimension(0, 10)));
        welcomeContent.add(welcomeSubtitle);
        
        welcomePanel.add(welcomeContent);
        return welcomePanel;
    }
    
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(12, 0));
inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(229, 231, 235)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionButtonPanel.setOpaque(false);
        
        btnFile = createActionButton("file.png", "Gửi file");
        btnMic = createActionButton("mic.png", "Ghi âm");
        btnLocation = createActionButton("location.png", "Gửi vị trí");
        
        actionButtonPanel.add(btnFile);
        actionButtonPanel.add(btnMic);
        actionButtonPanel.add(btnLocation);
        
        inputPanel.add(actionButtonPanel, BorderLayout.WEST);
        
        txtMessageInput = new RoundTextField(0);
        txtMessageInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMessageInput.setBackground(new Color(243, 244, 246));
        inputPanel.add(txtMessageInput, BorderLayout.CENTER);
        
        btnSend = new RoundButton(loadIcon("send.png", 20, 20));
        btnSend.setBackground(Constants.PRIMARY_COLOR);
        btnSend.setPreferredSize(new Dimension(44, 44));
        btnSend.setToolTipText("Gửi");
        inputPanel.add(btnSend, BorderLayout.EAST);
        
        return inputPanel;
    }
    
    private JButton createActionButton(String iconName, String tooltip) {
        JButton btn = new JButton();
        btn.setIcon(loadIcon(iconName, 22, 22));
        btn.setPreferredSize(new Dimension(38, 38));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);
        return btn;
    }
    
    // === CONNECTION ===
    
    private void connectToServer(String username) {
        try {
            networkController.connect(username);
            addSystemMessage_Safe("WELCOME", "Kết nối thành công!");
        } catch (IOException e) {
            addSystemMessage_Safe("WELCOME", "Không kết nối được tới server: " + e.getMessage());
        }
    }
    
    // === ACTIONS ===
    
    private void addActions() {
        // User selection
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                UserDisplay selectedUser = userList.getSelectedValue();
                
                if (selectedUser != null) {
                    chatState.setCurrentChatTarget(selectedUser.getUsername());
                    chatState.setCurrentChatIsGroup(selectedUser.isGroup());
                    
                    if (!chatPanes.containsKey(chatState.getCurrentChatTarget())) {
                        createNewChatTab(chatState.getCurrentChatTarget(), selectedUser.getFullName());
                    }
cardLayout.show(chatWindowsPanel, chatState.getCurrentChatTarget());
                    lblChattingWith.setText(" Đang chat với: " + selectedUser.getFullName());
                    
                    if (chatState.isCurrentChatIsGroup()) {
                        btnUser.setToolTipText("Thêm thành viên vào: " + selectedUser.getFullName());
                    } else {
                        btnUser.setToolTipText("Tạo nhóm mới");
                    }
                } else {
                    cardLayout.show(chatWindowsPanel, "WELCOME_PANEL");
                    lblChattingWith.setText(" Chọn 1 người để chat");
                    chatState.setCurrentChatTarget(null);
                    chatState.setCurrentChatIsGroup(false);
                    btnUser.setToolTipText("Tạo nhóm mới");
                }
            }
        });
        
        // User/Group button
        btnUser.addActionListener(e -> {
            if (chatState.isCurrentChatIsGroup() && chatState.getCurrentChatTarget() != null) {
                String groupFullName = chatState.getCurrentChatTarget();
                UserDisplay selectedGroup = userList.getSelectedValue();
                if (selectedGroup != null && selectedGroup.getUsername().equals(chatState.getCurrentChatTarget()) && selectedGroup.isGroup()) {
                    groupFullName = selectedGroup.getFullName();
                }
                showAddMembersDialog(chatState.getCurrentChatTarget(), groupFullName);
            } else {
                showCreateGroupDialog();
            }
        });
        
        // Call buttons
        btnCall.addActionListener(e -> {
            if (chatState.isInCall() || chatState.isInVideoCall()) {
                networkController.stopCall();
                resetCallUI();
            } else {
                if (chatState.getCurrentChatTarget() != null && !chatState.getCurrentChatTarget().equals("WELCOME")) {
                    startCallRequest(chatState.getCurrentChatTarget());
                } else {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn người hoặc nhóm để gọi.");
                }
            }
        });
        
        btnVideo.addActionListener(e -> {
            if (chatState.isInCall() || chatState.isInVideoCall()) {
                networkController.stopCall();
                resetCallUI();
            } else {
                if (chatState.getCurrentChatTarget() != null && !chatState.getCurrentChatTarget().equals("WELCOME")) {
                    startVideoCallRequest(chatState.getCurrentChatTarget());
                } else {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn người hoặc nhóm để gọi video.");
                }
            }
        });
        
        // Send message
        btnSend.addActionListener(e -> sendMessage());
        txtMessageInput.addActionListener(e -> sendMessage());
        
        // Send file
btnFile.addActionListener(e -> {
            if (chatState.getCurrentChatTarget() == null || 
                chatState.getCurrentChatTarget().equals("Server (Admin)") || 
                chatState.getCurrentChatTarget().equals("WELCOME")) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng hoặc nhóm (không phải Server) để gửi file.");
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                JPanel filePanel = createFilePanel("", file, true);
                addComponentToChat(chatState.getCurrentChatTarget(), filePanel);
                sendFile(file);
            }
        });
        
        // Record voice
        btnMic.addActionListener(e -> {
            if (chatState.isInCall() || chatState.isInVideoCall()) {
                JOptionPane.showMessageDialog(this, "Không thể ghi âm tin nhắn thoại khi đang trong cuộc gọi.");
                return;
            }
            if (chatState.getCurrentChatTarget() == null || 
                chatState.getCurrentChatTarget().equals("Server (Admin)") || 
                chatState.getCurrentChatTarget().equals("WELCOME")) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng hoặc nhóm (không phải Server) để gửi tin nhắn thoại.");
                return;
            }
            if (audioController.isRecording()) {
                audioController.stopRecordingAndSend();
            } else {
                audioController.startRecording();
            }
        });
        
        // Send location
        btnLocation.addActionListener(e -> {
            if (chatState.getCurrentChatTarget() == null || 
                chatState.getCurrentChatTarget().equals("Server (Admin)") || 
                chatState.getCurrentChatTarget().equals("WELCOME")) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng hoặc nhóm (không phải Server) để gửi vị trí.");
                return;
            }
            
            String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            locationController.getLocationAndWeather(new LocationController.LocationCallback() {
                @Override
                public void onLocationReceived(String lat, String lon, String weather, String mapLink) {
                    SwingUtilities.invokeLater(() -> {
                        JPanel locPanel = createLocationPanel("Vị trí:", mapLink, formattedTime, weather, true);
                        addComponentToChat(chatState.getCurrentChatTarget(), locPanel);
                        sendLocation(lat, lon, formattedTime, weather);
                    });
                }
                
                @Override
                public void onLocationError(String message) {
addSystemMessage_Safe(chatState.getCurrentChatTarget(), message);
                }
            });
        });
    }
    
    // === SEND METHODS ===
    
    private void sendMessage() {
        String message = txtMessageInput.getText().trim();
        if (message.isEmpty()) return;
        
        if (chatState.getCurrentChatTarget() == null || chatState.getCurrentChatTarget().equals("WELCOME")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng hoặc nhóm từ danh sách để chat.");
            return;
        }
        
        if (chatState.getCurrentChatTarget().equals("Server (Admin)")) {
            JOptionPane.showMessageDialog(this, "Bạn không thể gửi tin nhắn cho Server từ đây.");
            txtMessageInput.setText("");
            return;
        }
        
        try {
            if (chatState.isCurrentChatIsGroup()) {
                networkController.sendGroupMessage(chatState.getCurrentChatTarget(), message);
            } else {
                networkController.sendPrivateMessage(chatState.getCurrentChatTarget(), message);
            }
            
            addMessageToPanel(chatState.getCurrentChatTarget(), chatState.getMyFullName() + ": " + message, true, false);
            txtMessageInput.setText("");
            
        } catch (Exception e) {
            e.printStackTrace();
            addSystemMessage_Safe(chatState.getCurrentChatTarget(), "Lỗi gửi tin nhắn: " + e.getMessage());
        }
    }
    
    private void sendFile(File file) {
        try {
            String targetType = chatState.isCurrentChatIsGroup() ? "GROUP" : "USER";
            networkController.sendFile(targetType, chatState.getCurrentChatTarget(), file);
        } catch (IOException e) {
            addSystemMessage_Safe(chatState.getCurrentChatTarget(), "Lỗi gửi file: " + e.getMessage());
        }
    }
    
    private void sendLocation(String lat, String lon, String time, String weather) {
        try {
            String targetType = chatState.isCurrentChatIsGroup() ? "GROUP" : "USER";
            networkController.sendLocation(targetType, chatState.getCurrentChatTarget(), lat, lon, time, weather);
        } catch (IOException e) {
            addSystemMessage_Safe(chatState.getCurrentChatTarget(), "Lỗi gửi vị trí: " + e.getMessage());
        }
    }
    
    private void sendVoiceMessage(File file) {
        try {
            String targetType = chatState.isCurrentChatIsGroup() ? "GROUP" : "USER";
            networkController.sendVoiceMessage(targetType, chatState.getCurrentChatTarget(), file);
        } catch (IOException e) {
            addSystemMessage_Safe(chatState.getCurrentChatTarget(), "Lỗi gửi tin nhắn thoại: " + e.getMessage());
        }
    }
    
    // === CALL METHODS ===
    
    private void startCallRequest(String target) {
        if (chatState.isInCall() || chatState.isInVideoCall()) return;
        
        String targetFullName = getFullNameForUser(target);
try {
            networkController.sendVoiceCallRequest(target);
            
            btnCall.setIcon(loadIcon("hangup.png", 24, 24));
            btnCall.setToolTipText("Hủy cuộc gọi...");
            btnVideo.setEnabled(false);
            
            if (chatState.isCurrentChatIsGroup()) {
                networkController.startCallSession(target, targetFullName, this);
            } else {
                SwingUtilities.invokeLater(() -> {
                    OutgoingCallDialog dialog = new OutgoingCallDialog(this, targetFullName, false);
                    networkController.setOutgoingCallDialog(dialog);
                    dialog.setVisible(true);
                    
                    if (dialog.isCancelled()) {
                        try {
                            networkController.sendVoiceCallHangup(target);
                        } catch (IOException ex) { }
                        resetCallUI();
                    }
                });
            }
            
        } catch (IOException e) {
            addSystemMessage_Safe(target, "Lỗi khi thực hiện cuộc gọi: " + e.getMessage());
        }
    }
    
    private void startVideoCallRequest(String target) {
        if (chatState.isInCall() || chatState.isInVideoCall()) return;
        
        String targetFullName = getFullNameForUser(target);
        
        try {
            networkController.sendVideoCallRequest(target);
            
            btnVideo.setIcon(loadIcon("hangup.png", 24, 24));
            btnVideo.setToolTipText("Hủy video call...");
            btnCall.setEnabled(false);
            
            if (chatState.isCurrentChatIsGroup()) {
                networkController.startVideoCallSession(target, targetFullName, this);
            } else {
                SwingUtilities.invokeLater(() -> {
                    OutgoingCallDialog dialog = new OutgoingCallDialog(this, targetFullName, true);
                    networkController.setOutgoingCallDialog(dialog);
                    dialog.setVisible(true);
                    
                    if (dialog.isCancelled()) {
                        try {
                            networkController.sendVideoCallHangup(target);
                        } catch (IOException ex) { }
                        resetCallUI();
                    }
                });
            }
            
        } catch (IOException e) {
            addSystemMessage_Safe(target, "Lỗi khi thực hiện video call: " + e.getMessage());
        }
    }
    
    private void resetCallUI() {
        btnCall.setIcon(loadIcon("call.png", 24, 24));
        btnCall.setToolTipText("Gọi");
        btnCall.setEnabled(true);
        
        btnVideo.setIcon(loadIcon("video.png", 24, 24));
        btnVideo.setToolTipText("Gọi video");
        btnVideo.setEnabled(true);
    }
    
    // === MESSAGE HANDLERS ===
    
    private void handleUserListUpdate(DataInputStream dis) throws IOException {
int userCount = dis.readInt();
        DefaultListModel<UserDisplay> tempModel = new DefaultListModel<>();
        
        for (int i = 0; i < userCount; i++) {
            String username = dis.readUTF();
            String fullName = dis.readUTF();
            boolean isOnline = dis.readBoolean();
            tempModel.addElement(new UserDisplay(username, fullName, false, isOnline));
        }
        
        int groupCount = dis.readInt();
        for (int i = 0; i < groupCount; i++) {
            String groupName = dis.readUTF();
            String groupFullName = dis.readUTF();
            tempModel.addElement(new UserDisplay(groupName, groupFullName, true, false));
        }
        
        SwingUtilities.invokeLater(() -> {
            UserDisplay selected = userList.getSelectedValue();
            userListModel.clear();
            userListModel.addElement(new UserDisplay("Server (Admin)", "Server (Admin)", false, true));
            
            Enumeration<UserDisplay> elements = tempModel.elements();
            while (elements.hasMoreElements()) {
                userListModel.addElement(elements.nextElement());
            }
            
            if (selected != null) {
                for(int i = 0; i < userListModel.size(); i++) {
                    if (userListModel.getElementAt(i).getUsername().equals(selected.getUsername())) {
                        userList.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });
    }
    
    private void handleReceivePrivateMessage(DataInputStream dis) throws IOException {
        String fromUser = dis.readUTF();
        String message = dis.readUTF();
        addMessageToPanel(fromUser, message, false, false);
    }
    
    private void handleReceiveGroupMessage(DataInputStream dis) throws IOException {
        String groupName = dis.readUTF();
        String message = dis.readUTF();
        addMessageToPanel(groupName, message, false, false);
    }
    
    private void receiveFile(DataInputStream dis) throws IOException {
        dis.readUTF(); // fromContext
        String fileName = dis.readUTF();
        long fileSize = dis.readLong();
        File dir = new File("client_downloads");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            long remaining = fileSize;
            while (remaining > 0) {
                int bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (bytesRead == -1) break;
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }
        dis.readUTF(); // senderName
        String chatTarget = dis.readUTF();
        JPanel filePanel = createFilePanel("", file, false);
        addComponentToChat(chatTarget, filePanel);
    }
private void receiveVoiceMessage(DataInputStream dis) throws IOException {
        dis.readUTF(); // fromContext
        String fileName = dis.readUTF();
        long fileSize = dis.readLong();
        File dir = new File("client_downloads");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            long remaining = fileSize;
            while (remaining > 0) {
                int bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (bytesRead == -1) break;
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }
        dis.readUTF(); // senderName
        String chatTarget = dis.readUTF();
        JPanel voicePanel = createVoiceMessagePanel("Tin nhắn thoại:", file, false);
        addComponentToChat(chatTarget, voicePanel);
    }
    
    private void receiveLocation(DataInputStream dis) throws IOException {
        dis.readUTF(); // fromContext
        String lat = dis.readUTF();
        String lon = dis.readUTF();
        String time = dis.readUTF();
        String weather = dis.readUTF();
        String mapLink = String.format("https://www.google.com/maps?q=%s,%s", lat, lon);
        dis.readUTF(); // senderName
        String chatTarget = dis.readUTF();
        JPanel locationPanel = createLocationPanel("Vị trí:", mapLink, time, weather, false);
        addComponentToChat(chatTarget, locationPanel);
    }
    
    // === UI HELPER METHODS ===
    
    private void createNewChatTab(String username, String fullName) {
        JPanel newChatPanel = new JPanel();
        newChatPanel.setLayout(new BoxLayout(newChatPanel, BoxLayout.Y_AXIS));
        newChatPanel.setBackground(Constants.CHAT_BG_COLOR);
        newChatPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        chatPanes.put(username, newChatPanel);
        
        JScrollPane newScroll = new JScrollPane(newChatPanel);
        newScroll.setBorder(BorderFactory.createEmptyBorder());
        newScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        chatScrollPanes.put(username, newScroll);
        chatWindowsPanel.add(newScroll, username);
    }
    
    private void addMessageToPanel(String chatTarget, String message, boolean isMyMessage, boolean isSystem) {
        Color bgColor = isMyMessage ? Constants.MY_MESSAGE_COLOR : (isSystem ? Constants.SYSTEM_MESSAGE_COLOR : Constants.OTHER_MESSAGE_COLOR);
        JPanel bubblePanel = createMessageBubble(message, bgColor, isMyMessage, isSystem);
        addComponentToChat(chatTarget, bubblePanel);
    }
    
    private void addSystemMessage_Safe(String chatTarget, String msg) {
        addMessageToPanel(chatTarget, msg, false, true);
    }
    
    private void addComponentToChat(String chatTarget, JComponent component) {
if (!chatPanes.containsKey(chatTarget)) {
            String targetFullName = getFullNameForUser(chatTarget);
            createNewChatTab(chatTarget, targetFullName);
        }
        
        JPanel panel = chatPanes.get(chatTarget);
        
        if (panel == null) {
            System.err.println("NGHIÊM TRỌNG: Panel cho " + chatTarget + " là null!");
            panel = chatPanes.get("WELCOME");
            if (panel == null) {
                System.err.println("NGHIÊM TRỌNG: Panel WELCOME cũng là null!");
                return;
            }
        }
        
        final JPanel finalPanel = panel;
        SwingUtilities.invokeLater(() -> {
            finalPanel.add(component);
            finalPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            finalPanel.revalidate();
            finalPanel.repaint();
            
            JScrollPane scrollPane = chatScrollPanes.get(chatTarget);
            if (scrollPane == null) scrollPane = chatScrollPanes.get("WELCOME");
            
            if (scrollPane != null) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }
    
    private String getFullNameForUser(String username) {
        for(int i=0; i < userListModel.size(); i++) {
            if(userListModel.getElementAt(i).getUsername().equals(username)){
                return userListModel.getElementAt(i).getFullName();
            }
        }
        if (!username.equals("WELCOME")) {
            return "Nhóm: " + username;
        }
        return username;
    }
    
    // === PANEL CREATORS ===
    
    private JPanel createMessageBubble(String message, Color bgColor, boolean alignRight, boolean isSystem) {
        JPanel alignmentPanel = new JPanel(new FlowLayout(alignRight ? FlowLayout.RIGHT : (isSystem ? FlowLayout.CENTER : FlowLayout.LEFT), 0, 0));
        alignmentPanel.setOpaque(false);
        alignmentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        BubblePanel bubble = new BubblePanel(message, bgColor);
        if (alignRight && !isSystem) {
            bubble.setTextColor(Color.WHITE);
        } else if (!alignRight && !isSystem) {
            bubble.setTextColor(Color.BLACK);
        }
        alignmentPanel.add(bubble);
        alignmentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, bubble.getPreferredSize().height));
        return alignmentPanel;
    }
    
    private JPanel createFilePanel(String prefix, File file, boolean isMyFile) {
        JPanel alignmentPanel = new JPanel(new FlowLayout(isMyFile ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        alignmentPanel.setOpaque(false);
        alignmentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Modern file card with rounded corners and shadow
        JPanel fileBubble = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(3, 3, getWidth()-3, getHeight()-3, 16, 16);
                
                // Background with gradient
                Color bg1 = isMyFile ? new Color(16, 185, 129) : new Color(255, 255, 255);
                Color bg2 = isMyFile ? new Color(5, 150, 105) : new Color(249, 250, 251);
                GradientPaint gradient = new GradientPaint(0, 0, bg1, 0, getHeight(), bg2);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth()-3, getHeight()-3, 16, 16);
                
                // Border
                if (!isMyFile) {
                    g2.setColor(new Color(229, 231, 235));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth()-3, getHeight()-3, 16, 16);
                }
                
                g2.dispose();
            }
        };
        fileBubble.setOpaque(false);
        fileBubble.setBorder(new EmptyBorder(15, 15, 15, 15));
        fileBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fileBubble.setPreferredSize(new Dimension(320, 90));
        
        // Icon with circular background
        JPanel iconWrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color iconBg = isMyFile ? new Color(255, 255, 255, 30) : new Color(59, 130, 246, 15);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, 48, 48, 12, 12);
                g2.dispose();
            }
        };
        iconWrapper.setOpaque(false);
        iconWrapper.setPreferredSize(new Dimension(48, 48));
        iconWrapper.setLayout(new GridBagLayout());
        
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(loadIcon("file.png", 28, 28));
        iconWrapper.add(iconLabel);
        
        // File info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        Color textColor = isMyFile ? Color.WHITE : new Color(31, 41, 55);
        Color subtextColor = isMyFile ? new Color(255, 255, 255, 200) : new Color(107, 114, 128);
        
        JLabel fileNameLabel = new JLabel(file.getName().length() > 30 ? 
            file.getName().substring(0, 27) + "..." : file.getName());
        fileNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        fileNameLabel.setForeground(textColor);
        
        // File size
        long fileSize = file.length();
String sizeStr = fileSize < 1024 ? fileSize + " B" :
                        fileSize < 1024*1024 ? String.format("%.1f KB", fileSize/1024.0) :
                        String.format("%.1f MB", fileSize/(1024.0*1024.0));
        JLabel sizeLabel = new JLabel(sizeStr + " • Nhấn để xem");
        sizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sizeLabel.setForeground(subtextColor);
        
        infoPanel.add(fileNameLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(sizeLabel);
        
        // Download button with modern style
        JButton downloadButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(isMyFile ? new Color(255, 255, 255, 40) : new Color(59, 130, 246, 20));
                } else if (getModel().isRollover()) {
                    g2.setColor(isMyFile ? new Color(255, 255, 255, 30) : new Color(59, 130, 246, 15));
                } else {
                    g2.setColor(isMyFile ? new Color(255, 255, 255, 20) : new Color(59, 130, 246, 10));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        downloadButton.setText("Tải về");
        downloadButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        downloadButton.setForeground(isMyFile ? Color.WHITE : new Color(59, 130, 246));
        downloadButton.setPreferredSize(new Dimension(60, 32));
        downloadButton.setOpaque(false);
        downloadButton.setContentAreaFilled(false);
        downloadButton.setBorderPainted(false);
        downloadButton.setFocusPainted(false);
        downloadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        downloadButton.setToolTipText("Tải xuống");
        
        downloadButton.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(file.getName()));
            if (fc.showSaveDialog(Client.this) == JFileChooser.APPROVE_OPTION) {
                try {
                    copyFile(file, fc.getSelectedFile());
                    JOptionPane.showMessageDialog(Client.this, "✓ File đã được lưu thành công!", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(Client.this, "Lỗi khi lưu file: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        fileBubble.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
if (e.getButton() == MouseEvent.BUTTON1 && e.getSource() == fileBubble) {
                    previewFile(file);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                fileBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });
        
        fileBubble.add(iconWrapper, BorderLayout.WEST);
        fileBubble.add(infoPanel, BorderLayout.CENTER);
        fileBubble.add(downloadButton, BorderLayout.EAST);
        
        alignmentPanel.add(fileBubble);
        alignmentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        return alignmentPanel;
    }
    
    private JPanel createLocationPanel(String prefix, String mapLink, String time, String weather, boolean isMyLocation) {
        JPanel alignmentPanel = new JPanel(new FlowLayout(isMyLocation ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        alignmentPanel.setOpaque(false);
        alignmentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Modern location card with map-style design
        JPanel locationBubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(3, 3, getWidth()-3, getHeight()-3, 18, 18);
                
                // Background gradient (map-like colors)
                Color bg1 = isMyLocation ? new Color(239, 68, 68) : new Color(59, 130, 246);
                Color bg2 = isMyLocation ? new Color(220, 38, 38) : new Color(37, 99, 235);
                GradientPaint gradient = new GradientPaint(0, 0, bg1, 0, getHeight(), bg2);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth()-3, getHeight()-3, 18, 18);
                
                g2.dispose();
            }
        };
        locationBubble.setLayout(new BorderLayout(10, 8));
        locationBubble.setOpaque(false);
        locationBubble.setBorder(new EmptyBorder(14, 14, 14, 14));
        locationBubble.setPreferredSize(new Dimension(280, 150));
        locationBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Header with icon
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(loadIcon("location.png", 22, 22));
        
        JLabel titleLabel = new JLabel("Vị trí được chia sẻ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        
        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        
        // Info panel with weather and time
JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Time info
        JLabel timeLabel = new JLabel("Thời gian: " + time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Weather info  
        JLabel weatherLabel = new JLabel("Thời tiết: " + weather);
        weatherLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        weatherLabel.setForeground(Color.WHITE);
        weatherLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(timeLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(weatherLabel);
        
        // View map button
        JButton mapButton = new JButton("Xem bản đồ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(new Color(255, 255, 255, 50));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 35));
                } else {
                    g2.setColor(new Color(255, 255, 255, 25));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Border
                g2.setColor(new Color(255, 255, 255, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        mapButton.setIcon(loadIcon("map.png", 16, 16));
        mapButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mapButton.setForeground(Color.WHITE);
        mapButton.setOpaque(false);
        mapButton.setContentAreaFilled(false);
        mapButton.setBorderPainted(false);
        mapButton.setFocusPainted(false);
        mapButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mapButton.setPreferredSize(new Dimension(240, 32));
        mapButton.setMaximumSize(new Dimension(240, 32));
        
        mapButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new java.net.URI(mapLink));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(Client.this, "Khong the mo Google Maps.", 
                    "Loi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        locationBubble.add(headerPanel, BorderLayout.NORTH);
        locationBubble.add(infoPanel, BorderLayout.CENTER);
locationBubble.add(mapButton, BorderLayout.SOUTH);
        
        alignmentPanel.add(locationBubble);
        alignmentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        return alignmentPanel;
    }
    
    private JPanel createVoiceMessagePanel(String prefix, File audioFile, boolean isMyMessage) {
        JPanel alignmentPanel = new JPanel(new FlowLayout(isMyMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        alignmentPanel.setOpaque(false);
        alignmentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // State tracking
        final boolean[] isPlayingState = {false};
        final int[] animationOffset = {0};
        final javax.swing.Timer[] animationTimer = {null};
        final javax.swing.Timer[] countdownTimer = {null};
        
        // Calculate total duration
        long fileSize = audioFile.length();
        final int[] totalDurationSec = {(int)(fileSize / 16000)};
        final int[] remainingSec = {totalDurationSec[0]};
        
        // Modern voice message card with audio waveform design
        JPanel voiceBubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(3, 3, getWidth()-3, getHeight()-3, 20, 20);
                
                // Background gradient
                Color bg1 = isMyMessage ? new Color(139, 92, 246) : new Color(255, 255, 255);
                Color bg2 = isMyMessage ? new Color(124, 58, 237) : new Color(249, 250, 251);
                GradientPaint gradient = new GradientPaint(0, 0, bg1, getWidth(), 0, bg2);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth()-3, getHeight()-3, 20, 20);
                
                // Border for received messages
                if (!isMyMessage) {
                    g2.setColor(new Color(229, 231, 235));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth()-3, getHeight()-3, 20, 20);
                }
                
                g2.dispose();
            }
        };
        voiceBubble.setLayout(new BorderLayout(4, 0));
        voiceBubble.setOpaque(false);
        voiceBubble.setBorder(new EmptyBorder(10, 12, 10, 12));
        
        // Play/Pause button with circular design
        JButton playButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = Math.min(getWidth(), getHeight()) - 4;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // Outer circle glow
                if (getModel().isRollover()) {
                    g2.setColor(isMyMessage ? new Color(255, 255, 255, 20) : new Color(139, 92, 246, 15));
                    g2.fillOval(x - 2, y - 2, size + 4, size + 4);
                }
                
                // Main circle
                Color circleBg = isMyMessage ? new Color(255, 255, 255, 250) : new Color(139, 92, 246);
                if (getModel().isPressed()) {
                    circleBg = isMyMessage ? new Color(255, 255, 255, 200) : new Color(124, 58, 237);
                }
                g2.setColor(circleBg);
                g2.fillOval(x, y, size, size);
                
                // Play/Pause icon - centered in circle
                g2.setColor(isMyMessage ? new Color(139, 92, 246) : Color.WHITE);
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                
                if (isPlayingState[0]) {
                    // Pause icon (two vertical bars)
                    int barWidth = 4;
                    int barHeight = 14;
                    int gap = 4;
                    g2.fillRoundRect(centerX - gap - barWidth, centerY - barHeight/2, barWidth, barHeight, 2, 2);
                    g2.fillRoundRect(centerX + gap, centerY - barHeight/2, barWidth, barHeight, 2, 2);
                } else {
                    // Play icon (triangle) - centered
                    int triSize = 10;
                    int[] xPoints = {centerX - 4, centerX - 4, centerX + 8};
                    int[] yPoints = {centerY - triSize, centerY + triSize, centerY};
                    g2.fillPolygon(xPoints, yPoints, 3);
                }
                
                g2.dispose();
            }
        };
        playButton.setPreferredSize(new Dimension(40, 40));
        playButton.setMinimumSize(new Dimension(40, 40));
        playButton.setOpaque(false);
        playButton.setContentAreaFilled(false);
        playButton.setBorderPainted(false);
        playButton.setFocusPainted(false);
        playButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Animated Waveform visualization panel
        JPanel waveformPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color waveColor = isMyMessage ? new Color(255, 255, 255, 200) : new Color(139, 92, 246, 180);
                g2.setColor(waveColor);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                // Base heights for waveform bars
                int[] baseHeights = {8, 15, 22, 18, 12, 25, 20, 14, 10, 16, 23, 19, 11, 17, 24, 15, 20, 13};
                int spacing = 6;
                int centerY = getHeight() / 2;
                
                for (int i = 0; i < baseHeights.length && (i * spacing) < getWidth() - 5; i++) {
                    int x = 3 + i * spacing;
                    int barHeight = baseHeights[i];
                    
                    // Add animation effect when playing
                    if (isPlayingState[0]) {
                        double wave = Math.sin((i + animationOffset[0]) * 0.5) * 0.4 + 0.8;
                        barHeight = (int)(barHeight * wave);
                        barHeight = Math.max(4, Math.min(28, barHeight));
                    }
                    
                    g2.drawLine(x, centerY - barHeight/2, x, centerY + barHeight/2);
                }
                
                g2.dispose();
            }
        };
        waveformPanel.setOpaque(false);
        waveformPanel.setPreferredSize(new Dimension(110, 36));
        
        // Duration/Countdown label
        Color subtextColor = isMyMessage ? Color.WHITE : new Color(107, 114, 128);
        String durationStr = String.format("%d:%02d", totalDurationSec[0] / 60, totalDurationSec[0] % 60);
        
        JLabel durationLabel = new JLabel(durationStr);
        durationLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        durationLabel.setForeground(subtextColor);
        
        // Animation timer for waveform
        animationTimer[0] = new javax.swing.Timer(80, e -> {
            if (isPlayingState[0]) {
                animationOffset[0]++;
                waveformPanel.repaint();
            }
        });
        
        // Countdown timer
        countdownTimer[0] = new javax.swing.Timer(1000, e -> {
            if (isPlayingState[0] && remainingSec[0] > 0) {
                remainingSec[0]--;
                durationLabel.setText(String.format("%d:%02d", remainingSec[0] / 60, remainingSec[0] % 60));
                if (remainingSec[0] <= 0) {
                    countdownTimer[0].stop();
                }
            }
        });
        
        // Playback listener for this voice message
        AudioController.PlaybackListener playbackListener = new AudioController.PlaybackListener() {
            @Override
            public void onPlaybackStarted(File file) {
                if (file.equals(audioFile)) {
                    SwingUtilities.invokeLater(() -> {
                        isPlayingState[0] = true;
                        remainingSec[0] = totalDurationSec[0];
                        durationLabel.setText(String.format("%d:%02d", remainingSec[0] / 60, remainingSec[0] % 60));
                        animationTimer[0].start();
                        countdownTimer[0].start();
                        playButton.repaint();
                    });
                }
            }
            
            @Override
            public void onPlaybackPaused(File file) {
                if (file.equals(audioFile)) {
                    SwingUtilities.invokeLater(() -> {
                        isPlayingState[0] = false;
                        animationTimer[0].stop();
                        countdownTimer[0].stop();
playButton.repaint();
                        waveformPanel.repaint();
                    });
                }
            }
            
            @Override
            public void onPlaybackResumed(File file) {
                if (file.equals(audioFile)) {
                    SwingUtilities.invokeLater(() -> {
                        isPlayingState[0] = true;
                        animationTimer[0].start();
                        countdownTimer[0].start();
                        playButton.repaint();
                    });
                }
            }
            
            @Override
            public void onPlaybackStopped(File file) {
                if (file.equals(audioFile)) {
                    SwingUtilities.invokeLater(() -> {
                        isPlayingState[0] = false;
                        remainingSec[0] = totalDurationSec[0];
                        durationLabel.setText(String.format("%d:%02d", totalDurationSec[0] / 60, totalDurationSec[0] % 60));
                        animationTimer[0].stop();
                        countdownTimer[0].stop();
                        animationOffset[0] = 0;
                        playButton.repaint();
                        waveformPanel.repaint();
                    });
                }
            }
        };
        
        audioController.addPlaybackListener(playbackListener);
        
        // Play/Pause button action
        playButton.addActionListener(ev -> {
            audioController.togglePlayPause(audioFile);
        });
        
        // Assemble the voice bubble - use FlowLayout for center alignment
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(waveformPanel);
        centerPanel.add(durationLabel);
        
        voiceBubble.add(playButton, BorderLayout.WEST);
        voiceBubble.add(centerPanel, BorderLayout.CENTER);
        
        alignmentPanel.add(voiceBubble);
        alignmentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        return alignmentPanel;
    }
    
    // === DIALOGS ===
    
    private void showCreateGroupDialog() {
        DefaultListModel<UserDisplay> groupCreationModel = new DefaultListModel<>();
        for (int i = 0; i < userListModel.getSize(); i++) {
            UserDisplay user = userListModel.getElementAt(i);
            if (!user.isGroup() && !user.getUsername().equals("Server (Admin)") && !user.getUsername().equals(chatState.getMyUsername())) {
                groupCreationModel.addElement(user);
            }
        }
        if (groupCreationModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có người dùng nào khác đang hoạt động để tạo nhóm.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CreateGroupDialog dialog = new CreateGroupDialog(this, groupCreationModel);
        dialog.setVisible(true);
if (dialog.isSucceeded()) {
            String groupName = dialog.getGroupName();
            java.util.List<UserDisplay> selectedUsers = dialog.getSelectedUsers();
            
            try {
                java.util.List<String> members = new ArrayList<>();
                members.add(chatState.getMyUsername());
                for (UserDisplay user : selectedUsers) {
                    members.add(user.getUsername());
                }
                networkController.sendCreateGroupRequest(groupName, "Nhóm: " + groupName, members);
                addSystemMessage_Safe("WELCOME", "Đã gửi yêu cầu tạo nhóm: " + groupName);
            } catch (IOException e) {
                e.printStackTrace();
                addSystemMessage_Safe("WELCOME", "Lỗi khi gửi yêu cầu tạo nhóm: " + e.getMessage());
            }
        }
    }
    
    private void showAddMembersDialog(String groupName, String groupFullName) {
        DefaultListModel<UserDisplay> groupCreationModel = new DefaultListModel<>();
        for (int i = 0; i < userListModel.getSize(); i++) {
            UserDisplay user = userListModel.getElementAt(i);
            if (!user.isGroup() && !user.getUsername().equals("Server (Admin)") && !user.getUsername().equals(chatState.getMyUsername())) {
                groupCreationModel.addElement(user);
            }
        }
        if (groupCreationModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có người dùng nào khác đang hoạt động để thêm vào nhóm.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        AddMemberDialog dialog = new AddMemberDialog(this, groupFullName, groupCreationModel);
        dialog.setVisible(true);
        
        if (dialog.isSucceeded()) {
            java.util.List<UserDisplay> selectedUsers = dialog.getSelectedUsers();
            
            try {
                java.util.List<String> members = new ArrayList<>();
                for (UserDisplay user : selectedUsers) {
                    members.add(user.getUsername());
                }
                networkController.sendAddMembersToGroup(groupName, members);
                addSystemMessage_Safe(groupName, "Đã gửi yêu cầu thêm " + selectedUsers.size() + " thành viên...");
            } catch (IOException e) {
                e.printStackTrace();
                addSystemMessage_Safe("WELCOME", "Lỗi khi gửi yêu cầu thêm thành viên: " + e.getMessage());
            }
        }
    }
    
    // === UTILITY METHODS ===
    
    private void previewFile(File file) {
        try {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
                JDialog imageDialog = new JDialog(this, "Xem trước ảnh: " + file.getName(), true);
ImageIcon imageIcon = new ImageIcon(new ImageIcon(file.toURI().toURL()).getImage().getScaledInstance(800, 600, Image.SCALE_SMOOTH));
                JLabel imageLabel = new JLabel(imageIcon);
                imageDialog.add(new JScrollPane(imageLabel));
                imageDialog.pack();
                imageDialog.setLocationRelativeTo(this);
                imageDialog.setVisible(true);
            } else if (fileName.endsWith(".txt") || fileName.endsWith(".java") || fileName.endsWith(".log") || fileName.endsWith(".xml")) {
                JDialog textDialog = new JDialog(this, "Xem trước văn bản: " + file.getName(), true);
                JTextArea textArea = new JTextArea(25, 80);
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(file, java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null)
                        content.append(line).append("\n");
                }
                textArea.setText(content.toString());
                textArea.setCaretPosition(0);
                textDialog.add(new JScrollPane(textArea));
                textDialog.pack();
                textDialog.setLocationRelativeTo(this);
                textDialog.setVisible(true);
            } else {
                if (Desktop.isDesktopSupported())
                    Desktop.getDesktop().open(file);
                else
                    JOptionPane.showMessageDialog(this, "Không thể mở loại file này.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể mở file: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void copyFile(File source, File dest) throws IOException {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) > 0)
                os.write(buffer, 0, length);
        }
    }
}