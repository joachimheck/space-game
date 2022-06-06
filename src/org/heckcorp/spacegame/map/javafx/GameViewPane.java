package org.heckcorp.spacegame.map.javafx;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import org.heckcorp.spacegame.map.Point;

public class GameViewPane extends Pane {
    public void addCounter(Counter counter, Point position) {
        getChildren().add(counter);
        Point2D pixelPos = mapUtils.getHexCenter(new Point(position.x(), position.y()));
        setCounterLocation(counter, pixelPos);
    }

    public void removeCounter(Counter counter) {
        getChildren().remove(counter);
    }

    public void moveCounter(Counter counter, Point startHexPos, Point endHexPos) {
        Path path = new Path();
        Point2D endPos = mapUtils.getHexCenter(endHexPos);
        Point2D startPos = mapUtils.getHexCenter(startHexPos);
        path.getElements().add(new MoveTo(counter.getLayoutBounds().getCenterX(), counter.getLayoutBounds().getCenterY()));
        path.getElements().add(new LineTo(
                endPos.getX() - startPos.getX() + counter.getLayoutBounds().getCenterX(),
                endPos.getY() - startPos.getY() + counter.getLayoutBounds().getCenterY()));
        PathTransition pathTransition = new PathTransition(Duration.seconds(2), path, counter);
        pathTransition.setOrientation(PathTransition.OrientationType.NONE);
        pathTransition.setOnFinished(event -> setCounterLocation(counter, endPos));
        pathTransition.play();
    }
    //        Rotate rotate = new Rotate(90);
    //        imageView.getTransforms().add(rotate);

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

    private void setCounterLocation(Counter counter, Point2D location) {
        counter.setTranslateX(0);
        counter.setTranslateY(0);
        counter.relocate(
                location.getX() - counter.getLayoutBounds().getCenterX(),
                location.getY() - counter.getLayoutBounds().getCenterY());
    }

    public GameViewPane(MapUtils mapUtils) {
        this.mapUtils = mapUtils;
    }

    private final MapUtils mapUtils;
    private Polygon selectionHexagon = new Polygon(0d, 0d);
    private Point selectedHex = NO_SELECTED_HEX;

    private static final Point NO_SELECTED_HEX = new Point(-1, -1);
}
