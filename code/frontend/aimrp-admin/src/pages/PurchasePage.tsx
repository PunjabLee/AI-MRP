import { useState, useEffect } from 'react';
import { purchaseApi, PurchaseOrder } from '../api/purchase';
import { message } from 'antd';

export function PurchasePage() {
  const [data, setData] = useState<PurchaseOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState({ supplierCode: '', status: '' });
  const [pagination, setPagination] = useState({ pageNum: 1, pageSize: 10, total: 0 });

  useEffect(() => {
    fetchData();
  }, [pagination.pageNum]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const result = await purchaseApi.list({
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        ...searchParams
      });
      setData(result.list || []);
      setPagination(prev => ({ ...prev, total: result.total || 0 }));
    } catch (error) {
      console.error('获取采购订单失败', error);
      message.error('获取采购订单失败');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async (id: number) => {
    try {
      await purchaseApi.confirm(id);
      message.success('确认成功');
      fetchData();
    } catch (error) {
      message.error('确认失败');
    }
  };

  const handleReceive = async (id: number) => {
    try {
      await purchaseApi.receive(id, { qty: 100 });
      message.success('入库成功');
      fetchData();
    } catch (error) {
      message.error('入库失败');
    }
  };

  const getStatusBadge = (status?: string) => {
    const statuses: Record<string, { color: string; text: string }> = {
      'DRAFT': { color: 'gray', text: '草稿' },
      'CONFIRMED': { color: 'blue', text: '已确认' },
      'PARTIAL_RECEIVED': { color: 'orange', text: '部分到货' },
      'RECEIVED': { color: 'green', text: '已到货' },
      'CANCELLED': { color: 'red', text: '已取消' }
    };
    return statuses[status || ''] || { color: 'gray', text: status };
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">采购订单</h1>
      </div>

      {/* 搜索栏 */}
      <div className="mb-4 bg-white p-4 rounded-lg shadow">
        <div className="flex gap-4">
          <input
            placeholder="供应商编码"
            value={searchParams.supplierCode}
            onChange={e => setSearchParams({...searchParams, supplierCode: e.target.value})}
            className="border rounded px-3 py-2"
          />
          <select
            value={searchParams.status}
            onChange={e => setSearchParams({...searchParams, status: e.target.value})}
            className="border rounded px-3 py-2"
          >
            <option value="">全部状态</option>
            <option value="DRAFT">草稿</option>
            <option value="CONFIRMED">已确认</option>
            <option value="RECEIVED">已到货</option>
          </select>
          <button onClick={fetchData} className="px-4 py-2 bg-blue-600 text-white rounded">
            查询
          </button>
        </div>
      </div>

      {/* 列表 */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">订单号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">供应商</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">订单日期</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">预计到货</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">金额</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item) => {
              const statusBadge = getStatusBadge(item.status);
              return (
                <tr key={item.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.poNo}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.supplierName}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.orderDate}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.expectDate}</td>
                  <td className="px-6 py-4 whitespace-nowrap">¥{item.totalAmount}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs text-white bg-${statusBadge.color}-500`}>
                      {statusBadge.text}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {item.status === 'DRAFT' && (
                      <button onClick={() => item.id && handleConfirm(item.id)} className="text-blue-600 hover:underline mr-2">
                        确认
                      </button>
                    )}
                    {item.status === 'CONFIRMED' && (
                      <button onClick={() => item.id && handleReceive(item.id)} className="text-green-600 hover:underline">
                        入库
                      </button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
