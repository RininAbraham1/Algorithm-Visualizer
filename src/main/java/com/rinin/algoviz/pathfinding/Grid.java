package com.rinin.algoviz.pathfinding;

import java.util.ArrayList;
import java.util.List;

/**
 * A rectangular grid of open cells and walls, with a start and an end.
 * Movement is in four directions (no diagonals). Each open cell has a
 * weight: the cost of stepping into it (1 for normal cells).
 */
public final class Grid {
    private static final int[][] DIRECTIONS = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

    private final int rows;
    private final int cols;
    private final boolean[][] walls;
    private final int[][] weights;
    private Cell start;
    private Cell end;

    public Grid(int rows, int cols) {
        if (rows < 1 || cols < 2) {
            throw new IllegalArgumentException("grid must be at least 1x2");
        }
        this.rows = rows;
        this.cols = cols;
        this.walls = new boolean[rows][cols];
        this.weights = new int[rows][cols];
        for (int[] row : weights) {
            java.util.Arrays.fill(row, 1);
        }
        this.start = new Cell(rows / 2, cols / 4);
        this.end = new Cell(rows / 2, Math.max(cols * 3 / 4, cols / 4 + 1));
    }

    /**
     * Builds a grid from text, one string per row: {@code #} wall,
     * {@code S} start, {@code E} end, {@code 2}-{@code 9} a cell with that
     * weight, anything else open. Handy in tests.
     */
    public static Grid parse(String... lines) {
        Grid grid = new Grid(lines.length, lines[0].length());
        for (int r = 0; r < lines.length; r++) {
            for (int c = 0; c < lines[r].length(); c++) {
                switch (lines[r].charAt(c)) {
                    case '#' -> grid.walls[r][c] = true;
                    case 'S' -> grid.start = new Cell(r, c);
                    case 'E' -> grid.end = new Cell(r, c);
                    case '2', '3', '4', '5', '6', '7', '8', '9' ->
                            grid.weights[r][c] = lines[r].charAt(c) - '0';
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
            copy.weights[r] = weights[r].clone();
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

    /** Moves the start; ignored if the cell is the end. Clears any wall or weight there. */
    public void setStart(Cell cell) {
        if (inBounds(cell) && !cell.equals(end)) {
            clear(cell);
            start = cell;
        }
    }

    /** Moves the end; ignored if the cell is the start. Clears any wall or weight there. */
    public void setEnd(Cell cell) {
        if (inBounds(cell) && !cell.equals(start)) {
            clear(cell);
            end = cell;
        }
    }

    public boolean inBounds(Cell cell) {
        return cell.row() >= 0 && cell.row() < rows && cell.col() >= 0 && cell.col() < cols;
    }

    public boolean isWall(Cell cell) {
        return walls[cell.row()][cell.col()];
    }

    /** Sets or clears a wall (which also resets the weight). The start and end can't become walls. */
    public void setWall(Cell cell, boolean wall) {
        if (!cell.equals(start) && !cell.equals(end)) {
            walls[cell.row()][cell.col()] = wall;
            weights[cell.row()][cell.col()] = 1;
        }
    }

    /** Cost of stepping into {@code cell}. */
    public int weight(Cell cell) {
        return weights[cell.row()][cell.col()];
    }

    /** Makes {@code cell} an open cell with the given weight (at least 1). */
    public void setWeight(Cell cell, int weight) {
        if (weight < 1) {
            throw new IllegalArgumentException("weight must be at least 1");
        }
        if (!cell.equals(start) && !cell.equals(end)) {
            walls[cell.row()][cell.col()] = false;
            weights[cell.row()][cell.col()] = weight;
        }
    }

    public boolean hasWeights() {
        for (int[] row : weights) {
            for (int w : row) {
                if (w != 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Total cost of a path: the weight of every cell entered after the first. */
    public int pathCost(List<Cell> path) {
        int cost = 0;
        for (int i = 1; i < path.size(); i++) {
            cost += weight(path.get(i));
        }
        return cost;
    }

    /** Removes all walls and weights. */
    public void clearAll() {
        for (int r = 0; r < rows; r++) {
            java.util.Arrays.fill(walls[r], false);
            java.util.Arrays.fill(weights[r], 1);
        }
    }

    private void clear(Cell cell) {
        walls[cell.row()][cell.col()] = false;
        weights[cell.row()][cell.col()] = 1;
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
