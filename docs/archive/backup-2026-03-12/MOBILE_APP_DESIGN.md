# AI MRP 移动端设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、技术选型

### 1.1 技术栈

| 组件 | 技术 | 版本 |
|------|------|------|
| 框架 | Flutter | 3.x |
| 状态管理 | Riverpod | 2.x |
| 网络请求 | Dio | 4.x |
| 本地存储 | Hive | 2.x |
| 图表 | Fl_chart | 0.6.x |
| 扫码 | mobile_scanner | 3.x |
| 消息推送 | Firebase Cloud Messaging | - |

### 1.2 平台支持

| 平台 | 状态 |
|------|------|
| iOS | ✅ |
| Android | ✅ |
| H5 | ✅ |

---

## 二、功能模块

### 2.1 核心功能

| 模块 | 功能 |
|------|------|
| **首页** | 待办任务、快捷操作、关键指标 |
| **审批** | 待我审批、我已审批、审批详情 |
| **消息** | 系统通知、审批提醒、预警通知 |
| **扫描** | 物料扫码、库位扫码、扫二维码 |
| **查询** | 库存查询、订单查询、物流追踪 |
| **我的** | 个人设置、修改密码、退出登录 |

### 2.2 功能详情

#### 审批中心
```
- 待我审批
  - 采购订单审批
  - 生产工单审批
  - 库存调拨审批
  - 预算审批
- 我已审批
- 审批详情
  - 同意/拒绝
  - 审批意见
  - 查看流程图
- 催办
```

#### 扫码功能
```
- 物料扫码
  - 扫码识别物料
  - 查看物料详情
  - 库存数量
- 库位扫码
  - 扫码识别库位
  - 查看库位库存
- 生产报工
  - 扫码报工
  - 查看生产进度
```

---

## 三、页面结构

### 3.1 目录结构

```
lib/
├── main.dart
├── app.dart
├── core/
│   ├── constants/
│   │   ├── app_colors.dart
│   │   ├── app_strings.dart
│   │   └── app_styles.dart
│   ├── network/
│   │   ├── api_client.dart
│   │   └── interceptors.dart
│   ├── storage/
│   │   └── local_storage.dart
│   └── utils/
│       └── extensions.dart
├── features/
│   ├── home/
│   │   ├── home_page.dart
│   │   └── widgets/
│   ├── approval/
│   │   ├── approval_list_page.dart
│   │   ├── approval_detail_page.dart
│   │   └── widgets/
│   ├── message/
│   │   ├── message_list_page.dart
│   │   └── widgets/
│   ├── scan/
│   │   ├── scan_page.dart
│   │   └── widgets/
│   ├── query/
│   │   ├── inventory_query_page.dart
│   │   ├── order_query_page.dart
│   │   └── widgets/
│   └── profile/
│       ├── profile_page.dart
│       └── settings_page.dart
├── models/
│   ├── approval_task.dart
│   ├── message.dart
│   └── inventory.dart
└── repositories/
    ├── approval_repository.dart
    ├── message_repository.dart
    └── inventory_repository.dart
```

### 3.2 核心页面

```dart
// 首页
class HomePage extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tasks = ref.watch(pendingTasksProvider);
    
    return Scaffold(
      appBar: AppBar(title: const Text('AI MRP')),
      body: RefreshIndicator(
        onRefresh: () => ref.refresh(tasksProvider),
        child: ListView(
          children: [
            // 待办任务
            _buildPendingTasks(tasks),
            // 快捷操作
            _buildQuickActions(),
            // 关键指标
            _buildKPI(),
          ],
        ),
      ),
      bottomNavigationBar: BottomNavBar(),
    );
  }
}
```

---

## 四、API 对接

### 4.1 网络请求封装

```dart
class ApiClient extends Dio {
  ApiClient() {
    interceptors.add(AuthInterceptor());
    interceptors.add(LogInterceptor());
  }
  
  Future<T> get<T>(String path, {Map<String, dynamic>? params}) async {
    final response = await get(path, queryParameters: params);
    return ApiResponse.fromJson(response.data);
  }
  
  Future<T> post<T>(String path, {dynamic data}) async {
    final response = await post(path, data: data);
    return ApiResponse.fromJson(response.data);
  }
}
```

### 4.2 接口列表

| 接口 | 方法 | 功能 |
|------|------|------|
| `/api/mobile/home` | GET | 首页数据 |
| `/api/mobile/approval/todo` | GET | 待办任务 |
| `/api/mobile/approval/done` | GET | 已办任务 |
| `/api/mobile/approval/complete` | POST | 完成审批 |
| `/api/mobile/message/list` | GET | 消息列表 |
| `/api/mobile/scan/identify` | POST | 扫码识别 |
| `/api/mobile/inventory/query` | GET | 库存查询 |

---

## 五、本地缓存

### 5.1 Hive 存储

```dart
// 本地存储
class LocalStorage {
  static const tokenBox = 'token';
  static const userBox = 'user';
  static const cacheBox = 'cache';
  
  // 保存Token
  Future<void> saveToken(String token) async {
    await Hive.box(tokenBox).put('access_token', token);
  }
  
  // 获取Token
  String? getToken() {
    return Hive.box(tokenBox).get('access_token');
  }
  
  // 清除缓存
  Future<void> clear() async {
    await Hive.box(tokenBox).clear();
    await Hive.box(userBox).clear();
    await Hive.box(cacheBox).clear();
  }
}
```

---

## 六、推送集成

### 6.1 Firebase 配置

```dart
class PushNotificationService {
  static Future<void> initialize() async {
    await Firebase.initializeApp();
    
    // 获取Token
    final token = await FirebaseMessaging.instance.getToken();
    // 发送到服务器
    await api.updatePushToken(token);
    
    // 监听消息
    FirebaseMessaging.onMessage.listen((message) {
      // 处理前台消息
    });
    
    // 处理点击通知
    FirebaseMessaging.onMessageOpenedApp.listen((message) {
      // 跳转到对应页面
    });
  }
}
```

---

## 七、权限配置

### 7.1 Android (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

### 7.2 iOS (Info.plist)

```xml
<key>NSCameraUsageDescription</key>
<string>需要相机权限进行扫码</string>
<key>NSLocationWhenInUseUsageDescription</key>
<string>需要位置权限</string>
```

---

## 八、实施计划

| 周次 | 内容 |
|------|------|
| 第1周 | 项目初始化、基础框架、登录 |
| 第2周 | 首页、审批模块 |
| 第3周 | 消息、扫码功能 |
| 第4周 | 查询功能、我的模块 |
| 第5周 | 推送集成、测试优化 |

**预估工时**: 120小时

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
