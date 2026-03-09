/**
 * 订单页面
 */
import { useState, useEffect } from 'react';
import { orderApi, Order } from '../api/order';

export function OrderPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({ pageNum: 1, pageSize: 10, total: 0 });

  useEffect(() => {
    loadOrders();
  }, [pagination.pageNum]);

  const loadOrders = async () => {
    setLoading(true);
    try {
      const result = await orderApi.list({ 
        pageNum: pagination.pageNum, 
        pageSize: pagination.pageSize 
      });
      setOrders(result.list || []);
      setPagination(prev => ({ ...prev, total: result.total || 0 }));
    } catch (error) {
      console.error('加载失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async (id: number) => {
    try {
      await orderApi.confirm(id);
      loadOrders();
    } catch (error) {
      console.error('确认失败:', error);
    }
  };

  const getStatusBadge = (status?: string) => {
    const colors: Record<string, string> = {
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'CONFIRMED': 'bg-green-100 text-green-800',
      'CANCELLED': 'bg-red-100 text-red-800',
    };
    return colors[status || ''] || 'bg-gray-100 text-gray-800';
  };

  return (
    <div>
      <div className="mb-4 flex justify-between items-center">
        <h2 className="text-2xl font-bold">销售订单</h2>
        <button className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
          新建订单
        </button>
      </div>

      {/* 搜索栏 */}
      <div className="mb-4 bg-white p-4 rounded shadow">
        <div className="flex gap-4">
          <input
            type="text"
            placeholder="订单号"
            className="px-3 py-2 border rounded"
          />
          <input
            type="text"
            placeholder="客户编码"
            className="px-3 py-2 border rounded"
          />
          <button className="px-4 py-2 bg-blue-600 text-white rounded">
            搜索
          </button>
        </div>
      </div>

      {/* 表格 */}
      <div className="bg-white rounded shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">订单号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">客户</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">订单日期</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">交货日期</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">金额</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {loading ? (
              <tr><td colSpan={7} className="text-center py-8">加载中...</td></tr>
            ) : orders.length === 0 ? (
              <tr><td colSpan={7} className="text-center py-8">暂无数据</td></tr>
            ) : (
              orders.map((order) => (
                <tr key={order.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">{order.orderNo}</td>
                  <td className="px-6 py-4">{order.customerName}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{order.orderDate}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{order.dueDate}</td>
                  <td className="px-6 py-4">{order.totalAmount}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded text-xs ${getStatusBadge(order.status)}`}>
                      {order.status}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <button 
                      onClick={() => order.id && handleConfirm(order.id)}
                      className="text-blue-600 hover:text-blue-800 mr-3"
                    >
                      确认
                    </button>
                    <button className="text-gray-600 hover:text-gray-800 mr-3">
                      编辑
                    </button>
                    <button className="text-red-600 hover:text-red-800">
                      删除
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* 分页 */}
      <div className="mt-4 flex justify-end gap-2">
        <button 
          onClick={() => setPagination(p => ({ ...p, pageNum: p.pageNum - 1 }))}
          disabled={pagination.pageNum === 1}
          className="px-3 py-1 border rounded disabled:opacity-50"
        >
          上一页
        </button>
        <span className="px-3 py-1">
          第 {pagination.pageNum} 页 / 共 {Math.ceil(pagination.total / pagination.pageSize)} 页
        </span>
        <button 
          onClick={() => setPagination(p => ({ ...p, pageNum: p.pageNum + 1 }))}
          disabled={pagination.pageNum * pagination.pageSize >= pagination.total}
          className="px-3 py-1 border rounded disabled:opacity-50"
        >
          下一页
        </button>
      </div>
    </div>
  );
}
