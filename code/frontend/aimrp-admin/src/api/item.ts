/**
 * 物料 API
 */
import api from './index';

export interface Item {
  id?: number;
  itemCode?: string;
  itemName?: string;
  itemType?: string;
  unit?: string;
  source?: string;
  leadTime?: number;
  safetyStock?: number;
  minLotSize?: number;
  maxLotSize?: number;
  unitCost?: number;
  remark?: string;
}

export const itemApi = {
  // 物料列表
  list: (params: {
    pageNum?: number;
    pageSize?: number;
    keyword?: string;
    itemType?: string;
  }) => api.get<{list: Item[]; total: number}>('/items', { params }),
  
  // 物料详情
  get: (id: number) => api.get<Item>(`/items/${id}`),
  
  // 创建物料
  create: (data: Item) => api.post<Item>('/items', data),
  
  // 更新物料
  update: (id: number, data: Item) => api.put<void>(`/items/${id}`, data),
  
  // 删除物料
  delete: (id: number) => api.delete<void>(`/items/${id}`),
  
  // 按编码查询
  getByCode: (itemCode: string) => api.get<Item>(`/items/code/${itemCode}`),
};
