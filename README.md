# 智能客服系统（高并发 / 低延迟）

这是一个面向企业场景的智能客服工程化落地项目，目标是将 AI 能力从“可演示”推进到“可运行、可扩展、可验证”。

项目围绕以下核心诉求设计：
- 高并发：在服务层做限流、缓存与异步解耦，提升吞吐能力
- 低延迟：优先命中缓存，模型调用链路支持熔断/重试/降级
- AI 应用：RAG 检索增强 + 多模型路由 + 转人工兜底

## 目录结构

```text
.
├── cs-server/      # Java Spring Boot 后端服务
├── cs-web/         # React + Vite 前端对话页面
├── docs/           # 架构、调研、测试文档
├── tests/          # 压测脚本（k6）
└── tools/          # 本地工具（如便携 Maven）
```

## 核心能力

### 1) 智能问答主链路
- `ChatController`：提供 `/api/chat` 与 `/api/chat/stream`
- `ChatService`：编排限流、缓存、RAG、模型调用、事件发布
- `RagOrchestrator`：混合检索与 Prompt 组织
- `ModelRouter`：按问题复杂度与置信度选择模型层级
- `HumanHandoffService`：低置信度/敏感问题转人工

### 2) 高并发低延迟策略
- `ChatRateLimiter`：用户维度限流，保护核心链路
- `SemanticCacheService`：热点问题缓存，减少重复 LLM 调用
- `resilience4j`：模型调用侧熔断与重试
- `KafkaProducer`：异步事件投递，解耦非实时处理

### 3) 工程质量与可维护性
- 参数校验：`jakarta.validation`
- 全局异常处理：统一错误码 + 告警日志
- 分层清晰：便于替换真实向量库、模型网关与监控体系

## 技术栈

- 后端：Java 21、Spring Boot 3、Redis、Kafka、Resilience4j
- 前端：React 18、TypeScript、Vite
- 测试与验证：JUnit、MockMvc、k6

## 快速启动

## 先决条件
- JDK 21
- Node.js 18+
- Maven 3.9+（或项目内 `tools/apache-maven-3.9.6`）

## 后端

```bash
cd cs-server
mvn test
mvn spring-boot:run
```

如果本机没有全局 Maven，可在项目根目录使用便携 Maven：

```bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
./tools/apache-maven-3.9.6/bin/mvn -Dmaven.repo.local="$(pwd)/.m2repo" -f cs-server/pom.xml test
```

## 前端

```bash
cd cs-web
npm install
npm run build
npm run dev
```

## 压测

```bash
k6 run tests/k6/chat-load-test.js
```

## 本地验证记录（示例）

- 后端测试：核心单测 + 控制器集成测试通过
- 前端构建：TypeScript + Vite 打包通过
- 并发模拟（一次本地样例）：120 请求 / 并发 20，成功率 100%，P95 约 150ms

注：最终性能请以目标环境（真实模型 API、真实 Redis/Kafka、目标网络）复测结果为准。

## 文档索引

- 架构说明：`docs/architecture.md`
- 市场调研：`docs/market-research.md`
- 测试报告：`docs/test-report.md`

## 可直接用于填表的“成果描述”示例

我基于 Agent 驱动从 0 到 1 落地了一套智能客服系统，核心解决了传统客服知识更新慢、复杂问题处理不稳定、并发上来后延迟抖动明显这三个痛点。技术上采用了“RAG 检索增强 + 多模型路由 + 转人工兜底”的主链路，并在工程层加入限流、语义缓存、异步事件、熔断重试，形成可降级的高并发低延迟架构。项目已完成后端服务、前端对话页、自动化测试与压测脚本，能够支撑多轮问答与来源引用，且在本地模拟压测中达到稳定低延迟和 100% 请求成功率，具备继续接入真实知识库与生产模型网关的落地基础。
