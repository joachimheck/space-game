package org.heckcorp.spacegame;

import com.google.common.collect.Maps;
import javafx.collections.SetChangeListener;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.heckcorp.spacegame.model.Model;
import org.heckcorp.spacegame.model.Player;
import org.heckcorp.spacegame.model.Unit;
import org.heckcorp.spacegame.ui.GameViewPane;
import org.heckcorp.spacegame.ui.map.Counter;
import org.heckcorp.spacegame.ui.map.Point;
import org.heckcorp.spacegame.ui.map.ViewResources;

import java.util.HashMap;
import java.util.Map;

public class Controller {

  public void listenForPropertyChanges() {
    model
        .selectedHexPositionProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
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
        .addListener((observable, oldValue, newValue) -> view.targetUnit(newValue));
    model
        .unitsProperty()
        .addListener(
            (SetChangeListener<Unit>)
                change -> {
                  if (change.wasAdded()) {
                    Unit unit = change.getElementAdded();
                    Player.Color color = unit.getOwner().getColor();
                    unitCounters.put(
                        unit,
                        Counter.build(
                            viewResources, unit.getImageId(), color.r(), color.g(), color.b()));
                    @Nullable Point unitPosition = model.unitPositionsProperty().get().get(unit);
                    if (unitPosition != null) {
                      view.addCounter(unitCounters.get(unit), unitPosition);
                    }
                  }
                });
    model
        .unitPositionsProperty()
        .addListener(
            (observable, oldValue, newValue) ->
                Maps.difference(oldValue, newValue)
                    .entriesDiffering()
                    .forEach(
                        (u, d) -> {
                          if (unitCounters.containsKey(u)) {
                            view.moveCounter(unitCounters.get(u), d.leftValue(), d.rightValue());
                          }
                        }));
  }

  private Controller(Model model, GameViewPane view, ViewResources viewResources) {
    this.model = model;
    this.view = view;
    this.viewResources = viewResources;
  }

  @SuppressWarnings("UnusedReturnValue")
  public static Controller create(Model model, GameViewPane view, ViewResources viewResources) {
    Controller controller = new Controller(model, view, viewResources);
    controller.listenForPropertyChanges();
    return controller;
  }

  private final Model model;
  private final Map<Unit, Counter> unitCounters = new HashMap<>();
  private final GameViewPane view;
  private final ViewResources viewResources;
}
