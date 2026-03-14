import { request } from './index';

export const riskApi = {
  list: (params?: any) => request.get('/api/risk/items', { params }),
  
  get: (id: number) => request.get(`/api/risk/items/${id}`),
  
  scan: () => request.post('/api/risk/scan'),
  
  getWarnings: (params?: any) => request.get('/api/risk/warnings', { params }),
  
  handle: (id: number, data: any) => request.post(`/api/risk/warnings/${id}/handle`, data),
  
  getStatistics: () => request.get('/api/risk/statistics'),
};
