package com.euonia.osba.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.euonia.http.ResponseHttpStatusCode;

/**
 * 规则校验异常，当对象不满足保存条件时抛出。
 * <p>
 * 该异常携带按属性名分组的校验错误信息，并通过 {@link ResponseHttpStatusCode} 注解
 * 标记对应的 HTTP 状态码为 400（Bad Request）。
 *
 * @author damon(zhaorong@outlook)
 */
@ResponseHttpStatusCode(400)
public class RuleCheckException extends RuntimeException {

    /**
     * 按属性名分组的校验错误信息，key 为属性名，value 为该属性对应的错误消息列表。
     */
    private final Map<String, List<String>> errors = new HashMap<>();

    /**
     * 默认异常消息。
     */
    private final static String MESSAGE = "Object not valid for save.";

    /**
     * 使用指定的校验错误信息构造异常。
     *
     * @param errors 按属性名分组的校验错误映射，key 为属性名，value 为错误消息列表
     */
    public RuleCheckException(Map<String, List<String>> errors) {
        super(MESSAGE);
        this.errors.putAll(errors);
    }

    /**
     * 获取按属性名分组的校验错误信息。
     *
     * @return 不可修改的校验错误映射，key 为属性名，value 为错误消息列表
     */
    public Map<String, List<String>> getErrors() {
        return Map.copyOf(errors);
    }
}
