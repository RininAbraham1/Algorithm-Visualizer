package com.rinin.algoviz.sorting;

/**
 * Receives every operation a sorting algorithm performs, in order.
 * The UI turns these into animation frames; tests can ignore or count them.
 */
public interface SortListener {
    SortListener NONE = new SortListener() { };

    /** Indexes {@code i} and {@code j} were compared. */
    default void onCompare(int i, int j) { }

    /** The values at {@code i} and {@code j} were swapped. */
    default void onSwap(int i, int j) { }

    /** Index {@code i} was overwritten with {@code value}. */
    default void onSet(int i, int value) { }

    /** Index {@code i} holds its final value. */
    default void onSorted(int i) { }
}
