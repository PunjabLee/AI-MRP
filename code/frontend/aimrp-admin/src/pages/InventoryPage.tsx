import { useState, useEffect } from 'react';
import { inventoryApi, InventoryItem } from '../api/inventory';
import { message } from 'antd';

export function InventoryPage() {
  const [data, setData] = useState<InventoryItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState({ itemCode: '', warehouseCode: '' });
  const [pagination, setPagination] = useState({ pageNum: 1, pageSize: 10, total: 0 });

  useEffect(() => {
    fetchData();
  }, [pagination.pageNum, searchParams]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const result = await inventoryApi.list({
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        ...searchParams
      });
      setData(result.list || []);
      setPagination(prev => ({ ...prev, total: result.total || 0 }));
    } catch (error) {
      console.error('获取库存失败', error);
      message.error('获取库存失败');
    } finally {
      setLoading(false);
    }
  };

  // 入库
  const handleInStock = async (record: InventoryItem, qty: number) => {
    try {
      await inventoryApi.inStock({
        itemCode: record.itemCode,
        warehouseCode: record.warehouseCode,
        qty: qty,
        type: 'PURCHASE_IN'
      });
      message.success('入库成功');
      fetchData();
    } catch (error) {
      message.error('入库失败');
    }
  };

  // 出库
  const handleOutStock = async (record: InventoryItem, qty: number) => {
    try {
      await inventoryApi.outStock({
        itemCode: record.itemCode,
        warehouseCode: record.warehouseCode,
        qty: qty,
        type: 'PRODUCTION_OUT'
      });
      message.success('出库成功');
      fetchData();
    } catch (error) {
      message.error('出库失败');
    }
  };

  const getStatusBadge = (qty: number, safetyStock: number) => {
    if (qty === 0) return { color: 'red', text: '缺货' };
    if (qty < safetyStock) return { color: 'orange', text: '偏低' };
    return { color: 'green', text: '正常' };
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">库存管理</h1>
      </div>

      {/* 搜索栏 */}
      <div className="mb-4 bg-white p-4 rounded-lg shadow">
        <div className="flex gap-4">
          <input
            placeholder="物料编码"
            value={searchParams.itemCode}
            onChange={e => setSearchParams({...searchParams, itemCode: e.target.value})}
            className="border rounded px-3 py-2"
          />
          <input
            placeholder="仓库编码"
            value={searchParams.warehouseCode}
            onChange={e => setSearchParams({...searchParams, warehouseCode: e.target.value})}
            className="border rounded px-3 py-2"
          />
          <button onClick={fetchData} className="px-4 py-2 bg-blue-600 text-white rounded">
            查询
          </button>
        </div>
      </div>

      {/* 库存列表 */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料编码</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">仓库</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">库存数量</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">可用数量</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {data.map((item) => {
              const status = getStatusBadge(item.onHandQty || 0, item.safetyStock || 0);
              return (
                <tr key={item.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.itemCode}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.itemName}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.warehouseName}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.onHandQty}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.availableQty}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs text-white bg-${status.color}--500`}>
                      {status.text}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <button 
                      onClick={() => handleInStock(item, 100)}
                      className="text-blue-600 hover:underline mr-2"
                    >
                      入库
                    </button>
                    <button 
                      onClick={() => handleOutStock(item, 10)}
                      className="text-orange-600 hover:underline"
                    >
                      出库
                    </button>
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
