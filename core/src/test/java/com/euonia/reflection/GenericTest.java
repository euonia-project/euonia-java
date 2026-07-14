package com.euonia.reflection;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
@DisplayName("Generic")
class GenericTest {
    @Test
    @DisplayName("Given anonymous Generic subclass with class hierarchy when resolving then concrete type is returned")
    void givenAnonymousGenericSubclassWithClassHierarchyWhenResolvingThenConcreteTypeIsReturned() {
        Generic<String> generic = new Generic<String>(StringHandler.class) {
        };
        Class<? super String> resolved = generic.resolve();
        assertEquals(String.class, resolved);
    }
    @Test
    @DisplayName("Given anonymous Generic subclass with interface hierarchy when resolving then concrete type is returned")
    void givenAnonymousGenericSubclassWithInterfaceHierarchyWhenResolvingThenConcreteTypeIsReturned() {
        Generic<Runnable> generic = new Generic<Runnable>(TaskHandler.class) {
        };
        Class<? super Runnable> resolved = generic.resolve();
        assertEquals(Runnable.class, resolved);
    }
    @Test
    @DisplayName("Given Generic with type variable diamond and parameterized hierarchy when resolving then concrete type is returned")
    void givenTypeVariableDiamondAndParameterizedHierarchyWhenResolvingThenConcreteTypeIsReturned() {
        Generic<String> generic = new Generic<String>(UserHandler.class) {
        };
        Class<? super String> resolved = generic.resolve();
        assertEquals(String.class, resolved);
    }
    @Test
    @DisplayName("Given Generic with parameterized type diamond when resolving then raw type is returned")
    void givenParameterizedTypeDiamondWhenResolvingThenRawTypeIsReturned() {
        Generic<java.util.List<String>> generic = new Generic<java.util.List<String>>(Object.class) {
        };
        Class<? super java.util.List<String>> resolved = generic.resolve();
        assertEquals(java.util.List.class, resolved);
    }
    @Test
    @DisplayName("Given two forType instances with same class when comparing then they are equal")
    void givenTwoForTypeInstancesWithSameClassWhenComparingThenTheyAreEqual() {
        Generic<String> left = Generic.forType(String.class);
        Generic<String> right = Generic.forType(String.class);
        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
    }
    @Test
    @DisplayName("Given two forType instances with different classes when comparing then they are not equal")
    void givenTwoForTypeInstancesWithDifferentClassesWhenComparingThenTheyAreNotEqual() {
        Generic<String> left = Generic.forType(String.class);
        Generic<Integer> right = Generic.forType(Integer.class);
        assertNotEquals(left, right);
    }
    @Test
    @DisplayName("Given same Generic instance when comparing then equals returns true")
    void givenSameInstanceWhenComparingThenEqualsReturnsTrue() {
        Generic<String> generic = Generic.forType(String.class);
        assertEquals(generic, generic);
    }
    @Test
    @DisplayName("Given Generic instance when comparing with null then equals returns false")
    void givenGenericInstanceWhenComparingWithNullThenEqualsReturnsFalse() {
        Generic<String> generic = Generic.forType(String.class);
        assertNotEquals(null, generic);
    }
    @Test
    @DisplayName("Given Generic instance when comparing with different type then equals returns false")
    void givenGenericInstanceWhenComparingWithDifferentTypeThenEqualsReturnsFalse() {
        Generic<String> generic = Generic.forType(String.class);
        assertNotEquals(new Object(), generic);
    }
    @Test
    @DisplayName("Given raw Generic subclass when instantiating then illegal argument exception is thrown")
    void givenRawGenericSubclassWhenInstantiatingThenIllegalArgumentExceptionIsThrown() {
        @SuppressWarnings("rawtypes")
        class RawGeneric extends Generic {
            RawGeneric() {
                super(Object.class);
            }
        }
        assertThrows(IllegalArgumentException.class, RawGeneric::new);
    }
    @Test
    @DisplayName("Given same Generic instance when resolving twice then cached result is used")
    void givenSameInstanceWhenResolvingTwiceThenCachedResultIsUsed() {
        Generic<String> generic = new Generic<String>(UserHandler.class) {
        };
        ConcurrentHashMap<Generic<?>, Type> spyCache = new ConcurrentHashMap<>();
        Generic.setCache(spyCache);
        generic.resolve();
        generic.resolve();
        assertEquals(1, spyCache.size());
    }
    @Test
    @DisplayName("Given Generic with multi-level parameterized hierarchy when resolving then deepest concrete type is found")
    void givenMultiLevelParameterizedHierarchyWhenResolvingThenDeepestConcreteTypeIsFound() {
        Generic<Integer> generic = new Generic<Integer>(AdminHandler.class) {
        };
        Class<? super Integer> resolved = generic.resolve();
        assertEquals(Integer.class, resolved);
    }
    static interface Service<T> {}

    static abstract class AbstractHandler<C, R> implements Service<C> {}

    static class UserHandler extends AbstractHandler<String, Integer> {}

    static abstract class BaseHandler<A> extends AbstractHandler<A, Long> {}

    static class AdminHandler extends BaseHandler<Integer> {}

    static class StringHandler extends AbstractHandler<String, Integer> {}

    static class TaskHandler implements Service<Runnable> {}
}
