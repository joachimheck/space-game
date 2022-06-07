package org.heckcorp.spacegame;

import java.io.Serializable;

/**
 * Stores all the game-level information about a unit.
 */
public class Unit implements Serializable {
    public Player getOwner() {
        return owner;
    }

    public Unit(Player owner) {
        this.owner = owner;
    }

    private final Player owner;
}
