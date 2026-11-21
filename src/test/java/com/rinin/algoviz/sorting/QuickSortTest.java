package com.rinin.algoviz.sorting;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class QuickSortTest extends SortingAlgorithmContract {

    @Override
    SortingAlgorithm algorithm() {
        return new QuickSort();
    }

    @Test
    void sortedInputIsNotQuadratic() {
        // A naive last-element pivot would need ~n²/2 = 50M comparisons here.
        int n = 10_000;
        TrackedArray a = new TrackedArray(IntStream.rangeClosed(1, n).toArray(), SortListener.NONE);
        new QuickSort().sort(a);
        assertTrue(a.comparisons() < 1_000_000, "comparisons: " + a.comparisons());
    }
}
