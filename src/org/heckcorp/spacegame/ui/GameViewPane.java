package org.heckcorp.spacegame.ui;

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
import org.heckcorp.spacegame.model.Unit;
import org.heckcorp.spacegame.ui.map.Counter;
import org.heckcorp.spacegame.ui.map.MapPane;
import org.heckcorp.spacegame.ui.map.MapUtils;
import org.heckcorp.spacegame.ui.map.Point;

import java.io.FileNotFoundException;

import static org.heckcorp.spacegame.Constants.*;

@DefaultQualifier(NonNull.class)
public class GameViewPane extends VBox {

  public void addCounter(Counter counter, MapPosition position) {
    mapPane.addCounter(counter, position);
  }

  public void removeCounter(@Nullable Counter counter) {
    mapPane.removeCounter(counter);
  }

  public void moveCounter(Counter counter, MapPosition startPos, MapPosition endHexPos) {
    mapPane.moveCounter(counter, startPos, endHexPos);
  }

  public void selectHex(Point hexCoordinates) {
    mapPane.selectHexes(hexCoordinates);
  }

  public void unselectHex() {
    mapPane.unselectHex();
    mapPane.setTargetHexes(Sets.newHashSet());
    hexDescriptionPane.clear();
  }

  public void selectUnit(@Nullable Unit unit) {
    if (unit == null) {
      hexDescriptionPane.clear();
    } else {
      hexDescriptionPane.setSelectedUnitData(unit);
    }
  }

  public void targetUnit(Unit selectedUnit, @Nullable Unit unit) {
    if (unit != null) {
      hexDescriptionPane.setTargetUnitData(selectedUnit, unit);
    }
  }

  public void setTargetHexes(ObservableSet<? extends Point> hexes) {
    mapPane.setTargetHexes(hexes);
  }

  public static GameViewPane create(Model model, MapUtils mapUtils) throws FileNotFoundException {
    MapPane mapPane = MapPane.create(mapUtils, model);
    mapPane.setBorder(
        new Border(
            new BorderStroke(
                Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(10))));
    ScrollPane mapScrollPane = new ScrollPane(mapPane);
    mapScrollPane.setPrefSize(UI_COMPONENT_LARGE_WIDTH, UI_COMPONENT_LARGE_HEIGHT);
    HexDescriptionPane hexDescriptionPane = HexDescriptionPane.create(model);
    hexDescriptionPane.setPrefSize(UI_COMPONENT_SMALL_WIDTH, UI_COMPONENT_LARGE_HEIGHT);
    ScrollPane textScrollPane = new ScrollPane(new Text("Text pane!"));
    textScrollPane.setPrefSize(UI_COMPONENT_LARGE_WIDTH, UI_COMPONENT_SMALL_HEIGHT);
    Canvas miniMapPane = new Canvas(UI_COMPONENT_SMALL_WIDTH, UI_COMPONENT_SMALL_HEIGHT);
    GridPane gridPane = new GridPane();
    GridPane.setConstraints(mapScrollPane, 0, 1);
    GridPane.setConstraints(hexDescriptionPane, 1, 1);
    GridPane.setConstraints(textScrollPane, 0, 2);
    GridPane.setConstraints(miniMapPane, 1, 2);
    gridPane.getChildren().addAll(mapScrollPane, hexDescriptionPane, textScrollPane);
    MenuBar menuBar = createMenuBar(model);
    GameViewPane gameViewPane = new GameViewPane(mapPane, hexDescriptionPane);
    gameViewPane.getChildren().addAll(menuBar, gridPane);
    return gameViewPane;
  }

  private static @NonNull MenuBar createMenuBar(Model model) {
    MenuItem endTurn = new MenuItem("End Turn");
    endTurn.setOnAction(event -> model.endTurn());
    return new MenuBar(new Menu("File"), new Menu("Game", null, endTurn), new Menu("Unit"));
  }

  private GameViewPane(MapPane mapPane, HexDescriptionPane hexDescriptionPane) {
    this.mapPane = mapPane;
    this.hexDescriptionPane = hexDescriptionPane;
  }

  private final MapPane mapPane;

  private final HexDescriptionPane hexDescriptionPane;

  public void setWinner(String winner) {
    Alert winAlert = new Alert(Alert.AlertType.INFORMATION);
    winAlert.setTitle("Game Over");
    winAlert.setContentText("Game over. " + winner + " wins.");
    winAlert.show();
  }
}
