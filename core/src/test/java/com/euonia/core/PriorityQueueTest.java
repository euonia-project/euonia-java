package com.euonia.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PriorityQueue")
class PriorityQueueTest {

    @Test
    @DisplayName("Given new queue when checking isEmpty then returns true")
    void givenNewQueueWhenCheckingIsEmptyThenReturnsTrue() {
        PriorityQueue<String, Integer> queue = new PriorityQueue<>();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    @DisplayName("Given elements added when polling then returns in priority order")
    void givenElementsAddedWhenPollingThenReturnsInPriorityOrder() {
        PriorityQueue<String, Integer> queue = new PriorityQueue<>();
        queue.add("low", 10);
        queue.add("high", 1);
        queue.add("medium", 5);

        assertEquals("high", queue.poll());
        assertEquals("medium", queue.poll());
        assertEquals("low", queue.poll());
    }

    @Test
    @DisplayName("Given elements added when peeking then returns highest priority without removing")
    void givenElementsAddedWhenPeekingThenReturnsHighestPriorityWithoutRemoving() {
        PriorityQueue<String, Integer> queue = new PriorityQueue<>();
        queue.add("low", 10);
        queue.add("high", 1);

        assertEquals("high", queue.peek());
        assertEquals(2, queue.size());
        assertEquals("high", queue.peek());
    }

    @Test
    @DisplayName("Given empty queue when polling then returns null")
    void givenEmptyQueueWhenPollingThenReturnsNull() {
        PriorityQueue<String, Integer> queue = new PriorityQueue<>();

        assertNull(queue.poll());
    }

    @Test
    @DisplayName("Given empty queue when peeking then returns null")
    void givenEmptyQueueWhenPeekingThenReturnsNull() {
        PriorityQueue<String, Integer> queue = new PriorityQueue<>();

        assertNull(queue.peek());
    }

    @Test
    @DisplayName("Given elements added when clearing then queue is empty")
    void givenElementsAddedWhenClearingThenQueueIsEmpty() {
        PriorityQueue<String, Integer> queue = new PriorityQueue<>();
        queue.add("a", 1);
        queue.add("b", 2);
        queue.add("c", 3);

        queue.clear();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    @DisplayName("Given elements added when checking size then returns correct count")
    void givenElementsAddedWhenCheckingSizeThenReturnsCorrectCount() {
        PriorityQueue<String, Integer> queue = new PriorityQueue<>();
        queue.add("a", 1);
        queue.add("b", 2);
        queue.add("c", 3);

        assertEquals(3, queue.size());
        assertFalse(queue.isEmpty());
    }

    @Test
    @DisplayName("Given string priorities when adding then alphabetical priority is respected")
    void givenStringPrioritiesWhenAddingThenAlphabeticalPriorityIsRespected() {
        PriorityQueue<String, String> queue = new PriorityQueue<>();
        queue.add("last", "z");
        queue.add("first", "a");
        queue.add("middle", "m");

        assertEquals("first", queue.poll());
        assertEquals("middle", queue.poll());
        assertEquals("last", queue.poll());
    }
}
