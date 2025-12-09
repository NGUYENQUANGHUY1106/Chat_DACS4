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
    public static final int TYPE_SCREEN_SHARE_DATA = 32;
    
    // === CẤU HÌNH SERVER ===
    public static final String SERVER_IP = "192.168.195.189";
    public static final int TCP_PORT = 1234;
    public static final int UDP_PORT = 1235;
    public static final int UDP_TYPE_REGISTER_CLIENT = 99;
    
    // === BẢNG MÀU GRADIENT TRẮNG - XANH NGỌC NHẠT ===
    public static final Color PRIMARY_COLOR = new Color(94, 234, 212);       // Xanh ngọc chính (Teal-300)
    public static final Color PRIMARY_DARK = new Color(45, 212, 191);        // Xanh ngọc đậm (Teal-400)
    public static final Color PRIMARY_LIGHT = new Color(153, 246, 228);      // Xanh ngọc nhạt (Teal-200)
    public static final Color SECONDARY_COLOR = new Color(255, 255, 255);    // Trắng tinh khôi
    public static final Color ACCENT_COLOR = new Color(20, 184, 166);        // Xanh ngọc accent (Teal-500)
    public static final Color SIDEBAR_BG_COLOR = new Color(240, 253, 250);   // Trắng pha xanh ngọc nhạt
    public static final Color SIDEBAR_TEXT_COLOR = new Color(19, 78, 74);    // Xanh đen
    public static final Color HOVER_COLOR = new Color(204, 251, 241);        // Xanh ngọc rất nhạt hover (Teal-100)
    public static final Color ONLINE_COLOR = new Color(16, 185, 129);        // Xanh lá tươi (Emerald-500)
    public static final Color CHAT_BG_COLOR = new Color(249, 250, 251);      // Nền chat trắng xám nhẹ
    
    // === MÀU TIN NHẮN ===
    public static final Color MY_MESSAGE_COLOR = new Color(94, 234, 212);        // Xanh ngọc cho tin nhắn mình
    public static final Color OTHER_MESSAGE_COLOR = new Color(255, 255, 255);    // Trắng cho tin người khác
    public static final Color SYSTEM_MESSAGE_COLOR = new Color(224, 242, 241);   // Xanh ngọc rất nhạt cho hệ thống
    
    // === FONT CHỮ ===
    public static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font UI_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font UI_FONT_LARGE = new Font("Segoe UI", Font.BOLD, 18);
}
