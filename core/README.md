# Euonia Core 模块

Euonia 框架的核心基础设施模块，提供 **ID 生成**、**对象池**、**元组数据结构**、**注解式校验**、**HTTP 异常层次**、**请求上下文**、**安全主体**、**反射工具**、**类型转换**、**DI 抽象** 等基础能力。

此模块 **零外部依赖**（仅 `junit-jupiter` 用于测试），是所有其他 Euonia 模块的基石。

---

## 包结构

```
com.euonia
├── core/           # ID生成、对象池、单例容器、优先队列
├── tuple/          # 不可变元组（1..10 元组，基于 Record）
├── annotation/     # 注解驱动的校验系统
├── security/       # 安全异常、声明类型、用户主体
├── http/           # 请求上下文、HTTP 状态异常体系
├── utility/        # 字符串工具
└── reflection/     # DI 抽象、类扫描、类型转换、泛型捕获
```

---

## 模块架构

```
┌─────────────────────────────────────────────────────────────┐
│                     euonia-core                             │
├─────────────────────────────────────────────────────────────┤
│  com.euonia.core          com.euonia.tuple                 │
│  ┌──────────────┐        ┌──────────────────────┐          │
│  │ ObjectId     │        │ Tuple (接口)          │          │
│  │ SnowflakeId  │        │ Solo ... Decet        │          │
│  │ GuidGenerator│        │ (Record ×10)          │          │
│  │ ULID         │        └──────────────────────┘          │
│  │ ShortUniqueId│                                           │
│  │ RandomId     │        com.euonia.annotation             │
│  │ Singleton    │        ┌──────────────────────┐          │
│  │ PriorityQueue│        │ @Validation          │          │
│  │ Pair         │        │ Validator<A>         │          │
│  │ ObjectPool   │        │ @Required            │          │
│  └──────────────┘        │ RequiredValidator    │          │
│                           └──────────────────────┘          │
│  com.euonia.security                                       │
│  ┌───────────────────────────┐                             │
│  │ UserPrincipal             │                             │
│  │ UserClaimTypes            │                             │
│  │ AccountException (抽象)    │                             │
│  │ CredentialException (抽象) │                             │
│  │ AuthenticationException   │                             │
│  │ UnauthorizedAccessException│                            │
│  └───────────────────────────┘                             │
│                                                            │
│  com.euonia.http              com.euonia.reflection        │
│  ┌──────────────────────┐    ┌──────────────────────┐     │
│  │ HttpStatusException  │    │ ServiceProvider      │     │
│  │ (400..504 子类 ×12)  │    │ SimpleServiceProvider│     │
│  │ RequestContext       │    │ DelegateServiceProvider│   │
│  │ RequestContextAccessor│   │ ClassScanner         │     │
│  │ DefaultRequestContext │    │ TypeHelper           │     │
│  │   Accessor           │    │ GenericType<T>       │     │
│  │ @ResponseHttpStatusCode│  │ @DisplayName         │     │
│  └──────────────────────┘    └──────────────────────┘     │
│                                                            │
│  com.euonia.utility                                        │
│  ┌──────────────────┐                                      │
│  │ StringUtility    │                                      │
│  └──────────────────┘                                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 一、ID 生成（`com.euonia.core`）

### 统一 ID 门面：`ObjectId`（不可变）

`ObjectId` 包装 `long | String | UUID | Integer`，提供统一构造 + 静态工厂模式：

```java
// 五种生成策略
ObjectId id1 = ObjectId.snowflake();       // Snowflake 64-bit
ObjectId id2 = ObjectId.guid();            // UUID (默认)
ObjectId id3 = ObjectId.guid(GuidType.SEQUENTIAL_AS_STRING); // .NET 兼容顺序 GUID
ObjectId id4 = ObjectId.random();          // 随机字符串
ObjectId id5 = ObjectId.ulid();            // ULID 排序友好

// 获取原始值
long l = id1.getValue(Long.class);         // 类型安全取值
UUID u = id3.getValue(UUID.class);
```

| 方法 | 说明 |
|------|------|
| `ObjectId.snowflake()` | Snowflake 64-bit → `ObjectId(long)` |
| `ObjectId.guid()` / `ObjectId.guid(GuidType)` | UUID → `ObjectId(UUID)` |
| `ObjectId.random()` | 随机字符串 → `ObjectId(String)` |
| `ObjectId.ulid()` | ULID 字符串 → `ObjectId(String)` |
| `ObjectId(long|String|UUID|Integer)` | 直接构造 |
| `getValue(Class<T>)` | 类型安全取值；`Long`↔`Integer` 自动拆箱 |

### SnowflakeId — 分布式 64-bit ID

Twitter Snowflake 变体，自定义纪元 **2021-01-01**，无需节点间协调：

```
┌──────────────────────────────────────────────────────────┐
│ 1-bit │ 41-bit Timestamp │ 5-bit DC │ 5-bit Worker │ 12-bit Seq │
└──────────────────────────────────────────────────────────┘
```

| 参数 | 位数 | 最大值 |
|------|------|--------|
| 时间戳（距纪元 ms） | 41 | ~69 年 |
| 数据中心 ID | 5 | 32 |
| 工作节点 ID | 5 | 32 |
| 序列号 | 12 | 4096/ms |

```java
SnowflakeId id = SnowflakeId.getInstance(1, 1);  // workerId, datacenterId
long uniqueId = id.nextId();
```

**特性：**
- 时钟回拨检测（抛出异常）
- 同毫秒序列号溢出→等待下一毫秒
- 默认 `getInstance()` = worker 0 / datacenter 0

### GuidGenerator & GuidType

兼容 .NET 的顺序 GUID 布局的 UUID 生成器：

| GuidType | 说明 |
|----------|------|
| `DEFAULT` | 标准 UUID |
| `SEQUENTIAL_AS_STRING` | 字符串排序友好的顺序 GUID |
| `SEQUENTIAL_AS_BINARY` | 二进制排序友好的顺序 GUID |
| `SEQUENTIAL_AT_END` | 尾部顺序 GUID |
| `EMPTY` | 全零 UUID |

### ULID — 字典序可排序 ID

Crockford Base32 编码、时间戳前缀、毫秒级单调递增：

```java
String ulid = ULID.generate();  // 如 "01ARZ3NDEKTSV4RRFFQ69G5FAV"
```

### ShortUniqueId — 短 ID（Hashids 风格）

将整数编码为短字符串，支持自定义盐值、字母表、分隔符：

```java
ShortUniqueId suid = ShortUniqueId.getDefault();
String hash = suid.encode(12345);       // "j0gW"
int[] decoded = suid.decode(hash);      // [12345]
String hexHash = suid.encodeHex("A1B2C3D4E5F6");
```

### RandomId（内部）

基于种子的随机字符串生成器，由 `ObjectId.random()` 使用。

---

## 二、对象池（`com.euonia.core`）

基于策略模式的通用对象池，支持容量控制、对象验证与销毁：

### 核心接口

```java
// 池策略（生命周期定义）
public interface ObjectPoolPolicy<T> {
    T create();                              // 创建新对象
    boolean validate(T obj);                 // 归还前校验
    void destroy(T obj);                     // 销毁对象
    OversizeBehavior oversizeBehavior();     // 超量策略
}
```

### OversizeBehavior 枚举

| 行为 | 说明 |
|------|------|
| `THROW_EXCEPTION` | 池满时抛出异常 |
| `RETURN_NULL` | 池满时返回 null |
| `CREATE_NEW` | 池满时创建临时对象（不被池管理） |
| `WAIT_FOR_AVAILABLE` | 池满时阻塞等待（当前实现与 RETURN_NULL 相同） |

### DefaultObjectPool<T>

`synchronized` 线程安全实现：

```java
ObjectPoolPolicy<MyObject> policy = new ObjectPoolPolicy<>() {
    public MyObject create() { return new MyObject(); }
    public boolean validate(MyObject o) { return true; }
    public void destroy(MyObject o) { o.close(); }
    public OversizeBehavior oversizeBehavior() { return OversizeBehavior.CREATE_NEW; }
};

ObjectPool<MyObject> pool = DefaultObjectPoolProvider.getInstance().create(policy, 10);
MyObject obj = pool.acquire();
// ... 使用 ...
pool.release(obj);
```

### DefaultObjectPoolProvider（单例）

`ConcurrentMap<Class<?>, ObjectPool<?>>` 缓存池实例，按策略类型去重。`remove()` 移除指定策略的池。

---

## 三、单例容器（`com.euonia.core`）

`Singleton` 提供线程安全的全局单例工厂：

```java
// 自动反射创建
MyService svc = Singleton.getInstance(MyService.class);

// 带 Supplier 创建
MyService svc2 = Singleton.get(MyService.class, () -> new MyService(config));
```

内部使用 `ConcurrentHashMap.computeIfAbsent` 确保原子性。

---

## 四、元组（`com.euonia.tuple`）

不可变、可序列化的强类型元组，全部基于 Java `Record`：

### Tuple 接口

| 方法 | 说明 |
|------|------|
| `size()` | 元组大小 |
| `value(int index)` | 按索引取值 |
| `values()` | 所有值的不可变 List |
| `contains(Object)` | 是否包含某值 |
| `containsAll(Tuple)` | 是否包含另一元组的所有值 |
| `containsAny(Tuple)` | 是否有交集 |
| `indexOf(Object)` / `lastIndexOf(Object)` | 查找位置 |
| `toArray()` / `toArray(X[])` | 转数组 |
| `equalsIgnoreOrder(Tuple)` | 忽略顺序的等值比较 |

### 元组类型

| Record | 泛型参数 | 静态工厂 |
|--------|---------|----------|
| `Solo<V>` | 1 | `of(v)` `from(T[])` `from(List)` |
| `Duet<V1,V2>` | 2 | `of(v1,v2)` `from(T[])` `empty()` |
| `Trio<V1..V3>` | 3 | 同上 |
| `Quartet<V1..V4>` | 4 | 同上 |
| `Quintet<V1..V5>` | 5 | 同上 |
| `Sextet<V1..V6>` | 6 | 同上 |
| `Septet<V1..V7>` | 7 | 同上 |
| `Octet<V1..V8>` | 8 | 同上 |
| `Nonet<V1..V9>` | 9 | 同上 |
| `Decet<V1..V10>` | 10 | 同上 |

```java
Duet<String, Integer> pair = Duet.of("key", 42);
Trio<String, Integer, Boolean> triple = Trio.of("a", 1, true);
```

---

## 五、注解驱动校验（`com.euonia.annotation`）

元注解式校验框架，支持自定义注解与校验逻辑分离：

### 架构

```
@Required ──→ @Validation(validator = RequiredValidator.class)
                     │
                     ▼
              Validator<Required>.validate(annotation, value)
                     │
                     ▼
              Duet<Boolean, String> (valid? , message)
```

### 核心类型

| 类型 | 作用 |
|------|------|
| `@Validation` | 元注解：将自定义注解绑定到 `Validator` 实现 |
| `Validator<A>` | 校验器契约 → `Duet<Boolean, String> validate(A annotation, Object value)` |
| `@Required` | 必填校验注解：`allowEmpty`, `message`, `annotation`（支持代理注解） |
| `RequiredValidator` | `Required` 校验实现：null 检查、空字符串检查（`allowEmpty=false`） |

### 扩展自定义校验

```java
@Retention(RUNTIME)
@Target(FIELD)
@Validation(validator = MyValidator.class)
public @interface MyConstraint {
    int min() default 0;
    String message() default "";
}
```

---

## 六、安全（`com.euonia.security`）

### 异常层次

```
RuntimeException
├── AuthenticationException          # 认证失败
├── UnauthorizedAccessException      # 授权拒绝
├── AccountException (抽象)           # 账户异常 + identity + 详情 Map
│   └── (可被子类扩展)
└── CredentialException (抽象)        # 凭据异常 + credential + 详情 Map
    └── (可被子类扩展)
```

`AccountException` 和 `CredentialException` 支持链式 `with(key, value)` 附加结构化详情。

### UserPrincipal — 主体包装器

封装 `javax.security.auth.Subject`，提供角色检查与守卫方法：

| 方法 | 说明 |
|------|------|
| `getName()` | 主体名称 |
| `getClaim(String)` | 按声明类型取值 |
| `isAuthenticated()` | 是否已认证 |
| `hasRole(String)` / `isInRoles(String...)` | 角色检查 |
| `ensureAuthenticated()` | 未认证则抛异常 |
| `ensureHasRole(String)` / `ensureInRoles(String...)` | 角色守卫 |

### UserClaimTypes — 声明类型常量

OpenID Connect 风格声明常量 + 扩展：

```java
UserClaimTypes.SUBJECT       // "sub"
UserClaimTypes.NAME          // "name"
UserClaimTypes.EMAIL         // "email"
UserClaimTypes.ROLE          // "role"
UserClaimTypes.TENANT_ID     // "tenant_id"
UserClaimTypes.SCHEME        // "scheme"
// ... 等
```

---

## 七、HTTP（`com.euonia.http`）

### HTTP 状态异常体系

`HttpStatusException` 承载 HTTP 状态码的运行时异常，子类固定状态码以支持全局异常处理映射。

```
RuntimeException
└── HttpStatusException(int statusCode, String message)
    ├── BadRequestException          → 400
    ├── ForbiddenException           → 403
    ├── ResourceNotFoundException    → 404
    ├── MethodNotAllowedException    → 405
    ├── RequestTimeoutException      → 408
    ├── ConflictException            → 409
    ├── UpgradeRequiredException     → 426
    ├── TooManyRequestsException     → 429
    ├── InternalServerErrorException → 500
    ├── BadGatewayException          → 502
    ├── ServiceUnavailableException  → 503
    └── GatewayTimeoutException      → 504
```

```java
throw new ResourceNotFoundException("用户不存在: " + userId);
```

### `@ResponseHttpStatusCode` — 类型级状态码标记

```java
@ResponseHttpStatusCode(404)
public class UserNotFoundException extends RuntimeException { }
```

### RequestContext — 请求上下文

| 字段 | 类型 | 说明 |
|------|------|------|
| `connectionId` | `String` | 连接 ID |
| `requestUri` | `String` | 请求 URI |
| `requestMethod` | `String` | HTTP 方法 |
| `remoteIpAddress` | `String` | 远程 IP |
| `remotePort` | `int` | 远程端口 |
| `webSocketRequest` | `boolean` | 是否 WebSocket |
| `requestHeaders` | `Map<String,String>` | 请求头 |
| `user` | `UserPrincipal` | 当前用户 |
| `traceIdentifier` | `String` | 追踪标识 |
| `requestId` | `String` | 请求 ID（来自 Authorization 头） |
| `authorization` | `String` | 快捷获取 Authorization 头 |

### RequestContextAccessor — 访问器

| 类型 | 说明 |
|------|------|
| `RequestContextAccessor`（接口） | `RequestContext getContext()` |
| `DefaultRequestContextAccessor` | `ThreadLocal<RequestContext>` 实现 |
| `DelegateRequestContextAccessor`（`@FunctionalInterface`） | 委托访问器 |

---

## 八、反射 & DI 抽象（`com.euonia.reflection`）

### ServiceProvider — 服务定位器契约

Euonia 内部 DI 抽象层，解耦业务代码与具体 DI 容器：

```java
public interface ServiceProvider {
    <T> Optional<T> getService(Class<T> type);
    <T> Optional<T> getService(Class<T> type, Class<?>... genericTypeArguments);
    <T> Optional<T> getService(Class<T> type, String serviceName);
    <T> T getRequiredService(Class<T> type);                           // null→异常
    <T> List<T> getServices(Class<T> type);
    <T> T createInstance(Class<T> type, Object... args);               // 反射创建
    <T> T getServiceOrCreate(Class<T> type, Object... args);           // 默认方法
}
```

### 两种内置实现

| 实现 | 说明 |
|------|------|
| `SimpleServiceProvider` | 内存 Map 注册 (`ConcurrentHashMap`)，手动 `register(type, instance)` |
| `DelegateServiceProvider` | 委托到外部 `Function<Class<?>, ?>` bean 工厂（与 Spring `ApplicationContext` 集成） |

### ClassScanner — 类路径扫描

```java
List<Class<?>> classes = ClassScanner.scan("com.euonia.core");
```

支持 `file:` 和 `jar:` 协议的包扫描，自动处理嵌套目录与 JAR 内路径。

### TypeHelper — 类型转换工具

| 方法 | 说明 |
|------|------|
| `coerceValue(Class<T>, Object)` | 值类型转换（primitives、枚举、日期/时间、集合/Map、UUID、char） |
| `boxIfPrimitive(Class<?>)` | 基本类型→包装类型 |
| `isPrimitiveNumber(Class<?>)` | 是否为数值基本类型 |
| `defaultPrimitiveValue(Class<?>)` | 基本类型默认值 |

内部使用 Jackson `ObjectMapper` 进行复杂对象转换（可选依赖，运行时反射调用）。

### GenericType<T> — 泛型捕获（类型令牌模式）

```java
// 运行时捕获 List<String> 的泛型参数
GenericType<List<String>> type = new GenericType<>() {};
Type actualType = type.getType();        // List<String>
```

### @DisplayName — 字段展示名注解

```java
@DisplayName("用户姓名")
private String userName;
```

---

## 九、其他工具

### PriorityQueue<E, K> — 带优先级的优先队列

`java.util.PriorityQueue` 的泛型封装，按 `Pair<K,E>` 的键排序：

```java
PriorityQueue<String, Integer> pq = new PriorityQueue<>();
pq.add("high", 1);
pq.add("low", 10);
String highest = pq.poll();  // "high"
```

### PriorityValueFinder — 优先级值查找器

按优先级递减遍历 supplier 队列，返回首个匹配 predicate 的值：

```java
PriorityValueFinder.find(queueConsumer, Predicate.isEqual(target), defaultValue);
```

### Pair<K extends Comparable<K>, V> — 键值记录

```java
Pair<String, Integer> p = Pair.of("score", 100);
```

### StringUtility — 字符串工具

| 方法 | 说明 |
|------|------|
| `capitalizeFirstLetter(String)` | 首字母大写 |
| `decapitalizeFirstLetter(String)` | 首字母小写 |
| `capitalizeFirstLetterWithUnderscore(String)` | `foo_bar` → `FooBar` |
| `collapse(String...)` / `collapse(Supplier<String>...)` | 返回第一个非空/非空白值 |

---

## 设计模式速览

| 模式 | 应用位置 |
|------|---------|
| **门面 (Facade)** | `ObjectId` — 统一五种 ID 生成策略 |
| **单例 (Singleton)** | `Singleton`、`DefaultObjectPoolProvider` |
| **策略 (Strategy)** | `ObjectPoolPolicy` 生命周期；`GuidType` 生成模式 |
| **工厂方法 (Factory Method)** | 所有 Record 的 `of()`/`from()`/`empty()` |
| **模板方法 (Template Method)** | `AccountException`/`CredentialException` 抽象基类 |
| **元注解 (Meta-Annotation)** | `@Validation` 绑定注解到校验器 |
| **类型令牌 (Type Token)** | `GenericType<T>` |
| **委托 (Delegate)** | `DelegateServiceProvider`、`DelegateRequestContextAccessor` |

---

## Maven

```xml
<dependency>
    <groupId>com.euonia</groupId>
    <artifactId>core</artifactId>
    <version>${euonia.version}</version>
</dependency>
```

**零外部运行时依赖**，仅测试依赖 JUnit Jupiter 5。

---

## 模块依赖关系

`core` 是框架最底层模块，被所有其他 Euonia 模块依赖：

```
sample, spring, osba, ddd, uow, bus-*, pipeline → core
```
