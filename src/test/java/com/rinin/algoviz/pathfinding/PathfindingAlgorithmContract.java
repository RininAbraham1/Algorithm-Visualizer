package com.rinin.algoviz.pathfinding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Tests every pathfinding algorithm must pass. Subclass once per algorithm. */
abstract class PathfindingAlgorithmContract {

    abstract PathfindingAlgorithm algorithm();

    /** Checks the path runs start to end through adjacent open cells. */
    static void assertValidPath(Grid grid, List<Cell> path) {
        assertFalse(path.isEmpty(), "expected a path");
        assertEquals(grid.start(), path.get(0));
        assertEquals(grid.end(), path.get(path.size() - 1));
        for (int i = 1; i < path.size(); i++) {
            assertEquals(1, path.get(i - 1).manhattanDistance(path.get(i)), "path must be contiguous");
            assertFalse(grid.isWall(path.get(i)), "path must avoid walls");
        }
    }

    @Test
    void findsStraightPathOnOpenGrid() {
        Grid grid = Grid.parse(
                ".....",
                "S...E",
                ".....");
        List<Cell> path = algorithm().findPath(grid, SearchListener.NONE);
        assertValidPath(grid, path);
        if (algorithm().guaranteesShortestPath()) {
            assertEquals(5, path.size());
        }
    }

    @Test
    void goesThroughTheOnlyGap() {
        Grid grid = Grid.parse(
                "..#..",
                "S.#.E",
                ".....");
        List<Cell> path = algorithm().findPath(grid, SearchListener.NONE);
        assertValidPath(grid, path);
        assertTrue(path.contains(new Cell(2, 2)));
    }

    @Test
    void returnsEmptyWhenBlocked() {
        Grid grid = Grid.parse(
                "..#..",
                "S.#.E",
                "..#..");
        assertTrue(algorithm().findPath(grid, SearchListener.NONE).isEmpty());
    }

    @Test
    void findsValidPathsOnRandomGrids() {
        Random random = new Random(5);
        for (int trial = 0; trial < 200; trial++) {
            Grid grid = randomGrid(random);
            List<Cell> path = algorithm().findPath(grid.copy(), SearchListener.NONE);
            List<Cell> reference = new BreadthFirstSearch().findPath(grid.copy(), SearchListener.NONE);
            assertEquals(reference.isEmpty(), path.isEmpty(), "should agree with BFS on reachability");
            if (!path.isEmpty()) {
                assertValidPath(grid, path);
                if (algorithm().guaranteesShortestPath()) {
                    assertEquals(reference.size(), path.size(), "should be as short as BFS");
                }
            }
        }
    }

    @Test
    void visitsEachCellAtMostOnce() {
        Grid grid = randomGrid(new Random(9));
        boolean[][] visited = new boolean[grid.rows()][grid.cols()];
        algorithm().findPath(grid, new SearchListener() {
            @Override
            public void onVisit(Cell cell) {
                assertFalse(visited[cell.row()][cell.col()], "visited twice: " + cell);
                visited[cell.row()][cell.col()] = true;
            }
        });
    }

    static Grid randomGrid(Random random) {
        Grid grid = new Grid(15, 25);
        for (int r = 0; r < grid.rows(); r++) {
            for (int c = 0; c < grid.cols(); c++) {
                grid.setWall(new Cell(r, c), random.nextDouble() < 0.3);
            }
        }
        grid.setStart(new Cell(random.nextInt(15), random.nextInt(25)));
        grid.setEnd(new Cell(random.nextInt(15), random.nextInt(25)));
        return grid;
    }
}
