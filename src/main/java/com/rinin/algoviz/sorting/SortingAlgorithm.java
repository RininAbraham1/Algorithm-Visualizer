package com.rinin.algoviz.sorting;

/** A sorting algorithm the visualizer can run. */
public interface SortingAlgorithm {
    String name();

    /** Average-case time complexity, e.g. "O(n log n)". */
    String timeComplexity();

    /** Extra space used, e.g. "O(1)". */
    String spaceComplexity();

    /** Sorts the array in ascending order, reporting each step through it. */
    void sort(TrackedArray array);
}
