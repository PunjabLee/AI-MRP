import { useState, useEffect } from 'react';
import { Table, Button, Space, Input, Modal, Form, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';

interface BomItem {
  id: number;
  parentItemCode: string;
  parentItemName: string;
  childItemCode: string;
  childItemName: string;
  usageQty: number;
  level: number;
  status: string;
}

const BomPage: React.FC = () => {
  const [data, setData] = useState<BomItem[]>([]);
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
      // 模拟数据
      const mockData: BomItem[] = [
        { id: 1, parentItemCode: 'A001', parentItemName: '产品A', childItemCode: 'B001', childItemName: '部件B', usageQty: 2, level: 1, status: 'ACTIVE' },
        { id: 2, parentItemCode: 'A001', parentItemName: '产品A', childItemCode: 'C001', childItemName: '物料C', usageQty: 5, level: 1, status: 'ACTIVE' },
        { id: 3, parentItemCode: 'B001', parentItemName: '部件B', childItemCode: 'C001', childItemName: '物料C', usageQty: 3, level: 1, status: 'ACTIVE' },
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

  const handleEdit = (record: BomItem) => {
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这条BOM记录吗？',
      onOk: () => {
        setData(data.filter(item => item.id !== id));
        message.success('删除成功');
      },
    });
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const newItem = { ...values, id: data.length + 1, status: 'ACTIVE' };
      setData([...data, newItem]);
      setModalVisible(false);
      message.success('保存成功');
    } catch (error) {
      message.error('保存失败');
    }
  };

  const filteredData = data.filter(item =>
    item.parentItemCode.includes(searchText) ||
    item.childItemCode.includes(searchText)
  );

  const columns = [
    { title: '父件编码', dataIndex: 'parentItemCode', key: 'parentItemCode' },
    { title: '父件名称', dataIndex: 'parentItemName', key: 'parentItemName' },
    { title: '子件编码', dataIndex: 'childItemCode', key: 'childItemCode' },
    { title: '子件名称', dataIndex: 'childItemName', key: 'childItemName' },
    { title: '用量', dataIndex: 'usageQty', key: 'usageQty' },
    { title: '层级', dataIndex: 'level', key: 'level' },
    { title: '状态', dataIndex: 'status', key: 'status' },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: BomItem) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
          <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>BOM 管理</h1>
      <Space style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder="搜索父件/子件编码"
          onSearch={setSearchText}
          style={{ width: 300 }}
        />
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>新增 BOM</Button>
      </Space>
      <Table columns={columns} dataSource={filteredData} rowKey="id" loading={loading} />

      <Modal
        title={form.getFieldValue('id') ? '编辑BOM' : '新增BOM'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="parentItemCode" label="父件编码" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="parentItemName" label="父件名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="childItemCode" label="子件编码" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="childItemName" label="子件名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="usageQty" label="用量" rules={[{ required: true }]}>
            <Input type="number" />
          </Form.Item>
          <Form.Item name="level" label="层级">
            <Input type="number" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default BomPage;
