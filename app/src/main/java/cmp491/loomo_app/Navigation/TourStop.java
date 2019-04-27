package cmp491.loomo_app.Navigation;

public class TourStop {
    public String destName;
    public String speech;
    public int order;

    public TourStop() {
    }

    public TourStop(String destName, String speech, int order) {
        this.destName = destName;
        this.speech = speech;
        this.order = order;
    }
}
