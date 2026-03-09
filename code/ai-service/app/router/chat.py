"""
Chat Router - 对话服务
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict, Any

router = APIRouter()


class ChatRequest(BaseModel):
    """对话请求"""
    message: str
    session_id: Optional[str] = None
    context: Optional[Dict[str, Any]] = None


class ChatResponse(BaseModel):
    """对话响应"""
    message: str
    session_id: str
    intent: Optional[str] = None
    entities: Optional[Dict[str, Any]] = None
    suggestions: Optional[List[str]] = None


@router.post("/", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """
    AI 对话接口
    """
    # TODO: 实现对话逻辑
    return ChatResponse(
        message="你好！我是 AI MRP 助手，有什么可以帮助你的？",
        session_id=request.session_id or "default",
        intent="greeting",
        entities={},
        suggestions=["帮我查一下库存", "运行 MRP", "创建订单"]
    )


@router.get("/history/{session_id}")
async def get_history(session_id: str):
    """
    获取对话历史
    """
    # TODO: 从 Redis 获取历史
    return {
        "session_id": session_id,
        "messages": []
    }
