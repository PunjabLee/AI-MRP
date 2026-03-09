import { request } from './index';

export const purchaseApi = {
  list: (params?: any) => request.get('/api/purchase-orders', { params }),
  
  get: (id: number) => request.get(`/api/purchase-orders/${id}`),
  
  create: (data: any) => request.post('/api/purchase-orders', data),
  
  update: (id: number, data: any) => request.put(`/api/purchase-orders/${id}`, data),
  
  delete: (id: number) => request.delete(`/api/purchase-orders/${id}`),
  
  confirm: (id: number) => request.post(`/api/purchase-orders/${id}/confirm`),
  
  receive: (id: number, data: any) => request.post(`/api/purchase-orders/${id}/receive`, data),
  
  cancel: (id: number) => request.post(`/api/purchase-orders/${id}/cancel`),
  
  getSuggestions: (params?: any) => request.get('/api/purchase-orders/suggestions', { params }),
};
