package server.model;

/** Dùng để hiển thị tên + ID trong JList ở màn hình Admin */
public class UserDisplay {

    public String username;  // ID (SĐT)
    public String fullName;  // Tên hiển thị

    public UserDisplay(String username, String fullName) {
        this.username = username;
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        // JList sẽ hiển thị cái này
        return fullName + " (" + username + ")";
    }
}
