# 智能客服系统架构说明

## 设计目标
- 高并发：支持水平扩展与热点缓存。
- 低延迟：缓存命中优先，模型调用链路可降级。
- AI 应用：RAG 检索增强 + 多模型路由 + 人工兜底。

## 目录命名
- 后端工程目录：`cs-server`
- 前端工程目录：`cs-web`

## 模块划分
- `ChatController`：统一接收 `/api/chat` 与 `/api/chat/stream` 请求。
- `ChatService`：编排限流、缓存、RAG、模型生成、事件发布。
- `RagOrchestrator`：混合检索、上下文构造、Prompt 组织。
- `ModelRouter`：按问题复杂度和检索置信度选择 FAST / STRONG 模型。
- `LlmGateway`：云端 API 调用，配合熔断/重试，失败回退本地 mock 响应。
- `HumanHandoffService`：低置信度和敏感问题触发人工转接。
- `SemanticCacheService` + `SessionStore`：应答缓存与多轮上下文存储。
- `KafkaProducer`：异步写入会话事件（开关可配置）。

## 关键链路
1. 客户端请求进入 `ChatController`。
2. `ChatRateLimiter` 执行用户维度限流。
3. `SemanticCacheService` 尝试命中热点问题缓存。
4. 未命中则进入 `RagOrchestrator` 检索知识并估算置信度。
5. `HumanHandoffService` 判定是否转人工。
6. `ModelRouter` 选择模型，`LlmGateway` 调用云 API（含熔断与重试）。
7. 回写缓存与会话上下文，`KafkaProducer` 异步发布事件。

## 高并发低延迟策略
- 限流：用户维度滑动窗口限流。
- 熔断重试：`resilience4j` 配置在模型调用链路。
- 降级：云 API 异常时自动回退 mock 生成，保证服务连续性。
- 缓存：5分钟语义缓存，减少重复问题调用模型。

## 安全与可维护性
- 请求参数使用 `jakarta.validation` 校验。
- 全局异常处理统一返回标准错误码并输出告警日志。
- 模块职责清晰，便于替换真实向量库和模型网关实现。
