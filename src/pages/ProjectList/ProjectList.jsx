import React, { useEffect, useState } from "react";
import {
  Row,
  Col,
  Card,
  Button,
  Avatar,
  Typography,
  Spin,
  Empty,
  message,
  Dropdown,
} from "antd";
import { PlusOutlined, MoreOutlined, TeamOutlined } from "@ant-design/icons";
import projectApi from "../../api/projectApi.js";
import ProjectForm from "../../components/ProjectForm/ProjectForm.jsx";
import "./ProjectList.scss";

const { Title, Paragraph, Text } = Typography;

export default function ProjectList() {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);

  const [formOpen, setFormOpen] = useState(false);
  const [editingProject, setEditingProject] = useState(null);

  const fetchProjects = async () => {
    setLoading(true);
    try {
      const res = await projectApi.getAll();
      // Giả định response: [{ id, name, description, members: [...] }]
      setProjects(res?.data || res || []);
    } catch (err) {
      message.error(err?.message || "Không tải được danh sách dự án");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjects();
  }, []);

  const openCreate = () => {
    setEditingProject(null);
    setFormOpen(true);
  };

  const openEdit = (project) => {
    setEditingProject(project);
    setFormOpen(true);
  };

  const handleDelete = async (project) => {
    try {
      await projectApi.remove(project.id);
      message.success("Đã xoá dự án");
      fetchProjects();
    } catch (err) {
      message.error(err?.message || "Xoá dự án thất bại");
    }
  };

  const handleSaved = () => {
    fetchProjects(); // refresh lại list sau khi tạo/sửa/thêm-xoá thành viên
  };

  return (
    <div className="project-list">
      <div className="project-list__header">
        <div>
          <Title level={3} style={{ marginBottom: 0 }}>
            Dự án của tôi
          </Title>
          <Text type="secondary">Danh sách các dự án bạn đang tham gia</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          Tạo dự án
        </Button>
      </div>

      {loading ? (
        <div className="project-list__loading">
          <Spin size="large" />
        </div>
      ) : projects.length === 0 ? (
        <Empty description="Chưa có dự án nào" style={{ marginTop: 48 }}>
          <Button type="primary" onClick={openCreate}>
            Tạo dự án đầu tiên
          </Button>
        </Empty>
      ) : (
        <Row gutter={[16, 16]}>
          {projects.map((project) => (
            <Col xs={24} sm={12} lg={8} key={project.id}>
              <Card
                hoverable
                className="project-card"
                extra={
                  <Dropdown
                    menu={{
                      items: [
                        { key: "edit", label: "Sửa dự án" },
                        { key: "delete", label: "Xoá dự án", danger: true },
                      ],
                      onClick: ({ key }) => {
                        if (key === "edit") openEdit(project);
                        if (key === "delete") handleDelete(project);
                      },
                    }}
                    trigger={["click"]}
                  >
                    <Button type="text" icon={<MoreOutlined />} />
                  </Dropdown>
                }
                title={project.name}
                onClick={() => openEdit(project)}
              >
                <Paragraph
                  type="secondary"
                  ellipsis={{ rows: 2 }}
                  style={{ minHeight: 44 }}
                >
                  {project.description || "Chưa có mô tả"}
                </Paragraph>

                <div className="project-card__footer">
                  <Avatar.Group max={{ count: 4 }}>
                    {(project.members || []).map((m) => (
                      <Avatar key={m.id} src={m.avatar}>
                        {m.name?.[0]}
                      </Avatar>
                    ))}
                  </Avatar.Group>
                  <Text type="secondary" className="project-card__member-count">
                    <TeamOutlined /> {(project.members || []).length} thành viên
                  </Text>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      )}

      <ProjectForm
        open={formOpen}
        project={editingProject}
        onClose={() => setFormOpen(false)}
        onSaved={handleSaved}
      />
    </div>
  );
}