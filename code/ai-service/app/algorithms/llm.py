"""
LLM 集成模块 - 可配置化

支持多种 LLM 提供商：
- DeepSeek
- OpenAI
- Ollama (本地部署)
- Anthropic

特性：
- 可配置化：支持 API Key、模型、温度等参数
- 流式输出：支持流式响应
- 对话历史：支持多轮对话上下文
- 工具调用：支持 Tool Use/Function Calling
"""
from typing import List, Dict, Any, Optional, Callable
from dataclasses import dataclass, field
from enum import Enum
import json
import os
from abc import ABC, abstractmethod


class LLMProvider(Enum):
    """LLM 提供商"""
    DEEPSEEK = "deepseek"
    OPENAI = "openai"
    OLLAMA = "ollama"
    ANTHROPIC = "anthropic"


@dataclass
class Message:
    """对话消息"""
    role: str  # system, user, assistant
    content: str
    tool_calls: Optional[List[Dict]] = None
    tool_call_id: Optional[str] = None


@dataclass
class LLMConfig:
    """LLM 配置"""
    provider: str = "deepseek"
    model: str = "deepseek-chat"
    api_key: str = ""
    base_url: str = "https://api.deepseek.com"
    temperature: float = 0.7
    max_tokens: int = 2048
    top_p: float = 0.9
    stream: bool = False
    timeout: int = 60
    
    # Ollama 本地
    ollama_model: str = "llama2"
    
    # 系统提示词
    system_prompt: str = """你是一个专业的 AI MRP 助手，擅长物料需求计划、生产排程、库存管理等领域。
请用专业但易懂的语言回答用户问题。
如果需要执行操作，请明确说明。"""


@dataclass
class ToolDefinition:
    """工具定义"""
    name: str
    description: str
    parameters: Dict[str, Any]


class LLMBackend(ABC):
    """LLM 后端抽象类"""
    
    @abstractmethod
    def chat(
        self,
        messages: List[Message],
        tools: Optional[List[ToolDefinition]] = None,
        **kwargs
    ) -> Dict[str, Any]:
        """发送对话请求"""
        pass
    
    @abstractmethod
    def chat_stream(
        self,
        messages: List[Message],
        tools: Optional[List[ToolDefinition]] = None,
        **kwargs
    ):
        """流式对话"""
        pass


class DeepSeekBackend(LLMBackend):
    """DeepSeek 后端"""
    
    def __init__(self, config: LLMConfig):
        self.config = config
    
    def chat(self, messages: List[Message], tools: Optional[List[ToolDefinition]] = None, **kwargs) -> Dict[str, Any]:
        """DeepSeek API 调用"""
        try:
            import httpx
            
            headers = {
                "Authorization": f"Bearer {self.config.api_key}",
                "Content-Type": "application/json"
            }
            
            # 转换消息格式
            msgs = [{"role": m.role, "content": m.content} for m in messages]
            
            payload = {
                "model": self.config.model,
                "messages": msgs,
                "temperature": kwargs.get("temperature", self.config.temperature),
                "max_tokens": kwargs.get("max_tokens", self.config.max_tokens),
                "top_p": kwargs.get("top_p", self.config.top_p)
            }
            
            if tools:
                payload["tools"] = [
                    {
                        "type": "function",
                        "function": {
                            "name": t.name,
                            "description": t.description,
                            "parameters": t.parameters
                        }
                    }
                    for t in tools
                ]
            
            with httpx.Client(timeout=self.config.timeout) as client:
                response = client.post(
                    f"{self.config.base_url}/v1/chat/completions",
                    headers=headers,
                    json=payload
                )
                response.raise_for_status()
                result = response.json()
            
            return self._parse_response(result)
            
        except ImportError:
            return self._error_response("请安装 httpx: pip install httpx")
        except Exception as e:
            return self._error_response(f"API 调用失败: {str(e)}")
    
    def chat_stream(self, messages: List[Message], tools: Optional[List[ToolDefinition]] = None, **kwargs):
        """流式调用"""
        try:
            import httpx
            
            headers = {
                "Authorization": f"Bearer {self.config.api_key}",
                "Content-Type": "application/json"
            }
            
            msgs = [{"role": m.role, "content": m.content} for m in messages]
            
            payload = {
                "model": self.config.model,
                "messages": msgs,
                "temperature": kwargs.get("temperature", self.config.temperature),
                "max_tokens": kwargs.get("max_tokens", self.config.max_tokens),
                "stream": True
            }
            
            with httpx.Client(timeout=self.config.timeout) as client:
                with client.stream(
                    "POST",
                    f"{self.config.base_url}/v1/chat/completions",
                    headers=headers,
                    json=payload
                ) as response:
                    for chunk in response.iter_lines():
                        if chunk:
                            data = chunk.decode("utf-8")
                            if data.startswith("data: "):
                                json_data = json.loads(data[6:])
                                if "choices" in json_data:
                                    delta = json_data["choices"][0].get("delta", {})
                                    if "content" in delta:
                                        yield delta["content"]
                                    
        except Exception as e:
            yield f"Error: {str(e)}"
    
    def _parse_response(self, result: Dict) -> Dict[str, Any]:
        """解析响应"""
        try:
            choice = result["choices"][0]
            message = choice["message"]
            
            return {
                "success": True,
                "content": message.get("content", ""),
                "tool_calls": message.get("tool_calls"),
                "finish_reason": choice.get("finish_reason"),
                "usage": result.get("usage", {})
            }
        except (KeyError, IndexError):
            return self._error_response("解析响应失败")
    
    def _error_response(self, error: str) -> Dict[str, Any]:
        """错误响应"""
        return {
            "success": False,
            "error": error,
            "content": ""
        }


class OpenAIBackend(LLMBackend):
    """OpenAI 后端"""
    
    def __init__(self, config: LLMConfig):
        self.config = config
    
    def chat(self, messages: List[Message], tools: Optional[List[ToolDefinition]] = None, **kwargs) -> Dict[str, Any]:
        """OpenAI API 调用"""
        try:
            from openai import OpenAI
            
            client = OpenAI(
                api_key=self.config.api_key,
                base_url=self.config.base_url,
                timeout=self.config.timeout
            )
            
            msgs = [{"role": m.role, "content": m.content} for m in messages]
            
            params = {
                "model": self.config.model,
                "messages": msgs,
                "temperature": kwargs.get("temperature", self.config.temperature),
                "max_tokens": kwargs.get("max_tokens", self.config.max_tokens),
                "top_p": kwargs.get("top_p", self.config.top_p)
            }
            
            if tools:
                params["tools"] = [
                    {
                        "type": "function",
                        "function": {
                            "name": t.name,
                            "description": t.description,
                            "parameters": t.parameters
                        }
                    }
                    for t in tools
                ]
            
            response = client.chat.completions.create(**params)
            
            msg = response.choices[0].message
            
            return {
                "success": True,
                "content": msg.content or "",
                "tool_calls": [{"function": tc.model_dump()["function"]} for tc in msg.tool_calls] if msg.tool_calls else None,
                "finish_reason": response.choices[0].finish_reason,
                "usage": response.usage.model_dump() if response.usage else {}
            }
            
        except ImportError:
            return self._error_response("请安装 openai: pip install openai")
        except Exception as e:
            return self._error_response(f"API 调用失败: {str(e)}")
    
    def chat_stream(self, messages: List[Message], tools: Optional[List[ToolDefinition]] = None, **kwargs):
        """流式调用"""
        try:
            from openai import OpenAI
            
            client = OpenAI(
                api_key=self.config.api_key,
                base_url=self.config.base_url,
                timeout=self.config.timeout
            )
            
            msgs = [{"role": m.role, "content": m.content} for m in messages]
            
            params = {
                "model": self.config.model,
                "messages": msgs,
                "temperature": kwargs.get("temperature", self.config.temperature),
                "max_tokens": kwargs.get("max_tokens", self.config.max_tokens),
                "stream": True
            }
            
            for chunk in client.chat.completions.create(**params):
                if chunk.choices[0].delta.content:
                    yield chunk.choices[0].delta.content
                    
        except Exception as e:
            yield f"Error: {str(e)}"
    
    def _error_response(self, error: str) -> Dict[str, Any]:
        return {
            "success": False,
            "error": error,
            "content": ""
        }


class OllamaBackend(LLMBackend):
    """Ollama 本地后端"""
    
    def __init__(self, config: LLMConfig):
        self.config = config
        self.base_url = config.base_url or "http://localhost:11434"
    
    def chat(self, messages: List[Message], tools: Optional[List[ToolDefinition]] = None, **kwargs) -> Dict[str, Any]:
        """Ollama 本地调用"""
        try:
            import httpx
            
            msgs = [{"role": m.role, "content": m.content} for m in messages]
            
            payload = {
                "model": self.config.ollama_model,
                "messages": msgs,
                "stream": False,
                "options": {
                    "temperature": kwargs.get("temperature", self.config.temperature),
                    "num_predict": kwargs.get("max_tokens", self.config.max_tokens)
                }
            }
            
            with httpx.Client(timeout=self.config.timeout) as client:
                response = client.post(
                    f"{self.base_url}/api/chat",
                    json=payload
                )
                response.raise_for_status()
                result = response.json()
            
            return {
                "success": True,
                "content": result.get("message", {}).get("content", ""),
                "finish_reason": "stop",
                "usage": {}
            }
            
        except Exception as e:
            return self._error_response(f"Ollama 调用失败: {str(e)}")
    
    def chat_stream(self, messages: List[Message], tools: Optional[List[ToolDefinition]] = None, **kwargs):
        """流式调用"""
        try:
            import httpx
            
            msgs = [{"role": m.role, "content": m.content} for m in messages]
            
            payload = {
                "model": self.config.ollama_model,
                "messages": msgs,
                "stream": True
            }
            
            with httpx.Client(timeout=self.config.timeout) as client:
                with client.stream("POST", f"{self.base_url}/api/chat", json=payload) as response:
                    for line in response.iter_lines():
                        if line:
                            data = json.loads(line)
                            if "message" in data and "content" in data["message"]:
                                yield data["message"]["content"]
                                
        except Exception as e:
            yield f"Error: {str(e)}"
    
    def _error_response(self, error: str) -> Dict[str, Any]:
        return {
            "success": False,
            "error": error,
            "content": ""
        }


class LLMClient:
    """LLM 客户端"""
    
    def __init__(self, config: Optional[LLMConfig] = None):
        # 从环境变量加载配置
        if config is None:
            config = self._load_config_from_env()
        
        self.config = config
        self.backend = self._create_backend(config)
        self.conversation_history: List[Message] = []
        
        # 系统提示词
        if config.system_prompt:
            self.conversation_history.append(Message(
                role="system",
                content=config.system_prompt
            ))
    
    def _load_config_from_env(self) -> LLMConfig:
        """从环境变量加载配置"""
        provider = os.getenv("LLM_PROVIDER", "deepseek")
        
        configs = {
            "deepseek": LLMConfig(
                provider="deepseek",
                model=os.getenv("DEEPSEEK_MODEL", "deepseek-chat"),
                api_key=os.getenv("DEEPSEEK_API_KEY", ""),
                base_url=os.getenv("DEEPSEEK_BASE_URL", "https://api.deepseek.com")
            ),
            "openai": LLMConfig(
                provider="openai",
                model=os.getenv("OPENAI_MODEL", "gpt-4"),
                api_key=os.getenv("OPENAI_API_KEY", ""),
                base_url=os.getenv("OPENAI_BASE_URL", "https://api.openai.com")
            ),
            "ollama": LLMConfig(
                provider="ollama",
                ollama_model=os.getenv("OLLAMA_MODEL", "llama2"),
                base_url=os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
            )
        }
        
        return configs.get(provider, configs["deepseek"])
    
    def _create_backend(self, config: LLMConfig) -> LLMBackend:
        """创建后端"""
        if config.provider == "deepseek":
            return DeepSeekBackend(config)
        elif config.provider == "openai":
            return OpenAIBackend(config)
        elif config.provider == "ollama":
            return OllamaBackend(config)
        else:
            return DeepSeekBackend(config)
    
    def chat(
        self,
        message: str,
        tools: Optional[List[ToolDefinition]] = None,
        **kwargs
    ) -> Dict[str, Any]:
        """
        发送对话请求
        
        Args:
            message: 用户消息
            tools: 可用工具列表
            **kwargs: 其他参数 (temperature, max_tokens 等)
        
        Returns:
            响应结果
        """
        # 添加用户消息
        self.conversation_history.append(Message(
            role="user",
            content=message
        ))
        
        # 调用后端
        response = self.backend.chat(
            self.conversation_history,
            tools=tools,
            **kwargs
        )
        
        if response.get("success"):
            # 添加助手消息
            assistant_message = Message(
                role="assistant",
                content=response.get("content", "")
            )
            self.conversation_history.append(assistant_message)
        
        return response
    
    def chat_stream(self, message: str, **kwargs):
        """流式对话"""
        self.conversation_history.append(Message(
            role="user",
            content=message
        ))
        
        for chunk in self.backend.chat_stream(self.conversation_history, **kwargs):
            yield chunk
    
    def clear_history(self):
        """清空对话历史"""
        system_msg = self.conversation_history[0] if self.conversation_history and self.conversation_history[0].role == "system" else None
        self.conversation_history = []
        if system_msg:
            self.conversation_history.append(system_msg)
    
    def set_system_prompt(self, prompt: str):
        """设置系统提示词"""
        if self.conversation_history and self.conversation_history[0].role == "system":
            self.conversation_history[0].content = prompt
        else:
            self.conversation_history.insert(0, Message(role="system", content=prompt))


# ========== MRP 专用工具定义 =========-

def get_mrp_tools() -> List[ToolDefinition]:
    """获取 MRP 相关的工具定义"""
    return [
        ToolDefinition(
            name="query_inventory",
            description="查询物料库存",
            parameters={
                "type": "object",
                "properties": {
                    "item_code": {"type": "string", "description": "物料编码"}
                },
                "required": ["item_code"]
            }
        ),
        ToolDefinition(
            name="run_mrp",
            description="运行MRP计算",
            parameters={
                "type": "object",
                "properties": {
                    "item_codes": {"type": "array", "items": {"type": "string"}, "description": "物料编码列表"},
                    "horizon_days": {"type": "integer", "description": "计划周期天数"}
                }
            }
        ),
        ToolDefinition(
            name="create_purchase_order",
            description="创建采购订单",
            parameters={
                "type": "object",
                "properties": {
                    "item_code": {"type": "string", "description": "物料编码"},
                    "quantity": {"type": "number", "description": "数量"},
                    "supplier_code": {"type": "string", "description": "供应商编码"}
                },
                "required": ["item_code", "quantity", "supplier_code"]
            }
        ),
        ToolDefinition(
            name="query_orders",
            description="查询销售订单",
            parameters={
                "type": "object",
                "properties": {
                    "status": {"type": "string", "description": "订单状态"},
                    "customer": {"type": "string", "description": "客户名称"}
                }
            }
        ),
        ToolDefinition(
            name="forecast_demand",
            description="预测未来需求",
            parameters={
                "type": "object",
                "properties": {
                    "item_code": {"type": "string", "description": "物料编码"},
                    "days": {"type": "integer", "description": "预测天数"}
                },
                "required": ["item_code"]
            }
        )
    ]


# ========== 便捷函数 =========-

def create_llm_client(
    provider: str = "deepseek",
    api_key: str = "",
    model: str = ""
) -> LLMClient:
    """
    创建 LLM 客户端
    
    Args:
        provider: 提供商 (deepseek, openai, ollama)
        api_key: API Key
        model: 模型名称
    
    Returns:
        LLMClient 实例
    """
    config = LLMConfig(
        provider=provider,
        api_key=api_key,
        model=model
    )
    
    return LLMClient(config)
