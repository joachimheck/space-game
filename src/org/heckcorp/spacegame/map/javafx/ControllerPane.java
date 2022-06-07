package org.heckcorp.spacegame.map.javafx;

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

    public ControllerPane(JavaFxModel model, MapUtils mapUtils) {
        this.model = model;
        this.mapUtils = mapUtils;
    }

    private final JavaFxModel model;
    private final MapUtils mapUtils;
}
