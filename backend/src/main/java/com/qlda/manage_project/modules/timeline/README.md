# Module: Timeline (Gantt Chart)

Cung cấp dữ liệu cho trang Timeline kiểu Jira: cây phân cấp Epic ➔ Standard Issue ➔ Subtask, thanh lịch theo `start_date` / `due_date`, mũi tên phụ thuộc (`BLOCKS`) và mốc Sprint.

## 1. Kiến trúc dữ liệu
* Không có bảng riêng, đọc dữ liệu từ `issues` (`start_date`, `due_date`, `parent_id`), `issue_links` (`link_type = BLOCKS`) và `sprints`.
* **`TimelineResponse`**: `issues` (danh sách phẳng, FE dựng cây theo `parentId`), `dependencies`, `sprints`.

## 2. Quy tắc nghiệp vụ
* **Phân quyền xem**: phải là thành viên đang active của dự án.
* **Hiệu năng**: cố định 3 query (issues kèm assignee, links BLOCKS, sprints), không phát sinh N+1.
* **Issue mồ côi**: nếu Issue cha đã bị xóa mềm, `parentId` trả về `null` để Issue con hiển thị ở cấp gốc.
* **Dependency**: chỉ trả link mà cả 2 đầu còn tồn tại. `sourceIssueId` blocks `targetIssueId`.
* **Unscheduled**: Backend trả nguyên giá trị ngày (có thể `null`). FE quyết định cách hiển thị (ẩn thanh bar, hoặc fallback theo ngày của Sprint qua `sprintId`, hoặc roll-up Epic theo các Issue con).
* **Đổi lịch (kéo thả)**:
  * `VIEWER` không có quyền.
  * Bắt buộc `version` (Optimistic Locking); response trả về `version` mới.
  * `startDate` không được sau `dueDate`. Truyền `null` cả 2 để bỏ lịch.
  * Ghi `IssueHistory` qua `IssueUpdatedEvent` (`startDate`, `dueDate`).

## 3. API Endpoints
| Phương thức | Endpoint | Request | Response | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/projects/{projectId}/timeline` | - | `TimelineResponse` | Toàn bộ dữ liệu Timeline của dự án. |
| `PATCH` | `/projects/{projectId}/issues/{issueId}/schedule` | `IssueScheduleRequest` (`startDate`, `dueDate`, `version`) | `TimelineIssueResponse` | Cập nhật lịch khi kéo thả / kéo dãn thanh bar. |
