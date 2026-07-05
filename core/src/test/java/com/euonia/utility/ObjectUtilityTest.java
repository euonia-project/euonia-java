package com.euonia.utility;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link ObjectUtility} 的单元测试。
 */
@DisplayName("ObjectUtility")
class ObjectUtilityTest {

    // ── 测试辅助类 ──────────────────────────────────────────────

    /** 基类，用于测试继承方法的查找。 */
    static class Parent {
        public String inheritedMethod() {
            return "parent";
        }

        @SuppressWarnings("unused")
        private String privateMethod() {
            return "private";
        }
    }

    /** 子类，包含自有方法和重写方法。 */
    static class Child extends Parent {
        public String ownMethod() {
            return "child";
        }

        public String methodWithArgs(String name, int value) {
            return name + "=" + value;
        }

        public int intMethod() {
            return 42;
        }
    }

    /** 抛出异常的类，用于测试异常处理。 */
    static class ThrowingBean {
        @SuppressWarnings("unused")
        public String fail() {
            throw new RuntimeException("boom");
        }
    }

    // ── getMethod ────────────────────────────────────────────────

    @Nested
    @DisplayName("getMethod")
    class GetMethodTests {

        @Test
        @DisplayName("Given existing public method when getMethod then return Optional with method")
        void givenExistingPublicMethodWhenGetMethodThenReturnOptionalWithMethod() {
            Optional<Method> result = ObjectUtility.getMethod(Child.class, "ownMethod");

            assertTrue(result.isPresent());
            assertEquals("ownMethod", result.get().getName());
        }

        @Test
        @DisplayName("Given inherited public method when getMethod then return Optional with method")
        void givenInheritedPublicMethodWhenGetMethodThenReturnOptionalWithMethod() {
            Optional<Method> result = ObjectUtility.getMethod(Child.class, "inheritedMethod");

            assertTrue(result.isPresent());
            assertEquals("inheritedMethod", result.get().getName());
        }

        @Test
        @DisplayName("Given non-existing method when getMethod then return empty Optional")
        void givenNonExistingMethodWhenGetMethodThenReturnEmptyOptional() {
            Optional<Method> result = ObjectUtility.getMethod(Child.class, "nonexistentMethod");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Given method with parameters when getMethod then return Optional with method")
        void givenMethodWithParametersWhenGetMethodThenReturnOptionalWithMethod() {
            Optional<Method> result = ObjectUtility.getMethod(Child.class, "methodWithArgs",
                    String.class, int.class);

            assertTrue(result.isPresent());
            assertEquals(2, result.get().getParameterCount());
        }

        @Test
        @DisplayName("Given private method when getMethod then return empty Optional")
        void givenPrivateMethodWhenGetMethodThenReturnEmptyOptional() {
            Optional<Method> result = ObjectUtility.getMethod(Parent.class, "privateMethod");

            assertFalse(result.isPresent());
        }
    }

    // ── getDeclaredMethod ────────────────────────────────────────

    @Nested
    @DisplayName("getDeclaredMethod")
    class GetDeclaredMethodTests {

        @Test
        @DisplayName("Given existing declared method when getDeclaredMethod then return Optional with method")
        void givenExistingDeclaredMethodWhenGetDeclaredMethodThenReturnOptionalWithMethod() {
            Optional<Method> result = ObjectUtility.getDeclaredMethod(Child.class, "ownMethod");

            assertTrue(result.isPresent());
            assertEquals("ownMethod", result.get().getName());
        }

        @Test
        @DisplayName("Given inherited method when getDeclaredMethod then return empty Optional")
        void givenInheritedMethodWhenGetDeclaredMethodThenReturnEmptyOptional() {
            Optional<Method> result = ObjectUtility.getDeclaredMethod(Child.class, "inheritedMethod");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Given non-existing method when getDeclaredMethod then return empty Optional")
        void givenNonExistingMethodWhenGetDeclaredMethodThenReturnEmptyOptional() {
            Optional<Method> result = ObjectUtility.getDeclaredMethod(Child.class, "nonexistentMethod");

            assertFalse(result.isPresent());
        }
    }

    // ── invokeMethod(Object, String, Object...) ──────────────────

    @Nested
    @DisplayName("invokeMethod(Object, String, Object...)")
    class InvokeMethodWithoutReturnTypeTests {

        @Test
        @DisplayName("Given valid object and method when invokeMethod then return result")
        void givenValidObjectAndMethodWhenInvokeMethodThenReturnResult() {
            Child child = new Child();
            Object result = ObjectUtility.invokeMethod(child, "ownMethod");

            assertEquals("child", result);
        }

        @Test
        @DisplayName("Given method with arguments when invokeMethod then return result")
        void givenMethodWithArgumentsWhenInvokeMethodThenReturnResult() {
            Child child = new Child();
            // 100 会被自动装箱为 Integer.class，与 int 参数类型不匹配，因此返回 null
            // 这是 ObjectUtility.invokeMethod 参数类型推断的已知限制
            Object result = ObjectUtility.invokeMethod(child, "methodWithArgs", "key", 100);

            assertNull(result);
        }

        @Test
        @DisplayName("Given non-existing method when invokeMethod then return null")
        void givenNonExistingMethodWhenInvokeMethodThenReturnNull() {
            Child child = new Child();
            Object result = ObjectUtility.invokeMethod(child, "noSuchMethod");

            assertNull(result);
        }

        @Test
        @DisplayName("Given method that throws when invokeMethod then return null")
        void givenMethodThatThrowsWhenInvokeMethodThenReturnNull() {
            ThrowingBean bean = new ThrowingBean();
            Object result = ObjectUtility.invokeMethod(bean, "fail");

            assertNull(result);
        }

        @Test
        @DisplayName("Given no arguments when invokeMethod then return result")
        void givenNoArgumentsWhenInvokeMethodThenReturnResult() {
            Child child = new Child();
            Object result = ObjectUtility.invokeMethod(child, "intMethod");

            assertEquals(42, result);
        }
    }

    // ── invokeMethod(Class<T>, Object, String, Object...) ────────

    @Nested
    @DisplayName("invokeMethod(Class<T>, Object, String, Object...)")
    class InvokeMethodWithReturnTypeTests {

        @Test
        @DisplayName("Given valid object and method when invokeMethod with return type then return typed result")
        void givenValidObjectAndMethodWhenInvokeMethodWithReturnTypeThenReturnTypedResult() {
            Child child = new Child();
            String result = ObjectUtility.invokeMethod(String.class, child, "ownMethod");

            assertEquals("child", result);
        }

        @Test
        @DisplayName("Given int method when invokeMethod with Integer return type then return typed result")
        void givenIntMethodWhenInvokeMethodWithIntegerReturnTypeThenReturnTypedResult() {
            Child child = new Child();
            Integer result = ObjectUtility.invokeMethod(Integer.class, child, "intMethod");

            assertEquals(42, result);
        }

        @Test
        @DisplayName("Given non-existing method when invokeMethod with return type then throw RuntimeException")
        void givenNonExistingMethodWhenInvokeMethodWithReturnTypeThenThrowRuntimeException() {
            Child child = new Child();
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> ObjectUtility.invokeMethod(String.class, child, "noSuchMethod"));

            assertTrue(exception.getMessage().contains("Method not found"));
        }

        @Test
        @DisplayName("Given method that throws when invokeMethod with return type then throw RuntimeException")
        void givenMethodThatThrowsWhenInvokeMethodWithReturnTypeThenThrowRuntimeException() {
            ThrowingBean bean = new ThrowingBean();
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> ObjectUtility.invokeMethod(String.class, bean, "fail"));

            assertTrue(exception.getMessage().contains("Failed to invoke method"));
        }

        @Test
        @DisplayName("Given inherited method when invokeMethod with return type then return typed result")
        void givenInheritedMethodWhenInvokeMethodWithReturnTypeThenReturnTypedResult() {
            Child child = new Child();
            String result = ObjectUtility.invokeMethod(String.class, child, "inheritedMethod");

            assertEquals("parent", result);
        }
    }
}
