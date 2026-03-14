import { useState, useEffect } from 'react';

interface SupplierOrder {
  id: number;
  poNo: string;
  itemCode: string;
  itemName: string;
  qty: number;
  deliveryDate: string;
  status: string;
}

export function SupplierPortalPage() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [orders, setOrders] = useState<SupplierOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });

  const handleLogin = async () => {
    try {
      const res = await fetch('/api/supplier-portal/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginForm)
      });
      const data = await res.json();
      if (data.code === 200) {
        setIsLoggedIn(true);
        fetchPendingOrders();
      }
    } catch (error) {
      console.error('登录失败', error);
    }
  };

  const fetchPendingOrders = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/supplier-portal/orders/pending?supplierId=1');
      const data = await res.json();
      if (data.code === 200) {
        setOrders(data.data?.list || []);
      }
    } catch (error) {
      console.error('获取订单失败', error);
    }
    setLoading(false);
  };

  const handleConfirm = async (id: number) => {
    try {
      await fetch(`/api/supplier-portal/orders/${id}/confirm`, { method: 'POST' });
      fetchPendingOrders();
    } catch (error) {
      console.error('确认失败', error);
    }
  };

  const getStatusBadge = (status: string) => {
    const statuses: Record<string, { color: string; text: string }> = {
      'PENDING_CONFIRM': { color: 'yellow', text: '待确认' },
      'CONFIRMED': { color: 'blue', text: '已确认' },
      'SHIPPED': { color: 'green', text: '已发货' }
    };
    return statuses[status] || { color: 'gray', text: status };
  };

  if (!isLoggedIn) {
    return (
      <div className="p-6">
        <div className="max-w-md mx-auto mt-20 bg-white rounded-lg shadow p-8">
          <h1 className="text-2xl font-bold mb-6 text-center">供应商门户登录</h1>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">用户名</label>
              <input
                type="text"
                value={loginForm.username}
                onChange={e => setLoginForm({...loginForm, username: e.target.value})}
                className="w-full px-3 py-2 border rounded-lg"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">密码</label>
              <input
                type="password"
                value={loginForm.password}
                onChange={e => setLoginForm({...loginForm, password: e.target.value})}
                className="w-full px-3 py-2 border rounded-lg"
              />
            </div>
            <button 
              onClick={handleLogin}
              className="w-full py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              登录
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">供应商门户</h1>
        <button onClick={() => setIsLoggedIn(false)} className="text-gray-600 hover:underline">
          退出登录
        </button>
      </div>

      {/* 待确认订单 */}
      <div className="bg-white rounded-lg shadow mb-6">
        <div className="p-4 border-b">
          <h2 className="text-lg font-bold">待确认订单</h2>
        </div>
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">订单号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">物料</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">数量</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">交货日期</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {orders.map((item) => {
              const statusBadge = getStatusBadge(item.status);
              return (
                <tr key={item.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{item.poNo}</td>
                  <td className="px-6 py-4">
                    {item.itemCode}<br/>
                    <span className="text-gray-500 text-sm">{item.itemName}</span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.qty}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{item.deliveryDate}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 rounded text-xs text-white bg-${statusBadge.color}-500`}>
                      {statusBadge.text}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {item.status === 'PENDING_CONFIRM' && (
                      <button 
                        onClick={() => handleConfirm(item.id)}
                        className="text-blue-600 hover:underline"
                      >
                        确认订单
                      </button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* 功能菜单 */}
      <div className="grid grid-cols-4 gap-4">
        <div className="bg-white p-6 rounded-lg shadow text-center">
          <div className="text-3xl mb-2">📋</div>
          <div className="font-medium">订单管理</div>
        </div>
        <div className="bg-white p-6 rounded-lg shadow text-center">
          <div className="text-3xl mb-2">💰</div>
          <div className="font-medium">报价管理</div>
        </div>
        <div className="bg-white p-6 rounded-lg shadow text-center">
          <div className="text-3xl mb-2">🚚</div>
          <div className="font-medium">发货通知</div>
        </div>
        <div className="bg-white p-6 rounded-lg shadow text-center">
          <div className="text-3xl mb-2">📊</div>
          <div className="font-medium">对账账单</div>
        </div>
      </div>
    </div>
  );
}
