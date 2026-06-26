package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.annotation.Multicast;
import com.euonia.bus.annotation.Request;
import com.euonia.bus.annotation.Unicast;
import com.euonia.bus.convention.BaseMessageConvention;
import com.euonia.bus.convention.MessageConvention;

/**
 * 测试 {@link BaseMessageConvention} 的类型分类和缓存逻辑。
 */
@DisplayName("BaseMessageConvention")
class BaseMessageConventionTest {

    @Unicast
    static class UnicastMsg {
    }

    @Multicast
    static class MulticastMsg {
    }

    @Request(responseType = String.class)
    static class RequestMsg implements com.euonia.bus.contract.Request<String> {
    }

    static class UnknownMsg {
    }

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
        @DisplayName("should reject null message type")
        void shouldRejectNull() {
            var conv = new BaseMessageConvention();

            assertThatThrownBy(() -> conv.isUnicastType(null))
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
            conv.defineUnicastTypeConvention(t -> t == UnknownMsg.class);

            assertThat(conv.isUnicastType(UnknownMsg.class)).isTrue();
            assertThat(conv.isUnicastType(String.class)).isFalse();
        }

        @Test
        @DisplayName("should classify using defineMulticastTypeConvention")
        void shouldUseDefineMulticast() {
            var conv = new BaseMessageConvention();
            conv.defineMulticastTypeConvention(t -> t == UnknownMsg.class);

            assertThat(conv.isMulticastType(UnknownMsg.class)).isTrue();
            assertThat(conv.isMulticastType(String.class)).isFalse();
        }

        @Test
        @DisplayName("should classify using defineRequestTypeConvention")
        void shouldUseDefineRequest() {
            var conv = new BaseMessageConvention();
            conv.defineRequestTypeConvention(t -> t == UnknownMsg.class);

            assertThat(conv.isRequestType(UnknownMsg.class)).isTrue();
            assertThat(conv.isRequestType(String.class)).isFalse();
        }

        @Test
        @DisplayName("should classify using defineTypeConvention")
        void shouldUseDefineTypeConvention() {
            var conv = new BaseMessageConvention();
            conv.defineTypeConvention(t -> {
                if (t == UnicastMsg.class) return MessageConventionType.UNICAST;
                if (t == MulticastMsg.class) return MessageConventionType.MULTICAST;
                if (t == RequestMsg.class) return MessageConventionType.REQUEST;
                return MessageConventionType.NONE;
            });

            assertThat(conv.isUnicastType(UnicastMsg.class)).isTrue();
            assertThat(conv.isMulticastType(MulticastMsg.class)).isTrue();
            assertThat(conv.isRequestType(RequestMsg.class)).isTrue();
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
                public String getName() { return "test"; }

                @Override
                public boolean isUnicastType(Class<?> t) { return t == UnknownMsg.class; }

                @Override
                public boolean isMulticastType(Class<?> t) { return false; }

                @Override
                public boolean isRequestType(Class<?> t) { return false; }
            });

            assertThat(conv.isUnicastType(UnknownMsg.class)).isTrue();
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
