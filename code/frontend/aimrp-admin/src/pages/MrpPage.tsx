/**
 * MRP 页面
 */
import { useState } from 'react';
import { mrpApi } from '../api/mrp';

export function MrpPage() {
  const [running, setRunning] = useState(false);
  const [result, setResult] = useState<any>(null);

  const handleRunMrp = async () => {
    setRunning(true);
    try {
      const data = await mrpApi.run({ runType: 'MANUAL' });
      setResult(data);
    } catch (error) {
      console.error('MRP运行失败:', error);
    } finally {
      setRunning(false);
    }
  };

  return (
    <div>
      <div className="mb-6">
        <h2 className="text-2xl font-bold mb-4">MRP 计算</h2>
        
        <div className="bg-white p-6 rounded shadow">
          <div className="flex items-center justify-between mb-6">
            <div>
              <p className="text-gray-600">点击下方按钮执行 MRP 计算</p>
              <p className="text-sm text-gray-400 mt-1">系统将根据销售订单、BOM和库存自动计算采购/生产建议</p>
            </div>
            <button
              onClick={handleRunMrp}
              disabled={running}
              className={`px-6 py-3 rounded font-medium ${
                running 
                  ? 'bg-gray-400 cursor-not-allowed' 
                  : 'bg-blue-600 hover:bg-blue-700 text-white'
              }`}
            >
              {running ? '计算中...' : '执行 MRP'}
            </button>
          </div>

          {/* 参数配置 */}
          <div className="border-t pt-4 mt-4">
            <h3 className="font-medium mb-3">计算参数</h3>
            <div className="grid grid-cols-3 gap-4 text-sm">
              <div>
                <span className="text-gray-500">计划展望期：</span>
                <span className="ml-2">90 天</span>
              </div>
              <div>
                <span className="text-gray-500">计算模式：</span>
                <span className="ml-2">逐层展开</span>
              </div>
              <div>
                <span className="text-gray-500">批量规则：</span>
                <span className="ml-2">LOT_FOR_LOT</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 结果展示 */}
      {result && (
        <div className="bg-white p-6 rounded shadow">
          <h3 className="font-medium mb-4">计算结果</h3>
          
          <div className="grid grid-cols-4 gap-4 mb-6">
            <div className="bg-blue-50 p-4 rounded">
              <p className="text-sm text-gray-600">计算物料</p>
              <p className="text-2xl font-bold text-blue-600">
                {result.statistics?.totalItems || 0}
              </p>
            </div>
            <div className="bg-green-50 p-4 rounded">
              <p className="text-sm text-gray-600">需求单数</p>
              <p className="text-2xl font-bold text-green-600">
                {result.statistics?.totalDemands || 0}
              </p>
            </div>
            <div className="bg-yellow-50 p-4 rounded">
              <p className="text-sm text-gray-600">采购建议</p>
              <p className="text-2xl font-bold text-yellow-600">
                {result.statistics?.purchaseSuggestions || 0}
              </p>
            </div>
            <div className="bg-purple-50 p-4 rounded">
              <p className="text-sm text-gray-600">生产建议</p>
              <p className="text-2xl font-bold text-purple-600">
                {result.statistics?.productionSuggestions || 0}
              </p>
            </div>
          </div>

          <div className="flex gap-2">
            <button className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700">
              确认建议
            </button>
            <button className="px-4 py-2 border rounded hover:bg-gray-50">
              导出结果
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
