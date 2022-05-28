package org.heckcorp.domination;

import org.heckcorp.domination.Player.PlayerType;
import org.heckcorp.domination.Unit.Type;
import org.heckcorp.domination.desktop.*;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * An implementation of a GameModel.
 * 
 * @author Joachim Heck
 */
public class DefaultModel implements GameModel, Serializable {
    public final class GameStateManager implements ModelInitializer {
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
        
        private ObjectInputStream in;
        public void setInputStream(ObjectInputStream in) {
            this.in = in;
        }
        
        @SuppressWarnings("unchecked")
        public void initializeModel(GameModel model, GameView mainPlayerView)
        throws IOException, ClassNotFoundException
        {
            HexMap map = new HexMap(in);
            model.setMap(map);
            
            int currentPlayerIndex = in.readInt();
            
            int playerCount = in.readInt();
            for (int i=0; i<playerCount; i++) {
                log.finer("Reading player #" + i);
                int typeNumber = in.readInt();
                PlayerType type = PlayerType.values()[typeNumber];
                
                String name = (String) in.readObject();
                Color color = (Color) in.readObject();
                ShadowMap shadowMap = (ShadowMap) in.readObject();
                
                Player player = null;
                
                if (type == PlayerType.HUMAN) {
                    player = new HumanPlayer(name, color, shadowMap, mainPlayerView);
                } else if (type == PlayerType.COMPUTER) {
                    player = new ComputerPlayer(name, color, shadowMap, model,
                                                new ComputerPlayerView());
                } else if (type == PlayerType.NEUTRAL) {
                    player = new NeutralPlayer(name, color, shadowMap);
                } else {
                    assert false;
                }

                model.addPlayer(player);
                if (i == currentPlayerIndex) {
                    currentPlayer = player;
                }
                
                Set<City> cities = (Set<City>) in.readObject();
                for (City city : cities) {
                    city.setOwner(player);
                    player.addGamePiece(city);
                    model.addGamePiece(city, city.getPosition());
                }
                
                Set<Unit> units = (Set<Unit>) in.readObject();
                for (Unit unit : units) {
                    unit.setOwner(player);
                    player.addGamePiece(unit);
                    model.addGamePiece(unit, unit.getPosition());
                }
            }
            
            in.close();
            
            model.startTurnManager();
        }
    }

    /**
     * Adds the piece to the model at the specified position.
     * @param piece
     * @pre piece != null
     * @pre position != null
     * @pre getMap().contains(position)
     */
    public void addGamePiece(GamePiece piece, Point position) {
        piece.setHex(map.getHex(position));
        map.addGamePiece(piece, piece.getPosition());

        // Update the exploration map.
        List<Point> visible = new ArrayList<Point>();
        List<Hex> adjacent = map.getHexesInRange(piece.getHex(), 1);
        adjacent.add(piece.getHex());

        for (Hex hex : adjacent) {
            visible.add(hex.getPosition());
        }

        Player owner = piece.getOwner();
        owner.getShadowMap().setExplored(visible);
        owner.getShadowMap().setVisible(visible);

        views.addGamePiece(piece);
    }

    /**
     * Adds the specified player to the model.
     * @param player
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
            player.getView().setShadowMap(player.getShadowMap());
        }
    }

    public void destroyUnit(Unit unit) {
        unit.getOwner().removeUnit(unit);
        unit.getHex().removeUnit(unit);
        setStatus(unit, Status.DESTROYED);
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
     * @return
     * @pre position != null
     * @pre position is within the map boundary
     * @pre the map has been set.
     * @post result != null
     */
    public Hex getHex(Point position) {
        return map.getHex(position);
    }

    /**
      * Returns the currently selected city.
      * @return
      */
     public City getSelectedCity() {
         return selectedCity;
     }

    /**
     * @pre all players have been added.
     */
     public TurnManager getTurnManager() {
        assert !players.isEmpty();

        if (turnManager == null) {
            turnManager = new TurnManager(players);
            turnManager.setModel(this);
        }

        return turnManager;
     }

    /**
     * Returns a list of all the units in the hex at the
     * specified point.
     * @param position
     * @return
     * @pre point != null
     * @pre getMap().contains(point)
     * @post result != null
     */
    public List<Unit> getUnits(Point position) {
        return map.getHex(position).getUnits();
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
            assert hex != null;

            // We only attack if the hex next to the unit was clicked.
            if (unit.getPath().size() == 1 && unit.canAttack(hex)) {
                log.finer("Unit " + unit + " attacking hex " + hex);
                Unit defender = hex.getBestDefender();
                views.attack(unit, defender);
                attack(hex);

                if (unit.canEnterHex(hex)) {
                    Direction direction = unit.getNextDirection();
                    unit.move();
                    unit.getOwner().updateShadowMap(map);
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
                    setStatus(selectedUnit, Status.UNSELECTED);
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
     * @param position
     * @pre position != null
     */
    public void selectHex(Point position) {
        if (selectedUnit != null) {
            setStatus(selectedUnit, Status.UNSELECTED);
        }

        Hex hex = map.getHex(position);

        if (hex.getOwner() == currentPlayer) {
            selectedCity = hex.getCity();

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
                setStatus(topUnit, Status.SELECTED);
            }
        } else {
            selectedCity = null;
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
                setStatus(selectedUnit, Status.UNSELECTED);
            }

            setStatus(unit, Status.SELECTED);
            unit.setAsleep(false);
        } else {
            log.fine("Can't select " + unit + ": no moves left.");
        }
        
        log.finer("Selected unit - now selecting hex.");
        
        selectedCity = unit.getHex().getCity();
        views.selectHex(unit.getHex());

        log.exiting("DefaultModel", "selectUnit");
    }

    public void setCurrentPlayer(Player player) {
        currentPlayer = player;
        views.setCurrentPlayer(player.getName());
    }

    /**
     * Sets this model's map to the specified hex map.
     * @param map
     * @pre  map != null
     * @pre this model's map is null
     * @uml.property  name="map"
     */
    public void setMap(HexMap map) {
        assert this.map == null;

        this.map = map;

        // Because we don't get the views until we get the players,
        // there's no point trying to set their maps here.
//        views.setMap(map);
    }

    /**
      * @pre selectedCity != null
      * @pre type != null
      */
    public void setSelectedCityProductionType(Type type) {
        selectedCity.setProductionType(type);
        views.selectHex(selectedCity.getHex());
    }

    /**
     * Sets the destination of the selected unit to the specified point.
     * @param destination
     * @pre destination != null
     * @pre destination is in the map
     * @post result == false || the selected unit has a path
     */
    public boolean setSelectedUnitDestination(Point destination) {
        boolean result = false;
        
        if (selectedUnit != null) {
            assert destination != null;
            assert map.isInMap(destination);

            Hex hex = map.getHex(destination);
            Pathfinder pathfinder = map.getPathfinder();
            List<Hex> path = pathfinder.findPath(selectedUnit, hex);

            if (!path.isEmpty()) {
                selectedUnit.setPath(path);
            }

            // TODO: we could notify the view that the path has been set.

            result = !path.isEmpty();
        }
        
        return result;
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
             setStatus(selectedUnit, Status.UNSELECTED);
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
             setStatus(selectedUnit, Status.UNSELECTED);
             // TODO: we could notify the view that the unit is asleep.
         }
     }
    /**
     * Hex hiding is toggled to the state opposite its current state.
     */
    public void toggleHexHiding() {
        Player player = currentPlayer;
//        for (Player player : players) {
            ShadowMap shadowMap = player.getShadowMap();
            shadowMap.setActive(!shadowMap.isActive());
//        }
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
     * @param hex
     * @return
     * @pre unit != null
     * @pre hex != null
     * @pre unit.getOwner() == the current player
     * @pre unit.canAttack(hex)
     */
    private void attack(Hex hex) {
        // Compute the results of the attack.
        Unit loser = selectedUnit.attack(hex);

        if (loser.getHealth() == Unit.Health.DESTROYED) {
            setStatus(loser, Status.DESTROYED);
            if (loser == selectedUnit) {
                setStatus(loser, Status.UNSELECTED);
            }
        } else if (loser.getHealth() == Unit.Health.DAMAGED) {
            setStatus(loser, Status.DAMAGED);
        }
    }
    /**
     * @pre !selectedUnit.getPath().isEmpty()
     */
    private boolean moveSelectedUnitOneHex() {
        Direction direction = selectedUnit.getNextDirection();
        log.finer("Model moving " + selectedUnit + " one hex.");
        
        boolean moved = selectedUnit.move();
        
        if (moved) {
            log.finer("Model moved " + selectedUnit + " to " +
                               selectedUnit.getHex());
            selectedUnit.getOwner().updateShadowMap(map);
            views.move(selectedUnit, direction);
            
            if (selectedUnit.isOutOfFuel()) {
                destroyUnit(selectedUnit);
            } else  {
                selectedCity = selectedUnit.getHex().getCity();
                views.selectHex(selectedUnit.getHex());
            }
        } else {
            log.fine("Model couldn't move " + selectedUnit +
                               " to " + selectedUnit.getPath().get(0));
        }

        log.finer("Model finished moving unit one hex.");
        return moved;
    }

    /**
     * Sets the status of the specified unit.  Possible statuses include
     * damaged, destroyed, hidden and selected.
     * @param unit
     * @param status
     * @pre unit != null
     * @pre status != null
     */
    private synchronized void setStatus(Unit unit, Status status) {
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
        
        if (status == Status.SELECTED) {
            assert selectedUnit == null;
            selectedUnit = unit;
        } else if (status == Status.UNSELECTED) {
            assert selectedUnit != null;
            selectedUnit = null;
        } else if (status == Status.HIDDEN) {
            // TODO: Remove this status!
        } else if (status == Status.REVEALED) {
            // TODO: Remove this status!
        } else if (status == Status.DAMAGED) {
//            unit.setHealth(Health.DAMAGED);
        } else if (status == Status.DESTROYED) {
//            unit.setHealth(Health.DESTROYED);
        } else if (status == Status.HEALTHY) {
//            unit.setHealth(Health.HEALTHY);
        } else if (status == Status.SKIPPED) {
            // TODO: Remove this status!
        }
        
        views.setStatus(unit, status);
    }

    private Player currentPlayer;

    private final transient Logger log = Logger.getLogger(getClass().getName());

    private HexMap map;

    private List<Player> players = new ArrayList<Player>();

    private City selectedCity;

    private Unit selectedUnit;

    private transient TurnManager turnManager;

    private transient ViewMultiplexer views = new ViewMultiplexer();

    private final transient GameStateManager gameStateManager = new GameStateManager();

    private static final long serialVersionUID = 1L;

    public void startTurnManager() {
        getTurnManager().start(currentPlayer);
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }
}