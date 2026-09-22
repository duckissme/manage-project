import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login/Login.jsx";
import Register from "./pages/Register/Register.jsx";
import ProjectList from "./pages/ProjectList/ProjectList.jsx";
import PrivateRoute from "./components/PrivateRoute.jsx";

// Sơ đồ route hiện tại (sẽ mở rộng dần theo từng module trong Gantt):
//   /login      -> module 1 (Auth) - task 1.6
//   /register   -> module 1 (Auth) - task 1.6
//   /           -> Dashboard tạm, sau thay bằng Project List (task 2.5)
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route
        path="/"
        element={
          <PrivateRoute>
            <ProjectList />
          </PrivateRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
