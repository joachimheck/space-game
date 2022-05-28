package org.heckcorp.domination.desktop;

import org.heckcorp.domination.Calculator;
import org.heckcorp.domination.ComputerPlayerView;
import org.heckcorp.domination.GameModel;
import org.heckcorp.domination.GameView;
import org.heckcorp.domination.Hex;
import org.heckcorp.domination.HexFilter;
import org.heckcorp.domination.Player;
import org.heckcorp.domination.Positionable;
import org.heckcorp.domination.Unit;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

            Hex destination = null;

            // Order of precedence:
            // Attack cities, attack units, move toward cities, move toward units,
            // explore, sit.

            // Attack adjacent enemy cities.
            Hex enemyCityHex = getEnemyCityHex(enterable);
            if (enemyCityHex != null &&
                (enemyCityHex.isEmpty() || canAttack))
            {
                getLog().finer("Attacking city " + enemyCityHex);
                destination = enemyCityHex;
            }

            // Attack enemy units.
            if (destination == null && unit.getAttacksLeft() > 0) {
                Unit closestEnemy = getClosest(unit, getEnemiesInRange(unit));
                
                if (closestEnemy != null) {
                    getLog().finer("Attacking enemy: " + closestEnemy);
                    destination = closestEnemy.getHex();
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
        Set<Unit> inRange = new HashSet<>();
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
     * Returns a Hex containing an enemy city if there is one in the
     * specified collection of hexes.  An empty city is returned if
     * one is present.
     * 
     * @pre hexes != null
     */
    private Hex getEnemyCityHex(Collection<Hex> hexes) {
        Hex cityHex = null;
        
        for (Hex hex : hexes) {
            if (hex.getOwner() != this) {
                if (cityHex == null || hex.isEmpty()) {
                    cityHex = hex;
                }
            }
        }
        
        return cityHex;
    }

    public ComputerPlayer(String name, Color color, GameModel model, GameView view) {
        super(name, color, view);
        
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
