/**
 * 采购订单 API
 */
import api from './index';

export interface PurchaseOrder {
  id?: number;
  poNo?: string;
  supplierCode?: string;
  supplierName?: string;
  orderDate?: string;
  expectDate?: string;
  totalAmount?: number;
  status?: string;
  itemCode?: string;
  orderQty?: number;
  receivedQty?: number;
}

export const purchaseApi = {
  // 采购订单列表
  list: (params: {
    pageNum?: number;
    pageSize?: number;
    supplierCode?: string;
    status?: string;
  }) => api.get<{list: PurchaseOrder[]; total: number}>('/purchase-orders', { params }),
  
  // 采购订单详情
  get: (id: number) => api.get<PurchaseOrder>(`/purchase-orders/${id}`),
  
  // 创建采购订单
  create: (data: PurchaseOrder) => api.post<PurchaseOrder>('/purchase-orders', data),
  
  // 更新采购订单
  update: (id: number, data: PurchaseOrder) => api.put<void>(`/purchase-orders/${id}`, data),
  
  // 删除采购订单
  delete: (id: number) => api.delete<void>(`/purchase-orders/${id}`),
  
  // 确认订单
  confirm: (id: number) => api.post<void>(`/purchase-orders/${id}/confirm`),
  
  // 采购入库
  receive: (id: number, data: { qty: number }) => 
    api.post<void>(`/purchase-orders/${id}/receive`, data),
  
  // 取消订单
  cancel: (id: number) => api.post<void>(`/purchase-orders/${id}/cancel`),
  
  // 获取采购建议
  getSuggestions: (params?: { status?: string }) => 
    api.get<PurchaseOrder[]>('/purchase-orders/suggestions', { params }),
};
