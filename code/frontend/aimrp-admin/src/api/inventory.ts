/**
 * 库存 API
 */
import api from './index';

export interface Inventory {
  id?: number;
  itemCode?: string;
  itemName?: string;
  warehouseCode?: string;
  warehouseName?: string;
  onHandQty?: number;
  allocatedQty?: number;
  availableQty?: number;
}

export const inventoryApi = {
  // 库存列表
  list: (params?: {itemCode?: string; warehouseCode?: string}) => 
    api.get<Inventory[]>('/inventory', { params }),
  
  // 按物料查询
  getByItemCode: (itemCode: string) => 
    api.get<Inventory[]>(`/inventory/item/${itemCode}`),
  
  // 入库
  inStock: (data: {itemCode: string; qty: number; warehouseCode: string}) => 
    api.post<Inventory>('/inventory/in', data),
  
  // 出库
  outStock: (data: {itemCode: string; qty: number; warehouseCode: string}) => 
    api.post<Inventory>('/inventory/out', data),
};
