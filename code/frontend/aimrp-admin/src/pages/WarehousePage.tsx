import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';

interface Warehouse {
  id: number;
  warehouseCode: string;
  warehouseName: string;
  warehouseType: string;
  status: string;
  address?: string;
}

export function WarehousePage() {
  const [data, setData] = useState<Warehouse[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams] = useSearchParams();
  const [formData, setFormData] = useState({ warehouseType: '', status: '' });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      // 调用API
      const response = await fetch(`/api/warehouses/list?warehouseType=${formData.warehouseType}&status=${formData.status}`);
      const result = await response.json();
      
      if (result.code === 200) {
        setData(result.data?.list || []);
      }
    } catch (error) {
      console.error('获取仓库失败', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('确定要删除吗?')) return;
    try {
      await fetch(`/api/warehouses/${id}`, { method: 'DELETE' });
      fetchData();
    } catch (error) {
      console.error('删除失败', error);
    }
  };

  const getTypeBadge = (type: string) => {
    const types: Record<string, { color: string; text: string }> = {
      'MAIN': { color: 'blue', text: '主仓库' },
      'RAW': { color: 'green', text: '原料仓' },
      'FINISHED': { color: 'orange', text: '成品仓' }
    };
    return types[type] || { color: 'gray', text: type };
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">仓库管理</h1>
      </div>

      {/* 列表 */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">仓库编码</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">仓库名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">地址</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item) => {
              const typeBadge = getTypeBadge(item.warehouseType);
              return (
                <tr key={item.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.warehouseCode}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.warehouseName}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs text-white bg-${typeBadge.color}-500`}>
                      {typeBadge.text}
                    </span>
                  </td>
                  <td className="px-6 py-4">{item.address || '-'}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs ${item.status === 'ENABLED' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                      {item.status === 'ENABLED' ? '启用' : '停用'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <button className="text-blue-600 hover:underline mr-2">编辑</button>
                    <button onClick={() => handleDelete(item.id)} className="text-red-600 hover:underline">删除</button>
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
