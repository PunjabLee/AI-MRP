import { useState, useEffect } from 'react';
import { Table, Button, Card, Row, Col, Statistic, Tag, Space, Modal, Form, Input, Select, message } from 'antd';
import { PlusOutlined } from '@ant-design/icons';

interface Warehouse {
  id: number;
  warehouseCode: string;
  warehouseName: string;
  warehouseType: string;
  orgName: string;
  status: string;
}

const WarehousePage: React.FC = () => {
  const [data, setData] = useState<Warehouse[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockData: Warehouse[] = [
        { id: 1, warehouseCode: 'WH01', warehouseName: '主仓库', warehouseType: 'MAIN', orgName: '工厂一', status: 'ENABLED' },
        { id: 2, warehouseCode: 'WH02', warehouseName: '原料仓', warehouseType: 'RAW', orgName: '工厂一', status: 'ENABLED' },
        { id: 3, warehouseCode: 'WH03', warehouseName: '成品仓', warehouseType: 'FINISHED', orgName: '工厂一', status: 'ENABLED' },
        { id: 4, warehouseCode: 'WH04', warehouseName: '半成品仓', warehouseType: 'SEMI', orgName: '工厂二', status: 'ENABLED' },
      ];
      setData(mockData);
    } finally {
      setLoading(false);
    }
  };

  const getTypeTag = (type: string) => {
    const config: any = { MAIN: { color: 'blue', text: '主仓' }, RAW: { color: 'green', text: '原料仓' }, FINISHED: { color: 'orange', text: '成品仓' }, SEMI: { color: 'purple', text: '半成品仓' } };
    const c = config[type] || { color: 'default', text: type };
    return <Tag color={c.color}>{c.text}</Tag>;
  };

  const columns = [
    { title: '仓库编码', dataIndex: 'warehouseCode', key: 'warehouseCode' },
    { title: '仓库名称', dataIndex: 'warehouseName', key: 'warehouseName' },
    { title: '仓库类型', dataIndex: 'warehouseType', key: 'warehouseType', render: (t: string) => getTypeTag(t) },
    { title: '所属组织', dataIndex: 'orgName', key: 'orgName' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={s === 'ENABLED' ? 'green' : 'red'}>{s === 'ENABLED' ? '启用' : '停用'}</Tag> },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>多仓库管理</h1>
      
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}><Card><Statistic title="总仓库数" value={data.length} /></Card></Col>
        <Col span={6}><Card><Statistic title="主仓库" value={data.filter(d => d.warehouseType === 'MAIN').length} /></Card></Col>
        <Col span={6}><Card><Statistic title="原料仓" value={data.filter(d => d.warehouseType === 'RAW').length} /></Card></Col>
        <Col span={6}><Card><Statistic title="成品仓" value={data.filter(d => d.warehouseType === 'FINISHED').length} /></Card></Col>
      </Row>

      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} />
    </div>
  );
};

export default WarehousePage;
