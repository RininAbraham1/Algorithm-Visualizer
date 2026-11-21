package com.rinin.algoviz.pathfinding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DijkstraTest extends PathfindingAlgorithmContract {

    @Override
    PathfindingAlgorithm algorithm() {
        return new Dijkstra();
    }

    @Test
    void goesAroundExpensiveCells() {
        Grid grid = Grid.parse(
                ".....",
                "S.9.E",
                ".....");
        List<Cell> dijkstra = new Dijkstra().findPath(grid, SearchListener.NONE);
        List<Cell> bfs = new BreadthFirstSearch().findPath(grid, SearchListener.NONE);

        assertValidPath(grid, dijkstra);
        assertEquals(6, grid.pathCost(dijkstra), "detour: 6 steps of cost 1");
        assertEquals(12, grid.pathCost(bfs), "BFS goes straight through the 9");
    }

    @Test
    void walksThroughWeightWhenItIsCheaper() {
        Grid grid = Grid.parse(
                "#####",
                "S.2.E",
                ".....");
        // Straight through costs 1+2+1+1 = 5; the detour costs 6.
        assertEquals(5, grid.pathCost(new Dijkstra().findPath(grid, SearchListener.NONE)));
    }

    @Test
    void matchesBruteForceCostsOnRandomWeightedGrids() {
        Random random = new Random(21);
        for (int trial = 0; trial < 100; trial++) {
            Grid grid = randomWeightedGrid(random);
            List<Cell> path = new Dijkstra().findPath(grid.copy(), SearchListener.NONE);
            int expected = bellmanFordCost(grid);
            if (expected == Integer.MAX_VALUE) {
                assertTrue(path.isEmpty());
            } else {
                assertValidPath(grid, path);
                assertEquals(expected, grid.pathCost(path));
            }
        }
    }

    static Grid randomWeightedGrid(Random random) {
        Grid grid = randomGrid(random);
        for (int r = 0; r < grid.rows(); r++) {
            for (int c = 0; c < grid.cols(); c++) {
                Cell cell = new Cell(r, c);
                if (!grid.isWall(cell) && random.nextDouble() < 0.3) {
                    grid.setWeight(cell, 2 + random.nextInt(8));
                }
            }
        }
        return grid;
    }

    /** Cheapest cost to the end by brute-force relaxation (Bellman-Ford). */
    static int bellmanFordCost(Grid grid) {
        int[][] dist = new int[grid.rows()][grid.cols()];
        for (int[] row : dist) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }
        dist[grid.start().row()][grid.start().col()] = 0;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int r = 0; r < grid.rows(); r++) {
                for (int c = 0; c < grid.cols(); c++) {
                    Cell cell = new Cell(r, c);
                    if (dist[r][c] == Integer.MAX_VALUE || grid.isWall(cell)) {
                        continue;
                    }
                    for (Cell next : grid.neighbors(cell)) {
                        int cost = dist[r][c] + grid.weight(next);
                        if (cost < dist[next.row()][next.col()]) {
                            dist[next.row()][next.col()] = cost;
                            changed = true;
                        }
                    }
                }
            }
        }
        return dist[grid.end().row()][grid.end().col()];
    }
}
