package org.heckcorp.spacegame.map.javafx;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.heckcorp.spacegame.JavaFxModel;
import org.heckcorp.spacegame.Player;
import org.heckcorp.spacegame.Unit;
import org.heckcorp.spacegame.Util;
import org.heckcorp.spacegame.map.MouseButton;
import org.heckcorp.spacegame.map.Point;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ControllerPane extends Pane {
    public void onMouseClicked(MouseEvent mouseEvent) {
        Point2D position = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        Point hexCoordinates = mapUtils.getHexCoordinates(position);
        model.hexClicked(hexCoordinates, getMouseButton(mouseEvent));
    }

    private MouseButton getMouseButton(MouseEvent mouseEvent) {
        return switch (mouseEvent.getButton()) {
            case PRIMARY -> MouseButton.PRIMARY;
            case SECONDARY -> MouseButton.SECONDARY;
            default -> MouseButton.UNKNOWN;
        };
    }

    public ControllerPane(JavaFxModel model, GameViewPane view, MapUtils mapUtils) throws FileNotFoundException {
        this.model = model;
        this.mapUtils = mapUtils;

        model.selectedHexPositionProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                view.unselectHex();
            } else {
                view.selectHex(newValue.get());
            }
        });
        model.unitsProperty().addListener((observable, oldValue, newValue) -> {
            Sets.difference(oldValue, newValue).forEach(u -> view.removeCounter(unitCounters.get(u)));
            Sets.difference(newValue, oldValue).forEach(u -> {
                Player.Color color = u.getOwner().getColor();
                unitCounters.put(u, new Counter(SPACESHIP_IMAGE_STREAM, new Color(color.r(), color.g(), color.b(), 1.0)));
                view.addCounter(unitCounters.get(u), model.unitPositionsProperty().get().get(u));
            });
        });
        model.unitPositionsProperty().addListener((observable, oldValue, newValue) ->
                Maps.difference(oldValue, newValue).entriesDiffering().forEach((u, d) ->
                        view.moveCounter(unitCounters.get(u), d.leftValue(), d.rightValue())));
    }

    private final JavaFxModel model;
    private final MapUtils mapUtils;
    private final Map<Unit, Counter> unitCounters = new HashMap<>();

    private final InputStream SPACESHIP_IMAGE_STREAM = Util.getResource("resource/spaceship.png");
}
