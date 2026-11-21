package com.rinin.algoviz.pathfinding;

import java.util.List;

/** Every pathfinding algorithm shown in the UI, in menu order. */
public final class PathfindingAlgorithms {
    private PathfindingAlgorithms() { }

    public static List<PathfindingAlgorithm> all() {
        return List.of(
                new BreadthFirstSearch(),
                new DepthFirstSearch(),
                new Dijkstra(),
                new AStar());
    }
}
