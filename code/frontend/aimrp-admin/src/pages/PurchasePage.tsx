import { useState, useEffect } from 'react';
import { Table, Button, Space, Input, Modal, Form, message, Tag, Steps } from 'antd';
import { PlusOutlined, EyeOutlined } from '@ant-design/icons';

interface PurchaseOrder {
  id: number;
  orderNo: string;
  supplierCode: string;
  supplierName: string;
  itemCode: string;
  itemName: string;
  qty: number;
  unitPrice: number;
  totalAmount: number;
  status: string;
  createDate: string;
  expectedDate: string;
}

const PurchasePage: React.FC = () => {
  const [data, setData] = useState<PurchaseOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentOrder, setCurrentOrder] = useState<PurchaseOrder | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockData: PurchaseOrder[] = [
        { id: 1, orderNo: 'PO202403090001', supplierCode: 'S001', supplierName: '供应商A', itemCode: 'C001', itemName: '物料C', qty: 1000, unitPrice: 10, totalAmount: 10000, status: 'PENDING', createDate: '2024-03-09', expectedDate: '2024-03-15' },
        { id: 2, orderNo: 'PO202403090002', supplierCode: 'S002', supplierName: '供应商B', itemCode: 'C002', itemName: '物料D', qty: 500, unitPrice: 20, totalAmount: 10000, status: 'CONFIRMED', createDate: '2024-03-08', expectedDate: '2024-03-14' },
        { id: 3, orderNo: 'PO202403090003', supplierCode: 'S001', supplierName: '供应商A', itemCode: 'C003', itemName: '物料E', qty: 2000, unitPrice: 5, totalAmount: 10000, status: 'RECEIVED', createDate: '2024-03-07', expectedDate: '2024-03-12' },
      ];
      setData(mockData);
    } catch (error) {
      message.error('加载失败');
    } finally {
      setLoading(false);
    }
  };

  const handleView = (record: PurchaseOrder) => {
    setCurrentOrder(record);
    setDetailVisible(true);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return 'orange';
      case 'CONFIRMED': return 'blue';
      case 'RECEIVED': return 'green';
      case 'CANCELLED': return 'red';
      default: return 'default';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PENDING': return '待确认';
      case 'CONFIRMED': return '已确认';
      case 'RECEIVED': return '已入库';
      case 'CANCELLED': return '已取消';
      default: return status;
    }
  };

  const filteredData = data.filter(item =>
    item.orderNo.includes(searchText) ||
    item.supplierName.includes(searchText) ||
    item.itemCode.includes(searchText)
  );

  const columns = [
    { title: '采购单号', dataIndex: 'orderNo', key: 'orderNo' },
    { title: '供应商编码', dataIndex: 'supplierCode', key: 'supplierCode' },
    { title: '供应商名称', dataIndex: 'supplierName', key: 'supplierName' },
    { title: '物料编码', dataIndex: 'itemCode', key: 'itemCode' },
    { title: '物料名称', dataIndex: 'itemName', key: 'itemName' },
    { title: '数量', dataIndex: 'qty', key: 'qty' },
    { title: '单价', dataIndex: 'unitPrice', key: 'unitPrice' },
    { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount' },
    { 
      title: '状态', 
      dataIndex: 'status', 
      key: 'status',
      render: (status: string) => <Tag color={getStatusColor(status)}>{getStatusText(status)}</Tag>
    },
    { title: '创建日期', dataIndex: 'createDate', key: 'createDate' },
    { title: '预计到货日期', dataIndex: 'expectedDate', key: 'expectedDate' },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: PurchaseOrder) => (
        <Space>
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleView(record)}>查看</Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>采购管理</h1>
      <Space style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder="搜索采购单号/供应商/物料"
          onSearch={setSearchText}
          style={{ width: 300 }}
        />
        <Button type="primary" icon={<PlusOutlined />}>新建采购</Button>
      </Space>
      <Table columns={columns} dataSource={filteredData} rowKey="id" loading={loading} />

      <Modal
        title={`采购单详情 - ${currentOrder?.orderNo}`}
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentOrder && (
          <>
            <Steps
              current={currentOrder.status === 'PENDING' ? 0 : currentOrder.status === 'CONFIRMED' ? 1 : 2}
              items={[
                { title: '创建' },
                { title: '确认' },
                { title: '入库' },
              ]}
              style={{ marginBottom: 24 }}
            />
            <p><strong>采购单号：</strong>{currentOrder.orderNo}</p>
            <p><strong>供应商：</strong>{currentOrder.supplierName}</p>
            <p><strong>物料：</strong>{currentOrder.itemName}</p>
            <p><strong>数量：</strong>{currentOrder.qty}</p>
            <p><strong>单价：</strong>{currentOrder.unitPrice}</p>
            <p><strong>总金额：</strong>{currentOrder.totalAmount}</p>
            <p><strong>创建日期：</strong>{currentOrder.createDate}</p>
            <p><strong>预计到货：</strong>{currentOrder.expectedDate}</p>
          </>
        )}
      </Modal>
    </div>
  );
};

export default PurchasePage;
