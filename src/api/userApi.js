import axiosClient from "./axiosClient";

// ⚠️ WBS chưa mô tả rõ API danh sách User, endpoint dưới đây là GIẢ ĐỊNH để
// phục vụ ô chọn thành viên trong ProjectForm. Cần hỏi lại BE có sẵn API này
// chưa (thường đi kèm module Auth/RBAC).

const userApi = {
    // GET /users?search=...
    search: (keyword) => axiosClient.get("/users", { params: { search: keyword } }),
};

export default userApi;