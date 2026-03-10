import { useState, useEffect } from 'react';
import { transferApi, InventoryTransfer } from '../api/transfer';

export default function TransferPage() {
  const [transfers, setTransfers] = useState<InventoryTransfer[]>([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState<Partial<InventoryTransfer>>({});

  useEffect(() => {
    fetchTransfers();
  }, []);

  const fetchTransfers = async () => {
    setLoading(true);
    try {
      const res = await transferApi.list({});
      setTransfers(res.data.list || []);
    } catch (error) {
      console.error('获取调拨单失败', error);
    }
    setLoading(false);
  };

  const handleCreate = async () => {
    try {
      await transferApi.create(formData as InventoryTransfer);
      setShowModal(false);
      setFormData({});
      fetchTransfers();
    } catch (error) {
      console.error('创建调拨单失败', error);
    }
  };

  const handleApprove = async (id: number) => {
    try {
      await transferApi.approve(id, 'admin', '审核通过');
      fetchTransfers();
    } catch (error) {
      console.error('审核失败', error);
    }
  };

  const handleExecute = async (id: number) => {
    try {
      await transferApi.execute(id);
      fetchTransfers();
    } catch (error) {
      console.error('执行失败', error);
    }
  };

  const getStatusBadge = (status: string) => {
    const badges: Record<string, string> = {
      'DRAFT': 'bg-gray-100 text-gray-800',
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'APPROVED': 'bg-blue-100 text-blue-800',
      'COMPLETED': 'bg-green-100 text-green-800',
      'CANCELLED': 'bg-red-100 text-red-800',
    };
    return badges[status] || 'bg-gray-100';
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">库存调拨</h1>
        <button
          onClick={() => setShowModal(true)}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          创建调拨单
        </button>
      </div>

      {/* 调拨单列表 */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">调拨单号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">调出仓库</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">调入仓库</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">数量</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {transfers.map((item) => (
              <tr key={item.id}>
                <td className="px-6 py-4 whitespace-nowrap text-sm">{item.transferNo}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  {item.itemCode}<br/>
                  <span className="text-gray-500">{item.itemName}</span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">{item.fromWarehouseName}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">{item.toWarehouseName}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">{item.transferQty}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className={`px-2 py-1 rounded-full text-xs ${getStatusBadge(item.status || '')}`}>
                    {item.status}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  {item.status === 'DRAFT' && (
                    <button onClick={() => handleApprove(item.id!)} className="text-blue-600 hover:underline mr-2">
                      审核
                    </button>
                  )}
                  {item.status === 'APPROVED' && (
                    <button onClick={() => handleExecute(item.id!)} className="text-green-600 hover:underline">
                      执行
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 创建弹窗 */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">创建调拨单</h2>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">物料编码</label>
                <input
                  type="text"
                  value={formData.itemCode || ''}
                  onChange={(e) => setFormData({...formData, itemCode: e.target.value})}
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">调出仓库</label>
                <input
                  type="text"
                  value={formData.fromWarehouseCode || ''}
                  onChange={(e) => setFormData({...formData, fromWarehouseCode: e.target.value})}
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">调入仓库</label>
                <input
                  type="text"
                  value={formData.toWarehouseCode || ''}
                  onChange={(e) => setFormData({...formData, toWarehouseCode: e.target.value})}
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">调拨数量</label>
                <input
                  type="number"
                  value={formData.transferQty || ''}
                  onChange={(e) => setFormData({...formData, transferQty: Number(e.target.value)})}
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">调拨原因</label>
                <textarea
                  value={formData.reason || ''}
                  onChange={(e) => setFormData({...formData, reason: e.target.value})}
                  className="w-full px-3 py-2 border rounded-lg"
                  rows={2}
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
