package org.heckcorp.spacegame.map.javafx;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import org.heckcorp.spacegame.map.Point;

public class GameViewPane extends Pane {
    public void addCounter(Counter counter, Point position) {
        getChildren().add(counter);
        Point2D pixelPos = mapUtils.getHexCenter(new Point(position.x(), position.y()));
        setCounterLocation(counter, pixelPos, counter.getWidth(), counter.getHeight());
    }

    public void removeCounter(Counter counter) {
        getChildren().remove(counter);
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

    public GameViewPane(MapUtils mapUtils) {
        this.mapUtils = mapUtils;
    }

    private final MapUtils mapUtils;
    private Polygon selectionHexagon = new Polygon(0d, 0d);
    private Point selectedHex = NO_SELECTED_HEX;

    private static final Point NO_SELECTED_HEX = new Point(-1, -1);
}
