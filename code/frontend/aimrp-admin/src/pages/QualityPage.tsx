import { useState, useEffect } from 'react';
import { Table, Card, Row, Col, Statistic, Tag, Progress, Space, Modal, message } from 'antd';

interface Inspection {
  id: number;
  inspectionNo: string;
  inspectionType: string;
  supplierCode: string;
  itemCode: string;
  qty: number;
  qualifiedQty: number;
  status: string;
}

const QualityPage: React.FC = () => {
  const [data, setData] = useState<Inspection[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockData: Inspection[] = [
        { id: 1, inspectionNo: 'IQC20240309001', inspectionType: 'IQC', supplierCode: 'SUP001', itemCode: 'MAT001', qty: 1000, qualifiedQty: 980, status: 'APPROVED' },
        { id: 2, inspectionNo: 'IPQC20240309001', inspectionType: 'IPQC', supplierCode: '', itemCode: 'PRD001', qty: 500, qualifiedQty: 495, status: 'PENDING' },
        { id: 3, inspectionNo: 'OQC20240309001', inspectionType: 'OQC', supplierCode: '', itemCode: 'PRD002', qty: 800, qualifiedQty: 798, status: 'APPROVED' },
      ];
      setData(mockData);
    } finally {
      setLoading(false);
    }
  };

  const getTypeTag = (type: string) => {
    const config: any = { IQC: { color: 'blue', text: '来料检验' }, IPQC: { color: 'orange', text: '制程检验' }, OQC: { color: 'green', text: '出货检验' } };
    return <Tag color={config[type]?.color}>{config[type]?.text || type}</Tag>;
  };

  const getStatusTag = (status: string) => {
    const config: any = { PENDING: { color: 'orange', text: '待检验' }, APPROVED: { color: 'green', text: '已通过' }, REJECTED: { color: 'red', text: '不通过' } };
    return <Tag color={config[status]?.color}>{config[status]?.text || status}</Tag>;
  };

  const qualifiedRate = data.length > 0 ? data.reduce((a, b) => a + b.qualifiedQty, 0) / data.reduce((a, b) => a + b.qty, 0) : 0;

  const columns = [
    { title: '检验单号', dataIndex: 'inspectionNo', key: 'inspectionNo' },
    { title: '检验类型', dataIndex: 'inspectionType', key: 'inspectionType', render: (t: string) => getTypeTag(t) },
    { title: '供应商', dataIndex: 'supplierCode', key: 'supplierCode' },
    { title: '物料', dataIndex: 'itemCode', key: 'itemCode' },
    { title: '数量', dataIndex: 'qty', key: 'qty' },
    { title: '合格数', dataIndex: 'qualifiedQty', key: 'qualifiedQty' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (s: string) => getStatusTag(s) },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>质量管理</h1>
      
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}><Card><Statistic title="检验单数" value={data.length} /></Card></Col>
        <Col span={6}><Card><Statistic title="来料检验" value={data.filter(d => d.inspectionType === 'IQC').length} /></Card></Col>
        <Col span={6}><Card><Statistic title="制程检验" value={data.filter(d => d.inspectionType === 'IPQC').length} /></Card></Col>
        <Col span={6}><Card><Statistic title="合格率" value={qualifiedRate * 100} suffix="%" /></Card></Col>
      </Row>

      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} />
    </div>
  );
};

export default QualityPage;
