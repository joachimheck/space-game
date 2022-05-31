package org.heckcorp.spacegame;

import java.awt.Color;

import org.heckcorp.spacegame.Player;

public final class NeutralPlayer extends Player {
    private static final long serialVersionUID = 1L;

    public NeutralPlayer(String name, Color color) {
        super(name, color, null);
    }

    @Override
    public void move() {
        // Do nothing.  Neutral units don't move.
    }

}
