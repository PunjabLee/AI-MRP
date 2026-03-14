/**
 * 库存 API
 */
import api from './index';

export interface InventoryItem {
  id?: number;
  itemCode?: string;
  itemName?: string;
  warehouseCode?: string;
  warehouseName?: string;
  locationCode?: string;
  onHandQty?: number;
  allocatedQty?: number;
  availableQty?: number;
  safetyStock?: number;
  unit?: string;
}

export interface StockInOutRequest {
  itemCode: string;
  warehouseCode: string;
  locationCode?: string;
  qty: number;
  type: string;
  remark?: string;
}

export const inventoryApi = {
  // 库存列表
  list: (params: {
    pageNum?: number;
    pageSize?: number;
    itemCode?: string;
    warehouseCode?: string;
    keyword?: string;
  }) => api.get<{list: InventoryItem[]; total: number}>('/inventory', { params }),
  
  // 库存详情
  getById: (id: number) => api.get<InventoryItem>(`/inventory/${id}`),
  
  // 按物料查询
  getByItemCode: (itemCode: string) => 
    api.get<InventoryItem[]>(`/inventory/item/${itemCode}`),
  
  // 入库
  inStock: (data: StockInOutRequest) => 
    api.post<{id: number}>('/inventory/in', data),
  
  // 出库
  outStock: (data: StockInOutRequest) => 
    api.post<{id: number}>('/inventory/out', data),
  
  // 创建库存记录
  create: (data: InventoryItem) => api.post<InventoryItem>('/inventory', data),
  
  // 更新库存
  update: (id: number, data: InventoryItem) => api.put<void>(`/inventory/${id}`, data),
  
  // 删除库存
  delete: (id: number) => api.delete<void>(`/inventory/${id}`),
};
