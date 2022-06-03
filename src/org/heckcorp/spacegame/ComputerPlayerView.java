package org.heckcorp.spacegame;

import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.map.swing.ViewMonitor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

// TODO: merge this into ComputerPlayer itself.
public class ComputerPlayerView implements GameView {

    public ComputerPlayerView() {
        super();
    }

    public void addUnit(@NotNull Unit unit) {
    }

    public void attack(Unit attacker, Unit target) {
        // TODO: implement?
    }

    public void move(Unit unit, Direction direction) {
    }

    public void selectHex(Hex hex) {
        // TODO: implement?
    }

    public void setCurrentPlayer(String playerName) {
        // TODO: implement?
    }

    public void setMap(HexMap map) {
    }

    @Override
    public void initialize() {
    }

    public void setMonitor(ViewMonitor monitor) {
        // TODO: implement? Use this to communicate with the player?
    }

    public void setStatus(Unit unit, UnitStatus status) {
    }

    public void setWinningPlayer(String playerName, Color playerColor) { }

    public void message(String message) {
        // Ignore.
    }

}
