# Hướng Dẫn: Lưu Trữ Tin Nhắn Không Dùng Database

## 📋 Tổng Quan

Hệ thống lưu trữ tin nhắn này giúp bạn lưu lại toàn bộ lịch sử chat vào file text, không cần database. Khi bạn đóng và mở lại ứng dụng, tất cả tin nhắn cũ sẽ được load lại tự động.

## 🎯 Cách Hoạt Động

### 1. **Lưu Trữ Tự Động**
- Mỗi khi bạn **gửi** hoặc **nhận** tin nhắn, hệ thống sẽ tự động lưu vào file
- Mỗi cuộc hội thoại có 1 file riêng
- File được lưu trong thư mục `chat_history/`

### 2. **Load Tin Nhắn Cũ**
- Khi bạn click vào một người dùng/nhóm trong danh sách
- Nếu đã có tin nhắn cũ, chúng sẽ tự động hiển thị
- Có dòng phân cách "── Tin nhắn trước đó ──" để phân biệt

### 3. **Hỗ Trợ Đầy Đủ**
Hệ thống lưu và load được:
- ✅ Tin nhắn văn bản
- ✅ File đính kèm
- ✅ Tin nhắn thoại
- ✅ Vị trí (Location)
- ✅ Phân biệt tin nhắn của bạn và người khác

## 📁 Cấu Trúc File

### Thư Mục Lưu Trữ
```
Chat_DACS4/
└── chat_history/           ← Thư mục chứa lịch sử chat
    ├── 0123456789_0987654321.txt
    ├── 0123456789_Group1.txt
    └── ...
```

### Tên File
- Với chat cá nhân: `username1_username2.txt`
- Với chat nhóm: `username_groupname.txt`
- File được sắp xếp theo thứ tự alphabet để đồng nhất

### Định Dạng Lưu Trữ
Mỗi tin nhắn được lưu dưới dạng JSON một dòng:
```json
{"sender":"0123456789","content":"Xin chào!","timestamp":"2025-12-30 14:30:00","type":"TEXT","isMyMessage":true,"filePath":""}
```

## 🔧 Code Đã Thêm

### 1. Class MessageStorage
**File:** `src/UI_ChatClient/model/MessageStorage.java`

**Chức năng:**
- `saveMessage()` - Lưu một tin nhắn vào file
- `loadMessages()` - Đọc tất cả tin nhắn từ file
- `clearMessages()` - Xóa lịch sử chat
- `hasStoredMessages()` - Kiểm tra xem có tin nhắn lưu không

**Enum MessageType:**
- `TEXT` - Tin nhắn văn bản
- `FILE` - File đính kèm
- `VOICE` - Tin nhắn thoại
- `LOCATION` - Vị trí
- `SYSTEM` - Tin nhắn hệ thống

### 2. Cập Nhật Client.java

**Khởi tạo:**
```java
private MessageStorage messageStorage;

public Client(String username, String fullName) {
    // ...
    this.messageStorage = new MessageStorage(username);
    // ...
}
```

**Load tin nhắn khi mở chat:**
```java
userList.addListSelectionListener(e -> {
    // ...
    if (!chatPanes.containsKey(chatState.getCurrentChatTarget())) {
        createNewChatTab(chatState.getCurrentChatTarget(), selectedUser.getFullName());
        // Load tin nhắn cũ
        loadMessagesFromStorage(chatState.getCurrentChatTarget());
    }
    // ...
});
```

**Lưu tin nhắn tự động:**
- Khi gửi tin nhắn văn bản → `saveTextMessageToStorage()`
- Khi nhận tin nhắn → `saveTextMessageToStorage()`
- Khi gửi/nhận file → `saveFileToStorage()`
- Khi gửi/nhận voice → `saveVoiceToStorage()`
- Khi gửi/nhận location → `saveLocationToStorage()`

## 🚀 Cách Sử Dụng

### Bước 1: Biên dịch lại code
```bash
cd Massage
javac -encoding UTF-8 -d bin -cp "bin/libs/*" src/**/*.java
```

### Bước 2: Chạy ứng dụng
```bash
cd bin
java -cp ".;libs/*" UI_ChatClient.Client
```

### Bước 3: Test chức năng

1. **Đăng nhập với User 1**
   - Gửi vài tin nhắn cho User 2
   - Gửi file, voice, location

2. **Đóng ứng dụng**
   - Thoát hoàn toàn

3. **Mở lại và đăng nhập User 1**
   - Click vào User 2
   - ✅ Tất cả tin nhắn cũ sẽ hiển thị lại!

4. **Kiểm tra thư mục**
   - Vào thư mục `chat_history/`
   - Sẽ thấy file `user1_user2.txt`

## 📝 Lưu Ý Quan Trọng

### ✅ Ưu Điểm
- ✅ Không cần database, đơn giản
- ✅ Dễ backup (chỉ cần copy thư mục `chat_history/`)
- ✅ Dễ debug (mở file text để xem)
- ✅ Tự động lưu và load

### ⚠️ Giới Hạn
- ⚠️ File có thể lớn nếu chat nhiều (nhưng vẫn OK với hàng nghìn tin nhắn)
- ⚠️ File đính kèm/voice không được copy, chỉ lưu đường dẫn
  - Nếu xóa file trong `client_downloads/`, tin nhắn vẫn hiện nhưng không mở được
- ⚠️ Không đồng bộ giữa các thiết bị (vì lưu local)

### 🔒 Bảo Mật
- File lưu dưới dạng text có thể đọc được
- Nếu cần bảo mật, có thể thêm mã hóa sau

## 🔍 Troubleshooting

### Vấn đề: Tin nhắn không load
**Nguyên nhân:** Có thể file bị lỗi định dạng
**Giải pháp:** Xóa file trong `chat_history/` và chat lại

### Vấn đề: File/Voice không mở được
**Nguyên nhân:** File đã bị xóa khỏi `client_downloads/`
**Giải pháp:** Không xóa thư mục này, hoặc backup định kỳ

### Vấn đề: Tin nhắn bị trùng
**Nguyên nhân:** Lỗi code hoặc load nhiều lần
**Giải pháp:** Kiểm tra lại logic load tin nhắn

## 🎨 Tùy Chỉnh

### Thay đổi thư mục lưu trữ
Sửa trong `MessageStorage.java`:
```java
private static final String STORAGE_DIR = "chat_history";  // Đổi tên khác
```

### Xóa lịch sử chat
```java
messageStorage.clearMessages(chatTarget);  // Xóa chat với 1 người
```

### Thêm mã hóa
Có thể modify các hàm `messageToJson()` và `jsonToMessage()` để thêm mã hóa/giải mã.

## 📊 So Sánh Với Database

| Tiêu Chí | File Storage | Database |
|----------|--------------|----------|
| Độ phức tạp | ⭐ Đơn giản | ⭐⭐⭐ Phức tạp |
| Tốc độ | ⭐⭐⭐ Nhanh với ít dữ liệu | ⭐⭐⭐⭐ Nhanh với nhiều dữ liệu |
| Backup | ⭐⭐⭐⭐ Rất dễ | ⭐⭐ Cần export |
| Tìm kiếm | ⭐ Chậm | ⭐⭐⭐⭐ Nhanh |
| Đa người dùng | ⭐ Khó | ⭐⭐⭐⭐ Dễ |

## 🎓 Kết Luận

Giải pháp này phù hợp cho:
- ✅ Đồ án học tập
- ✅ Ứng dụng chat đơn giản
- ✅ Số lượng tin nhắn vừa phải (< 10,000 tin/chat)
- ✅ Không cần tìm kiếm phức tạp

Nếu cần mở rộng thêm, có thể:
- Thêm tìm kiếm tin nhắn
- Thêm mã hóa
- Thêm nén file (zip)
- Chuyển sang SQLite (database nhẹ)

---

**Tác giả:** GitHub Copilot  
**Ngày:** 30/12/2025  
**Version:** 1.0
