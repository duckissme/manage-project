import axios from "axios";

// TODO: đổi baseURL cho khớp với domain/port thật của BE (repo manage-project)
const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

const axiosClient = axios.create({
  baseURL: BASE_URL,
  withCredentials: true, // BẮT BUỘC nếu BE trả JWT qua HttpOnly Cookie (theo task 1.4 trong WBS)
  headers: {
    "Content-Type": "application/json",
  },
});

// Nếu BE trả JWT trong body thay vì Cookie, gắn Authorization header ở đây
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Xử lý lỗi tập trung (401 -> logout, 403 -> không đủ quyền theo RBAC...)
axiosClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("accessToken");
      // window.location.href = "/login";
    }
    return Promise.reject(error.response?.data || error);
  }
);

export default axiosClient;
