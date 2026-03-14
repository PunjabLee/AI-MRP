import { useState, useEffect } from 'react';
import { mpsApi } from '../api/mps';

interface MpsPlan {
  id: number;
  planNo: string;
  itemCode: string;
  itemName: string;
  planQty: number;
  scheduledQty: number;
  startDate: string;
  endDate: string;
  status: string;
}

export function MpsPage() {
  const [data, setData] = useState<MpsPlan[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const result = await mpsApi.list({});
      setData(result.list || []);
    } catch (error) {
      console.error('获取MPS计划失败', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    const statuses: Record<string, { color: string; text: string }> = {
      'DRAFT': { color: 'gray', text: '草稿' },
      'RELEASED': { color: 'blue', text: '已下达' },
      'COMPLETED': { color: 'green', text: '已完成' }
    };
    return statuses[status] || { color: 'gray', text: status };
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">主生产计划(MPS)</h1>
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">计划编号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">计划数量</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">已排产</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">开始日期</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">结束日期</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item) => {
              const statusBadge = getStatusBadge(item.status);
              return (
                <tr key={item.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.planNo}</td>
                  <td className="px-6 py-4">{item.itemCode}<br/><span className="text-gray-500">{item.itemName}</span></td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.planQty}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.scheduledQty || 0}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.startDate}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.endDate}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs text-white bg-${statusBadge.color}-500`}>
                      {statusBadge.text}
                    </span>
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
