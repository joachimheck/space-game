package org.heckcorp.domination.desktop;

import java.awt.Color;
import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.heckcorp.domination.Calculator;
import org.heckcorp.domination.City;
import org.heckcorp.domination.ComputerPlayerView;
import org.heckcorp.domination.GameModel;
import org.heckcorp.domination.GameView;
import org.heckcorp.domination.Hex;
import org.heckcorp.domination.HexFilter;
import org.heckcorp.domination.Player;
import org.heckcorp.domination.Positionable;
import org.heckcorp.domination.ShadowMap;
import org.heckcorp.domination.Unit;

public class ComputerPlayer extends Player {
    private void moveUnit(Unit unit) {
        getLog().finest("moveUnit(" + unit + ")");
        
        while (unit.isReadyForAction()) {
            int movesLeft = unit.getMovesLeft();
            getLog().fine("Computer moving unit " + unit +
                     " with " + movesLeft + " moves left.");

            Hex hex = unit.getHex();
            List<Hex> adjacent = myView.getMap().getHexesInRange(hex, 1);
            HexFilter moveFilter = unit.getMovementHexFilter();
            // These hexes can be entered without combat.
            List<Hex> enterable = unit.getAccessibleHexes(adjacent, moveFilter);
            boolean canAttack = unit.getAttacksLeft() > 0;
            City home = getClosest(unit, getCities());
            
            Hex destination = null;

            // Order of precedence:
            // Attack cities, attack units, move toward cities, move toward units,
            // explore, sit.

            // Return home if fuel is low.
            if (home != null && unit.getFuelLeft() == Calculator.distance(unit, home)) {
                if (home.getHex() != unit.getHex()) {
                    destination = home.getHex();
                    getLog().finer("Fuel low (" + unit.getFuelLeft() +
                              "/" + unit.getMovement() + "): heading home to " +
                              home.getHex());
                }
            }

            // Attack adjacent enemy cities.
            if (destination == null) {
                Hex enemyCityHex = getEnemyCityHex(enterable);
                if (enemyCityHex != null &&
                    (enemyCityHex.isEmpty() || canAttack))
                {
                    getLog().finer("Attacking city " + enemyCityHex);
                    destination = enemyCityHex;
                }
            }
            
            // Attack enemy units.
            if (destination == null && unit.getAttacksLeft() > 0) {
                Unit closestEnemy = getClosest(unit, getEnemiesInRange(unit));
                
                if (closestEnemy != null) {
                    getLog().finer("Attacking enemy: " + closestEnemy);
                    destination = closestEnemy.getHex();
                }
            }
            
            // Move toward non-adjacent enemy cities.
            if (destination == null) {
                City city = getClosest(unit, myView.getKnownEnemyCities());
                
                if (city != null && unit.isHexInRange(city.getHex()) &&
                    Calculator.distance(unit, city) > 1)
                {
                    getLog().finer("Moving toward city: " + city);
                    destination = city.getHex();
                }
            }
                
            // Move toward non-adjacent enemy units.
            if (destination == null) {
                Unit target = getClosest(unit, myView.getKnownEnemies());
                
                if (target != null && unit.isHexInRange(target.getHex()) &&
                    Calculator.distance(unit, target) > 1)
                {
                    getLog().finer("Moving toward enemy: " + target);
                    destination = target.getHex();
                }
            }
                
            // Explore.
            if (destination == null) {
                Set<Point> borderPoints = getShadowMap().getBorderPoints();
                Set<Hex> border = myView.getMap().getHexes(borderPoints);
                Hex toExplore = getClosest(unit, border);
                if (toExplore != null) {
                    Set<Hex> surrounding =
                        myView.getMap().getAdjacentHexes(toExplore);
                    Hex closest = getClosest(unit, surrounding);

                    if (closest != null) {
                        int toHex = Calculator.distance(unit, closest);
                        int toHome = Calculator.distance(closest, home);
                        int totalDistance = toHex + toHome;
                        
                        if (unit.isInRange(totalDistance)) {
                            if (closest.isEmpty() ||
                                closest.getOwner() == unit.getOwner())
                            {
                                getLog().finer("Exploring: " + closest);
                                destination = closest;
                            } else {
                                getLog().finer("Can't explore occupied hex " +
                                          closest);
                            }
                        } else if (unit.getHex() != home.getHex()) {
                            destination = home.getHex();
                            getLog().finer("Not enough fuel to explore: " +
                                      "heading home to " + home.getHex());
                        }
                    }
                } else {
                    getLog().finer("No border hexes to explore!");
                }
            }

            if (destination == null && !unit.isSafe()) {
                getLog().finer("Unit can't stay at " + unit.getPosition() +
                          ": trying to make it home to " + home);
                destination = home.getHex();
            }
            
            if (destination == null) {
                getLog().fine("CP skipping unit " + unit);
                model.skipSelectedUnit();
            } else {
                getLog().fine("CP moving unit from " +
                         unit.getHex().getPosition() + " to " +
                         destination.getPosition());
                model.setSelectedUnitDestination(destination.getPosition());
                model.moveSelectedUnit();
            }

            if (unit.getMovesLeft() >= movesLeft && unit.isReadyForAction()) {
                getLog().info("Unit " + unit + " (ready? " + unit.isReadyForAction() +
                         ") started with " + movesLeft + " moves, now has " +
                         unit.getMovesLeft());
                getLog().fine("Skipping unit due to bad movement orders.");
                model.skipSelectedUnit();
            }
        }

        getLog().fine("Move finished.");
    }

    private <P extends Positionable> P getClosest(Positionable base, Set<P> group) {
        P closest = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (P item : group) {
            int distance = Calculator.distance(base.getPosition(), item.getPosition());
            
            if (distance < minDistance) {
                minDistance = distance;
                closest = item;
            }
        }
        
        return closest;
    }

    private Set<Unit> getEnemiesInRange(Unit unit) {
        Set<Unit> inRange = new HashSet<Unit>();
        Set<Unit> allEnemies = myView.getKnownEnemies();
        
        int range = unit.getRange();
        
        for (Unit enemy : allEnemies) {
            if (Calculator.distance(unit.getPosition(), enemy.getPosition()) <= range) {
                inRange.add(enemy);
            }
        }
        
        return inRange;
    }

    /**
     * 
     * @param hexes
     * @return a random selection from the provided list of hexes.
     */
// TODO: use?
    //    private Hex chooseRandomHex(List<Hex> hexes) {
//        int choice = (int) Math.floor(hexes.size() * Math.random());
//        return hexes.get(choice);
//    }

    /**
     * Returns a Hex containing an enemy city if there is one in the
     * specified collection of hexes.  An empty city is returned if
     * one is present.
     * 
     * @param hexes
     * @return
     * @pre hexes != null
     */
    private Hex getEnemyCityHex(Collection<Hex> hexes) {
        Hex cityHex = null;
        
        for (Hex hex : hexes) {
            if (hex.getCity() != null && hex.getOwner() != this) {
                if (cityHex == null || (cityHex != null && hex.isEmpty())) {
                    cityHex = hex;
                }
            }
        }
        
        return cityHex;
    }

    public ComputerPlayer(String name, Color color,
                          ShadowMap shadowMap, GameModel model, GameView view)
    {
        super(name, color, shadowMap, view);
        
        assert view instanceof ComputerPlayerView;
        myView = (ComputerPlayerView)view;
        myView.setPlayer(this);
        
        this.model = model;
    }

    /**
     * Should be final but it screws up serialization.
     */
    private transient GameModel model;
    private transient ComputerPlayerView myView;
    
    private static final long serialVersionUID = 1L;

    /**
     * @pre view instanceof ComputerPlayerView
     */
    @Override
    public void setView(GameView view) {
        assert myView == null;
        
        myView = (ComputerPlayerView) view;
        
        super.setView(view);
    }

    /**
     * @pre model != null
     * @pre this player's model has not yet been set.
     * @param model
     */
    public void setModel(GameModel model) {
        assert model != null;
        assert this.model == null;
        
        this.model = model;
    }
    
    @Override
    public void move() {
        assert getReadyUnit() != null;
        moveUnit(getReadyUnit());
    }
}
