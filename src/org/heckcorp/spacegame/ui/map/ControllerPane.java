package org.heckcorp.spacegame.ui.map;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

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

    public ControllerPane(MapModel model, MapUtils mapUtils) {
        this.model = model;
        this.mapUtils = mapUtils;
    }

    private final MapModel model;
    private final MapUtils mapUtils;
}
