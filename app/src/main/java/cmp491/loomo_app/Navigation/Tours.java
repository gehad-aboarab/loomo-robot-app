package cmp491.loomo_app.Navigation;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cmp491.loomo_app.Android.LoomoApplication;

public class Tours {
    private ArrayList<TourStop> tourCheckPoints;
    private LoomoApplication application;

    public Tours(JSONObject tourData, LoomoApplication application){
        tourCheckPoints = new ArrayList<>();
        this.application = application;
        load(tourData);
    }

    private void load(JSONObject obj){
        try {
            JSONArray destinations = obj.getJSONArray("destinations");
            for(int i=0;i<destinations.length(); i++){
                JSONObject tmp = destinations.getJSONObject(i);
                TourStop tourStop = new TourStop();
                tourStop.order = tmp.getInt("order");
                tourStop.destName = tmp.getString("destinationName");
                tourStop.speech = tmp.getString("speech");
                addTourStop(tourStop);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void addTourStop(TourStop tourStop){ tourCheckPoints.add(tourStop); }
    public TourStop getTourStop() throws Exception{
        TourStop smallest;
        try {
            smallest = tourCheckPoints.get(0);
        } catch (Exception e){
//            C.log(application.mqttHelper.mqttAndroidClient,C.L2S_ADMIN_LOG,"List of tour stops has ended");
            throw new Exception(e);
        }
        for(TourStop t : tourCheckPoints ) {
            if (t.order < smallest.order)
                smallest = t;
        }
//        C.log(application.mqttHelper.mqttAndroidClient,C.L2S_ADMIN_LOG,"Next tour stop is: "+smallest.destName);
        tourCheckPoints.remove(smallest);
        Log.d("LoomoTours", "getTour: "+smallest.toString());
        return smallest;
    }
}
