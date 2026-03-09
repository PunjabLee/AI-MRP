/**
 * 布局组件
 */
import { useState } from 'react';

interface LayoutProps {
  children: React.ReactNode;
}

export function Layout({ children }: LayoutProps) {
  const [collapsed, setCollapsed] = useState(false);
  
  const menuItems = [
    { key: 'orders', label: '销售订单', path: '/orders' },
    { key: 'bom', label: 'BOM管理', path: '/bom' },
    { key: 'inventory', label: '库存管理', path: '/inventory' },
    { key: 'mrp', label: 'MRP计算', path: '/mrp' },
    { key: 'purchase', label: '采购管理', path: '/purchase' },
    { key: 'production', label: '生产管理', path: '/production' },
    { key: 'risk', label: '风险预警', path: '/risk' },
    { key: 'ai-chat', label: 'AI对话', path: '/chat' },
  ];
  
  return (
    <div className="flex h-screen">
      {/* 侧边栏 */}
      <aside className={`${collapsed ? 'w-16' : 'w-64'} bg-gray-800 text-white transition-all`}>
        <div className="h-16 flex items-center justify-center border-b border-gray-700">
          <h1 className="text-lg font-bold">AI MRP</h1>
        </div>
        
        <nav className="mt-4">
          {menuItems.map(item => (
            <a
              key={item.key}
              href={item.path}
              className="block px-4 py-3 hover:bg-gray-700 transition-colors"
            >
              {item.label}
            </a>
          ))}
        </nav>
      </aside>
      
      {/* 主内容区 */}
      <div className="flex-1 flex flex-col">
        {/* 头部 */}
        <header className="h-16 bg-white shadow flex items-center justify-between px-6">
          <button 
            onClick={() => setCollapsed(!collapsed)}
            className="text-gray-600 hover:text-gray-900"
          >
            {collapsed ? '展开' : '收起'}
          </button>
          
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-500">Admin</span>
          </div>
        </header>
        
        {/* 内容 */}
        <main className="flex-1 bg-gray-100 p-6 overflow-auto">
          {children}
        </main>
      </div>
    </div>
  );
}
