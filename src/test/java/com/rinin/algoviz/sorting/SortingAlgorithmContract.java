package com.rinin.algoviz.sorting;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Tests every sorting algorithm must pass. Subclass once per algorithm. */
abstract class SortingAlgorithmContract {

    abstract SortingAlgorithm algorithm();

    private int[] sort(int[] input) {
        int[] copy = Arrays.copyOf(input, input.length);
        algorithm().sort(new TrackedArray(copy, SortListener.NONE));
        return copy;
    }

    private void assertSorts(int[] input) {
        int[] expected = Arrays.copyOf(input, input.length);
        Arrays.sort(expected);
        assertArrayEquals(expected, sort(input), () -> "input " + Arrays.toString(input));
    }

    @Test
    void handlesEmptyAndSingleElement() {
        assertSorts(new int[] {});
        assertSorts(new int[] {42});
    }

    @Test
    void sortsSortedAndReversedInput() {
        assertSorts(new int[] {1, 2, 3, 4, 5, 6});
        assertSorts(new int[] {6, 5, 4, 3, 2, 1});
    }

    @Test
    void sortsDuplicatesAndNegatives() {
        assertSorts(new int[] {3, -1, 3, 0, -1, 7, 3});
        assertSorts(new int[] {5, 5, 5, 5});
    }

    @Test
    void sortsRandomArrays() {
        Random random = new Random(7);
        for (int trial = 0; trial < 50; trial++) {
            int[] input = random.ints(random.nextInt(300), -100, 100).toArray();
            assertSorts(input);
        }
    }

    @Test
    void marksEveryIndexSorted() {
        int[] input = new Random(3).ints(64, 0, 1000).toArray();
        BitSet sorted = new BitSet();
        algorithm().sort(new TrackedArray(input, new SortListener() {
            @Override
            public void onSorted(int i) {
                sorted.set(i);
            }
        }));
        assertTrue(sorted.cardinality() == input.length, "every bar should end up green");
    }

    @Test
    void describesItself() {
        assertFalse(algorithm().name().isBlank());
        assertTrue(algorithm().timeComplexity().startsWith("O("));
        assertTrue(algorithm().spaceComplexity().startsWith("O("));
    }
}
