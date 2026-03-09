import { useState, useEffect } from 'react';
import { Table, Card, Row, Col, Statistic, Tag, Progress, Space, Modal, message } from 'antd';
import { ToolOutlined } from '@ant-design/icons';

interface Equipment {
  id: number;
  equipmentCode: string;
  equipmentName: string;
  equipmentType: string;
  status: string;
  oee: number;
}

const EquipmentPage: React.FC = () => {
  const [data, setData] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockData: Equipment[] = [
        { id: 1, equipmentCode: 'EQ001', equipmentName: '加工中心1', equipmentType: 'CNC', status: 'RUNNING', oee: 0.85 },
        { id: 2, equipmentCode: 'EQ002', equipmentName: '车床1', equipmentType: 'LATHE', status: 'RUNNING', oee: 0.92 },
        { id: 3, equipmentCode: 'EQ003', equipmentName: '铣床1', equipmentType: 'MILLING', status: 'MAINTAIN', oee: 0.00 },
        { id: 4, equipmentCode: 'EQ004', equipmentName: '钻床1', equipmentType: 'DRILLING', status: 'IDLE', oee: 0.00 },
      ];
      setData(mockData);
    } finally {
      setLoading(false);
    }
  };

  const getStatusTag = (status: string) => {
    const config: any = { RUNNING: { color: 'green', text: '运行中' }, IDLE: { color: 'default', text: '空闲' }, MAINTAIN: { color: 'orange', text: '维护中' }, FAULT: { color: 'red', text: '故障' } };
    return <Tag color={config[status]?.color}>{config[status]?.text || status}</Tag>;
  };

  const avgOEE = data.length > 0 ? data.reduce((a, b) => a + b.oee, 0) / data.length : 0;

  const columns = [
    { title: '设备编码', dataIndex: 'equipmentCode', key: 'equipmentCode' },
    { title: '设备名称', dataIndex: 'equipmentName', key: 'equipmentName' },
    { title: '设备类型', dataIndex: 'equipmentType', key: 'equipmentType' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (s: string) => getStatusTag(s) },
    { title: 'OEE', dataIndex: 'oee', key: 'oee', render: (o: number) => o > 0 ? <Progress percent={o * 100} size="small" /> : '-' },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1><ToolOutlined /> 设备管理</h1>
      
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}><Card><Statistic title="设备总数" value={data.length} /></Card></Col>
        <Col span={6}><Card><Statistic title="运行中" value={data.filter(d => d.status === 'RUNNING').length} valueStyle={{ color: '#3f8600' }} /></Card></Col>
        <Col span={6}><Card><Statistic title="平均OEE" value={avgOEE * 100} suffix="%" /></Card></Col>
        <Col span={6}><Card><Statistic title="维护中" value={data.filter(d => d.status === 'MAINTAIN').length} valueStyle={{ color: '#cf1322' }} /></Card></Col>
      </Row>

      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} />
    </div>
  );
};

export default EquipmentPage;
