package org.heckcorp.spacegame.model;

import org.heckcorp.spacegame.ui.map.ViewResources;

import java.io.Serializable;

/** Stores all the game-level information about a unit. */
public class Unit implements Serializable {
  public int[] getArmor() {
    return armor;
  }

  public int getAttackStrength() {
    return attackStrength;
  }

  public int getEnergy() {
    return energy;
  }

  public int getHealth() {
    return health;
  }

  public ViewResources.Identifier getImageId() {
    return imageId;
  }

  public int[] getMaxArmor() {
    return maxArmor;
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

  public void setEnergy(int energy) {
    this.energy = energy;
  }

  public void setHealth(int health) {
    this.health = health;
  }

  public Unit(Player owner, ViewResources.Identifier imageId) {
    this.owner = owner;
    this.imageId = imageId;
    attackStrength = 3;
    health = 10;
    maxHealth = 10;
    energy = 5;
    maxEnergy = 5;
    armor = new int[] {10, 10, 10, 10, 10, 10};
    maxArmor = new int[] {10, 10, 10, 10, 10, 10};
  }

  private final int[] armor;
  private final int attackStrength;
  private int energy;
  private int health;
  private final int[] maxArmor;
  private final int maxEnergy;
  private final int maxHealth;
  private final ViewResources.Identifier imageId;

  private final Player owner;
}
