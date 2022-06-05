package org.heckcorp.spacegame.map;

import org.heckcorp.spacegame.Player;
import org.heckcorp.spacegame.Unit;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This object represents one of the hexagonal spaces from which the map is composed.
 */
public class Hex implements Serializable {

    public void addUnit(Unit unit) {
        assert isEmpty() || owner == unit.getOwner() :
            "Can't add " + unit + "! hex empty? " + isEmpty() +
            " owner=" + owner;

        units.add(0, unit);

        owner = unit.getOwner();
    }

    /**
     * Returns the unit with the highest defense.
     * @pre getUnits().isEmpty()
     */
    public Unit getBestDefender() {
        Unit bestDefender = getUnits().get(0);

        for (Unit unit : getUnits()) {
            if (unit.getDefense() > bestDefender.getDefense()) {
                bestDefender = unit;
            }
        }

        return bestDefender;
    }

    @Nullable
    public Player getOwner() {
        return owner;
    }

    /**
     * @return the map coordinates of this hex.
     */
    public Point getPosition() {
        return new Point(x, y);
    }

    public List<Unit> getUnits() {
        return units;
    }

    public boolean isAdjacentTo(Hex hex) {
        return HexMap.isAdjacent(this, hex);
    }

    /**
     *
     * @return true if there are no units in the hex.
     */
    public boolean isEmpty() {
        return units.isEmpty();
    }

    public void removeUnit(Unit unit) {
        units.remove(unit);

        if (units.isEmpty()) {
            owner = null;
        }
    }

    public String toString() {
        return "Hex (" + getPosition().x() + "," + getPosition().y() + ")";
    }

    public Hex(int x, int y) {
        this.x = x;
        this.y = y;

        units = new ArrayList<>(0);
    }

    public final int x;
    public final int y;
    @Nullable private transient Player owner = null;
    private final transient List<Unit> units;
    @Serial
    private static final long serialVersionUID = 1L;

}
