package com.rinin.algoviz.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Helpers shared by the search algorithms. */
final class Paths {
    private Paths() { }

    /**
     * Walks the {@code previous} links back from {@code end} to {@code start}.
     * Returns an empty list if {@code end} was never reached.
     */
    static List<Cell> reconstruct(Cell[][] previous, Cell start, Cell end) {
        if (!end.equals(start) && previous[end.row()][end.col()] == null) {
            return List.of();
        }
        List<Cell> path = new ArrayList<>();
        for (Cell c = end; c != null; c = c.equals(start) ? null : previous[c.row()][c.col()]) {
            path.add(c);
        }
        Collections.reverse(path);
        return path;
    }
}
