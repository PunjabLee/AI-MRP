import { useState, useEffect } from 'react';
import { supplierApi, Supplier } from '../api/supplier';
import { message } from 'antd';

export function SupplierPage() {
  const [data, setData] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState({ keyword: '', status: '' });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const result = await supplierApi.list(searchParams);
      setData(result.list || []);
    } catch (error) {
      console.error('获取供应商失败', error);
      message.error('获取供应商失败');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('确定要删除吗?')) return;
    try {
      await supplierApi.delete(id);
      message.success('删除成功');
      fetchData();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const getStatusBadge = (status?: string) => {
    const statuses: Record<string, { color: string; text: string }> = {
      'ACTIVE': { color: 'green', text: '合作中' },
      'INACTIVE': { color: 'gray', text: '已停用' }
    };
    return statuses[status || ''] || { color: 'gray', text: status };
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">供应商管理</h1>
      </div>

      {/* 搜索栏 */}
      <div className="mb-4 bg-white p-4 rounded-lg shadow">
        <div className="flex gap-4">
          <input
            placeholder="供应商编码/名称"
            value={searchParams.keyword}
            onChange={e => setSearchParams({...searchParams, keyword: e.target.value})}
            className="border rounded px-3 py-2 flex-1"
          />
          <select
            value={searchParams.status}
            onChange={e => setSearchParams({...searchParams, status: e.target.value})}
            className="border rounded px-3 py-2"
          >
            <option value="">全部状态</option>
            <option value="ACTIVE">合作中</option>
            <option value="INACTIVE">已停用</option>
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
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">供应商编码</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">供应商名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">联系人</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">电话</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">交期(天)</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item) => {
              const statusBadge = getStatusBadge(item.status);
              return (
                <tr key={item.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.supplierCode}</td>
                  <td className="px-6 py-4">{item.supplierName}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.contact}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.phone}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.leadTimeDays}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs text-white bg-${statusBadge.color}-500`}>
                      {statusBadge.text}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <button className="text-blue-600 hover:underline mr-2">编辑</button>
                    <button onClick={() => item.id && handleDelete(item.id)} className="text-red-600 hover:underline">删除</button>
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
