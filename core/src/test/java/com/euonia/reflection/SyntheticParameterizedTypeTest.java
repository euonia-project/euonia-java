package com.euonia.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SyntheticParameterizedType")
class SyntheticParameterizedTypeTest {

    @Test
    @DisplayName("Given rawType and type arguments when constructing then getRawType returns correct type")
    void givenRawTypeAndTypeArgumentsWhenConstructingThenGetRawTypeReturnsCorrectType() {
        SyntheticParameterizedType type = new SyntheticParameterizedType(List.class, String.class);

        assertEquals(List.class, type.getRawType());
    }

    @Test
    @DisplayName("Given rawType and type arguments when constructing then getActualTypeArguments returns arguments")
    void givenRawTypeAndTypeArgumentsWhenConstructingThenGetActualTypeArgumentsReturnsArguments() {
        SyntheticParameterizedType type = new SyntheticParameterizedType(List.class, String.class);

        assertEquals(1, type.getActualTypeArguments().length);
        assertEquals(String.class, type.getActualTypeArguments()[0]);
    }

    @Test
    @DisplayName("Given type arguments when constructing then getOwnerType returns null")
    void givenTypeArgumentsWhenConstructingThenGetOwnerTypeReturnsNull() {
        SyntheticParameterizedType type = new SyntheticParameterizedType(List.class, String.class);

        assertNull(type.getOwnerType());
    }

    @Test
    @DisplayName("Given type arguments when constructing then getTypeName returns formatted string")
    void givenTypeArgumentsWhenConstructingThenGetTypeNameReturnsFormattedString() {
        SyntheticParameterizedType type = new SyntheticParameterizedType(List.class, String.class);

        assertTrue(type.getTypeName().contains("List"));
        assertTrue(type.getTypeName().contains("String"));
    }

    @Test
    @DisplayName("Given no type arguments when constructing then getTypeName returns raw type name")
    void givenNoTypeArgumentsWhenConstructingThenGetTypeNameReturnsRawTypeName() {
        SyntheticParameterizedType type = new SyntheticParameterizedType(String.class);

        assertEquals(String.class.getTypeName(), type.getTypeName());
    }

    @Test
    @DisplayName("Given two identical instances when comparing then equals returns true")
    void givenTwoIdenticalInstancesWhenComparingThenEqualsReturnsTrue() {
        SyntheticParameterizedType type1 = new SyntheticParameterizedType(List.class, String.class);
        SyntheticParameterizedType type2 = new SyntheticParameterizedType(List.class, String.class);

        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    @DisplayName("Given two different instances when comparing then equals returns false")
    void givenTwoDifferentInstancesWhenComparingThenEqualsReturnsFalse() {
        SyntheticParameterizedType type1 = new SyntheticParameterizedType(List.class, String.class);
        SyntheticParameterizedType type2 = new SyntheticParameterizedType(List.class, Integer.class);

        assertNotEquals(type1, type2);
    }

    @Test
    @DisplayName("Given same instance when comparing then equals returns true")
    void givenSameInstanceWhenComparingThenEqualsReturnsTrue() {
        SyntheticParameterizedType type = new SyntheticParameterizedType(List.class, String.class);

        assertEquals(type, type);
    }

    @Test
    @DisplayName("Given instance when comparing with null then equals returns false")
    void givenInstanceWhenComparingWithNullThenEqualsReturnsFalse() {
        SyntheticParameterizedType type = new SyntheticParameterizedType(List.class, String.class);

        assertNotEquals(null, type);
    }

    @Test
    @DisplayName("Given instance when comparing with different type then equals returns false")
    void givenInstanceWhenComparingWithDifferentTypeThenEqualsReturnsFalse() {
        SyntheticParameterizedType type = new SyntheticParameterizedType(List.class, String.class);

        assertNotEquals(new Object(), type);
    }

    @Test
    @DisplayName("Given instance when comparing with ParameterizedType with owner then equals returns false")
    void givenInstanceWhenComparingWithParameterizedTypeWithOwnerThenEqualsReturnsFalse() {
        SyntheticParameterizedType type = new SyntheticParameterizedType(List.class, String.class);

        Type typeWithOwner = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return SyntheticParameterizedTypeTest.class;
            }
        };

        assertNotEquals(type, typeWithOwner);
    }

    @Test
    @DisplayName("Given instance when calling toString then returns same as getTypeName")
    void givenInstanceWhenCallingToStringThenReturnsSameAsGetTypeName() {
        SyntheticParameterizedType type = new SyntheticParameterizedType(List.class, String.class);

        assertEquals(type.getTypeName(), type.toString());
    }

    @Test
    @DisplayName("Given withGenerics factory method when creating then returns correct instance")
    void givenWithGenericsFactoryMethodWhenCreatingThenReturnsCorrectInstance() {
        SyntheticParameterizedType type = SyntheticParameterizedType.withGenerics(List.class, String.class);

        assertEquals(List.class, type.getRawType());
        assertEquals(1, type.getActualTypeArguments().length);
        assertEquals(String.class, type.getActualTypeArguments()[0]);
    }
}
