package cmp491.loomo_app.Navigation;

public class AStarCheckPoint {
    private static final int MOVEMENT_COST = 10;
    private int x;
    private int y;
    private boolean walkable;
    private AStarCheckPoint parent;
    private int g;
    private int h;

    public AStarCheckPoint(int x, int y, boolean walkable) {
        this.x = x;
        this.y = y;
        this.walkable = walkable;
    }

    public void setG(AStarCheckPoint parent) { g = (parent.getG() + MOVEMENT_COST); }
    public int calculateG(AStarCheckPoint parent) { return (parent.getG() + MOVEMENT_COST); }
    public void setH(AStarCheckPoint goal) { h = (Math.abs(getX() - goal.getX()) + Math.abs(getY() - goal.getY())) * MOVEMENT_COST; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public boolean isWalkable() { return walkable; }
    public void setWalkable(boolean walkable) { this.walkable = walkable; }
    public AStarCheckPoint getParent() { return parent; }
    public void setParent(AStarCheckPoint parent) { this.parent = parent; }
    public int getF() { return g + h; }
    public int getG() { return g; }
    public int getH() { return h; }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof AStarCheckPoint)) return false;
        if (o == this) return true;
        AStarCheckPoint n = (AStarCheckPoint) o;
        if (n.getX() == x && n.getY() == y && n.isWalkable() == walkable)
            return true;
        return false;
    }

    @Override
    public String toString() {
        return "AStarCheckPoint{" +
                "x=" + x +
                ", y=" + y +
                ", walkable=" + walkable +
                '}';
    }
}
