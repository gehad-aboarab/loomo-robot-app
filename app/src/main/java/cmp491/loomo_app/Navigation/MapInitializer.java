package cmp491.loomo_app.Navigation;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import cmp491.loomo_app.Android.LoomoApplication;
import cmp491.loomo_app.Helpers.C;

public class MapInitializer {

    private Destination homeDestination;
    private LoomoMap loomoMap;
    private LoomoApplication loomoApplication;
    private static final String TAG = "SeniorSucks_MapInit";

    public Destination getHomeDestination() {
        return homeDestination;
    }

    public void setHomeDestination(Destination homeDestination) {
        this.homeDestination = homeDestination;
    }

    public MapInitializer(LoomoApplication loomoApplication){
        this.loomoApplication = loomoApplication;
        this.loomoMap = loomoApplication.loomoMap;
        this.homeDestination = loomoApplication.homeDestination;
    }


    // Creates a 2D grid map for the Loomo to use
    // Added it to LoomoApplication so that other activities can use it
    public void load(String mapData){
        try {
            JSONObject obj = new JSONObject(mapData);
            int rows = (int)Math.round(obj.getDouble("rows"));
            int cols = (int)Math.round(obj.getDouble("columns"));
            loomoMap.buildMap(rows,cols);
            loomoMap.setMapName(obj.getString("name"));
            loomoMap.setCellSize(obj.getDouble("cellSize"));
            addLandmarkFromJSONMap(C.LANDMARK_OBSTACLE,obj,"obstacles");
            addLandmarkFromJSONMap(C.LANDMARK_DESTINATIONS,obj,"destinations");
            addLandmarkFromJSONMap(C.LANDMARK_HOME,obj,"homeStations");
            Destination hl = loomoMap.getLandmarks().get("homeA");
            homeDestination = hl;
            loomoApplication.updateHomeLocation(this.homeDestination);
            loomoMap.printMap();
        } catch (Exception e){
            Log.d(TAG, "loadMap: "+ e.getMessage());
        }
    }

    // Helper function to extract landmark from map JSON object (retrieved from server) and add it to loomoMap object
    private void addLandmarkFromJSONMap(int landmarkType, JSONObject mapObj, String landmarkName){
        try{
            JSONArray landmarks = mapObj.getJSONArray(landmarkName);
            for(int i = 0; i<landmarks.length(); i++){
                if(landmarkType==C.LANDMARK_OBSTACLE) {
                    JSONObject landmark = landmarks.getJSONObject(i).getJSONObject("corners");
                    String[] corners = new String[]{landmark.getString("0"), landmark.getString("1"), landmark.getString("2"), landmark.getString("3")};
                    loomoMap.addLandmark(landmarkType,corners);
                } else {
                    String id = landmarks.getJSONObject(i).getString("name");
                    int x_coordinate = (int) Math.round(landmarks.getJSONObject(i).getDouble(C.X_COORD));
                    int y_coordinate = (int) Math.round(landmarks.getJSONObject(i).getDouble(C.Y_COORD));
                    float thetha = (float) landmarks.getJSONObject(i).getDouble(C.THETHA);
                    loomoMap.addLandmark(landmarkType, id, x_coordinate, y_coordinate, thetha);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "No "+landmarkName+" landmarks found in JSONMap");
        }
    }
}