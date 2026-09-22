import React, { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { Spin } from "antd";
import authApi from "../api/authApi";

// Bọc quanh các route cần đăng nhập, vd: <PrivateRoute><ProjectList /></PrivateRoute>
// Gọi GET /auth/me để xác nhận Cookie/Token còn hợp lệ - cần khớp lại với BE thật
export default function PrivateRoute({ children }) {
  const [checking, setChecking] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    authApi
      .getCurrentUser()
      .then(() => setAuthenticated(true))
      .catch(() => setAuthenticated(false))
      .finally(() => setChecking(false));
  }, []);

  if (checking) {
    return (
      <div style={{ display: "flex", justifyContent: "center", marginTop: 80 }}>
        <Spin size="large" />
      </div>
    );
  }

  return authenticated ? children : <Navigate to="/login" replace />;
}
