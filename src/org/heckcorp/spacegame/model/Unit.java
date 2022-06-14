package org.heckcorp.spacegame.model;

import org.heckcorp.spacegame.ui.map.ViewResources;

import java.io.Serializable;

/** Stores all the game-level information about a unit. */
public class Unit implements Serializable {
  public int getAttackStrength() {
    return attackStrength;
  }

  public int getEnergy() {
    return energy;
  }

  public int getHealth() {
    return health;
  }

  public void setHealth(int health) {
    this.health = health;
  }

  public ViewResources.Identifier getImageId() {
    return imageId;
  }

  public int getMaxEnergy() {
    return maxEnergy;
  }

  public int getMaxHealth() {
    return maxHealth;
  }

  public Player getOwner() {
    return owner;
  }

  public void resetForTurn() {
    energy = maxEnergy;
  }

  public Unit(Player owner, ViewResources.Identifier imageId) {
    this.owner = owner;
    this.imageId = imageId;
    this.attackStrength = 3;
    this.health = 5;
    this.maxHealth = 5;
    this.energy = 5;
    this.maxEnergy = 5;
  }

  private final int attackStrength;
  private int energy;
  private int health;
  private final int maxEnergy;
  private final int maxHealth;
  private final ViewResources.Identifier imageId;
  private final Player owner;
}
