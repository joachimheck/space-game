package org.heckcorp.spacegame.ui;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.collections.ObservableSet;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.heckcorp.spacegame.model.MapPosition;
import org.heckcorp.spacegame.model.Model;
import org.heckcorp.spacegame.model.Player;
import org.heckcorp.spacegame.model.Unit;
import org.heckcorp.spacegame.ui.map.*;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.heckcorp.spacegame.Constants.*;

@DefaultQualifier(NonNull.class)
public class GameViewPane extends VBox {

  public void addUnit(Unit unit, MapPosition position, Player.Color color) {
    Counter counter =
        Counter.build(viewResources, unit.getImageId(), color.r(), color.g(), color.b());
    unitCounters.put(unit, counter);
    mapPane.addCounter(counter, position);
  }

  public void moveUnit(Unit unit, MapPosition startPos, MapPosition endHexPos) {
    @Nullable Counter counter = unitCounters.get(unit);
    assert counter != null : "@AssumeAssertion(nullness)";
    mapPane.moveCounter(counter, startPos, endHexPos);
  }

  public void removeUnit(Unit unit) {
    mapPane.removeCounter(unitCounters.remove(unit));
  }

  public void selectHex(Point hexCoordinates) {
    mapPane.selectHexes(hexCoordinates);
  }

  public void selectUnit(@Nullable Unit unit) {
    if (unit == null) {
      descriptionPane.clear();
    } else {
      descriptionPane.setSelectedUnitData(unit);
    }
  }

  public void setCurrentPlayer(Player player) {
    descriptionPane.setCurrentPlayer(player);
  }

  public void setTargetHexes(ObservableSet<? extends Point> hexes) {
    mapPane.setTargetHexes(hexes);
  }

  public void setWinner(String winner) {
    Alert winAlert = new Alert(Alert.AlertType.INFORMATION);
    winAlert.setTitle("Game Over");
    winAlert.setContentText("Game over. " + winner + " wins.");
    sequentialAnimationExecutor.submit(winAlert::show);
  }

  public void targetUnit(Unit selectedUnit, @Nullable Unit unit) {
    if (unit != null) {
      descriptionPane.setTargetUnitData(selectedUnit, unit);
    }
  }

  public void unselectHex() {
    mapPane.unselectHex();
    mapPane.setTargetHexes(Sets.newHashSet());
    descriptionPane.clear();
  }

  private static @NonNull MenuBar createMenuBar(Model model) {
    MenuItem endTurn = new MenuItem("End Turn");
    endTurn.setOnAction(event -> model.endTurn());
    return new MenuBar(new Menu("File"), new Menu("Game", null, endTurn), new Menu("Unit"));
  }

  public static GameViewPane create(Model model, MapUtils mapUtils, ViewResources viewResources)
      throws FileNotFoundException {
    ExecutorService sequentialAnimationExecutor = Executors.newSingleThreadExecutor();
    MapPane mapPane = MapPane.create(mapUtils, model, sequentialAnimationExecutor);
    mapPane.setBorder(
        new Border(
            new BorderStroke(
                Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(10))));
    ScrollPane mapScrollPane = new ScrollPane(mapPane);
    mapScrollPane.setPrefSize(UI_COMPONENT_LARGE_WIDTH, UI_COMPONENT_LARGE_HEIGHT);
    DescriptionPane descriptionPane = DescriptionPane.create(model, mapUtils, viewResources);
    descriptionPane.setPrefSize(UI_COMPONENT_SMALL_WIDTH, UI_COMPONENT_LARGE_HEIGHT);
    ScrollPane textScrollPane = new ScrollPane(new Text("Text pane!"));
    textScrollPane.setPrefSize(UI_COMPONENT_LARGE_WIDTH, UI_COMPONENT_SMALL_HEIGHT);
    Canvas miniMapPane = new Canvas(UI_COMPONENT_SMALL_WIDTH, UI_COMPONENT_SMALL_HEIGHT);
    GridPane gridPane = new GridPane();
    GridPane.setConstraints(mapScrollPane, 0, 1);
    GridPane.setConstraints(descriptionPane, 1, 1);
    GridPane.setConstraints(textScrollPane, 0, 2);
    GridPane.setConstraints(miniMapPane, 1, 2);
    gridPane.getChildren().addAll(mapScrollPane, descriptionPane, textScrollPane);
    MenuBar menuBar = createMenuBar(model);
    GameViewPane gameViewPane = new GameViewPane(mapPane, descriptionPane, sequentialAnimationExecutor, viewResources);
    gameViewPane.getChildren().addAll(menuBar, gridPane);
    return gameViewPane;
  }

  private GameViewPane(
      MapPane mapPane, DescriptionPane descriptionPane, ExecutorService sequentialAnimationExecutor, ViewResources viewResources) {
    this.mapPane = mapPane;
    this.descriptionPane = descriptionPane;
    this.sequentialAnimationExecutor = sequentialAnimationExecutor;
    this.viewResources = viewResources;
  }


  private final MapPane mapPane;
  private final DescriptionPane descriptionPane;
  private final ExecutorService sequentialAnimationExecutor;
  private final Map<Unit, Counter> unitCounters = Maps.newHashMap();
  private final ViewResources viewResources;
}
