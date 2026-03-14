/**
 * 生产工单 API
 */
import api from './index';

export interface ProductionOrder {
  id?: number;
  moNo?: string;
  itemCode?: string;
  itemName?: string;
  planQty?: number;
  completedQty?: number;
  status?: string;
  startDate?: string;
  endDate?: string;
}

export const productionApi = {
  // 工单列表
  list: (params: {
    pageNum?: number;
    pageSize?: number;
    itemCode?: string;
    status?: string;
  }) => api.get<{list: ProductionOrder[]; total: number}>('/production-orders', { params }),
  
  // 工单详情
  get: (id: number) => api.get<ProductionOrder>(`/production-orders/${id}`),
  
  // 创建工单
  create: (data: ProductionOrder) => api.post<ProductionOrder>('/production-orders', data),
  
  // 更新工单
  update: (id: number, data: ProductionOrder) => api.put<void>(`/production-orders/${id}`, data),
  
  // 删除工单
  delete: (id: number) => api.delete<void>(`/production-orders/${id}`),
  
  // 下达工单
  release: (id: number) => api.post<void>(`/production-orders/${id}/release`),
  
  // 开始生产
  start: (id: number) => api.post<void>(`/production-orders/${id}/start`),
  
  // 完工
  complete: (id: number, data: { completedQty: number }) => 
    api.post<void>(`/production-orders/${id}/complete`, data),
  
  // 报工
  report: (id: number, data: { reportQty: number; remark?: string }) => 
    api.post<void>(`/production-orders/${id}/report`, data),
  
  // 排程
  schedule: (params: { orderIds?: number[] }) => 
    api.post<ProductionOrder[]>('/production-orders/schedule', params),
};
