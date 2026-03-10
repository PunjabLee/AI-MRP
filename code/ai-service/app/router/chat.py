"""
Chat Router - 对话服务
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
import re

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
    action: Optional[Dict[str, Any]] = None  # 执行动作


# ========== 意图识别 ==========

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
    "query_forecast": [
        r"预测|预测.*需求|未来.*需求",
        r"需求.*预测"
    ],
    "query_risk": [
        r"风险|预警|风险.*查",
        r"有什么.*风险"
    ],
    "whatif": [
        r"What.if|what.if|假设.*分析|如果.*会",
        r"模拟.*场景"
    ],
    "help": [
        r"帮助|help|帮助.*我",
        r"你能.*什么"
    ]
}

ENTITY_PATTERNS = {
    "item_code": r"物料[编码代码]?[A-Z0-9]+|ITEM\d+|[A-Z]{2}\d{4,}",
    "item_name": r"物料名称|名叫|叫.*物料",
    "quantity": r"\d+[个件台]|数量|多少",
    "date": r"\d{4}[-/年]\d{1,2}[-/月]\d{1,2}日?|今天|明天|后天",
    "supplier": r"供应商|供应商.*|来自.*供应商",
    "order_id": r"订单号|订单编号|ORDER\d+"
}


def recognize_intent(message: str) -> str:
    """识别用户意图"""
    message_lower = message.lower()
    
    for intent, patterns in INTENT_PATTERNS.items():
        for pattern in patterns:
            if re.search(pattern, message_lower):
                return intent
    
    return "unknown"


def extract_entities(message: str) -> Dict[str, Any]:
    """提取实体"""
    entities = {}
    
    # 提取物料编码
    item_match = re.search(r'[A-Z]{2}\d{4,}', message, re.IGNORECASE)
    if item_match:
        entities["item_code"] = item_match.group()
    
    # 提取数量
    qty_match = re.search(r'(\d+)\s*[个件台]', message)
    if qty_match:
        entities["quantity"] = int(qty_match.group(1))
    
    # 提取日期
    if "今天" in message:
        entities["date"] = "today"
    elif "明天" in message:
        entities["date"] = "tomorrow"
    elif "后天" in message:
        entities["date"] = "day_after_tomorrow"
    else:
        date_match = re.search(r'\d{4}[-/年]\d{1,2}', message)
        if date_match:
            entities["date"] = date_match.group()
    
    return entities


def build_response(intent: str, entities: Dict[str, Any], 
                   session_id: str) -> ChatResponse:
    """构建响应"""
    
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
        "query_forecast": {
            "message": "根据历史数据分析，未来30天需求预测如下：\n\n📈 预测趋势：上升\n🔔 风险预警：2个物料可能缺货\n\n需要查看具体物料的预测详情吗？",
            "suggestions": ["查看预测详情", "查看安全库存", "运行 MRP"]
        },
        "query_risk": {
            "message": "当前风险监控状态：\n\n⚠️ 高风险：2项\n- 物料 XX001 库存不足\n- 供应商 YY002 交期延迟\n\n🟡 中风险：5项\n\n需要查看详细风险报告吗？",
            "suggestions": ["查看详细风险", "查看预警设置", "处理风险"]
        },
        "whatif": {
            "message": "进入 What-if 分析模式。\n\n请告诉我你想模拟的场景，例如：\n- 「如果订单增加100件会怎样」\n- 「如果提前5天交货会怎样」",
            "suggestions": ["订单增加场景", "提前交货场景", "查看历史模拟"]
        },
        "help": {
            "message": "我是 AI MRP 智能助手，我可以帮你：\n\n📦 库存管理\n- 查询库存数量\n- 查看库存变动\n\n📋 订单管理\n- 查询订单状态\n- 创建新订单\n\n🔢 MRP 计算\n- 运行 MRP 计算\n- 查看采购建议\n\n📈 智能预测\n- 需求预测分析\n- 安全库存推荐\n\n⚠️ 风险预警\n- 库存风险监控\n- 供应商风险\n\n💡 What-if 分析\n- 场景模拟\n- 方案对比\n\n请告诉我你需要什么帮助？",
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
        action=_build_action(intent, entities)
    )


def _build_stock_response(entities: Dict[str, Any]) -> str:
    """构建库存查询响应"""
    if entities.get("item_code"):
        return f"物料 {entities['item_code']} 当前库存：150 件\n安全库存：50 件\n可用数量：100 件"
    
    return "当前库存概览：\n\n📦 总物料数：256 种\n✅ 充足：198 种\n⚠️ 低于安全库存：35 种\n❌ 缺货：23 种\n\n需要查看具体物料库存吗？"


def _build_order_response(entities: Dict[str, Any]) -> str:
    """构建订单查询响应"""
    if entities.get("order_id"):
        return f"订单 {entities['order_id']} 状态：生产中\n预计完工：3天后\n已完成：60%"
    
    return "当前订单概览：\n\n📋 进行中订单：45 个\n✅ 已完成：123 个\n⏳ 待确认：12 个\n\n需要查看具体订单吗？"


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
        "query_forecast": {"type": "API", "endpoint": "/predict/demand", "params": entities},
        "query_risk": {"type": "API", "endpoint": "/risk/query", "params": {}},
        "whatif": {"type": "PAGE", "page": "/whatif", "params": entities}
    }
    
    return action_map.get(intent, {})


# ========== API 接口 ==========

@router.post("/", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """
    AI 对话接口
    """
    message = request.message
    session_id = request.session_id or "default"
    
    # 1. 意图识别
    intent = recognize_intent(message)
    
    # 2. 实体提取
    entities = extract_entities(message)
    
    # 3. 构建响应
    response = build_response(intent, entities, session_id)
    
    # 4. 记录上下文
    # TODO: 存储到 Redis
    
    return response


@router.get("/history/{session_id}")
async def get_history(session_id: str, limit: int = 10):
    """
    获取对话历史
    """
    # TODO: 从 Redis 获取历史
    return {
        "session_id": session_id,
        "messages": []
    }


@router.get("/intents")
async def get_supported_intents():
    """获取支持的意图列表"""
    return {
        "intents": list(INTENT_PATTERNS.keys()),
        "total": len(INTENT_PATTERNS)
    }


@router.post("/feedback")
async def submit_feedback(session_id: str, message: str, 
                          intent: str, correct_intent: str):
    """
    用户反馈 - 用于意图识别训练
    """
    # TODO: 存储反馈数据用于模型优化
    return {
        "status": "success",
        "message": "感谢反馈，我们将持续优化识别准确率"
    }
