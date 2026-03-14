/**
 * 供应商 API
 */
import api from './index';

export interface Supplier {
  id?: number;
  supplierCode?: string;
  supplierName?: string;
  contact?: string;
  phone?: string;
  email?: string;
  address?: string;
  leadTimeDays?: number;
  paymentTerms?: string;
  status?: string;
  rating?: number;
}

export const supplierApi = {
  // 供应商列表
  list: (params: {
    keyword?: string;
    status?: string;
    pageNum?: number;
    pageSize?: number;
  }) => api.get<{list: Supplier[]; total: number}>('/suppliers', { params }),
  
  // 供应商详情
  get: (id: number) => api.get<Supplier>(`/suppliers/${id}`),
  
  // 创建供应商
  create: (data: Supplier) => api.post<Supplier>('/suppliers', data),
  
  // 更新供应商
  update: (id: number, data: Supplier) => api.put<void>(`/suppliers/${id}`, data),
  
  // 删除供应商
  delete: (id: number) => api.delete<void>(`/suppliers/${id}`),
  
  // 按编码查询
  getByCode: (supplierCode: string) => api.get<Supplier>(`/suppliers/code/${supplierCode}`),
  
  // 供应商评估
  evaluate: (id: number) => api.post<void>(`/suppliers/${id}/evaluate`),
};
