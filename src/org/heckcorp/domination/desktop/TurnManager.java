package org.heckcorp.domination.desktop;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.heckcorp.domination.City;
import org.heckcorp.domination.DefaultModel;
import org.heckcorp.domination.Player;
import org.heckcorp.domination.Unit;

/**
 * The TurnManager runs through the turn, selecting units
 * and players after the previous ones finish.
 * 
 * @author Joachim Heck
 */
public class TurnManager implements Runnable, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * Determines whether the game has ended.
     */
    public Player getWinningPlayer() {
        Player onlyPlayer = null;

        // The game is over when all non-neutral cities belong to one player.
        for (Player player : players) {
            for (City city : player.getCities()) {
                if (!(city.getOwner() instanceof NeutralPlayer)) {
                    if (onlyPlayer == null) {
                        onlyPlayer = city.getOwner();
                    }

                    if (city.getOwner() != onlyPlayer) {
                        return null;
                    }
                }
            }
        }

        return onlyPlayer;
    }

    public void run() {
        assert model != null;

        Player winner = null;

        try {
            while (winner == null) {
                for (Player player : players) {
                    runPlayerTurn(player);
                }

                winner = getWinningPlayer();

                if (winner != null) {
                    log.info("Winner = " + winner);
                }
            }

            model.setWinningPlayer(winner);
        } catch (InterruptedException e) {
            log.fine("TurnManager interrupted!");
        }
    }
    
    private void runPlayerTurn(Player player) throws InterruptedException {
        // The first time through we may want to start
        // with a player other than the first one in the list.
        if (startPlayer == null || player == startPlayer) {
            startPlayer = null;

            startTurn(player);

            if (!(player instanceof NeutralPlayer)) {
                // Select each ready unit, and allow the player to
                // move them all.  Note that the player can move any
                // unit, not just the selected one!
                selectUnits(player);

                // Next move all units that have paths set.
                moveUnitsWithOrders(player);

                // Some units may have finished their orders but not
                // used up all their movement.  Let the player move them.
                selectUnits(player);
            }

            finishTurn(player);
        }
    }

    public void setModel(DefaultModel model) {
        this.model = model;
    }

    /**
     * @param startPlayer if not null, the player to start with.
     * @pre this TurnManager's model has been set.
     */
    public void start(Player startPlayer) {
        this.startPlayer = startPlayer;
        thread = new Thread(this);
        thread.start();
    }
    
    private transient Thread thread = null;
    
    public void interrupt() {
        log.fine("Interrupting turn manager.");
        thread.interrupt();
    }

    public void turnFinished() {
        turnOver = true;
    }
    
    private void finishTurn(Player player) {
        log.fine("Finishing turn for " + player);
        
        player.finishTurn();

        Set<Unit> toDestroy = new HashSet<Unit>();
        for (Unit unit : player.getUnits()) {
            // Reset each unit.
            unit.reset();

            // Make sure planes are in cities.
            if (unit.getType() == Unit.Type.BOMBER &&
                unit.getHex().getCity() == null)
            {
                toDestroy.add(unit);
            }
        }

        for (Unit unit : toDestroy) {
            model.destroyUnit(unit);
        }
    }

    /**
     * Return a unit that is ready for action and has no movement
     * orders.  If another unit is available, avoidUnit is not returned.
     * @param player
     * @param avoidUnit
     * @return
     */
    private Unit getReadyUnit(Player player, Unit avoidUnit) {
        Unit readyUnit = avoidUnit;
        
        if (avoidUnit == null || !avoidUnit.isReadyForAction() ||
            avoidUnit.getPath().size() > 0 || avoidUnit.isSkipped())
        {
            readyUnit = null;
        }

        for (Unit unit : player.getUnits()) {
            if (unit.isReadyForAction() && unit.getPath().size() == 0 &&
                unit != avoidUnit && !unit.isSkipped())
            {
                readyUnit = unit;
                break;
            } else {
//                log.finest("Unready: " + unit + " ready? " +
//                           unit.isReadyForAction() + " path size = " +
//                           unit.getPath().size());
            }
        }
        log.fine("Ready unit is: " + readyUnit);

        return readyUnit;
    }

    private void moveUnitsWithOrders(Player player) {
        log.fine("Moving units with orders for " + player);
        for (Unit unit : player.getUnits()) {
            while (unit.isReadyForAction() && !unit.getPath().isEmpty()) {
                log.finer("Moving unit with orders: " + unit);
                
                model.selectUnit(unit);
                model.moveSelectedUnit();
            }
        }
    }
    
    private void selectUnits(Player player) throws InterruptedException {
        log.fine("Selecting units for " + player);
        Unit unit = getReadyUnit(player, null);
        while (unit != null && !turnOver) {
            model.selectUnit(unit);
            player.setReadyUnit(unit);
            player.move();

            // We pass in the last unit so that when a unit
            // is waited, it doesn't immediately get selected again.
            unit = getReadyUnit(player, unit);
        }
    }

    private void startTurn(Player player) {
        log.fine("Starting turn for " + player);
        model.setCurrentPlayer(player);
        player.startTurn();
        turnOver = false;

        // Run production for cities.
        for (City city : player.getCities()) {
            city.incrementProductionPoints();
            Unit.Type type = city.getProductionType();

            if (city.getProductionPoints() >= type.getCost()) {
                Unit unit = new Unit(type, player);
                model.addGamePiece(unit, city.getPosition());
                city.setProductionPoints(0);
            }
        }
    }
    
    /**
     * @param players the players to iterate over.
     */
    public TurnManager(List<Player> players) {
        this.players = players;
        
        // The ready unit may still be set from before the game was saved.
        for (Player player : players) {
            player.clearReadyUnit();
        }
    }

    private final static Logger log = Logger.getLogger(TurnManager.class.getName());

    private DefaultModel model;
    
    private Player startPlayer;

    /**
     * @invariant never null.
     */
    private final List<Player> players;
    private boolean turnOver = false;
}
