import { useState, useEffect } from 'react';
import { forecastApi } from '../api/forecast';

export function ForecastPage() {
  const [forecastData, setForecastData] = useState<any[]>([]);
  const [safetyStockData, setSafetyStockData] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const result = await forecastApi.list({});
      setForecastData(result.list || []);
      
      const ssResult = await forecastApi.getSafetyStock({});
      setSafetyStockData(ssResult.list || []);
    } catch (error) {
      console.error('获取预测数据失败', error);
    } finally {
      setLoading(false);
    }
  };

  const runForecast = async () => {
    try {
      await forecastApi.run({ method: 'prophet', days: 30 });
      fetchData();
    } catch (error) {
      console.error('运行预测失败', error);
    }
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">需求预测</h1>
        <button onClick={runForecast} className="px-4 py-2 bg-blue-600 text-white rounded">
          运行预测
        </button>
      </div>

      {/* 预测数据 */}
      <div className="mb-6">
        <h2 className="text-xl font-bold mb-4">需求预测</h2>
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料编码</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">日期</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">预测数量</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">置信区间</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {forecastData.map((item, idx) => (
                <tr key={idx}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.itemCode}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.forecastDate}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.forecastQty}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.lowerBound} - {item.upperBound}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* 安全库存推荐 */}
      <div>
        <h2 className="text-xl font-bold mb-4">安全库存推荐</h2>
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料编码</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">当前安全库存</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">推荐安全库存</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">再订货点</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {safetyStockData.map((item, idx) => (
                <tr key={idx}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.itemCode}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.currentSafetyStock}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-green-600">{item.recommendedSafetyStock}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.reorderPoint}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
