package cmp491.loomo_app.Android;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.segway.robot.sdk.locomotion.sbv.Base;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import cmp491.loomo_app.Helpers.C;
import cmp491.loomo_app.Helpers.FloatPoint;
import cmp491.loomo_app.Helpers.MovementRules;
import cmp491.loomo_app.Navigation.AStarCheckPoint;
import cmp491.loomo_app.Navigation.Destination;
import cmp491.loomo_app.Navigation.MapInitializer;
import cmp491.loomo_app.Navigation.Routing;
import cmp491.loomo_app.Services.LoomoBaseService;
import cmp491.loomo_app.Services.LoomoRecognitionService;
import cmp491.loomo_app.Services.LoomoSpeakService;
import cmp491.loomo_app.Services.ObstacleAvoidanceService;
import cmp491.loomo_app.Services.StateBroadcastReceiver;

import static com.segway.robot.sdk.base.action.RobotAction.ActionEvent.STEP_OFF;
import static com.segway.robot.sdk.base.action.RobotAction.ActionEvent.STEP_ON;

public class LoomoMain {
    private LoomoApplication application;
    public LoomoBaseService loomoBaseService;
    public LoomoSpeakService loomoSpeakService;
    public LoomoRecognitionService loomoRecognitionService;
    private ObstacleAvoidanceService obstacleAvoidanceService;
    private StateBroadcastReceiver stateBroadcastReceiver;
    private Context context;
    private static final String TAG = "LoomoMain_Tag";

    public LoomoMain(Application application) {
        this.application = (LoomoApplication) application;
        this.context = application.getApplicationContext();
        this.application.currentRoute.setActivity(this);
    }

    public void onStart() {
        this.loomoBaseService = new LoomoBaseService(application, baseListener);
        this.loomoSpeakService = new LoomoSpeakService(context, speakListener);
        this.loomoRecognitionService = new LoomoRecognitionService(context, recogListener);
//        obstacleAvoidanceService = new ObstacleAvoidanceService();
        if (application.mqttHelper.mqttAndroidClient.isConnected()) {
            getUpdatedMap();
        }
        startMqtt();
    }

    public void onStop() {
//        obstacleAvoidanceService.cancel(true);
    }

    // Gets the latest map JSON object, either from local storage or from server
    private void getUpdatedMap() {
        MqttMessage msg = new MqttMessage();
        JSONObject obj = new JSONObject();
        String mapData = application.mapData;
        try {
            obj.put("loomoID", application.deviceId);
            obj.put("mapName", LoomoApplication.MAP_NAME_SERVER);
            if (mapData != null) {
                JSONObject oldMap = new JSONObject(mapData);
                obj.put("timeStamp", oldMap.get("timeStamp"));
            }
            msg.setPayload(obj.toString().getBytes());
            application.mqttHelper.mqttAndroidClient.publish(C.L2S_GET_MAP, msg);
        } catch (JSONException | MqttException e) {
            Log.d(TAG, "hi: " + e.getMessage());
        }
    }

    // Listener that is called from the LoomoBaseService class
    private LoomoBaseService.ServiceInteractionListener baseListener = new LoomoBaseService.ServiceInteractionListener() {
        @Override
        public void onServiceInteraction(int callbackCode, Object[] params) {
            switch (callbackCode) {
                case C.CALLBACK_BIND:
                    loomoBaseService.setControlMode(Base.CONTROL_MODE_NAVIGATION);
                    loomoBaseService.startVLSNavigation();
//                    if (!loomoBaseService.isObstacleDetectionStarted()) {
//                        loomoBaseService.setObstacleDetection(true);
//                    }
//                    loomoBaseService.setObstacleDetectionDistance(0.25f);
//                    loomoBaseService.setObstacleListener();
                    break;
                case C.CALLBACK_VLS_START:
                    // connect to obstacleAvoid service
//                    if (!obstacleAvoidanceService.isCancelled())
//                        obstacleAvoidanceService.execute(loomoBaseService, application, loomoSpeakService);
                    loomoBaseService.cleanAndResetOriginalPose();
                    break;
                case C.CALLBACK_CHECKPOINT_ARRIVED:
                case C.CALLBACK_CHECKPOINT_ARRIVED_LAST:
//                        application.obstCounter++;
                    int lastX = application.currentRoute.getCurrentCheckPoint().getX();
                    int lastY = application.currentRoute.getCurrentCheckPoint().getY();

                    application.currentRoute.moveToNextCheckpoint(new Destination(lastX, lastY));
                    Log.d(TAG, "Loomo reached checkpoint");
                    break;
                case C.CALLBACK_VLS_POSE_UPDATE:
                    break;
                case C.CALLBACK_OBSTACLE_STATE_CHANGED:
                    break;
            }
        }
    };

    // Listener that is called from the LoomoRecogService class
    private LoomoRecognitionService.ServiceInteractionListener recogListener = new LoomoRecognitionService.ServiceInteractionListener() {
        @Override
        public void onServiceInteraction(int callbackCode, Object[] params) {
            switch (callbackCode) {
                case C.CALLBACK_BIND:
                    Log.d(TAG, "recog binded");
                    break;
                case C.CALLBACK_UNBIND:
                    Log.d(TAG, "recog unbinded");
                    break;
                case C.CALLBACK_WAKEUP_ERROR:
                    Log.d(TAG, "wakeup_err: " + (String) params[0]);
                    break;
                case C.CALLBACK_RECOGNITION_RESULT:
                    // Checking the recognition words
                    Log.d(TAG, "recog_res: " + (String) params[0]);
//                    Log.d(TAG, "recog_res: " + ":" + (int) params[1]);
                    if (application.currentState == C.UNBOUND_TOWARDS_STATION) {
                        loomoSpeakService.speak("You have already dismissed me", C.UTTERANCE_LOOMO_ALREADY_DISMISSED);
                    } else if (application.currentState == C.UNBOUND_AVAILABLE) {
                        loomoSpeakService.speak("I am available", C.UTTERANCE_LOOMO_ALREADY_DISMISSED);
                    } else if ((int) params[1] >= 50) {
                        if (Arrays.asList(C.DISMISS_RECOG_LIST).contains((String) params[0]))
                            mqttDismissLoomo(true);
                        else if (Arrays.asList(C.START_JOUR_RECOG_LIST).contains((String) params[0]))
                            mqttStartJourney();
                        else
                            loomoSpeakService.speak("You have said an invalid command", C.UTTERANCE_RECOG_ERROR);
                    } else
                        loomoSpeakService.speak("You have said an invalid command", C.UTTERANCE_RECOG_ERROR);
                    break;
                case C.CALLBACK_RECOGNITION_ERROR:
                    Log.d(TAG, "recog_err: " + (String) params[0]);
                    loomoSpeakService.speak("I did not understand what you said", C.UTTERANCE_RECOG_ERROR);
                    break;
            }
        }
    };

    // Listener that is called from the LoomoSpeakService class
    private LoomoSpeakService.ServiceInteractionListener speakListener = new LoomoSpeakService.ServiceInteractionListener() {
        @Override
        public void onServiceInteraction(int callbackCode, Object[] params) {
            switch (callbackCode) {
                case C.CALLBACK_BIND:
                    Log.d(TAG, "Speak binded");
                    break;
                case C.CALLBACK_UNBIND:
                    Log.d(TAG, "Speak unbinded");
                    break;
                case C.CALLBACK_TTS_INIT:
                    Log.d(TAG, "Speak initialized");
                    break;
                case C.CALLBACK_SPEAK_STARTED:
                    Log.d(TAG, "Speak started");
                    break;
                case C.CALLBACK_SPEAK_DONE:
                    Log.d(TAG, "Speak done");
                    switch ((String) params[0]) {
                        case C.UTTERANCE_LOOMO_JOURNEY_DESTINATION_OVER:
                            loomoRecognitionService.startRecognitionAndWakeup();
                            break;
                        case C.UTTERANCE_TOUR_STOP:
                            // go to the next tour stop
//                            try {
//                                application.currentTourStop = application.tour.getTourStop();
//                                C.log(application.mqttHelper.mqttAndroidClient,C.L2S_ADMIN_LOG,"Just spoke,before destName: "+application.currentTourStop.destName);
//                                Position to = application.loomoMap.getLandmarks().get(application.currentTourStop.destName);
//                                C.log(application.mqttHelper.mqttAndroidClient,C.L2S_ADMIN_LOG,"Just spoke,after destName: "+application.currentTourStop.destName);
//                                application.finalDestinationName = application.currentTourStop.destName;
//                                application.targetDestination = new Position();
//                                application.targetDestination.x = to.x;
//                                application.targetDestination.y = to.y;
//                                application.targetDestination.thetha = to.thetha;
//                                application.targetDestination.mode = C.GUIDE_MODE;
//                                Log.d(TAG, "onServiceInteraction: poss: " + to.x + to.y);
//                                startJourney(Route.JOURNEY_DOING_TOUR);
//                            } catch (Exception ex) {
//                                C.log(application.mqttHelper.mqttAndroidClient,C.L2S_ADMIN_LOG,"Tour has ended: "+ex.getMessage());
//                                endJourney();
//                            }
                            break;
                    }
                    break;
                case C.CALLBACK_SPEAK_ERROR:
                    Log.d(TAG, "Speak error");
                    break;
            }
        }
    };


    private void startMqtt() {
        application.mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.d(TAG, "connectComplete: mqtt connecteddd");
                getUpdatedMap();
            }

            @Override
            public void connectionLost(Throwable throwable) {
//                C.log(application.mqttHelper.mqttAndroidClient, C.L2S_ADMIN_LOG, "Loomo mqtt disconnected");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }

            //Whenever we receive a message from MQTT server
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w(TAG, mqttMessage.toString() + " : " + topic);
                JSONObject obj = new JSONObject(mqttMessage.toString());

                // test movement of loomo to a certain coordinate
                if (topic.equals("server-to-loomo/test-route")) {
//                    Log.d(TAG, "messageArrived: ");
//                    float x = (float) obj.getDouble("x");
//                    float y = (float) obj.getDouble("y");
//                    float t = (float) obj.getDouble("thetha");
//                    application.updateLoomoMode(C.GUIDE_MODE);
//                    Log.d(TAG, "deets");
//                    application.targetDestination = new Position((int) x, (int) y, t, C.GUIDE_MODE);
//                    startJourney(Route.JOURNEY_GOING_TO_USER);
                }
                if (topic.equals(C.S2L_RPI_SENSOR)) {
//                    Log.d(TAG, "messageArrived: hiiiiiiiiiiiiiiiiiii");
//                    application.rpiSensorFront = true;
//                } else if (topic.equalsIgnoreCase(C.S2L_RESET)) {
//                    resetLoomo();
                } else if (topic.equalsIgnoreCase(C.S2L_RESET_MAP)) {
//                    Log.d(TAG, "messageArrived: reset");
//                    getUpdatedMap();
                }
                // To check if the message is meant for this loomo only or not
                if (obj.getString("loomoID").equals(application.deviceId)) {
                    switch (topic) {
                        // Server is sending map
                        case C.S2L_SEND_MAP:
                            boolean isUpdated = obj.getBoolean("updated");

                            // If map is updated, store it in shared prefs
                            if (Boolean.valueOf(isUpdated)) {
                                application.updateMap(obj.getJSONObject("map").toString());
                            }

                            // Load the map from shared prefs
                            MapInitializer mapInitializer = new MapInitializer(application);
                            mapInitializer.load(application.mapData);

                            // Set home destination and update it in shared prefs
                            application.homeDestination = mapInitializer.getHomeDestination();
                            application.updateHomeLocation(application.homeDestination);

                            break;
                        case C.S2L_SEND_TOUR:
//                            try {
//                                application.tour = new LoomoTours(obj.getJSONObject("tour"),application);
//                            } catch (Exception e) {
//                                Log.d(TAG, "No tours available");
//                            }
                            break;
                        // If loomo called, switch state to unavailable and retrieve the user's coordinates
                        case C.S2L_LOOMO_CALLED:
                            Log.d(TAG, "current state when loomo called: " + application.currentState);
                            if (application.currentState == C.BOUND_TOWARDS_USER) {
                                Log.d(TAG, "current state when loomo called: " + application.currentState);
                                break;
                            }
                            // Update the state
                            application.updateCurrentState(C.BOUND_TOWARDS_USER);

                            // Get client Id
                            application.updateClientId(obj.getString("clientID"));

                            // Get and update mode
                            String mode = obj.getString("mode");
                            if (mode.equals("ride"))
                                application.updateCurrentMode(C.RIDE_MODE);

                            else if (mode.equals("guide")) {
                                application.updateCurrentMode(C.GUIDE_MODE);

                                // Get the destination coordinates
                                int destination_x = (int) Math.round(obj.getDouble(application.X_DESTINATION));
                                int destination_y = (int) Math.round(obj.getDouble(application.Y_DESTINATION));
                                float destination_t = (float) obj.getDouble(application.T_DESTINATION);
                                application.targetDestinationName = obj.getString("destination_name");
                                application.targetDestination = new Destination(destination_x, destination_y, destination_t);

                            } else {
                                application.updateCurrentMode(C.TOUR_MODE);
//                                application.currentTourStop = application.tour.getTourStop();
//                                Position to = application.loomoMap.getLandmarks().get(application.currentTourStop.destName);
//                                application.finalDestinationName = application.currentTourStop.destName;
//                                application.finalDestination = new Position(to.x, to.y, to.thetha, application.GUIDE_MODE);
                            }

                            // Get user's coordinates
                            int user_x = (int) Math.round(obj.getDouble(application.X_USER));
                            int user_y = (int) Math.round(obj.getDouble(application.Y_USER));
                            application.userDestination = new Destination(user_x, user_y, 0);

                            // Update the server about starting journey
                            mqttStartJourney();
                            break;

                        case C.S2L_START_JOURNEY:
                            if (application.currentState == C.BOUND_ONGOING_JOURNEY) {
                                break;
                            }
                            application.updateCurrentState(C.BOUND_ONGOING_JOURNEY);
                            mqttStartJourney();
                            break;
                        // If loomo dismissed, switch status to available and head back to station
                        case C.S2L_LOOMO_DISMISSED:
                            if (application.currentState == C.UNBOUND_TOWARDS_STATION) {
                                break;
                            }
                            application.updateCurrentState(C.UNBOUND_TOWARDS_STATION);
                            mqttDismissLoomo(false);
                            break;
                        case C.S2L_RPI_SENSOR:
//                            Log.d(TAG, "messageArrived: hiiiiiiiiiiiiiiiiiii");
//                            application.rpiSensorFront = true;
                            break;
                    }
                }
            }
        });

    }

    private void mqttStartJourney() {
        if (application.currentState == C.BOUND_TOWARDS_USER) {
            C.publishSimpleMessage(
                    application.clientId,
                    application.deviceId,
                    application.mqttHelper.mqttAndroidClient,
                    C.L2S_ROUTE_TO_USER);
            application.currentRoute.startJourney(C.JOURNEY_GOING_TO_USER);

        } else if (application.currentState == C.BOUND_ONGOING_JOURNEY) {
            C.publishSimpleMessage(
                    application.clientId,
                    application.deviceId,
                    application.mqttHelper.mqttAndroidClient,
                    C.L2S_STARTED_JOURNEY);

            if (application.currentMode == C.RIDE_MODE)
                loomoSpeakService.speak("Hop on me", "");
            else {
                loomoSpeakService.speak("Let us go to " + application.targetDestinationName, "");
                application.currentRoute.startJourney(C.JOURNEY_GOING_TO_DESTINATION);
            }
            // Update server on journey started towards destination
//            C.log(application.deviceId, application.mqttHelper.mqttAndroidClient, C.L2S_ADMIN_DEST, application.finalDestinationName);

        }

    }

    private void mqttDismissLoomo(boolean notifyMobile) {
        loomoSpeakService.speak("I am going to leave you now", C.UTTERANCE_LOOMO_DISMISS);
        if (notifyMobile)
            C.publishSimpleMessage(application.clientId,
                    application.deviceId, application.mqttHelper.mqttAndroidClient, C.L2S_LOOMO_DISMISSED);
        application.updateClientId(null);
//        application.targetDestination = application.homeDestination;
//        application.targetDestination.x = application.homeDestination.x;
//        application.targetDestination.y = application.homeDestination.y;
//        application.targetDestination.thetha = application.homeDestination.thetha;
        Log.d(TAG, "run: in recogresult");
        application.currentRoute.startJourney(C.JOURNEY_GOING_TO_HOME);
    }

    public void switchState(int state) {
        application.updateCurrentState(state);
    }

    // Function to initialize the broadcast receiver
    private void setBroadCastReceiver() {
        stateBroadcastReceiver = new StateBroadcastReceiver(application, loomoBaseService, loomoSpeakService);
        // add all the ones needed
        // you can find valid action strings at
        // https://developer.segwayrobotics.com/developer/documents/segway-robots-sdk.html#toc_9
        String[] strings = {STEP_ON, STEP_OFF, Intent.ACTION_BATTERY_LOW};
        IntentFilter filter = new IntentFilter();
        for (String s : strings)
            filter.addAction(s);
        application.registerReceiver(stateBroadcastReceiver, filter);
    }
}
