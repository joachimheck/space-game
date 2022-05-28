package org.heckcorp.domination;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ViewMultiplexer implements GameView {

    /**
     * @pre piece != null
     * @pre piece.getPosition != null
     */
    public void addGamePiece(GamePiece piece) {
        for (GameView view : gameViews) {
            view.addGamePiece(piece);
        }
    }

    public void addGameView(GameView view) {
        gameViews.add(view);
    }

    public void attack(Unit attacker, Unit target) {
        for (GameView view : gameViews) {
            view.attack(attacker, target);
        }
    }

    public void message(String message) {
        for (GameView view : gameViews) {
            view.message(message);
        }
    }

    public void move(Unit unit, Direction direction) {
        for (GameView view : gameViews) {
            log.finer(view + ".move(" + unit + "," + direction + ")");
            view.move(unit, direction);
        }
    }

    public void selectHex(Hex hex) {
        for (GameView view : gameViews) {
            view.selectHex(hex);
        }
    }

    public void setCurrentPlayer(String playerName) {
        for (GameView view : gameViews) {
            view.setCurrentPlayer(playerName);
        }
    }

    public void setMap(HexMap map) {
        for (GameView view : gameViews) {
            view.setMap(map);
        }
    }

    public void setMonitor(ViewMonitor monitor) {
        for (GameView view : gameViews) {
            view.setMonitor(monitor);
        }
    }

    public void setStatus(Unit unit, Status status) {
        for (GameView view : gameViews) {
            log.finer("Setting status for view " + view);
            view.setStatus(unit, status);
        }
    }

    // TODO: remove?
    public void setWinningPlayer(String playerName, Color playerColor) {
        for (GameView view : gameViews) {
            view.setWinningPlayer(playerName, playerColor);
        }
    }

    private final transient Set<GameView> gameViews = new HashSet<>();
    private final transient Logger log = Logger.getLogger(getClass().getName());
}
