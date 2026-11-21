package com.rinin.algoviz.sorting;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrackedArrayTest {

    @Test
    void swapAndSetChangeValuesAndCountWrites() {
        int[] data = {3, 1, 2};
        TrackedArray a = new TrackedArray(data, SortListener.NONE);

        a.swap(0, 1);
        a.set(2, 9);

        assertArrayEquals(new int[] {1, 3, 9}, data);
        assertEquals(3, a.writes());
    }

    @Test
    void compareReturnsOrderAndCounts() {
        TrackedArray a = new TrackedArray(new int[] {5, 2}, SortListener.NONE);

        assertTrue(a.compare(0, 1) > 0);
        assertTrue(a.compare(1, 0) < 0);
        assertEquals(0, a.compareValues(0, 1, 4, 4));
        assertEquals(3, a.comparisons());
    }

    @Test
    void listenerSeesOperationsInOrder() {
        List<String> events = new ArrayList<>();
        SortListener recorder = new SortListener() {
            @Override
            public void onCompare(int i, int j) {
                events.add("compare " + i + " " + j);
            }

            @Override
            public void onSwap(int i, int j) {
                events.add("swap " + i + " " + j);
            }

            @Override
            public void onSet(int i, int value) {
                events.add("set " + i + " " + value);
            }

            @Override
            public void onSorted(int i) {
                events.add("sorted " + i);
            }
        };
        TrackedArray a = new TrackedArray(new int[] {2, 1}, recorder);

        a.compare(0, 1);
        a.swap(0, 1);
        a.set(0, 7);
        a.markAllSorted();

        assertEquals(List.of("compare 0 1", "swap 0 1", "set 0 7", "sorted 0", "sorted 1"), events);
    }
}
