package org.heckcorp.spacegame;

import org.heckcorp.spacegame.map.javafx.ViewResources;

import java.io.Serializable;

/**
 * Stores all the game-level information about a unit.
 */
public class Unit implements Serializable {
    public ViewResources.Identifier getImageId() {
        return imageId;
    }

    public Player getOwner() {
        return owner;
    }

    public Unit(Player owner, ViewResources.Identifier imageId) {
        this.owner = owner;
        this.imageId = imageId;
    }
    private final Player owner;

    private final ViewResources.Identifier imageId;
}
