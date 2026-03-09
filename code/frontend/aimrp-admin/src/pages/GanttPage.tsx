import { useState, useEffect } from 'react';
import { Table, Card, Row, Col, Select, Button, message, Tag, Tooltip } from 'antd';
import { GanttChartOutlined, ZoomInOutlined, ZoomOutOutlined } from '@ant-design/icons';

interface GanttTask {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  progress: number;
  resource: string;
  status: string;
}

const GanttPage: React.FC = () => {
  const [data, setData] = useState<GanttTask[]>([]);
  const [loading, setLoading] = useState(false);
  const [zoom, setZoom] = useState(1);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockData: GanttTask[] = [
        { id: 1, name: '产品A - 订单001', startDate: '2024-03-01', endDate: '2024-03-05', progress: 100, resource: '产线1', status: 'COMPLETED' },
        { id: 2, name: '产品A - 订单002', startDate: '2024-03-05', endDate: '2024-03-10', progress: 60, resource: '产线1', status: 'IN_PRODUCTION' },
        { id: 3, name: '部件B - 订单003', startDate: '2024-03-03', endDate: '2024-03-08', progress: 100, resource: '产线2', status: 'COMPLETED' },
        { id: 4, name: '部件B - 订单004', startDate: '2024-03-08', endDate: '2024-03-12', progress: 30, resource: '产线2', status: 'IN_PRODUCTION' },
        { id: 5, name: '产品A - 订单005', startDate: '2024-03-12', endDate: '2024-03-18', progress: 0, resource: '产线1', status: 'PENDING' },
      ];
      setData(mockData);
    } finally {
      setLoading(false);
    }
  };

  // 简单的甘特条形图渲染
  const renderGanttBar = (task: GanttTask) => {
    const startDate = new Date(task.startDate);
    const endDate = new Date(task.endDate);
    const today = new Date('2024-03-09'); // 模拟当前日期
    
    const totalDays = 30; // 显示30天
    const startOffset = Math.max(0, Math.floor((startDate.getTime() - today.getTime()) / (24 * 3600 * 1000)));
    const duration = Math.floor((endDate.getTime() - startDate.getTime()) / (24 * 3600 * 1000));
    
    const left = (startOffset / totalDays) * 100;
    const width = (duration / totalDays) * 100 * zoom;
    
    const getStatusColor = (status: string) => {
      switch (status) {
        case 'COMPLETED': return '#52c41a';
        case 'IN_PRODUCTION': return '#1890ff';
        case 'PENDING': return '#faad14';
        default: return '#d9d9d9';
      }
    };

    return (
      <div style={{ position: 'relative', height: 30, background: '#f5f5f5', borderRadius: 4, marginBottom: 4 }}>
        <Tooltip title={`${task.name}: ${task.progress}%`}>
          <div style={{
            position: 'absolute',
            left: `${left}%`,
            width: `${Math.max(width, 2)}%`,
            height: '100%',
            background: getStatusColor(task.status),
            borderRadius: 4,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontSize: 12,
            overflow: 'hidden',
            whiteSpace: 'nowrap',
          }}>
            {task.progress}%
          </div>
        </Tooltip>
      </div>
    );
  };

  const getStatusTag = (status: string) => {
    const config = {
      COMPLETED: { color: 'green', text: '已完成' },
      IN_PRODUCTION: { color: 'blue', text: '生产中' },
      PENDING: { color: 'orange', text: '待生产' },
    };
    const c = config[status as keyof typeof config] || { color: 'default', text: status };
    return <Tag color={c.color}>{c.text}</Tag>;
  };

  const columns = [
    { title: '任务', dataIndex: 'name', key: 'name', width: 200 },
    { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 100 },
    { title: '结束日期', dataIndex: 'endDate', key: 'endDate', width: 100 },
    { title: '资源', dataIndex: 'resource', key: 'resource', width: 80 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80, render: (s: string) => getStatusTag(s) },
    { title: '甘特图', key: 'gantt', render: (_: any, record: GanttTask) => renderGanttBar(record) },
  ];

  // 统计
  const stats = {
    total: data.length,
    completed: data.filter(d => d.status === 'COMPLETED').length,
    inProgress: data.filter(d => d.status === 'IN_PRODUCTION').length,
    pending: data.filter(d => d.status === 'PENDING').length,
  };

  return (
    <div style={{ padding: 24 }}>
      <h1><GanttChartOutlined /> 生产甘特图</h1>
      
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic title="总任务" value={stats.total} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="已完成" value={stats.completed} valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="生产中" value={stats.inProgress} valueStyle={{ color: '#1890ff' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="待生产" value={stats.pending} valueStyle={{ color: '#faad14' }} />
          </Card>
        </Col>
      </Row>

      <Card 
        title="甘特图视图"
        extra={
          <Space>
            <Button icon={<ZoomOutOutlined />} onClick={() => setZoom(z => Math.max(0.5, z - 0.2))} />
            <Button icon={<ZoomInOutlined />} onClick={() => setZoom(z => Math.min(2, z + 0.2))} />
          </Space>
        }
      >
        <Table 
          columns={columns} 
          dataSource={data} 
          rowKey="id" 
          pagination={false}
          size="small"
        />
      </Card>
    </div>
  );
};

export default GanttPage;
