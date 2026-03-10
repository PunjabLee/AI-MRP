import { useState, useEffect } from 'react';

interface Inspection {
  id: number;
  inspectionNo: string;
  inspectionType: string;
  sourceNo: string;
  itemCode: string;
  itemName: string;
  inspectionQty: number;
  qualifiedQty: number;
  qualifiedRate: number;
  result: string;
  inspector: string;
  status: string;
}

export function QualityPage() {
  const [data, setData] = useState<Inspection[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState({ inspectionType: '', status: '' });
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState<Partial<Inspection>>({});

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (searchParams.inspectionType) params.append('inspectionType', searchParams.inspectionType);
      if (searchParams.status) params.append('status', searchParams.status);
      
      const res = await fetch(`/api/quality/list?${params}`);
      const result = await res.json();
      if (result.code === 200) {
        setData(result.data?.list || []);
      }
    } catch (error) {
      console.error('获取检验单失败', error);
    }
    setLoading(false);
  };

  const handleCreate = async () => {
    try {
      await fetch('/api/quality', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });
      setShowModal(false);
      setFormData({});
      fetchData();
    } catch (error) {
      console.error('创建失败', error);
    }
  };

  const getTypeBadge = (type: string) => {
    const types: Record<string, { color: string; text: string }> = {
      'INCOMING': { color: 'blue', text: '来料' },
      'PROCESS': { color: 'orange', text: '过程' },
      'FINAL': { color: 'green', text: '成品' }
    };
    return types[type] || { color: 'gray', text: type };
  };

  const getResultBadge = (result: string) => {
    const results: Record<string, { color: string; text: string }> = {
      'QUALIFIED': { color: 'green', text: '合格' },
      'UNQUALIFIED': { color: 'red', text: '不合格' }
    };
    return results[result] || { color: 'gray', text: result || '待检验' };
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">质量管理</h1>
        <button
          onClick={() => setShowModal(true)}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          新增检验单
        </button>
      </div>

      {/* 统计卡片 */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="text-2xl font-bold text-blue-600">100</div>
          <div className="text-gray-500">总检验数</div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="text-2xl font-bold text-green-600">95</div>
          <div className="text-gray-500">合格数</div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="text-2xl font-bold text-red-600">5</div>
          <div className="text-gray-500">不合格数</div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="text-2xl font-bold text-green-600">95%</div>
          <div className="text-gray-500">合格率</div>
        </div>
      </div>

      {/* 搜索栏 */}
      <div className="mb-4 bg-white p-4 rounded-lg shadow">
        <div className="flex gap-4">
          <select
            value={searchParams.inspectionType}
            onChange={e => setSearchParams({...searchParams, inspectionType: e.target.value})}
            className="border rounded px-3 py-2"
          >
            <option value="">全部类型</option>
            <option value="INCOMING">来料检验</option>
            <option value="PROCESS">过程检验</option>
            <option value="FINAL">成品检验</option>
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
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">检验单号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">来源单号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">检验数量</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">合格率</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">结果</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">检验员</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item) => {
              const typeBadge = getTypeBadge(item.inspectionType);
              const resultBadge = getResultBadge(item.result);
              return (
                <tr key={item.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.inspectionNo}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs text-white bg-${typeBadge.color}-500`}>
                      {typeBadge.text}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.sourceNo}</td>
                  <td className="px-6 py-4">
                    {item.itemCode}<br/>
                    <span className="text-gray-500 text-sm">{item.itemName}</span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.inspectionQty}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.qualifiedRate || 0}%</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs text-white bg-${resultBadge.color}-500`}>
                      {resultBadge.text}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.inspector || '-'}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* 创建弹窗 */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">新增检验单</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">检验类型</label>
                <select
                  value={formData.inspectionType || ''}
                  onChange={e => setFormData({...formData, inspectionType: e.target.value})}
                  className="w-full px-3 py-2 border rounded-lg"
                >
                  <option value="">请选择</option>
                  <option value="INCOMING">来料检验</option>
                  <option value="PROCESS">过程检验</option>
                  <option value="FINAL">成品检验</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">来源单号</label>
                <input
                  type="text"
                  value={formData.sourceNo || ''}
                  onChange={e => setFormData({...formData, sourceNo: e.target.value})}
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">物料编码</label>
                <input
                  type="text"
                  value={formData.itemCode || ''}
                  onChange={e => setFormData({...formData, itemCode: e.target.value})}
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">检验数量</label>
                <input
                  type="number"
                  value={formData.inspectionQty || ''}
                  onChange={e => setFormData({...formData, inspectionQty: Number(e.target.value)})}
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
            </div>
            <div className="flex justify-end gap-2 mt-6">
              <button onClick={() => setShowModal(false)} className="px-4 py-2 border rounded-lg">
                取消
              </button>
              <button onClick={handleCreate} className="px-4 py-2 bg-blue-600 text-white rounded-lg">
                创建
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
