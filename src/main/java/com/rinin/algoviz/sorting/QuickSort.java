package com.rinin.algoviz.sorting;

/**
 * Quick sort: pick a pivot, move smaller values to its left and larger
 * values to its right (Lomuto partition), then sort each side.
 *
 * <p>The pivot is the median of the first, middle and last values, which
 * avoids the O(n²) worst case on already-sorted input. The smaller side is
 * sorted recursively and the larger side in a loop, which keeps the stack
 * O(log n) deep.
 */
public final class QuickSort implements SortingAlgorithm {

    @Override
    public String name() {
        return "Quick Sort";
    }

    @Override
    public String timeComplexity() {
        return "O(n log n)";
    }

    @Override
    public String spaceComplexity() {
        return "O(log n)";
    }

    @Override
    public void sort(TrackedArray a) {
        sort(a, 0, a.length() - 1);
    }

    private void sort(TrackedArray a, int lo, int hi) {
        while (lo < hi) {
            int p = partition(a, lo, hi);
            a.markSorted(p); // the pivot is now in its final place
            if (p - lo < hi - p) {
                sort(a, lo, p - 1);
                lo = p + 1;
            } else {
                sort(a, p + 1, hi);
                hi = p - 1;
            }
        }
        if (lo == hi) {
            a.markSorted(lo);
        }
    }

    /** Partitions a[lo..hi] around a median-of-three pivot; returns its index. */
    private int partition(TrackedArray a, int lo, int hi) {
        int mid = lo + (hi - lo) / 2;
        // Order lo, mid, hi so the median ends up at mid, then park it at hi.
        if (a.compare(mid, lo) < 0) {
            a.swap(mid, lo);
        }
        if (a.compare(hi, lo) < 0) {
            a.swap(hi, lo);
        }
        if (a.compare(mid, hi) < 0) {
            a.swap(mid, hi);
        }
        // Now a[hi] is the median of the three.
        int store = lo;
        for (int i = lo; i < hi; i++) {
            if (a.compare(i, hi) < 0) {
                if (i != store) {
                    a.swap(i, store);
                }
                store++;
            }
        }
        if (store != hi) {
            a.swap(store, hi);
        }
        return store;
    }
}
