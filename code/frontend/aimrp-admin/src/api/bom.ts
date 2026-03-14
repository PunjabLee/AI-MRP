/**
 * BOM API
 */
import api from './index';

export interface BomItem {
  id?: number;
  parentItemCode?: string;
  parentItemName?: string;
  childItemCode?: string;
  childItemName?: string;
  usageQty?: number;
  lossRate?: number;
  level?: number;
  validDate?: string;
  status?: string;
}

export const bomApi = {
  // BOM列表
  list: (params: { parentItemCode?: string; keyword?: string }) => 
    api.get<{list: BomItem[]; total: number}>('/boms', { params }),
  
  // BOM详情
  get: (id: number) => api.get<BomItem>(`/boms/${id}`),
  
  // 创建BOM
  create: (data: BomItem) => api.post<BomItem>('/boms', data),
  
  // 更新BOM
  update: (id: number, data: BomItem) => api.put<void>(`/boms/${id}`, data),
  
  // 删除BOM
  delete: (id: number) => api.delete<void>(`/boms/${id}`),
  
  // BOM展开
  expand: (itemCode: string, qty: number, level?: number) => 
    api.post<BomItem[]>('/boms/expand', { itemCode, qty, level }),
  
  // 获取物料的BOM
  getByParent: (parentItemCode: string) => 
    api.get<BomItem[]>(`/boms/parent/${parentItemCode}`),
};
