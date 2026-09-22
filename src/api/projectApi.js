import axiosClient from "./axiosClient";

// ⚠️ Endpoint GIẢ ĐỊNH theo mô tả task 2.4 "Xây dựng API CRUD Project (tạo, sửa,
// xóa, thêm/xóa thành viên)". Cần đối chiếu lại projectController thật bên BE.

const projectApi = {
    // GET /projects
    getAll: (params) => axiosClient.get("/projects", { params }),

    // GET /projects/:id
    getById: (id) => axiosClient.get(`/projects/${id}`),

    // POST /projects   body: { name, description }
    create: (payload) => axiosClient.post("/projects", payload),

    // PUT /projects/:id   body: { name, description }
    update: (id, payload) => axiosClient.put(`/projects/${id}`, payload),

    // DELETE /projects/:id
    remove: (id) => axiosClient.delete(`/projects/${id}`),

    // POST /projects/:id/members   body: { userId }
    addMember: (projectId, userId) =>
        axiosClient.post(`/projects/${projectId}/members`, { userId }),

    // DELETE /projects/:id/members/:userId
    removeMember: (projectId, userId) =>
        axiosClient.delete(`/projects/${projectId}/members/${userId}`),
};

export default projectApi;