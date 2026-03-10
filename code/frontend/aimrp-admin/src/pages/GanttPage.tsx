import { useState, useEffect } from 'react';

interface GanttTask {
  id: number;
  taskName: string;
  resource: string;
  startDate: string;
  endDate: string;
  progress: number;
  status: string;
}

export function GanttPage() {
  const [data, setData] = useState<GanttTask[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      // 调用排程API
      const response = await fetch('/api/schedule/optimize', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productionOrders: [], resources: [] })
      });
      const result = await response.json();
      
      if (result.schedule_details) {
        setData(result.schedule_details.map((item: any) => ({
          id: Math.random(),
          taskName: item.order_id,
          resource: item.resource_name,
          startDate: item.start_time,
          endDate: item.end_date,
          progress: 0,
          status: item.tardiness_hours > 0 ? 'delayed' : 'normal'
        })));
      }
    } catch (error) {
      console.error('获取排程数据失败', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">生产甘特图</h1>
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">订单</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">资源</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">开始时间</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">结束时间</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">进度</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item) => (
              <tr key={item.id}>
                <td className="px-6 py-4 whitespace-nowrap">{item.taskName}</td>
                <td className="px-6 py-4 whitespace-nowrap">{item.resource}</td>
                <td className="px-6 py-4 whitespace-nowrap">{item.startDate}</td>
                <td className="px-6 py-4 whitespace-nowrap">{item.endDate}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div className="bg-blue-600 h-2 rounded-full" style={{ width: `${item.progress}%` }}></div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className={`px-2 py-1 rounded text-xs ${item.status === 'delayed' ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'}`}>
                    {item.status === 'delayed' ? '延期' : '正常'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
