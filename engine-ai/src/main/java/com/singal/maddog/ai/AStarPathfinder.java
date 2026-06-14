package com.singal.maddog.ai;

import com.singal.maddog.math.Vector2i;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A thread-safe A* pathfinding implementation for 2D grids.
 */
public class AStarPathfinder {

    private static class Node {
        public Vector2i tile;
        public Node parent;
        public double gCost;
        public double hCost;
        public double fCost;

        public Node(Vector2i tile, Node parent, double gCost, double hCost) {
            this.tile = new Vector2i(tile);
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }

    private static final Comparator<Node> NODE_COMPARATOR = Comparator.comparingDouble(n -> n.fCost);

    /**
     * Finds a path from start to goal grid coordinates on the given navigation grid.
     * Runs in O(N log N) using a PriorityQueue.
     *
     * @return A list of grid coordinates representing the path from goal to start (reversed), or null if no path found.
     */
    public static List<Vector2i> findPath(Vector2i start, Vector2i goal, NavigationGrid grid) {
        if (grid == null || grid.isSolid(goal.x, goal.y)) {
            return null; // Target is unreachable or invalid
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>(NODE_COMPARATOR);
        Set<Vector2i> closedSet = new HashSet<>();

        Node startNode = new Node(start, null, 0, start.distance(goal));
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.tile.equals(goal)) {
                // Reconstruct path
                List<Vector2i> path = new ArrayList<>();
                Node temp = current;
                while (temp != null) {
                    path.add(temp.tile);
                    temp = temp.parent;
                }
                return path; // Path contains tiles from goal to start (reversed order)
            }

            closedSet.add(current.tile);

            // Explore 8 neighbors (including diagonals)
            for (int i = 0; i < 9; i++) {
                if (i == 4) continue; // Skip center self
                
                int dx = (i % 3) - 1;
                int dy = (i / 3) - 1;

                int nx = current.tile.x + dx;
                int ny = current.tile.y + dy;

                Vector2i neighborTile = new Vector2i(nx, ny);

                if (grid.isSolid(nx, ny)) continue;
                if (closedSet.contains(neighborTile)) continue;

                // Adjust cost: diagonal moves cost ~1.414, straight moves cost 1.0
                double moveCost = (dx != 0 && dy != 0) ? 1.414 : 1.0;
                double gCost = current.gCost + moveCost;
                double hCost = neighborTile.distance(goal);

                Node neighborNode = new Node(neighborTile, current, gCost, hCost);

                // Check if this neighbor is already in openSet with a lower cost
                boolean skip = false;
                for (Node openNode : openSet) {
                    if (openNode.tile.equals(neighborTile) && gCost >= openNode.gCost) {
                        skip = true;
                        break;
                    }
                }

                if (!skip) {
                    openSet.add(neighborNode);
                }
            }
        }

        return null; // No path found
    }
}
