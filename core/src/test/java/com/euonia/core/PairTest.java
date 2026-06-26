package com.euonia.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Pair")
class PairTest {

    @Test
    @DisplayName("Given key and value when creating pair then values are stored")
    void givenKeyAndValueWhenCreatingThenValuesAreStored() {
        Pair<Integer, String> pair = Pair.of(1, "one");

        assertEquals(1, pair.key());
        assertEquals("one", pair.value());
    }

    @Test
    @DisplayName("Given null key when creating pair then illegal argument is thrown")
    void givenNullKeyWhenCreatingThenThrowIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> Pair.of(null, "value"));
    }
}

