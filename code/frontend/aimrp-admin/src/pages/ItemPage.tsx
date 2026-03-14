import { useState, useEffect } from 'react';
import { itemApi, Item } from '../api/item';
import { message } from 'antd';

export function ItemPage() {
  const [data, setData] = useState<Item[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState({ keyword: '', itemType: '' });
  const [pagination, setPagination] = useState({ pageNum: 1, pageSize: 10, total: 0 });

  useEffect(() => {
    fetchData();
  }, [pagination.pageNum]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const result = await itemApi.list({
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        ...searchParams
      });
      setData(result.list || []);
      setPagination(prev => ({ ...prev, total: result.total || 0 }));
    } catch (error) {
      console.error('获取物料失败', error);
      message.error('获取物料失败');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('确定要删除吗?')) return;
    try {
      await itemApi.delete(id);
      message.success('删除成功');
      fetchData();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const getTypeBadge = (type?: string) => {
    const types: Record<string, { color: string; text: string }> = {
      'FINISHED': { color: 'blue', text: '成品' },
      'SEMI': { color: 'orange', text: '半成品' },
      'RAW': { color: 'green', text: '原材料' }
    };
    return types[type || ''] || { color: 'gray', text: type };
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">物料主数据</h1>
      </div>

      {/* 搜索栏 */}
      <div className="mb-4 bg-white p-4 rounded-lg shadow">
        <div className="flex gap-4">
          <input
            placeholder="物料编码/名称"
            value={searchParams.keyword}
            onChange={e => setSearchParams({...searchParams, keyword: e.target.value})}
            className="border rounded px-3 py-2 flex-1"
          />
          <select
            value={searchParams.itemType}
            onChange={e => setSearchParams({...searchParams, itemType: e.target.value})}
            className="border rounded px-3 py-2"
          >
            <option value="">全部类型</option>
            <option value="FINISHED">成品</option>
            <option value="SEMI">半成品</option>
            <option value="RAW">原材料</option>
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
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料编码</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">单位</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">来源</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">提前期</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item) => {
              const typeBadge = getTypeBadge(item.itemType);
              return (
                <tr key={item.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.itemCode}</td>
                  <td className="px-6 py-4">{item.itemName}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs text-white bg-${typeBadge.color}-500`}>
                      {typeBadge.text}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.unit}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.source}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.leadTime}天</td>
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
