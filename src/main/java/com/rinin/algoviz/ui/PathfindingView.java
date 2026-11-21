package com.rinin.algoviz.ui;

import com.rinin.algoviz.pathfinding.Cell;
import com.rinin.algoviz.pathfinding.Grid;
import com.rinin.algoviz.pathfinding.PathfindingAlgorithm;
import com.rinin.algoviz.pathfinding.PathfindingAlgorithms;
import com.rinin.algoviz.pathfinding.SearchListener;
import java.util.List;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * Grid editor and search animation. Drag to draw walls or weighted cells
 * (right-drag erases), and drag the green start or red end to move them.
 */
public final class PathfindingView extends BorderPane {

    private record Step(Kind kind, Cell cell) {
        enum Kind { FRONTIER, VISIT, PATH }
    }

    private enum Drag { NONE, START, END, DRAW, ERASE }

    /** What a left-drag paints. */
    private enum Brush {
        WALL("Brush: Wall"), WEIGHT("Brush: Weight ×" + WEIGHT_VALUE);

        private final String label;

        Brush(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /** Cost of stepping into a weighted cell (normal cells cost 1). */
    private static final int WEIGHT_VALUE = 5;

    private static final int ROWS = 25;
    private static final int COLS = 45;
    private static final double RANDOM_WALL_DENSITY = 0.28;

    private static final Color BACKGROUND = Color.web("#0d1117");
    private static final Color EMPTY = Color.web("#161b22");
    private static final Color WALL = Color.web("#6e7681");
    private static final Color WEIGHT = Color.web("#d29922");
    private static final Color FRONTIER = Color.web("#c77dff");
    private static final Color VISITED = Color.web("#3d2a5c");
    private static final Color PATH = Color.web("#f1e05a");
    private static final Color START = Color.web("#52b788");
    private static final Color END = Color.web("#ff6b6b");

    private static final int STATE_NONE = 0;
    private static final int STATE_FRONTIER = 1;
    private static final int STATE_VISITED = 2;
    private static final int STATE_PATH = 3;

    private final Grid grid = new Grid(ROWS, COLS);
    private final int[][] state = new int[ROWS][COLS];
    private final Random random = new Random();

    private final Canvas canvas = new Canvas();
    private final ComboBox<PathfindingAlgorithm> algorithmBox = new ComboBox<>();
    private final ComboBox<Brush> brushBox =
            new ComboBox<>(FXCollections.observableArrayList(Brush.values()));
    private final Slider speedSlider = new Slider(0, 100, 35);
    private final Button runButton = new Button("Run");
    private final Button clearPathButton = new Button("Clear path");
    private final Button clearGridButton = new Button("Clear grid");
    private final Button randomButton = new Button("Random walls");
    private final Label statusLabel = new Label();

    private Drag drag = Drag.NONE;
    /** Last cell painted in the current drag, to fill gaps between mouse events. */
    private Cell lastPainted;
    private int visitedCount;
    private int pathCells;
    private int pathCost;
    private boolean searched;

    private final StepPlayer<Step> player =
            new StepPlayer<>(this::apply, this::draw, this::onFinished);

    public PathfindingView() {
        algorithmBox.setItems(FXCollections.observableArrayList(PathfindingAlgorithms.all()));
        algorithmBox.setCellFactory(list -> new NameCell());
        algorithmBox.setButtonCell(new NameCell());
        algorithmBox.setPrefWidth(200);
        algorithmBox.getSelectionModel().selectFirst();
        algorithmBox.valueProperty().addListener((obs, old, now) -> clearSearch());

        brushBox.getSelectionModel().select(Brush.WALL);

        speedSlider.setPrefWidth(140);
        speedSlider.valueProperty().addListener((obs, old, now) -> updateSpeed());

        runButton.setMinWidth(70);
        runButton.setOnAction(e -> toggleRun());
        clearPathButton.setOnAction(e -> clearSearch());
        clearGridButton.setOnAction(e -> {
            clearSearch();
            grid.clearAll();
            draw();
        });
        randomButton.setOnAction(e -> randomWalls());

        setTop(new ToolBar(
                algorithmBox, brushBox, new Separator(),
                new Label("Speed"), speedSlider, new Separator(),
                runButton, clearPathButton, clearGridButton, randomButton));

        Pane canvasHolder = new Pane(canvas);
        canvas.widthProperty().bind(canvasHolder.widthProperty());
        canvas.heightProperty().bind(canvasHolder.heightProperty());
        canvas.widthProperty().addListener(o -> draw());
        canvas.heightProperty().addListener(o -> draw());
        canvas.setOnMousePressed(this::onPress);
        canvas.setOnMouseDragged(this::onDrag);
        canvas.setOnMouseReleased(e -> {
            // The last drag events can be merged away on a quick release,
            // so finish the stroke at the release point too.
            onDrag(e);
            drag = Drag.NONE;
        });
        setCenter(canvasHolder);

        statusLabel.setPadding(new Insets(6, 10, 6, 10));
        setBottom(statusLabel);

        updateSpeed();
        clearSearch();
    }

    // ---- running -------------------------------------------------------

    /** Exponential speed: 1 to ~300 steps per frame. */
    private void updateSpeed() {
        player.setStepsPerFrame((int) Math.round(Math.pow(300, speedSlider.getValue() / 100.0)));
    }

    private void toggleRun() {
        if (player.isRunning()) {
            player.stop();
            onFinished();
            return;
        }
        PathfindingAlgorithm algorithm = algorithmBox.getValue();
        if (algorithm == null) {
            return;
        }
        clearSearch();
        searched = true;
        Grid snapshot = grid.copy();
        player.start(emit -> {
            List<Cell> path = algorithm.findPath(snapshot, new SearchListener() {
                @Override
                public void onFrontier(Cell cell) {
                    emit.accept(new Step(Step.Kind.FRONTIER, cell));
                }

                @Override
                public void onVisit(Cell cell) {
                    emit.accept(new Step(Step.Kind.VISIT, cell));
                }
            });
            for (Cell cell : path) {
                emit.accept(new Step(Step.Kind.PATH, cell));
            }
        });
        runButton.setText("Stop");
        setEditingDisabled(true);
    }

    private void apply(Step step) {
        Cell c = step.cell();
        switch (step.kind()) {
            case FRONTIER -> {
                if (state[c.row()][c.col()] == STATE_NONE) {
                    state[c.row()][c.col()] = STATE_FRONTIER;
                }
            }
            case VISIT -> {
                state[c.row()][c.col()] = STATE_VISITED;
                visitedCount++;
            }
            case PATH -> {
                state[c.row()][c.col()] = STATE_PATH;
                if (pathCells > 0) {
                    pathCost += grid.weight(c); // the start costs nothing
                }
                pathCells++;
            }
        }
    }

    private void onFinished() {
        runButton.setText("Run");
        setEditingDisabled(false);
        draw();
    }

    private void setEditingDisabled(boolean disabled) {
        algorithmBox.setDisable(disabled);
        brushBox.setDisable(disabled);
        clearGridButton.setDisable(disabled);
        randomButton.setDisable(disabled);
        clearPathButton.setDisable(disabled);
    }

    /** Stops any run and removes the search overlay, keeping the walls. */
    private void clearSearch() {
        player.stop();
        for (int[] row : state) {
            java.util.Arrays.fill(row, STATE_NONE);
        }
        visitedCount = 0;
        pathCells = 0;
        pathCost = 0;
        searched = false;
        onFinished();
    }

    private void randomWalls() {
        clearSearch();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid.setWall(new Cell(r, c), random.nextDouble() < RANDOM_WALL_DENSITY);
            }
        }
        draw();
    }

    // ---- editing -------------------------------------------------------

    private void onPress(MouseEvent e) {
        if (player.isRunning()) {
            return;
        }
        Cell cell = cellAt(e.getX(), e.getY());
        if (cell == null) {
            return;
        }
        if (searched) {
            clearSearch();
        }
        if (cell.equals(grid.start())) {
            drag = Drag.START;
        } else if (cell.equals(grid.end())) {
            drag = Drag.END;
        } else {
            // Pressing on a cell that already has the brush's content erases instead.
            boolean filled = brushBox.getValue() == Brush.WALL
                    ? grid.isWall(cell) : grid.weight(cell) > 1;
            boolean erase = e.getButton() == MouseButton.SECONDARY || filled;
            drag = erase ? Drag.ERASE : Drag.DRAW;
            lastPainted = null;
            paint(cell);
        }
        draw();
    }

    private void onDrag(MouseEvent e) {
        if (player.isRunning() || drag == Drag.NONE) {
            return;
        }
        Cell cell = cellAt(e.getX(), e.getY());
        if (cell == null) {
            return;
        }
        switch (drag) {
            case START -> grid.setStart(cell);
            case END -> grid.setEnd(cell);
            default -> paint(cell);
        }
        draw();
    }

    /**
     * Paints {@code cell} and every cell on the straight line from the last
     * painted one. A fast drag can skip cells between mouse events, which
     * would otherwise leave gaps in the wall.
     */
    private void paint(Cell cell) {
        Cell from = lastPainted == null ? cell : lastPainted;
        int steps = Math.max(Math.abs(cell.row() - from.row()), Math.abs(cell.col() - from.col()));
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0 : (double) i / steps;
            int row = (int) Math.round(from.row() + t * (cell.row() - from.row()));
            int col = (int) Math.round(from.col() + t * (cell.col() - from.col()));
            Cell target = new Cell(row, col);
            if (drag == Drag.ERASE) {
                grid.setWall(target, false); // also resets the weight
            } else if (brushBox.getValue() == Brush.WEIGHT) {
                grid.setWeight(target, WEIGHT_VALUE);
            } else {
                grid.setWall(target, true);
            }
        }
        lastPainted = cell;
    }

    // ---- drawing -------------------------------------------------------

    private double cellSize() {
        return Math.floor(Math.min(canvas.getWidth() / COLS, canvas.getHeight() / ROWS));
    }

    private double offsetX() {
        return Math.floor((canvas.getWidth() - cellSize() * COLS) / 2);
    }

    private double offsetY() {
        return Math.floor((canvas.getHeight() - cellSize() * ROWS) / 2);
    }

    private Cell cellAt(double x, double y) {
        double size = cellSize();
        if (size <= 0) {
            return null;
        }
        Cell cell = new Cell((int) Math.floor((y - offsetY()) / size),
                (int) Math.floor((x - offsetX()) / size));
        return grid.inBounds(cell) ? cell : null;
    }

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(BACKGROUND);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        double size = cellSize();
        if (size <= 0) {
            return;
        }
        double ox = offsetX();
        double oy = offsetY();
        double inset = Math.round(size * 0.3);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Cell cell = new Cell(r, c);
                double x = ox + c * size;
                double y = oy + r * size;
                g.setFill(colorFor(cell));
                g.fillRect(x + 1, y + 1, size - 2, size - 2);
                if (grid.weight(cell) > 1 && !cell.equals(grid.start()) && !cell.equals(grid.end())) {
                    // A smaller amber square so weights stay visible under the search colors.
                    g.setFill(WEIGHT);
                    g.fillRect(x + inset, y + inset, size - 2 * inset, size - 2 * inset);
                }
            }
        }
        updateStatus();
    }

    private Color colorFor(Cell cell) {
        if (cell.equals(grid.start())) {
            return START;
        }
        if (cell.equals(grid.end())) {
            return END;
        }
        if (grid.isWall(cell)) {
            return WALL;
        }
        return switch (state[cell.row()][cell.col()]) {
            case STATE_FRONTIER -> FRONTIER;
            case STATE_VISITED -> VISITED;
            case STATE_PATH -> PATH;
            default -> EMPTY;
        };
    }

    private void updateStatus() {
        PathfindingAlgorithm a = algorithmBox.getValue();
        StringBuilder text = new StringBuilder(String.format("Visited: %,d", visitedCount));
        if (searched && !player.isRunning()) {
            text.append(pathCells == 0 ? "   ·   No path"
                    : "   ·   Path length: " + (pathCells - 1) + "   ·   Cost: " + pathCost);
        }
        if (a != null) {
            text.append("   ·   ").append(guarantee(a))
                    .append("   ·   Time ").append(a.timeComplexity())
                    .append("   ·   Space ").append(a.spaceComplexity());
        }
        statusLabel.setText(text.toString());
    }

    private String guarantee(PathfindingAlgorithm a) {
        if (!a.guaranteesShortestPath()) {
            return "Path may not be shortest";
        }
        if (!a.usesWeights() && grid.hasWeights()) {
            return "Fewest steps, but ignores weights";
        }
        return "Always finds a shortest path";
    }

    private static final class NameCell extends ListCell<PathfindingAlgorithm> {
        @Override
        protected void updateItem(PathfindingAlgorithm item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : item.name());
        }
    }
}
