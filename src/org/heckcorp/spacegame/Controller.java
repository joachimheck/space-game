package org.heckcorp.spacegame;

import javafx.collections.MapChangeListener;
import javafx.collections.SetChangeListener;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.heckcorp.spacegame.model.MapPosition;
import org.heckcorp.spacegame.model.Model;
import org.heckcorp.spacegame.model.Player;
import org.heckcorp.spacegame.model.Unit;
import org.heckcorp.spacegame.ui.GameViewPane;
import org.heckcorp.spacegame.ui.map.Point;

public class Controller {
  public void listenForPropertyChanges() {
    model
        .currentPlayerProperty()
        .addListener(
            (u1, u2, newValue) -> {
              view.setCurrentPlayer(newValue);
              aiView.setCurrentPlayer(newValue);
            });
    model
        .selectedHexPositionProperty()
        .addListener(
            (u1, u2, newValue) -> {
              if (newValue == null) {
                view.unselectHex();
              } else {
                view.unselectHex();
                view.selectHex(newValue);
              }
            });
    model
        .selectedUnitProperty()
        .addListener((observable, oldValue, newValue) -> view.selectUnit(newValue));
    model
        .targetUnitProperty()
        .addListener((observable, oldValue, newValue) -> view.targetUnit(model.selectedUnitProperty().get(), newValue));
    model
        .unitsProperty()
        .addListener(
            (SetChangeListener<Unit>)
                change -> {
                  if (change.wasAdded()) {
                    Unit unit = change.getElementAdded();
                    Player.Color color = unit.getOwner().getColor();
                    @Nullable MapPosition unitPosition =
                        model.unitPositionsProperty().get().get(unit);
                    if (unitPosition != null) {
                      view.addUnit(unit, unitPosition, color);
                    }
                  }
                  if (change.wasRemoved()) {
                    Unit unit = change.getElementRemoved();
                    view.removeUnit(unit);
                  }
                });
    model
        .unitPositionsProperty()
        .addListener(
            (MapChangeListener<Unit, MapPosition>)
                change -> {
                  if (change.wasRemoved() && change.wasAdded()) {
                    view.moveUnit(
                        change.getKey(), change.getValueRemoved(), change.getValueAdded());
                  }
                });
    model
        .targetHexesProperty()
        .addListener((SetChangeListener<Point>) change -> view.setTargetHexes(change.getSet()));
    model
        .winnerProperty()
        .addListener((observable, oldValue, newValue) -> view.setWinner(newValue));
  }

  @SuppressWarnings("UnusedReturnValue")
  public static Controller create(Model model, GameViewPane view, AIView aiView) {
    Controller controller = new Controller(model, view, aiView);
    controller.listenForPropertyChanges();
    return controller;
  }

  private Controller(Model model, GameViewPane view, AIView aiView) {
    this.model = model;
    this.view = view;
    this.aiView = aiView;
  }

  private final AIView aiView;
  private final Model model;
  private final GameViewPane view;
}
