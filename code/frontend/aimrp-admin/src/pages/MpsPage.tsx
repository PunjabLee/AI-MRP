import { useState, useEffect } from 'react';
import { Table, Button, Card, Row, Col, Statistic, Tag, Space, Modal, message, Steps, Progress } from 'antd';
import { PlayCircleOutlined, CheckCircleOutlined } from '@ant-design/icons';

interface MpsPlan {
  id: number;
  planNo: string;
  planType: string;
  status: string;
  startDate: string;
  endDate: string;
  itemCount: number;
}

const MpsPage: React.FC = () => {
  const [data, setData] = useState<MpsPlan[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [currentPlan, setCurrentPlan] = useState<MpsPlan | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockData: MpsPlan[] = [
        { id: 1, planNo: 'MPS20240301', planType: 'MONTHLY', status: 'APPROVED', startDate: '2024-03-01', endDate: '2024-03-31', itemCount: 50 },
        { id: 2, planNo: 'MPS20240302', planType: 'WEEKLY', status: 'CALCULATED', startDate: '2024-03-11', endDate: '2024-03-17', itemCount: 20 },
        { id: 3, planNo: 'MPS20240303', planType: 'WEEKLY', status: 'DRAFT', startDate: '2024-03-18', endDate: '2024-03-24', itemCount: 15 },
      ];
      setData(mockData);
    } finally {
      setLoading(false);
    }
  };

  const handleCalculate = async (plan: MpsPlan) => {
    setCurrentPlan(plan);
    setModalVisible(true);
    message.info('MPS计算中...');
    setTimeout(() => {
      setData(data.map(d => d.id === plan.id ? { ...d, status: 'CALCULATED' } : d));
      setModalVisible(false);
      message.success('计算完成');
    }, 2000);
  };

  const handleApprove = (plan: MpsPlan) => {
    setData(data.map(d => d.id === plan.id ? { ...d, status: 'APPROVED' } : d));
    message.success('已确认');
  };

  const getStatusTag = (status: string) => {
    const config: any = { 
      DRAFT: { color: 'default', text: '草稿' }, 
      CALCULATED: { color: 'processing', text: '已计算' }, 
      APPROVED: { color: 'success', text: '已确认' } 
    };
    const c = config[status] || { color: 'default', text: status };
    return <Tag color={c.color}>{c.text}</Tag>;
  };

  const columns = [
    { title: '计划编号', dataIndex: 'planNo', key: 'planNo' },
    { title: '计划类型', dataIndex: 'planType', key: 'planType', render: (t: string) => t === 'MONTHLY' ? '月度' : '周' },
    { title: '开始日期', dataIndex: 'startDate', key: 'startDate' },
    { title: '结束日期', dataIndex: 'endDate', key: 'endDate' },
    { title: '物料数', dataIndex: 'itemCount', key: 'itemCount' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (s: string) => getStatusTag(s) },
    { 
      title: '操作', 
      key: 'action',
      render: (_: any, record: MpsPlan) => (
        <Space>
          {record.status === 'DRAFT' && <Button type="link" icon={<PlayCircleOutlined />} onClick={() => handleCalculate(record)}>计算</Button>}
          {record.status === 'CALCULATED' && <Button type="link" icon={<CheckCircleOutlined />} onClick={() => handleApprove(record)}>确认</Button>}
        </Space>
      )
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>MPS 主生产计划</h1>
      
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}><Card><Statistic title="计划总数" value={data.length} /></Card></Col>
        <Col span={6}><Card><Statistic title="草稿" value={data.filter(d => d.status === 'DRAFT').length} /></Card></Col>
        <Col span={6}><Card><Statistic title="已计算" value={data.filter(d => d.status === 'CALCULATED').length} /></Card></Col>
        <Col span={6}><Card><Statistic title="已确认" value={data.filter(d => d.status === 'APPROVED').length} /></Card></Col>
      </Row>

      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} />

      <Modal title="MPS计算进度" open={modalVisible} footer={null} closable={false}>
        <Steps current={1} items={[{ title: '加载数据' }, { title: '计算中' }, { title: '完成' }]} />
        <Progress percent={60} status="active" style={{ marginTop: 24 }} />
      </Modal>
    </div>
  );
};

export default MpsPage;
