import { useState, useEffect } from 'react';
import { Table, Card, Row, Col, Statistic, Button, Space, Tag, Modal, message, Select, DatePicker } from 'antd';
import { FileExcelOutlined, FilePdfOutlined, ScheduleOutlined } from '@ant-design/icons';

interface Report {
  id: number;
  reportName: string;
  reportType: string;
  description: string;
}

const ReportPage: React.FC = () => {
  const [data, setData] = useState<Report[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [currentReport, setCurrentReport] = useState<Report | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockData: Report[] = [
        { id: 1, reportName: '库存台账', reportType: 'INVENTORY', description: '库存余额表' },
        { id: 2, reportName: '生产日报', reportType: 'PRODUCTION', description: '每日生产情况' },
        { id: 3, reportName: '采购分析', reportType: 'PURCHASE', description: '采购执行分析' },
        { id: 4, reportName: '质量统计', reportType: 'QUALITY', description: '质量检验统计' },
        { id: 5, reportName: '成本分析', reportType: 'COST', description: '成本分析报表' },
      ];
      setData(mockData);
    } finally {
      setLoading(false);
    }
  };

  const handleExecute = (report: Report) => {
    setCurrentReport(report);
    setModalVisible(true);
    setTimeout(() => {
      setModalVisible(false);
      message.success('报表生成完成');
    }, 2000);
  };

  const getTypeTag = (type: string) => {
    const config: any = { 
      INVENTORY: { color: 'blue', text: '库存' }, 
      PRODUCTION: { color: 'green', text: '生产' }, 
      PURCHASE: { color: 'orange', text: '采购' },
      QUALITY: { color: 'purple', text: '质量' },
      COST: { color: 'red', text: '成本' }
    };
    return <Tag color={config[type]?.color}>{config[type]?.text || type}</Tag>;
  };

  const columns = [
    { title: '报表名称', dataIndex: 'reportName', key: 'reportName' },
    { title: '报表类型', dataIndex: 'reportType', key: 'reportType', render: (t: string) => getTypeTag(t) },
    { title: '描述', dataIndex: 'description', key: 'description' },
    { 
      title: '操作', 
      key: 'action',
      render: (_: any, record: Report) => (
        <Space>
          <Button type="link" onClick={() => handleExecute(record)}>执行</Button>
          <Button type="link" icon={<FileExcelOutlined />}>Excel</Button>
          <Button type="link" icon={<FilePdfOutlined />}>PDF</Button>
        </Space>
      )
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>报表中心</h1>
      
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}><Card><Statistic title="报表总数" value={data.length} /></Card></Col>
        <Col span={6}><Card><Statistic title="今日执行" value={3} /></Card></Col>
        <Col span={6}><Card><Statistic title="定时报表" value={2} /></Card></Col>
        <Col span={6}><Card><Statistic title="导出次数" value={15} /></Card></Col>
      </Row>

      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} />

      <Modal title={`执行报表 - ${currentReport?.reportName}`} open={modalVisible} footer={null} closable={false}>
        <div style={{ textAlign: 'center', padding: 20 }}>报表执行中...</div>
      </Modal>
    </div>
  );
};

export default ReportPage;
