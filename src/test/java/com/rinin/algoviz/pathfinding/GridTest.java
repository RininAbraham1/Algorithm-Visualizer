package com.rinin.algoviz.pathfinding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class GridTest {

    @Test
    void parsesStartEndAndWalls() {
        Grid grid = Grid.parse(
                "S.#",
                "..E");
        assertEquals(new Cell(0, 0), grid.start());
        assertEquals(new Cell(1, 2), grid.end());
        assertTrue(grid.isWall(new Cell(0, 2)));
        assertFalse(grid.isWall(new Cell(1, 1)));
    }

    @Test
    void neighborsSkipWallsAndEdges() {
        Grid grid = Grid.parse(
                "S#.",
                "..E");
        assertEquals(List.of(new Cell(1, 0)), grid.neighbors(new Cell(0, 0)));
        assertEquals(List.of(new Cell(0, 2), new Cell(1, 1)), grid.neighbors(new Cell(1, 2)));
    }

    @Test
    void startAndEndCannotBecomeWallsOrOverlap() {
        Grid grid = Grid.parse("S.E");
        grid.setWall(grid.start(), true);
        grid.setWall(grid.end(), true);
        assertFalse(grid.isWall(grid.start()));
        assertFalse(grid.isWall(grid.end()));

        grid.setStart(grid.end());
        assertEquals(new Cell(0, 0), grid.start());
    }

    @Test
    void copyIsIndependent() {
        Grid grid = Grid.parse("S.E");
        Grid copy = grid.copy();
        grid.setWall(new Cell(0, 1), true);
        assertFalse(copy.isWall(new Cell(0, 1)));
    }

    @Test
    void weightsDefaultToOneAndCountTowardPathCost() {
        Grid grid = Grid.parse("S5.E");
        assertEquals(1, grid.weight(new Cell(0, 2)));
        assertEquals(5, grid.weight(new Cell(0, 1)));
        assertEquals(7, grid.pathCost(List.of(
                new Cell(0, 0), new Cell(0, 1), new Cell(0, 2), new Cell(0, 3))));
        assertTrue(grid.hasWeights());
    }

    @Test
    void wallsAndWeightsReplaceEachOther() {
        Grid grid = Grid.parse("S..E");
        Cell cell = new Cell(0, 1);
        grid.setWeight(cell, 4);
        grid.setWall(cell, true);
        assertEquals(1, grid.weight(cell));
        grid.setWeight(cell, 3);
        assertFalse(grid.isWall(cell));

        grid.clearAll();
        assertFalse(grid.hasWeights());
    }
}
