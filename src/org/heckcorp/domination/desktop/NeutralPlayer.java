package org.heckcorp.domination.desktop;

import java.awt.Color;

import org.heckcorp.domination.Player;

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
