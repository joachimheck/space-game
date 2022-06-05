package org.heckcorp.spacegame;

import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.Point;

import java.util.Set;


/**
 * @author  Joachim Heck
 */
public interface GameModel {
    /**
     * Adds the piece to the model at the specified location.
     * @pre piece != null
     * @pre position != null
     * @pre getMap().contains(position)
     */
    void addUnit(Unit unit, Point position);

    /**
     * Ends the current player's turn.  Units with movement orders
     * will continue to move along their paths, but units without
     * orders will remain in place.
     */
    void endTurn();

    /**
     * Returns the hex at the specified map coordinates.
     * @param position the map coordinates of the hex to get.
     * @pre position != null
     * @pre position is within the map boundary
     * @pre the map has been set.
     * @post result != null
     */
    Hex getHex(Point position);

    /**
     * Attempts to move the unit along its path.
     * @pre the selected unit must have a path defined.
     */
    void moveSelectedUnit();

    /**
     * Sets the destination of the selected unit to the specified point.
     * @pre destination != null
     * @pre destination is in the map
     * @post result == false || the selected unit has a path
     */
    void setSelectedUnitDestination(Point destination);

    /**
     * The hex at the specified location is selected.  If there
     * are friendly units in the hex, the first selectable unit
     * is selected.  Additionally, if the hex contains a city,
     * the city is selected.
     *
     * @pre position != null
     */
    void selectHex(Point position);

    /**
     * The specified unit is selected.
     * @pre unit != null
     * @pre unit.getOwner() == the current player
     */
    void selectUnit(Unit unit);

    /**
     * Skips the selected unit for a turn.  The unit will not move
     * or require orders this turn.
     */
    void skipSelectedUnit();

    /**
     * Puts the selected unit to sleep.  The unit will not move
     * or attack until ordered to do so.
     */
    void sleepSelectedUnit();

    /**
     * Hex hiding is toggled to the state opposite its current state.
     */
    void toggleHexHiding();

    /**
     * Returns the turn manager for this model.
     */
    TurnManager getTurnManager();

    /**
     * Moves on to the next unit.  The selected unit will be selected
     * again after all other units have moved.
     */
    void waitSelectedUnit();

    void startTurnManager();

    Set<Unit> getKnownEnemies(Player player);
}
