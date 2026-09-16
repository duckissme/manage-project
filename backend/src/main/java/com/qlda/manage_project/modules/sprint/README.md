# Module: Sprint

Module này chịu trách nhiệm quản lý toàn bộ vòng đời của chu kỳ lặp phát triển (Sprint) theo phương pháp Scrum, bao gồm lập kế hoạch, bắt đầu, theo dõi tiến độ, thống kê điểm khối lượng công việc (Story Points) và hoàn tất / xử lý công việc tồn đọng khi kết thúc Sprint.

## 1. Kiến trúc dữ liệu (Entities & Events)
* **`Sprint`**: Quản lý thông tin cốt lõi của Sprint (Name, Goal, Start Date, End Date, Status). Quản lý trạng thái xóa bằng Soft Delete (`is_deleted`) và tự động lưu vết thời gian (`created_at`, `updated_at`).
* **`SprintStatus`**: Enum chuẩn hóa vòng đời của Sprint gồm 3 trạng thái: `PENDING` (Chờ bắt đầu), `ACTIVE` (Đang chạy), `COMPLETED` (Đã hoàn thành).
* **`SprintStartedEvent`**: Sự kiện bắn ra khi bắt đầu một Sprint (`projectId`, `sprintId`, `sprintName`, `actorId`), phục vụ xử lý các tác vụ bất đồng bộ (Notification, Log).
* **`SprintResponse`**: DTO chuẩn bọc dữ liệu Sprint trả về cho Client, kèm danh sách các Issue con và các chỉ số thống kê số liệu tổng hợp (`totalTasks`, `totalStoryPoints`, `toDoPoints`, `inProgressPoints`, `donePoints`).

## 2. Quy tắc nghiệp vụ (Business Rules)
* **Tự động đặt tên Sprint**: Khi tạo mới nếu không truyền tên (hoặc để trống), hệ thống tự động sinh tên theo định dạng `Sprint {totalSprints + 1}` dựa trên tổng số Sprint hiện có trong Project. Mặc định Sprint mới tạo có trạng thái là `PENDING`.
* **Tối ưu truy vấn N+1 & Thống kê Story Point**: Khi lấy danh sách Sprint (chỉ lấy các Sprint `PENDING` và `ACTIVE`), hệ thống nạp toàn bộ Issue liên quan trong 1 truy vấn duy nhất và dùng Stream `groupingBy` để ghép dữ liệu, đồng thời tự động tổng hợp Story Point theo các nhóm trạng thái (`TO_DO`, `IN_PROGRESS`, `DONE`) nhằm giảm tải tính toán cho Frontend.
* **Bắt đầu Sprint (Start Sprint)**:
  * **Phân quyền**: Chỉ thành viên giữ vai trò `OWNER` hoặc `MANAGER` trong dự án mới có quyền bắt đầu Sprint.
  * **Trạng thái**: Sprint bắt buộc phải đang ở trạng thái `PENDING`.
  * **Quy tắc duy nhất**: Trong cùng một Project, **chỉ được phép có duy nhất 1 Sprint mang trạng thái `ACTIVE`**.
  * **Ràng buộc công việc**: Sprint bắt buộc phải có ít nhất 1 Issue mới cho phép bắt đầu.
  * **Sự kiện**: Bắn `SprintStartedEvent` để phục vụ thông báo và theo dõi.
* **Kết thúc Sprint (Complete Sprint)**:
  * **Phân quyền**: Chỉ dành cho `OWNER` hoặc `MANAGER`.
  * **Trạng thái**: Sprint bắt buộc phải đang ở trạng thái `ACTIVE`.
  * **Phân loại Issue tồn đọng**:
    * **Nhóm 1 (Trạng thái `DONE`)**: Giữ nguyên `sprint_id` để lưu trữ dữ liệu lịch sử và phục vụ báo cáo.
    * **Nhóm 2 (Trạng thái khác `DONE` - `TO_DO`, `IN_PROGRESS`,...)**: Tự động gỡ khỏi Sprint (`sprint_id = null`) đẩy về Backlog, lưu hàng loạt bằng `saveAll()` (bảo toàn Optimistic Locking), đồng thời kích hoạt `IssueUpdatedEvent` để lưu vết lịch sử chuyển về Backlog (`IssueHistory`).
  * Chuyển trạng thái Sprint thành `COMPLETED` và lưu lại.
* **Xóa Sprint (Delete Sprint)**:
  * Thực hiện Xóa Mềm (`is_deleted = true`). Yêu cầu quyền `OWNER` hoặc `MANAGER`.
  * Toàn bộ Issue trong Sprint bị xóa sẽ tự động được giải phóng về Backlog (`sprint_id = null`) và bắn Event lưu lại lịch sử thay đổi `IssueHistory`.

## 3. Danh sách API Endpoints

### Sprint API
* `POST   /api/v1/projects/{projectId}/sprints` - Tạo mới Sprint (Tự động sinh tên nếu để trống).
* `GET    /api/v1/projects/{projectId}/sprints` - Lấy danh sách Sprint (kèm danh sách Issue và các chỉ số thống kê Story Point).
* `PUT    /api/v1/projects/{projectId}/sprints/{sprintId}` - Cập nhật thông tin Sprint (Yêu cầu quyền OWNER/MANAGER).
* `DELETE /api/v1/projects/{projectId}/sprints/{sprintId}` - Xóa Sprint (Soft Delete, đẩy issue về Backlog, yêu cầu quyền OWNER/MANAGER).
* `POST   /api/v1/projects/{projectId}/sprints/{sprintId}/start` - Bắt đầu Sprint (Kiểm tra điều kiện 1 Active duy nhất, yêu cầu quyền OWNER/MANAGER).
* `POST   /api/v1/projects/{projectId}/sprints/{sprintId}/complete` - Kết thúc Sprint (Đẩy các issue chưa hoàn thành về Backlog, yêu cầu quyền OWNER/MANAGER).
