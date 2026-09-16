# Module: Backlog

Module này chịu trách nhiệm quản lý danh sách các công việc tồn đọng (Product Backlog) chưa được phân bổ vào bất kỳ Sprint nào trong phạm vi từng dự án. Cung cấp cơ chế tìm kiếm, lọc linh hoạt phục vụ việc lên kế hoạch và phân bổ công việc (Sprint Planning).

## 1. Kiến trúc dữ liệu (Entities & Specifications)
* **`Issue`**: Module Backlog hoạt động như một góc nhìn nghiệp vụ (Logical View) dựa trên thực thể `Issue`. Các bản ghi thuộc Backlog được xác định qua điều kiện `sprint_id IS NULL`, `project_id = :projectId` và `is_deleted = false`.
* **`IssueSpecification`**: Bộ định nghĩa điều kiện truy vấn động sử dụng `JpaSpecificationExecutor`. Cho phép sinh mã SQL tối ưu theo các tham số lọc truyền vào mà không phát sinh mệnh đề thừa trong cơ sở dữ liệu.
* **`IssueBacklogResponse`**: DTO chuẩn đại diện cho thông tin tóm tắt của một Issue trong danh sách Backlog (ID, Issue Key, Title, Type, Status, Priority, Created At).

## 2. Quy tắc nghiệp vụ (Business Rules)
* **Xác định công việc thuộc Backlog**: Một Issue được coi là nằm trong Backlog khi và chỉ khi chưa được gán vào Sprint nào (`sprint == null`), thuộc về một dự án hợp lệ và chưa bị xóa mềm (`is_deleted = false`).
* **Tìm kiếm & Bộ lọc động (Filter & Search)**: Hỗ trợ linh hoạt 4 tiêu chí tìm kiếm tùy chọn:
  * Lọc theo Loại công việc (`issueType`: STORY, TASK, BUG,...).
  * Lọc theo Người được phân công (`assigneeId`).
  * Lọc theo Trạng thái (`issueStatus`: TO_DO, IN_PROGRESS, DONE,...).
  * Tìm kiếm tương đối (`searchKeyword`): Khớp chuỗi không phân biệt hoa thường (`LIKE %keyword%`) trên cả tiêu đề (`title`) và mô tả (`description`) của Issue.
* **Sắp xếp mặc định**: Các công việc trong Backlog luôn được sắp xếp theo thứ tự thời gian tạo giảm dần (`createdAt DESC`), ưu tiên hiển thị công việc mới tạo lên trên đầu.
* **Toàn vẹn dữ liệu & Kiểm tra dự án**: Luôn kiểm tra sự tồn tại và tính hợp lệ của Project (`filter(!isDeleted)`) trước khi truy vấn danh sách Backlog. Nếu không tồn tại sẽ ném `ProjectNotFoundException`.

## 3. Danh sách API Endpoints

### Backlog API
* `GET /api/v1/projects/{projectId}/backlog` - Lấy danh sách Issue trong Backlog của dự án, hỗ trợ các query params tùy chọn: `issueType`, `assigneeId`, `issueStatus`, `searchKeyword`.
