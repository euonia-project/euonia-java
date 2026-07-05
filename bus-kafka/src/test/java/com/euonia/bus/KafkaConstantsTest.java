package com.euonia.bus;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link KafkaConstants} 常量定义。
 */
@SuppressWarnings("unused")
@DisplayName("KafkaConstants")
class KafkaConstantsTest {

    @Test
    @DisplayName("should define correct default topic prefix")
    void shouldDefineDefaultTopicPrefix() throws Exception {
        var value = getStaticField("DEFAULT_TOPIC_PREFIX");

        assertThat(value).isEqualTo("$nerosoft.euonia.topic");
    }

    @Test
    @DisplayName("should define default partitions as 6")
    void shouldDefineDefaultPartitions() throws Exception {
        var value = getStaticField("DEFAULT_PARTITIONS");

        assertThat(value).isEqualTo(6);
    }

    @Test
    @DisplayName("should define default replication factor as 1")
    void shouldDefineDefaultReplicationFactor() throws Exception {
        var value = getStaticField("DEFAULT_REPLICATION_FACTOR");

        assertThat((short) value).isEqualTo((short) 1);
    }

    @Test
    @DisplayName("all fields should be static final")
    void allFieldsShouldBeStaticFinal() {
        for (Field field : KafkaConstants.class.getDeclaredFields()) {
            int mod = field.getModifiers();
            assertThat(Modifier.isStatic(mod)).as("field %s should be static", field.getName()).isTrue();
            assertThat(Modifier.isFinal(mod)).as("field %s should be final", field.getName()).isTrue();
        }
    }

    private static Object getStaticField(String name) throws Exception {
        Field field = KafkaConstants.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }
}
