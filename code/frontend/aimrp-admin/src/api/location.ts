/**
 * 库位 API
 */
import api from './index';

export interface WarehouseLocation {
  id?: number;
  locationCode?: string;
  locationName?: string;
  warehouseCode?: string;
  warehouseName?: string;
  areaCode?: string;
  areaName?: string;
  locationType?: string;
  status?: string;
}

export const locationApi = {
  // 库位列表
  list: (params: {
    warehouseCode?: string;
    areaCode?: string;
    status?: string;
    keyword?: string;
    pageNum?: number;
    pageSize?: number;
  }) => api.get<{list: WarehouseLocation[]; total: number}>('/warehouse/locations', { params }),
  
  // 库位详情
  getById: (id: number) => api.get<WarehouseLocation>(`/warehouse/locations/${id}`),
  
  // 创建库位
  create: (data: WarehouseLocation) => api.post<WarehouseLocation>('/warehouse/locations', data),
  
  // 更新库位
  update: (id: number, data: WarehouseLocation) => api.put<void>(`/warehouse/locations/${id}`, data),
  
  // 更新状态
  updateStatus: (id: number, status: string) => 
    api.put<void>(`/warehouse/locations/${id}/status`, null, { params: { status } }),
  
  // 删除库位
  delete: (id: number) => api.delete<void>(`/warehouse/locations/${id}`),
  
  // 根据物料查询库位
  getByItemCode: (itemCode: string) => 
    api.get<WarehouseLocation[]>(`/warehouse/locations/by-item/${itemCode}`),
};
