/**
 * 订单 API
 */
import api from './index';

export interface Order {
  id?: number;
  orderNo?: string;
  customerCode?: string;
  customerName?: string;
  orderDate?: string;
  dueDate?: string;
  status?: string;
  totalAmount?: number;
  memo?: string;
}

export interface OrderQuery {
  pageNum?: number;
  pageSize?: number;
  orderNo?: string;
  customerCode?: string;
  status?: string;
}

export const orderApi = {
  // 订单列表
  list: (params: OrderQuery) => api.get<{list: Order[]; total: number}>('/orders', { params }),
  
  // 订单详情
  getById: (id: number) => api.get<Order>(`/orders/${id}`),
  
  // 创建订单
  create: (data: Order) => api.post<Order>('/orders', data),
  
  // 更新订单
  update: (id: number, data: Order) => api.put<Order>(`/orders/${id}`, data),
  
  // 删除订单
  delete: (id: number) => api.delete<void>(`/orders/${id}`),
  
  // 确认订单
  confirm: (id: number) => api.post<Order>(`/orders/${id}/confirm`),
  
  // 取消订单
  cancel: (id: number) => api.post<Order>(`/orders/${id}/cancel`),
};
