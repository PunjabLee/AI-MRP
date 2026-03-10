/**
 * 成本 API
 */
import api from './index';

export interface CostElement {
  id?: number;
  costCode?: string;
  costName?: string;
  costType?: string;
  unitCost?: number;
  unit?: string;
}

export const costApi = {
  // 成本要素列表
  list: (params: { costType?: string; keyword?: string }) => 
    api.get<{list: CostElement[]; total: number}>('/cost/elements', { params }),
  
  // 成本要素详情
  get: (id: number) => api.get<CostElement>(`/cost/elements/${id}`),
  
  // 创建成本要素
  create: (data: CostElement) => api.post<CostElement>('/cost/elements', data),
  
  // 更新成本要素
  update: (id: number, data: CostElement) => api.put<void>(`/cost/elements/${id}`, data),
  
  // 删除成本要素
  delete: (id: number) => api.delete<void>(`/cost/elements/${id}`),
  
  // 计算产品成本
  calculate: (itemCode: string) => api.post<any>('/cost/calculate', { itemCode }),
};
