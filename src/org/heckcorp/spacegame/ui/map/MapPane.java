package org.heckcorp.spacegame.ui.map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.heckcorp.spacegame.model.Direction;
import org.heckcorp.spacegame.model.MapPosition;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static org.heckcorp.spacegame.Constants.*;

public class MapPane extends StackPane {
  public void addCounter(Counter counter, MapPosition position) {
    runLaterSequentially(
        () -> {
          countersPane.getChildren().add(counter);
          Point2D pixelPos =
              mapUtils.getHexCenter(new Point(position.position().x(), position.position().y()));
          setCounterLocation(counter, pixelPos);
          counter.setRotate(60 * position.direction().getDirection());
        });
  }

  public void moveCounter(Counter counter, MapPosition startMapPos, MapPosition endMapPos) {
    ParallelTransition parallelTransition = new ParallelTransition(counter);

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
      PathTransition pathTransition = new PathTransition(ANIMATION_DURATION, path);
      pathTransition.setOrientation(PathTransition.OrientationType.NONE);
      pathTransition.setOnFinished(event -> setCounterLocation(counter, endPos));
      parallelTransition.getChildren().add(pathTransition);
    }

    if (!startMapPos.direction().equals(endMapPos.direction())) {
      RotateTransition rotateTransition = new RotateTransition(ANIMATION_DURATION);
      rotateTransition.setFromAngle(60.0 * startMapPos.direction().getDirection());
      rotateTransition.setToAngle(getClosestAngle(startMapPos.direction(), endMapPos.direction()));
      rotateTransition.setByAngle(5);
      rotateTransition.setOnFinished(
          event -> counter.setRotate(60 * endMapPos.direction().getDirection()));
      parallelTransition.getChildren().add(rotateTransition);
    }

    playSequentially(parallelTransition);
  }

  public void removeCounter(@Nullable Counter counter) {
    if (counter != null) {
      runLaterSequentially(() -> countersPane.getChildren().remove(counter));
    }
  }

  public void selectHexes(Point hexCoordinates) {
    runLaterSequentially(
        () -> {
          selectedHexes.clear();
          selectedHexes.addAll(selectHexes(Color.YELLOW, ImmutableSet.of(hexCoordinates)));
        });
  }

  public void unselectHex() {
    runLaterSequentially(
        () -> {
          countersPane.getChildren().removeAll(selectedHexes);
          selectedHexes.clear();
        });
  }

  public void setTargetHexes(ImmutableSet<? extends Point> hexes) {
    runLaterSequentially(
        () -> {
          countersPane.getChildren().removeAll(targetHexes);
          targetHexes.clear();
          targetHexes.addAll(selectHexes(Color.RED, hexes));
        });
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

  private void playSequentially(Animation animation) {
    sequentialAnimationExecutor.submit(
        () -> {
          CompletableFuture<Void> future = new CompletableFuture<>();
          animation.setOnFinished(e -> future.completeAsync(() -> null));
          animation.play();
          try {
            future.get();
          } catch (InterruptedException | ExecutionException e) {
            // TODO: handle.
            throw new RuntimeException(e);
          }
        });
  }

  private void runLaterSequentially(Runnable r) {
    sequentialAnimationExecutor.submit(() -> Platform.runLater(r));
  }

  private Set<Shape> selectHexes(Color color, Set<? extends Point> hexCoordinates) {
    Set<Shape> hexagons =
        hexCoordinates.stream()
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

  public static MapPane create(MapUtils mapUtils, MapModel model, ExecutorService sequentialAnimationExecutor) {
    MapPane mapPane = new MapPane(mapUtils, sequentialAnimationExecutor);
    // Not sure why I need this but without it, one line of background shows through at the bottom.
    mapPane.setBackground(
        new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

    Node mapCanvas = buildMapCanvas(mapUtils, MAP_WIDTH, MAP_HEIGHT);
    Pane countersPane = mapPane.countersPane;
    ControllerPane controllerPane = new ControllerPane(model, mapUtils);
    mapPane.getChildren().addAll(mapCanvas, countersPane, controllerPane);

    controllerPane.setOnMouseClicked(controllerPane::onMouseClicked);

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

  private MapPane(MapUtils mapUtils, ExecutorService sequentialAnimationExecutor) {
    this.mapUtils = mapUtils;
    this.countersPane = new Pane();
    this.sequentialAnimationExecutor = sequentialAnimationExecutor;
  }

  private final Pane countersPane;
  private final MapUtils mapUtils;
  private final Set<Shape> selectedHexes = Sets.newHashSet();
  private final ExecutorService sequentialAnimationExecutor;
  private final Set<Shape> targetHexes = Sets.newHashSet();
}
