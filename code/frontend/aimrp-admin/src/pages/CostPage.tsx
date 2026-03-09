import { useState, useEffect } from 'react';
import { Table, Card, Row, Col, Statistic, Progress, Space } from 'antd';

interface CostElement {
  id: number;
  elementCode: string;
  elementName: string;
  amount: number;
  ratio: number;
}

const CostPage: React.FC = () => {
  const [data, setData] = useState<CostElement[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockData: CostElement[] = [
        { id: 1, elementCode: 'MATERIAL', elementName: '材料成本', amount: 500000, ratio: 0.65 },
        { id: 2, elementCode: 'LABOR', elementName: '人工成本', amount: 150000, ratio: 0.20 },
        { id: 3, elementCode: 'OVERHEAD', elementName: '制造费用', amount: 120000, ratio: 0.15 },
      ];
      setData(mockData);
    } finally {
      setLoading(false);
    }
  };

  const totalCost = data.reduce((a, b) => a + b.amount, 0);

  const columns = [
    { title: '成本要素编码', dataIndex: 'elementCode', key: 'elementCode' },
    { title: '成本要素名称', dataIndex: 'elementName', key: 'elementName' },
    { title: '金额', dataIndex: 'amount', key: 'amount', render: (a: number) => `¥${a.toLocaleString()}` },
    { 
      title: '占比', 
      dataIndex: 'ratio', 
      key: 'ratio', 
      render: (r: number) => <Progress percent={r * 100} size="small" />
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>成本管理</h1>
      
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}><Card><Statistic title="总成本" value={totalCost} prefix="¥" /></Card></Col>
        <Col span={6}><Card><Statistic title="材料成本" value={500000} prefix="¥" valueStyle={{ color: '#1890ff' }} /></Card></Col>
        <Col span={6}><Card><Statistic title="人工成本" value={150000} prefix="¥" valueStyle={{ color: '#52c41a' }} /></Card></Col>
        <Col span={6}><Card><Statistic title="制造费用" value={120000} prefix="¥" valueStyle={{ color: '#faad14' }} /></Card></Col>
      </Row>

      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} pagination={false} />
    </div>
  );
};

export default CostPage;
