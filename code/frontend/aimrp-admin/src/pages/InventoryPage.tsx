/**
 * 库存页面
 */
import { useState, useEffect } from 'react';
import { inventoryApi, Inventory } from '../api/inventory';

export function InventoryPage() {
  const [inventory, setInventory] = useState<Inventory[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState({ itemCode: '', warehouseCode: '' });
  const [showInModal, setShowInModal] = useState(false);
  const [showOutModal, setShowOutModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState<Inventory | null>(null);
  const [ioQty, setIoQty] = useState(0);

  useEffect(() => {
    loadInventory();
  }, []);

  const loadInventory = async () => {
    setLoading(true);
    try {
      const result = await inventoryApi.list(searchParams);
      setInventory(result || []);
    } catch (error) {
      console.error('加载失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    loadInventory();
  };

  const handleInStock = async () => {
    if (!selectedItem || ioQty <= 0) return;
    try {
      await inventoryApi.inStock({
        itemCode: selectedItem.itemCode || '',
        qty: ioQty,
        warehouseCode: selectedItem.warehouseCode || '',
      });
      alert('入库成功');
      setShowInModal(false);
      setIoQty(0);
      loadInventory();
    } catch (error) {
      console.error('入库失败:', error);
    }
  };

  const handleOutStock = async () => {
    if (!selectedItem || ioQty <= 0) return;
    try {
      await inventoryApi.outStock({
        itemCode: selectedItem.itemCode || '',
        qty: ioQty,
        warehouseCode: selectedItem.warehouseCode || '',
      });
      alert('出库成功');
      setShowOutModal(false);
      setIoQty(0);
      loadInventory();
    } catch (error) {
      console.error('出库失败:', error);
    }
  };

  return (
    <div>
      <div className="mb-4 flex justify-between items-center">
        <h2 className="text-2xl font-bold">库存管理</h2>
      </div>

      {/* 搜索栏 */}
      <div className="mb-4 bg-white p-4 rounded shadow">
        <div className="flex gap-4 items-end">
          <div>
            <label className="block text-sm text-gray-600 mb-1">物料编码</label>
            <input
              type="text"
              value={searchParams.itemCode}
              onChange={(e) => setSearchParams(p => ({ ...p, itemCode: e.target.value }))}
              placeholder="请输入物料编码"
              className="px-3 py-2 border rounded w-40"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-600 mb-1">仓库</label>
            <input
              type="text"
              value={searchParams.warehouseCode}
              onChange={(e) => setSearchParams(p => ({ ...p, warehouseCode: e.target.value }))}
              placeholder="请输入仓库编码"
              className="px-3 py-2 border rounded w-40"
            />
          </div>
          <button
            onClick={handleSearch}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            搜索
          </button>
          <button
            onClick={() => {
              setSearchParams({ itemCode: '', warehouseCode: '' });
              loadInventory();
            }}
            className="px-4 py-2 border rounded hover:bg-gray-50"
          >
            重置
          </button>
        </div>
      </div>

      {/* 库存列表 */}
      <div className="bg-white rounded shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料编码</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">仓库</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">可用库存</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">已分配</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">库存数量</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {loading ? (
              <tr><td colSpan={7} className="text-center py-8">加载中...</td></tr>
            ) : inventory.length === 0 ? (
              <tr><td colSpan={7} className="text-center py-8">暂无数据</td></tr>
            ) : (
              inventory.map((item) => (
                <tr key={`${item.itemCode}-${item.warehouseCode}`} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">{item.itemCode}</td>
                  <td className="px-6 py-4">{item.itemName}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.warehouseName}</td>
                  <td className="px-6 py-4">
                    <span className="text-green-600 font-medium">{item.availableQty}</span>
                  </td>
                  <td className="px-6 py-4 text-yellow-600">{item.allocatedQty}</td>
                  <td className="px-6 py-4">{item.onHandQty}</td>
                  <td className="px-6 py-4">
                    <button 
                      onClick={() => {
                        setSelectedItem(item);
                        setShowInModal(true);
                      }}
                      className="text-green-600 hover:text-green-800 mr-3"
                    >
                      入库
                    </button>
                    <button 
                      onClick={() => {
                        setSelectedItem(item);
                        setShowOutModal(true);
                      }}
                      className="text-orange-600 hover:text-orange-800"
                    >
                      出库
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* 入库弹窗 */}
      {showInModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-96">
            <h3 className="text-lg font-bold mb-4">入库 - {selectedItem?.itemCode}</h3>
            <div className="mb-4">
              <label className="block text-sm text-gray-600 mb-1">入库数量</label>
              <input
                type="number"
                value={ioQty}
                onChange={(e) => setIoQty(Number(e.target.value))}
                className="w-full px-3 py-2 border rounded"
                min="1"
              />
            </div>
            <div className="flex justify-end gap-2">
              <button 
                onClick={() => setShowInModal(false)}
                className="px-4 py-2 border rounded"
              >
                取消
              </button>
              <button 
                onClick={handleInStock}
                className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
              >
                确认入库
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 出库弹窗 */}
      {showOutModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-96">
            <h3 className="text-lg font-bold mb-4">出库 - {selectedItem?.itemCode}</h3>
            <div className="mb-4">
              <label className="block text-sm text-gray-600 mb-1">出库数量</label>
              <input
                type="number"
                value={ioQty}
                onChange={(e) => setIoQty(Number(e.target.value))}
                className="w-full px-3 py-2 border rounded"
                min="1"
                max={selectedItem?.availableQty}
              />
              <p className="text-sm text-gray-500 mt-1">可用库存: {selectedItem?.availableQty}</p>
            </div>
            <div className="flex justify-end gap-2">
              <button 
                onClick={() => setShowOutModal(false)}
                className="px-4 py-2 border rounded"
              >
                取消
              </button>
              <button 
                onClick={handleOutStock}
                className="px-4 py-2 bg-orange-600 text-white rounded hover:bg-orange-700"
              >
                确认出库
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
