package cmp491.loomo_app.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.segway.robot.algo.Pose2D;

import cmp491.loomo_app.Android.LoomoApplication;
import cmp491.loomo_app.Helpers.C;
import cmp491.loomo_app.Helpers.FloatPoint;
import cmp491.loomo_app.Navigation.Destination;
import cmp491.loomo_app.Navigation.Routing;

import static com.segway.robot.sdk.base.action.RobotAction.ActionEvent.LIFT_UP;
import static com.segway.robot.sdk.base.action.RobotAction.ActionEvent.PUSHING;
import static com.segway.robot.sdk.base.action.RobotAction.ActionEvent.PUSH_RELEASE;
import static com.segway.robot.sdk.base.action.RobotAction.ActionEvent.PUT_DOWN;
import static com.segway.robot.sdk.base.action.RobotAction.ActionEvent.STEP_OFF;
import static com.segway.robot.sdk.base.action.RobotAction.ActionEvent.STEP_ON;
import static com.segway.robot.sdk.base.action.RobotAction.TransformEvent.ROBOT_MODE;
import static com.segway.robot.sdk.base.action.RobotAction.TransformEvent.SBV_MODE;

public class StateBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = "StateBroadcastReceiver_Tag";
    private LoomoApplication application;
    private LoomoBaseService loomoBaseService;
    private LoomoSpeakService loomoSpeakService;
    private Boolean isAway;

    public Boolean getAway() {
        return isAway;
    }

    public void setAway(Boolean away) {
        isAway = away;
    }

    public StateBroadcastReceiver(LoomoApplication application, LoomoBaseService loomoBaseService, LoomoSpeakService loomoSpeakService) {
        this.application = application;
        this.loomoBaseService = loomoBaseService;
        this.loomoSpeakService = loomoSpeakService;
        isAway = false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_LOW)) {
            application.currentRoute.startJourney(C.JOURNEY_GOING_TO_HOME);
            loomoSpeakService.speak("Sorry to cut this short. Battery low. Will go home now", "");
        }
        if (action.equalsIgnoreCase(STEP_ON)) {
            Pose2D pose2D = loomoBaseService.mBase.getOdometryPose(-1);
            Pose2D pose2D1 = loomoBaseService.mBase.getVLSPose(-1);
            //isAway = true;
            //SampleFunctions.testVLSPoseListenerOnServer(application,pose2D.getX(),pose2D.getY(),pose2D.getTheta(),"before stepping on");
            //SampleFunctions.testVLSPoseListenerOnServer(application,pose2D1.getX(),pose2D1.getY(),pose2D1.getTheta(),"before stepping on vls");
            application.updateStepOnPose(new FloatPoint(pose2D.getX(), pose2D.getY(), pose2D.getTheta()));
        } else if (action.equalsIgnoreCase(STEP_OFF)) {
            Pose2D newPose = loomoBaseService.mBase.getOdometryPose(-1);
            calculateLastLocation(application.stepOnPose, new FloatPoint(newPose.getX(), newPose.getY(), newPose.getTheta()));
            application.updateStepOnPose(new FloatPoint(C.DEFAULT_FLOAT_VALUE, C.DEFAULT_FLOAT_VALUE, C.DEFAULT_FLOAT_VALUE));
        } else {
            Log.d(TAG, "Unknown action: " + action);
        }
    }

    // calculate location from 2 given points
    // supposed to update location of loom in ride mode
    private void calculateLastLocation(FloatPoint from, FloatPoint to) {
        int dx = Math.round(to.x - from.x);
        Log.d(TAG, String.valueOf(dx));
        int dy = Math.round(to.y - from.y);
        Log.d(TAG, "calculateLastLocation: " + dx + "  " + dy);
        Destination updatedLoc = new Destination();
        updatedLoc.x = application.lastKnownLocation.x + dx;
        updatedLoc.y = application.lastKnownLocation.y + dy;
        updatedLoc.thetha = application.lastKnownLocation.thetha;
        application.updateLocation(updatedLoc);
    }
}
