package org.heckcorp.spacegame;

import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.Point;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Superclass of any class that can be owned by a Player and positioned on a Map.
 * @author Joachim Heck
 *
 */
public abstract class GamePiece implements Positionable, Serializable {
    @Nullable
    public Hex getHex() {
        return hex;
    }

    public Player getOwner() {
        return owner;
    }

    @Nullable
    public Point getPosition() {
        if (hex == null) {
            return null;
        } else {
            return hex.getPosition();
        }
    }

    public void setHex(Hex hex) {
        this.hex = hex;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    protected GamePiece(Player owner) {
        this.owner = owner;
    }

    @Nullable protected Hex hex;
    private transient Player owner;
}
