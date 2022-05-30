package org.heckcorp.spacegame;

import org.heckcorp.spacegame.desktop.ComputerPlayer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

// TODO: merge this into ComputerPlayer itself.
public class ComputerPlayerView implements GameView {
    private ComputerPlayer player;
    private HexMap map;
    private final Set<Unit> knownEnemies = new HashSet<>();
    private final Logger log;

    public ComputerPlayerView() {
        super();
        log = Logger.getLogger(getClass().getName());
    }

    public void addUnit(@NotNull Unit unit) {
        if (unit.getOwner() != player) {
            knownEnemies.add(unit);
        }
    }

    public void attack(Unit attacker, Unit target) {
        // TODO: implement?
    }

    public void move(Unit unit, Direction direction) {
        if (unit.getOwner() == player) {
            for (Hex hex : map.getHexesInRange(unit.getHex(), 1)) {
                for (Unit hexUnit : hex.getUnits()) {
                    if (hexUnit.getOwner() != player) {
                        knownEnemies.add(hexUnit);
                    }
                }
            }
        } else {
            knownEnemies.add(unit);
        }
    }

    public void selectHex(Hex hex) {
        // TODO: implement?
    }

    public void setCurrentPlayer(String playerName) {
        // TODO: implement?
    }

    public void setMap(HexMap map) {
        this.map = map;
    }

    @Override
    public void initialize() {
    }

    public void setMonitor(ViewMonitor monitor) {
        // TODO: implement? Use this to communicate with the player?
    }

    public void setStatus(Unit unit, Status status) {
        log.entering("ComputerPlayerView", "setStatus");
        if (status == Status.SELECTED && unit.getOwner() == player) {
//            player.setReadyUnit(unit);
        } else if (status == Status.DESTROYED) {
            knownEnemies.remove(unit);
        }
        log.exiting("ComputerPlayerView", "setStatus");
    }

    public void setWinningPlayer(String playerName, Color playerColor) { }

    public HexMap getMap() {
        return map;
    }

    public void setPlayer(ComputerPlayer player) {
        this.player = player;
    }

    public Set<Unit> getKnownEnemies() {
        return knownEnemies;
    }

    public void message(String message) {
        // Ignore.
    }

}
