/**
 * What-if 模拟页面
 */
import { useState } from 'react';

interface ChangeItem {
  id: string;
  changeType: string;
  targetType: string;
  targetCode: string;
  originalValue: string;
  newValue: string;
  description: string;
}

interface Scenario {
  id: string;
  name: string;
  type: string;
  description: string;
  changes: ChangeItem[];
}

interface ImpactResult {
  affectedOrders: number;
  affectedItems: number;
  completionDateChange: string;
  totalCostChange: number;
  resourceUtilizationChange: number;
  details: any[];
}

export function WhatIfPage() {
  const [scenarios, setScenarios] = useState<Scenario[]>([]);
  const [currentScenario, setCurrentScenario] = useState<Partial<Scenario>>({
    name: '',
    type: 'DEMAND_CHANGE',
    description: '',
    changes: [],
  });
  const [result, setResult] = useState<ImpactResult | null>(null);
  const [loading, setLoading] = useState(false);

  const changeTypes = [
    { value: 'DEMAND_CHANGE', label: '需求变更' },
    { value: 'ORDER_PRIORITY', label: '优先级调整' },
    { value: 'LEAD_TIME_CHANGE', label: '交期变更' },
    { value: 'INVENTORY_CHANGE', label: '库存变化' },
    { value: 'SUPPLY_CHANGE', label: '供应变化' },
  ];

  const targetTypes = [
    { value: 'ORDER', label: '订单' },
    { value: 'ITEM', label: '物料' },
    { value: 'SUPPLIER', label: '供应商' },
  ];

  const addChange = () => {
    const newChange: ChangeItem = {
      id: Date.now().toString(),
      changeType: currentScenario.type || 'DEMAND_CHANGE',
      targetType: 'ORDER',
      targetCode: '',
      originalValue: '',
      newValue: '',
      description: '',
    };
    setCurrentScenario({
      ...currentScenario,
      changes: [...(currentScenario.changes || []), newChange],
    });
  };

  const updateChange = (id: string, field: string, value: string) => {
    setCurrentScenario({
      ...currentScenario,
      changes: currentScenario.changes?.map(c => 
        c.id === id ? { ...c, [field]: value } : c
      ),
    });
  };

  const removeChange = (id: string) => {
    setCurrentScenario({
      ...currentScenario,
      changes: currentScenario.changes?.filter(c => c.id !== id),
    });
  };

  const runSimulation = async () => {
    setLoading(true);
    try {
      // 模拟 API 调用
      await new Promise(resolve => setTimeout(resolve, 1500));
      
      // 模拟结果
      setResult({
        affectedOrders: Math.floor(Math.random() * 10) + 1,
        affectedItems: Math.floor(Math.random() * 20) + 5,
        completionDateChange: `${Math.floor(Math.random() * 10) - 3} 天`,
        totalCostChange: Math.floor(Math.random() * 20000) - 5000,
        resourceUtilizationChange: Math.floor(Math.random() * 30) - 5,
        details: currentScenario.changes?.map(c => ({
          targetCode: c.targetCode,
          impactType: c.changeType,
          impactDescription: `变更 ${c.originalValue} -> ${c.newValue}`,
        })) || [],
      });
    } finally {
      setLoading(false);
    }
  };

  const saveScenario = () => {
    const newScenario: Scenario = {
      id: Date.now().toString(),
      name: currentScenario.name || '新场景',
      type: currentScenario.type || 'DEMAND_CHANGE',
      description: currentScenario.description || '',
      changes: currentScenario.changes || [],
    };
    setScenarios([...scenarios, newScenario]);
    setCurrentScenario({ name: '', type: 'DEMAND_CHANGE', description: '', changes: [] });
    setResult(null);
  };

  return (
    <div>
      <div className="mb-6">
        <h2 className="text-2xl font-bold mb-2">What-if 模拟</h2>
        <p className="text-gray-600">创建不同场景方案，评估变更对待生产计划的影响</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 场景配置 */}
        <div className="bg-white p-6 rounded shadow">
          <h3 className="font-medium mb-4">场景配置</h3>
          
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">场景名称</label>
              <input
                type="text"
                value={currentScenario.name}
                onChange={(e) => setCurrentScenario({...currentScenario, name: e.target.value})}
                placeholder="例如：紧急插单测试"
                className="w-full px-3 py-2 border rounded"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium mb-1">场景类型</label>
              <select
                value={currentScenario.type}
                onChange={(e) => setCurrentScenario({...currentScenario, type: e.target.value})}
                className="w-full px-3 py-2 border rounded"
              >
                {changeTypes.map(t => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium mb-1">描述</label>
              <textarea
                value={currentScenario.description}
                onChange={(e) => setCurrentScenario({...currentScenario, description: e.target.value})}
                placeholder="描述这个场景的目的"
                className="w-full px-3 py-2 border rounded"
                rows={2}
              />
            </div>

            {/* 变更项 */}
            <div>
              <div className="flex justify-between items-center mb-2">
                <label className="block text-sm font-medium">变更项</label>
                <button
                  onClick={addChange}
                  className="text-sm text-blue-600 hover:text-blue-800"
                >
                  + 添加变更
                </button>
              </div>
              
              {currentScenario.changes?.map((change, idx) => (
                <div key={change.id} className="border rounded p-3 mb-2 bg-gray-50">
                  <div className="grid grid-cols-2 gap-2 mb-2">
                    <select
                      value={change.targetType}
                      onChange={(e) => updateChange(change.id, 'targetType', e.target.value)}
                      className="px-2 py-1 border rounded text-sm"
                    >
                      {targetTypes.map(t => (
                        <option key={t.value} value={t.value}>{t.label}</option>
                      ))}
                    </select>
                    <input
                      type="text"
                      value={change.targetCode}
                      onChange={(e) => updateChange(change.id, 'targetCode', e.target.value)}
                      placeholder="编码"
                      className="px-2 py-1 border rounded text-sm"
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-2 mb-2">
                    <input
                      type="text"
                      value={change.originalValue}
                      onChange={(e) => updateChange(change.id, 'originalValue', e.target.value)}
                      placeholder="原值"
                      className="px-2 py-1 border rounded text-sm"
                    />
                    <input
                      type="text"
                      value={change.newValue}
                      onChange={(e) => updateChange(change.id, 'newValue', e.target.value)}
                      placeholder="新值"
                      className="px-2 py-1 border rounded text-sm"
                    />
                  </div>
                  <button
                    onClick={() => removeChange(change.id)}
                    className="text-sm text-red-600 hover:text-red-800"
                  >
                    删除
                  </button>
                </div>
              ))}
            </div>

            <div className="flex gap-2">
              <button
                onClick={runSimulation}
                disabled={loading || !currentScenario.name}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
              >
                {loading ? '计算中...' : '运行模拟'}
              </button>
              <button
                onClick={saveScenario}
                className="px-4 py-2 border rounded hover:bg-gray-50"
              >
                保存场景
              </button>
            </div>
          </div>
        </div>

        {/* 分析结果 */}
        <div className="bg-white p-6 rounded shadow">
          <h3 className="font-medium mb-4">影响分析结果</h3>
          
          {result ? (
            <div>
              {/* 关键指标 */}
              <div className="grid grid-cols-2 gap-4 mb-4">
                <div className="bg-blue-50 p-3 rounded">
                  <p className="text-sm text-gray-600">影响订单</p>
                  <p className="text-2xl font-bold text-blue-600">{result.affectedOrders}</p>
                </div>
                <div className="bg-green-50 p-3 rounded">
                  <p className="text-sm text-gray-600">影响物料</p>
                  <p className="text-2xl font-bold text-green-600">{result.affectedItems}</p>
                </div>
                <div className="bg-yellow-50 p-3 rounded">
                  <p className="text-sm text-gray-600">完工时间变化</p>
                  <p className="text-2xl font-bold text-yellow-600">{result.completionDateChange}</p>
                </div>
                <div className={`p-3 rounded ${result.totalCostChange > 0 ? 'bg-red-50' : 'bg-green-50'}`}>
                  <p className="text-sm text-gray-600">成本变化</p>
                  <p className={`text-2xl font-bold ${result.totalCostChange > 0 ? 'text-red-600' : 'text-green-600'}`}>
                    {result.totalCostChange > 0 ? '+' : ''}{result.totalCostChange}
                  </p>
                </div>
              </div>

              {/* 建议 */}
              <div className="border-t pt-4">
                <h4 className="font-medium mb-2">💡 建议</h4>
                <ul className="text-sm space-y-1 text-gray-600">
                  {result.affectedOrders > 0 && (
                    <li>• 建议重新运行MRP以更新受影响订单的计划</li>
                  )}
                  {result.totalCostChange > 0 ? (
                    <li>• 预计成本增加，建议评估是否接受当前方案</li>
                  ) : (
                    <li>• 方案成本优化，建议采纳</li>
                  )}
                  {result.resourceUtilizationChange > 10 && (
                    <li>• 资源利用率显著提升，建议确认产能安排</li>
                  )}
                </ul>
              </div>

              <button className="mt-4 w-full px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700">
                应用此方案
              </button>
            </div>
          ) : (
            <div className="text-center text-gray-400 py-12">
              <p>配置场景后点击"运行模拟"查看结果</p>
            </div>
          )}
        </div>
      </div>

      {/* 已保存场景 */}
      {scenarios.length > 0 && (
        <div className="mt-6 bg-white p-6 rounded shadow">
          <h3 className="font-medium mb-4">已保存场景</h3>
          <div className="space-y-2">
            {scenarios.map(s => (
              <div key={s.id} className="flex justify-between items-center p-3 border rounded hover:bg-gray-50">
                <div>
                  <span className="font-medium">{s.name}</span>
                  <span className="text-sm text-gray-500 ml-2">({s.type})</span>
                </div>
                <button
                  onClick={() => {
                    setCurrentScenario(s);
                    setResult(null);
                  }}
                  className="text-blue-600 hover:text-blue-800"
                >
                  加载
                </button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
