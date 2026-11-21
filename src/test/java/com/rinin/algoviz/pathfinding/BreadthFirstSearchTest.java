package com.rinin.algoviz.pathfinding;

class BreadthFirstSearchTest extends PathfindingAlgorithmContract {

    @Override
    PathfindingAlgorithm algorithm() {
        return new BreadthFirstSearch();
    }
}
