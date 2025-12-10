# Tính năng Tùy chọn Nhóm

## Mô tả
Đã thêm tính năng tùy chọn nhóm với giao diện hiện đại và đẹp mắt cho ứng dụng chat. Khi người dùng nhấn vào nút **caution** (⚠️) trong phần chat nhóm, sẽ hiển thị menu popup với 2 lựa chọn:

### 1. Xem thành viên nhóm 👤
- Hiển thị danh sách đầy đủ tất cả thành viên trong nhóm
- Giao diện hiện đại với:
  - Avatar gradient cho mỗi thành viên
  - Số thứ tự hiển thị trong avatar
  - Trạng thái online/offline (đang hoạt động)
  - Cuộn được nếu có nhiều thành viên

### 2. Rời khỏi nhóm 🚪
- Cho phép người dùng rời khỏi nhóm đang tham gia
- Hiển thị hộp thoại xác nhận trước khi rời nhóm
- Tự động:
  - Gửi yêu cầu rời nhóm tới server
  - Xóa tab chat của nhóm
  - Xóa nhóm khỏi danh sách người dùng
  - Chuyển về màn hình welcome

## Thiết kế UI

### GroupOptionsDialog
- **Thiết kế**: Menu popup hiện đại với hiệu ứng đổ bóng
- **Màu sắc**: Gradient xanh ngọc (teal) và trắng
- **Hiệu ứng**: Hover effect khi di chuột qua các tùy chọn
- **Biểu tượng**: Emoji sinh động (👤, 🚪)
- **Layout**: 
  - Header với icon và tên nhóm
  - 2 tùy chọn với mô tả chi tiết
  - Màu sắc phân biệt (xanh cho xem thành viên, đỏ cho rời nhóm)

### ViewGroupMembersDialog
- **Thiết kế**: Dialog hiển thị danh sách với scroll
- **Màu sắc**: Gradient xanh ngọc và trắng
- **Thành phần**:
  - Header với tiêu đề và tên nhóm
  - Danh sách thành viên với avatar gradient
  - Indicator trạng thái online (màu xanh lá)
  - Nút đóng với gradient

## Files đã thay đổi

### Files mới tạo:
1. `src/UI_ChatClient/view/dialogs/GroupOptionsDialog.java`
   - Dialog menu tùy chọn nhóm

2. `src/UI_ChatClient/view/dialogs/ViewGroupMembersDialog.java`
   - Dialog hiển thị danh sách thành viên

### Files đã chỉnh sửa:
1. `src/UI_ChatClient/Client.java`
   - Thêm biến instance `btnCaution`
   - Thêm action listener cho nút caution
   - Thêm phương thức `showGroupOptionsDialog()`
   - Thêm phương thức `showViewGroupMembers()`
   - Thêm phương thức `showLeaveGroupConfirmation()`
   - Thêm phương thức `leaveGroup()`
   - Thêm phương thức `getGroupMembers()`

2. `src/UI_ChatClient/controller/NetworkController.java`
   - Thêm phương thức `sendLeaveGroupRequest()`

3. `src/UI_ChatClient/model/Constants.java`
   - Thêm constant `TYPE_LEAVE_GROUP_REQUEST = 37`

## Hướng dẫn sử dụng

1. **Mở chat nhóm**: Chọn một nhóm từ danh sách người dùng
2. **Nhấn nút caution**: Click vào biểu tượng ⚠️ ở góc trên bên phải
3. **Chọn tùy chọn**:
   - **Xem thành viên nhóm**: Xem danh sách tất cả thành viên
   - **Rời khỏi nhóm**: Xác nhận và rời khỏi nhóm

## Lưu ý cho Developer

### TODO - Cần triển khai thêm:
1. **Server-side**:
   - Xử lý request `TYPE_LEAVE_GROUP_REQUEST` trong `ClientHandler.java`
   - Cập nhật danh sách thành viên trong `ChatServerCore.java`
   - Broadcast thông báo khi có người rời nhóm

2. **Client-side**:
   - Triển khai logic lấy danh sách thành viên thực tế từ server
   - Phương thức `getGroupMembers()` hiện tại chỉ trả về dữ liệu mẫu
   - Cần thêm protocol để request và nhận danh sách thành viên

3. **Cải tiến UI**:
   - Có thể thêm chức năng tìm kiếm thành viên
   - Thêm role/quyền (admin, member)
   - Hiển thị avatar thật từ database
   - Thêm context menu cho từng thành viên (kick, promote, etc.)

## Màn hình Preview

### GroupOptionsDialog
```
┌─────────────────────────────────┐
│  👥  Tùy chọn nhóm              │
│      Tên Nhóm ABC               │
├─────────────────────────────────┤
│  [👤] Xem thành viên nhóm       │
│      Xem danh sách tất cả...    │
│                                 │
│  [🚪] Rời khỏi nhóm             │
│      Bạn sẽ không thể nhận...   │
└─────────────────────────────────┘
```

### ViewGroupMembersDialog
```
┌─────────────────────────────────┐
│  👥  Thành viên nhóm            │
│      Tên Nhóm ABC               │
├─────────────────────────────────┤
│  ┌─────────────────────────┐   │
│  │ [1] Nguyễn Văn A      ● │   │
│  │ [2] Trần Thị B        ● │   │
│  │ [3] Lê Văn C          ● │   │
│  └─────────────────────────┘   │
│                                 │
│         [    Đóng    ]          │
└─────────────────────────────────┘
```

## Palette màu sử dụng

- **Primary**: `rgb(94, 234, 212)` - Xanh ngọc chính
- **Secondary**: `rgb(45, 212, 191)` - Xanh ngọc đậm
- **Text**: `rgb(19, 78, 74)` - Xanh đen
- **Danger**: `rgb(239, 68, 68)` - Đỏ cảnh báo
- **Online**: `rgb(34, 197, 94)` - Xanh lá online
- **Background**: `rgb(249, 250, 251)` - Xám trắng nhạt

## Version History
- **v1.0** (2025-12-10): Phiên bản đầu tiên với tính năng xem thành viên và rời nhóm
