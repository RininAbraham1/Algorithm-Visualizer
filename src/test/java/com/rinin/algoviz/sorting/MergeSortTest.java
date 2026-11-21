package com.rinin.algoviz.sorting;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class MergeSortTest extends SortingAlgorithmContract {

    @Override
    SortingAlgorithm algorithm() {
        return new MergeSort();
    }

    @Test
    void usesAtMostNLogNComparisons() {
        int n = 1024;
        int[] input = new Random(11).ints(n, 0, 1_000_000).toArray();
        TrackedArray a = new TrackedArray(input, SortListener.NONE);
        new MergeSort().sort(a);
        // Merge sort never needs more than n * log2(n) comparisons.
        assertTrue(a.comparisons() <= n * 10L, "comparisons: " + a.comparisons());
    }
}
