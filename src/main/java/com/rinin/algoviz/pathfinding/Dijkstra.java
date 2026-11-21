package com.rinin.algoviz.pathfinding;

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Dijkstra's algorithm: always explores the cell with the lowest total
 * cost from the start, using a priority queue. Unlike BFS it accounts for
 * weighted cells, so it finds the cheapest path, not just the one with
 * the fewest steps. Requires non-negative weights.
 */
public final class Dijkstra implements PathfindingAlgorithm {

    /**
     * A queue entry. {@code order} breaks ties first-in-first-out, which
     * makes the search spread evenly instead of in an arbitrary pattern.
     */
    private record Entry(Cell cell, int cost, long order) { }

    @Override
    public String name() {
        return "Dijkstra";
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
        PriorityQueue<Entry> queue = new PriorityQueue<>((a, b) -> a.cost() != b.cost()
                ? Integer.compare(a.cost(), b.cost())
                : Long.compare(a.order(), b.order()));
        long order = 0;

        Cell start = grid.start();
        best[start.row()][start.col()] = 0;
        queue.add(new Entry(start, 0, order++));
        listener.onFrontier(start);

        while (!queue.isEmpty()) {
            Entry entry = queue.poll();
            Cell cell = entry.cell();
            // Java's PriorityQueue can't lower a key, so a cell may be in the
            // queue several times. Skip the stale, more expensive copies.
            if (done[cell.row()][cell.col()]) {
                continue;
            }
            done[cell.row()][cell.col()] = true;
            listener.onVisit(cell);
            if (cell.equals(grid.end())) {
                break;
            }
            for (Cell next : grid.neighbors(cell)) {
                int cost = entry.cost() + grid.weight(next);
                if (cost < best[next.row()][next.col()]) {
                    best[next.row()][next.col()] = cost;
                    previous[next.row()][next.col()] = cell;
                    queue.add(new Entry(next, cost, order++));
                    listener.onFrontier(next);
                }
            }
        }
        return Paths.reconstruct(previous, start, grid.end());
    }
}
