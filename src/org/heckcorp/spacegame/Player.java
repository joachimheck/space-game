package org.heckcorp.spacegame;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

public abstract class Player implements Serializable {

    protected static Logger log;

    public abstract void move() throws InterruptedException;

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(getClass().getName());
        }

        return log;
    }

    public Player(String name, Color color, @Nullable GameView view)
    {
        this.name = name;
        this.color = color;
        this.view = view;
        // The turn manager steps over a player's units to move them,
        // but a unit can be destroyed, and removed from the set, while
        // moving.  Using this set prevents a ConcurrentModificationException.
        units = new CopyOnWriteArraySet<>();
    }

    private final String name;
    private final Color color;

    /**
     * This should be final, but that breaks serialization.
     */
    protected transient GameView view;
    private final Set<Unit> units;

    public enum PlayerType {
        HUMAN, COMPUTER, NEUTRAL
    }

    public Set<Unit> getUnits() {
        return units;
    }

    /**
     * Removes the specified unit from this Player's list.
     * @pre getUnits().contains(unit)
     */
    public void removeUnit(Unit unit) {
        assert units.contains(unit) : this + " does not have " + unit;
        units.remove(unit);
    }

    public void addUnit(Unit unit) {
        unit.setOwner(this);
        units.add(unit);
    }

    public Color getColor() {
        return color;
    }

    public GameView getView() {
        return view;
    }

    private Unit readyUnit = null;

    public void setReadyUnit(Unit readyUnit) {
        getLog().fine("Ready unit is " + readyUnit);
        assert this.readyUnit == null : this.readyUnit;
        this.readyUnit = readyUnit;
    }

    public Unit getReadyUnit() {
        return readyUnit;
    }

    public void clearReadyUnit() {
        this.readyUnit = null;
    }

    public void unitActionFinished() {
        getLog().finer("Unit action finished!");
        clearReadyUnit();
    }

    public void startTurn() {
        // The default version of this method does nothing.
    }

    /**
     * Instructs this Player to perform any necessary cleanup
     * action before its turn ends.
     */
    public void finishTurn() {
        // The default version of this method does nothing.
    }

    /**
     * Called to finish this player's turn and wake any
     * thread waiting in finishTurn().
     */
    public void turnFinished() {
        // The default version of this method does nothing.
    }

    public String getName() {
        return name;
    }

    public void setView(GameView view) {
        assert this.view == null;

        this.view = view;
    }

    public PlayerType getType() {
        PlayerType result = null;

        if (this instanceof HumanPlayer) {
            result = PlayerType.HUMAN;
        } else if (this instanceof ComputerPlayer) {
            result = PlayerType.COMPUTER;
        } else if (this instanceof NeutralPlayer) {
            result = PlayerType.NEUTRAL;
        } else {
            assert false;
        }

        return result;
    }

    public void write(ObjectOutputStream out) throws IOException {
        out.writeInt(getType().ordinal());
        out.writeObject(name);
        out.writeObject(color);
        out.writeObject(units);
    }
}
