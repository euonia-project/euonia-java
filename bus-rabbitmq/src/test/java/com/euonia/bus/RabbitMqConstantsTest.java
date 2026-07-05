package com.euonia.bus;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link RabbitMqConstants} 常量定义。
 */
@SuppressWarnings("unused")
@DisplayName("RabbitMqConstants")
class RabbitMqConstantsTest {

    @Test
    @DisplayName("should define default exchange name prefix")
    void shouldDefineDefaultExchangeNamePrefix() throws Exception {
        var value = getStaticField("DEFAULT_EXCHANGE_NAME_PREFIX");

        assertThat(value).isEqualTo("$nerosoft.euonia.exchange");
    }

    @Test
    @DisplayName("should define default queue name prefix")
    void shouldDefineDefaultQueueNamePrefix() throws Exception {
        var value = getStaticField("DEFAULT_QUEUE_NAME_PREFIX");

        assertThat(value).isEqualTo("$nerosoft.euonia.queue");
    }

    @Test
    @DisplayName("should define default rpc queue name prefix")
    void shouldDefineDefaultRpcQueueNamePrefix() throws Exception {
        var value = getStaticField("DEFAULT_RPC_QUEUE_NAME_PREFIX");

        assertThat(value).isEqualTo("$nerosoft.euonia.request");
    }

    @Test
    @DisplayName("should define default dlx exchange prefix")
    void shouldDefineDefaultDlxExchangePrefix() throws Exception {
        var value = getStaticField("DEFAULT_DLX_EXCHANGE_PREFIX");

        assertThat(value).isEqualTo("$nerosoft.euonia.dlx");
    }

    @Test
    @DisplayName("should define default dlq queue name prefix")
    void shouldDefineDefaultDlqQueueNamePrefix() throws Exception {
        var value = getStaticField("DEFAULT_DLQ_QUEUE_NAME_PREFIX");

        assertThat(value).isEqualTo("$nerosoft.euonia.dlq");
    }

    @Test
    @DisplayName("should define default dlx routing key")
    void shouldDefineDefaultDlxRoutingKey() throws Exception {
        var value = getStaticField("DEFAULT_DLX_ROUTING_KEY");

        assertThat(value).isEqualTo("dead-letter");
    }

    @Test
    @DisplayName("all fields should be static final")
    void allFieldsShouldBeStaticFinal() {
        for (Field field : RabbitMqConstants.class.getDeclaredFields()) {
            int mod = field.getModifiers();
            assertThat(Modifier.isStatic(mod)).as("field %s should be static", field.getName()).isTrue();
            assertThat(Modifier.isFinal(mod)).as("field %s should be final", field.getName()).isTrue();
        }
    }

    private static Object getStaticField(String name) throws Exception {
        Field field = RabbitMqConstants.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }
}
