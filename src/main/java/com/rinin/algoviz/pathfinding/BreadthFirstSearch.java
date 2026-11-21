package com.rinin.algoviz.pathfinding;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Breadth-first search: explores cells in rings of increasing distance from
 * the start using a FIFO queue. On a grid where every move costs the same,
 * the first time it reaches the end is along a shortest path.
 */
public final class BreadthFirstSearch implements PathfindingAlgorithm {

    @Override
    public String name() {
        return "Breadth-First Search";
    }

    @Override
    public String timeComplexity() {
        return "O(V + E)";
    }

    @Override
    public String spaceComplexity() {
        return "O(V)";
    }

    @Override
    public boolean guaranteesShortestPath() {
        return true;
    }

    @Override
    public List<Cell> findPath(Grid grid, SearchListener listener) {
        boolean[][] seen = new boolean[grid.rows()][grid.cols()];
        Cell[][] previous = new Cell[grid.rows()][grid.cols()];
        Queue<Cell> queue = new ArrayDeque<>();

        Cell start = grid.start();
        seen[start.row()][start.col()] = true;
        queue.add(start);
        listener.onFrontier(start);

        while (!queue.isEmpty()) {
            Cell cell = queue.poll();
            listener.onVisit(cell);
            if (cell.equals(grid.end())) {
                break;
            }
            for (Cell next : grid.neighbors(cell)) {
                if (!seen[next.row()][next.col()]) {
                    // Mark when queued, not when visited, so no cell is queued twice.
                    seen[next.row()][next.col()] = true;
                    previous[next.row()][next.col()] = cell;
                    queue.add(next);
                    listener.onFrontier(next);
                }
            }
        }
        return Paths.reconstruct(previous, start, grid.end());
    }
}
