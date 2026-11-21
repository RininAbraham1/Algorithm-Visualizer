package com.rinin.algoviz.pathfinding;

import java.util.List;

/** A search algorithm the visualizer can run on a {@link Grid}. */
public interface PathfindingAlgorithm {
    String name();

    /** Worst-case time complexity in terms of V cells and E edges. */
    String timeComplexity();

    String spaceComplexity();

    /**
     * Whether the path found is always a shortest one: lowest total cost if
     * {@link #usesWeights()}, otherwise fewest steps.
     */
    boolean guaranteesShortestPath();

    /** Whether the algorithm takes cell weights into account. */
    default boolean usesWeights() {
        return false;
    }

    /**
     * Searches from the grid's start to its end.
     *
     * @return the path including both ends, or an empty list if the end
     *         can't be reached
     */
    List<Cell> findPath(Grid grid, SearchListener listener);
}
