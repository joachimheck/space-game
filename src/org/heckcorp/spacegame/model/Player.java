package org.heckcorp.spacegame.model;

import java.io.Serializable;

public class Player implements Serializable {
  public Color getColor() {
    return color;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public Player(String name, Type type, double r, double g, double b) {
    this.name = name;
    this.type = type;
    this.color = new Color(r, g, b);
  }

  private final Color color;
  private final String name;
  private final Type type;

  public record Color(double r, double g, double b) {}

  public enum Type {
    COMPUTER,
    HUMAN
  }
}
