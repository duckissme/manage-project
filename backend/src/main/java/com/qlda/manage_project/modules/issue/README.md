# Module: Issue (Task / User Story / Epic / Subtask / Issue Linking)

Module này chịu trách nhiệm quản lý các đơn vị công việc lõi của hệ thống (Epic, Story, Task, Bug, Subtask), bao gồm phân cấp cha - con (Parent - Child), liên kết đồ thị ngang hàng (Issue Linking), thông tin chi tiết, trạng thái, người thực hiện, di chuyển giữa Backlog và Sprint, cơ chế kiểm soát đồng thời (Optimistic Locking) và tự động ghi nhận lịch sử thay đổi (Audit Log).

## 1. Kiến trúc dữ liệu (Entities & DTOs)
* **`Issue`**: Thực thể trung tâm lưu trữ thông tin công việc:
  * **Thông tin cơ bản**: `title`, `description`, `issueType` (`EPIC`, `USER_STORY`, `TASK`, `BUG`, `SUB_TASK`), `status` (`TO_DO`, `IN_PROGRESS`, `DONE`), `priority` (`LOW`, `MEDIUM`, `HIGH`, `URGENT`), `storyPoint`, `dueDate`.
  * **Phân cấp công việc (Parent - Child)**: Thuộc tính `parent_id` tự tham chiếu tới chính bảng `Issue`, hỗ trợ mô hình phân cấp Jira:
    * **Epic**: Issue cấp cao nhất, có thể làm cha của Story, Task, Bug.
    * **Standard Issue (Story / Task / Bug)**: Issue tiêu chuẩn, có thể gán vào Epic hoặc làm cha của các Subtask.
    * **Subtask**: Nhiệm vụ con trực thuộc một Standard Issue.
  * **Liên kết**: Thuộc về một `Project`, có thể được gán vào `Sprint` hoặc nằm ở Backlog (`sprint == null`), gắn với `assigneeId` (người thực hiện) và `reporterId` (người tạo).
  * **Toàn vẹn & An toàn dữ liệu**: Hỗ trợ Xóa mềm (`is_deleted`) và Chống xung đột ghi đè đồng thời bằng Optimistic Locking (`version`).
* **`IssueLink`**: Bảng trung gian (`issue_links`) lưu trữ mối quan hệ đồ thị ngang hàng (Peer-to-Peer) giữa 2 Issue bất kỳ trong cùng một dự án:
  * `source_issue_id`: Issue khởi tạo liên kết.
  * `target_issue_id`: Issue nhận liên kết.
  * `link_type`: Loại liên kết (`BLOCKS`, `RELATES_TO`, `DUPLICATES`, `CLONES`).
  * `created_by`: Người thiết lập liên kết.
  * `created_at`: Thời điểm tạo liên kết.
* **`IssueLinkType` (Enum)**: Định nghĩa loại liên kết kèm nhãn quan hệ 2 chiều:
  * `BLOCKS`: Outward `"blocks"`, Inward `"is blocked by"`. (Phục vụ vẽ đường mũi tên phụ thuộc trên Timeline / Gantt Chart).
  * `RELATES_TO`: Outward `"relates to"`, Inward `"relates to"`.
  * `DUPLICATES`: Outward `"duplicates"`, Inward `"is duplicated by"`.
  * `CLONES`: Outward `"clones"`, Inward `"is cloned by"`.
* **`IssueSummaryResponse` (DTO Tóm tắt)**: Siêu nhẹ, tối ưu băng thông cho các thao tác danh sách và tạo nhanh:
  * Bao gồm: `id`, `projectId`, `sprintId`, `parentId`, `issueKey`, `issueType`, `title`, `status`, `priority`, `storyPoint`, `assigneeId`, `dueDate`, `createdAt`.
* **`IssueResponse` (DTO Chi tiết)**: Phục vụ màn hình xem chi tiết (Detail Modal/Drawer) và phản hồi sau khi Cập nhật (Update):
  * Chứa toàn bộ thông tin chi tiết: `description`, `reporterId`, `updatedAt`, `parentId`, `version`, và danh sách liên kết `links` (`List<IssueLinkResponse>`).
* **`ProjectSequence`**: Quản lý bộ đếm tự tăng (`current_value`) cho từng dự án, sinh mã `issue_key` duy nhất và liên tục (Ví dụ: `PROJ-1`, `PROJ-2`).
* **`IssueHistory`**: Lưu vết kiểm toán (Audit Log) theo cặp Giá trị cũ - Giá trị mới (`field_name`, `old_value`, `new_value`, `actor_id`) cho mọi biến động của Issue.

## 2. Quy tắc nghiệp vụ (Business Rules)
* **Tạo Issue (Đơn lẻ & Hàng loạt - Bulk Create)**:
  * Mỗi Issue bắt buộc phải thuộc về một Project hợp lệ.
  * Mã `issueKey` được cấp phát tuần tự thông qua `ProjectSequence`.
  * Mặc định khi tạo mới: trạng thái là `TO_DO`, mức độ ưu tiên mặc định là `MEDIUM`, loại công việc mặc định là `USER_STORY`, và chưa được gán vào Sprint nào (thuộc Backlog).
  * Cho phép truyền `parentId` để thiết lập quan hệ phân cấp ngay lúc tạo (Issue cha phải thuộc cùng Project và chưa bị xóa).
  * Hỗ trợ tạo đồng thời danh sách nhiều Issue (`createBulk`) trong cùng một giao dịch. Kết quả trả về danh sách `IssueSummaryResponse`.
* **Quản lý Issue con (Child Issues / Subtasks)**:
  * Cho phép truy vấn toàn bộ danh sách Issue con của một Issue cha (`GET /issues/{issueId}/children`).
  * Cho phép tạo nhanh hàng loạt Subtask/Child Issue cho một Issue cha (`POST /issues/{issueId}/children`), hệ thống tự động gán `parentId` và kế thừa `projectId`. Kết quả trả về `IssueSummaryResponse`.
  * Ràng buộc tính toàn vẹn: Issue không được phép tự làm cha của chính nó (`parentId != id`).
* **Liên kết công việc (Issue Linking)**:
  * **Phạm vi liên kết**: Cả 2 Issue tham gia liên kết phải tồn tại, chưa bị xóa mềm (`is_deleted = false`) và thuộc **cùng một dự án**.
  * **Chặn liên kết không hợp lệ**:
    * Không cho phép tự liên kết với chính mình (`sourceIssueId <> targetIssueId`).
    * Chặn trùng lặp liên kết giữa 2 Issue.
    * **Chặn phụ thuộc vòng lặp (Circular Dependency)** với `BLOCKS`: Nếu Issue B đã `blocks` Issue A, thì hệ thống chặn không cho phép tạo Issue A `blocks` Issue B.
    * Quan hệ đối xứng với `RELATES_TO`: Nếu đã có A `relates to` B thì không cho phép tạo lặp B `relates to` A.
  * **Hiển thị 2 chiều (Bidirectional Mapping)**:
    * Khi xem Issue A: nếu A là nguồn ➔ hiển thị nhãn Outward (vd: *blocks PROJ-2*); nếu A là đích ➔ hiển thị nhãn Inward (vd: *is blocked by PROJ-3*).
  * **Phân quyền xóa link**: Chỉ người tạo liên kết (`created_by`) hoặc thành viên có role `OWNER`/`MANAGER` trong dự án mới có quyền gỡ bỏ liên kết.
  * **Audit Log**: Tự động ghi nhận log vào `IssueHistory` khi tạo hoặc gỡ liên kết.
* **Kiểm soát đồng thời (Optimistic Locking với `version`)**:
  * **Cập nhật (`PUT /issues/{issueId}`)**: Bắt buộc phải gửi kèm trường `version` (`@NotNull`). Hệ thống kiểm tra so khớp `request.version` với `issue.version` trong cơ sở dữ liệu. Nếu phiên bản bị lệch (do người khác đã sửa trước), hệ thống ném `BadRequestException("Dữ liệu đã bị thay đổi bởi người khác, vui lòng làm mới trang.")` để chống lỗi Lost Update.
  * **Di chuyển (`PATCH /projects/{projectId}/issues/{issueId}/move`)**: Bắt buộc gửi `version`. Ngăn chặn kéo thả trên giao diện nếu trạng thái dữ liệu đã cũ.
  * **Xóa (`DELETE /issues/{issueId}`)**: Hỗ trợ nhận Query Param `version` (tùy chọn). Nếu có truyền, hệ thống sẽ xác minh phiên bản trước khi thực hiện xóa mềm để tránh xóa nhầm dữ liệu đang có người khác thao tác.
* **Cập nhật & Audit Log (Event-Driven)**:
  * Khi cập nhật thông tin (`PUT /issues/{issueId}`), hệ thống tự động so sánh chi tiết từng trường dữ liệu (Title, Status, Type, Priority, Description, Assignee, Story Point, Due Date, Parent).
  * Sử dụng cơ chế Event-Driven (`IssueUpdatedEvent` thông qua Spring `ApplicationEventPublisher`) để tự động ghi log vào bảng `IssueHistory` mà không làm gián đoạn luồng xử lý chính.
* **Xóa Issue (Soft Delete & Phân quyền)**:
  * Thao tác xóa thực hiện Xóa mềm (`is_deleted = true`).
  * **Phân quyền chặt chẽ**: Chỉ thành viên có vai trò `OWNER` hoặc `MANAGER` trong dự án mới có quyền xóa Issue.
  * Tích hợp kiểm tra Optimistic Locking (`softDeleteByIdAndVersion`), phát hiện và chặn kịp thời xung đột nếu Issue đang bị chỉnh sửa đồng thời.
* **Di chuyển Issue (Move Issue - Backlog <-> Sprint)**:
  * Cho phép kéo thả hoặc di chuyển Issue giữa Backlog và Sprint, hoặc giữa hai Sprint khác nhau.
  * Ràng buộc nghiệp vụ: Không cho phép di chuyển Issue vào Sprint đã kết thúc (`COMPLETED`).

## 3. Danh sách API Endpoints

### 1. Quản lý Issue (`IssueController`)
| Phương thức | Endpoint | Request Body / Param | Response Body | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/projects/{projectId}/issues` | `IssueCreateRequest` | `IssueSummaryResponse` | Tạo mới một Issue (Tự sinh mã `issueKey`, hỗ trợ gán `parentId`). |
| `POST` | `/projects/{projectId}/issues/bulk` | `List<IssueCreateRequest>` | `List<IssueSummaryResponse>` | Tạo hàng loạt nhiều Issue cùng lúc cho dự án. |
| `GET` | `/projects/{projectId}/issues` | Query Params: `type`, `keyword`, `excludeIssueId`, `limit` (tùy chọn) | `List<IssueSummaryResponse>` | Lấy danh sách Issue trong dự án (Lọc theo loại, tìm kiếm Autocomplete để Link/Add Epic và giới hạn `limit`). |
| `GET` | `/issues/{issueId}` | - | `IssueResponse` | Xem thông tin chi tiết một Issue (Có `description`, `version`, `parentId`, `links`). |
| `PUT` | `/issues/{issueId}` | `IssueUpdateRequest` (Bắt buộc `version`) | `IssueResponse` | Cập nhật thông tin Issue (Kiểm tra `version`, ghi Audit Log). |
| `DELETE` | `/issues/{issueId}` | Query Param `version` (tùy chọn) | `void` (204 No Content) | Xóa mềm Issue (Yêu cầu quyền `OWNER`/`MANAGER`, kiểm tra `version`). |
| `PATCH` | `/projects/{projectId}/issues/{issueId}/move` | `MoveIssueRequest` (Bắt buộc `version`) | `void` (204 No Content) | Di chuyển Issue giữa Backlog và Sprint (Kiểm tra `version`, chặn Sprint `COMPLETED`). |

### 2. Quản lý Issue con / Subtask (`ChildIssueController`)
| Phương thức | Endpoint | Request Body / Param | Response Body | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/issues/{issueId}/children` | - | `List<IssueSummaryResponse>` | Lấy danh sách toàn bộ các Issue con (Subtask/Child Issue) của một Issue cha. |
| `POST` | `/issues/{issueId}/children` | `List<IssueCreateRequest>` | `List<IssueSummaryResponse>` | Tạo nhanh hàng loạt Issue con trực thuộc Issue cha chỉ định. |

### 3. Quản lý Liên kết Issue / Issue Linking (`IssueLinkController`)
| Phương thức | Endpoint | Request Body / Param | Response Body | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/issues/{issueId}/links` | `IssueLinkCreateRequest` | `IssueLinkResponse` | Thiết lập liên kết giữa `issueId` và `targetIssueId` (`BLOCKS`, `RELATES_TO`,...). |
| `GET` | `/issues/{issueId}/links` | - | `List<IssueLinkResponse>` | Lấy toàn bộ danh sách các liên kết 2 chiều (Inward & Outward) của Issue. |
| `DELETE` | `/issues/links/{linkId}` | - | `void` (204 No Content) | Gỡ bỏ liên kết giữa 2 Issue (Yêu cầu là người tạo hoặc `OWNER`/`MANAGER`). |