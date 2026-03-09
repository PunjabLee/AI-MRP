import { useState, useEffect } from 'react';
import { Table, Button, Space, Input, Modal, Form, message, Tag, Timeline, Card, Row, Col } from 'antd';
import { PlusOutlined, PlayCircleOutlined, CheckCircleOutlined, EyeOutlined } from '@ant-design/icons';

interface ProductionOrder {
  id: number;
  moNo: string;
  itemCode: string;
  itemName: string;
  qty: number;
  completedQty: number;
  status: string;
  startDate: string;
  endDate: string;
  priority: number;
}

const ProductionPage: React.FC = () => {
  const [data, setData] = useState<ProductionOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentOrder, setCurrentOrder] = useState<ProductionOrder | null>(null);
  const [form] = Form.useForm();
  const [searchText, setSearchText] = useState('');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockData: ProductionOrder[] = [
        { id: 1, moNo: 'MO202403090001', itemCode: 'A001', itemName: '产品A', qty: 1000, completedQty: 600, status: 'IN_PRODUCTION', startDate: '2024-03-08', endDate: '2024-03-15', priority: 1 },
        { id: 2, moNo: 'MO202403090002', itemCode: 'B001', itemName: '部件B', qty: 500, completedQty: 500, status: 'COMPLETED', startDate: '2024-03-05', endDate: '2024-03-10', priority: 2 },
        { id: 3, moNo: 'MO202403090003', itemCode: 'A001', itemName: '产品A', qty: 800, completedQty: 0, status: 'PENDING', startDate: '2024-03-12', endDate: '2024-03-18', priority: 3 },
      ];
      setData(mockData);
    } catch (error) {
      message.error('加载失败');
    } finally {
      setLoading(false);
    }
  };

  const handleView = (record: ProductionOrder) => {
    setCurrentOrder(record);
    setDetailVisible(true);
  };

  const handleStart = (id: number) => {
    setData(data.map(item => 
      item.id === id, status: 'IN_PRODUCTION' ? { ...item } : item
    ));
    message.success('已开工');
  };

  const handleComplete = (id: number) => {
    setData(data.map(item => 
      item.id === id ? { ...item, status: 'COMPLETED', completedQty: item.qty } : item
    ));
    message.success('已完工');
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return 'orange';
      case 'IN_PRODUCTION': return 'blue';
      case 'COMPLETED': return 'green';
      case 'CANCELLED': return 'red';
      default: return 'default';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PENDING': return '待生产';
      case 'IN_PRODUCTION': return '生产中';
      case 'COMPLETED': return '已完成';
      case 'CANCELLED': return '已取消';
      default: return status;
    }
  };

  const filteredData = data.filter(item =>
    item.moNo.includes(searchText) ||
    item.itemCode.includes(searchText) ||
    item.itemName.includes(searchText)
  );

  const columns = [
    { title: '工单号', dataIndex: 'moNo', key: 'moNo' },
    { title: '物料编码', dataIndex: 'itemCode', key: 'itemCode' },
    { title: '物料名称', dataIndex: 'itemName', key: 'itemName' },
    { title: '计划数量', dataIndex: 'qty', key: 'qty' },
    { title: '已完成', dataIndex: 'completedQty', key: 'completedQty' },
    { 
      title: '进度', 
      key: 'progress',
      render: (_: any, record: ProductionOrder) => 
        `${Math.round((record.completedQty / record.qty) * 100)}%`
    },
    { title: '开始日期', dataIndex: 'startDate', key: 'startDate' },
    { title: '结束日期', dataIndex: 'endDate', key: 'endDate' },
    { 
      title: '状态', 
      dataIndex: 'status', 
      key: 'status',
      render: (status: string) => <Tag color={getStatusColor(status)}>{getStatusText(status)}</Tag>
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: ProductionOrder) => (
        <Space>
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleView(record)}>查看</Button>
          {record.status === 'PENDING' && (
            <Button type="link" icon={<PlayCircleOutlined />} onClick={() => handleStart(record.id)}>开工</Button>
          )}
          {record.status === 'IN_PRODUCTION' && (
            <Button type="link" icon={<CheckCircleOutlined />} onClick={() => handleComplete(record.id)}>完工</Button>
          )}
        </Space>
      ),
    },
  ];

  // 统计卡片数据
  const statistics = {
    total: data.length,
    pending: data.filter(d => d.status === 'PENDING').length,
    inProduction: data.filter(d => d.status === 'IN_PRODUCTION').length,
    completed: data.filter(d => d.status === 'COMPLETED').length,
  };

  return (
    <div style={{ padding: 24 }}>
      <h1>生产管理</h1>
      
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic title="总工单" value={statistics.total} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="待生产" value={statistics.pending} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="生产中" value={statistics.inProduction} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="已完成" value={statistics.completed} />
          </Card>
        </Col>
      </Row>

      <Space style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder="搜索工单号/物料"
          onSearch={setSearchText}
          style={{ width: 300 }}
        />
        <Button type="primary" icon={<PlusOutlined />}>新建工单</Button>
      </Space>
      <Table columns={columns} dataSource={filteredData} rowKey="id" loading={loading} />

      <Modal
        title={`工单详情 - ${currentOrder?.moNo}`}
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={600}
      >
        {currentOrder && (
          <>
            <Timeline
              items={[
                { color: 'green', children: `创建工单 ${currentOrder.moNo}` },
                { color: currentOrder.status === 'PENDING' ? 'gray' : 'green', children: currentOrder.status !== 'PENDING' ? '开始生产' : '等待生产' },
                { color: currentOrder.status === 'COMPLETED' ? 'green' : 'gray', children: currentOrder.status === 'COMPLETED' ? '生产完成' : '生产进行中' },
              ]}
            />
            <p><strong>工单号：</strong>{currentOrder.moNo}</p>
            <p><strong>物料：</strong>{currentOrder.itemName}</p>
            <p><strong>计划数量：</strong>{currentOrder.qty}</p>
            <p><strong>已完成：</strong>{currentOrder.completedQty}</p>
            <p><strong>计划工期：</strong>{currentOrder.startDate} ~ {currentOrder.endDate}</p>
          </>
        )}
      </Modal>
    </div>
  );
};

// 简单的统计组件
const Statistic: React.FC<{ title: string; value: number }> = ({ title, value }) => (
  <div style={{ textAlign: 'center' }}>
    <div style={{ fontSize: 24, fontWeight: 'bold' }}>{value}</div>
    <div style={{ color: '#888' }}>{title}</div>
  </div>
);

export default ProductionPage;
