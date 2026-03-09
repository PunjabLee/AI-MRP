"""
AI MRP Python Service - Configuration
"""
from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    """应用配置"""
    
    # 服务配置
    app_name: str = "AI MRP Service"
    app_version: str = "1.0.0"
    debug: bool = False
    
    # 服务器配置
    host: str = "0.0.0.0"
    port: int = 8000
    
    # 数据库配置
    database_url: str = "postgresql://postgres:postgres@localhost:5432/aimrp"
    
    # Redis 配置
    redis_url: str = "redis://localhost:6379/0"
    
    # LLM 配置
    llm_provider: str = "deepseek"  # openai, deepseek, ollama
    
    # DeepSeek
    deepseek_api_key: str = ""
    deepseek_base_url: str = "https://api.deepseek.com"
    deepseek_model: str = "deepseek-chat"
    
    # OpenAI
    openai_api_key: str = ""
    openai_base_url: str = "https://api.openai.com"
    openai_model: str = "gpt-4"
    
    # Ollama (本地部署)
    ollama_base_url: str = "http://localhost:11434"
    ollama_model: str = "llama2"
    
    # Qdrant 向量库
    qdrant_url: str = "http://localhost:6333"
    qdrant_api_key: str = ""
    
    # CORS
    cors_origins: list = ["*"]
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


@lru_cache()
def get_settings() -> Settings:
    """获取配置单例"""
    return Settings()
