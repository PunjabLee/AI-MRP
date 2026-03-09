import { request } from './index';

export const supplierApi = {
  list: (params?: any) => request.get('/api/suppliers', { params }),
  
  get: (id: number) => request.get(`/api/suppliers/${id}`),
  
  create: (data: any) => request.post('/api/suppliers', data),
  
  update: (id: number, data: any) => request.put(`/api/suppliers/${id}`, data),
  
  delete: (id: number) => request.delete(`/api/suppliers/${id}`),
  
  getByCode: (supplierCode: string) => request.get(`/api/suppliers/code/${supplierCode}`),
  
  evaluate: (id: number) => request.post(`/api/suppliers/${id}/evaluate`),
};
