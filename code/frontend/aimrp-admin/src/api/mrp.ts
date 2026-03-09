/**
 * MRP API
 */
import api from './index';

export interface MrpRun {
  id?: number;
  runNo?: string;
  runType?: string;
  status?: string;
  planStartDate?: string;
  planEndDate?: string;
}

export interface MrpResult {
  runId?: number;
  status?: string;
  statistics?: {
    totalItems?: number;
    totalDemands?: number;
    totalSuggestions?: number;
    purchaseSuggestions?: number;
    productionSuggestions?: number;
  };
}

export interface MrpSuggestion {
  id?: number;
  suggestionType?: string;
  itemCode?: string;
  itemName?: string;
  suggestQty?: number;
  needDate?: string;
  status?: string;
}

export const mrpApi = {
  // 执行 MRP
  run: (params?: {runType?: string; planStartDate?: string; planEndDate?: string}) =>
    api.post<MrpResult>('/mrp/run', params),
  
  // 获取结果
  getResult: (runId: number) =>
    api.get<MrpResult>(`/mrp/result/${runId}`),
  
  // 获取参数
  getParameters: () =>
    api.get<Record<string, any>>('/mrp/parameters'),
  
  // 更新参数
  updateParameters: (params: Record<string, any>) =>
    api.put<void>('/mrp/parameters', params),
  
  // 建议列表
  getSuggestions: (params?: {suggestionType?: string; status?: string}) =>
    api.get<{suggestions: MrpSuggestion[]; total: number}>('/mrp/suggestions', { params }),
  
  // 确认建议
  acceptSuggestion: (id: number) =>
    api.post<void>(`/mrp/suggestions/${id}/accept`),
  
  // 拒绝建议
  rejectSuggestion: (id: number, reason: string) =>
    api.post<void>(`/mrp/suggestions/${id}/reject`, { reason }),
};
