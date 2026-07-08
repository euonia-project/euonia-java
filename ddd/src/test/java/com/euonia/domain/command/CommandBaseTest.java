package com.euonia.domain.command;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link CommandBase}。
 */
@SuppressWarnings("unused")
@DisplayName("CommandBase")
class CommandBaseTest {

    static class TestCommand extends CommandBase {}

    @Nested
    @DisplayName("properties")
    class Properties {

        @Test
        @DisplayName("should initially have empty properties")
        void shouldInitiallyHaveEmptyProperties() {
            var cmd = new TestCommand();

            assertThat(cmd.getProperties()).isEmpty();
        }

        @Test
        @DisplayName("should set and get string property")
        void shouldSetAndGetStringProperty() {
            var cmd = new TestCommand();
            cmd.set("name", "test");

            assertThat(cmd.get("name")).isEqualTo("test");
            assertThat(cmd.getProperties()).containsEntry("name", "test");
        }

        @Test
        @DisplayName("should return null for missing property")
        void shouldReturnNullForMissingProperty() {
            var cmd = new TestCommand();

            assertThat(cmd.get("missing")).isNull();
        }

        @Test
        @DisplayName("should get typed property via coercion")
        void shouldGetTypedPropertyViaCoercion() {
            var cmd = new TestCommand();
            cmd.set("amount", "100");

            assertThat(cmd.get("amount", Integer.class)).isEqualTo(100);
        }

        @Test
        @DisplayName("should return null for missing typed property")
        void shouldReturnNullForMissingTypedProperty() {
            var cmd = new TestCommand();

            assertThat(cmd.get("missing", Integer.class)).isNull();
        }

        @Test
        @DisplayName("should overwrite property with same key")
        void shouldOverwritePropertyWithSameKey() {
            var cmd = new TestCommand();
            cmd.set("key", "old");
            cmd.set("key", "new");

            assertThat(cmd.get("key")).isEqualTo("new");
        }
    }

    @Nested
    @DisplayName("implements Command")
    class ImplementsCommand {

        @Test
        @DisplayName("should implement Command interface")
        void shouldImplementCommand() {
            var cmd = new TestCommand();

            assertThat(cmd).isInstanceOf(Command.class);
        }
    }
}
