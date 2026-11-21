package com.rinin.algoviz.pathfinding;

import java.util.ArrayList;
import java.util.List;

/**
 * A rectangular grid of open cells and walls, with a start and an end.
 * Movement is in four directions (no diagonals).
 */
public final class Grid {
    private static final int[][] DIRECTIONS = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

    private final int rows;
    private final int cols;
    private final boolean[][] walls;
    private Cell start;
    private Cell end;

    public Grid(int rows, int cols) {
        if (rows < 1 || cols < 2) {
            throw new IllegalArgumentException("grid must be at least 1x2");
        }
        this.rows = rows;
        this.cols = cols;
        this.walls = new boolean[rows][cols];
        this.start = new Cell(rows / 2, cols / 4);
        this.end = new Cell(rows / 2, Math.max(cols * 3 / 4, cols / 4 + 1));
    }

    /**
     * Builds a grid from text, one string per row: {@code #} wall,
     * {@code S} start, {@code E} end, anything else open. Handy in tests.
     */
    public static Grid parse(String... lines) {
        Grid grid = new Grid(lines.length, lines[0].length());
        for (int r = 0; r < lines.length; r++) {
            for (int c = 0; c < lines[r].length(); c++) {
                switch (lines[r].charAt(c)) {
                    case '#' -> grid.walls[r][c] = true;
                    case 'S' -> grid.start = new Cell(r, c);
                    case 'E' -> grid.end = new Cell(r, c);
                    default -> { }
                }
            }
        }
        return grid;
    }

    /** An independent copy, so a search can run while the UI edits the original. */
    public Grid copy() {
        Grid copy = new Grid(rows, cols);
        for (int r = 0; r < rows; r++) {
            copy.walls[r] = walls[r].clone();
        }
        copy.start = start;
        copy.end = end;
        return copy;
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public Cell start() {
        return start;
    }

    public Cell end() {
        return end;
    }

    /** Moves the start; ignored if the cell is the end. Clears any wall there. */
    public void setStart(Cell cell) {
        if (inBounds(cell) && !cell.equals(end)) {
            walls[cell.row()][cell.col()] = false;
            start = cell;
        }
    }

    /** Moves the end; ignored if the cell is the start. Clears any wall there. */
    public void setEnd(Cell cell) {
        if (inBounds(cell) && !cell.equals(start)) {
            walls[cell.row()][cell.col()] = false;
            end = cell;
        }
    }

    public boolean inBounds(Cell cell) {
        return cell.row() >= 0 && cell.row() < rows && cell.col() >= 0 && cell.col() < cols;
    }

    public boolean isWall(Cell cell) {
        return walls[cell.row()][cell.col()];
    }

    /** Sets or clears a wall. The start and end can't become walls. */
    public void setWall(Cell cell, boolean wall) {
        if (!cell.equals(start) && !cell.equals(end)) {
            walls[cell.row()][cell.col()] = wall;
        }
    }

    public void clearWalls() {
        for (boolean[] row : walls) {
            java.util.Arrays.fill(row, false);
        }
    }

    /** Open cells next to {@code cell}, in the order up, right, down, left. */
    public List<Cell> neighbors(Cell cell) {
        List<Cell> result = new ArrayList<>(4);
        for (int[] d : DIRECTIONS) {
            Cell next = new Cell(cell.row() + d[0], cell.col() + d[1]);
            if (inBounds(next) && !isWall(next)) {
                result.add(next);
            }
        }
        return result;
    }
}
