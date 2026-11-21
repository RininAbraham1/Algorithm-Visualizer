package com.rinin.algoviz.sorting;

import java.util.List;

/** Every sorting algorithm shown in the UI, in menu order. */
public final class SortingAlgorithms {
    private SortingAlgorithms() { }

    public static List<SortingAlgorithm> all() {
        return List.of(
                new BubbleSort(),
                new MergeSort(),
                new QuickSort());
    }
}
