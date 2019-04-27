package cmp491.loomo_app.Helpers;

import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

/**
 * Created by Gehad on 4/24/2019.
 */

public class C {
    // Routing strings
    public static final String S2L_LOOMO_CALLED = "server-to-loomo/loomo-called";
    public static final String S2L_LOOMO_DISMISSED = "server-to-loomo/loomo-dismiss";
    public static final String S2L_SEND_MAP = "server-to-loomo/send-map";
    public static final String S2L_SEND_TOUR = "server-to-loomo/send-tour";
    public static final String S2L_RPI_SENSOR = "server-to-loomo/rpi-sensor";
    public static final String S2L_RESET= "server-to-loomo/reset";
    public static final String L2S_REACHED_HOME = "loomo-to-server/reached-home";
    public static final String L2S_BEACON_SIGNALS = "loomo-to-server/beacon-signals";
    public static final String L2S_ROUTE_TO_USER = "loomo-to-server/route-to-user";
    public static final String L2S_ARRIVED_TO_USER = "loomo-to-server/loomo-arrival";
    public static final String L2S_STARTED_JOURNEY = "loomo-to-server/started-journey";
    public static final String L2S_END_JOURNEY = "loomo-to-server/end-journey";
    public static final String S2L_START_JOURNEY = "server-to-loomo/start-journey";
    public static final String S2L_ERROR = "server-to-loomo/error";
    public static final String S2L_RESET_MAP = "server-to-loomo/reset-map";
    public static final String L2S_GET_MAP = "loomo-to-server/get-map";
    public static final String L2S_LOOMO_DISMISSED = "loomo-to-server/loomo-dismiss";
    public static final String L2S_ADMIN_LOG = "loomo-to-server-admin/log";
    public static final String L2S_ADMIN_POSE = "loomo-to-server-admin/pose";
    public static final String L2S_ADMIN_DEST = "loomo-to-server-admin/dest";

    // Modes
    public static final int GUIDE_MODE = 0;
    public static final int RIDE_MODE = 1;
    public static final int TOUR_MODE = 2;

    // Coordinates
    public static final String X_COORD = "x_coordinate";
    public static final String Y_COORD = "y_coordinate";
    public static final String THETHA = "thetha";

    // Loomo states
    public static final int UNBOUND_AVAILABLE = 400;
    public static final int UNBOUND_TOWARDS_STATION = 401;
    public static final int BOUND_TOWARDS_USER = 402;
    public static final int BOUND_JOURNEY_STARTABLE = 403;
    public static final int BOUND_ONGOING_JOURNEY = 404;
    public static final int BOUND_DISMISSABLE = 405;

    // Journey
    public static final int JOURNEY_GOING_TO_USER = 100;
    public static final int JOURNEY_GOING_TO_DESTINATION = 101;
    public static final int JOURNEY_GOING_TO_HOME = 102;
    public static final int JOURNEY_DOING_TOUR = 103;

    // Callback types
    public final static int CALLBACK_TEST = 5000;
    public final static int CALLBACK_BIND = 5001;
    public final static int CALLBACK_UNBIND = 5002;
    public final static int CALLBACK_CHECKPOINT_ARRIVED = 5003;
    public final static int CALLBACK_CHECKPOINT_ARRIVED_LAST = 5005;
    public final static int CALLBACK_CHECKPOINT_MISSED = 5004;
    public final static int CALLBACK_CHECKPOINT_MISSED_LAST = 5006;
    public final static int CALLBACK_OBSTACLE_STATE_CHANGED = 5007;
    public final static int CALLBACK_VLS_POSE_UPDATE = 5008;
    public final static int CALLBACK_VLS_START = 5009;
    public final static int CALLBACK_VLS_ERROR = 5010;
    public final static int CALLBACK_SPEAK_STARTED = 5011;
    public final static int CALLBACK_SPEAK_ERROR = 5012;
    public final static int CALLBACK_RECOGNITION_STARTED = 5013;
    public final static int CALLBACK_RECOGNITION_RESULT = 5014;
    public final static int CALLBACK_RECOGNITION_ERROR = 5015;
    public final static int CALLBACK_WAKEUP_RESULT = 5016;
    public final static int CALLBACK_WAKEUP_ERROR = 5017;
    public final static int CALLBACK_TTS_INIT = 5018;
    public final static int CALLBACK_SPEAK_DONE = 5019;

    // Landmark types
    public final static int LANDMARK_DOOR = 24;
    public final static int LANDMARK_FREE = 3;
    public final static int LANDMARK_BEACON = 25;
    public final static int LANDMARK_OBSTACLE = 26;
    public final static int LANDMARK_OTHER = 27;
    public final static int LANDMARK_HOME = 28;
    public final static int LANDMARK_DESTINATIONS = 29;
    public final static int LANDMARK_TEMP_OBSTACLE = 19;

    // Landmark appearance
    public final static int OBSTACLE_APPEARED = 1;
    public final static int OBSTACLE_DISAPPEARED = 0;

    // Directions
    public final static float DIRECTION_NORTH = 0;
    public final static float DIRECTION_NORTH_EAST = (float) Math.PI / 4.0f;
    public final static float DIRECTION_EAST = (float) Math.PI / 2.0f;
    public final static float DIRECTION_SOUTH_EAST = ((float) Math.PI) * (3.0f / 4.0f);
    public final static float DIRECTION_SOUTH1 = (float) Math.PI * -1.0f;
    public final static float DIRECTION_SOUTH2 = (float) Math.PI;
    public final static float DIRECTION_SOUTH_WEST = (((float) Math.PI) * (3.0f / 4.0f)) * -1.0f;
    public final static float DIRECTION_WEST = (((float) Math.PI) / 2.0f) * -1.0f;
    public final static float DIRECTION_NORTH_WEST = (((float) Math.PI) / 4.0f) * -1.0f;

    public final static float DIRECTION_RIGHT = DIRECTION_EAST;
    public final static float DIRECTION_LEFT = DIRECTION_WEST;
    public final static float DIRECTION_BACKWARD = DIRECTION_SOUTH1;
    public final static float DIRECTION_FORWARD = DIRECTION_NORTH;

    // Utterance strings
    public final static String UTTERANCE_RECOG_ERROR = "recogError";
    public final static String UTTERANCE_TOUR_STOP = "tourStop";
    public final static String UTTERANCE_LOOMO_DISMISS = "loomoDismiss";
    public final static String UTTERANCE_LOOMO_ALREADY_DISMISSED = "loomoAlreadyDismissed";
    public final static String UTTERANCE_LOOMO_JOURNEY_DESTINATION_OVER = "loomoJourDestOvr";

    // Recognition arrays
    public final static String[] DISMISS_RECOG_LIST = {"dismiss","go away","be gone","bye"};
    public final static String[] START_JOUR_RECOG_LIST = {"start journey","move"};

    // Sensors
    public static final int SENSOR_RIGHT =5890;
    public static final int SENSOR_LEFT =5891;
    public static final int SENSOR_FRONT =5892;

    // RPi
    public static final float OBSTACLE_ULTRASONIC_DIST_MM = 1000.0f;
    public static final int OBSTACLE_COUNTER_FREQUENCY = 200;
    public static final long SLEEP_MM = 3000;

    public static final float DEFAULT_FLOAT_VALUE = 100000;

    // How much 1.0x is in cm
    public final static float UNIT_TO_CM = 100.0f;

    public final static String TAG = "C_Tag";

    public static final void publishSimpleMessage(String clientID, String loomoID, MqttAndroidClient mqttAndroidClient, String topic) {
        MqttMessage msg = new MqttMessage();
        JSONObject responseObj = new JSONObject();
        try {
            responseObj.put("loomoID", loomoID);
            responseObj.put("clientID", clientID);
            msg.setPayload(responseObj.toString().getBytes());
            mqttAndroidClient.publish(topic, msg);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public static final String intModeToString(int mode){ return new String[]{"guide","ride","tour"}[mode]; }
}
