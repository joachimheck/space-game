package org.heckcorp.spacegame.map;

public record Point(int x, int y) {
    public double distance(Point o) {
        return Math.sqrt(Math.abs(x - o.x()) + Math.abs(y - o.y()));
    }
}
