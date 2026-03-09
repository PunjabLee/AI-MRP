import { request } from './index';

export const itemApi = {
  list: (params?: any) => request.get('/api/items', { params }),
  
  get: (id: number) => request.get(`/api/items/${id}`),
  
  create: (data: any) => request.post('/api/items', data),
  
  update: (id: number, data: any) => request.put(`/api/items/${id}`, data),
  
  delete: (id: number) => request.delete(`/api/items/${id}`),
  
  getByCode: (itemCode: string) => request.get(`/api/items/code/${itemCode}`),
};
