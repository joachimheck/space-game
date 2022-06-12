package org.heckcorp.spacegame.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import org.heckcorp.spacegame.ui.map.MapModel;
import org.heckcorp.spacegame.ui.map.MapUtils;
import org.heckcorp.spacegame.ui.map.MouseButton;
import org.heckcorp.spacegame.ui.map.Point;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Model implements MapModel {
  @Override
  public void hexClicked(Point hexCoordinates, MouseButton mouseButton) {
    if (mouseButton == MouseButton.PRIMARY) {
      List<Unit> units = getUnitsAt(hexCoordinates);
      if (selectionMode.equals(SelectionMode.SELECT)) {
        selectedHexPosition.setValue(hexCoordinates);
        if (units.isEmpty()) {
          selectedUnit.setValue(null);
        } else {
          selectedUnit.setValue(units.get(0));
        }
      } else if (selectionMode.equals(SelectionMode.TARGET) && selectedUnit.getValue() != null) {
        if (!units.isEmpty()) {
          targetUnit.setValue(units.get(0));
        }
        selectionMode = SelectionMode.SELECT;
      }
    } else if (mouseButton == MouseButton.SECONDARY) {
      moveSelectedUnit(hexCoordinates);
      selectedHexPosition.setValue(null);
    }
  }

  private void moveSelectedUnit(Point hexCoordinates) {
    @Nullable Point selectedCoordinates = selectedHexPosition.get();
    if (selectedCoordinates != null) {
      List<Unit> units = getUnitsAt(selectedCoordinates);
      if (!units.isEmpty()) {
        Unit selectedUnit = units.get(0);
        MapPosition newPosition =
            new MapPosition(hexCoordinates, unitPositions.get(selectedUnit).direction());
        unitPositions.put(selectedUnit, newPosition);
      }
    }
  }

  private List<Unit> getUnitsAt(Point point) {
    return unitPositions.getValue().entrySet().stream()
        .filter(e -> e.getValue().position().equals(point))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  public void addUnit(Unit unit, MapPosition mapPosition) {
    unitPositions.get().put(unit, mapPosition);
    units.add(unit);
  }

  public void setSelectionMode(SelectionMode mode) {
    this.selectionMode = mode;
    targetHexes.clear();
    if (selectionMode.equals(SelectionMode.TARGET)) {
      if (selectedUnit.get() != null) {
        MapPosition selectUnitPosition = unitPositions.get(selectedUnit.get());
        Point hexInFront = mapUtils.getAdjacentHex(selectUnitPosition);
        targetHexes.addAll(ImmutableSet.of(hexInFront));
      }
    }
  }

  public void rotateLeft() {
    MapPosition currentPosition = unitPositions.get(selectedUnit.get());
    unitPositions.put(
        selectedUnit.get(),
        new MapPosition(currentPosition.position(), currentPosition.direction().left()));
  }

  public void rotateRight() {
    MapPosition currentPosition = unitPositions.get(selectedUnit.get());
    unitPositions.put(
        selectedUnit.get(),
        new MapPosition(currentPosition.position(), currentPosition.direction().right()));
  }

  public void processAttack() {
    Unit attacker = selectedUnit.getValue();
    Unit defender = targetUnit.getValue();
    assert attacker != null;
    assert defender != null;
    defender.setHealth(defender.getHealth() - attacker.getAttackStrength());
    targetUnit.setValue(null);
    if (defender.getHealth() > 0) {
      targetUnit.setValue(defender);
    } else {
      removeUnit(defender);
    }
  }

  private void removeUnit(Unit unit) {
    unitPositions.get().remove(unit);
    units.remove(unit);

    Set<Player> remainingPlayers = units.stream().map(Unit::getOwner).collect(Collectors.toSet());
    if (remainingPlayers.size() == 1) {
      winner.setValue(remainingPlayers.iterator().next().getName());
    }
  }

  public final ObjectProperty<Point> selectedHexPositionProperty() {
    return selectedHexPosition;
  }

  public final ObjectProperty<Unit> selectedUnitProperty() {
    return selectedUnit;
  }

  public final SetProperty<Point> targetHexesProperty() {
    return targetHexes;
  }

  public final ObjectProperty<Unit> targetUnitProperty() {
    return targetUnit;
  }

  public final MapProperty<Unit, MapPosition> unitPositionsProperty() {
    return unitPositions;
  }

  public final SetProperty<Unit> unitsProperty() {
    return units;
  }

  public final ObjectProperty<String> winnerProperty() {
    return winner;
  }

  public enum SelectionMode {
    SELECT,
    TARGET
  }

  public Model(MapUtils mapUtils) {
    this.mapUtils = mapUtils;
  }

  private final MapUtils mapUtils;
  private final ObjectProperty<Point> selectedHexPosition = new SimpleObjectProperty<>();
  private final ObjectProperty<Unit> selectedUnit = new SimpleObjectProperty<>();
  private SelectionMode selectionMode = SelectionMode.SELECT;
  private final SetProperty<Point> targetHexes =
      new SimpleSetProperty<>(FXCollections.observableSet());
  private final ObjectProperty<Unit> targetUnit = new SimpleObjectProperty<>();
  private final SetProperty<Unit> units = new SimpleSetProperty<>(FXCollections.observableSet());
  private final MapProperty<Unit, MapPosition> unitPositions =
      new SimpleMapProperty<>(FXCollections.observableMap(Maps.newHashMap()));
  private final ObjectProperty<String> winner = new SimpleObjectProperty<>();
}
