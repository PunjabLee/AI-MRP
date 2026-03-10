import { createBrowserRouter, Navigate } from 'react-router-dom';

// 页面组件 - MVP
import OrderPage from '../pages/OrderPage';
import ItemPage from '../pages/ItemPage';
import InventoryPage from '../pages/InventoryPage';
import TransferPage from '../pages/TransferPage';
import BomPage from '../pages/BomPage';
import MrpPage from '../pages/MrpPage';
import PurchasePage from '../pages/PurchasePage';
import ProductionPage from '../pages/ProductionPage';
import SupplierPage from '../pages/SupplierPage';

// 页面组件 - Pro
import ForecastPage from '../pages/ForecastPage';
import GanttPage from '../pages/GanttPage';
import MpsPage from '../pages/MpsPage';
import WhatIfPage from '../pages/WhatIfPage';
import RiskPage from '../pages/RiskPage';
import ReportPage from '../pages/ReportPage';
import CostPage from '../pages/CostPage';

// 页面组件 - Enterprise
import OrgPage from '../pages/OrgPage';
import EquipmentPage from '../pages/EquipmentPage';
import QualityPage from '../pages/QualityPage';
import SupplierPortalPage from '../pages/SupplierPortalPage';
import WarehousePage from '../pages/WarehousePage';

const router = createBrowserRouter([
  // MVP 核心功能
  { path: '/', element: <Navigate to="/orders" replace /> },
  { path: '/orders', element: <OrderPage /> },
  { path: '/items', element: <ItemPage /> },
  { path: '/inventory', element: <InventoryPage /> },
  { path: '/transfer', element: <TransferPage /> },
  { path: '/bom', element: <BomPage /> },
  { path: '/mrp', element: <MrpPage /> },
  { path: '/purchase', element: <PurchasePage /> },
  { path: '/production', element: <ProductionPage /> },
  { path: '/suppliers', element: <SupplierPage /> },
  
  // Pro 高级功能
  { path: '/forecast', element: <ForecastPage /> },
  { path: '/gantt', element: <GanttPage /> },
  { path: '/mps', element: <MpsPage /> },
  { path: '/whatif', element: <WhatIfPage /> },
  { path: '/risk', element: <RiskPage /> },
  { path: '/report', element: <ReportPage /> },
  { path: '/cost', element: <CostPage /> },
  
  // Enterprise 企业功能
  { path: '/org', element: <OrgPage /> },
  { path: '/equipment', element: <EquipmentPage /> },
  { path: '/quality', element: <QualityPage /> },
  { path: '/supplier-portal', element: <SupplierPortalPage /> },
  { path: '/warehouse', element: <WarehousePage /> },
]);

export default router;
