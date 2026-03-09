import { useState, useEffect } from 'react';
import { Table, Button, Space, Input, Modal, Form, message, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';

interface Supplier {
  id: number;
  supplierCode: string;
  supplierName: string;
  contact: string;
  phone: string;
  email: string;
  address: string;
  grade: string;
  status: string;
}

const SupplierPage: React.FC = () => {
  const [data, setData] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [searchText, setSearchText] = useState('');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockData: Supplier[] = [
        { id: 1, supplierCode: 'S001', supplierName: '供应商A', contact: '张三', phone: '13800138001', email: 'zhangsan@example.com', address: '上海市浦东新区', grade: 'A', status: 'ACTIVE' },
        { id: 2, supplierCode: 'S002', supplierName: '供应商B', contact: '李四', phone: '13800138002', email: 'lisi@example.com', address: '北京市朝阳区', grade: 'B', status: 'ACTIVE' },
        { id: 3, supplierCode: 'S003', supplierName: '供应商C', contact: '王五', phone: '13800138003', email: 'wangwu@example.com', address: '广州市天河区', grade: 'C', status: 'ACTIVE' },
      ];
      setData(mockData);
    } catch (error) {
      message.error('加载失败');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Supplier) => {
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个供应商吗？',
      onOk: () => {
        setData(data.filter(item => item.id !== id));
        message.success('删除成功');
      },
    });
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (values.id) {
        setData(data.map(item => item.id === values.id ? { ...item, ...values } : item));
      } else {
        setData([...data, { ...values, id: data.length + 1, status: 'ACTIVE' }]);
      }
      setModalVisible(false);
      message.success('保存成功');
    } catch (error) {
      message.error('保存失败');
    }
  };

  const filteredData = data.filter(item =>
    item.supplierCode.includes(searchText) ||
    item.supplierName.includes(searchText)
  );

  const getGradeColor = (grade: string) => {
    switch (grade) {
      case 'A': return 'green';
      case 'B': return 'orange';
      case 'C': return 'red';
      default: return 'default';
    }
  };

  const columns = [
    { title: '供应商编码', dataIndex: 'supplierCode', key: 'supplierCode' },
    { title: '供应商名称', dataIndex: 'supplierName', key: 'supplierName' },
    { title: '联系人', dataIndex: 'contact', key: 'contact' },
    { title: '电话', dataIndex: 'phone', key: 'phone' },
    { title: '邮箱', dataIndex: 'email', key: 'email' },
    { title: '地址', dataIndex: 'address', key: 'address', ellipsis: true },
    { 
      title: '等级', 
      dataIndex: 'grade', 
      key: 'grade',
      render: (grade: string) => <Tag color={getGradeColor(grade)}>{grade}级</Tag>
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Supplier) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
          <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>供应商管理</h1>
      <Space style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder="搜索供应商编码/名称"
          onSearch={setSearchText}
          style={{ width: 300 }}
        />
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>新增供应商</Button>
      </Space>
      <Table columns={columns} dataSource={filteredData} rowKey="id" loading={loading} />

      <Modal
        title={form.getFieldValue('id') ? '编辑供应商' : '新增供应商'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="supplierCode" label="供应商编码" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="supplierName" label="供应商名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="contact" label="联系人">
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="电话">
            <Input />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input type="email" />
          </Form.Item>
          <Form.Item name="address" label="地址">
            <Input.TextArea />
          </Form.Item>
          <Form.Item name="grade" label="等级">
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default SupplierPage;
