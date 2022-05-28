package org.heckcorp.domination;

import java.awt.*;
import java.io.Serializable;

/**
 * Superclass of any class that can be owned by a Player and positioned on a Map.
 * @author Joachim Heck
 *
 */
public abstract class GamePiece implements Positionable, Serializable {

    /**
     * @return  the hex
     * @uml.property  name="hex"
     */
    public Hex getHex() {
        return hex;
    }
    
    /**
     * @return  the owner
     * @uml.property  name="owner"
     */
    public Player getOwner() {
        return owner;
    }

    public Point getPosition() {
        if (hex == null) {
            return null;
        } else {
            return hex.getPosition();
        }
    }

    public abstract boolean isHidden(ShadowMap shadowMap);
    
    /**
     * @param hex  the hex to set
     * @uml.property  name="hex"
     */
    public void setHex(Hex hex) {
        this.hex = hex;
    }

    /**
     * @param owner  the owner to set
     * @uml.property  name="owner"
     */
    public void setOwner(Player owner) {
        this.owner = owner;
    }
    
    protected Hex hex;
    private transient Player owner;
}
