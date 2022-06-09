package org.heckcorp.spacegame.model;

import org.heckcorp.spacegame.map.javafx.ViewResources;

import java.io.Serializable;

/** Stores all the game-level information about a unit. */
public class Unit implements Serializable {
  public int getHealth() {
    return health;
  }

  public ViewResources.Identifier getImageId() {
    return imageId;
  }

  public int getMaxHealth() {
    return maxHealth;
  }

  public Player getOwner() {
    return owner;
  }

  public Unit(Player owner, ViewResources.Identifier imageId) {
    this.owner = owner;
    this.imageId = imageId;
  }

  private final int attackStrength = 3;
  private int health = 5;
  private int maxHealth = 5;

  private final ViewResources.Identifier imageId;
  private final Player owner;
}
