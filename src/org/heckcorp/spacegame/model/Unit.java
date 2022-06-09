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

  public Unit(Player owner, ViewResources.Identifier imageId, int health, int maxHealth) {
    this.owner = owner;
    this.imageId = imageId;
    this.health = health;
    this.maxHealth = maxHealth;
  }
  private final Player owner;
  private final ViewResources.Identifier imageId;
  private int health;
  private int maxHealth;
}
