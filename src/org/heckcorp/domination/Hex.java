package org.heckcorp.domination;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This object represents one of the hexagonal spaces from which the map is
 * composed.
 * 
 * @author Joachim Heck
 * 
 */
public class Hex implements Serializable, Positionable {
    /**
     * @author    Joachim Heck
     */
    public enum Terrain {
        LAND(1, "Land"),
        WATER(0, "Water");
        
        Terrain(int value, String name) {
            this.value = value;
            this.name = name;
        }
        
        public final String name;
        public final int value;
    }

    /**
     * @pre getCity() == null
     * @pre isEmpty()
     * @post getOwner() == city.getOwner()
     */
    public void addCity(City city) {
        assert isEmpty();
        assert this.city == null;
        
        this.city = city;
        owner = city.getOwner();
    }
    
    public void addUnit(Unit unit) {
        assert isEmpty() || owner == unit.getOwner() :
            "Can't add " + unit + "! hex empty? " + isEmpty() +
            " owner=" + owner;
        
        units.add(0, unit);
        
        owner = unit.getOwner();
        
        if (city != null) {
            if (city.getOwner() != unit.getOwner()) {
                city.getOwner().removeGamePiece(city);
                unit.getOwner().addGamePiece(city);
                city.setOwner(unit.getOwner());
            }
        }
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

    /**
     * @return  the city
     * @uml.property  name="city"
     */
    public City getCity() {
        return city;
    }

    /**
     * @return  the owner
     * @uml.property  name="owner"
     */
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
        
        if (units.isEmpty() && city == null) {
            owner = null;
        }
    }
    
    public String toString() {
        return "Hex (" + getPosition().x + "," + getPosition().y + ")";
    }
    
    public Hex(int x, int y, Terrain terrain, int elevation) {
        this.x = x;
        this.y = y;
        this.terrain = terrain;
        this.elevation = elevation;
        
        units = new ArrayList<>(0);
    }

    public final int elevation;
    public final Terrain terrain;
    public final int x;
    public final int y;
    private transient City city;
    private transient Player owner = null;
    private final transient List<Unit> units;
    private static final long serialVersionUID = 1L;

}
