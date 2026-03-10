import { useState, useEffect } from 'react';

interface Report {
  id: number;
  reportCode: string;
  reportName: string;
  reportType: string;
  status: string;
}

export function ReportPage() {
  const [data, setData] = useState<Report[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await fetch('/api/reports/configs');
      const result = await response.json();
      
      if (result.code === 200) {
        setData(result.data?.list || []);
      }
    } catch (error) {
      console.error('获取报表失败', error);
    } finally {
      setLoading(false);
    }
  };

  const getTypeBadge = (type: string) => {
    const types: Record<string, { color: string; text: string }> = {
      'INVENTORY': { color: 'blue', text: '库存' },
      'SALES': { color: 'green', text: '销售' },
      'PRODUCTION': { color: 'orange', text: '生产' },
      'PURCHASE': { color: 'purple', text: '采购' },
      'MRP': { color: 'cyan', text: 'MRP' }
    };
    return types[type] || { color: 'gray', text: type };
  };

  const executeReport = async (id: number) => {
    try {
      const response = await fetch(`/api/reports/execute/${id}`, { method: 'POST' });
      const result = await response.json();
      
      if (result.code === 200) {
        alert('报表执行成功');
      }
    } catch (error) {
      console.error('执行报表失败', error);
    }
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">报表中心</h1>
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">报表编码</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">报表名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item) => {
              const typeBadge = getTypeBadge(item.reportType);
              return (
                <tr key={item.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.reportCode}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.reportName}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs text-white bg-${typeBadge.color}-500`}>
                      {typeBadge.text}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs ${item.status === 'ENABLED' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                      {item.status === 'ENABLED' ? '启用' : '停用'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <button onClick={() => executeReport(item.id)} className="text-blue-600 hover:underline mr-2">
                      执行
                    </button>
                    <button className="text-green-600 hover:underline">查看</button>
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
