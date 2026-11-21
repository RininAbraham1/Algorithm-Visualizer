package com.rinin.algoviz.ui;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import javafx.animation.AnimationTimer;

/**
 * Runs an algorithm on a background thread and replays its steps on the
 * JavaFX thread at a chosen speed.
 *
 * <p>The algorithm pushes steps into a small bounded queue, so it simply
 * blocks when it gets ahead of the animation. That keeps memory flat even
 * for millions of steps (bubble sort on 10,000 elements), instead of
 * recording every step up front.
 *
 * @param <E> the step type
 */
public final class StepPlayer<E> {

    /** Thrown inside the worker thread when a run is stopped early. */
    private static final class Cancelled extends RuntimeException {
        Cancelled() {
            super(null, null, false, false);
        }
    }

    private final Consumer<E> applyStep;
    private final Runnable onFrame;
    private final Runnable onFinished;
    private final AnimationTimer timer;

    private Thread worker;
    private BlockingQueue<E> queue;
    private volatile boolean producerDone;
    private boolean running;
    private boolean paused;
    private int stepsPerFrame = 1;

    /**
     * @param applyStep  applies one step to the view's state
     * @param onFrame    redraws after a batch of steps
     * @param onFinished called once every step has been shown
     */
    public StepPlayer(Consumer<E> applyStep, Runnable onFrame, Runnable onFinished) {
        this.applyStep = applyStep;
        this.onFrame = onFrame;
        this.onFinished = onFinished;
        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!paused) {
                    advance(stepsPerFrame);
                }
            }
        };
    }

    /** Starts a new run. {@code algorithm} receives a sink to emit steps into. */
    public void start(Consumer<Consumer<E>> algorithm) {
        stop();
        BlockingQueue<E> q = new ArrayBlockingQueue<>(4096);
        queue = q;
        producerDone = false;
        paused = false;
        running = true;
        worker = new Thread(() -> {
            try {
                algorithm.accept(step -> {
                    try {
                        q.put(step);
                    } catch (InterruptedException e) {
                        throw new Cancelled();
                    }
                });
            } catch (Cancelled ignored) {
                // stopped by the user
            } finally {
                producerDone = true;
            }
        }, "algorithm-worker");
        worker.setDaemon(true);
        worker.start();
        timer.start();
    }

    /** Applies up to {@code count} steps, then redraws. */
    private void advance(int count) {
        if (!running) {
            return;
        }
        for (int k = 0; k < count; k++) {
            boolean done = producerDone; // read before polling, see below
            E step = queue.poll();
            if (step == null) {
                // The worker sets producerDone only after its last put, so an
                // empty queue seen after done==true means nothing is left.
                if (done) {
                    finish();
                }
                break;
            }
            applyStep.accept(step);
        }
        onFrame.run();
    }

    /** Shows exactly one step (used by the Step button while paused). */
    public void stepOnce() {
        advance(1);
    }

    private void finish() {
        running = false;
        timer.stop();
        onFinished.run();
    }

    /** Cancels the current run, if any. */
    public void stop() {
        running = false;
        timer.stop();
        if (worker != null) {
            worker.interrupt();
            worker = null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setStepsPerFrame(int stepsPerFrame) {
        this.stepsPerFrame = Math.max(1, stepsPerFrame);
    }
}
