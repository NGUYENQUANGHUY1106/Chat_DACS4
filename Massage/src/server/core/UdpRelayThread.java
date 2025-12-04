package server.core;

import static server.core.ChatServerCore.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;

import server.model.GroupInfo;

public class UdpRelayThread implements Runnable {

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
                    String targetId = activeCalls.get(fromId); // lấy ID người nhận/nhóm nhận

                    if (targetId != null) {
                        // 1. Nếu target là nhóm -> gửi cho tất cả thành viên
                        if (groups.containsKey(targetId)) {
                            GroupInfo group = groups.get(targetId);
                            for (String memberId : group.members) {
                                if (memberId.equals(fromId)) continue;
                                SocketAddress targetAddress = udpAddressBook.get(memberId);
                                if (targetAddress != null) {
                                    packet.setSocketAddress(targetAddress);
                                    udpSocket.send(packet);
                                }
                            }
                        } else {
                            // 2. 1-1
                            SocketAddress targetAddress = udpAddressBook.get(targetId);
                            if (targetAddress != null) {
                                packet.setSocketAddress(targetAddress);
                                udpSocket.send(packet);
                            }
                        }
                        continue;
                    }
                }

                // Nếu chưa biết client -> kiểm tra gói đăng ký
                try (DataInputStream dis = new DataInputStream(
                        new ByteArrayInputStream(packet.getData(), 0, length))) {
                    int dataType = dis.readInt();
                    if (dataType == UDP_TYPE_REGISTER_CLIENT) {
                        String clientId = dis.readUTF();
                        udpClientMap.put(senderAddress, clientId);
                        udpAddressBook.put(clientId, senderAddress);
                        addSystemLog("UDP: Client " + clientId + " đã đăng ký địa chỉ " + senderAddress);
                    }
                } catch (IOException ignored) {
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
