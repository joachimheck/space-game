package org.heckcorp.spacegame.map.javafx;

import com.google.common.collect.Sets;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.heckcorp.spacegame.JavaFxModel;
import org.heckcorp.spacegame.map.MouseButton;
import org.heckcorp.spacegame.map.Point;

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

    public ControllerPane(JavaFxModel model, GameViewPane view, MapUtils mapUtils) {
        this.model = model;
        this.mapUtils = mapUtils;

        model.currentSelectedHexPosition().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                view.unselectHex();
            } else {
                view.selectHex(newValue.get());
            }
        });
        model.currentUnits().addListener((observable, oldValue, newValue) -> {
            view.removeUnits(Sets.difference(oldValue, newValue));
            view.addUnits(Sets.difference(newValue, oldValue));
        });
    }

    private final JavaFxModel model;
    private final MapUtils mapUtils;
}
