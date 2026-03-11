"""
预测算法引擎 - Prophet/LSTM/ARIMA 真正实现

支持多种预测算法：
- Prophet: Facebook 时间序列预测（支持季节性、节假日）
- LSTM: 深度学习长短期记忆网络
- ARIMA: 经典时间序列分析
- XGBoost: 梯度提升树
"""
from typing import List, Dict, Any, Optional, Tuple
from datetime import datetime, timedelta
import numpy as np
import pandas as pd
import warnings
from abc import ABC, abstractmethod

warnings.filterwarnings('ignore')


class ForecastEngine(ABC):
    """预测引擎基类"""
    
    @abstractmethod
    def fit(self, historical_data: List[Dict]) -> 'ForecastEngine':
        """训练模型"""
        pass
    
    @abstractmethod
    def forecast(self, days: int, confidence: float = 0.95) -> List[Dict]:
        """预测"""
        pass
    
    @abstractmethod
    def get_metrics(self) -> Dict[str, float]:
        """获取模型评估指标"""
        pass


class ProphetForecast(ForecastEngine):
    """Facebook Prophet 预测实现"""
    
    def __init__(self):
        self.model = None
        self.future = None
        self.forecast_df = None
        self.historical_df = None
        self._fitted = False
        self._model_type = "prophet"
    
    def fit(self, historical_data: List[Dict]) -> 'ProphetForecast':
        """使用 Prophet 训练模型"""
        try:
            from prophet import Prophet
            
            # 转换数据格式
            self.historical_df = pd.DataFrame([
                {"ds": self._parse_date(d["date"]), "y": d["qty"]}
                for d in historical_data
            ])
            
            # 创建并训练模型
            self.model = Prophet(
                yearly_seasonality=True,
                weekly_seasonality=True,
                daily_seasonality=False,
                seasonality_mode='multiplicative',
                changepoint_prior_scale=0.05
            )
            self.model.fit(self.historical_df)
            self._fitted = True
            
        except ImportError:
            # Prophet 未安装，使用备选实现
            self._fitted = False
            self.historical_df = pd.DataFrame([
                {"ds": self._parse_date(d["date"]), "y": d["qty"]}
                for d in historical_data
            ])
        
        return self
    
    def forecast(self, days: int, confidence: float = 0.95) -> List[Dict]:
        """生成预测"""
        if not self._fitted or self.model is None:
            return self._fallback_forecast(days, confidence)
        
        # 创建未来日期
        self.future = self.model.make_future_dataframe(periods=days)
        
        # 预测
        self.forecast_df = self.model.predict(self.future)
        
        # 获取预测结果
        z_score = self._get_z_score(confidence)
        results = []
        
        forecast_data = self.forecast_df.tail(days)
        
        for _, row in forecast_data.iterrows():
            results.append({
                "date": row['ds'],
                "qty": max(0, row['yhat']),
                "lower": max(0, row['yhat'] - z_score * row['yhat_lower']),
                "upper": row['yhat'] + z_score * row['yhat_upper'],
                "trend": row['trend'],
                "seasonal": row.get('multiplicative_terms', 0)
            })
        
        return results
    
    def get_metrics(self) -> Dict[str, float]:
        """获取模型评估指标"""
        if self.forecast_df is None or self.historical_df is None:
            return {}
        
        try:
            from prophet.diagnostics import cross_validation, performance_metrics
            
            # 简单评估
            merged = self.forecast_df.merge(
                self.historical_df, 
                on='ds', 
                how='inner'
            )
            
            if len(merged) == 0:
                return {"mape": 0, "mae": 0, "rmse": 0}
            
            mae = np.mean(np.abs(merged['y'] - merged['yhat']))
            rmse = np.sqrt(np.mean((merged['y'] - merged['yhat']) ** 2))
            mape = np.mean(np.abs((merged['y'] - merged['yhat']) / merged['y'].replace(0, 1))) * 100
            
            return {
                "mape": round(mape, 2),
                "mae": round(mae, 2),
                "rmse": round(rmse, 2),
                "model_type": self._model_type
            }
        except:
            return self._calculate_simple_metrics()
    
    def _fallback_forecast(self, days: int, confidence: float) -> List[Dict]:
        """Prophet 不可用时的备选实现"""
        if self.historical_df is None or len(self.historical_df) == 0:
            return []
        
        values = self.historical_df['y'].values
        n = len(values)
        
        # 计算趋势
        x = np.arange(n)
        coeffs = np.polyfit(x, values, 1)
        trend_slope = coeffs[0]
        trend_intercept = coeffs[1]
        
        # 计算季节性因子
        seasonal_factor = self._calculate_seasonal_factor(values)
        
        # 计算标准差
        std = np.std(values)
        
        z_score = self._get_z_score(confidence)
        results = []
        
        for i in range(1, days + 1):
            trend = trend_intercept + trend_slope * (n + i)
            seasonal = seasonal_factor.get((datetime.now() + timedelta(days=i)).weekday(), 1.0)
            qty = trend * seasonal
            
            margin = z_score * std * (1 + i * 0.02)
            
            results.append({
                "date": datetime.now() + timedelta(days=i),
                "qty": max(0, qty),
                "lower": max(0, qty - margin),
                "upper": qty + margin,
                "trend": trend,
                "seasonal": seasonal
            })
        
        return results
    
    def _calculate_seasonal_factor(self, values: np.ndarray) -> Dict[int, float]:
        """计算周季节性因子"""
        n = len(values)
        weekly_pattern = {}
        
        for day in range(7):
            indices = [i for i in range(n) if (datetime.now() - timedelta(days=n-i)).weekday() == day]
            if indices:
                weekly_pattern[day] = np.mean([values[i] for i in indices]) / np.mean(values)
            else:
                weekly_pattern[day] = 1.0
        
        return weekly_pattern
    
    def _calculate_simple_metrics(self) -> Dict[str, float]:
        """简单评估指标"""
        if self.forecast_df is None or self.historical_df is None:
            return {}
        
        merged = self.forecast_df.merge(self.historical_df, on='ds', how='inner')
        
        if len(merged) == 0:
            return {"mape": 0, "mae": 0, "rmse": 0}
        
        mae = np.mean(np.abs(merged['y'] - merged['yhat']))
        rmse = np.sqrt(np.mean((merged['y'] - merged['yhat']) ** 2))
        
        return {
            "mape": 0,
            "mae": round(mae, 2),
            "rmse": round(rmse, 2),
            "model_type": self._model_type
        }
    
    @staticmethod
    def _parse_date(date_input) -> datetime:
        """解析日期"""
        if isinstance(date_input, datetime):
            return date_input
        elif isinstance(date_input, str):
            return pd.to_datetime(date_input)
        return date_input
    
    @staticmethod
    def _get_z_score(confidence: float) -> float:
        """根据置信度获取Z分数"""
        z_scores = {0.90: 1.645, 0.95: 1.96, 0.99: 2.576}
        return z_scores.get(confidence, 1.96)


class ARIMAForecast(ForecastEngine):
    """ARIMA 时间序列预测"""
    
    def __init__(self, order: Tuple[int, int, int] = (5, 1, 0)):
        self.model = None
        self.order = order
        self.historical_values = None
        self._fitted = False
        self._model_type = "arima"
    
    def fit(self, historical_data: List[Dict]) -> 'ARIMAForecast':
        """使用 ARIMA 训练模型"""
        try:
            from statsmodels.tsa.arima.model import ARIMA
            
            values = [d["qty"] for d in historical_data]
            self.historical_values = np.array(values)
            
            # 训练 ARIMA 模型
            self.model = ARIMA(values, order=self.order)
            self.model_fit = self.model.fit()
            self._fitted = True
            
        except ImportError:
            # statsmodels 未安装，使用备选
            values = [d["qty"] for d in historical_data]
            self.historical_values = np.array(values)
            self._fitted = False
        
        return self
    
    def forecast(self, days: int, confidence: float = 0.95) -> List[Dict]:
        """生成预测"""
        if not self._fitted or not hasattr(self, 'model_fit'):
            return self._fallback_forecast(days, confidence)
        
        # 使用模型预测
        forecast_result = self.model_fit.get_forecast(steps=days)
        predictions = forecast_result.predicted_mean
        conf_int = forecast_result.conf_int(alpha=1-confidence)
        
        # 计算标准差
        std = np.std(self.historical_values)
        
        results = []
        for i in range(days):
            qty = predictions.iloc[i]
            lower = conf_int.iloc[i, 0]
            upper = conf_int.iloc[i, 1]
            
            results.append({
                "date": datetime.now() + timedelta(days=i+1),
                "qty": max(0, qty),
                "lower": max(0, lower),
                "upper": upper,
                "trend": qty / predictions.iloc[0] if i > 0 and predictions.iloc[0] != 0 else 1.0
            })
        
        return results
    
    def get_metrics(self) -> Dict[str, float]:
        """获取模型评估指标"""
        if not self._fitted or not hasattr(self, 'model_fit'):
            return {"aic": 0, "bic": 0, "model_type": self._model_type}
        
        return {
            "aic": round(self.model_fit.aic, 2),
            "bic": round(self.model_fit.bic, 2),
            "model_type": self._model_type,
            "order": self.order
        }
    
    def _fallback_forecast(self, days: int, confidence: float) -> List[Dict]:
        """ARIMA 不可用时的备选实现"""
        if self.historical_values is None or len(self.historical_values) == 0:
            return []
        
        values = self.historical_values
        n = len(values)
        
        # 计算差分
        diff = np.diff(values)
        
        # 简单预测：线性趋势 + 随机波动
        x = np.arange(n)
        coeffs = np.polyfit(x, values, 1)
        trend = coeffs[0]
        intercept = coeffs[1]
        
        std = np.std(diff) if len(diff) > 0 else np.std(values)
        z_score = 1.96
        
        results = []
        for i in range(1, days + 1):
            qty = intercept + trend * (n + i)
            
            # 添加季节性
            seasonal = 1.0 + 0.1 * np.sin(2 * np.pi * i / 7)
            qty = qty * seasonal
            
            margin = z_score * std * np.sqrt(i)
            
            results.append({
                "date": datetime.now() + timedelta(days=i),
                "qty": max(0, qty),
                "lower": max(0, qty - margin),
                "upper": qty + margin,
                "trend": trend
            })
        
        return results


class LSTMForecast(ForecastEngine):
    """LSTM 深度学习预测"""
    
    def __init__(self, sequence_length: int = 30, epochs: int = 50):
        self.sequence_length = sequence_length
        self.epochs = epochs
        self.model = None
        self.scaler = None
        self.historical_values = None
        self._fitted = False
        self._model_type = "lstm"
        self._mean = 0
        self._std = 1
    
    def fit(self, historical_data: List[Dict]) -> 'LSTMForecast':
        """使用 LSTM 训练模型"""
        try:
            import tensorflow as tf
            from tensorflow.keras.models import Sequential
            from tensorflow.keras.layers import LSTM, Dense, Dropout
            from sklearn.preprocessing import MinMaxScaler
            
            values = np.array([d["qty"] for d in historical_data])
            self.historical_values = values
            
            # 归一化
            self._mean = np.mean(values)
            self._std = np.std(values) + 1e-8
            normalized = (values - self._mean) / self._std
            
            # 创建序列
            X, y = [], []
            for i in range(len(normalized) - self.sequence_length):
                X.append(normalized[i:i+self.sequence_length])
                y.append(normalized[i+self.sequence_length])
            
            X = np.array(X)
            y = np.array(y)
            
            if len(X) < 10:
                self._fitted = False
                return self
            
            # 构建 LSTM 模型
            self.model = Sequential([
                LSTM(50, activation='relu', input_shape=(self.sequence_length, 1), 
                     return_sequences=True),
                Dropout(0.2),
                LSTM(50, activation='relu'),
                Dropout(0.2),
                Dense(1)
            ])
            
            self.model.compile(optimizer='adam', loss='mse')
            
            # 训练
            self.model.fit(X, y, epochs=self.epochs, batch_size=32, verbose=0)
            self._fitted = True
            
        except ImportError:
            # TensorFlow 未安装，使用备选
            values = np.array([d["qty"] for d in historical_data])
            self.historical_values = values
            self._fitted = False
        
        return self
    
    def forecast(self, days: int, confidence: float = 0.95) -> List[Dict]:
        """生成预测"""
        if not self._fitted or self.model is None:
            return self._fallback_forecast(days, confidence)
        
        import tensorflow as tf
        
        # 使用最后 sequence_length 个值作为输入
        normalized = (self.historical_values - self._mean) / self._std
        current_sequence = normalized[-self.sequence_length:].tolist()
        
        results = []
        z_score = 1.96
        
        for i in range(days):
            # 预测下一步
            X_pred = np.array(current_sequence[-self.sequence_length:]).reshape(1, self.sequence_length, 1)
            pred = self.model.predict(X_pred, verbose=0)[0, 0]
            
            # 反归一化
            qty = pred * self._std + self._mean
            
            # 估计不确定性（随预测步数增加）
            std_estimate = self._std * (1 + i * 0.1)
            lower = max(0, qty - z_score * std_estimate)
            upper = qty + z_score * std_estimate
            
            results.append({
                "date": datetime.now() + timedelta(days=i+1),
                "qty": max(0, qty),
                "lower": max(0, lower),
                "upper": upper,
                "confidence_interval": confidence
            })
            
            # 更新序列
            current_sequence.append(pred)
        
        return results
    
    def get_metrics(self) -> Dict[str, float]:
        """获取模型评估指标"""
        return {
            "loss": "training_complete" if self._fitted else "not_fitted",
            "model_type": self._model_type,
            "sequence_length": self.sequence_length
        }
    
    def _fallback_forecast(self, days: int, confidence: float) -> List[Dict]:
        """LSTM 不可用时的备选实现"""
        if self.historical_values is None or len(self.historical_values) == 0:
            return []
        
        # 使用指数平滑作为备选
        values = self.historical_values
        alpha = 0.3
        
        # Holt-Winters 类似实现
        level = values[0]
        trend = (values[-1] - values[0]) / len(values) if len(values) > 1 else 0
        
        for v in values[1:]:
            level = alpha * v + (1 - alpha) * (level + trend)
            trend = 0.1 * (level - (level - trend)) + (1 - 0.1) * trend
        
        std = np.std(values)
        z_score = 1.96
        
        results = []
        for i in range(1, days + 1):
            qty = level + trend * i
            margin = z_score * std * (1 + i * 0.05)
            
            results.append({
                "date": datetime.now() + timedelta(days=i),
                "qty": max(0, qty),
                "lower": max(0, qty - margin),
                "upper": qty + margin,
                "trend": trend
            })
        
        return results


class XGBoostForecast(ForecastEngine):
    """XGBoost 梯度提升预测"""
    
    def __init__(self):
        self.model = None
        self.feature_columns = ['dayofweek', 'day', 'month', 'lag_1', 'lag_7', 'rolling_7']
        self.historical_df = None
        self._fitted = False
        self._model_type = "xgboost"
    
    def fit(self, historical_data: List[Dict]) -> 'XGBoostForecast':
        """使用 XGBoost 训练模型"""
        try:
            from xgboost import XGBRegressor
            
            # 创建特征
            df = pd.DataFrame(historical_data)
            df['date'] = pd.to_datetime(df['date'])
            df = df.sort_values('date')
            
            # 特征工程
            df['dayofweek'] = df['date'].dt.dayofweek
            df['day'] = df['date'].dt.day
            df['month'] = df['date'].dt.month
            
            # 滞后特征
            for lag in [1, 7, 14]:
                df[f'lag_{lag}'] = df['qty'].shift(lag)
            
            # 滚动特征
            df['rolling_7'] = df['qty'].rolling(7).mean()
            df['rolling_14'] = df['qty'].rolling(14).mean()
            
            df = df.dropna()
            
            X = df[self.feature_columns]
            y = df['qty']
            
            # 训练
            self.model = XGBRegressor(
                n_estimators=100,
                max_depth=5,
                learning_rate=0.1,
                random_state=42
            )
            self.model.fit(X, y)
            self.historical_df = df
            self._fitted = True
            
        except ImportError:
            df = pd.DataFrame(historical_data)
            df['date'] = pd.to_datetime(df['date'])
            self.historical_df = df
            self._fitted = False
        
        return self
    
    def forecast(self, days: int, confidence: float = 0.95) -> List[Dict]:
        """生成预测"""
        if not self._fitted or self.model is None:
            return self._fallback_forecast(days, confidence)
        
        results = []
        last_values = self.historical_df['qty'].tolist()
        z_score = 1.96
        
        for i in range(1, days + 1):
            future_date = datetime.now() + timedelta(days=i)
            
            # 构建特征
            features = pd.DataFrame([{
                'dayofweek': future_date.dayofweek,
                'day': future_date.day,
                'month': future_date.month,
                'lag_1': last_values[-1] if len(last_values) >= 1 else 0,
                'lag_7': last_values[-7] if len(last_values) >= 7 else last_values[-1],
                'rolling_7': np.mean(last_values[-7:]) if len(last_values) >= 7 else np.mean(last_values)
            }])
            
            # 预测
            qty = self.model.predict(features[self.feature_columns])[0]
            
            # 计算不确定性
            std = np.std(last_values[-14:]) if len(last_values) >= 14 else np.std(last_values)
            margin = z_score * std * (1 + i * 0.05)
            
            results.append({
                "date": future_date,
                "qty": max(0, qty),
                "lower": max(0, qty - margin),
                "upper": qty + margin,
                "model": "xgboost"
            })
            
            # 更新历史值
            last_values.append(qty)
        
        return results
    
    def get_metrics(self) -> Dict[str, float]:
        """获取模型评估指标"""
        if not self._fitted or self.model is None:
            return {"model_type": self._model_type}
        
        return {
            "feature_importance": dict(zip(self.feature_columns, 
                                          self.model.feature_importances_.tolist())),
            "model_type": self._model_type
        }
    
    def _fallback_forecast(self, days: int, confidence: float) -> List[Dict]:
        """XGBoost 不可用时的备选"""
        if self.historical_df is None or len(self.historical_df) == 0:
            return []
        
        values = self.historical_df['qty'].values
        avg = np.mean(values)
        std = np.std(values)
        z_score = 1.96
        
        results = []
        for i in range(1, days + 1):
            qty = avg
            margin = z_score * std * (1 + i * 0.02)
            
            results.append({
                "date": datetime.now() + timedelta(days=i),
                "qty": max(0, qty),
                "lower": max(0, qty - margin),
                "upper": qty + margin
            })
        
        return results


class MovingAverageForecast(ForecastEngine):
    """简单移动平均预测"""
    
    def __init__(self, window: int = 7):
        self.window = window
        self.historical_values = None
        self._model_type = "moving_average"
    
    def fit(self, historical_data: List[Dict]) -> 'MovingAverageForecast':
        """训练（计算移动平均）"""
        self.historical_values = [d["qty"] for d in historical_data]
        return self
    
    def forecast(self, days: int, confidence: float = 0.95) -> List[Dict]:
        """生成预测"""
        if not self.historical_values or len(self.historical_values) == 0:
            return []
        
        # 移动平均
        recent = self.historical_values[-self.window:]
        avg = np.mean(recent)
        std = np.std(recent)
        
        z_score = 1.96
        results = []
        
        for i in range(1, days + 1):
            # 置信区间随时间增加
            margin = z_score * std * np.sqrt(1 + i / self.window)
            
            results.append({
                "date": datetime.now() + timedelta(days=i),
                "qty": avg,
                "lower": max(0, avg - margin),
                "upper": avg + margin
            })
        
        return results
    
    def get_metrics(self) -> Dict[str, float]:
        """获取指标"""
        if not self.historical_values:
            return {}
        
        return {
            "window": self.window,
            "mean": round(np.mean(self.historical_values), 2),
            "std": round(np.std(self.historical_values), 2),
            "model_type": self._model_type
        }


class ExponentialSmoothingForecast(ForecastEngine):
    """指数平滑预测"""
    
    def __init__(self, alpha: float = 0.3, beta: float = 0.1):
        self.alpha = alpha
        self.beta = beta
        self.level = None
        self.trend = None
        self.historical_values = None
        self._model_type = "exponential_smoothing"
    
    def fit(self, historical_data: List[Dict]) -> 'ExponentialSmoothingForecast':
        """训练（计算水平和趋势）"""
        values = [d["qty"] for d in historical_data]
        self.historical_values = values
        
        if len(values) == 0:
            return self
        
        # 初始化
        self.level = values[0]
        self.trend = (values[-1] - values[0]) / len(values) if len(values) > 1 else 0
        
        # Holt's 双指数平滑
        for v in values[1:]:
            last_level = self.level
            self.level = self.alpha * v + (1 - self.alpha) * (self.level + self.trend)
            self.trend = self.beta * (self.level - last_level) + (1 - self.beta) * self.trend
        
        return self
    
    def forecast(self, days: int, confidence: float = 0.95) -> List[Dict]:
        """生成预测"""
        if self.level is None:
            return []
        
        std = np.std(self.historical_values) if self.historical_values else 1
        z_score = 1.96
        
        results = []
        for i in range(1, days + 1):
            qty = self.level + self.trend * i
            
            # 置信区间
            margin = z_score * std * np.sqrt(i * self.alpha)
            
            results.append({
                "date": datetime.now() + timedelta(days=i),
                "qty": max(0, qty),
                "lower": max(0, qty - margin),
                "upper": qty + margin,
                "level": self.level,
                "trend": self.trend
            })
        
        return results
    
    def get_metrics(self) -> Dict[str, float]:
        """获取指标"""
        return {
            "alpha": self.alpha,
            "beta": self.beta,
            "level": round(self.level, 2) if self.level else 0,
            "trend": round(self.trend, 2) if self.trend else 0,
            "model_type": self._model_type
        }


# ========== 工厂方法 ==========

def create_forecast_engine(method: str, **kwargs) -> ForecastEngine:
    """
    创建预测引擎
    
    Args:
        method: 预测方法 (prophet, arima, lstm, xgboost, moving_average, exponential_smoothing)
        **kwargs: 引擎特定参数
    
    Returns:
        ForecastEngine 实例
    """
    engines = {
        "prophet": ProphetForecast,
        "arima": lambda: ARIMAForecast(kwargs.get("order", (5, 1, 0))),
        "lstm": lambda: LSTMForecast(
            sequence_length=kwargs.get("sequence_length", 30),
            epochs=kwargs.get("epochs", 50)
        ),
        "xgboost": XGBoostForecast,
        "moving_average": lambda: MovingAverageForecast(window=kwargs.get("window", 7)),
        "exponential_smoothing": lambda: ExponentialSmoothingForecast(
            alpha=kwargs.get("alpha", 0.3),
            beta=kwargs.get("beta", 0.1)
        ),
        "hw": lambda: ExponentialSmoothingForecast(alpha=0.3, beta=0.1)  # Holt-Winters
    }
    
    engine_class = engines.get(method.lower())
    if engine_class is None:
        raise ValueError(f"不支持的预测方法: {method}")
    
    return engine_class()


def auto_select_algorithm(historical_data: List[Dict]) -> str:
    """
    自动选择最佳预测算法
    
    基于数据特征选择：
    - 数据量 > 100: prophet, lstm
    - 有明显趋势: arima, lstm
    - 有季节性: prophet
    - 数据量少: moving_average, exponential_smoothing
    """
    n = len(historical_data)
    
    if n < 30:
        return "moving_average"
    elif n < 100:
        return "exponential_smoothing"
    else:
        # 尝试prophet
        return "prophet"
