import { request } from './index';

export const productionApi = {
  list: (params?: any) => request.get('/api/production-orders', { params }),
  
  get: (id: number) => request.get(`/api/production-orders/${id}`),
  
  create: (data: any) => request.post('/api/production-orders', data),
  
  update: (id: number, data: any) => request.put(`/api/production-orders/${id}`, data),
  
  delete: (id: number) => request.delete(`/api/production-orders/${id}`),
  
  start: (id: number) => request.post(`/api/production-orders/${id}/start`),
  
  complete: (id: number, data?: any) => request.post(`/api/production-orders/${id}/complete`, data),
  
  report: (id: number, data: any) => request.post(`/api/production-orders/${id}/report`, data),
  
  schedule: (params?: any) => request.post('/api/production-orders/schedule', params),
};
