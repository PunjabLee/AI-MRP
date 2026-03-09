import { request } from './index';

export const forecastApi = {
  // 需求预测
  forecast: (data: {
    itemCode: string;
    history: Array<{ date: string; qty: number }>;
    periods?: number;
    method?: string;
  }) => request.post('/api/forecast/forecast', data),
  
  // 获取预测历史
  getHistory: (params?: { itemCode?: string; startDate?: string; endDate?: string }) => 
    request.get('/api/forecast/history', { params }),
  
  // 获取预测结果
  getResults: (params?: { itemCode?: string }) => 
    request.get('/api/forecast/results', { params }),
  
  // 安全库存推荐
  recommendSafetyStock: (data: {
    itemCode: string;
    avgDemand: number;
    demandHistory: number[];
    leadTime?: number;
    method?: string;
    serviceLevel?: number;
  }) => request.post('/api/forecast/safety-stock', data),
  
  // 获取安全库存建议列表
  getSafetyStockList: (params?: { itemCode?: string }) => 
    request.get('/api/forecast/safety-stock/list', { params }),
};
