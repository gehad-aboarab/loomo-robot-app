package cmp491.loomo_app.Navigation;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import cmp491.loomo_app.Helpers.C;

public class AStarMap {
    private int width;
    private int height;
    private AStarCheckPoint[][] AStarCheckPoints;
    private static final String TAG = "AStarMap_Tag";

    // Creates a map based on a two dimensional array, where each zero is a walkable node and any other number is not.
    public AStarMap(int[][] map) {
        this.height = map[0].length;
        this.width = map.length;
        AStarCheckPoints = new AStarCheckPoint[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                AStarCheckPoints[x][y] = new AStarCheckPoint(x, y, (map[x][y] != C.LANDMARK_OBSTACLE && map[x][y]
                        != C.LANDMARK_OTHER&&map[x][y] % C.LANDMARK_TEMP_OBSTACLE != 0));
            }
        }
    }
    // Prints the map to the standard out, where each walkable node is simply
    // not printed, each non-walkable node is printed as a '#' (pound sign) and
    // each node that is in the path as a '@' (at sign).
    public void printMap(List<AStarCheckPoint> path) {
        Log.d(TAG, "printMap: ");
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++) {
                if (!AStarCheckPoints[i][j].isWalkable()) {
                    System.out.print(" #");
                }
                else if (path.contains(new AStarCheckPoint(i, j, true))) {
                    System.out.print(" @");
                }
                else {
                    System.out.print(" *");
                }
            }
            System.out.print("\n");
        }
    }

    // If the X and Y parameters are within the map boundaries, return the node
    // in the specific coordinates, null otherwise
    public AStarCheckPoint getNode(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return AStarCheckPoints[x][y];
        }
        else {
            return null;
        }
    }

    // Tries to calculate a path from the start and end positions.
    public final List<AStarCheckPoint> findPath(int startX, int startY, int goalX, int goalY) {
        // If our start position is the same as our goal position ...
        if (startX == goalX && startY == goalY) {
            // Return an empty path, because we don't need to move at all.
            return new LinkedList<AStarCheckPoint>();
        }

        // The set of AStarCheckPoints already visited.
        List<AStarCheckPoint> openList = new LinkedList<AStarCheckPoint>();
        // The set of currently discovered AStarCheckPoints still to be visited.
        List<AStarCheckPoint> closedList = new LinkedList<AStarCheckPoint>();

        // Add starting node to open list.
        openList.add(AStarCheckPoints[startX][startY]);

        // This loop will be broken as soon as the current node position is
        // equal to the goal position.
        while (true) {
            // Gets node with the lowest F score from open list.
            AStarCheckPoint current = lowestFInList(openList);
            // Remove current node from open list.
            openList.remove(current);
            // Add current node to closed list.
            closedList.add(current);

            // If the current node position is equal to the goal position ...
            if ((current.getX() == goalX) && (current.getY() == goalY)) {
                // Return a LinkedList containing all of the visited AStarCheckPoints.
                return calcPath(AStarCheckPoints[startX][startY], current);
            }

            List<AStarCheckPoint> adjacentAStarCheckPoints = getAdjacent(current, closedList);
            for (AStarCheckPoint adjacent : adjacentAStarCheckPoints) {
                // If node is not in the open list ...
                if (!openList.contains(adjacent)) {
                    // Set current node as parent for this node.
                    adjacent.setParent(current);
                    // Set H costs of this node (estimated costs to goal).
                    adjacent.setH(AStarCheckPoints[goalX][goalY]);
                    // Set G costs of this node (costs from start to this node).
                    adjacent.setG(current);
                    // Add node to openList.
                    openList.add(adjacent);
                }
                // Else if the node is in the open list and the G score from
                // current node is cheaper than previous costs ...
                else if (adjacent.getG() > adjacent.calculateG(current)) {
                    // Set current node as parent for this node.
                    adjacent.setParent(current);
                    // Set G costs of this node (costs from start to this node).
                    adjacent.setG(current);
                }
            }

            // If no path exists ...
            if (openList.isEmpty()) {
                // Return an empty list.
                return new LinkedList<AStarCheckPoint>();
            }
            // But if it does, continue the loop.
        }
    }

    private List<AStarCheckPoint> calcPath(AStarCheckPoint start, AStarCheckPoint goal) {
        LinkedList<AStarCheckPoint> path = new LinkedList<AStarCheckPoint>();

        AStarCheckPoint AStarCheckPoint = goal;
        boolean done = false;
        while (!done) {
            path.addFirst(AStarCheckPoint);
            AStarCheckPoint = AStarCheckPoint.getParent();
            if (AStarCheckPoint.equals(start)) {
                done = true;
            }
        }
        return path;
    }

    /**
     * @param list
     *            The list to be checked.
     * @return The node with the lowest F score in the list.
     */
    private AStarCheckPoint lowestFInList(List<AStarCheckPoint> list) {
        AStarCheckPoint cheapest = list.get(0);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getF() < cheapest.getF()) {
                cheapest = list.get(i);
            }
        }
        return cheapest;
    }

    private List<AStarCheckPoint> getAdjacent(AStarCheckPoint AStarCheckPoint, List<AStarCheckPoint> closedList) {
        List<AStarCheckPoint> adjacentAStarCheckPoints = new LinkedList<AStarCheckPoint>();
        int x = AStarCheckPoint.getX();
        int y = AStarCheckPoint.getY();

        AStarCheckPoint adjacent;

        // Check left AStarCheckPoint
        if (x > 0) {
            adjacent = getNode(x - 1, y);
            if (adjacent != null && adjacent.isWalkable() && !closedList.contains(adjacent)) {
                adjacentAStarCheckPoints.add(adjacent);
            }
        }

        // Check northwest AStarCheckPoint
        if (x > 0 && y > 0) {
            adjacent = getNode(x - 1, y - 1);
            if (adjacent != null && adjacent.isWalkable() && !closedList.contains(adjacent)) {
                adjacentAStarCheckPoints.add(adjacent);
            }
        }

        // Check northeast AStarCheckPoint
        if (x < width && y > 0) {
            adjacent = getNode(x + 1, y - 1);
            if (adjacent != null && adjacent.isWalkable() && !closedList.contains(adjacent)) {
                adjacentAStarCheckPoints.add(adjacent);
            }
        }

        // Check right AStarCheckPoint
        if (x < width) {
            adjacent = getNode(x + 1, y);
            if (adjacent != null && adjacent.isWalkable() && !closedList.contains(adjacent)) {
                adjacentAStarCheckPoints.add(adjacent);
            }
        }

        // Check southwest AStarCheckPoint
        if (x < height && y < width) {
            adjacent = getNode(x + 1, y + 1);
            if (adjacent != null && adjacent.isWalkable() && !closedList.contains(adjacent)) {
                adjacentAStarCheckPoints.add(adjacent);
            }
        }

        // Check southeast AStarCheckPoint
        if (x < height && y > 0) {
            adjacent = getNode(x + 1, y - 1);
            if (adjacent != null && adjacent.isWalkable() && !closedList.contains(adjacent)) {
                adjacentAStarCheckPoints.add(adjacent);
            }
        }

        // Check top AStarCheckPoint
        if (y > 0) {
            adjacent = this.getNode(x, y - 1);
            if (adjacent != null && adjacent.isWalkable() && !closedList.contains(adjacent)) {
                adjacentAStarCheckPoints.add(adjacent);
            }
        }

        // Check bottom AStarCheckPoint
        if (y < height) {
            adjacent = this.getNode(x, y + 1);
            if (adjacent != null && adjacent.isWalkable() && !closedList.contains(adjacent)) {
                adjacentAStarCheckPoints.add(adjacent);
            }
        }
        return adjacentAStarCheckPoints;
    }

}