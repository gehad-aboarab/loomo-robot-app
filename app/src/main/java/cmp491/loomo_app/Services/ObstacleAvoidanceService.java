package cmp491.loomo_app.Services;

import android.os.AsyncTask;
import android.util.Log;

import cmp491.loomo_app.Helpers.C;
import cmp491.loomo_app.Helpers.Counter;
import cmp491.loomo_app.Helpers.MovementRules;
import cmp491.loomo_app.Android.LoomoApplication;
import cmp491.loomo_app.Navigation.AStarCheckPoint;
import cmp491.loomo_app.Navigation.Destination;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ObstacleAvoidanceService extends AsyncTask<Object, Void, Float> {
    private final String TAG = "ObstacleAvoidanceService_Tag";
    private boolean i = true;
    private LoomoBaseService loomoBaseService;
    private LoomoSpeakService loomoSpeakService;
    private LoomoApplication loomoApplication;
    private Counter counter;

    private float getAvgUltrasonicValue() {
        while (true) {
            counter.addToArrays(loomoBaseService.getUltrasonicData().getDistance());
            if (counter.increment() % counter.frequency == 0) {
                float val = counter.getAverage();
                return val;
            }
        }
    }

    private void threadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    protected void onPreExecute() {
        i = true;
        counter = new Counter();
        counter.frequency = C.OBSTACLE_COUNTER_FREQUENCY;
        Log.d(TAG, "onPreExecute: ");
        super.onPreExecute();
    }

    @Override
    protected Float doInBackground(Object... params) {
        loomoBaseService = (LoomoBaseService) params[0];
        loomoSpeakService = (LoomoSpeakService) params[2];
        loomoApplication = (LoomoApplication) params[1];
        float ultrasonicValue = -1.0f;
//        C.log(loomoApplication.mqttHelper.mqttAndroidClient, C.L2S_ADMIN_LOG, "Obst Avoid started");
        while (i) {
            if (isCancelled()) {
                break;
            }
            ultrasonicValue = getAvgUltrasonicValue();
            Log.d(TAG, String.valueOf(ultrasonicValue));
            if (loomoApplication.currentRoute.isStarted() && (ultrasonicValue < C.OBSTACLE_ULTRASONIC_DIST_MM || loomoApplication.rpiSensorFront)) {
                ultrasonicValue = getAvgUltrasonicValue();
                if (ultrasonicValue < C.OBSTACLE_ULTRASONIC_DIST_MM || loomoApplication.rpiSensorFront) {
                    loomoBaseService.clearCheckPoints();
                    AStarCheckPoint to = loomoApplication.currentRoute.getCurrentCheckPoint();
                    AStarCheckPoint from = new AStarCheckPoint(loomoApplication.lastKnownLocation.x, loomoApplication.lastKnownLocation.y, true);
                    loomoApplication.loomoMap.addLandmark(C.LANDMARK_TEMP_OBSTACLE, to.getX(), to.getY());
//                    C.log(loomoApplication.mqttHelper.mqttAndroidClient,C.L2S_ADMIN_LOG,"gotta make new route to: "+to.getX()+to.getY());
                    publishProgress();
                }
                threadSleep(C.SLEEP_MM);
                ultrasonicValue = getAvgUltrasonicValue();
                loomoApplication.rpiSensorFront = false;
                if (ultrasonicValue < 270.0) {
                    loomoSpeakService.speak("Somebody help me. I cannot move", "");
                }
            }
//            if (loomoApplication.currentRoute.isStarted()){// && (loomoApplication.rpiSensorLeft || loomoApplication.rpiSensorRight)) {
//                AStarCheckPoint to = loomoApplication.currentRoute.getCurrentCheckPoint();
//                AStarCheckPoint from = new AStarCheckPoint(loomoApplication.lastKnownLocation.x, loomoApplication.lastKnownLocation.y, true);
//                float dir = MovementRules.findDirection(to.getX() - from.getX(), to.getY() - from.getY());
//                ArrayList<Destination> obsPos = new ArrayList<>();
//                if (loomoApplication.rpiSensorLeft)
//                    obsPos = MovementRules.getObstacleLocation(dir, C.SENSOR_LEFT);
//                if (loomoApplication.rpiSensorRight)
//                    obsPos = MovementRules.getObstacleLocation(dir, C.SENSOR_RIGHT);
//                for (Destination p : obsPos) {
//                    C.log(loomoApplication.mqttHelper.mqttAndroidClient, C.L2S_ADMIN_LOG, "Adding obst at: " + to.getX() + " " + to.getY());
//                    loomoApplication.loomoMap.addLandmark(C.LANDMARK_TEMP_OBSTACLE, p.x + loomoApplication.lastKnownLocation.x, p.y + loomoApplication.lastKnownLocation.y);
//                }
//                loomoApplication.rpiSensorRight = false;
//                loomoApplication.rpiSensorLeft = false;
//                new Timer().schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        loomoApplication.rpiSensorLeft = loomoApplication.rpiSensorRight = true;
//                    }
//                }, 3000);
//            }
        }
        return ultrasonicValue;
    }

    @Override
    protected void onProgressUpdate(Void... aVoid) {
        super.onProgressUpdate();
        Log.d(TAG, "obtc6:  ");
//        C.log(loomoApplication.mqttHelper.mqttAndroidClient,C.L2S_ADMIN_LOG,"Journey type is: "+loomoApplication.currentRoute.getJOURNEY_TYPE());
        loomoApplication.currentRoute.startJourney(loomoApplication.currentRoute.getJourneyType());
    }

    @Override
    protected void onCancelled(Float aFloat) {
        i = false;
        Log.d(TAG, "onCancelled: with var");
        super.onCancelled(aFloat);
    }

    @Override
    protected void onPostExecute(Float values) {
        super.onPostExecute(values);
        Log.d(TAG, "onPostExecute: " + values);
    }
}