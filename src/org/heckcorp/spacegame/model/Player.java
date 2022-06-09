package org.heckcorp.spacegame.model;

import java.io.Serializable;

public class Player implements Serializable {
  public record Color(double r, double g, double b) {}

  public Color getColor() {
    return color;
  }

  public String getName() {
    return name;
  }

  public Player(String name, double r, double g, double b) {
    this.name = name;
    this.color = new Color(r, g, b);
  }

  private final Color color;
  private final String name;
}
