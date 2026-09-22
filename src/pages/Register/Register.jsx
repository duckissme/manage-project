import React, { useState } from "react";
import { Form, Input, Button, Typography, Alert } from "antd";
import { UserOutlined, MailOutlined, LockOutlined } from "@ant-design/icons";
import { Link, useNavigate } from "react-router-dom";
import authApi from "../../api/authApi";
import "../Login/Login.scss"; // dùng chung style căn giữa dạng card với Login

const { Title, Text } = Typography;

export default function Register() {
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);
  const navigate = useNavigate();

  const onFinish = async (values) => {
    setErrorMsg(null);
    setLoading(true);
    try {
      await authApi.register({
        name: values.name,
        email: values.email,
        password: values.password,
      });

      navigate("/login"); // đăng ký xong quay lại trang Login
    } catch (err) {
      setErrorMsg(err?.message || "Đăng ký thất bại. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-card__header">
          <Title level={3}>Tạo tài khoản</Title>
          <img src="https://images.scalebranding.com/chat-bubble-doberman-dog-logo.png-01KWS3GQW03HX2KZX616AA1WPJ-thumbnail.png" alt="Logo" className="auth-card__logo" />
        </div>

        {errorMsg && (
          <Alert type="error" message={errorMsg} showIcon className="auth-card__alert" />
        )}

        <Form layout="vertical" onFinish={onFinish} requiredMark={false}>
          <Form.Item
            name="name"
            label="Họ tên"
            rules={[{ required: true, message: "Vui lòng nhập họ tên" }]}
          >
            <Input prefix={<UserOutlined />} placeholder="Nguyễn Văn A" size="large" />
          </Form.Item>

          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: "Vui lòng nhập email" },
              { type: "email", message: "Email không hợp lệ" },
            ]}
          >
            <Input prefix={<MailOutlined />} placeholder="you@example.com" size="large" />
          </Form.Item>

          <Form.Item
            name="password"
            label="Mật khẩu"
            rules={[
              { required: true, message: "Vui lòng nhập mật khẩu" },
              { min: 6, message: "Mật khẩu tối thiểu 6 ký tự" },
            ]}
            hasFeedback
          >
            <Input.Password prefix={<LockOutlined />} placeholder="••••••••" size="large" />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="Xác nhận mật khẩu"
            dependencies={["password"]}
            hasFeedback
            rules={[
              { required: true, message: "Vui lòng xác nhận mật khẩu" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue("password") === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error("Mật khẩu xác nhận không khớp"));
                },
              }),
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="••••••••" size="large" />
          </Form.Item>

          <Form.Item className="auth-card__submit">
            <Button type="primary" htmlType="submit" size="large" block loading={loading}>
              Đăng ký
            </Button>
          </Form.Item>
        </Form>

        <div className="auth-card__footer">
          <Text type="secondary">Đã có tài khoản? </Text>
          <Link to="/login">Đăng nhập</Link>
        </div>
      </div>
    </div>
  );
}
