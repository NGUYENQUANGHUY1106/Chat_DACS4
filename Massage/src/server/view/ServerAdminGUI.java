package server.view;

import static server.core.ChatServerCore.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import server.controller.ServerAdminController;
import server.core.ClientHandler;
import server.model.GroupInfo;
import server.model.UserDisplay;

public class ServerAdminGUI extends JFrame {

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

    // Các panel view đã có sẵn
    private DashboardPanel dashboardPanel;
    private UserManagementPanel userManagementPanel;
    private FileManagementPanel fileManagementPanel;

    public ServerAdminGUI() {
        setTitle("ChatSphere-Admin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1200, 700);
        setLocationRelativeTo(null);

        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(Color.WHITE);
        setContentPane(contentPane);

        // ==================== HEADER ====================
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

        // ==================== SIDEBAR ====================
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(sidebarColor);
        sidebarPanel.setPreferredSize(new Dimension(230, 0));

        sidebarButtons = new JButton[5];
        btnDashboard = new JButton("Dashboard");
        btnDashboard.setIcon(loadAndScaleIcon("dashboard.png", 30, 30));

        sidebarButtons[0] = btnDashboard;
        styleSidebarButton(btnDashboard);

        btnUsers = new JButton("Người dùng");
        btnUsers.setIcon(loadAndScaleIcon("user_nav.png", 30, 30));
        sidebarButtons[1] = btnUsers;
        styleSidebarButton(btnUsers);

        btnMessages = new JButton("Tin nhắn");
        btnMessages.setIcon(loadAndScaleIcon("comments.png", 30, 30));
        sidebarButtons[2] = btnMessages;
        styleSidebarButton(btnMessages);

        btnRooms = new JButton("Phòng chat");
        btnRooms.setIcon(loadAndScaleIcon("room_chat.png", 30, 30));
        sidebarButtons[3] = btnRooms;
        styleSidebarButton(btnRooms);

        btnFiles = new JButton("File / Ảnh");
        btnFiles.setIcon(loadAndScaleIcon("image-files.png", 30, 30));
        sidebarButtons[4] = btnFiles;
        styleSidebarButton(btnFiles);

        sidebarPanel.add(btnDashboard);
        sidebarPanel.add(btnUsers);
        sidebarPanel.add(btnMessages);
        sidebarPanel.add(btnRooms);
        sidebarPanel.add(btnFiles);

        sidebarPanel.add(Box.createVerticalGlue());

        setActiveSidebarButton(btnDashboard);
        contentPane.add(sidebarPanel, BorderLayout.WEST);

        // ==================== FOOTER ====================
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        footerPanel.setPreferredSize(new Dimension(0, 30));
        JLabel lblFooter = new JLabel("© 2025 ChatSphere. All rights reserved.");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooter.setForeground(Color.GRAY);
        footerPanel.add(lblFooter);
        contentPane.add(footerPanel, BorderLayout.SOUTH);

        // ==================== MAIN CONTENT (CARD) ====================
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Color.WHITE);

        dashboardPanel = new DashboardPanel();
        userManagementPanel = new UserManagementPanel();
        fileManagementPanel = new FileManagementPanel();

        // ==================== PANEL TIN NHẮN (MESSAGES) ====================
        JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
        messagePanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        messagePanel.setBackground(Color.WHITE);

        // Danh sách client bên trái
        chatClientListModel = new DefaultListModel<>();
        chatClientList = new JList<>(chatClientListModel);
        chatClientList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatClientList.setFixedCellHeight(30);
        JScrollPane clientListScrollPane = new JScrollPane(chatClientList);
        clientListScrollPane.setPreferredSize(new Dimension(250, 0));
        clientListScrollPane.setBorder(BorderFactory.createTitledBorder("Clients đang Online"));

        // Cửa sổ chat bên phải
        serverChatCardLayout = new CardLayout();
        serverChatWindowsPanel = new JPanel(serverChatCardLayout);

        JPanel welcomeChatPanel = new JPanel(new GridBagLayout());
        welcomeChatPanel.setBackground(Color.WHITE);
        welcomeChatPanel.add(new JLabel("Chọn một client để xem tin nhắn."));
        serverChatWindowsPanel.add(welcomeChatPanel, "WELCOME_CHAT");
        serverChatCardLayout.show(serverChatWindowsPanel, "WELCOME_CHAT");

        JSplitPane chatSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                clientListScrollPane, serverChatWindowsPanel);
        chatSplitPane.setDividerLocation(250);
        chatSplitPane.setBorder(null);
        messagePanel.add(chatSplitPane, BorderLayout.CENTER);

        // Khung nhập tin nhắn Server
        JPanel serverInputPanel = new JPanel(new BorderLayout(10, 10));
        serverInputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        serverInputPanel.setOpaque(false);

        JTextField serverMessageField = new JTextField("Gửi tin nhắn với tư cách Server...");
        serverMessageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        serverSendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        serverSendButton.setBackground(new Color(52, 152, 219));
        serverSendButton.setForeground(Color.WHITE);

        serverInputPanel.add(serverMessageField, BorderLayout.CENTER);
        serverInputPanel.add(serverSendButton, BorderLayout.EAST);
        messagePanel.add(serverInputPanel, BorderLayout.SOUTH);

        // Listener chọn client
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
            if (message.isEmpty() || message.equals("Gửi tin nhắn với tư cách Server...")) return;

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

        // ==================== PANEL PHÒNG CHAT (ROOMS) ====================
        roomPanel = new JPanel(new BorderLayout(10, 10));
        roomPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        roomPanel.setBackground(Color.WHITE);

        JLabel lblRoomTitle = new JLabel("Quản lý Phòng chat / Nhóm");
        lblRoomTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        roomPanel.add(lblRoomTitle, BorderLayout.NORTH);

        groupListModel = new DefaultListModel<>();
        groupList = new JList<>(groupListModel);
        groupList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane groupListScrollPane = new JScrollPane(groupList);
        groupListScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách Nhóm"));

        memberListModel = new DefaultListModel<>();
        memberList = new JList<>(memberListModel);
        memberList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane memberListScrollPane = new JScrollPane(memberList);
        memberListScrollPane.setBorder(BorderFactory.createTitledBorder("Thành viên trong Nhóm"));

        JPanel roomActionPanel = new JPanel(new GridBagLayout());
        roomActionPanel.setBackground(Color.WHITE);
        roomActionPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ===== NÚT XÓA THÀNH VIÊN =====
        JButton btnRemoveMember = new JButton("Xóa Thành Viên");
        btnRemoveMember.setForeground(Color.RED.darker());
        btnRemoveMember.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy++;
        roomActionPanel.add(btnRemoveMember, gbc);

        // ===== NÚT XÓA NHÓM =====
        JButton btnDeleteGroup = new JButton("Xóa Nhóm");
        btnDeleteGroup.setForeground(Color.RED.darker());
        btnDeleteGroup.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy++;
        roomActionPanel.add(btnDeleteGroup, gbc);
        // ==========================

        gbc.gridy++;
        gbc.weighty = 1.0;
        roomActionPanel.add(Box.createVerticalGlue(), gbc);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                groupListScrollPane, memberListScrollPane);
        splitPane.setDividerLocation(280);
        splitPane.setOpaque(false);

        centerPanel.add(splitPane, BorderLayout.CENTER);
        centerPanel.add(roomActionPanel, BorderLayout.EAST);
        roomPanel.add(centerPanel, BorderLayout.CENTER);

        // Sự kiện chọn nhóm
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

        // XÓA THÀNH VIÊN KHỎI NHÓM
        btnRemoveMember.addActionListener(e -> {
            String selectedGroupFullName = groupList.getSelectedValue();
            UserDisplay selectedMember = memberList.getSelectedValue();

            if (selectedGroupFullName == null || selectedMember == null) {
                JOptionPane.showMessageDialog(roomPanel,
                        "Vui lòng chọn một nhóm VÀ một thành viên để xóa.",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            GroupInfo group = findGroupByName(selectedGroupFullName);
            String memberIdToRemove = selectedMember.username;

            if (group == null) {
                JOptionPane.showMessageDialog(roomPanel,
                        "Lỗi: Không tìm thấy thông tin nhóm.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(roomPanel,
                    "Bạn có chắc muốn xóa " + selectedMember.fullName +
                            " khỏi nhóm " + group.groupFullName + "?",
                    "Xác nhận Xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) return;

            if (group.members.remove(memberIdToRemove)) {
                addSystemLog("ADMIN: Đã xóa " + memberIdToRemove + " khỏi nhóm " + group.groupName);

                memberListModel.removeElement(selectedMember);

                ClientHandler removedHandler = clients.get(memberIdToRemove);
                if (removedHandler != null) {
                    removedHandler.sendSystemMessage(
                            "Hệ thống: Bạn đã bị admin xóa khỏi nhóm '" + group.groupFullName + "'.");
                }

                String notification = "Hệ thống: " + selectedMember.fullName +
                        " đã bị admin xóa khỏi nhóm.";
                for (String memberId : group.members) {
                    ClientHandler handler = clients.get(memberId);
                    if (handler != null) {
                        handler.sendSystemMessage(notification);
                    }
                }

                broadcastUserListUpdate();

            } else {
                addSystemLog("ADMIN: Lỗi, không tìm thấy " + memberIdToRemove +
                        " trong nhóm " + group.groupName);
            }
        });

        // XÓA CẢ NHÓM
        btnDeleteGroup.addActionListener(e -> {
            String selectedGroupFullName = groupList.getSelectedValue();

            if (selectedGroupFullName == null) {
                JOptionPane.showMessageDialog(roomPanel,
                        "Vui lòng chọn một nhóm để xóa.",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            GroupInfo group = findGroupByName(selectedGroupFullName);
            if (group == null) {
                JOptionPane.showMessageDialog(roomPanel,
                        "Lỗi: Không tìm thấy thông tin nhóm.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int choice = JOptionPane.showConfirmDialog(
                    roomPanel,
                    "Bạn có chắc chắn muốn xóa nhóm '" + group.groupFullName + "' không?\n"
                            + "Tất cả thành viên sẽ không còn thấy nhóm này nữa.",
                    "Xác nhận xóa nhóm",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            // Thông báo cho các thành viên trong nhóm
            String notification = "Hệ thống: Nhóm '" + group.groupFullName + "' đã bị admin xóa.";
            for (String memberId : group.members) {
                ClientHandler handler = clients.get(memberId);
                if (handler != null) {
                    handler.sendSystemMessage(notification);
                }
            }

            // Ghi log
            addSystemLog("ADMIN: Đã xóa nhóm " + group.groupName);

            // Xóa khỏi map groups (trong ChatServerCore)
            groups.remove(group.groupName);

            // Xóa khỏi list hiển thị trên GUI
            groupListModel.removeElement(selectedGroupFullName);
            memberListModel.clear();

            // Cập nhật lại danh sách cho client
            broadcastUserListUpdate();
        });

        // ==================== CÁC PANEL KHÁC ====================
        JPanel reportPanel = createPlaceholderPanel("▪ Báo cáo", "Xem báo cáo hệ thống");
        JPanel settingPanel = createPlaceholderPanel("⚙️ Cấu hình", "Cấu hình hệ thống");

        mainContentPanel.add(dashboardPanel, "DASHBOARD");
        mainContentPanel.add(userManagementPanel, "USERS");
        mainContentPanel.add(messagePanel, "MESSAGES");
        mainContentPanel.add(roomPanel, "ROOMS");
        mainContentPanel.add(fileManagementPanel, "FILES");
        mainContentPanel.add(reportPanel, "REPORTS");
        mainContentPanel.add(settingPanel, "SETTINGS");

        contentPane.add(mainContentPanel, BorderLayout.CENTER);

        // Sidebar actions
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

        // ======= Gán tham chiếu GUI sang ChatServerCore & Start server =======
        initGUIReferences(
                dashboardPanel,
                userManagementPanel,
                fileManagementPanel,
                groupListModel,
                groupList,
                memberListModel,
                memberList,
                chatClientListModel,
                chatClientList,
                serverChatWindowsPanel,
                serverChatCardLayout
        );

        ServerAdminController.startServer();
    }

    // ==================== GUI Helper ====================

    private ImageIcon loadAndScaleIcon(String fileName, int width, int height) {
        URL resourceUrl = getClass().getResource("/icons/" + fileName);
        if (resourceUrl == null) {
            System.err.println("Không thể tìm thấy icon: /icons/" + fileName);
            return new ImageIcon(
                    new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB));
        }
        ImageIcon icon = new ImageIcon(resourceUrl);
        java.awt.Image img = icon.getImage();
        java.awt.Image scaledImg = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
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
}
