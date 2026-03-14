/**
 * MPS API
 */
import api from './index';

export interface MpsPlan {
  id?: number;
  planNo?: string;
  itemCode?: string;
  itemName?: string;
  planQty?: number;
  scheduledQty?: number;
  startDate?: string;
  endDate?: string;
  status?: string;
}

export const mpsApi = {
  // MPS计划列表
  list: (params: { status?: string; itemCode?: string }) => 
    api.get<{list: MpsPlan[]; total: number}>('/mps/plans', { params }),
  
  // MPS计划详情
  get: (id: number) => api.get<MpsPlan>(`/mps/plans/${id}`),
  
  // 创建MPS计划
  create: (data: MpsPlan) => api.post<MpsPlan>('/mps/plans', data),
  
  // 更新MPS计划
  update: (id: number, data: MpsPlan) => api.put<void>(`/mps/plans/${id}`, data),
  
  // 下达MPS计划
  release: (id: number) => api.post<void>(`/mps/plans/${id}/release`),
  
  // 获取MPS建议
  getSuggestions: () => api.get<any[]>('/mps/suggestions'),
};
