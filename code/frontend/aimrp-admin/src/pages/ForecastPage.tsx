import { useState, useEffect } from 'react';
import { Table, Button, Space, Card, Row, Col, Select, Input, DatePicker, message, Tabs, Statistic } from 'antd';
import { LineChartOutlined, SafetyCertificateOutlined, ReloadOutlined } from '@ant-design/icons';

const { RangePicker } = DatePicker;

interface ForecastData {
  date: string;
  forecastQty: number;
  lowerBound: number;
  upperBound: number;
}

interface SafetyStockRecommendation {
  itemCode: string;
  recommendedSafetyStock: number;
  currentSafetyStock: number;
  serviceLevel: number;
}

const ForecastPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('forecast');
  const [loading, setLoading] = useState(false);
  const [itemCode, setItemCode] = useState('');
  const [method, setMethod] = useState('SIMPLE_MA');
  const [forecastData, setForecastData] = useState<ForecastData[]>([]);
  const [safetyStockData, setSafetyStockData] = useState<SafetyStockRecommendation[]>([]);

  // 模拟预测数据
  const handleForecast = async () => {
    if (!itemCode) {
      message.warning('请输入物料编码');
      return;
    }
    setLoading(true);
    try {
      const mockData: ForecastData[] = Array.from({ length: 7 }, (_, i) => ({
        date: new Date(Date.now() + (i + 1) * 24 * 3600 * 1000).toISOString().split('T')[0],
        forecastQty: Math.round(100 + Math.random() * 20),
        lowerBound: Math.round(80 + Math.random() * 10),
        upperBound: Math.round(120 + Math.random() * 10),
      }));
      setForecastData(mockData);
      message.success('预测完成');
    } finally {
      setLoading(false);
    }
  };

  // 模拟安全库存建议
  const handleSafetyStock = async () => {
    setLoading(true);
    try {
      const mockData: SafetyStockRecommendation[] = [
        { itemCode: 'A001', recommendedSafetyStock: 150, currentSafetyStock: 100, serviceLevel: 0.95 },
        { itemCode: 'B001', recommendedSafetyStock: 80, currentSafetyStock: 50, serviceLevel: 0.90 },
        { itemCode: 'C001', recommendedSafetyStock: 250, currentSafetyStock: 200, serviceLevel: 0.95 },
      ];
      setSafetyStockData(mockData);
      message.success('安全库存计算完成');
    } finally {
      setLoading(false);
    }
  };

  const forecastColumns = [
    { title: '日期', dataIndex: 'date', key: 'date' },
    { title: '预测数量', dataIndex: 'forecastQty', key: 'forecastQty' },
    { title: '下限', dataIndex: 'lowerBound', key: 'lowerBound' },
    { title: '上限', dataIndex: 'upperBound', key: 'upperBound' },
  ];

  const safetyStockColumns = [
    { title: '物料编码', dataIndex: 'itemCode', key: 'itemCode' },
    { title: '当前安全库存', dataIndex: 'currentSafetyStock', key: 'currentSafetyStock' },
    { title: '建议安全库存', dataIndex: 'recommendedSafetyStock', key: 'recommendedSafetyStock' },
    { title: '服务水平', dataIndex: 'serviceLevel', key: 'serviceLevel', 
      render: (v: number) => `${(v * 100).toFixed(0)}%` },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>AI 智能预测</h1>
      
      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <Tabs.TabPane tab={<span><LineChartOutlined /> 需求预测</span>} key="forecast">
          <Card style={{ marginBottom: 16 }}>
            <Space>
              <Input 
                placeholder="物料编码" 
                value={itemCode} 
                onChange={e => setItemCode(e.target.value)}
                style={{ width: 200 }}
              />
              <Select 
                value={method} 
                onChange={setMethod}
                style={{ width: 150 }}
              >
                <Select.Option value="SIMPLE_MA">简单移动平均</Select.Option>
                <Select.Option value="WEIGHTED_MA">加权移动平均</Select.Option>
                <Select.Option value="EXPONENTIAL">指数平滑</Select.Option>
                <Select.Option value="PROPHET">Prophet</Select.Option>
              </Select>
              <Button type="primary" icon={<ReloadOutlined />} onClick={handleForecast} loading={loading}>
                开始预测
              </Button>
            </Space>
          </Card>
          <Table 
            columns={forecastColumns} 
            dataSource={forecastData} 
            rowKey="date"
            title={() => '预测结果'}
          />
        </Tabs.TabPane>

        <Tabs.TabPane tab={<span><SafetyCertificateOutlined /> 安全库存</span>} key="safetyStock">
          <Card style={{ marginBottom: 16 }}>
            <Space>
              <Button type="primary" onClick={handleSafetyStock} loading={loading}>
                计算安全库存
              </Button>
            </Space>
          </Card>
          <Row gutter={16} style={{ marginBottom: 16 }}>
            <Col span={8}>
              <Card>
                <Statistic title="平均建议安全库存" value={safetyStockData.length > 0 ? 
                  Math.round(safetyStockData.reduce((a, b) => a + b.recommendedSafetyStock, 0) / safetyStockData.length) : 0} />
              </Card>
            </Col>
            <Col span={8}>
              <Card>
                <Statistic title="物料数量" value={safetyStockData.length} />
              </Card>
            </Col>
            <Col span={8}>
              <Card>
                <Statistic title="平均服务水平" value={safetyStockData.length > 0 ? 
                  (safetyStockData.reduce((a, b) => a + b.serviceLevel, 0) / safetyStockData.length * 100).toFixed(0) : 0} 
                  suffix="%" />
              </Card>
            </Col>
          </Row>
          <Table 
            columns={safetyStockColumns} 
            dataSource={safetyStockData} 
            rowKey="itemCode"
            title={() => '安全库存建议'}
          />
        </Tabs.TabPane>
      </Tabs>
    </div>
  );
};

export default ForecastPage;
