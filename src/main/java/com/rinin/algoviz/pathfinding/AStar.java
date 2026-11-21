package com.rinin.algoviz.pathfinding;

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * A* search: Dijkstra plus a guess of the remaining distance. It expands
 * the cell with the lowest {@code f = g + h}, where {@code g} is the cost
 * so far and {@code h} is the Manhattan distance to the end.
 *
 * <p>Every step costs at least 1, so Manhattan distance never overestimates
 * the real remaining cost (it's <em>admissible</em>), and it drops by at most
 * one cell's cost per step (it's <em>consistent</em>). That's what lets A*
 * still guarantee the cheapest path while exploring far fewer cells.
 */
public final class AStar implements PathfindingAlgorithm {

    /** Queue entry. Ties on f go to the cell closer to the end, then FIFO. */
    private record Entry(Cell cell, int g, int h, long order) {
        int f() {
            return g + h;
        }
    }

    @Override
    public String name() {
        return "A*";
    }

    @Override
    public String timeComplexity() {
        return "O((V + E) log V)";
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
    public boolean usesWeights() {
        return true;
    }

    @Override
    public List<Cell> findPath(Grid grid, SearchListener listener) {
        int[][] best = new int[grid.rows()][grid.cols()];
        for (int[] row : best) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }
        boolean[][] done = new boolean[grid.rows()][grid.cols()];
        Cell[][] previous = new Cell[grid.rows()][grid.cols()];
        PriorityQueue<Entry> queue = new PriorityQueue<>((a, b) -> {
            if (a.f() != b.f()) {
                return Integer.compare(a.f(), b.f());
            }
            if (a.h() != b.h()) {
                return Integer.compare(a.h(), b.h());
            }
            return Long.compare(a.order(), b.order());
        });
        long order = 0;

        Cell start = grid.start();
        Cell end = grid.end();
        best[start.row()][start.col()] = 0;
        queue.add(new Entry(start, 0, start.manhattanDistance(end), order++));
        listener.onFrontier(start);

        while (!queue.isEmpty()) {
            Entry entry = queue.poll();
            Cell cell = entry.cell();
            if (done[cell.row()][cell.col()]) {
                continue; // stale entry, as in Dijkstra
            }
            done[cell.row()][cell.col()] = true;
            listener.onVisit(cell);
            if (cell.equals(end)) {
                break;
            }
            for (Cell next : grid.neighbors(cell)) {
                int g = entry.g() + grid.weight(next);
                if (g < best[next.row()][next.col()]) {
                    best[next.row()][next.col()] = g;
                    previous[next.row()][next.col()] = cell;
                    queue.add(new Entry(next, g, next.manhattanDistance(end), order++));
                    listener.onFrontier(next);
                }
            }
        }
        return Paths.reconstruct(previous, start, end);
    }
}
