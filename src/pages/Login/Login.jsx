import React, { useState } from "react";
import { Form, Input, Button, Checkbox, Typography, Alert } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";
import { Link, useNavigate } from "react-router-dom";
import authApi from "../../api/authApi";
import "./Login.scss";

const { Title, Text } = Typography;

export default function Login() {
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);
  const navigate = useNavigate();

  const onFinish = async (values) => {
    setErrorMsg(null);
    setLoading(true);
    try {
      const res = await authApi.login({
        email: values.email,
        password: values.password,
      });

      // Nếu BE trả token trong body thay vì chỉ set Cookie, lưu lại để gắn Authorization header
      if (res?.accessToken) {
        localStorage.setItem("accessToken", res.accessToken);
      }

      navigate("/"); // điều hướng vào dashboard/project list sau khi đăng nhập
    } catch (err) {
      setErrorMsg(err?.message || "Đăng nhập thất bại. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-card__header">
          <Title level={3}>Đăng nhập</Title>
          <img src="https://images.scalebranding.com/chat-bubble-doberman-dog-logo.png-01KWS3GQW03HX2KZX616AA1WPJ-thumbnail.png" alt="Logo" className="auth-card__logo" />
        </div>

        {errorMsg && (
          <Alert
            type="error"
            message={errorMsg}
            showIcon
            className="auth-card__alert"
          />
        )}

        <Form layout="vertical" onFinish={onFinish} requiredMark={false}>
          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: "Vui lòng nhập email" },
              { type: "email", message: "Email không hợp lệ" },
            ]}
          >
            <Input prefix={<UserOutlined />} placeholder="you@example.com" size="large" />
          </Form.Item>

          <Form.Item
            name="password"
            label="Mật khẩu"
            rules={[{ required: true, message: "Vui lòng nhập mật khẩu" }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="••••••••" size="large" />
          </Form.Item>

          <div className="auth-card__row">
            <Form.Item name="remember" valuePropName="checked" noStyle>
              <Checkbox>Ghi nhớ đăng nhập</Checkbox>
            </Form.Item>
            <Link to="/forgot-password">Quên mật khẩu?</Link>
          </div>

          <Form.Item className="auth-card__submit">
            <Button type="primary" htmlType="submit" size="large" block loading={loading}>
              Đăng nhập
            </Button>
          </Form.Item>
        </Form>

        <div className="auth-card__footer">
          <Text type="secondary">Chưa có tài khoản? </Text>
          <Link to="/register">Đăng ký ngay</Link>
        </div>
      </div>
    </div>
  );
}
