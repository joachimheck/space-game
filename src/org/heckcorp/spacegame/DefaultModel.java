package org.heckcorp.spacegame;

import com.google.common.collect.Lists;
import org.heckcorp.spacegame.Player.PlayerType;
import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.map.Pathfinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * An implementation of a GameModel.
 */
public class DefaultModel implements GameModel, Serializable {
    public void write(ObjectOutputStream out) throws IOException {
        // Write Map
        map.write(out);

        // Write Current player id #
        out.writeInt(players.indexOf(currentPlayer));

        // Write Players
        out.writeInt(players.size());
        for (Player player : players) {
            player.write(out);
        }
    }

    /**
     * Adds the piece to the model at the specified position.
     * @pre getMap().contains(position)
     */
    public void addUnit(@NotNull Unit unit, @NotNull Point position) {
        unit.setHex(map.getHex(position));
        map.addUnit(unit, unit.getPosition());
        views.addUnit(unit);
    }

    /**
     * Adds the specified player to the model.
     * @pre player must not have been added already.
     * @pre cannot be called after the turn manager begins running.
     */
    public void addPlayer(Player player) {
        players.add(player);
        if (player.getView() != null) {
            views.addGameView(player.getView());
            // We don't get the views until we get the players,
            // so we haven't set the map yet.
            player.getView().setMap(map);
        }
    }

    /**
     * Ends the current player's turn.  Units with movement orders
     * will continue to move along their paths, but units without
     * orders will remain in place.
     */
    public void endTurn() {
        currentPlayer.turnFinished();
        turnManager.turnFinished();
        currentPlayer.unitActionFinished();
    }

    /**
     * Returns the hex at the specified map coordinates.
     * @param position the map coordinates of the hex to get.
     * @pre position is within the map boundary
     */
    public Hex getHex(Point position) {
        return map.getHex(position);
    }

     public TurnManager getTurnManager() {
         return turnManager;
     }

    /**
     * Attempts to move the unit along its path.
     */
    public void moveSelectedUnit() {
        if (selectedUnit != null && !selectedUnit.getPath().isEmpty()) {
            // We use a copy of the unit because selectedUnit
            // may be set to null inside moveSelectedUnitOneHex().
            Unit unit = selectedUnit;

            Hex hex = unit.getPath().get(0);

            // We only attack if the hex next to the unit was clicked.
            if (unit.getPath().size() == 1 && unit.canAttack(hex)) {
                log.finer("Unit " + unit + " attacking hex " + hex);
                Unit defender = hex.getBestDefender();
                views.attack(unit, defender);
                attack(hex);

                if (unit.canEnterHex(hex)) {
                    Direction direction = unit.getNextDirection();
                    unit.move();
                    views.move(unit, direction);
                } else {
                    unit.decreaseMovesLeft(1);
                    unit.getPath().remove(0);
                }
            } else {
                while (unit.canMoveAlongPath()) {
                    int movesLeft = unit.getMovesLeft();
                    moveSelectedUnitOneHex();
                    assert unit.getMovesLeft() < movesLeft :
                        "Unit didn't move to " + unit.getPath().get(0);

                    Set<Hex> adjacent = map.getAdjacentHexes(unit.getHex());
                    for (Hex adjacentHex : adjacent) {
                        if (adjacentHex.getOwner() != unit.getOwner() &&
                            !adjacentHex.isEmpty())
                        {
                            log.fine("Stopping unit " + unit);
                            unit.clearPath();
                        }
                    }
                }

                // If a unit was stopped, don't let it keep trying to move
                // along its former path.
                if (unit.getMovesLeft() > 0 && !unit.getPath().isEmpty()) {
                    unit.clearPath();
                }

                log.fine("Finished moving selected unit: " + unit);
            }

            if (unit.getMovesLeft() == 0 || unit.isDestroyed()) {
                if (unit.isDestroyed()) {
                    log.fine("Unit is destroyed.");
                } else {
                    log.fine("Unit is out of moves.");
                    setStatus(selectedUnit, UnitStatus.UNSELECTED);
                }

                unit.getOwner().unitActionFinished();
            }
        } else {
            if (selectedUnit == null) {
                log.info("Attempt to move null selected unit!");
            } else if (selectedUnit.getPath().isEmpty()) {
                log.fine(selectedUnit + " has no movement orders.");
            }
        }
    }

    /**
     * The hex at the specified location is selected.  If there
     * are friendly units in the hex, the first selectable unit
     * is selected.  Additionally, if the hex contains a city,
     * and that city belongs to the current player, the city is selected.
     *
     * @pre position != null
     */
    public void selectHex(Point position) {
        if (selectedUnit != null) {
            setStatus(selectedUnit, UnitStatus.UNSELECTED);
        }

        Hex hex = map.getHex(position);

        if (hex.getOwner() == currentPlayer) {
            if (!hex.getUnits().isEmpty()) {
                // The user clicked on a hex with a unit in it.
                List<Unit> units = hex.getUnits();

                // Find the first movable unit that's not already selected.
                Unit topUnit = units.get(0);
                for (Unit unit : units) {
                    if (unit.getMovesLeft() > 0 && unit != selectedUnit) {
                        topUnit = unit;
                        break;
                    }
                }

                // Cycle the new top unit to the top.
                log.finer("Selecting new top unit: " + topUnit);
                units.remove(topUnit);
                units.add(topUnit);
                setStatus(topUnit, UnitStatus.SELECTED);
            }
        }

        views.selectHex(hex);
    }

    /**
     * The specified unit is selected.
     * @pre unit != null
     * @pre unit.getOwner() == the current player
     */
    public synchronized void selectUnit(Unit unit) {
        log.entering("DefaultModel", "selectUnit");
        assert unit.getOwner() == currentPlayer;

        if (unit.getMovesLeft() > 0) {
            if (selectedUnit != null) {
                setStatus(selectedUnit, UnitStatus.UNSELECTED);
            }

            setStatus(unit, UnitStatus.SELECTED);
            unit.setAsleep(false);
        } else {
            log.fine("Can't select " + unit + ": no moves left.");
        }

        log.finer("Selected unit - now selecting hex.");

        views.selectHex(unit.getHex());

        log.exiting("DefaultModel", "selectUnit");
    }

    public void setCurrentPlayer(Player player) {
        currentPlayer = player;
        views.setCurrentPlayer(player.getName());
    }

    /**
     * Sets the destination of the selected unit to the specified point.
     * @pre destination is in the map
     * @post result == false || the selected unit has a path
     */
    public void setSelectedUnitDestination(Point destination) {
        if (selectedUnit != null) {
            assert map.isInMap(destination);

            Hex hex = map.getHex(destination);
            Pathfinder pathfinder = map.getPathfinder();
            List<Hex> path = pathfinder.findPath(selectedUnit.getHex(), hex);

            if (!path.isEmpty()) {
                selectedUnit.setPath(path);
            }
            // TODO: we could notify the view that the path has been set.
        }
    }

    public void setWinningPlayer(Player player) {
         views.setWinningPlayer(player.getName(), player.getColor());
     }
    /**
      * Skips the selected unit for a turn.  The unit will not move or require
      * orders this turn.
      * @pre unit != null
      * @pre unit.getOwner() == the current player
      */
     public void skipSelectedUnit() {
         if (selectedUnit != null) {
             selectedUnit.skip();
             log.fine("Model skipping unit.");
             selectedUnit.getOwner().unitActionFinished();
             setStatus(selectedUnit, UnitStatus.UNSELECTED);
         }
     }
    /**
      * Puts the selected unit to sleep.  The unit will not move
      * or attack until ordered to do so.
      */
     public void sleepSelectedUnit() {
         if (selectedUnit != null) {
             selectedUnit.setAsleep(true);
             log.fine("Model sleeping unit.");
             selectedUnit.getOwner().unitActionFinished();
             setStatus(selectedUnit, UnitStatus.UNSELECTED);
             // TODO: we could notify the view that the unit is asleep.
         }
     }
    /**
     * Hex hiding is toggled to the state opposite its current state.
     */
    public void toggleHexHiding() {
    }

    /**
     * Moves on to the next unit.  The selected unit will be selected
     * again after all other units have moved.
     */
    public void waitSelectedUnit() {
        if (selectedUnit != null) {
            selectedUnit.getOwner().unitActionFinished();
        }
    }
    /**
     * Initiates an attack by the selected unit on the specified hex.
     * @pre unit != null
     * @pre hex != null
     * @pre unit.getOwner() == the current player
     * @pre unit.canAttack(hex)
     */
    private void attack(Hex hex) {
        assert selectedUnit != null;

        // Compute the results of the attack.
        Unit loser = selectedUnit.attack(hex);

        if (loser.getHealth() == Unit.Health.DESTROYED) {
            setStatus(loser, UnitStatus.DESTROYED);
            if (loser == selectedUnit) {
                setStatus(loser, UnitStatus.UNSELECTED);
            }
        } else if (loser.getHealth() == Unit.Health.DAMAGED) {
            setStatus(loser, UnitStatus.DAMAGED);
        }
    }
    /**
     * @pre !selectedUnit.getPath().isEmpty()
     */
    private void moveSelectedUnitOneHex() {
        assert selectedUnit != null;

        Direction direction = selectedUnit.getNextDirection();
        log.finer("Model moving " + selectedUnit + " one hex.");

        boolean moved = selectedUnit.move();

        if (moved) {
            log.finer("Model moved " + selectedUnit + " to " + selectedUnit.getHex());
            views.move(selectedUnit, direction);
            views.selectHex(selectedUnit.getHex());
        } else {
            log.fine("Model couldn't move " + selectedUnit + " to " + selectedUnit.getPath().get(0));
        }

        log.finer("Model finished moving unit one hex.");
    }

    /**
     * Sets the status of the specified unit.  Possible statuses include
     * damaged, destroyed, hidden and selected.
     * @pre unit != null
     * @pre status != null
     */
    private synchronized void setStatus(Unit unit, UnitStatus status) {
        log.fine("Setting status of " + unit + " to " + status);

        // TODO: well this is ugly.  Make a method to do it?  If getStackTrace()
        // returned the right thing to start with we wouldn't have to.
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        int i;
        for (i=0; i<trace.length; i++) {
            if ("setStatus".equals(trace[i].getMethodName())) {
                i++;
                break;
            }
        }
        log.fine("SetStatus was called by " +
                 trace[i].getClassName() + "." + trace[i].getMethodName());

        if (status == UnitStatus.SELECTED) {
            assert selectedUnit == null;
            selectedUnit = unit;
        } else if (status == UnitStatus.UNSELECTED) {
            assert selectedUnit != null;
            selectedUnit = null;
        } else if (status == UnitStatus.HIDDEN) {
            // TODO: Remove this status!
        } else if (status == UnitStatus.REVEALED) {
            // TODO: Remove this status!
        } else if (status == UnitStatus.DAMAGED) {
//            unit.setHealth(Health.DAMAGED);
        } else if (status == UnitStatus.DESTROYED) {
//            unit.setHealth(Health.DESTROYED);
        } else if (status == UnitStatus.HEALTHY) {
//            unit.setHealth(Health.HEALTHY);
        } else if (status == UnitStatus.SKIPPED) {
            // TODO: Remove this status!
        }

        views.setStatus(unit, status);
    }

    public static DefaultModel initialize(GameView view, ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        Logger log = Logger.getLogger("DefaultModel");

        List<GameView> views = Lists.newArrayList();
        HexMap map = new HexMap(in);
        view.setMap(map);
        List<Player> players = Lists.newArrayList();
        @Nullable Player currentPlayer = null;

        int currentPlayerIndex = in.readInt();
        int playerCount = in.readInt();
        for (int i=0; i<playerCount; i++) {
            log.finer("Reading player #" + i);
            int typeNumber = in.readInt();
            PlayerType type = PlayerType.values()[typeNumber];

            String name = (String) in.readObject();
            Color color = (Color) in.readObject();

            @Nullable Player player = null;

            if (type == PlayerType.HUMAN) {
                player = new HumanPlayer(name, color, view);
            } else if (type == PlayerType.COMPUTER) {
                player = new ComputerPlayer(name, color, new ComputerPlayerView());
            } else {
                assert false;
            }

            players.add(player);
            // TODO: make the player's view non-null.
            @Nullable GameView playerView = player.getView();
            if (playerView != null) {
                views.add(playerView);
                // We don't get the views until we get the players, so we haven't set the map yet.
                playerView.setMap(map);
            }

            if (i == currentPlayerIndex) {
                currentPlayer = player;
            }

            Set<Unit> units = (Set<Unit>) in.readObject();
            for (Unit unit : units) {
                unit.setOwner(player);
                player.addUnit(unit);
                unit.setHex(map.getHex(unit.getPosition()));
                map.addUnit(unit, unit.getPosition());
                if (playerView != null) {
                    playerView.addUnit(unit);
                }
            }
        }

        if (currentPlayer == null) {
            currentPlayer = players.get(0);
        }

        in.close();

        DefaultModel model = new DefaultModel(map, players, views, currentPlayer);
        model.startTurnManager();
        return model;
    }

    public void startTurnManager() {
        getTurnManager().start(currentPlayer);
    }

    DefaultModel(HexMap map, List<Player> players, List<GameView> views, Player currentPlayer) {
        this.map = map;
        this.players = players;
        this.views = new ViewMultiplexer(views);
        this.currentPlayer = currentPlayer;
        turnManager = new TurnManager(players, this);
    }

    private Player currentPlayer;

    private final transient Logger log = Logger.getLogger(getClass().getName());

    private final HexMap map;

    private final List<Player> players;

    private final transient ViewMultiplexer views;

    @Nullable private Unit selectedUnit;

    private final TurnManager turnManager;

    @Serial
    private static final long serialVersionUID = 1L;
}
