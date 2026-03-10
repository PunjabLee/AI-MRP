import { useState, useEffect } from 'react';

interface CostElement {
  id: number;
  costCode: string;
  costName: string;
  costType: string;
  unitCost: number;
  unit: string;
}

export function CostPage() {
  const [data, setData] = useState<CostElement[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await fetch('/api/cost/elements');
      const result = await response.json();
      
      if (result.code === 200) {
        setData(result.data?.list || []);
      }
    } catch (error) {
      console.error('获取成本要素失败', error);
    } finally {
      setLoading(false);
    }
  };

  const getTypeBadge = (type: string) => {
    const types: Record<string, { color: string; text: string }> = {
      'MATERIAL': { color: 'blue', text: '材料' },
      'LABOR': { color: 'green', text: '人工' },
      'OVERHEAD': { color: 'orange', text: '制造费用' }
    };
    return types[type] || { color: 'gray', text: type };
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">成本管理</h1>
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">成本要素编码</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">成本要素名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">单位成本</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">单位</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item) => {
              const typeBadge = getTypeBadge(item.costType);
              return (
                <tr key={item.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.costCode}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.costName}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs text-white bg-${typeBadge.color}-500`}>
                      {typeBadge.text}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">¥{item.unitCost}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.unit}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
