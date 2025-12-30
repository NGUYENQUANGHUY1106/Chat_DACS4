package UI_ChatClient.model;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Lớp quản lý lưu trữ tin nhắn vào file JSON
 * Không cần database, chỉ dùng file
 */
public class MessageStorage {
    
    private static final String STORAGE_DIR = "chat_history";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Enum để phân loại tin nhắn
    public enum MessageType {
        TEXT, FILE, VOICE, LOCATION, SYSTEM
    }
    
    // Inner class để lưu thông tin tin nhắn
    public static class StoredMessage {
        public String sender;           // username người gửi
        public String content;          // nội dung tin nhắn
        public String timestamp;        // thời gian
        public MessageType type;        // loại tin nhắn
        public boolean isMyMessage;     // tin nhắn của mình hay người khác
        public String filePath;         // đường dẫn file (nếu có)
        
        public StoredMessage() {}
        
        public StoredMessage(String sender, String content, MessageType type, boolean isMyMessage, String filePath) {
            this.sender = sender;
            this.content = content;
            this.timestamp = LocalDateTime.now().format(DATE_FORMAT);
            this.type = type;
            this.isMyMessage = isMyMessage;
            this.filePath = filePath;
        }
        
        @Override
        public String toString() {
            return String.format("{sender:%s, content:%s, timestamp:%s, type:%s, isMyMessage:%s, filePath:%s}",
                    sender, content, timestamp, type, isMyMessage, filePath);
        }
    }
    
    private String myUsername;
    
    public MessageStorage(String myUsername) {
        this.myUsername = myUsername;
        createStorageDirectory();
    }
    
    /**
     * Tạo thư mục lưu trữ nếu chưa có
     */
    private void createStorageDirectory() {
        try {
            Path path = Paths.get(STORAGE_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Không thể tạo thư mục lưu trữ: " + e.getMessage());
        }
    }
    
    /**
     * Lấy tên file lưu trữ cho một cuộc hội thoại
     */
    private String getStorageFileName(String chatTarget) {
        // Tạo tên file duy nhất cho mỗi cuộc hội thoại
        // Dùng sắp xếp để đảm bảo file giống nhau cho cả 2 phía
        String[] users = {myUsername, chatTarget};
        Arrays.sort(users);
        return STORAGE_DIR + File.separator + users[0] + "_" + users[1] + ".txt";
    }
    
    /**
     * Lưu một tin nhắn vào file
     */
    public void saveMessage(String chatTarget, StoredMessage message) {
        String fileName = getStorageFileName(chatTarget);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            // Ghi tin nhắn dưới dạng JSON đơn giản
            writer.write(messageToJson(message));
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu tin nhắn: " + e.getMessage());
        }
    }
    
    /**
     * Đọc tất cả tin nhắn từ file
     */
    public List<StoredMessage> loadMessages(String chatTarget) {
        List<StoredMessage> messages = new ArrayList<>();
        String fileName = getStorageFileName(chatTarget);
        
        File file = new File(fileName);
        if (!file.exists()) {
            return messages; // File chưa tồn tại, trả về danh sách rỗng
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                StoredMessage message = jsonToMessage(line);
                if (message != null) {
                    messages.add(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc tin nhắn: " + e.getMessage());
        }
        
        return messages;
    }
    
    /**
     * Chuyển tin nhắn thành chuỗi JSON đơn giản
     */
    private String messageToJson(StoredMessage msg) {
        // Escape các ký tự đặc biệt trong JSON
        String escapedContent = escapeJson(msg.content);
        String escapedFilePath = msg.filePath != null ? escapeJson(msg.filePath) : "";
        
        return String.format("{\"sender\":\"%s\",\"content\":\"%s\",\"timestamp\":\"%s\",\"type\":\"%s\",\"isMyMessage\":%b,\"filePath\":\"%s\"}",
                msg.sender, escapedContent, msg.timestamp, msg.type, msg.isMyMessage, escapedFilePath);
    }
    
    /**
     * Chuyển chuỗi JSON thành tin nhắn
     */
    private StoredMessage jsonToMessage(String json) {
        try {
            StoredMessage msg = new StoredMessage();
            
            // Parse JSON thủ công (không dùng thư viện)
            msg.sender = extractJsonValue(json, "sender");
            msg.content = unescapeJson(extractJsonValue(json, "content"));
            msg.timestamp = extractJsonValue(json, "timestamp");
            msg.type = MessageType.valueOf(extractJsonValue(json, "type"));
            msg.isMyMessage = Boolean.parseBoolean(extractJsonValue(json, "isMyMessage"));
            msg.filePath = unescapeJson(extractJsonValue(json, "filePath"));
            
            if (msg.filePath != null && msg.filePath.isEmpty()) {
                msg.filePath = null;
            }
            
            return msg;
        } catch (Exception e) {
            System.err.println("Lỗi khi parse JSON: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lấy giá trị từ chuỗi JSON
     */
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) {
            // Thử với giá trị boolean/number (không có dấu ngoặc kép)
            pattern = "\"" + key + "\":";
            start = json.indexOf(pattern);
            if (start == -1) return "";
            
            start += pattern.length();
            int end = json.indexOf(",", start);
            if (end == -1) {
                end = json.indexOf("}", start);
            }
            return json.substring(start, end).trim();
        }
        
        start += pattern.length();
        int end = json.indexOf("\"", start);
        
        // Xử lý trường hợp có ký tự escape
        while (end > 0 && json.charAt(end - 1) == '\\') {
            end = json.indexOf("\"", end + 1);
        }
        
        if (end == -1) return "";
        return json.substring(start, end);
    }
    
    /**
     * Escape các ký tự đặc biệt trong JSON
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Unescape các ký tự đặc biệt trong JSON
     */
    private String unescapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t")
                  .replace("\\\"", "\"")
                  .replace("\\\\", "\\");
    }
    
    /**
     * Xóa tất cả tin nhắn của một cuộc hội thoại
     */
    public void clearMessages(String chatTarget) {
        String fileName = getStorageFileName(chatTarget);
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }
    
    /**
     * Kiểm tra xem có tin nhắn lưu trữ hay không
     */
    public boolean hasStoredMessages(String chatTarget) {
        String fileName = getStorageFileName(chatTarget);
        File file = new File(fileName);
        return file.exists() && file.length() > 0;
    }
}
