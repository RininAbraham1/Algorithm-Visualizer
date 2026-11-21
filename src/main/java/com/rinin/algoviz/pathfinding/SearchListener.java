package com.rinin.algoviz.pathfinding;

/** Receives each step of a search so the UI can animate it. */
public interface SearchListener {
    SearchListener NONE = new SearchListener() { };

    /** {@code cell} was discovered and is waiting to be explored. */
    default void onFrontier(Cell cell) { }

    /** {@code cell} was taken off the frontier and explored. */
    default void onVisit(Cell cell) { }
}
