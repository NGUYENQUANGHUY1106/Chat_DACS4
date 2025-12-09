package server.core;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import server.model.GroupInfo;
import server.model.UserDisplay;
import server.view.DashboardPanel;
import server.view.FileManagementPanel;
import server.view.UserManagementPanel;

public class ChatServerCore {

    // ======================================================
    // CÁC CONST TYPE (CHUNG TCP/UDP) - GIỮ NGUYÊN
    // ======================================================
    public static final int TYPE_FILE_TRANSFER = 2;
    public static final int TYPE_LOCATION_SHARE = 3;
    public static final int TYPE_VOICE_MESSAGE = 4;
    public static final int TYPE_REGISTER_USERNAME = 5;
    public static final int TYPE_USER_LIST_UPDATE = 6;
    public static final int TYPE_PRIVATE_MESSAGE = 7;
    public static final int TYPE_GROUP_MESSAGE = 8;
    public static final int TYPE_CREATE_GROUP_REQUEST = 9;
    public static final int TYPE_RECEIVE_PRIVATE_MESSAGE = 10;
    public static final int TYPE_RECEIVE_GROUP_MESSAGE = 11;
    public static final int TYPE_SYSTEM_MESSAGE = 12;
    public static final int TYPE_ADD_MEMBERS_TO_GROUP = 13;

    public static final int TYPE_VOICE_CALL_REQUEST = 14;
    public static final int TYPE_VOICE_CALL_INCOMING = 15;
    public static final int TYPE_VOICE_CALL_ACCEPT = 16;
    public static final int TYPE_VOICE_CALL_DECLINE = 17;
    public static final int TYPE_VOICE_CALL_ACCEPTED = 18;
    public static final int TYPE_VOICE_CALL_DECLINED = 19;
    public static final int TYPE_VOICE_CALL_HANGUP = 20;
    public static final int TYPE_VOICE_CALL_ENDED = 21;
    public static final int TYPE_VOICE_CALL_DATA = 22; // UDP

    public static final int TYPE_VIDEO_CALL_REQUEST = 23;
    public static final int TYPE_VIDEO_CALL_INCOMING = 24;
    public static final int TYPE_VIDEO_CALL_ACCEPT = 25;
    public static final int TYPE_VIDEO_CALL_DECLINE = 26;
    public static final int TYPE_VIDEO_CALL_ACCEPTED = 27;
    public static final int TYPE_VIDEO_CALL_DECLINED = 28;
    public static final int TYPE_VIDEO_CALL_HANGUP = 29;
    public static final int TYPE_VIDEO_CALL_ENDED = 30;
    public static final int TYPE_VIDEO_CALL_DATA = 31; // UDP

    public static final int TYPE_INVITE_TO_CALL_REQUEST = 32;
    public static final int TYPE_JOIN_CALL_REQUEST = 33;
    public static final int TYPE_CALL_STATUS_UPDATE = 34;
    public static final int TYPE_CALL_JOINED_SUCCESS = 35;
// ======================================================
    // UDP
    // ======================================================
    public static final int TCP_PORT = 1234;
    public static final int UDP_PORT = 1235;
    public static final int UDP_TYPE_REGISTER_CLIENT = 99;

    public static DatagramSocket udpSocket;

    // UDP maps
    public static final ConcurrentHashMap<SocketAddress, String> udpClientMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, SocketAddress> udpAddressBook = new ConcurrentHashMap<>();

    // ======================================================
    // DATA TYPE SUB
    // ======================================================
    public static final String DATA_TYPE_FILE = "FILE";
    public static final String DATA_TYPE_LOCATION = "LOCATION";
    public static final String DATA_TYPE_VOICE = "VOICE";

    // ======================================================
    // MAPS CHÍNH
    // ======================================================
    public static final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, GroupInfo> groups = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, String> activeCalls = new ConcurrentHashMap<>();

    // ======================================================
    // THAM CHIẾU GUI (ĐƯỢC GÁN TỪ ServerAdminGUI)
    // ======================================================
    public static DashboardPanel dashboardPanel;
    public static UserManagementPanel userManagementPanel;
    public static FileManagementPanel fileManagementPanel;

    public static JPanel roomPanel;

    public static DefaultListModel<String> groupListModel;
    public static JList<String> groupList;
    public static DefaultListModel<UserDisplay> memberListModel;
    public static JList<UserDisplay> memberList;

    public static DefaultListModel<String> chatClientListModel;
    public static JList<String> chatClientList;
    public static JPanel serverChatWindowsPanel;
    public static java.awt.CardLayout serverChatCardLayout;

    private ChatServerCore() {
    }

    // Gọi hàm này từ GUI sau khi tạo xong các component
    public static void initGUIReferences(
            DashboardPanel dashboard,
            UserManagementPanel userPanel,
            FileManagementPanel filePanel,
            DefaultListModel<String> groupModel,
            JList<String> groupListComp,
            DefaultListModel<UserDisplay> memberModel,
            JList<UserDisplay> memberListComp,
            DefaultListModel<String> chatClientModel,
            JList<String> chatClientListComp,
            JPanel chatWindowsPanel,
            java.awt.CardLayout chatCardLayout
    ) {
        dashboardPanel = dashboard;
        userManagementPanel = userPanel;
        fileManagementPanel = filePanel;
        groupListModel = groupModel;
groupList = groupListComp;
        memberListModel = memberModel;
        memberList = memberListComp;
        chatClientListModel = chatClientModel;
        chatClientList = chatClientListComp;
        serverChatWindowsPanel = chatWindowsPanel;
        serverChatCardLayout = chatCardLayout;
    }

    // ======================================================
    // LOG / DASHBOARD
    // ======================================================
    public static void addSystemLog(String message) {
        System.out.println("LOG: " + message);
        SwingUtilities.invokeLater(() -> {
            if (dashboardPanel != null) {
                dashboardPanel.addLog(message);
            }
        });
    }

    public static void updateDashboardCounts() {
        if (dashboardPanel != null) {
            dashboardPanel.updateUserCount(clients.size());
        }
    }

    // ======================================================
    // KHỞI ĐỘNG SERVER
    // ======================================================
    public static void startServer() {
        try {
            Files.createDirectories(Paths.get("server_downloads"));
            addSystemLog("Thư mục 'server_downloads' đã sẵn sàng.");
        } catch (IOException e) {
            addSystemLog("LỖI: Không thể tạo thư mục 'server_downloads': " + e.getMessage());
        }

        ExecutorService executor = Executors.newCachedThreadPool();

        // UDP relay
        try {
            udpSocket = new DatagramSocket(UDP_PORT);
            UdpRelayThread udpRelay = new UdpRelayThread();
            executor.execute(udpRelay);
            addSystemLog("UDP Relay started... Listening for data on port " + UDP_PORT);
        } catch (IOException e) {
            addSystemLog("SERVER ERROR: Không thể khởi động UDP socket trên port " + UDP_PORT + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // TCP server
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

    // ======================================================
    // BROADCAST / HỖ TRỢ LOGIC
    // ======================================================
    public static void broadcastUserListUpdate() {
        for (ClientHandler handler : clients.values()) {
            List<GroupInfo> filteredGroups = new ArrayList<>();
            for (GroupInfo group : groups.values()) {
                if (group.members.contains(handler.clientId)) {
filteredGroups.add(group);
                }
            }
            handler.sendUserListUpdate(clients, filteredGroups);
        }
    }

    public static void broadcastSystemMessage(String message, String exceptUser) {
        for (ClientHandler handler : clients.values()) {
            if (exceptUser == null || !handler.clientId.equals(exceptUser)) {
                handler.sendSystemMessage(message);
            }
        }
    }

    /**
     * Hàm kết thúc cuộc gọi, đã gộp phiên bản hỗ trợ Group Call.
     */
    public static synchronized void endCall(String userA, String userB) {
        if (userA == null) return;

        // Lấy contextId của userA (có thể là ID người hoặc ID nhóm)
        String contextId = activeCalls.get(userA);
        
        // Xóa activeCalls của userA trước
        activeCalls.remove(userA);
        addSystemLog("Đã xóa activeCalls của: " + userA);

        if (contextId != null && groups.containsKey(contextId)) {
            // Đây là cuộc gọi nhóm - CHỈ xóa activeCalls, KHÔNG xóa thành viên khỏi nhóm chat
            GroupInfo group = groups.get(contextId);
            
            // Gửi ENDED cho chính userA
            ClientHandler handlerA = clients.get(userA);
            if (handlerA != null) {
                try {
                    handlerA.dos.writeInt(TYPE_VOICE_CALL_ENDED);
                    handlerA.dos.writeUTF(contextId);
                    handlerA.dos.flush();
                } catch (IOException e) { /* ignore */ }
            }
            
            // Kiểm tra xem còn ai trong cuộc gọi nhóm không
            boolean anyoneStillInCall = false;
            for (String memberId : group.members) {
                if (activeCalls.containsKey(memberId) && contextId.equals(activeCalls.get(memberId))) {
                    anyoneStillInCall = true;
                    break;
                }
            }
            
            if (!anyoneStillInCall) {
                addSystemLog("Cuộc gọi nhóm " + contextId + " đã kết thúc (không còn ai)");
            }
            
        } else if (userB != null) {
            // Cuộc gọi 1-1
            activeCalls.remove(userB);
            addSystemLog("Đã kết thúc cuộc gọi (TCP/UDP) giữa: " + userA + " và " + userB);

            ClientHandler handlerB = clients.get(userB);
            if (handlerB != null) {
                try {
                    handlerB.dos.writeInt(TYPE_VOICE_CALL_ENDED);
                    handlerB.dos.writeUTF(userA);
                    handlerB.dos.flush();
                } catch (IOException e) { /* ignore */ }
            }
        }
    }

    // ======================================================
    // HỖ TRỢ PHÒNG CHAT (GUI)
    // ======================================================
    public static GroupInfo findGroupByName(String fullName) {
        if (fullName == null) return null;
for (GroupInfo group : groups.values()) {
            if (fullName.equals(group.groupFullName)) {
                return group;
            }
        }
        return null;
    }

    public static void updateMemberPanel(String selectedGroupFullName) {
        SwingUtilities.invokeLater(() -> {
            if (memberListModel == null) return;

            memberListModel.clear();
            GroupInfo group = findGroupByName(selectedGroupFullName);

            if (group != null) {
                for (String memberId : group.members) {
                    ClientHandler handler = clients.get(memberId);
                    String fullName = memberId;
                    if (handler != null) {
                        fullName = handler.fullName;
                    } else {
                        fullName = memberId + " (Offline)";
                    }
                    memberListModel.addElement(new UserDisplay(memberId, fullName));
                }
            }
        });
    }

}
