package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.euonia.bus.annotation.Channel;
import com.euonia.bus.message.MessageCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageCache} 单例和通道名称缓存。
 */
@DisplayName("MessageCache")
class MessageCacheTest {

    @Channel("annotated-channel")
    static class AnnotatedMsg {
    }

    static class NotAnnotatedMsg {
    }

    @Nested
    @DisplayName("singleton")
    class Singleton {

        @Test
        @DisplayName("should return same instance")
        void shouldReturnSameInstance() {
            var a = MessageCache.getInstance();
            var b = MessageCache.getInstance();

            assertThat(a).isSameAs(b);
        }
    }

    @Nested
    @DisplayName("channel resolution")
    class ChannelResolution {

        @Test
        @DisplayName("should resolve channel from @Channel annotation")
        void shouldResolveFromAnnotation() {
            var channel = MessageCache.getInstance().getOrAddChannel(AnnotatedMsg.class);

            assertThat(channel).isEqualTo("annotated-channel");
        }

        @Test
        @DisplayName("should fallback to class name when no annotation")
        void shouldFallbackToClassName() {
            var channel = MessageCache.getInstance().getOrAddChannel(NotAnnotatedMsg.class);

            assertThat(channel).isEqualTo(NotAnnotatedMsg.class.getName());
        }

        @Test
        @DisplayName("should cache and return same value on subsequent calls")
        void shouldCache() {
            var cache = MessageCache.getInstance();
            var ch1 = cache.getOrAddChannel(NotAnnotatedMsg.class);
            var ch2 = cache.getOrAddChannel(NotAnnotatedMsg.class);

            assertThat(ch1).isSameAs(ch2);
        }

        @Test
        @DisplayName("should reject null message type")
        void shouldRejectNull() {
            var cache = MessageCache.getInstance();

            assertThatThrownBy(() -> cache.getOrAddChannel(null))
                .isInstanceOf(NullPointerException.class);
        }
    }
}
