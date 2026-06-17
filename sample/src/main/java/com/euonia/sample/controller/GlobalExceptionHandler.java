package com.euonia.sample.controller;

import com.euonia.osba.rules.RuleCheckException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuleCheckException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleRuleCheckException(RuleCheckException e) {
        var result = new HashMap<String, Object>();
        result.put("message", e.getMessage());
        result.put("errors", e.getErrors());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(result);
    }
}
