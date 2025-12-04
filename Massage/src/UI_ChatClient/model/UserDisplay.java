package UI_ChatClient.model;

/**
 * Lớp đại diện cho một người dùng hoặc nhóm trong danh sách chat
 */
public class UserDisplay {
    private String username;    // ID (SĐT hoặc Tên Nhóm)
    private String fullName;    // Tên hiển thị
    private boolean isGroup;    // Cờ để phân biệt User/Group
    private boolean isOnline;   // Cờ trạng thái online

    public UserDisplay(String username, String fullName, boolean isGroup, boolean isOnline) {
        this.username = username;
        this.fullName = fullName;
        this.isGroup = isGroup;
        this.isOnline = isOnline;
    }

    // Getters
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public boolean isGroup() { return isGroup; }
    public boolean isOnline() { return isOnline; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setGroup(boolean group) { isGroup = group; }
    public void setOnline(boolean online) { isOnline = online; }

    @Override
    public String toString() {
        return fullName;
    }
}
