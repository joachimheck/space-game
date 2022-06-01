package org.heckcorp.spacegame;

import java.awt.*;
import java.io.Serial;

public final class NeutralPlayer extends Player {
    @Serial
    private static final long serialVersionUID = 1L;

    public NeutralPlayer(String name, Color color) {
        super(name, color, null);
    }

    @Override
    public void move(GameModel model) {
        // Do nothing.  Neutral units don't move.
    }
}
