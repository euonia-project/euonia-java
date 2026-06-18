# Bus InMemory 模块

Euonia 消息总线的进程内传输适配器。基于 `bus-abstract` 的抽象契约，提供无外部中间件的纯内存消息传递实现，适用于开发测试、单进程集成以及需要超低延迟的场景。

---

## 架构

```
                          InMemoryTransport
                    (实现 com.euonia.bus.contract.Transport)
                                  │
                    ┌─────────────┼─────────────┐
                    │             │             │
                    ▼             ▼             ▼
            publishAsync    sendAsync     requestAsync
            (WeakRef 发送)  (StrongRef)   (sendAsync 包装)
                    │             │             │
                    └─────────────┼─────────────┘
                                  │
                                  ▼
                    ┌─────────────────────────┐
                    │       MessagePack        │
                    │  RoutedMessage + Context  │
                    └────────────┬────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
      ┌──────────────────────┐    ┌──────────────────────┐
      │ StrongReferenceMess. │    │ WeakReferenceMess.    │
      │  (单播 / 请求-响应)    │    │  (多播 / 发布-订阅)     │
      └──────────┬───────────┘    └──────────┬───────────┘
                 │                           │
                 ▼                           ▼
         InMemoryRecipient            InMemoryRecipient
                 │                           │
    ┌────────────┼────────────┐    ┌─────────┼─────────┐
    ▼            ▼            ▼    ▼         ▼         ▼
UnicastRec.  RequestRec.   MulticastRecipient
(Executor)  (Executor)     (Subscriber)
    │            │            │
    └────────────┴────────────┘
                 │
                 ▼
         HandlerContext
      (bus-core 处理器运行时)

    ┌──────────────────────────────────────┐
    │      InMemoryRecipientRegistrar       │
    │  HandlerRegistration → Recipient     │
    │  约定分类 + 策略过滤 + 信使注册          │
    └──────────────────────────────────────┘
```

---

## 核心概念

### InMemoryTransport — 进程内传输实现

实现 `com.euonia.bus.contract.Transport` 接口，是整个模块的入口点。

| 方法 | 信使 | 异步模型 |
|------|------|----------|
| `publishAsync(message)` | `WeakReferenceMessenger` | 发送后立即返回已完成 Future |
| `sendAsync(message)` | `StrongReferenceMessenger` | 等待 `context.onReplied` 或 `complete` 事件 |
| `sendAsync(message, Class<R>)` | `StrongReferenceMessenger` | 从 reply 事件载荷转型为 `R`，失败则异常 |
| `requestAsync(message)` | `StrongReferenceMessenger` | 委托给 `sendAsync(message, Object.class)` |

**投递流程：**
1. 从 `RoutedMessage` 创建 `MessageContextBase`（拷贝 IDs / auth / metadata）
2. 包装为 `MessagePack`（message + context + aborted 标志）
3. 通过对应信使发送至目标通道
4. 触发 `MessageDeliveredEvent` 监听器

**生命周期：** 实现 `AutoCloseable`，关闭时重置 `StrongReferenceMessenger` 默认实例。

### 信使引擎（Messenger）

内部消息分发子系统，基于引用类型分为两种模式：

#### StrongReferenceMessenger

- **用途：** Unicast、Request 消息
- **注册结构：** `ConcurrentMap<Class<?>, Map<String, Map<RecipientKey, MessageHandlerDispatcher>>>`
- **RecipientKey：** 基于接收者对象**身份**的包装器（非 `equals`），防止重复注册
- **发送：** 按消息类（精确匹配）+ 通道查找，快照处理防止迭代中突变
- **快速路径：** 接收者实现 `Recipient<TMessage>` 时，使用 `NULL` dispatcher 标记，直接调用 `receive()`

#### WeakReferenceMessenger

- **用途：** Multicast 消息
- **注册结构：** 同 Strong，但使用 `WeakKey`（`WeakReference` 包装器）
- **WeakKey：** 哈希在创建时固化；相等性基于 referent 身份（已回收则退化为 key 对象身份）
- **发送：** 跳过已被 GC 回收的接收者
- **cleanup：** 扫描 `recipientKeys`，移除已回收接收者的所有注册
- **GC 语义：** 接收者无人持有强引用时自动退订

### InMemoryRecipient — 接收者基类

实现 `com.euonia.bus.messenger.Recipient<MessagePack>`，所有具体接收者的抽象父类。

**接收流水线：**
```
receive(MessagePack)
  ├── 提取 RoutedMessage + MessageContext
  ├── 触发 MessageReceivedEvent
  ├── 调用 handleAsync(channel, payload, context, aborted)  ← 子类实现
  └── 触发 MessageAcknowledgedEvent（不等异步完成）
```

**事件系统：** 支持 `messageReceivedListeners` 和 `messageAcknowledgedListeners` 回调。

### 具体接收者

| 类 | 角色 | handleAsync 行为 |
|----|------|-----------------|
| `InMemoryUnicastRecipient` | `Executor` | 委托 `HandlerContext.handleAsync`；`whenComplete` 中调用 `context.complete(message)` |
| `InMemoryMulticastRecipient` | `Subscriber` | 同上（行为一致，语义不同） |
| `InMemoryRequestRecipient` | `Executor` | 同上 |

> 三种接收者的 `handleAsync` 实现完全相同：委托 handler context 调用 → 捕获日志异常 → 完成上下文。其差异在于 `InMemoryRecipientRegistrar` 将它们注册到不同信使。

### InMemoryRecipientRegistrar — 注册映射器

实现 `com.euonia.bus.recipient.RecipientRegistrar`，将 `HandlerRegistration` 列表映射为信使注册。

**映射规则：**

| 消息类型 | 接收者 | 信使 | 实例策略 |
|----------|--------|------|----------|
| `Unicast` | `InMemoryUnicastRecipient` | `StrongReferenceMessenger` | 单例或按需创建 |
| `Multicast` | `InMemoryMulticastRecipient` | `WeakReferenceMessenger` | 单例或按需创建 |
| `Request` | `InMemoryRequestRecipient` | `StrongReferenceMessenger` | 单例或按需创建 |

**过滤逻辑：**
- 非默认传输且策略 `incoming(messageType)` 返回 `false` → 跳过

**实例策略：** 由 `InMemoryBusOptions.multipleSubscriberInstance` 控制：
- `false`（默认）：每个接收者类型一个单例
- `true`：每个处理器注册创建独立实例

### MessagePack — 传输信封

| 字段 | 类型 | 说明 |
|------|------|------|
| `message` | `RoutedMessage<?>` | 传输消息（载荷 + ID + 通道 + 元数据） |
| `context` | `MessageContext` | 处理上下文（响应/失败/完成生命周期） |
| `aborted` | `boolean` | 消息是否已中止 |

`MessagePack` 是信使系统中实际注册和投递的消息类型（而非用户载荷类型），作为信封在 Transport → Messenger → Recipient 之间传递。

### InMemoryBusOptions — 配置选项

| 选项 | 默认值 | 说明 |
|------|--------|------|
| `DEFAULT_TRANSPORT_NAME` | `InMemoryTransport` | 传输名称 |
| `name` | `InMemoryTransport` | 传输实例名 |
| `lazyInitialize` | `false` | 延迟初始化 |
| `maxConcurrentCalls` | `Integer.MAX_VALUE` | 最大并发调用数 |
| `multipleSubscriberInstance` | `false` | 多订阅者实例 |

### InMemoryMessageDispatcher — 通道分发器

独立的通道分发器单例（`channel → List<Consumer<MessagePack>>`），支持基于通道名的手动注册/分发。**当前未与信使引擎集成。**

---

## 完整消息生命周期

### 启动阶段 — 处理器注册

```
HandlerRegistration 列表（来自 bus-core）
        │
        ▼
InMemoryRecipientRegistrar.register()
        │
        ├── 约定分类 → 确定接收者类型
        ├── 策略过滤 → 仅入站匹配的消息类型
        ├── 获取/创建接收者实例
        └── 向对应信使注册 (MessagePack.class, channel)
```

### 运行时 — Publish（发布/多播）

```
1. 调用方: bus.publishAsync(message)
2. MessageBus → Transport.publishAsync(routedMessage)
3. InMemoryTransport:
   ├── 创建 MessageContextBase + MessagePack
   ├── WeakReferenceMessenger.send(pack, channel)
   └── 返回已完成 CompletableFuture
4. WeakReferenceMessenger:
   ├── 按 MessagePack.class + channel 查找 WeakKey 列表
   ├── 快照当前处理器
   └── 逐一调用 InMemoryMulticastRecipient.receive(pack)
5. InMemoryMulticastRecipient:
   ├── 触发 MessageReceivedEvent
   ├── handleAsync → HandlerContext.handleAsync(channel, message, context)
   ├── DefaultHandlerContext: 并行执行所有处理器（忽略返回值）
   └── 触发 MessageAcknowledgedEvent
```

### 运行时 — Send（单播）

```
1. 调用方: bus.sendAsync(command)
2. MessageBus → Transport.sendAsync(routedMessage)
3. InMemoryTransport:
   ├── 创建 MessageContextBase + MessagePack
   ├── 创建 CompletableFuture<Void>
   ├── 订阅 context.onReplied: onNext → complete, onError → completeExceptionally
   ├── 添加完成回调: 若 future 仍未完成，触发 complete
   ├── StrongReferenceMessenger.send(pack, channel)
   └── 返回 CompletableFuture<Void>
4. StrongReferenceMessenger:
   ├── 按 MessagePack.class + channel 查找
   └── 调用 InMemoryUnicastRecipient.receive(pack)
5. InMemoryUnicastRecipient:
   ├── 触发 MessageReceivedEvent
   ├── handleAsync → HandlerContext.handleAsync
   └── DefaultHandlerContext: 执行单个处理器
       ├── 结果非 null → context.response(result)
       ├── 异常 → context.failure(cause)
       └── whenComplete → context.complete(message)
6. context.onReplied 触发 → future 完成
```

### 运行时 — Request（请求-响应）

```
1. 调用方: bus.callAsync(request)
2. sendAsync(message, responseType) — 与 Send 类似
3. context.onReplied 事件的 result 被转型为 responseType
4. 转型成功 → future.complete(typedResult)
5. 转型失败 / handler 异常 → future.completeExceptionally
```

---

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>com.euonia</groupId>
    <artifactId>bus-inmemory</artifactId>
    <version>${revision}</version>
</dependency>
```

### 配置进程内总线

```java
import com.euonia.bus.*;
import com.euonia.bus.convention.*;
import com.euonia.bus.options.*;
import com.euonia.reflection.SimpleServiceProvider;

// 1. 创建传输
var transport = new InMemoryTransport(new InMemoryBusOptions());

// 2. 注册到 ServiceProvider
var provider = new SimpleServiceProvider();
provider.addSingleton(transport);

// 3. 创建配置与选项
var configurator = new DefaultConfigurator();
configurator.setConvention(b -> b.add(new DefaultMessageConvention()));
configurator.setStrategy("InMemoryTransport", b -> {});

var options = new MessageBusOptions(configurator);
options.setDefaultTransport("InMemoryTransport");

// 4. 创建总线
var bus = new MessageBus(provider, new StrategicDispatcher(options), options);

// 5. 注册处理器
configurator.registerHandlers(OrderHandler.class);

// 6. 注册接收者
var registrar = new InMemoryRecipientRegistrar(provider, new InMemoryBusOptions(), configurator);
registrar.register(configurator.getRegistrations(), "InMemoryTransport");
```

### 发布-订阅示例

```java
// 消息
public class OrderCreatedEvent implements Multicast {
    private String orderId;
    // ...
}

// 发送
bus.publishAsync(new OrderCreatedEvent("123"))
    .toCompletableFuture()
    .join();
```

### 单播示例

```java
// 命令
public class CreateOrderCommand implements Unicast {
    private String productId;
    // ...
}

// 发送（无响应）
bus.sendAsync(new CreateOrderCommand("P123"))
    .toCompletableFuture()
    .join();

// 发送（带响应回调）
bus.sendAsync(command, String.class, new Flow.Subscriber<>() {
    @Override public void onNext(String result) {
        System.out.println("Order ID: " + result);
    }
    @Override public void onError(Throwable t) { t.printStackTrace(); }
    @Override public void onComplete() { System.out.println("Done"); }
    @Override public void onSubscribe(Flow.Subscription s) { s.request(1); }
});
```

### 请求-响应示例

```java
// 查询
public class GetOrderQuery implements Request<OrderDto> {
    private String orderId;
    // ...
}

// 调用
CompletableFuture<OrderDto> future = bus.callAsync(
    new GetOrderQuery("123"), OrderDto.class);

OrderDto result = future.join();
```

---

## 与其他模块的关系

```
bus-abstract (契约层)
    │
    ├──▶ Transport      → InMemoryTransport 实现
    ├──▶ RecipientRegistrar → InMemoryRecipientRegistrar 实现
    ├──▶ Executor       → InMemoryUnicastRecipient / InMemoryRequestRecipient
    ├──▶ Subscriber     → InMemoryMulticastRecipient
    ├──▶ MessageContext  → MessageContextBase (运行时)
    └──▶ RoutedMessage   → 作为 MessagePack 载荷

bus-core (编排层)
    │
    ├──▶ HandlerContext  → 注入到接收者中进行处理器调用
    └──▶ Configurator    → 提供给 InMemoryRecipientRegistrar 的约定/策略源
```

---

## 设计模式

| 模式 | 应用 |
|------|------|
| **适配器模式** | `InMemoryTransport` 将 `Transport` 契约适配为内部信使引擎 |
| **观察者模式** | `InMemoryRecipient` 的 received/acknowledged 事件；`MessageContextBase` 的 replied 事件 |
| **策略模式** | `InMemoryRecipientRegistrar` 根据 `MessageConvention` 选择接收者类型和信使类型 |
| **工厂模式** | `InMemoryRecipientRegistrar.getRecipient()` 通过 `ServiceProvider` 创建/缓存接收者实例 |
| **注册表模式** | `StrongReferenceMessenger` / `WeakReferenceMessenger` 基于通道的处理器注册 |
| **信封模式** | `MessagePack` 封装 `RoutedMessage` + `MessageContext` |
| **单例模式** | `StrongReferenceMessenger` / `WeakReferenceMessenger` 默认实例；`InMemoryRecipientRegistrar` 接收者缓存 |
| **快速路径模式** | Messenger 中 `NULL` dispatcher 标记，直接调用 `Recipient.receive()` |

---

## 许可证

本项目使用 [MIT License](../../LICENSE)。
