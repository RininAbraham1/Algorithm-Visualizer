package com.rinin.algoviz.pathfinding;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Depth-first search: always follows the most recently discovered cell
 * (a LIFO stack), so it dives down one corridor as far as it can before
 * backtracking. It finds <em>a</em> path if one exists, but usually not a
 * shortest one. Written iteratively so a big open grid can't overflow the
 * call stack.
 */
public final class DepthFirstSearch implements PathfindingAlgorithm {

    /** A cell waiting on the stack, with the cell it was reached from. */
    private record Entry(Cell cell, Cell from) { }

    @Override
    public String name() {
        return "Depth-First Search";
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
        return false;
    }

    @Override
    public List<Cell> findPath(Grid grid, SearchListener listener) {
        boolean[][] visited = new boolean[grid.rows()][grid.cols()];
        Cell[][] previous = new Cell[grid.rows()][grid.cols()];
        Deque<Entry> stack = new ArrayDeque<>();

        stack.push(new Entry(grid.start(), null));
        listener.onFrontier(grid.start());

        while (!stack.isEmpty()) {
            Entry entry = stack.pop();
            Cell cell = entry.cell();
            // A cell can be pushed more than once before it's popped;
            // only the first pop counts.
            if (visited[cell.row()][cell.col()]) {
                continue;
            }
            visited[cell.row()][cell.col()] = true;
            previous[cell.row()][cell.col()] = entry.from();
            listener.onVisit(cell);
            if (cell.equals(grid.end())) {
                break;
            }
            List<Cell> neighbors = grid.neighbors(cell);
            // Push in reverse so the first neighbor (up) is explored first.
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                Cell next = neighbors.get(i);
                if (!visited[next.row()][next.col()]) {
                    stack.push(new Entry(next, cell));
                    listener.onFrontier(next);
                }
            }
        }
        return Paths.reconstruct(previous, grid.start(), grid.end());
    }
}
