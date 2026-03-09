/**
 * API 服务基础配置
 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

interface ApiResponse<T> {
  code: number;
  msg: string;
  data: T;
  timestamp: number;
}

class ApiService {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`;
    
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.statusText}`);
    }

    const result: ApiResponse<T> = await response.json();
    
    if (result.code !== 200) {
      throw new Error(result.msg || 'Request failed');
    }

    return result.data;
  }

  get<T>(endpoint: string, options?: { params?: any }): Promise<T> {
    let url = endpoint;
    if (options?.params) {
      const searchParams = new URLSearchParams(options.params);
      url += `?${searchParams.toString()}`;
    }
    return this.request<T>(url, { method: 'GET' });
  }

  post<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  put<T>(endpoint: string, data: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }
}

export const request = new ApiService(API_BASE_URL);
export default request;

// 导出各模块 API
export { orderApi } from './order';
export { inventoryApi } from './inventory';
export { mrpApi } from './mrp';
export { bomApi } from './bom';
export { itemApi } from './item';
export { supplierApi } from './supplier';
export { purchaseApi } from './purchase';
export { productionApi } from './production';
export { riskApi } from './risk';
export { whatIfApi } from './whatif';
