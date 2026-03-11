# AI MRP 运营 Dashboard 设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 核心指标

| 指标类型 | 指标项 |
|----------|---------|
| **销售指标** | 订单金额、订单数量、发货及时率 |
| **库存指标** | 库存周转率、库存周转天数、呆滞物料 |
| **生产指标** | 产线利用率、计划达成率、良品率 |
| **采购指标** | 采购及时率、供应商准时交货率 |
| **财务指标** | 营收、毛利、净利润、预算执行率 |

### 1.2 图表类型

| 图表 | 用途 |
|------|------|
| 折线图 | 趋势分析 |
| 柱状图 | 对比分析 |
| 饼图 | 占比分析 |
| 漏斗图 | 转化分析 |
| 热力图 | 时段分析 |
| 地图 | 区域分布 |

---

## 二、技术架构

### 2.1 技术选型

| 组件 | 技术 | 版本 |
|------|------|------|
| 图表库 | ECharts | 5.x |
| 数据可视化 | AntV | 2.x |
| 动画 | Animation | - |

### 2.2 数据架构

```
┌─────────────────────────────────────────────┐
│              Dashboard 前端                │
├─────────────────────────────────────────────┤
│  ┌─────────┐ ┌─────────┐ ┌─────────┐  │
│  │ KPI卡片 │ │ 折线图  │ │ 柱状图  │  │
│  └─────────┘ └─────────┘ └─────────┘  │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐  │
│  │ 饼图   │ │ 漏斗图  │ │ 热力图  │  │
│  └─────────┘ └─────────┘ └─────────┘  │
└─────────────────────────────────────────────┘
                    │ API
                    ▼
┌─────────────────────────────────────────────┐
│            Dashboard Service              │
├─────────────────────────────────────────────┤
│  ┌─────────┐ ┌─────────┐ ┌─────────┐  │
│  │ 聚合查询 │ │ 趋势计算 │ │ 对比计算 │  │
│  └─────────┘ └─────────┘ └─────────┘  │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐  │
│  │ 缓存    │ │ 定时任务│ │ 导出   │  │
│  └─────────┘ └─────────┘ └─────────┘  │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│              数据源                         │
├─────────────────────────────────────────────┤
│  PostgreSQL │ Redis │ ClickHouse          │
└─────────────────────────────────────────────┘
```

---

## 三、页面结构

### 3.1 目录结构

```
src/pages/Dashboard/
├── index.tsx                 # Dashboard主页
├── components/
│   ├── KPICard.tsx          # KPI卡片
│   ├── TrendChart.tsx       # 趋势图
│   ├── CompareChart.tsx      # 对比图
│   ├── PieChart.tsx        # 饼图
│   ├── FunnelChart.tsx     # 漏斗图
│   ├── HeatmapChart.tsx    # 热力图
│   ├── MapChart.tsx        # 地图
│   └── DashboardFilter.tsx # 筛选条件
├── hooks/
│   ├── useDashboardData.ts # 数据Hook
│   └── useChartConfig.ts   # 图表配置
└── utils/
    ├── chartOptions.ts     # 图表配置工具
    └── dataProcess.ts      # 数据处理工具
```

### 3.2 Dashboard 主页

```tsx
const DashboardPage: React.FC = () => {
  const { data, loading } = useDashboardData();
  
  return (
    <div className="dashboard">
      {/* 筛选条件 */}
      <DashboardFilter 
        onChange={handleFilterChange}
        defaultValue={defaultFilters}
      />
      
      {/* KPI 指标行 */}
      <Row gutter={16}>
        <Col span={4}><KPICard title="今日订单" value={data.todayOrders} trend={+5.2} /></Col>
        <Col span={4}><KPICard title="库存周转" value={data.turnover} trend={-1.3} /></Col>
        <Col span={4}><KPICard title="生产达成率" value={data.productionRate} trend={+2.8} /></Col>
        <Col span={4}><KPICard title="采购及时率" value={data.purchaseRate} trend={0} /></Col>
      </Row>
      
      {/* 趋势图表 */}
      <Row gutter={16}>
        <Col span={16}>
          <Card title="销售趋势">
            <TrendChart data={data.salesTrend} />
          </Card>
        </Col>
        <Col span={8}>
          <Card title="订单占比">
            <PieChart data={data.orderType} />
          </Card>
        </Col>
      </Row>
      
      {/* 对比图表 */}
      <Row gutter={16}>
        <Col span={12}>
          <Card title="各部门预算执行">
            <CompareChart data={data.budgetCompare} type="bar" />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="转化漏斗">
            <FunnelChart data={data.funnel} />
          </Card>
        </Col>
      </Row>
    </div>
  );
};
```

---

## 四、核心组件

### 4.1 KPI 卡片

```tsx
interface KPICardProps {
  title: string;           // 标题
  value: number | string;  // 数值
  unit?: string;          // 单位
  trend?: number;        // 趋势百分比
  trendDirection?: 'up' | 'down' | 'flat'; // 趋势方向
  suffix?: string;       // 后缀
}

const KPICard: React.FC<KPICardProps> = ({ 
  title, value, unit, trend, suffix 
}) => {
  const trendColor = trend > 0 ? '#52c41a' : trend < 0 ? '#f5222d' : '#999';
  
  return (
    <Card>
      <div className="kpi-card">
        <div className="kpi-title">{title}</div>
        <div className="kpi-value">
          {value}
          {unit && <span className="kpi-unit">{unit}</span>}
        </div>
        {trend !== undefined && (
          <div className="kpi-trend" style={{ color: trendColor }}>
            {trend > 0 ? '↑' : trend < 0 ? '↓' : '-'} 
            {Math.abs(trend)}%
        </div>
        )}
      </div>
    </Card>
  );
};
```

### 4.2 趋势图配置

```tsx
const getTrendChartOptions = (data: TrendData[]): EChartsOption => ({
  tooltip: {
    trigger: 'axis',
  },
  legend: {
    data: ['销售额', '订单数', '毛利'],
  },
  xAxis: {
    type: 'category',
    data: data.map(d => d.date),
  },
  yAxis: [
    { type: 'value', name: '金额' },
    { type: 'value', name: '数量' },
  ],
  series: [
    {
      name: '销售额',
      type: 'line',
      smooth: true,
      data: data.map(d => d.sales),
      itemStyle: { color: '#1890ff' },
    },
    {
      name: '订单数',
      type: 'line',
      yAxisIndex: 1,
      data: data.map(d => d.orders),
      itemStyle: { color: '#52c41a' },
    },
  ],
});
```

---

## 五、API 接口

### 5.1 数据接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/dashboard/kpi` | GET | KPI指标 |
| `/api/dashboard/sales-trend` | GET | 销售趋势 |
| `/api/dashboard/inventory` | GET | 库存分析 |
| `/api/dashboard/production` | GET | 生产分析 |
| `/api/dashboard/purchase` | GET | 采购分析 |
| `/api/dashboard/finance` | GET | 财务分析 |
| `/api/dashboard/export` | POST | 导出报表 |

### 5.2 后端服务

```java
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * KPI指标
     */
    @GetMapping("/kpi")
    public ApiResponse<KPIData> getKPI(DashboardQuery query) {
        KPIData data = dashboardService.getKPI(query);
        return ApiResponse.success(data);
    }

    /**
     * 销售趋势
     */
    @GetMapping("/sales-trend")
    public ApiResponse<List<TrendData>> getSalesTrend(DashboardQuery query) {
        List<TrendData> data = dashboardService.getSalesTrend(query);
        return ApiResponse.success(data);
    }

    /**
     * 库存分析
     */
    @GetMapping("/inventory")
    public ApiResponse<InventoryAnalysis> getInventoryAnalysis(DashboardQuery query) {
        InventoryAnalysis data = dashboardService.getInventoryAnalysis(query);
        return ApiResponse.success(data);
    }
}
```

---

## 六、缓存策略

### 6.1 缓存配置

```java
@Service
public class DashboardCacheService {

    private static final String CACHE_KEY_PREFIX = "dashboard:";
    private static final long EXPIRE_MINUTES = 5;

    /**
     * 获取KPI数据 (带缓存)
     */
    @Cacheable(value = "dashboard:kpi", key = "#query.toString()")
    public KPIData getKPIWithCache(DashboardQuery query) {
        return dashboardMapper.queryKPI(query);
    }

    /**
     * 清除缓存
     */
    @CacheEvict(value = "dashboard:*", allEntries = true)
    public void clearCache() {
        // 清除所有Dashboard缓存
    }

    /**
     * 定时刷新缓存
     */
    @Scheduled(cron = "0 */5 * * * ?")  // 每5分钟
    public void refreshCache() {
        // 预计算热门数据
    }
}
```

---

## 七、导出功能

### 7.1 Excel 导出

```java
/**
 * 导出Dashboard数据
 */
@PostMapping("/export")
public void exportDashboard(HttpServletResponse response, DashboardQuery query) {
    // 1. 查询数据
    DashboardData data = dashboardService.getFullData(query);

    // 2. 构建Excel
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Dashboard");

        // KPI
        createKPISheet(workbook, data.getKpi());

        // 趋势
        createTrendSheet(workbook, data.getTrend());

        // 3. 输出
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=dashboard.xlsx");
        workbook.write(response.getOutputStream());
    }
}
```

---

## 八，实施计划

| 周次 | 内容 |
|------|------|
| 第1周 | 框架搭建、KPI卡片、筛选器 |
| 第2周 | 趋势图、对比图 |
| 第3周 | 饼图、漏斗图、热力图 |
| 第4周 | 导出功能、性能优化 |

**预估工时**: 80小时

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
