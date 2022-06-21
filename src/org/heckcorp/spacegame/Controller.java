package org.heckcorp.spacegame;

import com.google.common.collect.ImmutableSet;
import javafx.application.Platform;
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
            (u1, u2, newValue) ->
                sequentialExecutor.submit(
                    () ->
                        Platform.runLater(
                            () -> {
                              view.setCurrentPlayer(newValue);
                              aiPlayer.setCurrentPlayer(newValue);
                            })));
    model
        .selectedHexPositionProperty()
        .addListener(
            (u1, u2, newValue) ->
                sequentialExecutor.submit(
                    () ->
                        Platform.runLater(
                            () -> {
                              view.unselectHex();
                              if (newValue != null) {
                                view.selectHex(newValue);
                              }
                            })));
    model
        .selectedUnitProperty()
        .addListener(
            (observable, oldValue, newValue) ->
                sequentialExecutor.submit(
                    () -> Platform.runLater(() -> view.selectUnit(newValue))));
    model
        .targetHexesProperty()
        .addListener(
            (SetChangeListener<Point>)
                change ->
                    sequentialExecutor.submit(
                        () ->
                            Platform.runLater(
                                () -> view.setTargetHexes(ImmutableSet.copyOf(change.getSet())))));
    model
        .targetUnitProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              final Unit selectedUnit = model.selectedUnitProperty().get();
              final Unit targetUnit = newValue;
              if (selectedUnit != null && targetUnit != null) {
                sequentialExecutor.submit(
                    () -> Platform.runLater(() -> view.targetUnit(selectedUnit, targetUnit)));
              }
            });
    model
        .unitsProperty()
        .addListener(
            (SetChangeListener<Unit>)
                change ->
                    sequentialExecutor.submit(
                        () ->
                            Platform.runLater(
                                () -> {
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
                                })));
    model
        .unitPositionsProperty()
        .addListener(
            (MapChangeListener<Unit, MapPosition>)
                change -> {
                  if (change.wasRemoved() && change.wasAdded()) {
                    sequentialExecutor.submit(
                        () ->
                            Platform.runLater(
                                () ->
                                    view.moveUnit(
                                        change.getKey(),
                                        change.getValueRemoved(),
                                        change.getValueAdded())));
                  }
                });
    model
        .winnerProperty()
        .addListener(
            (observable, oldValue, newValue) ->
                sequentialExecutor.submit(() -> Platform.runLater(() -> view.setWinner(newValue))));
  }

  @SuppressWarnings("UnusedReturnValue")
  public static Controller create(
      Model model,
      GameViewPane view,
      AIPlayer aiPlayer,
      SequentialExecutor sequentialAnimationExecutor) {
    Controller controller = new Controller(model, view, sequentialAnimationExecutor, aiPlayer);
    controller.listenForPropertyChanges();
    return controller;
  }

  private Controller(
      Model model,
      GameViewPane view,
      SequentialExecutor sequentialAnimationExecutor,
      AIPlayer aiPlayer) {
    this.model = model;
    this.view = view;
    this.sequentialExecutor = sequentialAnimationExecutor;
    this.aiPlayer = aiPlayer;
  }


  private final AIPlayer aiPlayer;
  private final Model model;
  private final SequentialExecutor sequentialExecutor;
  private final GameViewPane view;
}
