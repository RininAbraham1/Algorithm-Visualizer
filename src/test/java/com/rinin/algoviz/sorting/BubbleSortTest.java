package com.rinin.algoviz.sorting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BubbleSortTest extends SortingAlgorithmContract {

    @Override
    SortingAlgorithm algorithm() {
        return new BubbleSort();
    }

    @Test
    void stopsAfterOnePassWhenAlreadySorted() {
        TrackedArray a = new TrackedArray(new int[] {1, 2, 3, 4, 5}, SortListener.NONE);
        new BubbleSort().sort(a);
        assertEquals(4, a.comparisons());
        assertEquals(0, a.writes());
    }
}
