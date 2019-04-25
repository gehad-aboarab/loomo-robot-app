package cmp491.loomo_app.Services;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.PoseVLS;
import com.segway.robot.algo.VLSPoseListener;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.algo.minicontroller.ObstacleStateChangedListener;
import com.segway.robot.sdk.base.bind.ServiceBinder.BindStateListener;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.locomotion.sbv.IBase;
import com.segway.robot.sdk.locomotion.sbv.StartVLSListener;
import com.segway.robot.sdk.locomotion.sbv.UltrasonicData;

import cmp491.loomo_app.Android.LoomoApplication;
import cmp491.loomo_app.Helpers.C;
import cmp491.loomo_app.Helpers.Counter;
import cmp491.loomo_app.Navigation.Destination;

public class LoomoBaseService {
    private static final String TAG = "LoomoBaseService_Tag";
    private Context context;
    private LoomoApplication application;
    public Base mBase;
    private ServiceInteractionListener mListener;
    private float THETHA_SHIFT = 0.0f;

    // Constructor
    public LoomoBaseService(Context context, ServiceInteractionListener mListener) {
        this.context = context;
        this.mListener = mListener;
        init();
    }

    // Constructor
    public LoomoBaseService(Application application, ServiceInteractionListener mListener) {
        this.application = (LoomoApplication) application;
        this.context = application.getApplicationContext();
        this.mListener = mListener;
        init();
    }

    // Unbind the service
    public void disconnect() {
        mBase.stopVLS();
        mBase.clearCheckPointsAndStop();
        mBase.stop();
        mBase.unbindService();
        Log.d(TAG, "Disconnect: " + mBase.isVLSStarted());
    }

    // Reconnect the service
    public void reconnect() {
        init();
    }

    //Instantiate mBase
    //Instantiate checkPoints list
    // Bind the service
    public void init() {
        // Instantiate the mBase
        mBase = Base.getInstance();
        if (mBase.isBind()) {
            mBase.unbindService();
        }
        // Bind service and instantiate checkpoints list
        mBase.bindService(context, new BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "onBind");
                setCheckPointListener();
                if (mListener != null)
                    mListener.onServiceInteraction(C.CALLBACK_BIND, null);
//                cleanAndResetOriginalPose(3,0,0);
//                moveRobot(1,0);
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "onUnbind: " + reason);
                if (mListener != null)
                    mListener.onServiceInteraction(C.CALLBACK_UNBIND, null);
            }
        });
    }

    //============================INTERFACE===========================================
    // Interface to create listeners for an activity using this service
    public interface ServiceInteractionListener {
        void onServiceInteraction(int callbackCode, Object[] params);
    }

    public void setCheckPointListener() {
        mBase.setOnCheckPointArrivedListener(new CheckPointStateListener() {
            @Override
            public void onCheckPointArrived(CheckPoint checkPoint, Pose2D realPose, boolean isLast) {
                if (mListener != null) {
                    if (isLast)
                        mListener.onServiceInteraction(C.CALLBACK_CHECKPOINT_ARRIVED_LAST, new Object[]{checkPoint, realPose});
                    else
                        mListener.onServiceInteraction(C.CALLBACK_CHECKPOINT_ARRIVED, new Object[]{checkPoint, realPose});
                }
            }

            @Override
            public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {
                Log.d(TAG, "onCheckPointMiss: " + reason);
                if (mListener != null) {
                    if (isLast)
                        mListener.onServiceInteraction(C.CALLBACK_CHECKPOINT_MISSED_LAST, new Object[]{checkPoint, realPose});
                    else
                        mListener.onServiceInteraction(C.CALLBACK_CHECKPOINT_MISSED, new Object[]{checkPoint, realPose});
                }
            }
        });
    }

    //Return Loomo's linear speed
    public float getLinearSpeed() {
        return mBase.getLinearVelocity().getSpeed();
    }

    //Return Loomo's angular speed
    public float getAngularSpeed() {
        return mBase.getAngularVelocity().getSpeed();
    }

    //Set the base control mode
    //Control Modes:
    //CONTROL_MODE_RAW
    //CONTROL_MODE_NAVIGATION --> The one we will be using
    //CONTROL_MODE_FOLLOW_TARGET
    public void setControlMode(int controlMode) {
        mBase.setControlMode(controlMode);
    }

    //================================Obstacle Avoidance================================

    public boolean isObstacleDetectionStarted() {
        return mBase.isUltrasonicObstacleAvoidanceEnabled();
    }

    public void setObstacleListener() {
        Log.d(TAG, "setObstacleListener: ");
        Log.d(TAG, "onObstacleStateChanged: "+mBase.isUltrasonicObstacleAvoidanceEnabled()+" "+mBase.getUltrasonicObstacleAvoidanceDistance());
        mBase.setObstacleStateChangeListener(new ObstacleStateChangedListener() {
            @Override
            public void onObstacleStateChanged(int ObstacleAppearance) {
                Log.d(TAG, "ObstacleChanged: " + ObstacleAppearance);
                if (mListener != null)
                    mListener.onServiceInteraction(C.CALLBACK_OBSTACLE_STATE_CHANGED, new Object[]{ObstacleAppearance});
            }
        });
    }

    // Called when it DETECTS obstacles, robot may not stop
    public void setObstacleDetection(boolean value) {
        mBase.setUltrasonicObstacleAvoidanceEnabled(value);
    }

    // Robot STOPS when it reaches this distance
    public void setObstacleDetectionDistance(float value) {
        mBase.setUltrasonicObstacleAvoidanceDistance(value);
    }

    // Get Robot ultrasonic data
    public UltrasonicData getUltrasonicData(){
        return mBase.getUltrasonicDistance();
    }

    //================================VLS================================
    // Start VLS if not already started
    public void startVLSNavigation() {
        if (mBase.isVLSStarted()) {
            Log.d(TAG, "VLS already started, stopping VLS");
            mBase.stopVLS();
        }
        mBase.startVLS(true, true, new StartVLSListener() {
            @Override
            public void onOpened() {
                Log.d(TAG, "VLS onOpened");
                // Can be either from VLS or from Odom, depending to navigation method used
                mBase.setNavigationDataSource(Base.NAVIGATION_SOURCE_TYPE_VLS);
                setVLSPoseListener();

                if (mListener != null)
                    mListener.onServiceInteraction(C.CALLBACK_VLS_START, null);
            }
            @Override
            public void onError(String errorMessage) {
                if (mListener != null)
                    mListener.onServiceInteraction(C.CALLBACK_VLS_ERROR, null);
                Log.d(TAG, "VLS onError: " + errorMessage);
            }
        });
    }

    // Clear all the checkpoints stored in Loomo and put Loomo to stop
    public void stopVLSNavigation() {
        Log.d(TAG, "VLS navigation stopped");
        mBase.clearCheckPointsAndStop();
    }

    // Stop the VLS service on Loomo
    public void stopVLS() {
        Log.d(TAG, "VLS stopped");
        if (mBase.isVLSStarted())
            mBase.stopVLS();
    }

    public void setVLSPoseListener() {
        final Counter c = new Counter();
        Log.d(TAG, "Latest odom pose: " + mBase.getOdometryPose(-1));
        Log.d(TAG, "Latest VLS pose: " + mBase.getVLSPose(-1));
        mBase.setVLSPoseListener(new VLSPoseListener() {
            @Override
            public void onVLSPoseUpdate(long timestamp, float pose_x, float pose_y, float pose_theta, float v, float w) {
                c.addToArrays(pose_x, pose_y, pose_theta, v, w);
                if (c.increment() % c.frequency == 0) {
                    if (mListener != null) {
                        float[] res = c.getAverages();
                        mListener.onServiceInteraction(C.CALLBACK_VLS_POSE_UPDATE, new Object[]{res[0], res[1], res[2], res[3], res[4]});
                    }
                }
            }
        });
    }

    //================================Robot Movement================================
    // Reset the origin pose of the VLS to Loomo's current pose
    public void cleanAndResetOriginalPose(){
        if(mBase == null)
            Log.d(TAG, "Null mBase");
        mBase.cleanOriginalPoint();
        mBase.setOriginalPoint(getLoomoPose());
    }

    // Reset the origin point of the VLS to our given coordinate
    public void cleanAndResetOriginalPose(float x, float y, float thetha){
        if(mBase == null)
            Log.d(TAG, "Null mBase");
        mBase.cleanOriginalPoint();
        mBase.setOriginalPoint(new Pose2D(x,y,thetha,0,0,-1));
    }

    // Move robot with theta
    public void moveRobot(float x, float y, float thetha) {
        mBase.addCheckPoint(x, y, thetha);
    }

    // Move robot without theta
    public void moveRobot(float x, float y) {
        mBase.addCheckPoint(x, y);
    }

    // Get position of robot on VLS map
    public Pose2D getLoomoPose(){
        Pose2D pose = mBase.getVLSPose(-1);
        if(pose.getTimestamp()==0 || pose.getTimestamp()==-1){
            pose = mBase.getOdometryPose(-1);
        }
//        if(application != null)
//            SampleFunctions.testVLSPoseListenerOnServer((LoomoApplication)application,
//                    pose.getX(),
//                    pose.getY(),
//                    pose.getTheta(),
//                    "getLoomoPose: " + pose.getTimestamp());
        return pose;
    }

    // Clear checkpoints and stop, same as stopVLSNavigation()
    public void clearCheckPoints(){
        mBase.clearCheckPointsAndStop();
    }

    //================================Odometry================================

    // Start odometry navigation
    public void startOdomNavigation() {
        mBase.setNavigationDataSource(Base.NAVIGATION_SOURCE_TYPE_ODOM);
        // Clear checkpoints
        mBase.cleanOriginalPoint();
        // Set current pose as origin
        Pose2D pose2D = mBase.getOdometryPose(-1);
        mBase.setOriginalPoint(pose2D);
    }

    //Stop odometry navigation, same as stopVLSNavigation()
    public void stopOdomNavigation() {
        mBase.clearCheckPointsAndStop();
    }
}
