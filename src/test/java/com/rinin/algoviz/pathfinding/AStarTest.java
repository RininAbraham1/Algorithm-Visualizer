package com.rinin.algoviz.pathfinding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class AStarTest extends PathfindingAlgorithmContract {

    @Override
    PathfindingAlgorithm algorithm() {
        return new AStar();
    }

    @Test
    void findsSameCostAsDijkstraOnRandomWeightedGrids() {
        Random random = new Random(33);
        for (int trial = 0; trial < 200; trial++) {
            Grid grid = DijkstraTest.randomWeightedGrid(random);
            List<Cell> astar = new AStar().findPath(grid.copy(), SearchListener.NONE);
            List<Cell> dijkstra = new Dijkstra().findPath(grid.copy(), SearchListener.NONE);
            assertEquals(dijkstra.isEmpty(), astar.isEmpty());
            if (!astar.isEmpty()) {
                assertValidPath(grid, astar);
                assertEquals(grid.pathCost(dijkstra), grid.pathCost(astar));
            }
        }
    }

    @Test
    void visitsFarFewerCellsThanDijkstraOnOpenGrid() {
        Grid grid = new Grid(25, 45);
        int astar = countVisits(new AStar(), grid);
        int dijkstra = countVisits(new Dijkstra(), grid);
        assertTrue(astar * 5 < dijkstra, "A* " + astar + " vs Dijkstra " + dijkstra);
    }

    private static int countVisits(PathfindingAlgorithm algorithm, Grid grid) {
        AtomicInteger visits = new AtomicInteger();
        algorithm.findPath(grid.copy(), new SearchListener() {
            @Override
            public void onVisit(Cell cell) {
                visits.incrementAndGet();
            }
        });
        return visits.get();
    }
}
