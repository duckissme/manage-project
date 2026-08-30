# Module: Issue (Task / User Story)

Module này chịu trách nhiệm quản lý các đơn vị công việc lõi của hệ thống (Task, Bug, User Story), bao gồm thông tin chi tiết, trạng thái, người thực hiện và tự động theo dõi lịch sử thay đổi (Audit Log).

## 1. Kiến trúc dữ liệu (Entities)
* **`Issue`**: Lưu trữ thông tin cốt lõi của công việc (Title, Type, Status, Priority, Story Point, Assignee, Reporter, Due Date). Quản lý xóa bằng Soft Delete (`is_deleted`) và chống ghi đè dữ liệu bằng Optimistic Locking (`version`).
* **`ProjectSequence`**: Bảng phụ trợ lưu bộ đếm (`current_value`) cho từng dự án. Đảm nhiệm việc sinh mã Issue tự động và duy nhất (Ví dụ: ECO-1, ECO-2) thông qua cơ chế Pessimistic Locking.
* **`IssueHistory`**: Lưu vết mọi thay đổi của Issue theo định dạng Key-Value (Trường bị đổi, Giá trị cũ, Giá trị mới). Hỗ trợ truy vết ai đã sửa trường nào và vào thời điểm nào.

## 2. Quy tắc nghiệp vụ (Business Rules)
* **Tạo Issue mới**: Bắt buộc phải thuộc về một Project. Mã `issue_key` được tự động cấp phát tuyến tính. Mặc định Issue mới tạo sẽ có trạng thái là `TO_DO` và chưa nằm trong Sprint nào (thuộc Backlog).
* **Cập nhật & Lưu vết (Audit Log)**: Khi cập nhật, hệ thống tự động so sánh (diff) dữ liệu mới và cũ. Nếu có sự thay đổi, hệ thống sử dụng Event-Driven (chạy ngầm bất đồng bộ) để lưu tự động vào bảng `IssueHistory`. Bắt chặt lỗi xung đột dữ liệu nếu có 2 người cùng lưu 1 lúc.
* **Xóa Issue**: Thực hiện thao tác Xóa Mềm. Issue bị xóa sẽ tự động bị ẩn khỏi toàn bộ các truy vấn thông thường của hệ thống.
* **Chuẩn hóa dữ liệu**: Trạng thái (Status), Loại (Type), và Mức độ ưu tiên (Priority) được bắt buộc chuẩn hóa bằng Enum để tránh lỗi dữ liệu rác.

## 3. Danh sách API Endpoints

### Issue API
* `POST   /api/v1/projects/{projectId}/issues` - Tạo mới một Issue (Tự động sinh mã issueKey).
* `GET    /api/v1/issues/{id}` - Xem chi tiết thông tin của một Issue.
* `PUT    /api/v1/issues/{id}` - Cập nhật thông tin Issue (Hệ thống tự đối chiếu và ghi log thay đổi).
* `DELETE /api/v1/issues/{id}` - Xóa Issue (Soft Delete).