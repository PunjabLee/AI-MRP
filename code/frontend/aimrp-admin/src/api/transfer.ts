/**
 * 库存调拨 API
 */
import api from './index';

export interface InventoryTransfer {
  id?: number;
  transferNo?: string;
  transferType?: string;
  status?: string;
  fromWarehouseCode?: string;
  fromWarehouseName?: string;
  toWarehouseCode?: string;
  toWarehouseName?: string;
  itemCode?: string;
  itemName?: string;
  transferQty?: number;
  unit?: string;
  transferDate?: string;
  expectDate?: string;
  reason?: string;
  remark?: string;
}

export const transferApi = {
  // 调拨单列表
  list: (params: {
    status?: string;
    fromWarehouse?: string;
    toWarehouse?: string;
    itemCode?: string;
    pageNum?: number;
    pageSize?: number;
  }) => api.get<{list: InventoryTransfer[]; total: number}>('/inventory/transfer', { params }),
  
  // 调拨单详情
  getById: (id: number) => api.get<InventoryTransfer>(`/inventory/transfer/${id}`),
  
  // 创建调拨单
  create: (data: InventoryTransfer) => api.post<InventoryTransfer>('/inventory/transfer', data),
  
  // 审核调拨单
  approve: (id: number, approver: string, remark: string) => 
    api.post<void>(`/inventory/transfer/${id}/approve`, { approver, remark }),
  
  // 执行调拨
  execute: (id: number) => api.post<void>(`/inventory/transfer/${id}/execute`),
  
  // 取消调拨单
  cancel: (id: number, reason: string) => 
    api.post<void>(`/inventory/transfer/${id}/cancel`, { reason }),
};
