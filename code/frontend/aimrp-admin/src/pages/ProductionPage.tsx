import { useState, useEffect } from 'react';
import { productionApi, ProductionOrder } from '../api/production';
import { message } from 'antd';

export function ProductionPage() {
  const [data, setData] = useState<ProductionOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState({ itemCode: '', status: '' });
  const [pagination, setPagination] = useState({ pageNum: 1, pageSize: 10, total: 0 });

  useEffect(() => {
    fetchData();
  }, [pagination.pageNum]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const result = await productionApi.list({
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        ...searchParams
      });
      setData(result.list || []);
      setPagination(prev => ({ ...prev, total: result.total || 0 }));
    } catch (error) {
      console.error('获取生产工单失败', error);
      message.error('获取生产工单失败');
    } finally {
      setLoading(false);
    }
  };

  const handleRelease = async (id: number) => {
    try {
      await productionApi.release(id);
      message.success('下达成功');
      fetchData();
    } catch (error) {
      message.error('下达失败');
    }
  };

  const handleStart = async (id: number) => {
    try {
      await productionApi.start(id);
      message.success('开始生产');
      fetchData();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleComplete = async (id: number) => {
    try {
      await productionApi.complete(id, { completedQty: 100 });
      message.success('完工成功');
      fetchData();
    } catch (error) {
      message.error('完工失败');
    }
  };

  const getStatusBadge = (status?: string) => {
    const statuses: Record<string, { color: string; text: string }> = {
      'DRAFT': { color: 'gray', text: '草稿' },
      'RELEASED': { color: 'blue', text: '已下达' },
      'PROCESSING': { color: 'orange', text: '生产中' },
      'COMPLETED': { color: 'green', text: '已完成' },
      'CANCELLED': { color: 'red', text: '已取消' }
    };
    return statuses[status || ''] || { color: 'gray', text: status };
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">生产工单</h1>
      </div>

      {/* 搜索栏 */}
      <div className="mb-4 bg-white p-4 rounded-lg shadow">
        <div className="flex gap-4">
          <input
            placeholder="物料编码"
            value={searchParams.itemCode}
            onChange={e => setSearchParams({...searchParams, itemCode: e.target.value})}
            className="border rounded px-3 py-2"
          />
          <select
            value={searchParams.status}
            onChange={e => setSearchParams({...searchParams, status: e.target.value})}
            className="border rounded px-3 py-2"
          >
            <option value="">全部状态</option>
            <option value="DRAFT">草稿</option>
            <option value="RELEASED">已下达</option>
            <option value="PROCESSING">生产中</option>
            <option value="COMPLETED">已完成</option>
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
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">工单号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">计划数量</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">已完成</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">开始日期</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item) => {
              const statusBadge = getStatusBadge(item.status);
              return (
                <tr key={item.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.moNo}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.itemCode}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.planQty}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.completedQty || 0}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.startDate}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs text-white bg-${statusBadge.color}-500`}>
                      {statusBadge.text}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {item.status === 'DRAFT' && (
                      <button onClick={() => item.id && handleRelease(item.id)} className="text-blue-600 hover:underline mr-2">
                        下达
                      </button>
                    )}
                    {item.status === 'RELEASED' && (
                      <button onClick={() => item.id && handleStart(item.id)} className="text-green-600 hover:underline mr-2">
                        开始
                      </button>
                    )}
                    {item.status === 'PROCESSING' && (
                      <button onClick={() => item.id && handleComplete(item.id)} className="text-orange-600 hover:underline">
                        完工
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
