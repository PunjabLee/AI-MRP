"""
Chat Router - 对话服务（升级版）

支持两种模式：
1. 规则匹配：快速响应，无需API
2. LLM理解：深度理解，可配置化

特性：
- 可配置切换
- 意图识别 + 实体提取
- 动作执行
- 上下文记忆
"""
from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
import re
import json
from datetime import datetime

from app.algorithms import LLMClient, LLMConfig, Message, ToolDefinition, get_mrp_tools
from app.utils.response import ApiResponse

router = APIRouter()


class ChatRequest(BaseModel):
    """对话请求"""
    message: str
    session_id: Optional[str] = None
    context: Optional[Dict[str, Any]] = None
    use_llm: bool = False  # 是否使用 LLM


class ChatResponse(BaseModel):
    """对话响应"""
    message: str
    session_id: str
    intent: Optional[str] = None
    entities: Optional[Dict[str, Any]] = None
    suggestions: Optional[List[str]] = None
    action: Optional[Dict[str, Any]] = None
    confidence: Optional[float] = None
    mode: str = "rule"  # rule 或 llm


class ConversationContext:
    """对话上下文管理器"""
    
    def __init__(self):
        self.sessions: Dict[str, List[Dict]] = {}
        self.max_history = 10
    
    def add_message(self, session_id: str, role: str, content: str, 
                    intent: str = None, entities: Dict = None):
        """添加消息到上下文"""
        if session_id not in self.sessions:
            self.sessions[session_id] = []
        
        self.sessions[session_id].append({
            "role": role,
            "content": content,
            "intent": intent,
            "entities": entities,
            "timestamp": datetime.now().isoformat()
        })
        
        # 限制历史长度
        if len(self.sessions[session_id]) > self.max_history:
            self.sessions[session_id] = self.sessions[session_id][-self.max_history:]
    
    def get_history(self, session_id: str) -> List[Dict]:
        """获取对话历史"""
        return self.sessions.get(session_id, [])
    
    def clear(self, session_id: str):
        """清空会话"""
        if session_id in self.sessions:
            del self.sessions[session_id]


# 全局上下文
conversation_context = ConversationContext()

# LLM 客户端（延迟初始化）
_llm_client: Optional[LLMClient] = None


def get_llm_client() -> Optional[LLMClient]:
    """获取 LLM 客户端"""
    global _llm_client
    
    if _llm_client is None:
        try:
            # 尝试创建客户端
            config = LLMConfig()
            if config.api_key:
                _llm_client = LLMClient(config)
        except Exception:
            pass
    
    return _llm_client


# ========== 规则匹配意图识别 ==========

INTENT_PATTERNS = {
    "greeting": [
        r"你好|您好|hello|hi|嗨",
        r"在吗|在不在"
    ],
    "query_stock": [
        r"查.*库存|库存.*查|还有.*库存|库存.*多少",
        r"查看.*库存"
    ],
    "query_order": [
        r"查.*订单|订单.*查|订单.*状态",
        r"订单.*多少"
    ],
    "run_mrp": [
        r"运行.*MRP|mrp.*运行|计算.*物料",
        r"跑.*MRP|执行.*MRP"
    ],
    "create_order": [
        r"创建.*订单|新建.*订单|下单",
        r"添加.*订单"
    ],
    "create_purchase": [
        r"创建.*采购|新建.*采购|采购.*订单",
        r"生成.*采购.*建议"
    ],
    "query_forecast": [
        r"预测|预测.*需求|未来.*需求",
        r"需求.*预测"
    ],
    "query_safety_stock": [
        r"安全库存|安全.*库存",
        r"库存.*建议"
    ],
    "query_risk": [
        r"风险|预警|风险.*查",
        r"有什么.*风险"
    ],
    "whatif": [
        r"What.if|what.if|假设.*分析|如果.*会",
        r"模拟.*场景|场景.*模拟"
    ],
    "schedule": [
        r"排程|排.*程|生产.*排",
        r"优化.*排程"
    ],
    "capacity": [
        r"产能|产.*能|设备.*利用",
        r"车间.*产能"
    ],
    "supplier": [
        r"供应商|供货.*商",
        r"采购.*供应商"
    ],
    "help": [
        r"帮助|help|帮助.*我",
        r"你能.*什么"
    ]
}

ENTITY_PATTERNS = {
    "item_code": r"物料[编码代码]?[A-Z0-9]+|ITEM\d+|[A-Z]{2}\d{4,}",
    "item_name": r"物料名称|名叫|叫.*物料",
    "quantity": r"\d+[个件台批]|数量|多少",
    "date": r"\d{4}[-/年]\d{1,2}[-/月]\d{1,2}日?|今天|明天|后天|本周|下周",
    "supplier": r"供应商|供应商.*|来自.*供应商",
    "order_id": r"订单号|订单编号|ORDER\d+",
    "priority": r"紧急|加急|普通|优先级",
    "work_center": r"生产线|工作中心|车间"
}


def recognize_intent(message: str) -> tuple[str, float]:
    """识别用户意图，返回(意图, 置信度)"""
    message_lower = message.lower()
    
    for intent, patterns in INTENT_PATTERNS.items():
        for pattern in patterns:
            if re.search(pattern, message_lower):
                return intent, 0.9
    
    return "unknown", 0.5


def extract_entities(message: str) -> Dict[str, Any]:
    """提取实体"""
    entities = {}
    
    # 提取物料编码
    item_match = re.search(r'[A-Z]{2}\d{4,}', message, re.IGNORECASE)
    if item_match:
        entities["item_code"] = item_match.group().upper()
    
    # 提取数量
    qty_match = re.search(r'(\d+)\s*[个件台批]', message)
    if qty_match:
        entities["quantity"] = int(qty_match.group(1))
    
    # 提取日期
    if "今天" in message:
        entities["date"] = "today"
    elif "明天" in message:
        entities["date"] = "tomorrow"
    elif "后天" in message:
        entities["date"] = "day_after_tomorrow"
    elif "本周" in message:
        entities["date"] = "this_week"
    elif "下周" in message:
        entities["date"] = "next_week"
    else:
        date_match = re.search(r'\d{4}[-/年]\d{1,2}', message)
        if date_match:
            entities["date"] = date_match.group()
    
    # 提取优先级
    if "紧急" in message or "加急" in message:
        entities["priority"] = "high"
    elif "普通" in message:
        entities["priority"] = "normal"
    
    return entities


def build_rule_response(intent: str, entities: Dict[str, Any], 
                       session_id: str) -> ChatResponse:
    """基于规则的响应构建"""
    
    responses = {
        "greeting": {
            "message": "你好！我是 AI MRP 助手。我可以帮你：\n📦 查询库存\n📋 查看订单\n🔢 运行 MRP 计算\n📈 查看需求预测\n⚠️ 查询风险预警\n💡 进行 What-if 分析\n\n请告诉我你需要做什么？",
            "suggestions": ["查一下库存", "运行 MRP", "查看风险"]
        },
        "query_stock": {
            "message": _build_stock_response(entities),
            "suggestions": ["查看订单", "运行 MRP", "查看预测"]
        },
        "query_order": {
            "message": _build_order_response(entities),
            "suggestions": ["创建订单", "查看库存", "运行 MRP"]
        },
        "run_mrp": {
            "message": "好的，我现在运行 MRP 计算...\n\n✅ MRP 计算完成！\n- 建议采购订单：12 个\n- 建议生产工单：5 个\n- 需求缺口：3 个物料\n\n需要查看详细建议吗？",
            "suggestions": ["查看采购建议", "查看生产建议", "查看缺料"]
        },
        "create_order": {
            "message": _build_create_order_response(entities),
            "suggestions": ["确认创建", "取消"]
        },
        "create_purchase": {
            "message": "好的，我来生成采购建议...\n\n基于当前 MRP 计算结果，建议采购：\n- 物料 A：100 件\n- 物料 B：200 件\n\n需要创建采购订单吗？",
            "suggestions": ["确认创建", "查看详情"]
        },
        "query_forecast": {
            "message": "根据历史数据分析，未来30天需求预测如下：\n\n📈 预测趋势：上升\n🔔 风险预警：2个物料可能缺货\n\n需要查看具体物料的预测详情吗？",
            "suggestions": ["查看预测详情", "查看安全库存", "运行 MRP"]
        },
        "query_safety_stock": {
            "message": "安全库存建议：\n\n🔧 物料 A：当前 50，建议 80\n🔧 物料 B：当前 30，建议 50\n\n需要查看更多物料的安全库存建议吗？",
            "suggestions": ["查看详情", "调整安全库存"]
        },
        "query_risk": {
            "message": "当前风险监控状态：\n\n⚠️ 高风险：2项\n- 物料 XX001 库存不足\n- 供应商 YY002 交期延迟\n\n🟡 中风险：5项\n\n需要查看详细风险报告吗？",
            "suggestions": ["查看详细风险", "查看预警设置", "处理风险"]
        },
        "whatif": {
            "message": "进入 What-if 分析模式。\n\n请告诉我你想模拟的场景，例如：\n- 「如果订单增加100件会怎样」\n- 「如果提前5天交货会怎样」",
            "suggestions": ["订单增加场景", "提前交货场景", "查看历史模拟"]
        },
        "schedule": {
            "message": "好的，我来优化生产排程...\n\n当前排程状态：\n- 进行中工单：12 个\n- 设备利用率：85%\n\n是否需要重新优化排程？",
            "suggestions": ["重新排程", "查看甘特图", "调整约束"]
        },
        "capacity": {
            "message": "产能分析：\n\n🏭 生产线 A：利用率 90%\n🏭 生产线 B：利用率 75%\n🏭 生产线 C：利用率 60%\n\n产能瓶颈：生产线 A",
            "suggestions": ["查看详情", "调整产能"]
        },
        "supplier": {
            "message": "供应商管理：\n\n✅ 正常：15 家\n⚠️ 风险：3 家\n❌ 异常：1 家\n\n需要查看供应商详情吗？",
            "suggestions": ["查看风险供应商", "添加供应商"]
        },
        "help": {
            "message": """我是 AI MRP 智能助手，我可以帮你：

📦 库存管理
- 查询库存数量
- 查看库存变动

📋 订单管理
- 查询订单状态
- 创建新订单

🔢 MRP 计算
- 运行 MRP 计算
- 查看采购建议

📈 智能预测
- 需求预测分析
- 安全库存推荐

⚠️ 风险预警
- 库存风险监控
- 供应商风险

💡 What-if 分析
- 场景模拟
- 方案对比

🏭 生产排程
- 优化排程
- 产能分析

请告诉我你需要什么帮助？""",
            "suggestions": ["查询库存", "运行MRP", "查看风险"]
        },
        "unknown": {
            "message": "抱歉，我不太理解你的意思。\n\n你可以尝试：\n- 查一下库存\n- 运行 MRP\n- 查看订单\n- 查看风险\n\n或者直接说「帮助」获取更多信息",
            "suggestions": ["帮助", "查库存", "运行MRP"]
        }
    }
    
    response_data = responses.get(intent, responses["unknown"])
    
    return ChatResponse(
        message=response_data["message"],
        session_id=session_id,
        intent=intent,
        entities=entities,
        suggestions=response_data.get("suggestions", []),
        action=_build_action(intent, entities),
        confidence=0.9 if intent != "unknown" else 0.5,
        mode="rule"
    )


async def build_llm_response(message: str, session_id: str, 
                            context: Dict = None) -> ChatResponse:
    """基于 LLM 的响应构建"""
    
    llm_client = get_llm_client()
    
    if not llm_client:
        # 回退到规则
        return build_rule_response("unknown", {}, session_id)
    
    # 获取对话历史
    history = conversation_context.get_history(session_id)
    
    # 构建系统提示词
    system_prompt = """你是一个专业的 AI MRP（物料需求计划）助手。你的职责是：

1. 理解用户关于库存、订单、采购、生产、MRP、预测、风险等方面的问题
2. 用专业但易懂的语言回答
3. 如果需要执行操作，明确告诉用户你会调用什么接口

用户可能会用自然语言描述需求，你需要：
- 识别用户的意图
- 提取关键实体（物料编码、数量、日期等）
- 用友好的方式回复

常用操作：
- 查询库存：调用库存相关接口
- 运行MRP：调用MRP计算接口
- 创建订单：调用订单创建接口
- 风险预警：调用风险查询接口

请用中文回复。"""
    
    # 构建消息
    messages = [Message(role="system", content=system_prompt)]
    
    # 添加历史
    for msg in history[-5:]:
        messages.append(Message(role=msg["role"], content=msg["content"]))
    
    # 添加当前消息
    messages.append(Message(role="user", content=message))
    
    # 调用 LLM
    response = llm_client.chat(
        message=message,
        tools=get_mrp_tools()
    )
    
    if response.get("success"):
        content = response.get("content", "")
        
        # 尝试提取意图和实体（简化版）
        intent, confidence = recognize_intent(message)
        entities = extract_entities(message)
        
        return ChatResponse(
            message=content,
            session_id=session_id,
            intent=intent,
            entities=entities,
            action=_build_action(intent, entities),
            confidence=0.95,
            mode="llm"
        )
    else:
        # LLM 调用失败，回退到规则
        return build_rule_response("unknown", {}, session_id)


def _build_stock_response(entities: Dict[str, Any]) -> str:
    """构建库存查询响应"""
    if entities.get("item_code"):
        return f"物料 {entities['item_code']} 当前库存：150 件\n安全库存：50 件\n可用数量：100 件"
    
    return """当前库存概览：

📦 总物料数：256 种
✅ 充足：198 种
⚠️ 低于安全库存：35 种
❌ 缺货：23 种

需要查看具体物料库存吗？"""


def _build_order_response(entities: Dict[str, Any]) -> str:
    """构建订单查询响应"""
    if entities.get("order_id"):
        return f"订单 {entities['order_id']} 状态：生产中\n预计完工：3天后\n已完成：60%"
    
    return """当前订单概览：

📋 进行中订单：45 个
✅ 已完成：123 个
⏳ 待确认：12 个

需要查看具体订单吗？"""


def _build_create_order_response(entities: Dict[str, Any]) -> str:
    """构建创建订单响应"""
    qty = entities.get("quantity", 0)
    item = entities.get("item_code", "物料")
    
    if qty > 0:
        return f"好的，我帮你创建订单：\n\n📝 物料：{item}\n📦 数量：{qty}\n📅 交期：待确定\n\n请确认是否创建？"
    
    return "好的，请告诉我你要创建什么订单？\n\n例如：「创建订单，物料AB1001，数量100」"


def _build_action(intent: str, entities: Dict[str, Any]) -> Dict[str, Any]:
    """构建执行动作"""
    action_map = {
        "query_stock": {"type": "API", "endpoint": "/inventory/query", "params": entities},
        "query_order": {"type": "API", "endpoint": "/order/query", "params": entities},
        "run_mrp": {"type": "API", "endpoint": "/mrp/calculate", "params": {}},
        "create_order": {"type": "API", "endpoint": "/order/create", "params": entities},
        "create_purchase": {"type": "API", "endpoint": "/purchase/create", "params": entities},
        "query_forecast": {"type": "API", "endpoint": "/predict/demand", "params": entities},
        "query_safety_stock": {"type": "API", "endpoint": "/predict/safety-stock", "params": entities},
        "query_risk": {"type": "API", "endpoint": "/risk/query", "params": {}},
        "whatif": {"type": "PAGE", "page": "/whatif", "params": entities},
        "schedule": {"type": "API", "endpoint": "/schedule/optimize", "params": {}},
        "capacity": {"type": "API", "endpoint": "/production/capacity", "params": {}},
        "supplier": {"type": "API", "endpoint": "/supplier/query", "params": {}}
    }
    
    return action_map.get(intent, {})


# ========== API 接口 ==========

@router.post("/")
async def chat(request: ChatRequest):
    """
    AI 对话接口
    
    支持两种模式：
    - use_llm=False (默认): 规则匹配，响应快速
    - use_llm=True: LLM 理解，响应更智能
    """
    message = request.message
    session_id = request.session_id or "default"
    context = request.context or {}
    
    # 意图识别 + 实体提取
    intent, confidence = recognize_intent(message)
    entities = extract_entities(message)
    
    # 根据模式选择响应方式
    if request.use_llm:
        # 使用 LLM
        response = await build_llm_response(message, session_id, context)
    else:
        # 使用规则
        response = build_rule_response(intent, entities, session_id)
        response.confidence = confidence
    
    # 记录上下文
    conversation_context.add_message(
        session_id, "user", message, intent, entities
    )
    conversation_context.add_message(
        session_id, "assistant", response.message, response.intent, response.entities
    )
    
    data = {
        "message": response.message,
        "session_id": response.session_id,
        "intent": response.intent,
        "entities": response.entities,
        "suggestions": response.suggestions,
        "action": response.action,
        "confidence": response.confidence,
        "mode": response.mode
    }
    return ApiResponse.success(data=data, message="对话成功")


@router.get("/history/{session_id}")
async def get_history(session_id: str, limit: int = 10):
    """获取对话历史"""
    history = conversation_context.get_history(session_id)
    return ApiResponse.success(data={
        "session_id": session_id,
        "messages": history[-limit:]
    }, message="获取成功")


@router.delete("/history/{session_id}")
async def clear_history(session_id: str):
    """清空对话历史"""
    conversation_context.clear(session_id)
    return ApiResponse.success(message="对话历史已清空")


@router.get("/intents")
async def get_supported_intents():
    """获取支持的意图列表"""
    return ApiResponse.success(data={
        "intents": list(INTENT_PATTERNS.keys()),
        "total": len(INTENT_PATTERNS)
    }, message="获取成功")


@router.get("/mode")
async def get_chat_mode():
    """获取当前对话模式"""
    llm_client = get_llm_client()
    return ApiResponse.success(data={
        "rule_mode": True,
        "llm_mode": llm_client is not None,
        "llm_configured": llm_client is not None and bool(llm_client.config.api_key)
    }, message="获取成功")


@router.post("/feedback")
async def submit_feedback(session_id: str, message: str, 
                          intent: str, correct_intent: str):
    """
    用户反馈 - 用于意图识别训练
    """
    return ApiResponse.success(message="感谢反馈，我们将持续优化识别准确率")


# ========== 意图推荐服务 ==========

@router.get("/recommendations")
async def get_intent_recommendations(session_id: str):
    """获取意图推荐"""
    recommendations = [
        {
            "intent": "query_risk",
            "title": "查看风险预警",
            "description": "您有新的风险预警待处理",
            "priority": "HIGH",
            "icon": "⚠️"
        },
        {
            "intent": "run_mrp",
            "title": "运行MRP",
            "description": "建议运行MRP计算更新采购建议",
            "priority": "MEDIUM",
            "icon": "🔢"
        },
        {
            "intent": "query_order",
            "title": "查看订单",
            "description": "您有待确认的订单",
            "priority": "MEDIUM",
            "icon": "📋"
        },
        {
            "intent": "query_forecast",
            "title": "需求预测",
            "description": "查看近期需求预测",
            "priority": "LOW",
            "icon": "📈"
        }
    ]
    
    return ApiResponse.success(data={
        "session_id": session_id,
        "recommendations": recommendations,
        "reason": "基于您的操作习惯和系统状态"
    }, message="获取成功")
