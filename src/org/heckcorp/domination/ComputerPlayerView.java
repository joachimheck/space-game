package org.heckcorp.domination;

import org.heckcorp.domination.desktop.ComputerPlayer;

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
    
    public void addGamePiece(GamePiece piece) {
        if (piece instanceof City) {
            knownCities.add((City) piece);
        }

        if (piece instanceof Unit && piece.getOwner() != player) {
            knownEnemies.add((Unit) piece);
        }
    }

    public void attack(Unit attacker, Unit target) {
        // TODO: implement?
    }

    public void move(Unit unit, Direction direction) {
        if (unit.getOwner() == player) {
            for (Hex hex : map.getHexesInRange(unit.getHex(), 1)) {
                if (hex.getCity() != null) {
                    knownCities.add(hex.getCity());
                }

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

    public void setWinningPlayer(String playerName, Color playerColor) {
    }

    public HexMap getMap() {
        return map;
    }

    public void setPlayer(ComputerPlayer player) {
        this.player = player;
    }

    public Set<City> getKnownEnemyCities() {
        // TODO: this would probably be more efficient with an iterator,
        // or if we just kept a set of enemy cities all the time.
        Set<City> enemyCities = new HashSet<>();
        for (City city : knownCities) {
            if (city.getOwner() != player) {
                enemyCities.add(city);
            }
        }
        return enemyCities;
    }
    
    final Set<City> knownCities = new HashSet<>();

    public Set<Unit> getKnownEnemies() {
        return knownEnemies;
    }

    public void message(String message) {
        // Ignore.
    }

}
