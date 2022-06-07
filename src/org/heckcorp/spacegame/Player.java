package org.heckcorp.spacegame;

import java.io.Serializable;

public class Player implements Serializable {
    public record Color(double r, double g, double b) { }

    public Color getColor() {
        return color;
    }

    public Player(double r, double g, double b) {
        this.color = new Color(r, g, b);
    }

    private final Color color;
}
