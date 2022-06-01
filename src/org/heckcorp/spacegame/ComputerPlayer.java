package org.heckcorp.spacegame;

import org.heckcorp.spacegame.map.Calculator;
import org.heckcorp.spacegame.map.Hex;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.Serial;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ComputerPlayer extends Player {
    private void moveUnit(Unit unit, GameModel model) {
        getLog().finest("moveUnit(" + unit + ")");

        while (unit.isReadyForAction()) {
            int movesLeft = unit.getMovesLeft();
            getLog().fine("Computer moving unit " + unit +
                     " with " + movesLeft + " moves left.");

            // Order of precedence:
            // Attack units, move toward units, sit.
            @Nullable Hex destination = null;

            // Attack enemy units.
            if (unit.getAttacksLeft() > 0) {
                Optional<Unit> closestEnemy = getClosest(unit, getEnemiesInRange(unit));

                if (closestEnemy.isPresent()) {
                    getLog().finer("Attacking enemy: " + closestEnemy);
                    destination = closestEnemy.get().getHex();
                }
            }

            // Move toward non-adjacent enemy units.
            if (destination == null) {
                Optional<Unit> target = getClosest(unit, myView.getKnownEnemies());

                if (target.isPresent() && Calculator.distance(unit, target.get()) > 1) {
                    getLog().finer("Moving toward enemy: " + target.get());
                    destination = target.get().getHex();
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

    private <P extends Positionable> Optional<P> getClosest(Positionable base, Set<P> group) {
        return group.stream().min((p1, p2) ->
            Calculator.distance(base.getPosition(), p2.getPosition())
                    - Calculator.distance(base.getPosition(), p1.getPosition())
        );
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

    public ComputerPlayer(String name, Color color, GameView view) {
        super(name, color, view);

        assert view instanceof ComputerPlayerView;
        myView = (ComputerPlayerView)view;
        myView.setPlayer(this);
    }

    @Override
    public void move(GameModel model) {
        assert getReadyUnit() != null;
        moveUnit(getReadyUnit(), model);
    }

    /**
     * Should be final but it screws up serialization.
     * @noinspection FieldMayBeFinal
     */
    private transient ComputerPlayerView myView;

    @Serial
    private static final long serialVersionUID = 1L;
}
