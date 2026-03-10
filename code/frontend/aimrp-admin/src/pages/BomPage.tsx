import { useState, useEffect } from 'react';
import { bomApi, BomItem } from '../api/bom';
import { message } from 'antd';

export function BomPage() {
  const [data, setData] = useState<BomItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState({ parentItemCode: '' });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const result = await bomApi.list(searchParams);
      setData(result.list || []);
    } catch (error) {
      console.error('获取BOM失败', error);
      message.error('获取BOM失败');
    } finally {
      setLoading(false);
    }
  };

  const handleExpand = async (itemCode: string) => {
    try {
      const result = await bomApi.expand(itemCode, 3);
      message.success(`展开成功，共${result.length}个子物料`);
    } catch (error) {
      message.error('展开失败');
    }
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">BOM管理</h1>
      </div>

      {/* 搜索栏 */}
      <div className="mb-4 bg-white p-4 rounded-lg shadow">
        <div className="flex gap-4">
          <input
            placeholder="父物料编码"
            value={searchParams.parentItemCode}
            onChange={e => setSearchParams({...searchParams, parentItemCode: e.target.value})}
            className="border rounded px-3 py-2 flex-1"
          />
          <button onClick={fetchData} className="px-4 py-2 bg-blue-600 text-white rounded">
            查询
          </button>
        </div>
      </div>

      {/* BOM列表 */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">父物料</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">子物料</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">用量</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">损耗率</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">层级</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item, idx) => (
              <tr key={idx}>
                <td className="px-6 py-4 whitespace-nowrap">{item.parentItemCode}</td>
                <td className="px-6 py-4 whitespace-nowrap">{item.childItemCode}</td>
                <td className="px-6 py-4 whitespace-nowrap">{item.usageQty}</td>
                <td className="px-6 py-4 whitespace-nowrap">{item.lossRate}%</td>
                <td className="px-6 py-4 whitespace-nowrap">{item.level}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <button 
                    onClick={() => item.childItemCode && handleExpand(item.childItemCode)}
                    className="text-blue-600 hover:underline"
                  >
                    展开
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
