/**
 * 报表 API
 */
import api from './index';

export interface ReportConfig {
  id?: number;
  reportCode?: string;
  reportName?: string;
  reportType?: string;
  status?: string;
}

export const reportApi = {
  // 报表列表
  list: (params: { reportType?: string; status?: string; keyword?: string }) => 
    api.get<{list: ReportConfig[]; total: number}>('/reports/configs', { params }),
  
  // 报表详情
  get: (id: number) => api.get<ReportConfig>(`/reports/configs/${id}`),
  
  // 创建报表
  create: (data: ReportConfig) => api.post<ReportConfig>('/reports/configs', data),
  
  // 更新报表
  update: (id: number, data: ReportConfig) => api.put<void>(`/reports/configs/${id}`, data),
  
  // 删除报表
  delete: (id: number) => api.delete<void>(`/reports/configs/${id}`),
  
  // 执行报表
  execute: (id: number) => api.post<any>(`/reports/execute/${id}`),
  
  // 获取报表数据
  getData: (reportType: string, params?: any) => api.get<any>('/reports/data', { params: { reportType, ...params } }),
};
