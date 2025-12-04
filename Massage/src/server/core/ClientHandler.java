package server.core;

import static server.core.ChatServerCore.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import database.DBConnection;
import server.model.GroupInfo;
import server.model.UserDisplay;

public class ClientHandler implements Runnable {

    final Socket socket;
    public String clientId;      // SĐT
    public String fullName;      // Họ tên
    DataInputStream dis;
    public DataOutputStream dos;

    private final JPanel chatPanel;
    private final JScrollPane scrollPane;

    // UI constants (riêng cho server chat window)
    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font UI_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private static final Color MY_MESSAGE_COLOR = new Color(0, 132, 255);
    private static final Color OTHER_MESSAGE_COLOR = new Color(225, 225, 225);
    private static final Color SYSTEM_MESSAGE_COLOR = new Color(225, 225, 225);

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

    // =========================== RUN =======================

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

                    // Voice control
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

                    // Video control
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
                // Nếu đang trong cuộc gọi thì tự gác máy
                if (activeCalls.containsKey(this.clientId)) {
                    String partnerId = activeCalls.get(this.clientId);
                    endCall(this.clientId, partnerId);
                }

                // Dọn dẹp UDP map
                SocketAddress myUdpAddress = udpAddressBook.remove(this.clientId);
                if (myUdpAddress != null) {
                    udpClientMap.remove(myUdpAddress);
                    addSystemLog("UDP: Đã hủy đăng ký " + this.clientId + " khỏi " + myUdpAddress);
                }

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

    // ====================== CALL GROUP / INVITE =====================

    /** Mời người khác vào cuộc gọi hiện tại */
    private void handleInviteToCall() throws IOException {
        String invitedId = dis.readUTF();
        String callType = dis.readUTF();

        String currentContextId = activeCalls.get(this.clientId);
        if (currentContextId == null) {
            sendSystemMessage("Lỗi: Bạn không đang trong cuộc gọi nào để mời.");
            return;
        }

        GroupInfo callGroup;
        String groupName;

        if (groups.containsKey(currentContextId)) {
            // Đang trong Group Call
            groupName = currentContextId;
            callGroup = groups.get(groupName);
            addSystemLog("SERVER: " + this.clientId + " mời " + invitedId + " vào Nhóm GỌI " + groupName);
        } else if (clients.containsKey(currentContextId)) {
            // Đang 1-1, nâng cấp lên nhóm tạm thời
            String partnerId = currentContextId;

            groupName = "CALL_" + Math.min(this.clientId.hashCode(), partnerId.hashCode()) +
                    "_" + Math.max(this.clientId.hashCode(), partnerId.hashCode());

            if (groups.containsKey(groupName)) {
                callGroup = groups.get(groupName);
            } else {
                callGroup = new GroupInfo();
                callGroup.groupName = groupName;
                callGroup.groupFullName = (callType.equals("VIDEO") ? "Phòng Video" : "Phòng Voice") + " Tạm thời";
                callGroup.members.add(this.clientId);
                callGroup.members.add(partnerId);
                groups.put(groupName, callGroup);
                addSystemLog("SERVER: Nâng cấp cuộc gọi 1-1 (" + this.clientId + ", " + partnerId + ") lên nhóm tạm " + groupName);

                activeCalls.put(this.clientId, groupName);
                activeCalls.put(partnerId, groupName);

                ClientHandler partnerHandler = clients.get(partnerId);
                if (partnerHandler != null) {
                    partnerHandler.sendCallStatusUpdate(groupName, callGroup.groupFullName, "CREATED");
                }
                this.sendCallStatusUpdate(groupName, callGroup.groupFullName, "CREATED");
                broadcastUserListUpdate();
            }
        } else {
            sendSystemMessage("Lỗi: Không tìm thấy ngữ cảnh cuộc gọi.");
            return;
        }

        ClientHandler invitedHandler = clients.get(invitedId);
        if (invitedHandler == null) {
            sendSystemMessage("Hệ thống: Người được mời (" + invitedId + ") không online.");
            return;
        }

        if (!callGroup.members.contains(invitedId)) {
            callGroup.members.add(invitedId);
            addSystemLog("SERVER: Thêm thành viên " + invitedId + " vào nhóm " + groupName);

            if (callType.equals("VIDEO")) {
                invitedHandler.sendVideoCallIncoming(groupName, this.fullName + " (Mời vào Video Nhóm)");
            } else {
                invitedHandler.sendVoiceCallIncoming(groupName, this.fullName + " (Mời vào Voice Nhóm)");
            }

            String notification = "Hệ thống: " + invitedHandler.fullName + " đang được mời tham gia.";
            for (String memberId : callGroup.members) {
                if (memberId.equals(this.clientId) || memberId.equals(invitedId)) continue;
                ClientHandler memberHandler = clients.get(memberId);
                if (memberHandler != null) {
                    memberHandler.sendSystemMessage(notification);
                }
            }

            broadcastUserListUpdate();
        } else {
            sendSystemMessage("Hệ thống: Người này đã ở trong cuộc gọi rồi.");
        }
    }

    /** Tham gia phòng gọi bằng mã phòng */
    private void handleJoinCallRequest() throws IOException {
        String groupId = dis.readUTF();

        GroupInfo group = groups.get(groupId);

        if (group == null || (!activeCalls.containsKey(group.groupName) && group.members.size() < 2)) {
            sendSystemMessage("Lỗi: Mã phòng không tồn tại hoặc cuộc gọi đã kết thúc.");
            return;
        }

        if (activeCalls.containsKey(this.clientId)) {
            sendSystemMessage("Lỗi: Bạn đang trong cuộc gọi khác. Vui lòng gác máy trước.");
            return;
        }

        if (group.members.add(this.clientId)) {
            addSystemLog("SERVER: User " + this.clientId + " đã tự tham gia vào nhóm " + groupId + " bằng mã phòng.");
        }

        activeCalls.put(this.clientId, groupId);
        this.sendCallJoinedSuccess(groupId, group.groupFullName);

        String notification = "Hệ thống: " + this.fullName + " đã tham gia cuộc gọi.";
        for (String memberId : group.members) {
            if (memberId.equals(this.clientId)) continue;
            ClientHandler memberHandler = clients.get(memberId);
            if (memberHandler != null) {
                memberHandler.sendSystemMessage(notification);
            }
        }

        broadcastUserListUpdate();
    }

    // ===================== CALL STATUS MESSAGES ====================

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

    // ========================= ĐĂNG KÝ USER ========================

    private void handleRegisterUsername() throws IOException {
        String username = dis.readUTF(); // SĐT

        if (username == null || username.trim().isEmpty() || clients.containsKey(username)) {
            sendSystemMessage("Lỗi: Tên đăng nhập không hợp lệ hoặc đã tồn tại.");
            socket.close();
            return;
        }

        String foundFullName = username;
        String sql = "SELECT full_name FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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

        updateDashboardCounts();
        broadcastSystemMessage("Hệ thống: " + this.fullName + " đã tham gia.", this.clientId);
        sendSystemMessage("Chào mừng, " + this.fullName + "!");
        broadcastUserListUpdate();
    }

    // ====================== PRIVATE / GROUP MSG ====================

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

        if (!members.contains(this.clientId)) {
            return;
        }

        String formattedMessage = "[" + group.groupFullName + "] " + this.fullName + ": " + message;
        String formattedMessageForServer = this.fullName + " -> [" + group.groupFullName + "]: " + message;

        for (String memberName : members) {
            if (memberName.equals(this.clientId)) continue;
            ClientHandler memberHandler = clients.get(memberName);
            if (memberHandler != null) {
                memberHandler.sendGroupMessage(group.groupName, formattedMessage);
            }
        }

        this.addClientMessageToServerGUI(formattedMessageForServer);

        if (dashboardPanel != null)
            dashboardPanel.incrementMessageCount();
    }

    private void handleCreateGroup() throws IOException {
        String userProvidedName = dis.readUTF();
        String groupFullName = dis.readUTF();
        int memberCount = dis.readInt();

        // Tạo groupId duy nhất bằng UUID để tránh ghi đè nhóm cũ
        String groupId = "GRP_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);

        GroupInfo newGroup = new GroupInfo();
        newGroup.groupName = groupId;  // Sử dụng ID duy nhất làm key
        newGroup.groupFullName = groupFullName;

        for (int i = 0; i < memberCount; i++) {
            newGroup.members.add(dis.readUTF());
        }

        // Không cần kiểm tra trùng tên vì ID luôn duy nhất
        groups.put(groupId, newGroup);
        
        addSystemLog("Nhóm mới được tạo: " + groupFullName + " (ID: " + groupId + ")");

        SwingUtilities.invokeLater(() -> {
            if (groupListModel != null) {
                groupListModel.addElement(groupFullName);
            }
            // nếu admin đang chọn đúng nhóm này thì cập nhật luôn panel thành viên
            refreshMemberPanelForCurrentSelection();
        });

        broadcastUserListUpdate();

    }

    private void handleAddMembersToGroup() throws IOException {
        String groupName = dis.readUTF();
        int newMemberCount = dis.readInt();

        GroupInfo group = groups.get(groupName);
        if (group == null) {
            sendSystemMessage("Lỗi: Không tìm thấy nhóm '" + groupName + "'.");
            for (int i = 0; i < newMemberCount; i++) dis.readUTF();
            return;
        }

        Set<String> newMembers = new HashSet<>();
        for (int i = 0; i < newMemberCount; i++) {
            String newMemberId = dis.readUTF();
            if (group.members.add(newMemberId)) {
                newMembers.add(newMemberId);
            }
        }

        if (newMembers.isEmpty()) {
            sendSystemMessage("Hệ thống: Không có thành viên mới nào được thêm (có thể họ đã ở trong nhóm).");
            return;
        }

        String joinMsg = "Hệ thống: Bạn vừa được " + this.fullName + " thêm vào nhóm '" + group.groupFullName + "'.";
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

    // ========================= VOICE CALL ===========================

    private void handleVoiceCallRequest() throws IOException {
        String targetId = dis.readUTF();

        // Kiểm tra xem người gọi có đang trong cuộc gọi khác không
        if (activeCalls.containsKey(this.clientId)) {
            addSystemLog("SERVER: Từ chối gọi vì " + this.clientId + " đang trong cuộc gọi khác");
            this.sendVoiceCallDeclined(targetId);
            return;
        }

        if (groups.containsKey(targetId)) {
            GroupInfo group = groups.get(targetId);
            addSystemLog("SERVER: Yêu cầu gọi NHÓM (Voice): " + this.clientId + " -> Nhóm " + group.groupFullName);

            activeCalls.put(this.clientId, targetId);

            for (String memberId : group.members) {
                if (memberId.equals(this.clientId)) continue;
                ClientHandler memberHandler = clients.get(memberId);
                if (memberHandler != null) {
                    memberHandler.sendVoiceCallIncoming(targetId,
                            this.fullName + " (Nhóm: " + group.groupFullName + ")");
                }
            }
        } else {
            ClientHandler targetHandler = clients.get(targetId);
            addSystemLog("SERVER: Yêu cầu gọi 1-1: " + this.clientId + " -> " + targetId);

            if (targetHandler != null && !activeCalls.containsKey(targetId)) {
                targetHandler.sendVoiceCallIncoming(this.clientId, this.fullName);
            } else {
                this.sendVoiceCallDeclined(targetId);
                if (targetHandler == null)
                    addSystemLog("Server: Từ chối do " + targetId + " offline.");
                else
                    addSystemLog("Server: Từ chối do " + targetId + " đang bận.");
            }
        }
    }

    private void handleVoiceCallAccept() throws IOException {
        String contextId = dis.readUTF();

        if (groups.containsKey(contextId)) {
            addSystemLog("SERVER: User " + this.clientId + " đã chấp nhận tham gia Voice Nhóm " + contextId);
            activeCalls.put(this.clientId, contextId);
            this.sendVoiceCallAccepted(contextId);
        } else {
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
        addSystemLog("Từ chối gọi (Voice): " + this.clientId + " -> " + callerUsername);
        
        // Nếu đây là cuộc gọi nhóm, chỉ cần xóa activeCalls của người gọi
        // Kiểm tra xem callerUsername là groupId hay userId
        if (groups.containsKey(callerUsername)) {
            // Đây là từ chối cuộc gọi nhóm - xóa activeCalls của người này
            activeCalls.remove(this.clientId);
            addSystemLog("Đã xóa activeCalls cho " + this.clientId + " (từ chối tham gia nhóm " + callerUsername + ")");
        } else {
            // Cuộc gọi 1-1
            ClientHandler callerHandler = clients.get(callerUsername);
            if (callerHandler != null) {
                callerHandler.sendVoiceCallDeclined(this.clientId);
            }
            // Xóa activeCalls của cả hai bên
            activeCalls.remove(this.clientId);
            activeCalls.remove(callerUsername);
        }
    }

    private void handleVoiceCallHangup() throws IOException {
        String partnerUsername = dis.readUTF();
        addSystemLog("Gác máy (Chung): " + this.clientId + " -> " + partnerUsername);
        endCall(this.clientId, partnerUsername);
    }

    // ========================= VIDEO CALL ===========================

    private void handleVideoCallRequest() throws IOException {
        String targetId = dis.readUTF();

        // Kiểm tra xem người gọi có đang trong cuộc gọi khác không
        if (activeCalls.containsKey(this.clientId)) {
            addSystemLog("SERVER: Từ chối video call vì " + this.clientId + " đang trong cuộc gọi khác");
            this.sendVideoCallDeclined(targetId);
            return;
        }

        if (groups.containsKey(targetId)) {
            GroupInfo group = groups.get(targetId);
            addSystemLog("SERVER: Yêu cầu VIDEO CALL NHÓM: " + this.clientId + " -> Nhóm " + group.groupFullName);

            activeCalls.put(this.clientId, targetId);

            for (String memberId : group.members) {
                if (memberId.equals(this.clientId)) continue;
                ClientHandler memberHandler = clients.get(memberId);
                if (memberHandler != null) {
                    memberHandler.sendVideoCallIncoming(targetId,
                            this.fullName + " (Video Nhóm: " + group.groupFullName + ")");
                }
            }
        } else {
            ClientHandler targetHandler = clients.get(targetId);
            addSystemLog("SERVER: Yêu cầu VIDEO CALL 1-1: " + this.clientId + " -> " + targetId);

            if (targetHandler != null && !activeCalls.containsKey(targetId)) {
                targetHandler.sendVideoCallIncoming(this.clientId, this.fullName);
            } else {
                this.sendVideoCallDeclined(targetId);
                if (targetHandler == null)
                    addSystemLog("Server: Từ chối video do " + targetId + " offline.");
                else
                    addSystemLog("Server: Từ chối video do " + targetId + " đang bận.");
            }
        }
    }

    private void handleVideoCallAccept() throws IOException {
        String contextId = dis.readUTF();

        if (groups.containsKey(contextId)) {
            addSystemLog("SERVER: User " + this.clientId + " đã chấp nhận tham gia Video Nhóm " + contextId);
            activeCalls.put(this.clientId, contextId);
            this.sendVideoCallAccepted(contextId);
        } else {
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
        addSystemLog("Từ chối VIDEO CALL: " + this.clientId + " -> " + callerUsername);
        
        // Nếu đây là cuộc gọi nhóm, chỉ cần xóa activeCalls của người này
        if (groups.containsKey(callerUsername)) {
            // Đây là từ chối cuộc gọi video nhóm
            activeCalls.remove(this.clientId);
            addSystemLog("Đã xóa activeCalls cho " + this.clientId + " (từ chối video nhóm " + callerUsername + ")");
        } else {
            // Cuộc gọi video 1-1
            ClientHandler callerHandler = clients.get(callerUsername);
            if (callerHandler != null) {
                callerHandler.sendVideoCallDeclined(this.clientId);
            }
            // Xóa activeCalls của cả hai bên
            activeCalls.remove(this.clientId);
            activeCalls.remove(callerUsername);
        }
    }

    // ======================= FILE / LOCATION / VOICE ===============

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
                    if (bytesRead == -1) break;
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

                    forwardFileToHandler(targetHandler, this.clientId, this.clientId, this.fullName, savedFile,
                            fileName, subDataType);
                }
            } else if (targetType.equals("GROUP")) {
                GroupInfo group = groups.get(targetName);
                if (group != null) {
                    addSystemLog(this.fullName + " -> [" + group.groupFullName + "] (gửi " + fileName + ")");
                    for (String memberName : group.members) {
                        if (memberName.equals(this.clientId)) continue;
                        ClientHandler memberHandler = clients.get(memberName);
                        if (memberHandler != null) {
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
                    forwardLocationToHandler(targetHandler, this.clientId, this.clientId, this.fullName, lat, lon,
                            time, weather);
                }
            } else if (targetType.equals("GROUP")) {
                GroupInfo group = groups.get(targetName);
                if (group != null) {
                    for (String memberName : group.members) {
                        if (memberName.equals(this.clientId)) continue;
                        ClientHandler memberHandler = clients.get(memberName);
                        if (memberHandler != null) {
                            forwardLocationToHandler(memberHandler, targetName, targetName, this.fullName, lat, lon,
                                    time, weather);
                        }
                    }
                }
            }
        }
    }

    private void forwardFileToHandler(ClientHandler targetHandler, String fromContext, String chatTargetId,
                                      String senderFullName, File savedFile, String originalFileName,
                                      String subDataType) throws IOException {
        synchronized (targetHandler.dos) {
            targetHandler.dos.writeInt(subDataType.equals(DATA_TYPE_FILE) ? TYPE_FILE_TRANSFER : TYPE_VOICE_MESSAGE);
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
                                          String senderFullName, String lat, String lon, String time,
                                          String weather) throws IOException {
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

    // ======================= GỬI TIN RA CLIENT =====================

    public void sendUserListUpdate(
            ConcurrentHashMap<String, ClientHandler> userMap,
            Collection<GroupInfo> filteredGroups
    ) {
        try {
            dos.writeInt(TYPE_USER_LIST_UPDATE);

            dos.writeInt(userMap.size() - 1);
            for (ClientHandler handler : userMap.values()) {
                if (handler.clientId.equals(this.clientId)) continue;
                dos.writeUTF(handler.clientId);
                dos.writeUTF(handler.fullName);
                dos.writeBoolean(true); // online
            }

            dos.writeInt(filteredGroups.size());
            for (GroupInfo group : filteredGroups) {
                dos.writeUTF(group.groupName);
                dos.writeUTF(group.groupFullName);
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

    // Voice
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

    // Video
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

    // ========================== UI BUBBLE ==========================

    static class RoundedPanel extends JPanel {
        private final Color backgroundColor;
        private final int cornerRadius = 20;

        public RoundedPanel(java.awt.LayoutManager layout, Color backgroundColor) {
            super(layout);
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
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
        RoundedPanel bubble = new RoundedPanel(new java.awt.BorderLayout(), bgColor);

        String safeMessage = message.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
        JLabel label = new JLabel("<html><body>" + safeMessage + "</body></html>");
        label.setFont(isSystem ? UI_FONT_BOLD : UI_FONT);
        label.setForeground(alignRight && !isSystem ? Color.WHITE : Color.BLACK);
        label.setBorder(new EmptyBorder(8, 12, 8, 12));
        bubble.add(label, java.awt.BorderLayout.CENTER);
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
}
