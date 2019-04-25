package cmp491.loomo_app.Helpers;

import android.util.Log;

import com.segway.robot.algo.Pose2D;

import java.util.ArrayList;

import cmp491.loomo_app.Navigation.Destination;

public class MovementRules {
    private static final String TAG = "MovementRules_Tag";

    public static FloatPoint serverToLoomo(float x, float y, double cellSize){
        float x_server = (((1.0f/C.UNIT_TO_CM) * (float)cellSize * x));
        float y_server = (((1.0f/C.UNIT_TO_CM) * (float)cellSize * y));
        return new FloatPoint(x_server, y_server);
    }

    public static float findDirection(int dx,int dy){
        float directions[] = {C.DIRECTION_EAST, C.DIRECTION_NORTH, C.DIRECTION_NORTH_EAST, C.DIRECTION_NORTH_WEST, C.DIRECTION_SOUTH1, C.DIRECTION_WEST, C.DIRECTION_SOUTH2, C.DIRECTION_SOUTH_EAST, C.DIRECTION_SOUTH_WEST};
        switch(dx){
            case -1:
                switch(dy){
                    case -1:
                        return C.DIRECTION_SOUTH_WEST;
                    case 0:
                        return C.DIRECTION_SOUTH1;
                    case 1:
                        return C.DIRECTION_SOUTH_EAST;
                }
                break;
            case 0:
                switch(dy){
                    case -1:
                        return C.DIRECTION_EAST;
                    case 0:
                        return C.DEFAULT_FLOAT_VALUE;
                    case 1:
                        return C.DIRECTION_WEST;
                }
                break;
            case 1:
                switch(dy){
                    case -1:
                        return C.DIRECTION_NORTH_WEST;
                    case 0:
                        return C.DIRECTION_NORTH;
                    case 1:
                        return C.DIRECTION_NORTH_EAST;
                }
                break;
        }
        return dx;
    }

    public static ArrayList<Destination> getObstacleLocation(float dir, int sensor){
        ArrayList<Destination> res = new ArrayList<>();
        switch(sensor) {
            case C.SENSOR_RIGHT:
                if (dir == C.DIRECTION_NORTH_EAST) {
                    res.add(new Destination(0,-1));
                    res.add(new Destination(-1,-1));
                } else if (dir == C.DIRECTION_NORTH) {
                    res.add(new Destination(0,-1));
                } else if (dir == C.DIRECTION_NORTH_WEST) {
                    res.add(new Destination(1,0));
                    res.add(new Destination(1,-1));
                } else if (dir == C.DIRECTION_WEST) {
                    res.add(new Destination(1,0));
                } else if (dir == C.DEFAULT_FLOAT_VALUE) {
                } else if (dir == C.DIRECTION_EAST) {
                    res.add(new Destination(-1,0));
                } else if (dir == C.DIRECTION_SOUTH_WEST) {
                    res.add(new Destination(1,1));
                    res.add(new Destination(0,1));
                } else if (dir == C.DIRECTION_SOUTH1) {
                    res.add(new Destination(0,1));
                } else {
                    res.add(new Destination(-1,1));
                    res.add(new Destination(-1,0));//
                }
                break;
            case C.SENSOR_LEFT:
                if (dir == C.DIRECTION_NORTH_EAST) {
                    res.add(new Destination(1,0));
                    res.add(new Destination(1,1));
                } else if (dir == C.DIRECTION_NORTH) {
                    res.add(new Destination(0,1));
                } else if (dir == C.DIRECTION_NORTH_WEST) {
                    res.add(new Destination(0,1));
                    res.add(new Destination(-1,1));
                } else if (dir == C.DIRECTION_WEST) {
                    res.add(new Destination(-1,0));
                } else if (dir == C.DEFAULT_FLOAT_VALUE) {
                } else if (dir == C.DIRECTION_EAST) {
                    res.add(new Destination(1,0));
                } else if (dir == C.DIRECTION_SOUTH_WEST) {
                    res.add(new Destination(-1,0));
                    res.add(new Destination(-1,-1));
                } else if (dir == C.DIRECTION_SOUTH1) {
                    res.add(new Destination(0,-1));
                } else {
                    res.add(new Destination(1,-1));
                    res.add(new Destination(0,-1));
                }
                break;
        }
        return null;
    }

    public static Pose2D moveDirection(float direction, float MOVEMENT_UNIT, Pose2D loomoPosition){
        float xf = 0, yf = 0, dx = 0, dy = 0;
        Log.d(TAG, "moveDirection, position before: "+loomoPosition.getX()+":"+loomoPosition.getY()+":"+loomoPosition.getTheta());
        dx = (float)Math.cos(direction) * MOVEMENT_UNIT;
        //dx = (float)Math.cos(direction+loomoPosition.getTheta()) * MOVEMENT_UNIT;
        dy = (float)Math.sin(direction) * MOVEMENT_UNIT;
        //dy = (float)Math.sin(direction+loomoPosition.getTheta()) * MOVEMENT_UNIT;
        xf = loomoPosition.getX() + dx;
        yf = loomoPosition.getY() - dy;
        Log.d(TAG, "moveDirection, position after: "+xf+":"+yf+":"+loomoPosition.getTheta()+direction);
        return new Pose2D(xf, yf, loomoPosition.getTheta(),0,0,0);
    }
}
