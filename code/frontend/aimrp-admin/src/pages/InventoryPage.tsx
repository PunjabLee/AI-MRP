import { useState, useEffect } from 'react';
import { Table, Button, Space, Input, Modal, Form, message, Select, Tag } from 'react';
import { PlusOutlined, EditOutlined, DeleteOutlined, ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';

interface InventoryItem {
  id: number;
  itemCode: string;
  itemName: string;
  warehouseCode: string;
  warehouseName: string;
  onHandQty: number;
  allocatedQty: number;
  availableQty: number;
  unit: string;
}

const InventoryPage: React.FC = () => {
  const [data, setData] = useState<InventoryItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [searchText, setSearchText] = useState('');
  const [actionType, setActionType] = useState<'in' | 'out'>('in');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockData: InventoryItem[] = [
        { id: 1, itemCode: 'A001', itemName: '产品A', warehouseCode: 'WH01', warehouseName: '主仓库', onHandQty: 500, allocatedQty: 100, availableQty: 400, unit: 'PCS' },
        { id: 2, itemCode: 'B001', itemName: '部件B', warehouseCode: 'WH01', warehouseName: '主仓库', onHandQty: 200, allocatedQty: 0, availableQty: 200, unit: 'PCS' },
        { id: 3, itemCode: 'C001', itemName: '物料C', warehouseCode: 'WH01', warehouseName: '主仓库', onHandQty: 1000, allocatedQty: 50, availableQty: 950, unit: 'KG' },
      ];
      setData(mockData);
    } catch (error) {
      message.error('加载失败');
    } finally {
      setLoading(false);
    }
  };

  const handleInStock = (record: InventoryItem) => {
    setActionType('in');
    form.setFieldsValue({ itemCode: record.itemCode, warehouseCode: record.warehouseCode });
    setModalVisible(true);
  };

  const handleOutStock = (record: InventoryItem) => {
    setActionType('out');
    form.setFieldsValue({ itemCode: record.itemCode, warehouseCode: record.warehouseCode });
    setModalVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const { qty } = values;
      
      setData(data.map(item => {
        if (item.itemCode === values.itemCode && item.warehouseCode === values.warehouseCode) {
          const newOnHand = actionType === 'in' 
            ? item.onHandQty + qty 
            : item.onHandQty - qty;
          return {
            ...item,
            onHandQty: newOnHand,
            availableQty: newOnHand - item.allocatedQty
          };
        }
        return item;
      }));
      
      setModalVisible(false);
      message.success(actionType === 'in' ? '入库成功' : '出库成功');
    } catch (error) {
      message.error('操作失败');
    }
  };

  const filteredData = data.filter(item =>
    item.itemCode.includes(searchText) ||
    item.itemName.includes(searchText)
  );

  const columns = [
    { title: '物料编码', dataIndex: 'itemCode', key: 'itemCode' },
    { title: '物料名称', dataIndex: 'itemName', key: 'itemName' },
    { title: '仓库编码', dataIndex: 'warehouseCode', key: 'warehouseCode' },
    { title: '仓库名称', dataIndex: 'warehouseName', key: 'warehouseName' },
    { title: '现有量', dataIndex: 'onHandQty', key: 'onHandQty' },
    { title: '预留量', dataIndex: 'allocatedQty', key: 'allocatedQty' },
    { title: '可用量', dataIndex: 'availableQty', key: 'availableQty', 
      render: (qty: number) => qty < 100 ? <Tag color="red">{qty}</Tag> : qty },
    { title: '单位', dataIndex: 'unit', key: 'unit' },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: InventoryItem) => (
        <Space>
          <Button type="primary" icon={<ArrowUpOutlined />} onClick={() => handleInStock(record)}>入库</Button>
          <Button danger icon={<ArrowDownOutlined />} onClick={() => handleOutStock(record)}>出库</Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>库存管理</h1>
      <Space style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder="搜索物料编码/名称"
          onSearch={setSearchText}
          style={{ width: 300 }}
        />
      </Space>
      <Table columns={columns} dataSource={filteredData} rowKey="id" loading={loading} />

      <Modal
        title={actionType === 'in' ? '入库' : '出库'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="itemCode" label="物料编码">
            <Input disabled />
          </Form.Item>
          <Form.Item name="warehouseCode" label="仓库">
            <Input disabled />
          </Form.Item>
          <Form.Item name="qty" label="数量" rules={[{ required: true }]}>
            <Input type="number" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default InventoryPage;
