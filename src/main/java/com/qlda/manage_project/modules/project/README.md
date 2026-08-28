# 📁 Module: Project 

Module này chịu trách nhiệm quản lý vòng đời của dự án (CRUD) và phân quyền nhân sự cấp độ dự án (Project-level RBAC) trong hệ thống.

## 1. Kiến trúc dữ liệu (Entities)
* **`Project`**: Lưu trữ thông tin lõi (Tên, mô tả, trạng thái, ngày bắt đầu, deadline). Quản lý việc xóa dự án bằng cơ chế Soft Delete (`is_deleted`).
* **`ProjectMember`**: Bảng trung gian nối `Project` và `User`. Quản lý vai trò (Role) của từng cá nhân trong dự án cụ thể (OWNER, MANAGER, MEMBER, VIEWER). Hỗ trợ cơ chế Soft Delete để lưu lại lịch sử nhân sự.

## 2. Quy tắc nghiệp vụ (Business Rules - RBAC)
* **Tạo dự án**: Mọi user đã xác thực (Authenticated) đều có thể tạo. Hệ thống tự động gán user đó làm `OWNER` của dự án mới.
* **Xem chi tiết dự án**: User bắt buộc phải là thành viên hợp lệ (đang active) của dự án.
* **Cập nhật thông tin / Xóa dự án**: Chỉ `OWNER` hoặc `MANAGER` của dự án đó mới có quyền thao tác. Xóa Project là thao tác Xóa Mềm.
* **Thêm / Gỡ (Kick) thành viên**: Chỉ `OWNER` hoặc `MANAGER` mới được phép mời hoặc gỡ người khác. Chặn logic: Không được phép gỡ `OWNER` ra khỏi dự án.

## 3. Danh sách API Endpoints

### Project API
* `POST   /api/v1/projects` - Tạo dự án mới.
* `GET    /api/v1/projects` - Lấy danh sách các dự án mà user hiện tại đang tham gia.
* `GET    /api/v1/projects/{id}` - Xem chi tiết dự án (bao gồm toàn bộ danh sách thành viên đang active).
* `PUT    /api/v1/projects/{id}` - Cập nhật thông tin dự án.
* `DELETE /api/v1/projects/{id}` - Xóa dự án (Soft Delete).

### Project Member API
* `POST   /api/v1/projects/{projectId}/members` - Thêm một thành viên mới vào dự án kèm role chỉ định.
* `DELETE /api/v1/projects/{projectId}/members/{userId}` - Gỡ một thành viên khỏi dự án (Soft Delete).