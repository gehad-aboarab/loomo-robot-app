package cmp491.loomo_app.Navigation;

import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cmp491.loomo_app.Helpers.C;

public class LoomoMap {
    private int[][] map;
    private AStarMap aStarMap;
    private int x_rows;
    private int y_columns;
    private String mapName;
    private double cellSize;
    private Destination loomoLocation;
    private HashMap<String, Destination> landmarks;
    private static final String TAG = "LoomoMap_Tag";

    // Constructor
    public LoomoMap() {
        landmarks = new HashMap<>();
    }

    // Get the landmarks hashmap
    public HashMap<String, Destination> getLandmarks() {
        return landmarks;
    }

    // Set the landmarks hashmap
    public void setLandmarks(HashMap<String, Destination> landmarks) {
        this.landmarks = landmarks;
    }

    // Create an empty map
    public void buildMap(int rows, int columns) {
        map = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                map[i][j] = C.LANDMARK_FREE;
            }
        }
        this.x_rows = rows;
        this.y_columns = columns;
        moveLoomoMapToAStarMap();
    }

    // Resets all the values of the map to free
    public void emptyMap() {
        for (int i = 0; i < x_rows; i++) {
            for (int j = 0; j < y_columns; j++) {
                if (map[i][j] != C.LANDMARK_OBSTACLE)
                    map[i][j] = C.LANDMARK_FREE;
            }
        }
        moveLoomoMapToAStarMap();
    }

    // Make the map null and reset its attributes
    public void clearMap() {
        this.map = null;
        this.aStarMap = null;
        x_rows = y_columns = 0;
        cellSize = 0;
    }

    // Add landmark to the map
    public void addLandmark(int landmarkType, String id, int x, int y, float thetha) {
        map[x][y] = landmarkType;
        if(landmarkType == C.LANDMARK_DESTINATIONS || landmarkType == C.LANDMARK_HOME)
            landmarks.put(id, new Destination(x, y, thetha));
        moveLoomoMapToAStarMap();
    }

    // Add a temporary obstacle
    public void addTempObstacle(int x_coordinate, int y_coordinate) {
        map[x_coordinate][y_coordinate] *= C.LANDMARK_TEMP_OBSTACLE;
    }

    // Remove all temporary obstacles
    public void removeTempObstacles() {
        for (int i = 0; i < x_rows; i++) {
            for (int j = 0; j < y_columns; j++) {
                if (map[i][j] % C.LANDMARK_TEMP_OBSTACLE == 0)
                    map[i][j] /= C.LANDMARK_TEMP_OBSTACLE;
            }
        }
    }

    // Add a landmarks that is not a point (based on corners)
    public void addLandmark(int landmarkType, String[] corners) {
        Destination[] points = new Destination[corners.length];
        for (int i = 0; i < corners.length; i++) {
            String[] pointsString = corners[i].split(",");
            points[i] = new Destination(Math.round(Float.valueOf(pointsString[0])), Math.round(Float.valueOf(pointsString[1])));
        }
        for (int j = points[0].x; j < points[3].x; j++) {
            Arrays.fill(map[j], points[0].y, points[1].y, landmarkType);
        }
        moveLoomoMapToAStarMap();
    }

    // Add landmarks that is a point
    public void addLandmark(int landmarkType, int x, int y) {
        if (landmarkType == C.LANDMARK_TEMP_OBSTACLE && map[x][y] % C.LANDMARK_TEMP_OBSTACLE != 0)
            addTempObstacle(x, y);
        else
            map[x][y] = landmarkType;
        moveLoomoMapToAStarMap();
    }

    // Print the AStar map
    public void printAStarMap(List<AStarCheckPoint> points) {
        aStarMap.printMap(points);
    }

    // Print the map
    public void printMap() {
        Log.d(TAG, "Printing map");
        for (int k = 0; k < x_rows; k++) {
            for (int l = 0; l < y_columns; l++) {
                if (map[k][l] % C.LANDMARK_TEMP_OBSTACLE == 0)
                    System.out.println(" ]");
                else if (map[k][l] == C.LANDMARK_OBSTACLE)
                    System.out.print(" #");
                else if (map[k][l] == C.LANDMARK_DESTINATIONS)
                    System.out.print(" '");
                else if (map[k][l] == C.LANDMARK_HOME)
                    System.out.print(" !");
                else
                    System.out.print("  ");
            }
            System.out.print("\n");
        }
    }

    // Make AStar map out of Loomo map
    private void moveLoomoMapToAStarMap() {
        aStarMap = new AStarMap(this.map);
    }

    // Calculate route
    public List<AStarCheckPoint> calcRoute(int x_start, int y_start, int x_end, int y_end) {
        List<AStarCheckPoint> result = aStarMap.findPath(x_start, y_start, x_end, y_end);
        return result;
    }

    // Get the home location of Loomo
    public Destination getHomeLocation() {
        Destination d = new Destination(0, 0);
        for (int i = 0; i < x_rows; i++) {
            for (int j = 0; j < y_columns; j++) {
                if (map[i][j] == C.LANDMARK_HOME) {
                    d.x = i;
                    d.y = j;
                }
            }
        }
        Log.d(TAG, "getHomeLocation: " + d.toString());
        return d;
    }

    // Getters and setters
    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }
    public double getCellSize() { return cellSize; }
    public void setCellSize(double cellSize) { this.cellSize = cellSize; }
    public int[][] getMap() { return map; }
    public Destination getLoomoLocation() { return loomoLocation; }
    public void setLoomoLocation(Destination loomoLocation) { this.loomoLocation = loomoLocation; }
}
