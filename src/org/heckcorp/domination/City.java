package org.heckcorp.domination;

import java.io.Serializable;



public class City extends GamePiece implements Serializable {

    /**
     * @return  the productionPoints
     * @uml.property  name="productionPoints"
     */
    public int getProductionPoints() {
        return productionPoints;
    }

    /**
     * @return  the productionType
     * @uml.property  name="productionType"
     */
    public Unit.Type getProductionType() {
        return productionType;
    }
    
    public void incrementProductionPoints() {
        this.productionPoints += 1;
    }

    /**
     * @param productionPoints  the productionPoints to set
     * @uml.property  name="productionPoints"
     */
    public void setProductionPoints(int productionPoints) {
        this.productionPoints = productionPoints;
    }

    /**
     * @param type the productionType to set
     * @uml.property  name="productionType"
     */
    public void setProductionType(Unit.Type type) {
        productionType = type;
    }

    public String toString() {
        return "City (" + getPosition().x + "," + getPosition().y + ")";
    }

    public City(Player player) {
        player.addGamePiece(this);
        setOwner(player);
    }

    /**
     * @uml.property  name="productionPoints"
     */
    private int productionPoints = 0;
    /**
     * @uml.property  name="productionType"
     * @uml.associationEnd  multiplicity="(1 1)"
     */
    private Unit.Type productionType = Unit.Type.TANK;

    /**
     * @return a HexFilter that only accepts hexes
     *   into which a city can be placed.
     */
    public static HexFilter getHexFilter() {
        return new HexFilter() {
            @Override
            public boolean accept(Hex hex) {
                return hex.terrain != Hex.Terrain.WATER &&
                    hex.getCity() == null;
            }
        };
    }
    
    private static final long serialVersionUID = 1L;
}
