package cmp491.loomo_app.Helpers;

import android.util.Log;

import java.util.ArrayList;

// Class used to filter the VLS Poses from the listener
public class Counter {
    private static final String TAG = "Counter_Tag";
    public int count = 0;
    public int frequency = 30; // the higher the value, the less readings are sent
    public ArrayList<Float> x, y, theta, velocity, w;

    public int increment() { return ++count; }

    public Counter(){
        x = new ArrayList<>();
        y = new ArrayList<>();
        theta = new ArrayList<>();
        velocity = new ArrayList<>();
        w = new ArrayList<>();
    }

    // Add pose information to the arrays
    public void addToArrays (float x, float y, float thetha, float velocity, float w) {
        try {
            this.x.add(x);
            this.y.add(y);
            theta.add(thetha);
            this.velocity.add(velocity);
            this.w.add(w);
        }catch(Exception e){
            Log.d("Counter", "addToArrays: "+e.getMessage());
        }
    }
    public void addToArrays (float x) {
        try {
            this.x.add(x);
        }catch(Exception e){
            Log.d(TAG, "addToArrays: " + e.getMessage());
        }
    }

    // Average of given list
    private float average(ArrayList<Float> x) {
        float sum = 0;
        int size = x.size();
        for (Float f: x)
            sum += f;
        x.clear();
        return sum / size;
    }

    // Average the values over a certain time period
    public float[] getAverages(){
        return new float[]{average(x),
                average(y),
                average(theta),
                average(velocity),
                average(w)};
    }

    // Average of x values
    public float getAverage(){
        count = 1;
        return average(x);
    }
}
