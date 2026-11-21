package com.rinin.algoviz.pathfinding;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DepthFirstSearchTest extends PathfindingAlgorithmContract {

    @Override
    PathfindingAlgorithm algorithm() {
        return new DepthFirstSearch();
    }

    @Test
    void canReturnALongerPathThanBfs() {
        // DFS tries "up" first, so it wanders through the top rows
        // instead of going straight right.
        Grid grid = Grid.parse(
                ".......",
                ".......",
                "S.....E");
        List<Cell> dfs = new DepthFirstSearch().findPath(grid, SearchListener.NONE);
        List<Cell> bfs = new BreadthFirstSearch().findPath(grid, SearchListener.NONE);
        assertValidPath(grid, dfs);
        assertTrue(dfs.size() > bfs.size(), "dfs " + dfs.size() + " vs bfs " + bfs.size());
    }

    @Test
    void handlesLargeOpenGridWithoutStackOverflow() {
        Grid grid = new Grid(300, 300);
        assertValidPath(grid, new DepthFirstSearch().findPath(grid, SearchListener.NONE));
    }
}
