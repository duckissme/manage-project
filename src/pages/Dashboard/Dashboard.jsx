import React from "react";
import { Typography, Card } from "antd";

const { Title, Paragraph } = Typography;

export default function Dashboard() {
  return (
    <div style={{ padding: 32 }}>
      <Card>
        <Title level={3}>Chào mừng 👋</Title>
        <Paragraph>
          Đây là trang tạm sau khi đăng nhập thành công. Sẽ thay bằng
          <strong> Danh sách Project</strong> (task 2.5) khi module Quản lý Project
          được triển khai.
        </Paragraph>
      </Card>
    </div>
  );
}
