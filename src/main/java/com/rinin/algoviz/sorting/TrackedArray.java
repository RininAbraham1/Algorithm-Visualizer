package com.rinin.algoviz.sorting;

/**
 * An int array that reports every compare and write to a {@link SortListener}
 * and counts them, so the algorithms stay free of UI code.
 */
public final class TrackedArray {
    private final int[] values;
    private final SortListener listener;
    private long comparisons;
    private long writes;

    public TrackedArray(int[] values, SortListener listener) {
        this.values = values;
        this.listener = listener;
    }

    public int length() {
        return values.length;
    }

    /** Reads a value without reporting it (reads are not animated). */
    public int get(int i) {
        return values[i];
    }

    /** Compares the values at two indexes, like {@link Integer#compare}. */
    public int compare(int i, int j) {
        comparisons++;
        listener.onCompare(i, j);
        return Integer.compare(values[i], values[j]);
    }

    /**
     * Compares two values that live outside the array (e.g. in a merge buffer),
     * highlighting indexes {@code i} and {@code j} where they came from.
     */
    public int compareValues(int i, int j, int a, int b) {
        comparisons++;
        listener.onCompare(i, j);
        return Integer.compare(a, b);
    }

    public void swap(int i, int j) {
        writes += 2;
        int tmp = values[i];
        values[i] = values[j];
        values[j] = tmp;
        listener.onSwap(i, j);
    }

    public void set(int i, int value) {
        writes++;
        values[i] = value;
        listener.onSet(i, value);
    }

    public void markSorted(int i) {
        listener.onSorted(i);
    }

    public void markAllSorted() {
        for (int i = 0; i < values.length; i++) {
            listener.onSorted(i);
        }
    }

    public long comparisons() {
        return comparisons;
    }

    public long writes() {
        return writes;
    }
}
