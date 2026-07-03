package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.convention.BaseMessageConvention;
import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.convention.MessageConventionType;

/**
 * 测试 {@link BaseMessageConvention} 的通道分类和缓存逻辑。
 */
@DisplayName("BaseMessageConvention")
class BaseMessageConventionTest {

    // 测试用通道名
    static final String UNICAST_CHANNEL = "unicast-demo-channel";
    static final String MULTICAST_CHANNEL = "multicast-demo-channel";
    static final String REQUEST_CHANNEL = "request-demo-channel";
    static final String UNKNOWN_CHANNEL = "unknown-channel";

    @Nested
    @DisplayName("default convention")
    class DefaultConvention {

        @Test
        @DisplayName("should return Default as name")
        void shouldReturnDefaultName() {
            var conv = new BaseMessageConvention();

            assertThat(conv.getName()).isEqualTo("Default");
        }

        @Test
        @DisplayName("should reject null channel")
        void shouldRejectNull() {
            var conv = new BaseMessageConvention();

            assertThatThrownBy(() -> conv.isUnicast(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("custom convention definitions")
    class CustomDefinitions {

        @Test
        @DisplayName("should classify using defineUnicastTypeConvention")
        void shouldUseDefineUnicast() {
            var conv = new BaseMessageConvention();
            conv.defineUnicastTypeConvention(channel -> channel.startsWith("unicast"));

            assertThat(conv.isUnicast("unicast-event")).isTrue();
            assertThat(conv.isUnicast("other-channel")).isFalse();
        }

        @Test
        @DisplayName("should classify using defineMulticastTypeConvention")
        void shouldUseDefineMulticast() {
            var conv = new BaseMessageConvention();
            conv.defineMulticastTypeConvention(channel -> channel.startsWith("multicast"));

            assertThat(conv.isMulticast("multicast-event")).isTrue();
            assertThat(conv.isMulticast("other-channel")).isFalse();
        }

        @Test
        @DisplayName("should classify using defineRequestTypeConvention")
        void shouldUseDefineRequest() {
            var conv = new BaseMessageConvention();
            conv.defineRequestTypeConvention(channel -> channel.startsWith("request"));

            assertThat(conv.isRequest("request-get")).isTrue();
            assertThat(conv.isRequest("other-channel")).isFalse();
        }

        @Test
        @DisplayName("should classify using defineTypeConvention")
        void shouldUseDefineTypeConvention() {
            var conv = new BaseMessageConvention();
            conv.defineTypeConvention(channel -> {
                if (channel.contains("unicast")) return MessageConventionType.UNICAST;
                if (channel.contains("multicast")) return MessageConventionType.MULTICAST;
                if (channel.contains("request")) return MessageConventionType.REQUEST;
                return MessageConventionType.NONE;
            });

            assertThat(conv.isUnicast("my.unicast.event")).isTrue();
            assertThat(conv.isMulticast("my.multicast.event")).isTrue();
            assertThat(conv.isRequest("my.request.event")).isTrue();
        }
    }

    @Nested
    @DisplayName("additional conventions")
    class AdditionalConventions {

        @Test
        @DisplayName("should evaluate added conventions")
        void shouldEvaluateAddedConventions() {
            var conv = new BaseMessageConvention();
            conv.add(new MessageConvention() {
                @Override
                public String getName() {
                    return "test";
                }

                @Override
                public boolean isUnicast(String channel) {
                    return "custom-unicast".equals(channel);
                }

                @Override
                public boolean isMulticast(String channel) {
                    return false;
                }

                @Override
                public boolean isRequest(String channel) {
                    return false;
                }
            });

            assertThat(conv.isUnicast("custom-unicast")).isTrue();
            assertThat(conv.isUnicast("other")).isFalse();
        }

        @Test
        @DisplayName("should reject null/empty array in add")
        void shouldRejectNullAdd() {
            var conv = new BaseMessageConvention();

            assertThatThrownBy(() -> conv.add((MessageConvention[]) null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should list registered conventions")
        void shouldListRegisteredConventions() {
            var conv = new BaseMessageConvention();

            var names = conv.getRegisteredConventions();

            assertThat(names).isNotEmpty();
        }
    }
}
