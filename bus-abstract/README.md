# Bus Abstract 模块

Euonia 消息总线的抽象契约层，定义了消息传输、分发、约定分类、策略路由和接收者注册的核心接口。该模块不包含任何具体传输实现，而是为 `bus-core`（编排层）和各传输适配器（`bus-inmemory`、`bus-rabbitmq`、`bus-kafka`）提供统一的扩展点。

---

## 架构

```
                        ┌─────────────────────────┐
                        │      Configurator         │  ← 全局约定、策略、处理器注册
                        └────────────┬────────────┘
                                     │
          ┌──────────────────────────┼──────────────────────────┐
          │                          │                          │
          ▼                          ▼                          ▼
┌─────────────────┐     ┌─────────────────────┐     ┌─────────────────────┐
│ MessageConvention│     │  TransportStrategy   │     │  HandlerRegistration │
│  (消息分类)       │     │  (传输策略路由)       │     │  (处理器元数据)       │
│                  │     │                      │     │                      │
│ unicast/multicast│     │ outgoing / incoming  │     │ channel + type       │
│ /request         │     │ local / distributed  │     │ + method             │
└────────┬────────┘     └──────────┬───────────┘     └─────────────────────┘
         │                         │
         ▼                         ▼
┌─────────────────┐     ┌─────────────────────┐
│    Dispatcher    │────▶│     Transport        │  ← publishAsync / sendAsync / requestAsync
│ (选择传输通道)    │     │   (抽象传输契约)      │
└─────────────────┘     └─────────────────────┘

                         ┌─────────────────────┐
                         │   RecipientRegistrar │  ← 将 HandlerRegistration 绑定到传输
                         └─────────────────────┘
                                     │
                          ┌──────────┴──────────┐
                          ▼                     ▼
                   ┌────────────┐       ┌────────────┐
                   │  Subscriber │       │  Executor   │
                   │ (多播接收者) │       │(单播/请求接收)│
                   └────────────┘       └────────────┘
```

---

## 核心概念

### 约定（Convention）—— 消息类型分类

消息约定决定消息的投递语义：**单播**、**多播**还是**请求-响应**。支持两种分类方式：

| 分类方式 | 实现 | 说明 |
|---------|------|------|
| **接口标记** | `DefaultMessageConvention` | 消息类实现 `Unicast`、`Multicast`、`Request<R>` 接口 |
| **注解标记** | `AnnotationMessageConvention` | 消息类标注 `@Unicast`、`@Multicast`、`@Request` 注解 |

```java
// 方式一：接口标记
public class CreateOrderCommand implements Unicast { ... }
public class OrderCreatedEvent implements Multicast { ... }
public class GetOrderQuery implements Request<OrderDto> { ... }

// 方式二：注解标记
@Unicast
public class CreateOrderCommand { ... }
@Multicast
public class OrderCreatedEvent { ... }
@Request(responseType = OrderDto.class)
public class GetOrderQuery { ... }
```

| 类 / 接口 | 描述 |
|-----------|------|
| `MessageConvention` | 分类契约接口：`isUnicastType()`、`isMulticastType()`、`isRequestType()` |
| `BaseMessageConvention` | 组合引擎 — 聚合多个约定，带结果缓存 |
| `DefaultMessageConvention` | 基于 `Unicast` / `Multicast` / `Request` 接口的约定 |
| `AnnotationMessageConvention` | 基于 `@Unicast` / `@Multicast` / `@Request` 注解的约定 |
| `OverridableMessageConvention` | 包装约定，允许通过谓词覆写分类结果 |
| `MessageConventionBuilder` | 流式构建器，聚合多个约定实例 |
| `MessageConventionType` | 枚举：`NONE`、`UNICAST`、`MULTICAST`、`REQUEST` |

### 策略（Strategy）—— 传输路由

传输策略决定消息的出站/入站传输选择。与约定正交：约定决定"怎么投"，策略决定"走哪条通道"。

| 类 / 接口 | 描述 |
|-----------|------|
| `TransportStrategy` | 策略契约：`outgoing()` / `incoming()` 返回消息类型匹配的传输名 |
| `BaseTransportStrategy` | 组合引擎 — 聚合多个策略，带结果缓存 |
| `DefaultTransportStrategy` | 中性默认策略，始终返回 `false` |
| `AnnotationTransportStrategy` | 基于 `@DispatchIn` / `@ReceiveIn` 注解的策略 |
| `LocalMessageTransportStrategy` | 匹配 `@LocalMessage` 标注的类型 |
| `DistributedMessageTransportStrategy` | 匹配 `@DistributedMessage` 标注的类型 |
| `OverridableTransportStrategy` | 包装策略，允许通过谓词覆写 |
| `TransportStrategyBuilder` | 流式构建器 |

### 传输（Transport）—— 抽象契约

`Transport` 接口是所有传输适配器的契约，`bus-core.MessageBus` 通过它完成实际的消息投递。

| 方法 | 说明 |
|------|------|
| `getName()` | 返回传输名称（如 `"inmemory"`、`"rabbitmq"`、`"kafka"`） |
| `publishAsync(RoutedMessage)` | 发布/多播消息 |
| `sendAsync(RoutedMessage)` | 单播消息（无响应） |
| `sendAsync(RoutedMessage, Class<R>)` | 单播消息，返回 `CompletableFuture<R>` |
| `requestAsync(RoutedMessage, Class<R>)` | 请求-响应，返回 `CompletableFuture<R>` |

### 消息信封（Envelope）

| 类 / 接口 | 描述 |
|-----------|------|
| `MessageEnvelope` | 信封接口：`messageId`、`correlationId`、`conversationId`、`requestTrackId`、`channel` |
| `RoutedMessage<T>` | 完整传输消息 — 封装载荷 + 路由元数据 + 时间戳 + 授权信息 |
| `MessageHeaders` | 消息头常量（`MessageId`、`CorrelationId`、`ConversationId`、`ContentType`、`RequestTraceId`、`Authorization`） |
| `MessageMetadata` | 可扩展的元数据 Map，带类型化读取方法 |

### 处理上下文（Context）

| 类 / 接口 | 描述 |
|-----------|------|
| `MessageContext` | 处理上下文抽象 — 提供消息访问、响应发送、失败通知、完成回调 |
| `MessageContextBase` | 默认实现 — 基于 `SubmissionPublisher` 的响应事件流，支持完成/失败监听器 |

### 分发与注册

| 类 / 接口 | 描述 |
|-----------|------|
| `Dispatcher` | 消息类型 → 传输名称的决策接口 |
| `HandlerContext` | 处理器上下文 — 管理消息订阅监听器，提供 `handleAsync()` 方法 |
| `HandlerRegistration` | **Record** — 处理器注册元组：`channel`、`messageType`、`handlerType`、`method` |
| `Configurator` | 全局配置接口 — 暴露约定构建器、策略构建器（按传输名）、处理器注册列表 |
| `RecipientRegistrar` | 接收者注册器 — 将 `HandlerRegistration` 列表绑定到具体传输 |

### 接收者角色

| 接口 | 说明 |
|------|------|
| `Recipient` | 接收者基类（继承 `AutoCloseable`），提供 `getName()` |
| `Subscriber` | 多播接收者 — 用于 `Multicast` 消息 |
| `Executor` | 单播/请求接收者 — 用于 `Unicast` 和 `Request` 消息 |

### 事件体系

| 类 | 描述 |
|----|------|
| `MessageProcessedEvent` | 基础事件 — 包含消息、上下文、处理类型 |
| `MessageDeliveredEvent` | 消息已投递 |
| `MessageReceivedEvent` | 消息已接收 |
| `MessageAcknowledgedEvent` | 消息已确认 |
| `MessageRepliedEvent` | 消息已回复 — 包含响应载荷 |
| `MessageHandledEvent` | 消息已处理 — 包含处理器类型 |
| `MessageSubscribedEvent` | 订阅元数据 — channel、messageType、handlerType |
| `MessageProcessType` | 处理类型枚举：`SEND`、`DELIVERED`、`RECEIVED`、`ACKNOWLEDGED`、`REPLIED`、`HANDLED` |

### 异常层次

| 异常类 | 说明 |
|--------|------|
| `MessageDeliverException` | 消息投递失败 |
| `MessageProcessingException` | 消息处理过程失败 |
| `MessageTransportException` | 传输层错误 |
| `MessageTypeException` | 无效/未分类的消息类型 |

### 注解

| 注解 | 作用域 | 说明 |
|------|--------|------|
| `@Subscribe` | 方法 | 标记消息处理器方法，指定 `channel` 和 `group` |
| `@Channel` | 类型 | 为消息类型指定默认通道 |
| `@Unicast` | 类型 | 标记为单播消息 |
| `@Multicast` | 类型 | 标记为多播消息 |
| `@Request` | 类型 | 标记为请求-响应消息，指定 `responseType()` |
| `@LocalMessage` | 类型 | 标记为本地传输消息 |
| `@DistributedMessage` | 类型 | 标记为分布式传输消息 |
| `@DispatchIn` | 类型 | 指定出站传输通道 |
| `@ReceiveIn` | 类型 | 指定入站传输通道 |
| `@Enqueue` | 类型 | 指定队列名称和优先级 |

### 序列化

| 接口 | 说明 |
|------|------|
| `MessageSerializer` | 消息序列化契约（预留接口） |

---

## 与其他模块的关系

```
bus-abstract  (本模块 — 契约层)
    │
    ├──▶ bus-core      (编排层)   — 实现 Configurator、Dispatcher、HandlerContext；
    │                                MessageBus 使用 Transport、RoutedMessage、异常等
    │
    ├──▶ bus-inmemory  (传输适配器) — 实现 Transport、RecipientRegistrar、Subscriber、Executor
    ├──▶ bus-rabbitmq  (传输适配器) — 实现 Transport（骨架）
    └──▶ bus-kafka     (传输适配器) — 依赖声明，待实现
```

---

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>com.euonia</groupId>
    <artifactId>bus-abstract</artifactId>
    <version>${revision}</version>
</dependency>
```

### 定义消息类型

```java
import com.euonia.bus.contract.Unicast;
import com.euonia.bus.contract.Multicast;
import com.euonia.bus.contract.Request;

// 单播命令
public class CreateOrderCommand implements Unicast {
    private String orderId;
    // ...
}

// 多播事件
public class OrderCreatedEvent implements Multicast {
    private String orderId;
    // ...
}

// 请求-响应
public class GetOrderQuery implements Request<OrderDto> {
    private String orderId;
    // ...
}
```

### 使用注解方式

```java
import com.euonia.bus.annotation.*;

@Unicast
@Channel("orders")
@DispatchIn(transports = {"rabbitmq"})
public class CreateOrderCommand { ... }

@Multicast
@DistributedMessage
public class OrderCreatedEvent { ... }
```

### 配置消息约定与策略

```java
import com.euonia.bus.convention.*;
import com.euonia.bus.strategy.*;

// 构建消息约定
MessageConvention convention = new MessageConventionBuilder()
        .add(new DefaultMessageConvention())       // 接口标记
        .add(new AnnotationMessageConvention())     // 注解标记
        .getConvention();

// 构建传输策略
TransportStrategy strategy = new TransportStrategyBuilder()
        .add(new AnnotationTransportStrategy())
        .add(new LocalMessageTransportStrategy())
        .add(new DistributedMessageTransportStrategy())
        .getStrategy();

// 判断消息类型
boolean isUnicast = convention.isUnicastType(CreateOrderCommand.class);     // true
boolean isMulticast = convention.isMulticastType(OrderCreatedEvent.class);  // true
boolean isRequest = convention.isRequestType(GetOrderQuery.class);          // true
```

---

## 设计模式

| 模式 | 应用 |
|------|------|
| **策略模式** | `MessageConvention` 和 `TransportStrategy` 的多种实现 |
| **组合模式** | `BaseMessageConvention` 和 `BaseTransportStrategy` 聚合多个求值器 |
| **构建器模式** | `MessageConventionBuilder` 和 `TransportStrategyBuilder` |
| **装饰器模式** | `OverridableMessageConvention` 和 `OverridableTransportStrategy` |
| **标记接口 / 标记注解** | 双通道消息分类（`Unicast` / `@Unicast`） |
| **观察者模式** | `MessageContextBase` 通过 `SubmissionPublisher` 发布响应和完成事件 |
| **信封模式** | `RoutedMessage` 携带载荷和传输元数据 |
| **注册表模式** | `Configurator` 存储策略构建器和处理器注册 |

---

## 许可证

本项目使用 [MIT License](../../LICENSE)。
