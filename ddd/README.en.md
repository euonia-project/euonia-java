# DDD Module

Domain-Driven Design tactical toolbox for the Euonia framework. Provides core DDD building blocks — Entity, Aggregate, ValueObject, Command, DomainEvent, UseCase, and ApplicationService — enabling developers to build highly cohesive, loosely coupled business systems centered around the domain model.

This module depends on `core`, `bus-core`, and `slf4j-api`, and is deeply integrated with the message bus to support **command dispatching**, **event-driven**, and **use-case orchestration** interaction patterns.

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                    DDD Module Architecture                       │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────── Domain Layer ───────────────────────────┐ │
│  │                                                              │ │
│  │   Entity<ID>          Aggregate<ID>        ValueObject<T>   │ │
│  │      ↑                     ↑                    ↑            │ │
│  │   EntityBase<ID>     AggregateBase<ID>    (compare, equals)  │ │
│  │   (id management)    (event management, handlers)             │ │
│  │                                                              │ │
│  ├──────────────────── Event System ───────────────────────────┤ │
│  │                                                              │ │
│  │   Event ◄── DomainEvent ◄── ApplicationEvent               │ │
│  │    ↑            ↑               ↑                            │ │
│  │   EventBase  DomainEventBase  ApplicationEventBase          │ │
│  │                                                              │ │
│  │   EventAggregate: event metadata aggregate                    │ │
│  │   HasDomainEvents: aggregate root event contract             │ │
│  │                                                              │ │
│  ├──────────────────── Command System ─────────────────────────┤ │
│  │                                                              │ │
│  │   Command ◄── CommandBase                                   │ │
│  │   (extends Unicast)  (property container)                    │ │
│  │                                                              │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌──────────────────── Application Layer ──────────────────────┐ │
│  │                                                              │ │
│  │   ApplicationService ◄── BaseApplicationService             │ │
│  │                        (LazyServiceProvider, Bus, Logger)    │ │
│  │                                                              │ │
│  ├──────────────────── Use Case Layer ─────────────────────────┤ │
│  │                                                              │ │
│  │   UseCase<I,O>           UseCaseOutputSuccess<O>            │ │
│  │   UseCasePresenter<O>    UseCaseOutputFailure               │ │
│  │   UseCaseExecutor        (CompletionStage async execution)   │ │
│  │                                                              │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌──────────────────── Auditing ───────────────────────────────┤ │
│  │   @Audited            AuditRecord<ID>                       │ │
│  │   AuditStore           (entity audit log)                    │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## Core Concepts

### Domain Building Blocks

| Class / Interface | Description |
|-------------------|-------------|
| `Entity<ID>` | Entity interface — defines domain objects with unique identity via `getId()` and `getKeys()` |
| `EntityBase<ID>` | Abstract entity base — provides default `id` property implementation |
| `Aggregate<ID>` | Aggregate root marker interface — extends `Entity<ID>`, designates the root of an aggregate |
| `AggregateBase<ID>` | Aggregate root base class — extends `EntityBase`, built-in domain event management (register, raise, apply, clear) and `HasDomainEvents` contract implementation |
| `ValueObject<T>` | Value object base class — field-based `equals`, `hashCode`, and `compareTo` implementations for immutable value types |

#### Entity

```java
public interface Entity<ID extends Comparable<ID>> {
    ID getId();
    void setId(ID id);
    default Object[] getKeys() { return new Object[]{getId()}; }
}
```

Entities are domain objects with unique identity, distinguished by their identifier rather than their attributes.

#### Aggregate Root

```java
public interface Aggregate<ID extends Comparable<ID>> extends Entity<ID> { }
```

An aggregate is a cluster of related domain objects treated as a single unit. The aggregate root serves as the entry point for access and modification, enforces internal consistency, and supports event sourcing through the `HasDomainEvents` contract.

#### Value Object

```java
public class ValueObject<T extends ValueObject<T>> implements Comparable<T> {
    // compareTo and equals based on all declared fields
}
```

Value objects have no independent identity — equality is determined by their attribute values. All fields participate in comparison and hashing.

---

### Aggregate Event Management

| Interface / Class | Description |
|-------------------|-------------|
| `HasDomainEvents` | Aggregate root event contract — defines `getEvents()`, `raiseEvent()`, `applyEvent()`, `clearEvents()`, `attachEvents()` |
| `AggregateBase<ID>` | Built-in `registerEvent()` for handler registration, `raiseEvent()` to trigger and store events, `applyEvent()` to update state |

```java
// Register event handler in aggregate root
registerEvent(OrderCreatedEvent.class, event -> {
    this.status = OrderStatus.CREATED;
});

// Raise a domain event
raiseEvent(new OrderCreatedEvent(orderId, amount));

// Retrieve uncommitted events
List<DomainEvent> events = order.getEvents();
```

---

### Event System

Events are categorized into **Domain Events** (`DomainEvent`) and **Application Events** (`ApplicationEvent`), both extending the `Event` interface and the message bus `Multicast` contract.

| Class / Interface | Description |
|-------------------|-------------|
| `Event` | Event base interface — defines `sequence`, `eventIntent`, `originatorType`, `originatorId` metadata |
| `EventBase` | Abstract event base — `HashMap`-based property storage, auto-infers `eventIntent` from class name |
| `DomainEvent` | Domain event interface — extends `Event`, can `attach()` to an aggregate root, produces `EventAggregate` |
| `DomainEventBase` | Domain event base class — implements `attach()` with automatic originator tracking and payload support |
| `ApplicationEvent` | Application event marker interface — for application-level events |
| `ApplicationEventBase` | Application event base class — marker implementation |
| `EventAggregate` | Event metadata aggregate — contains `eventId`, `timestamp`, `typeName`, `eventIntent`, `originatorType`, `originatorId`, `eventSequence`, `eventPayload` |

#### Event Source Tracking

Each event carries origin information:
- `originatorType` — the type name of the event originator
- `originatorId` — the unique identifier of the event originator
- `eventIntent` — the intent of the event (defaults to class name)
- `sequence` — event sequence number (defaults to epoch millis)

---

### Command System

| Class / Interface | Description |
|-------------------|-------------|
| `Command` | Command interface — extends `Unicast` (message bus unicast contract) for point-to-point command dispatch |
| `CommandBase` | Abstract command base class — `HashMap`-based property container with type-safe access |

Commands are dispatched through the message bus's `Unicast` mechanism to designated command handlers, implementing the command side of CQRS.

---

### Use Case Layer

| Class / Interface | Description |
|-------------------|-------------|
| `UseCase<I, O>` | Use case interface — defines `executeAsync(I input): CompletionStage<O>` for a single asynchronous business operation |
| `UntypedUseCase` | Untyped root use case contract used by the executor |
| `NonOutputUseCase<I>` | Use case without business output — `executeVoidAsync(I input)` |
| `NonInputUseCase<O>` | Use case without input — `executeAsync()` |
| `ParameterlessUseCase` | Use case without input or output — `executeVoidAsync()` |
| `UseCaseInput` / `UseCaseOutput` | Marker interfaces for use case input/output |
| `EmptyUseCaseInput` / `EmptyUseCaseOutput` | Shared empty input/output instances |
| `UseCaseOutputSuccess<O>` | Success output port — `ok(O output)` |
| `UseCaseOutputFailure` | Failure output port — `error(Throwable throwable)` |
| `UseCasePresenter<O>` | Presenter contract — supports success, failure and cancellation listeners |
| `DefaultUseCasePresenter<O>` | Default presenter — stores output and dispatches events; `close()` clears listeners |
| `UseCaseExecutor` / `DefaultUseCaseExecutor` | Executes use cases and dispatches results to presenters; supports container resolution |

#### Use Case and Presenter

```java
public class GreetUseCase implements UseCase<String, String> {
    @Override
    public CompletionStage<String> executeAsync(String name) {
        return CompletableFuture.completedFuture("Hello, " + name + "!");
    }
}

var presenter = new DefaultUseCasePresenter<String>()
    .onSucceed(result -> System.out.println("Success: " + result))
    .onFailed(error -> System.err.println("Error: " + error.getMessage()));

var executor = new DefaultUseCaseExecutor(serviceProvider);
executor.executeAsync(new GreetUseCase(), "Alice", presenter);
```

---

### Application Service

| Class / Interface | Description |
|-------------------|-------------|
| `ApplicationService` | Application service marker — extends `HasLazyServiceProvider` |
| `BaseApplicationService` | Application service base class — lazily resolves `Bus`, `UserPrincipal`, logger and `UseCaseExecutor` through `LazyServiceProvider`, and exposes the request context via `RequestContextAccessor` |

```java
public abstract class BaseApplicationService implements ApplicationService {
    public ServiceProvider getLazyServiceProvider() { ... }
    public void setLazyServiceProvider(ServiceProvider provider) { ... }

    protected <T> Optional<T> getService(Class<T> type) { ... }
    protected Bus getBus() { ... }
    protected UserPrincipal getUser() { ... }
    protected RequestContext getRequestContext() { ... }
    protected Logger getLogger() { ... }
    protected UseCaseExecutor getExecutor() { ... }
}
```

The Spring integration module injects `LazyServiceProvider` through
`ApplicationServiceBeanPostProcessor` and registers a `UseCaseExecutor`. Application
services only need to extend `BaseApplicationService` to use `getBus()`, `getUser()`
and `getExecutor()`.

---

### Auditing

| Class / Interface | Description |
|-------------------|-------------|
| `@Audited` | Auditing annotation — applicable to types, fields, and methods; marks components for auditing |
| `AuditRecord<ID>` | Audit record entity — records `entityName`, `entityId`, `action`, `timestamp`, `comment`, `userId`, `userName` |
| `AuditStore` | Audit storage interface — generic `save(T record)` contract |

---

## Package Structure

```
com.euonia
├── domain/
│   ├── Entity.java               # Entity interface
│   ├── EntityBase.java           # Abstract entity base
│   ├── Aggregate.java            # Aggregate root interface
│   ├── AggregateBase.java        # Aggregate root base (with event management)
│   ├── ValueObject.java          # Value object base
│   ├── HasDomainEvents.java      # Domain event contract
│   ├── event/
│   │   ├── Event.java            # Event interface
│   │   ├── EventBase.java        # Event base class
│   │   ├── DomainEvent.java      # Domain event interface
│   │   ├── DomainEventBase.java  # Domain event base class
│   │   ├── ApplicationEvent.java # Application event interface
│   │   ├── ApplicationEventBase.java # Application event base class
│   │   └── EventAggregate.java   # Event metadata aggregate
│   ├── command/
│   │   ├── Command.java          # Command interface
│   │   └── CommandBase.java      # Command base class
│   └── auditing/
│       ├── Audited.java          # Auditing annotation
│       ├── AuditRecord.java      # Audit record entity
│       └── AuditStore.java       # Audit storage interface
├── application/
│   ├── ApplicationService.java   # Application service interface
│   └── BaseApplicationService.java # Application service base class
└── usecase/
    ├── UseCase.java              # Use case interface
    ├── UntypedUseCase.java       # Untyped root use case contract
    ├── NonOutputUseCase.java     # Use case without output
    ├── NonInputUseCase.java      # Use case without input
    ├── ParameterlessUseCase.java # Use case without input or output
    ├── UseCaseOutputSuccess.java # Success output port
    ├── UseCaseOutputFailure.java # Failure output port
    ├── UseCasePresenter.java     # Presenter contract
    ├── DefaultUseCasePresenter.java # Default presenter
    ├── UseCaseExecutor.java      # Use case executor contract
    └── DefaultUseCaseExecutor.java # Default use case executor
```

---

## Design Patterns

| Pattern | Application |
|---------|-------------|
| **Entity-Value Object** | `Entity` vs `ValueObject` — identity equality vs attribute equality |
| **Aggregate Pattern** | `AggregateBase` manages child entity consistency and domain events |
| **Event Sourcing** | `HasDomainEvents` + `raiseEvent` / `applyEvent` |
| **CQRS (Command Side)** | `Command` dispatched via `Unicast` bus |
| **Ports & Adapters** | `UseCaseOutputSuccess` / `UseCaseOutputFailure` output ports |
| **Observer Pattern** | `UseCasePresenter` listeners with result dispatching in `DefaultUseCaseExecutor` |
| **Template Method** | `DomainEventBase.attach()` automatically sets originator info |
| **Strategy Pattern** | `AuditStore` supports pluggable persistence strategies |

---

## Dependencies

```
ddd
 ├── core           (ID generation, reflection utilities)
 ├── bus-core       (message bus — Bus convenience access in application services)
 └── slf4j-api      (application service logging)
```
