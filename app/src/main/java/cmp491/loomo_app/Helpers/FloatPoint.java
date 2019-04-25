package cmp491.loomo_app.Helpers;

public class FloatPoint {
    public static final String TAG = "FloatPoint_Tag";
    public float x;
    public float y;
    public float thetha;

    public FloatPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public FloatPoint(float x, float y, float thetha) {
        this.x = x;
        this.y = y;
        this.thetha = thetha;
    }
}