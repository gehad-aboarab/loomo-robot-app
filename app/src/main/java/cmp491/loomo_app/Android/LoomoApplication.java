package cmp491.loomo_app.Android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.segway.robot.sdk.locomotion.sbv.Base;

import cmp491.loomo_app.Helpers.C;
import cmp491.loomo_app.Helpers.FloatPoint;
import cmp491.loomo_app.Helpers.MqttHelper;
import cmp491.loomo_app.Navigation.Destination;
import cmp491.loomo_app.Navigation.LoomoMap;
import cmp491.loomo_app.Navigation.Routing;
import cmp491.loomo_app.Services.LoomoBaseService;

public class LoomoApplication extends Application {
    public static final String MAP_NAME_SERVER = "EB2v2-Rotunda";
    public final String TAG = "Application_Tag";

    // Configuration file XML keys
    public static final String CONFIG_LOCAL_FILE = "config.txt";
    public static final String X_HOME = "x_home";
    public static final String Y_HOME = "y_home";
    public static final String T_HOME = "theta";
    public static final String X_USER = "x_user";
    public static final String Y_USER = "y_user";
    public static final String T_USER = "thetha_user";
    public static final String X_DESTINATION = "x_destination";
    public static final String Y_DESTINATION = "y_destination";
    public static final String T_DESTINATION = "thetha_destination";
    public static final String CLIENT_ID = "clientID";
    public static final String MAP_DATA = "mapData";
    public static final String TOURS_DATA = "toursData";
    public static final String CURRENT_STATE = "currentState";
    public static final String CURRENT_MODE = "currentMode";
    public static final String STEP_ON_X = "stepOnX";
    public static final String STEP_ON_Y = "stepOnY";
    public static final String STEP_ON_T = "stepOnT";

    public MqttHelper mqttHelper;

    public String deviceId;
    public String clientId;

    public int currentState;
    public int currentMode;

    public String mapData;
    public LoomoMap loomoMap;
    public Routing currentRoute;
//    public TourStop currentTourStop;
    public int obstCounter = 0;
    public Destination userDestination;
    public Destination targetDestination;
    public String targetDestinationName;
    public Destination homeDestination;
    public Destination lastKnownLocation;
    public FloatPoint stepOnPose;
//    public LoomoTours tour;

    public boolean rpiSensorFront;
    public boolean rpiSensorRight;
    public boolean rpiSensorLeft;

    @Override
    public void onCreate() {
        super.onCreate();
        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        loomoMap = new LoomoMap();
        mqttHelper = new MqttHelper(this);
        currentRoute = new Routing(this);

        rpiSensorFront = rpiSensorRight = rpiSensorLeft = false;
        loadConfigFile();

        Intent intent = new Intent(this, LoomoService.class);
        startService(intent);
    }

    @Override
    public void onTerminate() {
        Intent intent = new Intent(this, LoomoService.class);
        stopService(intent);
        super.onTerminate();
    }

    private void loadConfigFile() {
        SharedPreferences sp = getSharedPreferences(CONFIG_LOCAL_FILE, Context.MODE_PRIVATE);
        homeDestination = new Destination(sp.getInt(X_HOME, -100), sp.getInt(Y_HOME, -100),sp.getInt(T_HOME, -100));
        lastKnownLocation = new Destination(sp.getInt(C.X_COORD, homeDestination.x), sp.getInt(C.Y_COORD, homeDestination.y),sp.getFloat(C.THETHA, homeDestination.thetha));

        clientId = sp.getString(CLIENT_ID,null);
        mapData = sp.getString(MAP_DATA, null);

        currentState = sp.getInt(CURRENT_STATE, C.UNBOUND_AVAILABLE);
        currentMode = sp.getInt(CURRENT_MODE, C.GUIDE_MODE);
        stepOnPose = new FloatPoint(sp.getFloat(STEP_ON_X, C.DEFAULT_FLOAT_VALUE),sp.getFloat(STEP_ON_Y,C.DEFAULT_FLOAT_VALUE),sp.getFloat(STEP_ON_T,C.DEFAULT_FLOAT_VALUE));
    }

    // Update current position of robot in the map, stored in shared prefs
    public void updateLocation(Destination lastKnownPosition) {
        Log.d(TAG, "updateLocation: " + lastKnownPosition.x + ", " + lastKnownPosition.y);
        SharedPreferences.Editor editor = getSharedPreferences(CONFIG_LOCAL_FILE, Context.MODE_PRIVATE).edit();
//        C.log(deviceId,mqttHelper.mqttAndroidClient,C.L2S_ADMIN_POSE,lastKnownPosition.x+":"+lastKnownPosition.y);

        Destination tmp = new Destination();
        tmp.x = lastKnownPosition.x;
        tmp.y = lastKnownPosition.y;

        this.lastKnownLocation = tmp;
        editor.putInt(C.X_COORD, tmp.x);
        editor.putInt(C.Y_COORD, tmp.y);
        editor.apply();
    }

    // Update the home location
    public void updateHomeLocation(Destination homeDestination) {
        SharedPreferences.Editor editor = getSharedPreferences(CONFIG_LOCAL_FILE, Context.MODE_PRIVATE).edit();
        this.homeDestination = homeDestination;
        editor.putInt(X_HOME, homeDestination.x);
        editor.putInt(Y_HOME, homeDestination.y);
        editor.apply();
    }

    // Update the map in shared prefs to the one received (updated)
    public void updateMap(String map) {
        mapData = map;
        getSharedPreferences(CONFIG_LOCAL_FILE, Context.MODE_PRIVATE).edit().putString(MAP_DATA, mapData).apply();
    }

    // Update the clientID in shared prefs
    public void updateClientId(String id) {
        clientId = id;
        getSharedPreferences(CONFIG_LOCAL_FILE, Context.MODE_PRIVATE).edit().putString(CLIENT_ID, clientId).apply();
    }

    // Update current state in shared prefs
    public void updateCurrentState(int state){
        currentState = state;
        getSharedPreferences(CONFIG_LOCAL_FILE, Context.MODE_PRIVATE).edit().putInt(CURRENT_STATE, state).apply();
    }

    // Update current mode in shared prefs
    public void updateCurrentMode(int mode){
        this.currentMode = mode;
        getSharedPreferences(CONFIG_LOCAL_FILE, Context.MODE_PRIVATE).edit().putInt(CURRENT_MODE, mode).apply();
    }
    public void updateStepOnPose(FloatPoint pose){
        SharedPreferences.Editor editor = getSharedPreferences(CONFIG_LOCAL_FILE, Context.MODE_PRIVATE).edit();
        this.stepOnPose = pose;
        editor.putFloat(STEP_ON_X, stepOnPose.x);
        editor.putFloat(STEP_ON_Y, stepOnPose.y);
        editor.putFloat(STEP_ON_T, stepOnPose.thetha);
        editor.apply();
    }

}
