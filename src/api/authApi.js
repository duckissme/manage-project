import axiosClient from "./axiosClient";

// ⚠️ Các endpoint dưới đây là GIẢ ĐỊNH theo mô tả task 1.4 "Xây dựng API Đăng ký,
// Đăng nhập, xác thực JWT/Cookie" trong file Gantt. Cần đối chiếu lại với
// authController thật trong repo manage-project (Đức) trước khi dùng, đặc biệt là:
//   - path chính xác (/auth/login hay /users/login ...)
//   - tên field trong body & response (email hay username, accessToken hay token...)

const authApi = {
  // POST /auth/register
  register: (payload) => {
    // payload: { name, email, password }
    return axiosClient.post("/auth/register", payload);
  },

  // POST /auth/login
  login: (payload) => {
    // payload: { email, password }
    // Response giả định: { user: { id, name, email, role }, accessToken? }
    return axiosClient.post("/auth/login", payload);
  },

  // POST /auth/logout
  logout: () => {
    return axiosClient.post("/auth/logout");
  },

  // GET /auth/me  (lấy thông tin user hiện tại từ Cookie/Token)
  getCurrentUser: () => {
    return axiosClient.get("/auth/me");
  },
};

export default authApi;
