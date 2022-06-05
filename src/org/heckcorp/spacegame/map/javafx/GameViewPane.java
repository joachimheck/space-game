package org.heckcorp.spacegame.map.javafx;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import org.heckcorp.spacegame.Unit;
import org.heckcorp.spacegame.map.MouseButton;
import org.heckcorp.spacegame.map.Point;
import org.heckcorp.spacegame.map.ViewMonitor;
import org.heckcorp.spacegame.map.swing.Util;

import java.io.FileNotFoundException;

public class GameViewPane extends Pane {
    public void addUnit(Unit unit) {
        try {
            Image spaceshipImage = new Image(Util.getResource("resource/spaceship.png"));
            Counter counter = new Counter(spaceshipImage);
            getChildren().add(counter);
            Point2D pixelPos = mapUtils.getHexCenter(new Point(unit.getPosition().x(), unit.getPosition().y()));
            setCounterLocation(counter, pixelPos, spaceshipImage.getWidth(), spaceshipImage.getHeight());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCounterLocation(Counter counter, Point2D location, double width, double height) {
        counter.relocate(location.getX() - (width / 2.0), location.getY() - (height / 2.0));
    }

//        Rotate rotate = new Rotate(90);
//        imageView.getTransforms().add(rotate);
//        Path path = new Path();
//        path.getElements().add(new MoveTo(200, 200));
//        path.getElements().add(new CubicCurveTo(400, 40, 175, 250, 500, 150));
//        PathTransition pathTransition = new PathTransition(Duration.seconds(5), path, imageView);
//        pathTransition.setOrientation(PathTransition.OrientationType.NONE);
//        pathTransition.setAutoReverse(true);
//        pathTransition.play();

    public void onMouseClicked(MouseEvent mouseEvent) {
        Point2D position = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        Point hexCoordinates = mapUtils.getHexCoordinates(position);
        viewMonitor.hexClicked(hexCoordinates, getMouseButton(mouseEvent));
    }

    private MouseButton getMouseButton(MouseEvent mouseEvent) {
        return switch (mouseEvent.getButton()) {
            case PRIMARY -> MouseButton.PRIMARY;
            case SECONDARY -> MouseButton.SECONDARY;
            default -> MouseButton.UNKNOWN;
        };
    }

    public void selectHex(Point hexCoordinates) {
        selectedHex = hexCoordinates;
        selectionHexagon = mapUtils.getHexagon(selectedHex);
        selectionHexagon.getStrokeDashArray().setAll(10d, 10d);
        selectionHexagon.setStrokeWidth(2);
        selectionHexagon.setStroke(Color.YELLOW);
        selectionHexagon.setFill(Color.TRANSPARENT);
        getChildren().add(selectionHexagon);

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(
                                selectionHexagon.strokeDashOffsetProperty(),
                                20,
                                Interpolator.LINEAR
                        )
                ),
                new KeyFrame(
                        Duration.seconds(2),
                        new KeyValue(
                                selectionHexagon.strokeDashOffsetProperty(),
                                0,
                                Interpolator.LINEAR
                        )
                )
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void unselectHex() {
        selectedHex = NO_SELECTED_HEX;
        getChildren().remove(selectionHexagon);
    }

    public GameViewPane(MapUtils mapUtils, ViewMonitor viewMonitor) {
        this.mapUtils = mapUtils;
        this.viewMonitor = viewMonitor;
    }

    private final MapUtils mapUtils;
    private Polygon selectionHexagon = new Polygon(0d, 0d);
    private Point selectedHex = NO_SELECTED_HEX;
    private final ViewMonitor viewMonitor;

    private static final Point NO_SELECTED_HEX = new Point(-1, -1);
}
