package com.euonia.spring;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link BeanScope} 常量定义。
 */
@SuppressWarnings("unused")
@DisplayName("BeanScope")
class BeanScopeTest {

    @Test
    @DisplayName("should define APPLICATION scope constant")
    void shouldDefineApplicationScope() {
        assertThat(BeanScope.APPLICATION).isEqualTo("application");
    }

    @Test
    @DisplayName("should define PROTOTYPE scope constant")
    void shouldDefinePrototypeScope() {
        assertThat(BeanScope.PROTOTYPE).isEqualTo("prototype");
    }

    @Test
    @DisplayName("should define REQUEST scope constant")
    void shouldDefineRequestScope() {
        assertThat(BeanScope.REQUEST).isEqualTo("request");
    }

    @Test
    @DisplayName("should define SESSION scope constant")
    void shouldDefineSessionScope() {
        assertThat(BeanScope.SESSION).isEqualTo("session");
    }

    @Test
    @DisplayName("should define SINGLETON scope constant")
    void shouldDefineSingletonScope() {
        assertThat(BeanScope.SINGLETON).isEqualTo("singleton");
    }

    @Test
    @DisplayName("should define WEB_SOCKET scope constant")
    void shouldDefineWebSocketScope() {
        assertThat(BeanScope.WEB_SOCKET).isEqualTo("websocket");
    }
}
