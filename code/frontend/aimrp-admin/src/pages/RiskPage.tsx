/**
 * 风险监控页面
 */
import { useState, useEffect } from 'react';

interface RiskItem {
  riskId: number;
  riskCode: string;
  riskType: string;
  riskLevel: string;
  title: string;
  description: string;
  relatedCode: string;
  riskValue: number;
  suggestedAction: string;
  status: string;
  detectedAt: string;
}

interface RiskStats {
  totalRisks: number;
  criticalCount: number;
  highCount: number;
  mediumCount: number;
  lowCount: number;
  overallRiskValue: number;
}

export function RiskPage() {
  const [risks, setRisks] = useState<RiskItem[]>([]);
  const [stats, setStats] = useState<RiskStats | null>(null);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    loadRisks();
  }, []);

  const loadRisks = async () => {
    setLoading(true);
    try {
      // 模拟数据
      const mockRisks: RiskItem[] = [
        {
          riskId: 1,
          riskCode: 'INV-A001',
          riskType: 'INVENTORY_SHORTAGE',
          riskLevel: 'CRITICAL',
          title: '库存不足：A001',
          description: '物料 A001 库存 50 低于安全库存 100',
          relatedCode: 'A001',
          riskValue: 85,
          suggestedAction: '建议立即采购',
          status: 'ACTIVE',
          detectedAt: new Date().toISOString(),
        },
        {
          riskId: 2,
          riskCode: 'SUP-SUP001',
          riskType: 'SUPPLIER_DELAY',
          riskLevel: 'HIGH',
          title: '供应商交期延迟：SUP001',
          description: '供应商 SUP001 平均延迟 5 天',
          relatedCode: 'SUP001',
          riskValue: 65,
          suggestedAction: '建议寻找替代供应商',
          status: 'ACTIVE',
          detectedAt: new Date().toISOString(),
        },
        {
          riskId: 3,
          riskCode: 'INV-C003',
          riskType: 'INVENTORY_SHORTAGE',
          riskLevel: 'MEDIUM',
          title: '库存可用天数不足：C003',
          description: '物料 C003 可用天数仅 3 天',
          relatedCode: 'C003',
          riskValue: 45,
          suggestedAction: '建议补充库存',
          status: 'MONITORING',
          detectedAt: new Date().toISOString(),
        },
      ];
      
      setRisks(mockRisks);
      setStats({
        totalRisks: mockRisks.length,
        criticalCount: 1,
        highCount: 1,
        mediumCount: 1,
        lowCount: 0,
        overallRiskValue: 65,
      });
    } finally {
      setLoading(false);
    }
  };

  const getLevelColor = (level: string) => {
    switch (level) {
      case 'CRITICAL': return 'bg-red-100 text-red-800 border-red-300';
      case 'HIGH': return 'bg-orange-100 text-orange-800 border-orange-300';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800 border-yellow-300';
      case 'LOW': return 'bg-green-100 text-green-800 border-green-300';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      'INVENTORY_SHORTAGE': '库存短缺',
      'SUPPLIER_DELAY': '供应商延迟',
      'DEMAND_SURGE': '需求突变',
      'PRODUCTION_DELAY': '生产延期',
      'CAPACITY_SHORTAGE': '产能不足',
    };
    return labels[type] || type;
  };

  const filteredRisks = filter === 'ALL' 
    ? risks 
    : risks.filter(r => r.riskLevel === filter);

  return (
    <div>
      <div className="mb-6">
        <h2 className="text-2xl font-bold mb-2">风险监控</h2>
        <p className="text-gray-600">实时监控库存、供应商、生产等风险</p>
      </div>

      {/* 统计卡片 */}
      {stats && (
        <div className="grid grid-cols-5 gap-4 mb-6">
          <div className="bg-white p-4 rounded shadow">
            <p className="text-sm text-gray-500">总风险数</p>
            <p className="text-3xl font-bold">{stats.totalRisks}</p>
          </div>
          <div className="bg-red-50 p-4 rounded border border-red-200">
            <p className="text-sm text-red-600">严重</p>
            <p className="text-3xl font-bold text-red-600">{stats.criticalCount}</p>
          </div>
          <div className="bg-orange-50 p-4 rounded border border-orange-200">
            <p className="text-sm text-orange-600">高</p>
            <p className="text-3xl font-bold text-orange-600">{stats.highCount}</p>
          </div>
          <div className="bg-yellow-50 p-4 rounded border border-yellow-200">
            <p className="text-sm text-yellow-600">中</p>
            <p className="text-3xl font-bold text-yellow-600">{stats.mediumCount}</p>
          </div>
          <div className="bg-green-50 p-4 rounded border border-green-200">
            <p className="text-sm text-green-600">低</p>
            <p className="text-3xl font-bold text-green-600">{stats.lowCount}</p>
          </div>
        </div>
      )}

      {/* 筛选 */}
      <div className="flex gap-2 mb-4">
        {['ALL', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].map(level => (
          <button
            key={level}
            onClick={() => setFilter(level)}
            className={`px-4 py-2 rounded ${
              filter === level 
                ? 'bg-blue-600 text-white' 
                : 'bg-white border hover:bg-gray-50'
            }`}
          >
            {level === 'ALL' ? '全部' : level}
          </button>
        ))}
        <button
          onClick={loadRisks}
          className="ml-auto px-4 py-2 border rounded hover:bg-gray-50"
        >
          🔄 刷新
        </button>
      </div>

      {/* 风险列表 */}
      <div className="space-y-3">
        {loading ? (
          <div className="text-center py-12 text-gray-400">加载中...</div>
        ) : filteredRisks.length === 0 ? (
          <div className="text-center py-12 text-gray-400">
            <p className="text-4xl mb-2">✅</p>
            <p>暂无风险</p>
          </div>
        ) : (
          filteredRisks.map(risk => (
            <div 
              key={risk.riskId} 
              className={`bg-white p-4 rounded shadow border-l-4 ${
                risk.riskLevel === 'CRITICAL' ? 'border-red-500' :
                risk.riskLevel === 'HIGH' ? 'border-orange-500' :
                risk.riskLevel === 'MEDIUM' ? 'border-yellow-500' :
                'border-green-500'
              }`}
            >
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <span className={`px-2 py-0.5 rounded text-xs font-medium ${getLevelColor(risk.riskLevel)}`}>
                      {risk.riskLevel}
                    </span>
                    <span className="text-sm text-gray-500">{risk.riskCode}</span>
                    <span className="text-sm bg-gray-100 px-2 rounded">
                      {getTypeLabel(risk.riskType)}
                    </span>
                  </div>
                  <h3 className="font-medium text-lg">{risk.title}</h3>
                  <p className="text-gray-600 text-sm mt-1">{risk.description}</p>
                  <div className="mt-2 p-2 bg-blue-50 rounded text-sm text-blue-700">
                    💡 {risk.suggestedAction}
                  </div>
                </div>
                <div className="text-right ml-4">
                  <div className="text-2xl font-bold text-gray-400">{risk.riskValue}</div>
                  <div className="text-xs text-gray-500">风险值</div>
                  <div className="mt-2 flex gap-1">
                    <button className="px-2 py-1 text-xs bg-green-100 text-green-700 rounded hover:bg-green-200">
                      解决
                    </button>
                    <button className="px-2 py-1 text-xs bg-gray-100 text-gray-700 rounded hover:bg-gray-200">
                      忽略
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
