package com.rinin.algoviz.sorting;

/**
 * Top-down merge sort: split the array in half, sort each half
 * recursively, then merge the two sorted halves through a buffer.
 * Always O(n log n), and stable (equal values keep their order).
 */
public final class MergeSort implements SortingAlgorithm {

    @Override
    public String name() {
        return "Merge Sort";
    }

    @Override
    public String timeComplexity() {
        return "O(n log n)";
    }

    @Override
    public String spaceComplexity() {
        return "O(n)";
    }

    @Override
    public void sort(TrackedArray a) {
        int n = a.length();
        if (n == 0) {
            return;
        }
        sort(a, new int[n], 0, n - 1, true);
    }

    /** Sorts a[lo..hi]. Bars only turn green on the final, top-level merge. */
    private void sort(TrackedArray a, int[] buffer, int lo, int hi, boolean topLevel) {
        if (lo >= hi) {
            if (topLevel) {
                a.markSorted(lo);
            }
            return;
        }
        int mid = lo + (hi - lo) / 2;
        sort(a, buffer, lo, mid, false);
        sort(a, buffer, mid + 1, hi, false);
        merge(a, buffer, lo, mid, hi, topLevel);
    }

    private void merge(TrackedArray a, int[] buffer, int lo, int mid, int hi, boolean topLevel) {
        for (int k = lo; k <= hi; k++) {
            buffer[k] = a.get(k);
        }
        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            int value;
            if (i > mid) {
                value = buffer[j++];
            } else if (j > hi) {
                value = buffer[i++];
            } else if (a.compareValues(i, j, buffer[j], buffer[i]) < 0) {
                value = buffer[j++]; // right is strictly smaller
            } else {
                value = buffer[i++]; // take left on ties to stay stable
            }
            a.set(k, value);
            if (topLevel) {
                a.markSorted(k);
            }
        }
    }
}
