# DDD 模块

领域驱动设计（Domain-Driven Design）—— Euonia 框架的战术设计工具箱。提供 Entity、Aggregate、ValueObject、Command、DomainEvent、UseCase 和 ApplicationService 等核心 DDD 构建块，帮助开发者以领域模型为中心构建高内聚、低耦合的业务系统。

该模块依赖 `core`、`pipeline`、`unit-of-work`、`bus-core` 模块，并与消息总线深度集成，支持 **命令分发**、**事件驱动** 和 **用例编排** 等多种交互模式。

---

## 架构

```
┌──────────────────────────────────────────────────────────────────┐
│                       DDD 模块架构                               │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────── 领域层 ──────────────────────────────┐ │
│  │                                                              │ │
│  │   Entity<ID>          Aggregate<ID>        ValueObject<T>   │ │
│  │      ↑                     ↑                    ↑            │ │
│  │   EntityBase<ID>     AggregateBase<ID>    (比较、等价)        │ │
│  │   (id 管理)          (事件管理、处理器注册)                    │ │
│  │                                                              │ │
│  ├─────────────────────── 事件系统 ────────────────────────────┤ │
│  │                                                              │ │
│  │   Event ◄── DomainEvent ◄── ApplicationEvent               │ │
│  │    ↑            ↑               ↑                            │ │
│  │   EventBase  DomainEventBase  ApplicationEventBase          │ │
│  │                                                              │ │
│  │   EventAggregate: 事件元数据聚合                               │ │
│  │   HasDomainEvents: 聚合根事件契约                             │ │
│  │                                                              │ │
│  ├─────────────────────── 命令系统 ────────────────────────────┤ │
│  │                                                              │ │
│  │   Command ◄── CommandBase                                   │ │
│  │   (extends Unicast)  (属性容器)                               │ │
│  │                                                              │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌─────────────────────── 应用层 ──────────────────────────────┐ │
│  │                                                              │ │
│  │   ApplicationService ◄── BaseApplicationService             │ │
│  │                        (ServiceProvider、Bus 集成)            │ │
│  │                                                              │ │
│  ├─────────────────────── 用例层 ──────────────────────────────┤ │
│  │                                                              │ │
│  │   UseCase<I,O>         UseCaseSuccess<O>                    │ │
│  │   UseCasePresenter<O>  UseCaseFailure                       │ │
│  │   (Reactive Streams / SubmissionPublisher)                   │ │
│  │                                                              │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌─────────────────────── 审计 ────────────────────────────────┐ │
│  │   @Audited            AuditRecord<ID>                       │ │
│  │   AuditStore           (实体审计日志)                         │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 核心概念

### 领域构建块（Domain Building Blocks）

| 类 / 接口 | 描述 |
|-----------|------|
| `Entity<ID>` | 实体接口 — 定义具有唯一标识（ID）的领域对象，通过 `getId()` 和 `getKeys()` 表达身份 |
| `EntityBase<ID>` | 实体抽象基类 — 提供 `id` 属性的默认实现 |
| `Aggregate<ID>` | 聚合根标记接口 — 继承 `Entity<ID>`，表示该实体是聚合的根 |
| `AggregateBase<ID>` | 聚合根基类 — 扩展 `EntityBase`，内置领域事件管理（注册、触发、应用、清理）和 `HasDomainEvents` 契约实现 |
| `ValueObject<T>` | 值对象基类 — 基于字段的 `equals`、`hashCode` 和 `compareTo` 实现，适合不可变值类型 |

#### 实体 (Entity)

```java
public interface Entity<ID extends Comparable<ID>> {
    ID getId();
    void setId(ID id);
    default Object[] getKeys() { return new Object[]{getId()}; }
}
```

实体是具有唯一标识的领域对象，通过标识而非属性来区分彼此。

#### 聚合根 (Aggregate)

```java
public interface Aggregate<ID extends Comparable<ID>> extends Entity<ID> { }
```

聚合是一组相关领域对象的集合，以聚合根为入口进行访问和修改。聚合根负责维护聚合内部的一致性，并通过 `HasDomainEvents` 契约实现事件溯源模式。

#### 值对象 (ValueObject)

```java
public class ValueObject<T extends ValueObject<T>> implements Comparable<T> {
    // 基于所有字段的 equals / hashCode / compareTo
}
```

值对象没有独立标识，通过其属性值来定义等价性。所有字段参与比较和哈希计算。

---

### 聚合根事件管理

| 接口 / 类 | 描述 |
|-----------|------|
| `HasDomainEvents` | 聚合根领域事件契约 — 定义 `getEvents()`、`raiseEvent()`、`applyEvent()`、`clearEvents()`、`attachEvents()` |
| `AggregateBase<ID>` | 内置 `registerEvent()` 注册处理器、`raiseEvent()` 触发并存储事件、`applyEvent()` 应用事件到状态 |

```java
// 聚合根中注册事件处理器
registerEvent(OrderCreatedEvent.class, event -> {
    this.status = OrderStatus.CREATED;
});

// 触发领域事件
raiseEvent(new OrderCreatedEvent(orderId, amount));

// 获取所有未提交事件
List<DomainEvent> events = order.getEvents();
```

---

### 事件系统 (Event System)

事件分为 **领域事件**（`DomainEvent`）和 **应用事件**（`ApplicationEvent`），均继承自 `Event` 接口，扩展自消息总线的 `Multicast` 契约。

| 类 / 接口 | 描述 |
|-----------|------|
| `Event` | 事件基类接口 — 定义 `sequence`、`eventIntent`、`originatorType`、`originatorId` 元数据 |
| `EventBase` | 事件抽象基类 — 基于 `HashMap` 的属性存储，默认从类名推断 `eventIntent` |
| `DomainEvent` | 领域事件接口 — 扩展 `Event`，可 `attach()` 到聚合根，生成 `EventAggregate` |
| `DomainEventBase` | 领域事件基类 — 实现 `attach()` 自动设置来源信息，支持载体负载 |
| `ApplicationEvent` | 应用事件标记接口 — 用于应用层级事件 |
| `ApplicationEventBase` | 应用事件基类 — 空实现，仅标记 |
| `EventAggregate` | 事件元数据聚合 — 包含 `eventId`、`timestamp`、`typeName`、`eventIntent`、`originatorType`、`originatorId`、`eventSequence`、`eventPayload` |

#### 事件源追踪

每个事件携带来源信息：
- `originatorType` — 事件发起者的类型名
- `originatorId` — 事件发起者的唯一标识
- `eventIntent` — 事件意图（默认为类名）
- `sequence` — 事件序号（默认使用时间戳）

---

### 命令系统 (Command System)

| 类 / 接口 | 描述 |
|-----------|------|
| `Command` | 命令接口 — 扩展 `Unicast`（消息总线单播契约），用于点对点命令分发 |
| `CommandBase` | 命令抽象基类 — 基于 `HashMap` 的属性容器，支持类型安全的属性存取 |

命令通过消息总线的 `Unicast` 机制发送到指定的命令处理器，实现 CQRS 的命令侧。

---

### 用例层 (Use Case Layer)

| 类 / 接口 | 描述 |
|-----------|------|
| `UseCase<I, O>` | 用例接口 — 定义 `execute(I input): O`，表示单个业务操作 |
| `UseCaseSuccess<O>` | 成功输出端口 — `success(O output)` |
| `UseCaseFailure` | 失败输出端口 — `error(Throwable throwable)` |
| `UseCasePresenter<O>` | 用例展示器 — 同时实现 `UseCaseSuccess<O>` 和 `UseCaseFailure`，基于 `SubmissionPublisher` 的响应式订阅 |

#### 用例展示器模式

```java
var presenter = new UseCasePresenter<OrderResult>();

// 订阅成功和失败事件
presenter.subscribe(
    result -> System.out.println("Success: " + result),
    error  -> System.err.println("Error: " + error.getMessage())
);

// 用例执行完成后通知
presenter.success(orderResult);   // 或 presenter.error(exception);
```

---

### 应用服务 (Application Service)

| 类 / 接口 | 描述 |
|-----------|------|
| `ApplicationService` | 应用服务标记接口 |
| `BaseApplicationService` | 应用服务基类 — 内置 `ServiceProvider` 和 `Bus` 引用，提供 `getService()` 和 `getUser()` 便捷方法 |

```java
public abstract class BaseApplicationService implements ApplicationService {
    protected final ServiceProvider provider;
    protected final Bus bus;

    protected <T> Optional<T> getService(Class<T> type) { ... }
    protected UserPrincipal getUser() { ... }
}
```

应用服务负责编排领域对象，协调用例执行，并通过消息总线发送命令和事件。

---

### 审计 (Auditing)

| 类 / 接口 | 描述 |
|-----------|------|
| `@Audited` | 审计注解 — 可用于类型、字段、方法级别，标记需要审计的组件 |
| `AuditRecord<ID>` | 审计记录实体 — 记录 `entityName`、`entityId`、`action`、`timestamp`、`comment`、`userId`、`userName` |
| `AuditStore` | 审计存储接口 — `save(T record)` 通用保存契约 |

---

## 包结构

```
com.euonia
├── domain/
│   ├── Entity.java               # 实体接口
│   ├── EntityBase.java           # 实体抽象基类
│   ├── Aggregate.java            # 聚合根接口
│   ├── AggregateBase.java        # 聚合根抽象基类（含事件管理）
│   ├── ValueObject.java          # 值对象基类
│   ├── HasDomainEvents.java      # 领域事件契约
│   ├── event/
│   │   ├── Event.java            # 事件接口
│   │   ├── EventBase.java        # 事件基类
│   │   ├── DomainEvent.java      # 领域事件接口
│   │   ├── DomainEventBase.java  # 领域事件基类
│   │   ├── ApplicationEvent.java # 应用事件接口
│   │   ├── ApplicationEventBase.java # 应用事件基类
│   │   └── EventAggregate.java   # 事件元数据聚合
│   ├── command/
│   │   ├── Command.java          # 命令接口
│   │   └── CommandBase.java      # 命令基类
│   └── auditing/
│       ├── Audited.java          # 审计注解
│       ├── AuditRecord.java      # 审计记录
│       └── AuditStore.java       # 审计存储接口
├── application/
│   ├── ApplicationService.java   # 应用服务接口
│   └── BaseApplicationService.java # 应用服务基类
└── usecase/
    ├── UseCase.java              # 用例接口
    ├── UseCaseSuccess.java       # 成功输出端口
    ├── UseCaseFailure.java       # 失败输出端口
    └── UseCasePresenter.java     # 用例展示器
```

---

## 设计模式

| 模式 | 应用 |
|------|------|
| **实体-值对象** | `Entity` vs `ValueObject` — 标识等价 vs 属性等价 |
| **聚合模式** | `AggregateBase` 管理子实体一致性和领域事件 |
| **事件溯源** | `HasDomainEvents` + `raiseEvent` / `applyEvent` |
| **CQRS（命令侧）** | `Command` 通过 `Unicast` 总线分发 |
| **端口-适配器** | `UseCaseSuccess` / `UseCaseFailure` 输出端口 |
| **观察者模式** | `UseCasePresenter` 使用 `SubmissionPublisher` 响应式订阅 |
| **模板方法** | `DomainEventBase.attach()` 自动设置来源信息 |
| **策略模式** | `AuditStore` 可替换持久化策略 |

---

## 依赖关系

```
ddd
 ├── core           (ID 生成、反射工具)
 ├── pipeline       (管道/过滤器抽象)
 ├── unit-of-work   (工作单元事务管理)
 └── bus-core       (消息总线 — Command 的 Unicast、Event 的 Multicast)
```
