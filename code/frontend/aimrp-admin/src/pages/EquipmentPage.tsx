import { useState, useEffect } from 'react';

interface Equipment {
  id: number;
  equipmentCode: string;
  equipmentName: string;
  equipmentType: string;
  spec?: string;
  workshopCode?: string;
  workCenterCode?: string;
  status: string;
  capacity?: number;
  responsible?: string;
}

export function EquipmentPage() {
  const [data, setData] = useState<Equipment[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState({ status: '', equipmentType: '' });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (searchParams.status) params.append('status', searchParams.status);
      if (searchParams.equipmentType) params.append('equipmentType', searchParams.equipmentType);
      
      const res = await fetch(`/api/equipment/list?${params}`);
      const result = await res.json();
      if (result.code === 200) {
        setData(result.data?.list || []);
      }
    } catch (error) {
      console.error('获取设备失败', error);
    }
    setLoading(false);
  };

  const handleReportFault = async (id: number) => {
    try {
      await fetch(`/api/equipment/${id}/report-fault`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ remark: '设备故障' })
      });
      fetchData();
    } catch (error) {
      console.error('报修失败', error);
    }
  };

  const handleRepairComplete = async (id: number) => {
    try {
      await fetch(`/api/equipment/${id}/repair-complete`, { method: 'POST' });
      fetchData();
    } catch (error) {
      console.error('维修完成失败', error);
    }
  };

  const getStatusBadge = (status: string) => {
    const statuses: Record<string, { color: string; text: string }> = {
      'IDLE': { color: 'gray', text: '空闲' },
      'RUNNING': { color: 'green', text: '运行中' },
      'MAINTENANCE': { color: 'yellow', text: '维护中' },
      'BROKEN': { color: 'red', text: '故障' }
    };
    return statuses[status] || { color: 'gray', text: status };
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">设备管理</h1>
      </div>

      {/* 搜索栏 */}
      <div className="mb-4 bg-white p-4 rounded-lg shadow">
        <div className="flex gap-4">
          <select
            value={searchParams.status}
            onChange={e => setSearchParams({...searchParams, status: e.target.value})}
            className="border rounded px-3 py-2"
          >
            <option value="">全部状态</option>
            <option value="IDLE">空闲</option>
            <option value="RUNNING">运行中</option>
            <option value="MAINTENANCE">维护中</option>
            <option value="BROKEN">故障</option>
          </select>
          <select
            value={searchParams.equipmentType}
            onChange={e => setSearchParams({...searchParams, equipmentType: e.target.value})}
            className="border rounded px-3 py-2"
          >
            <option value="">全部类型</option>
            <option value="MACHINE">机器</option>
            <option value="TOOL">工装</option>
            <option value="FIXTURE">夹具</option>
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
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">设备编码</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">设备名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">规格</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">产能</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">负责人</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item) => {
              const statusBadge = getStatusBadge(item.status);
              return (
                <tr key={item.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.equipmentCode}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.equipmentName}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.equipmentType}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.spec || '-'}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.capacity || '-'}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded-full text-xs text-white bg-${statusBadge.color}-500`}>
                      {statusBadge.text}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.responsible || '-'}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {item.status === 'RUNNING' && (
                      <button 
                        onClick={() => handleReportFault(item.id)}
                        className="text-red-600 hover:underline mr-2"
                      >
                        报修
                      </button>
                    )}
                    {item.status === 'BROKEN' && (
                      <button 
                        onClick={() => handleRepairComplete(item.id)}
                        className="text-green-600 hover:underline"
                      >
                        维修完成
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
