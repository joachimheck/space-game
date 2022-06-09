package org.heckcorp.spacegame.map.javafx;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.heckcorp.spacegame.map.Point;
import org.heckcorp.spacegame.model.Model;
import org.heckcorp.spacegame.model.Unit;
import org.jetbrains.annotations.Nullable;

import static org.heckcorp.spacegame.Constants.*;

public class GameViewPane extends VBox {

  public void addCounter(Counter counter, Point position) {
    mapPane.addCounter(counter, position);
  }

  public void removeCounter(@Nullable Counter counter) {
    mapPane.removeCounter(counter);
  }

  public void moveCounter(Counter counter, Point startHexPos, Point endHexPos) {
    mapPane.moveCounter(counter, startHexPos, endHexPos);
  }

  public void selectHex(Point hexCoordinates) {
    mapPane.selectHex(hexCoordinates);
  }

  public void unselectHex() {
    mapPane.unselectHex();
    hexDescriptionPane.clear();
  }

  public void selectUnit(@Nullable Unit unit) {
    if (unit == null) {
      hexDescriptionPane.clear();
    } else {
      hexDescriptionPane.setSelectedUnitData(
          String.format(
              "%s's unit: health %d/%d",
              unit.getOwner().getName(), unit.getHealth(), unit.getMaxHealth()));
      hexDescriptionPane.setTargetUnitData("");
    }
  }

  public void targetUnit(Unit unit) {
    hexDescriptionPane.setTargetUnitData(
        String.format(
            "%s's unit: health %d/%d",
            unit.getOwner().getName(), unit.getHealth(), unit.getMaxHealth()));
  }

  // TODO: should the view classes be directly calling methods on the model?
  public static GameViewPane create(Model model, MapUtils mapUtils) {
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
    MenuBar menuBar = new MenuBar(new Menu("File"), new Menu("Game"), new Menu("Unit"));
    GameViewPane gameViewPane = new GameViewPane(mapPane, hexDescriptionPane);
    gameViewPane.getChildren().addAll(menuBar, gridPane);
    return gameViewPane;
  }

  private GameViewPane(MapPane mapPane, HexDescriptionPane hexDescriptionPane) {
    this.mapPane = mapPane;
    this.hexDescriptionPane = hexDescriptionPane;
  }

  private final MapPane mapPane;
  private final HexDescriptionPane hexDescriptionPane;
}
