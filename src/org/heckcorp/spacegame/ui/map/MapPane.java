package org.heckcorp.spacegame.ui.map;

import com.google.common.collect.Sets;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.heckcorp.spacegame.model.Direction;
import org.heckcorp.spacegame.model.MapPosition;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.heckcorp.spacegame.Constants.*;

public class MapPane extends StackPane {
  public void addCounter(Counter counter, MapPosition position) {
    countersPane.getChildren().add(counter);
    Point2D pixelPos =
        mapUtils.getHexCenter(new Point(position.position().x(), position.position().y()));
    setCounterLocation(counter, pixelPos);
    counter.setRotate(60 * position.direction().getDirection());
  }

  public void removeCounter(@Nullable Counter counter) {
    if (counter != null) {
      countersPane.getChildren().remove(counter);
    }
  }

  public void moveCounter(Counter counter, MapPosition startMapPos, MapPosition endMapPos) {
    ParallelTransition parallelTransition = new ParallelTransition(counter);
    Duration duration = Duration.seconds(2);

    Point2D startPos = mapUtils.getHexCenter(startMapPos.position());
    Point2D endPos = mapUtils.getHexCenter(endMapPos.position());
    if (!startPos.equals(endPos)) {
      Path path = new Path();
      path.getElements()
          .add(
              new MoveTo(
                  counter.getLayoutBounds().getCenterX(), counter.getLayoutBounds().getCenterY()));
      path.getElements()
          .add(
              new LineTo(
                  endPos.getX() - startPos.getX() + counter.getLayoutBounds().getCenterX(),
                  endPos.getY() - startPos.getY() + counter.getLayoutBounds().getCenterY()));
      PathTransition pathTransition = new PathTransition(duration, path);
      pathTransition.setOrientation(PathTransition.OrientationType.NONE);
      pathTransition.setOnFinished(event -> setCounterLocation(counter, endPos));
      parallelTransition.getChildren().add(pathTransition);
    }

    if (!startMapPos.direction().equals(endMapPos.direction())) {
      RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1));
      rotateTransition.setFromAngle(60.0 * startMapPos.direction().getDirection());
      rotateTransition.setToAngle(getClosestAngle(startMapPos.direction(), endMapPos.direction()));
      rotateTransition.setByAngle(5);
      rotateTransition.setOnFinished(
          event -> counter.setRotate(60 * endMapPos.direction().getDirection()));
      parallelTransition.getChildren().add(rotateTransition);
    }

    parallelTransition.play();
  }

  private double getClosestAngle(Direction startDirection, Direction endDirection) {
    double startAngle = 60.0 * startDirection.getDirection();
    double endAngle = 60.0 * endDirection.getDirection();
    if (endAngle - startAngle > 180.0) {
      return endAngle - 360.0;
    } else if (endAngle - startAngle < -180.0) {
      return endAngle + 360.0;
    } else {
      return endAngle;
    }
  }

  public void selectHexes(Point hexCoordinates) {
    selectedHexes.clear();
    selectedHexes.addAll(selectHexes(Color.YELLOW, hexCoordinates));
  }

  public void unselectHex() {
    countersPane.getChildren().removeAll(selectedHexes);
    selectedHexes.clear();
  }

  public void setTargetHexes(Set<? extends Point> hexes) {
    countersPane.getChildren().removeAll(targetHexes);
    targetHexes.clear();
    targetHexes.addAll(selectHexes(Color.RED, hexes.toArray(new Point[0])));
  }

  private Set<Shape> selectHexes(Color color, Point... hexCoordinates) {
    Set<Shape> hexagons =
        Arrays.stream(hexCoordinates)
            .map(
                point -> {
                  Shape hexagon = mapUtils.getHexagon(point);
                  hexagon.getStrokeDashArray().setAll(10d, 10d);
                  hexagon.setStrokeWidth(2);
                  hexagon.setStroke(color);
                  hexagon.setFill(Color.TRANSPARENT);
                  return hexagon;
                })
            .collect(Collectors.toSet());

    countersPane.getChildren().addAll(hexagons);

    hexagons.forEach(
        hexagon -> {
          Timeline timeline =
              new Timeline(
                  new KeyFrame(
                      Duration.ZERO,
                      new KeyValue(hexagon.strokeDashOffsetProperty(), 20, Interpolator.LINEAR)),
                  new KeyFrame(
                      Duration.seconds(2),
                      new KeyValue(hexagon.strokeDashOffsetProperty(), 0, Interpolator.LINEAR)));
          timeline.setCycleCount(Timeline.INDEFINITE);
          timeline.play();
        });

    return hexagons;
  }

  private void setCounterLocation(Counter counter, Point2D location) {
    counter.setTranslateX(0);
    counter.setTranslateY(0);
    counter.relocate(
        location.getX() - counter.getLayoutBounds().getCenterX(),
        location.getY() - counter.getLayoutBounds().getCenterY());
  }

  private MapPane(MapUtils mapUtils) {
    this.mapUtils = mapUtils;
    this.countersPane = new Pane();
  }

  public static MapPane create(MapUtils mapUtils, MapModel model) {
    MapPane mapPane = new MapPane(mapUtils);
    BorderPane theMapPane = new BorderPane(buildMapCanvas(mapUtils, MAP_WIDTH, MAP_HEIGHT));
    Pane countersPane = mapPane.countersPane;
    ControllerPane controllerPane = new ControllerPane(model, mapUtils);
    controllerPane.setOnMouseClicked(controllerPane::onMouseClicked);
    mapPane.getChildren().addAll(theMapPane, countersPane, controllerPane);
    return mapPane;
  }

  public static Node buildMapCanvas(MapUtils mapUtils, int width, int height) {
    Pane mapCanvas = new Pane();

    mapCanvas.setBackground(
        new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        Point position = new Point(i, j);
        Polygon hexagon = mapUtils.getHexagon(position);
        hexagon.setStroke(Color.WHITE);
        mapCanvas.getChildren().add(hexagon);
        Point2D labelPos = mapUtils.getHexLabelPosition(position);
        Text text = new Text(labelPos.getX(), labelPos.getY(), position.x() + "," + position.y());
        text.setTextOrigin(VPos.TOP);
        text.setStroke(Color.WHITE);
        mapCanvas.getChildren().add(text);
      }
    }
    mapCanvas.layout();

    SnapshotParameters params = new SnapshotParameters();
    params.setFill(Color.BLACK);
    params.setViewport(
        new Rectangle2D(
            0,
            0,
            MAP_WIDTH * mapUtils.getColumnWidth() + (2.0 * HEX_RADIUS - mapUtils.getColumnWidth()),
            (MAP_HEIGHT + .5) * 2.0 * mapUtils.getMinorRadius()));
    return new ImageView(mapCanvas.snapshot(params, null));
  }

  private final MapUtils mapUtils;
  private final Set<Shape> selectedHexes = Sets.newHashSet();
  private final Set<Shape> targetHexes = Sets.newHashSet();
  private final Pane countersPane;
}
