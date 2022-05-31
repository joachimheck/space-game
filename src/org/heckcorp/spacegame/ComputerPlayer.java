package org.heckcorp.spacegame;

import org.heckcorp.spacegame.map.Calculator;
import org.heckcorp.spacegame.map.Hex;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class ComputerPlayer extends Player {
    private void moveUnit(Unit unit) {
        getLog().finest("moveUnit(" + unit + ")");

        while (unit.isReadyForAction()) {
            int movesLeft = unit.getMovesLeft();
            getLog().fine("Computer moving unit " + unit +
                     " with " + movesLeft + " moves left.");

            // Order of precedence:
            // Attack units, move toward units, sit.
            Hex destination = null;

            // Attack enemy units.
            if (unit.getAttacksLeft() > 0) {
                Unit closestEnemy = getClosest(unit, getEnemiesInRange(unit));

                if (closestEnemy != null) {
                    getLog().finer("Attacking enemy: " + closestEnemy);
                    destination = closestEnemy.getHex();
                }
            }

            // Move toward non-adjacent enemy units.
            if (destination == null) {
                Unit target = getClosest(unit, myView.getKnownEnemies());

                if (target != null && Calculator.distance(unit, target) > 1) {
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

    public ComputerPlayer(String name, Color color, GameModel model, GameView view) {
        super(name, color, view);

        assert view instanceof ComputerPlayerView;
        myView = (ComputerPlayerView)view;
        myView.setPlayer(this);

        this.model = model;
    }

    /**
     * @pre view instanceof ComputerPlayerView
     */
    @Override
    public void setView(GameView view) {
        assert myView == null;

        myView = (ComputerPlayerView) view;

        super.setView(view);
    }

    @Override
    public void move() {
        assert getReadyUnit() != null;
        moveUnit(getReadyUnit());
    }

    /**
     * Should be final but it screws up serialization.
     * @noinspection FieldMayBeFinal
     */
    private transient GameModel model;
    private transient ComputerPlayerView myView;

    private static final long serialVersionUID = 1L;
}
