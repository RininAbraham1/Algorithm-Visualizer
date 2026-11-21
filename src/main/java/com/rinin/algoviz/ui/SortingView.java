package com.rinin.algoviz.ui;

import com.rinin.algoviz.sorting.SortListener;
import com.rinin.algoviz.sorting.SortingAlgorithm;
import com.rinin.algoviz.sorting.SortingAlgorithms;
import com.rinin.algoviz.sorting.TrackedArray;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/** Animated bar chart for sorting algorithms. */
public final class SortingView extends BorderPane {

    /** One recorded operation, replayed by the {@link StepPlayer}. */
    private record Step(Kind kind, int i, int j) {
        enum Kind { COMPARE, SWAP, SET, SORTED }
    }

    private static final Color BACKGROUND = Color.web("#0d1117");
    private static final Color BAR = Color.web("#c77dff");
    private static final Color COMPARE = Color.web("#f1e05a");
    private static final Color WRITE = Color.web("#ff6b6b");
    private static final Color SORTED = Color.web("#52b788");

    private static final int STATE_NONE = 0;
    private static final int STATE_COMPARE = 1;
    private static final int STATE_WRITE = 2;
    private static final int STATE_SORTED = 3;

    private final Random random = new Random();
    private final Canvas canvas = new Canvas();
    private final ComboBox<SortingAlgorithm> algorithmBox = new ComboBox<>();
    private final Slider sizeSlider = new Slider(10, 10_000, 100);
    private final Slider speedSlider = new Slider(0, 100, 30);
    private final Button shuffleButton = new Button("Shuffle");
    private final Button playButton = new Button("Play");
    private final Button stepButton = new Button("Step");
    private final Label sizeLabel = new Label();
    private final Label statusLabel = new Label();

    private int[] values = new int[0];
    private int[] state = new int[0];
    /** Indexes highlighted this frame; cleared before the next one. */
    private final List<Integer> transientMarks = new ArrayList<>();
    private long comparisons;
    private long writes;

    private final StepPlayer<Step> player =
            new StepPlayer<>(this::apply, this::draw, this::onFinished);

    public SortingView() {
        algorithmBox.setItems(FXCollections.observableArrayList(SortingAlgorithms.all()));
        algorithmBox.setCellFactory(list -> new NameCell());
        algorithmBox.setButtonCell(new NameCell());
        algorithmBox.setPromptText("No algorithms yet");
        if (!algorithmBox.getItems().isEmpty()) {
            algorithmBox.getSelectionModel().selectFirst();
        }
        algorithmBox.valueProperty().addListener((obs, old, now) -> reset());

        sizeSlider.setPrefWidth(160);
        sizeSlider.valueProperty().addListener((obs, old, now) -> {
            if (!sizeSlider.isValueChanging()) {
                shuffle();
            }
        });
        sizeSlider.valueChangingProperty().addListener((obs, was, changing) -> {
            if (!changing) {
                shuffle();
            }
        });

        speedSlider.setPrefWidth(160);
        speedSlider.valueProperty().addListener((obs, old, now) -> updateSpeed());

        shuffleButton.setOnAction(e -> shuffle());
        playButton.setOnAction(e -> togglePlay());
        stepButton.setOnAction(e -> step());

        ToolBar toolbar = new ToolBar(
                algorithmBox, new Separator(),
                new Label("Size"), sizeSlider, sizeLabel, new Separator(),
                new Label("Speed"), speedSlider, new Separator(),
                shuffleButton, playButton, stepButton);
        setTop(toolbar);

        Pane canvasHolder = new Pane(canvas);
        canvas.widthProperty().bind(canvasHolder.widthProperty());
        canvas.heightProperty().bind(canvasHolder.heightProperty());
        canvas.widthProperty().addListener(o -> draw());
        canvas.heightProperty().addListener(o -> draw());
        setCenter(canvasHolder);

        statusLabel.setPadding(new Insets(6, 10, 6, 10));
        setBottom(statusLabel);

        updateSpeed();
        shuffle();
    }

    private int size() {
        return (int) Math.round(sizeSlider.getValue());
    }

    /** Speed slider is exponential: 1 step per frame up to ~5,000. */
    private void updateSpeed() {
        int stepsPerFrame = (int) Math.round(Math.pow(5000, speedSlider.getValue() / 100.0));
        player.setStepsPerFrame(stepsPerFrame);
    }

    private void shuffle() {
        int n = size();
        values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = i + 1;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
        sizeLabel.setText(String.valueOf(n));
        reset();
    }

    /** Stops any run and clears highlights, keeping the current values. */
    private void reset() {
        player.stop();
        state = new int[values.length];
        transientMarks.clear();
        comparisons = 0;
        writes = 0;
        playButton.setText("Play");
        setControlsRunning(false);
        updateStatus();
        draw();
    }

    private void togglePlay() {
        if (!player.isRunning()) {
            startRun(false);
        } else {
            player.setPaused(!player.isPaused());
            playButton.setText(player.isPaused() ? "Resume" : "Pause");
        }
    }

    private void step() {
        if (!player.isRunning()) {
            startRun(true);
        }
        player.stepOnce();
    }

    private void startRun(boolean paused) {
        SortingAlgorithm algorithm = algorithmBox.getValue();
        if (algorithm == null) {
            return;
        }
        if (isSorted()) {
            shuffle();
        }
        reset();
        int[] copy = Arrays.copyOf(values, values.length);
        player.start(emit -> algorithm.sort(new TrackedArray(copy, new SortListener() {
            @Override
            public void onCompare(int i, int j) {
                emit.accept(new Step(Step.Kind.COMPARE, i, j));
            }

            @Override
            public void onSwap(int i, int j) {
                emit.accept(new Step(Step.Kind.SWAP, i, j));
            }

            @Override
            public void onSet(int i, int value) {
                emit.accept(new Step(Step.Kind.SET, i, value));
            }

            @Override
            public void onSorted(int i) {
                emit.accept(new Step(Step.Kind.SORTED, i, 0));
            }
        })));
        player.setPaused(paused);
        playButton.setText(paused ? "Resume" : "Pause");
        setControlsRunning(true);
    }

    private boolean isSorted() {
        for (int i = 1; i < values.length; i++) {
            if (values[i - 1] > values[i]) {
                return false;
            }
        }
        return values.length > 1;
    }

    private void setControlsRunning(boolean running) {
        sizeSlider.setDisable(running);
        algorithmBox.setDisable(running);
    }

    /** Applies one step to the displayed array. Runs on the FX thread. */
    private void apply(Step step) {
        switch (step.kind()) {
            case COMPARE -> {
                comparisons++;
                mark(step.i(), STATE_COMPARE);
                mark(step.j(), STATE_COMPARE);
            }
            case SWAP -> {
                writes += 2;
                int tmp = values[step.i()];
                values[step.i()] = values[step.j()];
                values[step.j()] = tmp;
                mark(step.i(), STATE_WRITE);
                mark(step.j(), STATE_WRITE);
            }
            case SET -> {
                writes++;
                values[step.i()] = step.j();
                mark(step.i(), STATE_WRITE);
            }
            case SORTED -> state[step.i()] = STATE_SORTED;
        }
    }

    private void mark(int i, int kind) {
        if (state[i] != STATE_SORTED) {
            state[i] = kind;
            transientMarks.add(i);
        }
    }

    private void onFinished() {
        playButton.setText("Play");
        setControlsRunning(false);
        draw();
    }

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        g.setFill(BACKGROUND);
        g.fillRect(0, 0, w, h);

        int n = values.length;
        if (n > 0 && w > 0 && h > 0) {
            double barWidth = w / n;
            double gap = barWidth > 4 ? 1 : 0;
            double top = 12;
            for (int i = 0; i < n; i++) {
                double barHeight = (h - top) * values[i] / n;
                g.setFill(colorFor(state[i]));
                g.fillRect(i * barWidth, h - barHeight, Math.max(1, barWidth - gap), barHeight);
            }
        }

        // Compare/write highlights last for one frame only.
        for (int i : transientMarks) {
            if (state[i] != STATE_SORTED) {
                state[i] = STATE_NONE;
            }
        }
        transientMarks.clear();
        updateStatus();
    }

    private static Color colorFor(int s) {
        return switch (s) {
            case STATE_COMPARE -> COMPARE;
            case STATE_WRITE -> WRITE;
            case STATE_SORTED -> SORTED;
            default -> BAR;
        };
    }

    private void updateStatus() {
        SortingAlgorithm a = algorithmBox.getValue();
        String complexity = a == null ? ""
                : "   ·   Time " + a.timeComplexity() + "   ·   Space " + a.spaceComplexity();
        statusLabel.setText(String.format("Comparisons: %,d   ·   Writes: %,d%s",
                comparisons, writes, complexity));
    }

    private static final class NameCell extends ListCell<SortingAlgorithm> {
        @Override
        protected void updateItem(SortingAlgorithm item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : item.name());
        }
    }
}
