package org.heckcorp.domination;

import java.awt.Point;
import java.util.List;

import org.heckcorp.domination.Unit.Type;
import org.heckcorp.domination.desktop.TurnManager;


/**
 * @author  Joachim Heck
 */
public interface GameModel {
    /**
     * Adds the piece to the model at the specified location.
     * @param piece
     * @pre piece != null
     * @pre position != null
     * @pre getMap().contains(position)
     */
    public void addGamePiece(GamePiece piece, Point position);

    /**
     * Adds the specified player to the model.
     * @param player
     * @pre cannot be called after the turn manager begins running.
     */
    public void addPlayer(Player player);

    /**
     * Initiates an attack by the selected unit on the specified hex.
     * @param hex
     * @pre hex != null
     * @pre unit.getOwner() == the current player
     * @pre unit.canAttack(hex)
     */
//    public void attack(Hex hex);

    /**
     * Ends the current player's turn.  Units with movement orders
     * will continue to move along their paths, but units without
     * orders will remain in place.
     */
    public void endTurn();
    
    /**
     * Returns the hex at the specified map coordinates.
     * @param position the map coordinates of the hex to get.
     * @return
     * @pre position != null
     * @pre position is within the map boundary
     * @pre the map has been set.
     * @post result != null
     */
    public Hex getHex(Point position);
    
//    /**
//     * Returns this model's map.
//     * @return
//     * @uml.property  name="map"
//     * @uml.associationEnd  
//     */
//    public HexMap getMap();

    /**
     * Returns a list of all the units in the hex at the
     * specified point.
     * @param position
     * @return
     * @pre position != null
     * @pre getMap().contains(position)
     * @post result != null
     */
    public List<Unit> getUnits(Point position);

    /**
     * Attempts to move the unit along its path.
     * @pre the selected unit must have a path defined.
     */
    public void moveSelectedUnit();

    /**
     * Sets the destination of the selected unit to the specified point.
     * @param destination
     * @pre destination != null
     * @pre destination is in the map
     * @post result == false || the selected unit has a path
     */
    public boolean setSelectedUnitDestination(Point destination);

    /**
     * The hex at the specified location is selected.  If there
     * are friendly units in the hex, the first selectable unit
     * is selected.  Additionally, if the hex contains a city,
     * the city is selected.
     * 
     * @param position
     * @pre position != null
     */
    public void selectHex(Point position);

    /**
     * The specified unit is selected.
     * @pre unit != null
     * @pre unit.getOwner() == the current player
     */
    public void selectUnit(Unit unit);
    
    /**
     * Sets this model's map to the specified hex map.
     * @param map
     * @pre  map != null
     * @uml.property  name="map"
     */
    public void setMap(HexMap map);
    
    /**
     * Skips the selected unit for a turn.  The unit will not move
     * or require orders this turn.
     */
    public void skipSelectedUnit();
    
    /**
     * Puts the selected unit to sleep.  The unit will not move
     * or attack until ordered to do so.
     */
    public void sleepSelectedUnit();
    
    /**
     * Hex hiding is toggled to the state opposite its current state.
     */
    public void toggleHexHiding();

    /**
     * Returns the currently selected city.
     * @return
     */
    public City getSelectedCity();

    /**
     * Returns the turn manager for this model.
     * @return
     */
    public TurnManager getTurnManager();

    /**
     * Sets the production type of the currently selected city to the
     * specified type.
     * @param type the type of unit to produce.
     */
    public void setSelectedCityProductionType(Type type);

    /**
     * Moves on to the next unit.  The selected unit will be selected
     * again after all other units have moved.
     */
    public void waitSelectedUnit();

    public void startTurnManager();
}
