import { useState, useEffect } from 'react';
import { Table, Button, Space, Input, Modal, Form, message, Tag, Select } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';

interface Item {
  id: number;
  itemCode: string;
  itemName: string;
  itemType: string;
  source: string;
  unit: string;
  leadTime: number;
  safetyStock: number;
  status: string;
}

const ItemPage: React.FC = () => {
  const [data, setData] = useState<Item[]>([]);
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
      const mockData: Item[] = [
        { id: 1, itemCode: 'A001', itemName: '产品A', itemType: 'FINISHED', source: 'MAKE', unit: 'PCS', leadTime: 7, safetyStock: 100, status: 'ACTIVE' },
        { id: 2, itemCode: 'B001', itemName: '部件B', itemType: 'SEMI', source: 'MAKE', unit: 'PCS', leadTime: 3, safetyStock: 50, status: 'ACTIVE' },
        { id: 3, itemCode: 'C001', itemName: '物料C', itemType: 'RAW', source: 'BUY', unit: 'KG', leadTime: 5, safetyStock: 200, status: 'ACTIVE' },
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

  const handleEdit = (record: Item) => {
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个物料吗？',
      onOk: () => {
        setData(data.filter(item => item.id !== id));
        message.success('删除成功');
      },
    });
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const exists = data.find(item => item.itemCode === values.itemCode && !values.id);
      if (exists) {
        message.error('物料编码已存在');
        return;
      }
      
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
    item.itemCode.includes(searchText) ||
    item.itemName.includes(searchText)
  );

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'FINISHED': return 'blue';
      case 'SEMI': return 'orange';
      case 'RAW': return 'green';
      default: return 'default';
    }
  };

  const getSourceText = (source: string) => {
    switch (source) {
      case 'MAKE': return '自制';
      case 'BUY': return '采购';
      case 'CONTRACT': return '委外';
      default: return source;
    }
  };

  const columns = [
    { title: '物料编码', dataIndex: 'itemCode', key: 'itemCode' },
    { title: '物料名称', dataIndex: 'itemName', key: 'itemName' },
    { 
      title: '物料类型', 
      dataIndex: 'itemType', 
      key: 'itemType',
      render: (type: string) => <Tag color={getTypeColor(type)}>{type}</Tag>
    },
    { 
      title: '来源', 
      dataIndex: 'source', 
      key: 'source',
      render: (source: string) => getSourceText(source)
    },
    { title: '单位', dataIndex: 'unit', key: 'unit' },
    { title: '采购周期(天)', dataIndex: 'leadTime', key: 'leadTime' },
    { title: '安全库存', dataIndex: 'safetyStock', key: 'safetyStock' },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Item) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
          <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>物料主数据</h1>
      <Space style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder="搜索物料编码/名称"
          onSearch={setSearchText}
          style={{ width: 300 }}
        />
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>新增物料</Button>
      </Space>
      <Table columns={columns} dataSource={filteredData} rowKey="id" loading={loading} />

      <Modal
        title={form.getFieldValue('id') ? '编辑物料' : '新增物料'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="itemCode" label="物料编码" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="itemName" label="物料名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="itemType" label="物料类型" rules={[{ required: true }]}>
            <Select>
              <Select.Option value="FINISHED">成品</Select.Option>
              <Select.Option value="SEMI">半成品</Select.Option>
              <Select.Option value="RAW">原材料</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="source" label="来源" rules={[{ required: true }]}>
            <Select>
              <Select.Option value="MAKE">自制</Select.Option>
              <Select.Option value="BUY">采购</Select.Option>
              <Select.Option value="CONTRACT">委外</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="unit" label="单位" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="leadTime" label="采购周期(天)">
            <Input type="number" />
          </Form.Item>
          <Form.Item name="safetyStock" label="安全库存">
            <Input type="number" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ItemPage;
