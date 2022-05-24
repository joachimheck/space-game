package org.heckcorp.domination;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import org.heckcorp.domination.desktop.ComputerPlayer;
import org.heckcorp.domination.desktop.HumanPlayer;
import org.heckcorp.domination.desktop.NeutralPlayer;

public abstract class Player implements Serializable {
    
    protected static Logger log;
    
    public abstract void move() throws InterruptedException;
    
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(getClass().getName());
        }
        
        return log;
    }

    public Player(String name, Color color,
                  ShadowMap shadowMap, GameView view)
    {
        this.name = name;
        this.color = color;
        this.shadowMap = shadowMap;
        this.view = view;
        // The turn manager steps over a player's units to move them,
        // but a unit can be destroyed, and removed from the set, while
        // moving.  Using this set prevents a ConcurrentModificationException.
        units = new CopyOnWriteArraySet<Unit>();
        cities = new HashSet<City>();
    }
    
    private final String name;
    private final Color color;
    private final ShadowMap shadowMap;
    /**
     * This should be final, but that breaks serialization.
     */
    protected transient GameView view;
    private final Set<Unit> units;
    private final Set<City> cities;
    
    /**
     * @author    Joachim Heck
     */
    public enum PlayerType {
        HUMAN, COMPUTER, NEUTRAL;
    }

    /**
     * @return  the shadowMap
     * @uml.property  name="shadowMap"
     */
    public ShadowMap getShadowMap() {
        return shadowMap;
    }

    public Set<Unit> getUnits() {
        return units;
    }

    /**
     * Removes the specified unit from this Player's list.
     * @param unit
     * @pre unit != null
     * @pre getUnits().contains(unit)
     */
    public void removeUnit(Unit unit) {
        // This is unnecessary but it could catch a real bug.
        assert units.contains(unit) :
            this + " does not have " + unit;
        units.remove(unit);
    }

    public void removeCity(City city) {
        assert cities.contains(city);
        cities.remove(city);
    }
    
    public Set<City> getCities() {
        return cities;
    }

    public void addGamePiece(GamePiece piece) {
        piece.setOwner(this);
        
        if (piece instanceof Unit) {
            assert !units.contains(piece);
            units.add((Unit) piece);
        } else if (piece instanceof City) {
            City city = (City) piece;
            assert !cities.contains(city);
            cities.add(city);
            city.setProductionPoints(0);
        } else {
            assert false;
        }
    }

    public Color getColor() {
        return color;
    }
    
    public List<GamePiece> getGamePieces() {
        List<GamePiece> result = new ArrayList<GamePiece>();
        result.addAll(cities);
        result.addAll(units);
        return result;
    }

    /**
     * Updates the shadow map for this player.
     */
    public void updateShadowMap(HexMap map) {
        List<Point> visible = new ArrayList<Point>();
        for (GamePiece entity : getGamePieces()) {
            List<Hex> adjacent = map.getHexesInRange(entity.getHex(), 1);
            adjacent.add(entity.getHex());

            for (Hex hex : adjacent) {
                visible.add(hex.getPosition());
            }
        }

        shadowMap.clearVisible();
        shadowMap.setExplored(visible);
        shadowMap.setVisible(visible);

    }

    public GameView getView() {
        return view;
    }

    public void removeGamePiece(GamePiece piece) {
        if (piece instanceof Unit) {
            assert units.contains(piece);
            units.remove(piece);
        } else if (piece instanceof City) {
            assert cities.contains(piece);
            cities.remove(piece);
        } else {
            assert false;
        }
    }
    private Unit readyUnit = null;
    /**
     * @pre getReadyUnit() == null
     * @param readyUnit
     */
    public void setReadyUnit(Unit readyUnit) {
        getLog().fine("Ready unit is " + readyUnit);
        assert readyUnit != null;
        assert this.readyUnit == null : this.readyUnit;
        this.readyUnit = readyUnit;
    }
    public Unit getReadyUnit() {
        return readyUnit;
    }
    /**
     * @pre getReadyUnit() != null
     */
    public void clearReadyUnit() {
//        assert this.readyUnit != null;
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

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @pre view != null
     * @pre getView() == null
     * @param view
     */
    public void setView(GameView view) {
        assert this.view == null;
        assert view != null;
        
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
        out.writeObject(shadowMap);

        out.writeObject(cities);
        out.writeObject(units);
    }
}
