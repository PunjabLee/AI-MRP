import { request } from './index';

export const bomApi = {
  list: (params?: any) => request.get('/api/boms', { params }),
  
  get: (id: number) => request.get(`/api/boms/${id}`),
  
  create: (data: any) => request.post('/api/boms', data),
  
  update: (id: number, data: any) => request.put(`/api/boms/${id}`, data),
  
  delete: (id: number) => request.delete(`/api/boms/${id}`),
  
  expand: (itemCode: string, qty: number, level?: number) => 
    request.post('/api/boms/expand', { itemCode, qty, level }),
};
