package cmp491.loomo_app.Navigation;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cmp491.loomo_app.Android.LoomoMain;
import cmp491.loomo_app.Android.LoomoApplication;
import cmp491.loomo_app.Helpers.C;
import cmp491.loomo_app.Helpers.FloatPoint;
import cmp491.loomo_app.Helpers.MovementRules;

public class Routing {
    private Queue<AStarCheckPoint> checkPoints;
    private AStarCheckPoint destinationCheckPoint;
    private Destination targetDestination;
    private LoomoMain activity;
    private LoomoApplication application;
    private AStarCheckPoint currentCheckPoint;
    private boolean isStarted = false;
    private int journeyType = -1; // When -1 it is not moving
    public static final String TAG = "Routing_Tag";

    public Routing(LoomoApplication application, LoomoMain activity) {
        this.application = application;
        this.activity = activity;
    }

    public Routing(LoomoApplication application) {
        this.application = application;
    }

    public LoomoApplication getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = (LoomoApplication) application;
    }

    public LoomoMain getActivity() {
        return activity;
    }

    public void setActivity(LoomoMain activity) {
        this.activity = activity;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public int getJourneyType() throws NullPointerException {
        if (journeyType == -1)
            throw new NullPointerException();
        else
            return journeyType;
    }

    public void setJourneyType(int journeyType) {
        this.journeyType = journeyType;
    }

    public Destination getTargetDestination() {
        return targetDestination;
    }

    public void setTargetDestination(Destination targetDestination) {
        this.targetDestination = targetDestination;
    }

    public AStarCheckPoint getCurrentCheckPoint() {
        return currentCheckPoint;
    }

    public void setCurrentCheckPoint(AStarCheckPoint currentCheckPoint) {
        this.currentCheckPoint = currentCheckPoint;
    }

    public void setCheckPoints(List<AStarCheckPoint> checkPoints) {
        this.checkPoints = new LinkedList<>(checkPoints);
        if (this.checkPoints.size() != 0)
            this.destinationCheckPoint = checkPoints.get(this.checkPoints.size() - 1);
    }

    public AStarCheckPoint getNextCheckPoint() throws Exception {
        currentCheckPoint = this.checkPoints.remove();
        return currentCheckPoint;
    }

    public void startJourney(final int journeyType) {
        this.journeyType = journeyType;
//        application.obstCounter = 1;

        activity.loomoBaseService.clearCheckPoints();
        Destination from = new Destination();

        if(application.lastKnownLocation.x != -100) {
            from.x = application.lastKnownLocation.x;
            from.y = application.lastKnownLocation.y;
            from.thetha = application.lastKnownLocation.thetha;
        }else{
            from.x = application.homeDestination.x;
            from.y = application.homeDestination.y;
            from.thetha = application.homeDestination.thetha;
        }

        List<AStarCheckPoint> route = new ArrayList<>();

        // Calculating the route and setting it locally in this class
        if(journeyType == C.JOURNEY_GOING_TO_USER) {
            route = application.loomoMap.calcRoute(
                    from.x,
                    from.y,
                    application.userDestination.x,
                    application.userDestination.y);
        } else if(journeyType == C.JOURNEY_GOING_TO_DESTINATION) {
            route = application.loomoMap.calcRoute(
                    from.x,
                    from.y,
                    application.targetDestination.x,
                    application.targetDestination.y);
        } else if(journeyType == C.JOURNEY_GOING_TO_HOME) {
            route = application.loomoMap.calcRoute(
                    from.x,
                    from.y,
                    application.homeDestination.x,
                    application.homeDestination.y);
        }
        setCheckPoints(route);

        // Logging the checkpoints
        StringBuilder logging = new StringBuilder();
        logging.append("Checkpoints list size: " + route.size());
        logging.append("Loomo route:\n");

        for (AStarCheckPoint checkpoint : route) {
            logging.append(checkpoint.getX() + ", " + checkpoint.getY() + "\n");
        }
        Log.d(TAG, "startJourney: " + logging);

        moveToNextCheckpoint(from);
        setStarted(true);
    }

    public void moveToNextCheckpoint(Destination from){
        // Updating current location and moving through the checkpoints
        application.updateLocation(from);
        try {
            currentCheckPoint = getNextCheckPoint();
        } catch (Exception e) {
            e.printStackTrace();
            endJourney();
        }
        FloatPoint fp_target = MovementRules.serverToLoomo(currentCheckPoint.getX(), currentCheckPoint.getY(), application.loomoMap.getCellSize());
        FloatPoint fp_home = MovementRules.serverToLoomo(application.homeDestination.x, application.homeDestination.y, application.loomoMap.getCellSize());
        activity.loomoBaseService.moveRobot((float) fp_target.x - fp_home.x, (float) fp_target.y - fp_home.y);
    }

    public void endJourney(){
        Log.d(TAG, "endJourney: "+application.lastKnownLocation.x+", "+application.lastKnownLocation.y);
        switch(getJourneyType()){
            case C.JOURNEY_GOING_TO_USER:
                C.publishSimpleMessage(application.clientId,application.deviceId,application.mqttHelper.mqttAndroidClient, C.L2S_ARRIVED_TO_USER);
                updateUserAndLoomoUI(C.BOUND_JOURNEY_STARTABLE,"Hi, My name is Loomo, and I am here to help you with your " + C.intModeToString(application.currentMode)+" journey","");
                break;
            case C.JOURNEY_GOING_TO_DESTINATION:
                activity.loomoSpeakService.speak("I have arrived at the destination. It has been a pleasure guiding you","");
                C.publishSimpleMessage(application.clientId,application.deviceId,application.mqttHelper.mqttAndroidClient, C.L2S_END_JOURNEY);
                updateUserAndLoomoUI(C.BOUND_DISMISSABLE,"Please dismiss me through the mobile app or tell me to go away",C.UTTERANCE_LOOMO_JOURNEY_DESTINATION_OVER);
                break;
            case C.JOURNEY_GOING_TO_HOME:
                C.publishSimpleMessage(application.clientId,application.deviceId,application.mqttHelper.mqttAndroidClient,C.L2S_REACHED_HOME);
                updateUserAndLoomoUI(C.UNBOUND_AVAILABLE,"I have reached my home. I will rest now","");
                break;
            case C.JOURNEY_DOING_TOUR:
                C.publishSimpleMessage(application.clientId,application.deviceId,application.mqttHelper.mqttAndroidClient, C.L2S_END_JOURNEY);
                updateUserAndLoomoUI(C.BOUND_DISMISSABLE,"Hope you enjoyed your tour. Please dismiss me through the mobile app or tell me to go away",C.UTTERANCE_LOOMO_JOURNEY_DESTINATION_OVER);
                break;
        }
        application.loomoMap.removeTempObstacles();
        setStarted(false);
        setJourneyType(-1);
    }

    private void updateUserAndLoomoUI(final int state, String message, String utteranceID){
        activity.loomoSpeakService.speak(message, utteranceID);
        activity.switchState(state);
    }
}
