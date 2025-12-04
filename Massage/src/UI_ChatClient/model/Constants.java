package UI_ChatClient.model;

import java.awt.Color;
import java.awt.Font;

/**
 * Lớp chứa các hằng số được sử dụng trong ứng dụng
 */
public final class Constants {
    
    private Constants() {} // Không cho phép khởi tạo
    
    // === CÁC LOẠI TIN NHẮN (Protocol Types) ===
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
    public static final int TYPE_VOICE_CALL_DATA = 22;
    public static final int TYPE_VIDEO_CALL_REQUEST = 23;
    public static final int TYPE_VIDEO_CALL_INCOMING = 24;
    public static final int TYPE_VIDEO_CALL_ACCEPT = 25;
    public static final int TYPE_VIDEO_CALL_DECLINE = 26;
    public static final int TYPE_VIDEO_CALL_ACCEPTED = 27;
    public static final int TYPE_VIDEO_CALL_DECLINED = 28;
    public static final int TYPE_VIDEO_CALL_HANGUP = 29;
    public static final int TYPE_VIDEO_CALL_ENDED = 30;
    public static final int TYPE_VIDEO_CALL_DATA = 31;
    
    // === CẤU HÌNH SERVER ===
    public static final String SERVER_IP = "192.168.195.189";
    public static final int TCP_PORT = 1234;
    public static final int UDP_PORT = 1235;
    public static final int UDP_TYPE_REGISTER_CLIENT = 99;
    
    // === BẢNG MÀU HIỆN ĐẠI ===
    public static final Color PRIMARY_COLOR = new Color(99, 102, 241);      // Indigo hiện đại
    public static final Color SECONDARY_COLOR = new Color(249, 250, 251);   // Xám rất nhạt
    public static final Color ACCENT_COLOR = new Color(79, 70, 229);        // Indigo đậm hơn
    public static final Color SIDEBAR_BG_COLOR = new Color(17, 24, 39);     // Xanh đen sang trọng
    public static final Color SIDEBAR_TEXT_COLOR = new Color(229, 231, 235); // Trắng nhạt
    public static final Color HOVER_COLOR = new Color(31, 41, 55);          // Hover cho sidebar
    public static final Color ONLINE_COLOR = new Color(34, 197, 94);        // Xanh lá tươi
    public static final Color CHAT_BG_COLOR = new Color(243, 244, 246);     // Nền chat nhẹ nhàng
    
    // === MÀU TIN NHẮN ===
    public static final Color MY_MESSAGE_COLOR = new Color(99, 102, 241);       // Indigo cho tin nhắn mình
    public static final Color OTHER_MESSAGE_COLOR = new Color(255, 255, 255);   // Trắng cho tin người khác
    public static final Color SYSTEM_MESSAGE_COLOR = new Color(209, 213, 219);  // Xám nhạt cho hệ thống
    
    // === FONT CHỮ ===
    public static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font UI_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font UI_FONT_LARGE = new Font("Segoe UI", Font.BOLD, 18);
}
