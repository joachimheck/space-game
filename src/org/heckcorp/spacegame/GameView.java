package org.heckcorp.spacegame;

import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.map.swing.ViewMonitor;

import java.awt.*;

public interface GameView {
    /**
     * Displays a message in the view.
     * @pre message != null
     */
    void message(String message);

    /**
     * Makes the unit available for use in the UI.
     */
    void addUnit(Unit unit);

    /**
     * Initiates an attack.
     * @pre attacker != null
     * @pre target != null
     * @pre attacker has been added to this view, and not destroyed
     * @pre attacker has been added to this view, and not destroyed
     * @pre attacker.canAttack(target.getHex())
     */
    void attack(Unit attacker, Unit target);

    /**
     * Moves the unit.
     * @pre unit != null
     * @pre direction != null
     * @pre unit has been added to this view, and not destroyed
     */
    void move(Unit unit, Direction direction);

    /**
     * Selects the specified hex.  If a unit or another hex is
     * currently selected, it is first unselected.
     * @pre hex != null
     */
    void selectHex(Hex hex);

    /**
     * Sets this view's monitor.  The view will update the
     * controller whenever the viewport size or position changes.
     * @pre monitor != null
     */
    void setMonitor(ViewMonitor monitor);

    /**
     * Sets this view's current player to the specified player.
     * @pre player != null
     */
    void setCurrentPlayer(String playerName);

    /**
     * Sets the map to be displayed in this view.
     * @pre map != null
     */
    void setMap(HexMap map);

    void initialize();

    /**
     * Sets a status attribute of the unit.
     * @pre unit != null
     * @pre unit has been added to this view, and not destroyed.
     */
    void setStatus(Unit unit, UnitStatus status);

    /**
     * Announces that the specified player is the winner.
     * @pre player != null
     */
    void setWinningPlayer(String playerName, Color playerColor);
}
