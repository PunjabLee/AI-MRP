import { useState, useEffect } from 'react';
import { Table, Button, Space, Input, Modal, Form, message, Tag, Tree } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';

interface Organization {
  id: number;
  orgCode: string;
  orgName: string;
  orgType: string;
  level: number;
  leader: string;
  status: string;
}

const OrgPage: React.FC = () => {
  const [data, setData] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [treeData, setTreeData] = useState<any[]>([]);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockData: Organization[] = [
        { id: 1, orgCode: 'GROUP', orgName: '集团', orgType: 'GROUP', level: 1, leader: '张总', status: 'ENABLED' },
        { id: 2, orgCode: 'COMPANY', orgName: '母公司', orgType: 'COMPANY', level: 2, leader: '李总', status: 'ENABLED' },
        { id: 3, orgCode: 'FACTORY01', orgName: '工厂一', orgType: 'FACTORY', level: 3, leader: '王厂', status: 'ENABLED' },
        { id: 4, orgCode: 'FACTORY02', orgName: '工厂二', orgType: 'FACTORY', level: 3, leader: '赵厂', status: 'ENABLED' },
      ];
      setData(mockData);
      
      // 构建树形数据
      setTreeData([
        { key: '1', title: '集团', children: [
          { key: '2', title: '母公司', children: [
            { key: '3', title: '工厂一' },
            { key: '4', title: '工厂二' },
          ]},
        ]},
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    form.resetFields();
    setModalVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setData([...data, { ...values, id: data.length + 1, status: 'ENABLED' }]);
      setModalVisible(false);
      message.success('创建成功');
    } catch (error) {
      message.error('创建失败');
    }
  };

  const getTypeTag = (type: string) => {
    const colors: any = { GROUP: 'red', COMPANY: 'orange', FACTORY: 'blue', DEPARTMENT: 'green' };
    const names: any = { GROUP: '集团', COMPANY: '公司', FACTORY: '工厂', DEPARTMENT: '部门' };
    return <Tag color={colors[type]}>{names[type] || type}</Tag>;
  };

  const columns = [
    { title: '组织编码', dataIndex: 'orgCode', key: 'orgCode' },
    { title: '组织名称', dataIndex: 'orgName', key: 'orgName' },
    { title: '类型', dataIndex: 'orgType', key: 'orgType', render: (type: string) => getTypeTag(type) },
    { title: '层级', dataIndex: 'level', key: 'level' },
    { title: '负责人', dataIndex: 'leader', key: 'leader' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={s === 'ENABLED' ? 'green' : 'red'}>{s === 'ENABLED' ? '启用' : '停用'}</Tag> },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>组织架构管理</h1>
      
      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} />
      
      <Modal title="新建组织" open={modalVisible} onOk={handleSubmit} onCancel={() => setModalVisible(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="orgCode" label="组织编码" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="orgName" label="组织名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="orgType" label="组织类型" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="leader" label="负责人">
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default OrgPage;
