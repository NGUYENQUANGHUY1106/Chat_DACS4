package UI_ChatClient.controller;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

import UI_ChatClient.model.Constants;
import UI_ChatClient.model.ChatState;
import UI_ChatClient.view.dialogs.*;

/**
 * Controller xử lý kết nối mạng TCP/UDP
 */
public class NetworkController {
    
    private DataInputStream dis;
    private DataOutputStream dos;
    private DatagramSocket udpSocket;
    private InetSocketAddress serverUdpAddress;
    private Thread udpReceiverThread;
    
    private ChatState chatState;
    private MessageHandler messageHandler;
    
    // Call session
    private TargetDataLine callMicLine;
    private SourceDataLine callSpeakerLine;
    private Thread audioCaptureThread;
    private Webcam webcam;
    private Thread videoCaptureThread;
    private JPanel myVideoPanel;
    private JPanel partnerVideoPanel;
    
    // Dialogs
    private ActiveCallWindow activeCallWindow;
    private OutgoingCallDialog outgoingCallDialog;
    
    private final AudioFormat callAudioFormat = new AudioFormat(16000, 8, 1, true, true);
    
    public interface MessageHandler {
        void onUserListUpdate(DataInputStream dis) throws IOException;
        void onPrivateMessage(DataInputStream dis) throws IOException;
        void onGroupMessage(DataInputStream dis) throws IOException;
        void onSystemMessage(String message);
        void onFileReceived(DataInputStream dis) throws IOException;
        void onLocationReceived(DataInputStream dis) throws IOException;
        void onVoiceMessageReceived(DataInputStream dis) throws IOException;
        void onCallIncoming(String fromUser, String fromFullName, boolean isVideo);
        void onCallAccepted(String targetUser, boolean isVideo);
        void onCallDeclined(String targetUser, boolean isVideo);
        void onCallEnded(String fromUser, boolean isVideo);
    }
    
    public NetworkController(ChatState chatState, MessageHandler handler) {
        this.chatState = chatState;
        this.messageHandler = handler;
    }
    
    public void connect(String username) throws IOException {
        // === 1. KẾT NỐI TCP ===
        Socket socket = new Socket(Constants.SERVER_IP, Constants.TCP_PORT);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_REGISTER_USERNAME);
            dos.writeUTF(username);
            dos.flush();
        }
        
        // === 2. KHỞI CHẠY LUỒNG ĐỌC TCP ===
        Thread readerThread = new Thread(() -> runTcpReader());
        readerThread.setDaemon(true);
        readerThread.start();
        
        // === 3. KẾT NỐI VÀ ĐĂNG KÝ UDP ===
        udpSocket = new DatagramSocket();
        serverUdpAddress = new InetSocketAddress(Constants.SERVER_IP, Constants.UDP_PORT);
        
        sendUdpRegistration();
        
        udpReceiverThread = new Thread(this::runUdpReceiver);
        udpReceiverThread.setDaemon(true);
        udpReceiverThread.start();
    }
    
    private void runTcpReader() {
        try {
            while (true) {
                int dataType = dis.readInt();
                handleTcpMessage(dataType);
            }
        } catch (IOException e) {
            if (udpSocket == null || !udpSocket.isClosed()) {
                messageHandler.onSystemMessage("Đã mất kết nối với server (TCP).");
                if (chatState.isInCall() || chatState.isInVideoCall()) {
                    stopCall();
                }
            }
        }
    }
    
    private void handleTcpMessage(int dataType) throws IOException {
        switch (dataType) {
            case Constants.TYPE_USER_LIST_UPDATE:
                messageHandler.onUserListUpdate(dis);
                break;
            case Constants.TYPE_RECEIVE_PRIVATE_MESSAGE:
                messageHandler.onPrivateMessage(dis);
                break;
            case Constants.TYPE_RECEIVE_GROUP_MESSAGE:
                messageHandler.onGroupMessage(dis);
                break;
            case Constants.TYPE_SYSTEM_MESSAGE:
                String sysMsg = dis.readUTF();
                messageHandler.onSystemMessage(sysMsg);
                break;
            case Constants.TYPE_FILE_TRANSFER:
                messageHandler.onFileReceived(dis);
                break;
            case Constants.TYPE_LOCATION_SHARE:
                messageHandler.onLocationReceived(dis);
                break;
            case Constants.TYPE_VOICE_MESSAGE:
                messageHandler.onVoiceMessageReceived(dis);
                break;
            // Voice Call
            case Constants.TYPE_VOICE_CALL_INCOMING: {
                String fromUser = dis.readUTF();
                String fromFullName = dis.readUTF();
                messageHandler.onCallIncoming(fromUser, fromFullName, false);
                break;
            }
            case Constants.TYPE_VOICE_CALL_ACCEPTED: {
                String targetUser = dis.readUTF();
                messageHandler.onCallAccepted(targetUser, false);
                break;
            }
            case Constants.TYPE_VOICE_CALL_DECLINED: {
                String targetUser = dis.readUTF();
                messageHandler.onCallDeclined(targetUser, false);
                break;
            }
            case Constants.TYPE_VOICE_CALL_ENDED: {
                String fromUser = dis.readUTF();
                messageHandler.onCallEnded(fromUser, false);
                break;
            }
            // Video Call
            case Constants.TYPE_VIDEO_CALL_INCOMING: {
                String fromUser = dis.readUTF();
                String fromFullName = dis.readUTF();
                messageHandler.onCallIncoming(fromUser, fromFullName, true);
                break;
            }
            case Constants.TYPE_VIDEO_CALL_ACCEPTED: {
                String targetUser = dis.readUTF();
                messageHandler.onCallAccepted(targetUser, true);
                break;
            }
            case Constants.TYPE_VIDEO_CALL_DECLINED: {
                String targetUser = dis.readUTF();
                messageHandler.onCallDeclined(targetUser, true);
                break;
            }
            case Constants.TYPE_VIDEO_CALL_ENDED: {
                String fromUser = dis.readUTF();
                messageHandler.onCallEnded(fromUser, true);
                break;
            }
        }
    }
    
    private void sendUdpRegistration() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(baos);
        
        dataOut.writeInt(Constants.UDP_TYPE_REGISTER_CLIENT);
        dataOut.writeUTF(chatState.getMyUsername());
        dataOut.flush();
        
        byte[] packetData = baos.toByteArray();
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, serverUdpAddress);
        udpSocket.send(packet);
    }
    
    private void runUdpReceiver() {
        byte[] buffer = new byte[65507];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while (true) {
            try {
                udpSocket.receive(packet);
                
                if (chatState.isInCall() || chatState.isInVideoCall()) {
                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                    int dataType = dis.readInt();
                    
                    switch (dataType) {
                        case Constants.TYPE_VOICE_CALL_DATA:
                            handleReceiveVoiceData(dis);
                            break;
                        case Constants.TYPE_VIDEO_CALL_DATA:
                            handleReceiveVideoData(dis);
                            break;
                    }
                }
                
            } catch (IOException e) {
                if (udpSocket.isClosed()) {
                    break;
                }
                System.err.println("Lỗi nhận UDP: " + e.getMessage());
            }
        }
    }
    
    private void handleReceiveVoiceData(DataInputStream dis) throws IOException {
        int dataLength = dis.readInt();
        byte[] audioData = new byte[dataLength];
        dis.readFully(audioData, 0, dataLength);
        if (chatState.isInCall() && callSpeakerLine != null) {
            callSpeakerLine.write(audioData, 0, dataLength);
        }
    }
    
    private void handleReceiveVideoData(DataInputStream dis) throws IOException {
        int dataLength = dis.readInt();
        byte[] videoData = new byte[dataLength];
        dis.readFully(videoData, 0, dataLength);
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(videoData);
            BufferedImage image = ImageIO.read(bais);
            if (image != null && partnerVideoPanel != null) {
                SwingUtilities.invokeLater(() -> {
                    Graphics g = partnerVideoPanel.getGraphics();
                    if (g != null) {
                        g.drawImage(image, 0, 0, partnerVideoPanel.getWidth(), partnerVideoPanel.getHeight(), null);
                        g.dispose();
                    }
                });
            }
        } catch (IOException e) {
            // Ignore invalid image data
        }
    }
    
    // === SEND METHODS ===
    
    public void sendPrivateMessage(String target, String message) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_PRIVATE_MESSAGE);
            dos.writeUTF(target);
            dos.writeUTF(message);
            dos.flush();
        }
    }
    
    public void sendGroupMessage(String groupName, String message) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_GROUP_MESSAGE);
            dos.writeUTF(groupName);
            dos.writeUTF(message);
            dos.flush();
        }
    }
    
    public void sendFile(String targetType, String target, File file) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_FILE_TRANSFER);
            dos.writeUTF(targetType);
            dos.writeUTF(target);
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1)
                    dos.write(buffer, 0, bytesRead);
            }
            dos.flush();
        }
    }
    
    public void sendLocation(String targetType, String target, String lat, String lon, String time, String weather) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_LOCATION_SHARE);
            dos.writeUTF(targetType);
            dos.writeUTF(target);
            dos.writeUTF(lat);
            dos.writeUTF(lon);
            dos.writeUTF(time);
            dos.writeUTF(weather);
            dos.flush();
        }
    }
    
    public void sendVoiceMessage(String targetType, String target, File file) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_VOICE_MESSAGE);
            dos.writeUTF(targetType);
            dos.writeUTF(target);
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
            }
            dos.flush();
        }
    }
    
    public void sendCreateGroupRequest(String groupName, String groupFullName, java.util.List<String> members) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_CREATE_GROUP_REQUEST);
            dos.writeUTF(groupName);
            dos.writeUTF(groupFullName);
            dos.writeInt(members.size());
            for (String member : members) {
                dos.writeUTF(member);
            }
            dos.flush();
        }
    }
    
    public void sendAddMembersToGroup(String groupName, java.util.List<String> members) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_ADD_MEMBERS_TO_GROUP);
            dos.writeUTF(groupName);
            dos.writeInt(members.size());
            for (String member : members) {
                dos.writeUTF(member);
            }
            dos.flush();
        }
    }
    
    // === CALL METHODS ===
    
    public void sendVoiceCallRequest(String target) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_VOICE_CALL_REQUEST);
            dos.writeUTF(target);
            dos.flush();
        }
    }
    
    public void sendVoiceCallAccept(String target) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_VOICE_CALL_ACCEPT);
            dos.writeUTF(target);
            dos.flush();
        }
    }
    
    public void sendVoiceCallDecline(String target) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_VOICE_CALL_DECLINE);
            dos.writeUTF(target);
            dos.flush();
        }
    }
    
    public void sendVoiceCallHangup(String target) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_VOICE_CALL_HANGUP);
            dos.writeUTF(target);
            dos.flush();
        }
    }
    
    public void sendVideoCallRequest(String target) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_VIDEO_CALL_REQUEST);
            dos.writeUTF(target);
            dos.flush();
        }
    }
    
    public void sendVideoCallAccept(String target) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_VIDEO_CALL_ACCEPT);
            dos.writeUTF(target);
            dos.flush();
        }
    }
    
    public void sendVideoCallDecline(String target) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_VIDEO_CALL_DECLINE);
            dos.writeUTF(target);
            dos.flush();
        }
    }
    
    public void sendVideoCallHangup(String target) throws IOException {
        synchronized (dos) {
            dos.writeInt(Constants.TYPE_VIDEO_CALL_HANGUP);
            dos.writeUTF(target);
            dos.flush();
        }
    }
    
    // === CALL SESSION ===
    
    public void startCallSession(String partnerUsername, String partnerFullName, JFrame owner) {
        if (chatState.isInCall() || chatState.isInVideoCall()) return;
        
        chatState.setInCall(true);
        chatState.setCallPartnerUsername(partnerUsername);
        
        try {
            // Mở cửa sổ đang gọi
            SwingUtilities.invokeLater(() -> {
                activeCallWindow = new ActiveCallWindow(partnerFullName, false, this::stopCall);
                activeCallWindow.setVisible(true);
            });
            
            // Thiết lập Loa
            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, callAudioFormat);
            callSpeakerLine = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            callSpeakerLine.open(callAudioFormat);
            callSpeakerLine.start();
            
            // Thiết lập Mic
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, callAudioFormat);
            callMicLine = (TargetDataLine) AudioSystem.getLine(micInfo);
            callMicLine.open(callAudioFormat);
            callMicLine.start();
            
            // Luồng gửi âm thanh
            audioCaptureThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                
                while (chatState.isInCall()) {
                    try {
                        if (activeCallWindow != null && activeCallWindow.isMicMuted()) {
                            callMicLine.read(buffer, 0, buffer.length);
                            Thread.sleep(10);
                            continue;
                        }
                        
                        int bytesRead = callMicLine.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            DataOutputStream dataOut = new DataOutputStream(baos);
                            dataOut.writeInt(Constants.TYPE_VOICE_CALL_DATA);
                            dataOut.writeInt(bytesRead);
                            dataOut.write(buffer, 0, bytesRead);
                            dataOut.flush();
                            
                            byte[] packetData = baos.toByteArray();
                            DatagramPacket packet = new DatagramPacket(packetData, packetData.length, serverUdpAddress);
                            udpSocket.send(packet);
                        }
                    } catch (Exception e) {
                        if (chatState.isInCall()) SwingUtilities.invokeLater(this::stopCall);
                        break;
                    }
                }
            });
            audioCaptureThread.setDaemon(true);
            audioCaptureThread.start();
            
        } catch (LineUnavailableException e) {
            messageHandler.onSystemMessage("Lỗi thiết bị âm thanh: " + e.getMessage());
            stopCall();
        }
    }
    
    public void startVideoCallSession(String partnerUsername, String partnerFullName, JFrame owner) {
        if (chatState.isInCall() || chatState.isInVideoCall()) return;
        
        chatState.setInCall(true);
        chatState.setInVideoCall(true);
        chatState.setCallPartnerUsername(partnerUsername);
        
        try {
            // Mở cửa sổ ActiveCallWindow (Mode Video)
            SwingUtilities.invokeLater(() -> {
                activeCallWindow = new ActiveCallWindow(partnerFullName, true, this::stopCall);
                activeCallWindow.setVisible(true);
                
                myVideoPanel = new JPanel();
                myVideoPanel.setBackground(Color.BLACK);
                
                partnerVideoPanel = new JPanel();
                partnerVideoPanel.setBackground(Color.BLACK);
                
                JPanel container = activeCallWindow.getVideoPanel();
                container.setLayout(new GridLayout(1, 2));
                container.add(myVideoPanel);
                container.add(partnerVideoPanel);
                container.revalidate();
            });
            
            // Thiết lập Âm thanh
            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, callAudioFormat);
            callSpeakerLine = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            callSpeakerLine.open(callAudioFormat);
            callSpeakerLine.start();
            
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, callAudioFormat);
            callMicLine = (TargetDataLine) AudioSystem.getLine(micInfo);
            callMicLine.open(callAudioFormat);
            callMicLine.start();
            
            // Luồng gửi âm thanh
            audioCaptureThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (chatState.isInVideoCall()) {
                    try {
                        if (activeCallWindow != null && activeCallWindow.isMicMuted()) {
                            callMicLine.read(buffer, 0, buffer.length);
                            Thread.sleep(10);
                            continue;
                        }
                        int bytesRead = callMicLine.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            DataOutputStream dataOut = new DataOutputStream(baos);
                            dataOut.writeInt(Constants.TYPE_VOICE_CALL_DATA);
                            dataOut.writeInt(bytesRead);
                            dataOut.write(buffer, 0, bytesRead);
                            dataOut.flush();
                            byte[] packetData = baos.toByteArray();
                            DatagramPacket packet = new DatagramPacket(packetData, packetData.length, serverUdpAddress);
                            udpSocket.send(packet);
                        }
                    } catch (Exception e) { break; }
                }
            });
            audioCaptureThread.setDaemon(true);
            audioCaptureThread.start();
            
            // Thiết lập Webcam
            webcam = Webcam.getDefault();
            if (webcam == null) throw new IOException("Không tìm thấy webcam.");
            webcam.setViewSize(new Dimension(320, 240));
            webcam.open();
            
            // Luồng gửi Video
            videoCaptureThread = new Thread(() -> {
                while (chatState.isInVideoCall() && webcam.isOpen()) {
                    try {
                        if (activeCallWindow != null && activeCallWindow.isCamOff()) {
                            SwingUtilities.invokeLater(() -> {
                                if (myVideoPanel != null) {
                                    Graphics g = myVideoPanel.getGraphics();
                                    if(g != null) {
                                        g.setColor(Color.BLACK);
                                        g.fillRect(0, 0, myVideoPanel.getWidth(), myVideoPanel.getHeight());
                                        g.dispose();
                                    }
                                }
                            });
                            Thread.sleep(100);
                            continue;
                        }
                        
                        BufferedImage image = webcam.getImage();
                        if (image == null) continue;
                        
                        // Hiển thị video của mình
                        SwingUtilities.invokeLater(() -> {
                            if (myVideoPanel != null) {
                                Graphics g = myVideoPanel.getGraphics();
                                if(g != null) {
                                    g.drawImage(image, 0, 0, myVideoPanel.getWidth(), myVideoPanel.getHeight(), null);
                                    g.dispose();
                                }
                            }
                        });
                        
                        // Gửi video
                        ByteArrayOutputStream baosImage = new ByteArrayOutputStream();
                        ImageIO.write(image, "jpg", baosImage);
                        byte[] videoData = baosImage.toByteArray();
                        
                        ByteArrayOutputStream baosPacket = new ByteArrayOutputStream();
                        DataOutputStream dataOut = new DataOutputStream(baosPacket);
                        dataOut.writeInt(Constants.TYPE_VIDEO_CALL_DATA);
                        dataOut.writeInt(videoData.length);
                        dataOut.write(videoData);
                        dataOut.flush();
                        
                        byte[] packetData = baosPacket.toByteArray();
                        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, serverUdpAddress);
                        udpSocket.send(packet);
                        
                        Thread.sleep(33);
                        
                    } catch (Exception e) { break; }
                }
            });
            videoCaptureThread.setDaemon(true);
            videoCaptureThread.start();
            
        } catch (Exception e) {
            e.printStackTrace();
            messageHandler.onSystemMessage("Lỗi khi bắt đầu video call: " + e.getMessage());
            stopCall();
        }
    }
    
    public void stopCall() {
        if (!chatState.isInCall() && !chatState.isInVideoCall()) return;
        
        boolean wasVideoCall = chatState.isInVideoCall();
        chatState.setInCall(false);
        chatState.setInVideoCall(false);
        
        // Gửi tín hiệu gác máy
        try {
            synchronized (dos) {
                if(wasVideoCall) {
                    dos.writeInt(Constants.TYPE_VIDEO_CALL_HANGUP);
                } else {
                    dos.writeInt(Constants.TYPE_VOICE_CALL_HANGUP);
                }
                dos.writeUTF(chatState.getCallPartnerUsername());
                dos.flush();
            }
        } catch (IOException e) { }
        
        // Dọn dẹp Thread
        if (audioCaptureThread != null) { audioCaptureThread.interrupt(); audioCaptureThread = null; }
        if (callMicLine != null) { callMicLine.stop(); callMicLine.close(); callMicLine = null; }
        if (callSpeakerLine != null) { callSpeakerLine.stop(); callSpeakerLine.close(); callSpeakerLine = null; }
        if (videoCaptureThread != null) { videoCaptureThread.interrupt(); videoCaptureThread = null; }
        if (webcam != null && webcam.isOpen()) { webcam.close(); webcam = null; }
        
        // Đóng cửa sổ gọi
        if (activeCallWindow != null) {
            activeCallWindow.close();
            activeCallWindow = null;
        }
        
        chatState.setCallPartnerUsername(null);
    }
    
    public void closeOutgoingCallDialog() {
        if (outgoingCallDialog != null) {
            outgoingCallDialog.closeDialog();
            outgoingCallDialog = null;
        }
    }
    
    public void setOutgoingCallDialog(OutgoingCallDialog dialog) {
        this.outgoingCallDialog = dialog;
    }
    
    public OutgoingCallDialog getOutgoingCallDialog() {
        return outgoingCallDialog;
    }
    
    public ActiveCallWindow getActiveCallWindow() {
        return activeCallWindow;
    }
    
    public void close() {
        if (chatState.isInCall() || chatState.isInVideoCall()) {
            stopCall();
        }
        if (udpSocket != null) {
            udpSocket.close();
        }
        if (udpReceiverThread != null) {
            udpReceiverThread.interrupt();
        }
    }
    
    public DataInputStream getDataInputStream() {
        return dis;
    }
    
    public DataOutputStream getDataOutputStream() {
        return dos;
    }
}
