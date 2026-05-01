# 智能客服系统

企业场景下的智能客服示例工程，覆盖从问答链路到基础工程能力的最小可运行闭环。当前版本已实现前后端联调、RAG 检索增强、模型路由、转人工兜底、限流与缓存，并保留向生产能力演进的扩展点。

## 功能概览

### 已实现
- 问答接口：`/api/chat` 与 `/api/chat/stream`
- 主链路编排：限流 -> 语义缓存 -> RAG 检索 -> 模型路由 -> 生成回答 -> 异步事件
- 风险兜底：低置信度/敏感问题转人工
- 模型调用可靠性：Resilience4j 熔断 + 重试 + 回退
- 前端对话页面：会话消息、失败重试、知识来源展示

### 可演进方向
- 对接真实向量库与召回排序链路
- 对接真实模型网关并统一响应解析
- 将会话与语义缓存迁移到 Redis 等外部存储
- 增加观测指标（缓存命中率、路由命中率、模型成本）

## 技术栈

- 后端：Java 21、Spring Boot 3.3、Spring Web、Validation、Actuator、Kafka、Redis Starter、Resilience4j
- 前端：React 18、TypeScript 5、Vite 5
- 测试与验证：JUnit 5、MockMvc、k6

## 项目结构

```text
.
├── cs-server/                # 后端服务（Spring Boot）
│   └── src/main/java/com/company/cs/
│       ├── api/              # Controller、DTO、全局异常
│       ├── service/          # Chat 主链路、RAG、模型路由、LLM 网关
│       ├── infra/            # cache/queue/ratelimit/retrieval/monitoring
│       └── domain/           # 领域对象
├── cs-web/                   # 前端应用（React + Vite）
├── docs/                     # 架构、调研、测试报告
└── tests/k6/                 # 压测脚本
```

## 快速启动

### 1) 环境要求
- JDK 21
- Maven 3.9+
- Node.js 18+

说明：当前仓库未包含 `tools/apache-maven-3.9.6`，默认使用本机安装的 Maven。

### 2) 启动后端

```bash
cd cs-server
mvn test
mvn spring-boot:run
```

默认端口 `8080`。

### 3) 启动前端

```bash
cd cs-web
npm install
npm run dev
```

默认端口 `5173`，并通过 Vite 代理将 `/api` 转发到 `http://localhost:8080`。

### 4) 最小联调验证

浏览器打开 `http://localhost:5173`，发送任意问题，或直接使用 curl：

```bash
curl -X POST "http://localhost:8080/api/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId":"demo-session-1",
    "userId":"demo-user",
    "message":"请问退款流程是什么？",
    "channel":"web"
  }'
```

## 配置说明

后端配置文件：`cs-server/src/main/resources/application.yml`

### Spring 通用配置
- `server.port`：服务端口，默认 `8080`
- `spring.data.redis.host/port`：Redis 连接参数，默认 `localhost:6379`
- `spring.kafka.bootstrap-servers`：Kafka 地址，默认 `localhost:9092`

### 业务配置（`app.*`）
- `app.kafka.enabled`：是否启用 Kafka 发送，默认 `false`
- `app.kafka.topic`：Kafka topic，默认 `cs-chat-events`
- `app.llm.cloud-enabled`：是否启用云端 LLM 调用，默认 `false`
- `app.llm.api-url` / `app.llm.api-key`：云端 LLM 地址与鉴权
- `app.llm.fast-model` / `app.llm.strong-model`：路由模型名
- `app.rate-limit.requests-per-window` / `app.rate-limit.window-seconds`：限流窗口配置

## API 简述

### `POST /api/chat`
请求体：

```json
{
  "sessionId": "string",
  "userId": "string",
  "message": "string, max 2000",
  "channel": "string"
}
```

响应体（示例字段）：

```json
{
  "requestId": "uuid",
  "sessionId": "string",
  "answer": "string",
  "route": "FAST | STRONG | HANDOFF",
  "handoff": false,
  "sources": []
}
```

### `POST /api/chat/stream`
- 返回 `text/event-stream`
- 事件类型：`meta`、`chunk`、`done`

## 测试与压测

### 后端测试

```bash
cd cs-server
mvn test
```

### 前端构建验证

```bash
cd cs-web
npm run build
```

### k6 压测

```bash
k6 run tests/k6/chat-load-test.js
```

压测脚本目标地址为 `http://localhost:8080/api/chat`，阈值：
- `http_req_failed < 1%`
- `http_req_duration p95 < 800ms`

## 已知限制与风险

- 当前 LLM 默认走 mock：`app.llm.cloud-enabled=false` 或未配置 `api-url` 时，不会调用真实云模型。
- 前端当前请求的是 `/api/chat`；页面里的“流式展示”是前端逐字渲染效果，不是后端 SSE 真流式消费。
- `/api/chat/stream` 为服务端将完整答案拆分后再推送 `chunk`，并非 token 级实时生成。
- `SemanticCacheService` 与 `SessionStore` 当前为进程内内存实现，不具备多实例共享能力。
- `app.redis.enabled` 当前未被业务代码消费；引入了 Redis starter 与 `StringRedisTemplate` 配置，但缓存与会话仍未落 Redis。
- 无 OpenAPI/Swagger 文档，接口请以 `ChatController` 与 DTO 定义为准。

## 文档索引

- 架构说明：`docs/architecture.md`
- 测试报告：`docs/test-report.md`
- 市场调研：`docs/market-research.md`
