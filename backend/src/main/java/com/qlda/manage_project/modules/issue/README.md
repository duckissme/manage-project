# Module: Issue (Task / User Story / Epic / Subtask)

Module này chịu trách nhiệm quản lý các đơn vị công việc lõi của hệ thống (Epic, Story, Task, Bug, Subtask), bao gồm phân cấp cha - con (Parent - Child), thông tin chi tiết, trạng thái, người thực hiện, di chuyển giữa Backlog và Sprint, cùng cơ chế tự động ghi nhận lịch sử thay đổi (Audit Log).

## 1. Kiến trúc dữ liệu (Entities)
* **`Issue`**: Thực thể trung tâm lưu trữ thông tin công việc:
  * **Thông tin cơ bản**: `title`, `description`, `issueType` (`EPIC`, `USER_STORY`, `TASK`, `BUG`, `SUB_TASK`), `status` (`TO_DO`, `IN_PROGRESS`, `DONE`), `priority` (`LOW`, `MEDIUM`, `HIGH`, `URGENT`), `storyPoint`, `dueDate`.
  * **Phân cấp công việc (Parent - Child)**: Thuộc tính `parent_id` tự tham chiếu tới chính bảng `Issue`, hỗ trợ mô hình phân cấp Jira:
    * **Epic**: Issue cấp cao nhất, có thể làm cha của Story, Task, Bug.
    * **Standard Issue (Story / Task / Bug)**: Issue tiêu chuẩn, có thể gán vào Epic hoặc làm cha của các Subtask.
    * **Subtask**: Nhiệm vụ con trực thuộc một Standard Issue.
  * **Liên kết**: Thuộc về một `Project`, có thể được gán vào `Sprint` hoặc nằm ở Backlog (`sprint == null`), gắn với `assigneeId` (người thực hiện) và `reporterId` (người tạo).
  * **Toàn vẹn & An toàn dữ liệu**: Hỗ trợ Xóa mềm (`is_deleted`) và Chống xung đột ghi đè đồng thời bằng Optimistic Locking (`version`).
* **`ProjectSequence`**: Quản lý bộ đếm tự tăng (`current_value`) cho từng dự án, sinh mã `issue_key` duy nhất và liên tục (Ví dụ: `PROJ-1`, `PROJ-2`).
* **`IssueHistory`**: Lưu vết kiểm toán (Audit Log) theo cặp Giá trị cũ - Giá trị mới (`field_name`, `old_value`, `new_value`, `actor_id`) cho mọi biến động của Issue.

## 2. Quy tắc nghiệp vụ (Business Rules)
* **Tạo Issue (Đơn lẻ & Hàng loạt - Bulk Create)**:
  * Mỗi Issue bắt buộc phải thuộc về một Project hợp lệ.
  * Mã `issueKey` được cấp phát tuần tự thông qua `ProjectSequence`.
  * Mặc định khi tạo mới: trạng thái là `TO_DO`, mức độ ưu tiên mặc định là `MEDIUM`, loại công việc mặc định là `USER_STORY`, và chưa được gán vào Sprint nào (thuộc Backlog).
  * Cho phép truyền `parentId` để thiết lập quan hệ phân cấp ngay lúc tạo (Issue cha phải thuộc cùng Project và chưa bị xóa).
  * Hỗ trợ tạo đồng thời danh sách nhiều Issue (`createBulk`) trong cùng một giao dịch.
* **Quản lý Issue con (Child Issues / Subtasks)**:
  * Cho phép truy vấn toàn bộ danh sách Issue con của một Issue cha (`GET /issues/{issueId}/children`).
  * Cho phép tạo nhanh hàng loạt Subtask/Child Issue cho một Issue cha (`POST /issues/{issueId}/children`), hệ thống tự động gán `parentId` và kế thừa `projectId`.
  * Ràng buộc tính toàn vẹn: Issue không được phép tự làm cha của chính nó (`parentId != id`).
* **Cập nhật & Audit Log (Event-Driven)**:
  * Khi cập nhật thông tin (`PUT /issues/{issueId}`), hệ thống tự động so sánh chi tiết từng trường dữ liệu (Title, Status, Type, Priority, Description, Assignee, Story Point, Due Date, Parent).
  * Sử dụng cơ chế Event-Driven (`IssueUpdatedEvent` thông qua Spring `ApplicationEventPublisher`) để tự động ghi log vào bảng `IssueHistory` mà không làm gián đoạn luồng xử lý chính.
* **Xóa Issue (Soft Delete & Phân quyền)**:
  * Thao tác xóa thực hiện Xóa mềm (`is_deleted = true`).
  * **Phân quyền chặt chẽ**: Chỉ thành viên có vai trò `OWNER` hoặc `MANAGER` trong dự án mới có quyền xóa Issue.
  * Tích hợp kiểm tra Optimistic Locking (`softDeleteByIdAndVersion`), phát hiện và chặn kịp thời xung đột nếu Issue đang bị chỉnh sửa đồng thời.
  * Tự động phát sinh sự kiện ghi vết hành động xóa vào `IssueHistory`.
* **Di chuyển Issue (Move Issue - Backlog <-> Sprint)**:
  * Cho phép kéo thả hoặc di chuyển Issue giữa Backlog và Sprint, hoặc giữa hai Sprint khác nhau.
  * Bắt buộc kiểm tra phiên bản dữ liệu (`version`) của Issue để chống xung đột thao tác trên giao diện.
  * Ràng buộc nghiệp vụ: Không cho phép di chuyển Issue vào Sprint đã kết thúc (`COMPLETED`).
  * Tự động ghi nhận log thay đổi Sprint (ví dụ: chuyển từ Backlog sang "Sprint 1", hoặc ngược lại).
* **Tra cứu & Lọc Issue theo dự án**:
  * Hỗ trợ lấy toàn bộ danh sách Issue của dự án, có thể lọc nhanh theo loại công việc (`type`, ví dụ: chỉ lấy danh sách `EPIC` để phục vụ dropdown gán Epic).

## 3. Danh sách API Endpoints

### 1. Quản lý Issue (`IssueController`)
| Phương thức | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `POST` | `/projects/{projectId}/issues` | Tạo mới một Issue (Tự sinh mã `issueKey`, hỗ trợ gán `parentId`). |
| `POST` | `/projects/{projectId}/issues/bulk` | Tạo hàng loạt nhiều Issue cùng lúc cho dự án. |
| `GET` | `/projects/{projectId}/issues` | Lấy danh sách Issue trong dự án (Hỗ trợ Query Param `type` để lọc theo `IssueType`). |
| `GET` | `/issues/{issueId}` | Xem thông tin chi tiết một Issue. |
| `PUT` | `/issues/{issueId}` | Cập nhật thông tin Issue (Tự động so sánh thay đổi và ghi Audit Log). |
| `DELETE` | `/issues/{issueId}` | Xóa mềm Issue (Yêu cầu quyền `OWNER` hoặc `MANAGER`, kiểm tra `@Version`). |
| `PATCH` | `/projects/{projectId}/issues/{issueId}/move` | Di chuyển Issue giữa Backlog và Sprint (Kiểm tra `version`, chặn Sprint `COMPLETED`). |

### 2. Quản lý Issue con / Subtask (`ChildIssueController`)
| Phương thức | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/issues/{issueId}/children` | Lấy danh sách toàn bộ các Issue con (Subtask/Child Issue) của một Issue cha. |
| `POST` | `/issues/{issueId}/children` | Tạo nhanh hàng loạt Issue con trực thuộc Issue cha chỉ định. |