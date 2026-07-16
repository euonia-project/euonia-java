package com.euonia.security;

/**
 * OpenID Connect Core 1.0 规范中定义的用户声明（Claim）类型常量。
 * 参考：<a href=
 * "https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims">标准声明</a>
 *
 * @author damon(zhaorong@outlook.com)
 */
public class UserClaimTypes {
    /**
     * 主题（Subject） - 用户唯一标识符
     */
    public static final String SUBJECT = "sub";
    /**
     * 全名
     */
    public static final String NAME = "name";
    /**
     * 名
     */
    public static final String GIVEN_NAME = "given_name";
    /**
     * 姓
     */
    public static final String FAMILY_NAME = "family_name";
    /**
     * 中间名
     */
    public static final String MIDDLE_NAME = "middle_name";
    /**
     * 昵称
     */
    public static final String NICKNAME = "nickname";
    /**
     * 首选用户名
     */
    public static final String PREFERRED_USER_NAME = "preferred_username";
    /**
     * 个人资料页面 URL
     */
    public static final String PROFILE = "profile";
    /**
     * 头像图片 URL
     */
    public static final String PICTURE = "picture";
    /**
     * 个人网站 URL
     */
    public static final String WEBSITE = "website";
    /**
     * 电子邮件
     */
    public static final String EMAIL = "email";
    /**
     * 邮箱是否已验证
     */
    public static final String EMAIL_VERIFIED = "email_verified";
    /**
     * 性别
     */
    public static final String GENDER = "gender";
    /**
     * 生日
     */
    public static final String BIRTHDATE = "birthdate";
    /**
     * 时区信息
     */
    public static final String ZONE_INFO = "zoneinfo";
    /**
     * 区域/语言设置
     */
    public static final String LOCALE = "locale";
    /**
     * 电话号码
     */
    public static final String PHONE_NUMBER = "phone_number";
    /**
     * 电话号码是否已验证
     */
    public static final String PHONE_NUMBER_VERIFIED = "phone_number_verified";
    /**
     * 地址
     */
    public static final String ADDRESS = "address";
    /**
     * 目标受众（Audience）
     */
    public static final String AUDIENCE = "aud";
    /**
     * 签发者（Issuer）
     */
    public static final String ISSUER = "iss";
    /**
     * 生效时间（Not Before）
     */
    public static final String NOT_BEFORE = "nbf";
    /**
     * 过期时间（Expiration）
     */
    public static final String EXPIRATION = "exp";
    /**
     * 签发时间（Issued At）
     */
    public static final String ISSUED_AT = "iat";
    /**
     * 更新时间
     */
    public static final String UPDATED_AT = "updated_at";
    /**
     * 认证方式（Authentication Method Reference）
     */
    public static final String AUTHENTICATION_METHOD = "amr";
    /**
     * 会话 ID
     */
    public static final String SESSION_ID = "sid";
    /**
     * 认证上下文类引用（ACR）
     */
    public static final String AUTHENTICATION_CONTEXT_CLASS_REFERENCE = "acr";
    /**
     * 认证时间
     */
    public static final String AUTHENTICATION_TIME = "auth_time";
    /**
     * 授权方（Authorized Party）
     */
    public static final String AUTHORIZED_PARTY = "azp";
    /**
     * Access Token 哈希
     */
    public static final String ACCESS_TOKEN_HASH = "at_hash";
    /**
     * 授权码哈希
     */
    public static final String AUTHORIZATION_CODE_HASH = "c_hash";
    /**
     * State 哈希
     */
    public static final String STATE_HASH = "s_hash";
    /**
     * 一次性随机数（Nonce）
     */
    public static final String NONCE = "nonce";
    /**
     * JWT ID
     */
    public static final String JWT_ID = "jti";
    /**
     * 事件
     */
    public static final String EVENTS = "events";
    /**
     * 客户端 ID
     */
    public static final String CLIENT_ID = "client_id";
    /**
     * 授权范围（Scope）
     */
    public static final String SCOPE = "scope";
    /**
     * 执行者（Actor）
     */
    public static final String ACTOR = "act";
    /**
     * 可代理执行者
     */
    public static final String MAY_ACT = "may_act";
    /**
     * 唯一标识符
     */
    public static final String ID = "id";
    /**
     * 身份提供者
     */
    public static final String IDENTITY_PROVIDER = "idp";
    /**
     * 角色
     */
    public static final String ROLE = "role";
    /**
     * 引用令牌 ID
     */
    public static final String REFERENCE_TOKEN_ID = "reference_token_id";
    /**
     * 确认声明（Confirmation）
     */
    public static final String CONFIRMATION = "cnf";
    /**
     * 授权码
     */
    public static final String CODE = "code";
    /**
     * 授权类型
     */
    public static final String GRANT_TYPE = "grant_type";
    /**
     * 租户
     */
    public static final String TENANT = "tenant";
    /**
     * 协议方案
     */
    public static final String SCHEME = "scheme";
}
