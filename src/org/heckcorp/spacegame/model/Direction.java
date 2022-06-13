package org.heckcorp.spacegame.model;

public enum Direction {
  NORTH(0),
  NORTHEAST(1),
  SOUTHEAST(2),
  SOUTH(3),
  SOUTHWEST(4),
  NORTHWEST(5);

  public int getDirection() {
    return direction;
  }

  public Direction left() {
    return values[(this.ordinal() + values.length - 1) % values.length];
  }

  public Direction right() {
    return values[(this.ordinal() + 1) % values.length];
  }

  Direction(int direction) {
    this.direction = direction;
  }

  private static final Direction[] values = values();
  private final int direction;
}
