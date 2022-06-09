package org.heckcorp.spacegame.model;

import org.heckcorp.spacegame.ui.map.ViewResources;

import java.io.Serializable;

/** Stores all the game-level information about a unit. */
public class Unit implements Serializable {
  public int getAttackStrength() {
    return attackStrength;
  }

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

  public void setHealth(int health) {
    this.health = health;
  }

  public Unit(Player owner, ViewResources.Identifier imageId) {
    this.owner = owner;
    this.imageId = imageId;
    this.attackStrength = 3;
    this.health = 5;
    this.maxHealth = 5;
  }

  private final int attackStrength;
  private int health;
  private final int maxHealth;
  private final ViewResources.Identifier imageId;
  private final Player owner;
}
