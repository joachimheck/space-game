package org.heckcorp.spacegame.ui.map;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
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
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.jetbrains.annotations.Nullable;

import static org.heckcorp.spacegame.Constants.MAP_HEIGHT;
import static org.heckcorp.spacegame.Constants.MAP_WIDTH;

public class MapPane extends StackPane {
  public void addCounter(Counter counter, Point position) {
    countersPane.getChildren().add(counter);
    Point2D pixelPos = mapUtils.getHexCenter(new Point(position.x(), position.y()));
    setCounterLocation(counter, pixelPos);
  }

  public void removeCounter(@Nullable Counter counter) {
    if (counter != null) {
      countersPane.getChildren().remove(counter);
    }
  }

  public void moveCounter(Counter counter, Point startHexPos, Point endHexPos) {
    Path path = new Path();
    Point2D endPos = mapUtils.getHexCenter(endHexPos);
    Point2D startPos = mapUtils.getHexCenter(startHexPos);
    path.getElements()
        .add(
            new MoveTo(
                counter.getLayoutBounds().getCenterX(), counter.getLayoutBounds().getCenterY()));
    path.getElements()
        .add(
            new LineTo(
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
    countersPane.getChildren().add(selectionHexagon);

    Timeline timeline =
        new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(selectionHexagon.strokeDashOffsetProperty(), 20, Interpolator.LINEAR)),
            new KeyFrame(
                Duration.seconds(2),
                new KeyValue(selectionHexagon.strokeDashOffsetProperty(), 0, Interpolator.LINEAR)));
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
  }

  public void unselectHex() {
    selectedHex = NO_SELECTED_HEX;
    countersPane.getChildren().remove(selectionHexagon);
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

    SnapshotParameters params = new SnapshotParameters();
    params.setFill(Color.BLACK);
    return new ImageView(mapCanvas.snapshot(params, null));
  }

  private final MapUtils mapUtils;
  private Polygon selectionHexagon = new Polygon(0d, 0d);
  private final Pane countersPane;
  private Point selectedHex = NO_SELECTED_HEX;

  private static final Point NO_SELECTED_HEX = new Point(-1, -1);
}
