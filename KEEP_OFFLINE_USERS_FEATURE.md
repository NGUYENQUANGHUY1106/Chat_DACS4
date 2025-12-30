# Tính năng giữ người dùng offline trong danh sách chat

## Mô tả
Khi 2 người đang trò chuyện với nhau mà 1 người offline, người còn lại vẫn sẽ thấy được người đó trong danh sách chat với trạng thái offline (chấm màu xám) thay vì bị mất khỏi danh sách.

## Các thay đổi đã thực hiện

### 1. ChatState.java
**File:** `Massage/src/UI_ChatClient/model/ChatState.java`

Thêm lưu trữ lịch sử chat:
- Thêm `Set<String> chatHistory` để lưu username của những người đã từng chat
- Thêm các method:
  - `getChatHistory()` - lấy danh sách lịch sử chat
  - `addToChatHistory(String username)` - thêm user vào lịch sử
  - `hasChatHistoryWith(String username)` - kiểm tra đã chat chưa

### 2. Client.java
**File:** `Massage/src/UI_ChatClient/Client.java`

#### a. Lưu lịch sử chat khi chọn user:
- Khi user click vào một người trong danh sách để chat, tự động lưu vào `chatHistory`
- Chỉ lưu user cá nhân, không lưu group

#### b. Lưu lịch sử chat khi nhận tin nhắn:
- Khi nhận tin nhắn từ người khác, tự động lưu người gửi vào `chatHistory`

#### c. Cập nhật logic `handleUserListUpdate()`:
- Không còn xóa hoàn toàn danh sách user
- Sau khi thêm user online từ server:
  - Duyệt qua `chatHistory`
  - Với mỗi user đã từng chat nhưng không có trong danh sách online:
    - Lấy thông tin từ database
    - Thêm vào danh sách với trạng thái `isOnline = false`

#### d. Thêm method `getFullNameFromDB()`:
- Query database để lấy tên đầy đủ của user
- Dùng để hiển thị thông tin của user offline

### 3. Hiển thị trạng thái
**File:** `Massage/src/UI_ChatClient/view/components/UserListCellRenderer.java` (đã có sẵn)

Renderer đã hỗ trợ hiển thị trạng thái:
- User online: Chấm xanh lá + text "Đang hoạt động"
- User offline: Chấm xám + text "Không hoạt động"

**File:** `Massage/src/UI_ChatClient/view/components/StatusIconPanel.java` (đã có sẵn)

Vẽ chấm trạng thái:
- `isOnline = true`: Chấm xanh lá với hiệu ứng glow
- `isOnline = false`: Chấm xám

## Cách hoạt động

### Kịch bản 1: User A và User B đang chat
1. User A chọn User B trong danh sách → User B được lưu vào `chatHistory` của User A
2. User B gửi tin nhắn cho User A → User B được lưu vào `chatHistory` của User A
3. User B offline → Server gửi cập nhật danh sách
4. Client của User A:
   - Nhận danh sách user online (không có User B)
   - Kiểm tra `chatHistory`, thấy User B đã từng chat
   - Lấy thông tin User B từ database
   - Thêm User B vào danh sách với `isOnline = false`
5. User A vẫn thấy User B trong danh sách với chấm xám và text "Không hoạt động"

### Kịch bản 2: User B online trở lại
1. User B kết nối lại server
2. Server gửi cập nhật danh sách user online (có User B với `isOnline = true`)
3. Client của User A cập nhật:
   - User B trong danh sách được cập nhật thành `isOnline = true`
   - Chấm chuyển từ xám sang xanh lá
   - Text đổi thành "Đang hoạt động"

## Lưu ý kỹ thuật

1. **Lưu trữ lịch sử**: Chỉ lưu trong bộ nhớ, không lưu vào database. Khi restart app, lịch sử sẽ bị xóa.

2. **Chỉ lưu user cá nhân**: Không lưu group vào `chatHistory`

3. **Query database**: Mỗi user offline trong lịch sử sẽ cần 2 query:
   - Query avatar: `loadUserAvatarFromDB()`
   - Query fullname: `getFullNameFromDB()`

4. **Performance**: Nếu có nhiều user offline trong lịch sử, có thể tối ưu bằng cách:
   - Cache thông tin user
   - Query batch thay vì query từng user

## Testing

### Test case 1: Giữ user offline
1. Đăng nhập 2 client với 2 user khác nhau
2. User A gửi tin nhắn cho User B
3. User B đóng app (offline)
4. **Kỳ vọng**: User A vẫn thấy User B trong danh sách với chấm xám

### Test case 2: User online trở lại
1. Tiếp tục test case 1
2. User B mở app lại (online)
3. **Kỳ vọng**: 
   - User A thấy User B chuyển sang chấm xanh lá
   - Text đổi thành "Đang hoạt động"

### Test case 3: Restart app
1. User A và User B đã chat với nhau
2. User B offline
3. User A restart app
4. **Kỳ vọng**: User B không còn trong danh sách của User A (do lịch sử chỉ lưu trong RAM)

## Cải tiến trong tương lai

1. **Lưu lịch sử vào file/database**: Để giữ lịch sử sau khi restart
2. **Limit số lượng**: Chỉ giữ N user offline gần nhất
3. **Timestamp**: Lưu thời gian chat cuối cùng để sắp xếp
4. **Auto-remove**: Tự động xóa user offline sau một khoảng thời gian không chat
