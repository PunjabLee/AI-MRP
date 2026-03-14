import { request } from './index';

export const whatIfApi = {
  list: (params?: any) => request.get('/api/whatif/scenarios', { params }),
  
  get: (id: number) => request.get(`/api/whatif/scenarios/${id}`),
  
  create: (data: any) => request.post('/api/whatif/scenarios', data),
  
  simulate: (data: any) => request.post('/api/whatif/simulate', data),
  
  compare: (scenarioId1: number, scenarioId2: number) => 
    request.post('/api/whatif/compare', { scenarioId1, scenarioId2 }),
  
  delete: (id: number) => request.delete(`/api/whatif/scenarios/${id}`),
};
