package com.rinin.algoviz.sorting;

/**
 * Bubble sort: repeatedly walks the array swapping neighbours that are out
 * of order, so the largest remaining value "bubbles" to the end each pass.
 * Stops early if a pass makes no swaps, which makes it O(n) on sorted input.
 */
public final class BubbleSort implements SortingAlgorithm {

    @Override
    public String name() {
        return "Bubble Sort";
    }

    @Override
    public String timeComplexity() {
        return "O(n²)";
    }

    @Override
    public String spaceComplexity() {
        return "O(1)";
    }

    @Override
    public void sort(TrackedArray a) {
        int end = a.length() - 1;
        boolean swapped = true;
        while (swapped && end > 0) {
            swapped = false;
            for (int i = 0; i < end; i++) {
                if (a.compare(i, i + 1) > 0) {
                    a.swap(i, i + 1);
                    swapped = true;
                }
            }
            a.markSorted(end);
            end--;
        }
        // Everything left of `end` is already in order (no swaps last pass).
        for (int i = end; i >= 0; i--) {
            a.markSorted(i);
        }
    }
}
