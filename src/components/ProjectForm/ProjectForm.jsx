import React, { useEffect, useState } from "react";
import { Modal, Form, Input, Select, Avatar, List, Button, message, Divider, Empty } from "antd";
import { UserAddOutlined, DeleteOutlined } from "@ant-design/icons";
import projectApi from "../../api/projectApi";
import userApi from "../../api/userApi";

// props:
//   open: boolean
//   project: object|null  -> null = tạo mới, có object = sửa
//   onClose: () => void
//   onSaved: (project) => void  -> gọi lại sau khi tạo/sửa xong để ProjectList refresh
export default function ProjectForm({ open, project, onClose, onSaved }) {
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);

  const [currentProject, setCurrentProject] = useState(project); // để cập nhật id sau khi tạo mới
  const [members, setMembers] = useState(project?.members || []);

  const [userOptions, setUserOptions] = useState([]);
  const [searchingUser, setSearchingUser] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState(null);
  const [addingMember, setAddingMember] = useState(false);

  const isEdit = !!currentProject?.id;

  useEffect(() => {
    if (open) {
      setCurrentProject(project);
      setMembers(project?.members || []);
      form.setFieldsValue({
        name: project?.name || "",
        description: project?.description || "",
      });
    }
  }, [open, project]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleSearchUser = async (keyword) => {
    if (!keyword) {
      setUserOptions([]);
      return;
    }
    setSearchingUser(true);
    try {
      const res = await userApi.search(keyword);
      // Giả định response: [{ id, name, email, avatar }]
      const list = res?.data || res || [];
      // Lọc bỏ user đã là thành viên
      setUserOptions(list.filter((u) => !members.some((m) => m.id === u.id)));
    } catch (err) {
      // Không chặn UI nếu API user chưa sẵn sàng, chỉ log để dev biết
      console.warn("Không tìm được user:", err);
    } finally {
      setSearchingUser(false);
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);

      if (isEdit) {
        await projectApi.update(currentProject.id, values);
        message.success("Đã cập nhật dự án");
        onSaved?.({ ...currentProject, ...values });
      } else {
        const created = await projectApi.create(values);
        // Giả định BE trả về project vừa tạo (có id) trong `created` hoặc `created.data`
        const newProject = created?.data || created;
        message.success("Đã tạo dự án. Bạn có thể thêm thành viên ngay bên dưới.");
        setCurrentProject(newProject);
        onSaved?.(newProject);
        // Không đóng modal ngay - để người dùng thêm thành viên luôn cho project mới tạo
      }
    } catch (err) {
      if (err?.errorFields) return; // lỗi validate form, không cần message thêm
      message.error(err?.message || "Lưu dự án thất bại");
    } finally {
      setSaving(false);
    }
  };

  const handleAddMember = async () => {
    if (!selectedUserId || !currentProject?.id) return;
    setAddingMember(true);
    try {
      await projectApi.addMember(currentProject.id, selectedUserId);
      const addedUser = userOptions.find((u) => u.id === selectedUserId);
      if (addedUser) setMembers((prev) => [...prev, addedUser]);
      setSelectedUserId(null);
      setUserOptions([]);
      message.success("Đã thêm thành viên");
    } catch (err) {
      message.error(err?.message || "Thêm thành viên thất bại");
    } finally {
      setAddingMember(false);
    }
  };

  const handleRemoveMember = async (userId) => {
    if (!currentProject?.id) return;
    try {
      await projectApi.removeMember(currentProject.id, userId);
      setMembers((prev) => prev.filter((m) => m.id !== userId));
      message.success("Đã xoá thành viên");
    } catch (err) {
      message.error(err?.message || "Xoá thành viên thất bại");
    }
  };

  return (
    <Modal
      open={open}
      title={isEdit ? "Sửa dự án" : "Tạo dự án mới"}
      onCancel={onClose}
      footer={[
        <Button key="close" onClick={onClose}>
          Đóng
        </Button>,
        <Button key="submit" type="primary" loading={saving} onClick={handleSubmit}>
          {isEdit ? "Lưu thay đổi" : "Tạo dự án"}
        </Button>,
      ]}
      destroyOnClose
      width={520}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="name"
          label="Tên dự án"
          rules={[{ required: true, message: "Vui lòng nhập tên dự án" }]}
        >
          <Input placeholder="VD: Hệ thống Quản lý Dự án" />
        </Form.Item>

        <Form.Item name="description" label="Mô tả">
          <Input.TextArea rows={3} placeholder="Mô tả ngắn về dự án" />
        </Form.Item>
      </Form>

      <Divider orientation="left" plain>
        Thành viên
      </Divider>

      {!currentProject?.id ? (
        <Empty
          description="Tạo dự án trước để thêm thành viên"
          image={Empty.PRESENTED_IMAGE_SIMPLE}
        />
      ) : (
        <>
          <List
            size="small"
            dataSource={members}
            locale={{ emptyText: "Chưa có thành viên nào" }}
            renderItem={(m) => (
              <List.Item
                actions={[
                  <Button
                    key="remove"
                    type="text"
                    danger
                    size="small"
                    icon={<DeleteOutlined />}
                    onClick={() => handleRemoveMember(m.id)}
                  />,
                ]}
              >
                <List.Item.Meta
                  avatar={<Avatar src={m.avatar}>{m.name?.[0]}</Avatar>}
                  title={m.name}
                  description={m.email}
                />
              </List.Item>
            )}
          />

          <div style={{ display: "flex", gap: 8, marginTop: 12 }}>
            <Select
              showSearch
              placeholder="Tìm theo tên hoặc email..."
              style={{ flex: 1 }}
              filterOption={false}
              loading={searchingUser}
              value={selectedUserId}
              onSearch={handleSearchUser}
              onChange={setSelectedUserId}
              options={userOptions.map((u) => ({
                value: u.id,
                label: `${u.name} (${u.email})`,
              }))}
              notFoundContent={searchingUser ? "Đang tìm..." : "Gõ để tìm thành viên"}
            />
            <Button
              icon={<UserAddOutlined />}
              onClick={handleAddMember}
              loading={addingMember}
              disabled={!selectedUserId}
            >
              Thêm
            </Button>
          </div>
        </>
      )}
    </Modal>
  );
}