package org.heckcorp.spacegame;

import org.heckcorp.spacegame.map.MouseButton;
import org.heckcorp.spacegame.map.Point;
import org.heckcorp.spacegame.map.ViewMonitor;
import org.heckcorp.spacegame.map.javafx.GameViewPane;
import org.jetbrains.annotations.Nullable;

public class JavaFxViewMonitor implements ViewMonitor {
    @Override
    public void hexClicked(Point hexCoordinates, MouseButton button) {
        assert gameViewPane != null;
        gameViewPane.unselectHex();
        if (button == MouseButton.PRIMARY) {
            gameViewPane.selectHex(hexCoordinates);
        }
    }

    public void setGameViewPane(GameViewPane gameViewPane) {
        this.gameViewPane = gameViewPane;
    }

    @Nullable private GameViewPane gameViewPane;
}
