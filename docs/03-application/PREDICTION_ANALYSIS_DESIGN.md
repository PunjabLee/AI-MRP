# AI MRP 预测分析模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 功能概述

预测分析模块利用AI算法预测未来需求，为MRP提供输入。

### 1.2 预测类型

| 类型 | 说明 |
|------|------|
| 需求预测 | 预测未来销量/需求量 |
| 交期预测 | 预测供应商交期 |
| 库存预测 | 预测未来库存水平 |
| 安全库存推荐 | AI推荐最优安全库存 |

---

## 二、算法设计

### 2.1 时间序列预测

```python
# 预测模型
class DemandForecast:
    
    # 移动平均
    def moving_average(self, data, window=3):
        return data.rolling(window).mean()
    
    # 指数平滑
    def exponential_smoothing(self, data, alpha=0.3):
        result = [data[0]]
        for n in range(1, len(data)):
            result.append(alpha * data[n] + (1 - alpha) * result[n-1])
        return result
    
    # ARIMA
    def arima(self, data, order=(1,1,1)):
        model = ARIMA(data, order=order)
        return model.fit().forecast(steps=self.forecast_horizon)
```

### 2.2 安全库存计算

```python
# 安全库存公式
def safety_stock(demand_mean, demand_std, lead_time, service_level=0.95):
    # 需求不确定性
    demand_factor = demand_std * math.sqrt(lead_time)
    
    # 交期不确定性  
    lt_factor = lead_time * demand_std
    
    # 服务水平系数 (95% = 1.65)
    service_factor = norm.ppf(service_level)
    
    return service_factor * math.sqrt(demand_factor**2 + lt_factor**2)
```

---

## 三、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/forecast/demand` | POST | 需求预测 |
| `/api/forecast/delivery` | POST | 交期预测 |
| `/api/forecast/safety-stock` | POST | 安全库存推荐 |
| `/api/forecast/history` | GET | 预测历史 |

---

## 四、预测流程

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  历史数据   │───▶│  数据清洗   │───▶│  模型训练   │───▶│  预测输出   │
└──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
```

---

## 五、预测结果

```python
# 预测结果
class ForecastResult:
    item_id: str
    forecast_date: date
    quantity: float
    confidence_lower: float   # 置信区间下限
    confidence_upper: float   # 置信区间上限
    confidence: float         # 置信度
    model: str               # 使用的模型
    accuracy: float          # 准确度(回测)
```

---

## 六、模型管理

```python
# 模型版本
class ModelVersion:
    model_id: str
    version: str
    accuracy: float
    trained_at: datetime
    status: str  # ACTIVE/DEPRECATED
    
# 模型选择
def select_best_model(item_id, historical_data):
    models = ['moving_average', 'exponential', 'arima', 'prophet']
    best = None
    best_accuracy = -1
    
    for model in models:
        accuracy = cross_validate(model, historical_data)
        if accuracy > best_accuracy:
            best = model
            best_accuracy = accuracy
            
    return best
```

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
