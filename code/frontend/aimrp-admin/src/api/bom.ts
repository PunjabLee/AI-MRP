/**
 * BOM API
 */
import api from './index';

export interface Bom {
  id?: number;
  bomNo?: string;
  itemCode?: string;
  itemName?: string;
  version?: string;
  status?: string;
  effectiveDate?: string;
}

export interface BomLine {
  id?: number;
  bomId?: number;
  lineNo?: number;
  childItemCode?: string;
  childItemName?: string;
  usageQty?: number;
  lossRate?: number;
}

export const bomApi = {
  // BOM 列表
  list: (params?: {itemCode?: string; status?: string}) => 
    api.get<{list: Bom[]; total: number}>('/boms', { params }),
  
  // BOM 详情（包含行）
  getById: (id: number) => 
    api.get<{bom: Bom; lines: BomLine[]}>(`/boms/${id}`),
  
  // 创建 BOM
  create: (data: {bom: Bom; lines: BomLine[]}) => 
    api.post<Bom>('/boms', data),
  
  // 更新 BOM
  update: (id: number, data: {bom: Bom; lines: BomLine[]}) => 
    api.put<Bom>(`/boms/${id}`, data),
  
  // 删除 BOM
  delete: (id: number) => api.delete<void>(`/boms/${id}`),
  
  // BOM 展开
  expand: (itemCode: string, qty: number) => 
    api.get<{items: any[]; total: number}>(`/boms/expand/${itemCode}`, { params: { qty } }),
};
