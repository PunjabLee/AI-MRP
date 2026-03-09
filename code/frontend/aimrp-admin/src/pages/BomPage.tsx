/**
 * BOM 页面
 */
import { useState, useEffect } from 'react';
import { bomApi, Bom, BomLine } from '../api/bom';

export function BomPage() {
  const [boms, setBoms] = useState<Bom[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedBom, setSelectedBom] = useState<{bom: Bom; lines: BomLine[]} | null>(null);
  const [expandLoading, setExpandLoading] = useState(false);

  useEffect(() => {
    loadBoms();
  }, []);

  const loadBoms = async () => {
    setLoading(true);
    try {
      const result = await bomApi.list({});
      setBoms(result.list || []);
    } catch (error) {
      console.error('加载失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetail = async (id: number) => {
    try {
      const result = await bomApi.getById(id);
      setSelectedBom(result);
    } catch (error) {
      console.error('加载详情失败:', error);
    }
  };

  const handleExpand = async (itemCode: string) => {
    setExpandLoading(true);
    try {
      const result = await bomApi.expand(itemCode, 1);
      console.log('展开结果:', result);
      alert(`展开成功，共 ${result.total} 个子物料`);
    } catch (error) {
      console.error('展开失败:', error);
    } finally {
      setExpandLoading(false);
    }
  };

  const getStatusBadge = (status?: string) => {
    const colors: Record<string, string> = {
      'DRAFT': 'bg-gray-100 text-gray-800',
      'ACTIVE': 'bg-green-100 text-green-800',
      'OBSOLETE': 'bg-red-100 text-red-800',
    };
    return colors[status || ''] || 'bg-gray-100 text-gray-800';
  };

  return (
    <div>
      <div className="mb-4 flex justify-between items-center">
        <h2 className="text-2xl font-bold">BOM 管理</h2>
        <button className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
          新建 BOM
        </button>
      </div>

      {/* BOM 列表 */}
      <div className="bg-white rounded shadow overflow-hidden mb-6">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">BOM编号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料编码</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">版本</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">生效日期</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {loading ? (
              <tr><td colSpan={7} className="text-center py-8">加载中...</td></tr>
            ) : boms.length === 0 ? (
              <tr><td colSpan={7} className="text-center py-8">暂无数据</td></tr>
            ) : (
              boms.map((bom) => (
                <tr key={bom.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">{bom.bomNo}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{bom.itemCode}</td>
                  <td className="px-6 py-4">{bom.itemName}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{bom.version}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{bom.effectiveDate}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded text-xs ${getStatusBadge(bom.status)}`}>
                      {bom.status}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <button 
                      onClick={() => bom.id && handleViewDetail(bom.id)}
                      className="text-blue-600 hover:text-blue-800 mr-3"
                    >
                      详情
                    </button>
                    <button 
                      onClick={() => handleExpand(bom.itemCode || '')}
                      className="text-green-600 hover:text-green-800 mr-3"
                    >
                      展开
                    </button>
                    <button className="text-gray-600 hover:text-gray-800">
                      编辑
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* BOM 详情弹窗 */}
      {selectedBom && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg w-3/4 max-h-[80vh] overflow-hidden">
            <div className="p-4 border-b flex justify-between items-center">
              <h3 className="text-lg font-bold">BOM 详情 - {selectedBom.bom.bomNo}</h3>
              <button 
                onClick={() => setSelectedBom(null)}
                className="text-gray-500 hover:text-gray-700"
              >
                ✕
              </button>
            </div>
            <div className="p-4 overflow-auto max-h-[60vh]">
              <div className="mb-4 grid grid-cols-2 gap-4">
                <div>
                  <span className="text-gray-500">物料编码：</span>
                  <span>{selectedBom.bom.itemCode}</span>
                </div>
                <div>
                  <span className="text-gray-500">版本：</span>
                  <span>{selectedBom.bom.version}</span>
                </div>
              </div>
              
              <h4 className="font-medium mb-2">BOM 行</h4>
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">序号</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">子物料编码</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">子物料名称</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">用量</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">损耗率</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {selectedBom.lines.map((line, idx) => (
                    <tr key={line.id}>
                      <td className="px-4 py-2">{idx + 1}</td>
                      <td className="px-4 py-2">{line.childItemCode}</td>
                      <td className="px-4 py-2">{line.childItemName}</td>
                      <td className="px-4 py-2">{line.usageQty}</td>
                      <td className="px-4 py-2">{line.lossRate ? `${(Number(line.lossRate) * 100).toFixed(1)}%` : '0%'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
