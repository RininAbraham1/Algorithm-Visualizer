package com.rinin.algoviz.pathfinding;

/** A position on the grid. */
public record Cell(int row, int col) {

    /** Grid steps between two cells when moving only up/down/left/right. */
    public int manhattanDistance(Cell other) {
        return Math.abs(row - other.row) + Math.abs(col - other.col);
    }
}
