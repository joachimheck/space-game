package org.heckcorp.spacegame.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Model implements MapModel {
  @Override
  public void hexClicked(Point hexCoordinates, MouseButton mouseButton) {
    if (mouseButton == MouseButton.PRIMARY) {
      if (selectionMode.equals(SelectionMode.SELECT)) {
        selectHex(hexCoordinates);
      } else if (selectionMode.equals(SelectionMode.TARGET)
          && selectedUnit.getValue() != null
          && targetHexes.contains(hexCoordinates)) {
        List<Unit> units = getUnitsAt(hexCoordinates);
        if (!units.isEmpty()) {
          targetUnit.setValue(units.get(0));
        }
        selectionMode = SelectionMode.SELECT;
      }
    } else if (mouseButton == MouseButton.SECONDARY) {
      if (selectionMode.equals(SelectionMode.SELECT)) {
        moveSelectedUnit(hexCoordinates);

        selectHex(hexCoordinates);
      } else if (selectionMode.equals(SelectionMode.TARGET)) {
        selectionMode = SelectionMode.SELECT;
        targetHexes.clear();
      }
    }
  }

  private void selectHex(Point hexCoordinates) {
    selectedHexPosition.setValue(hexCoordinates);
    selectedUnit.setValue(null);
    List<Unit> units = getUnitsAt(hexCoordinates);
    if (!units.isEmpty()) {
      selectedUnit.setValue(units.get(0));
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
        MapPosition selectedUnitPosition = unitPositions.get(selectedUnit.get());
        targetHexes.addAll(getTargetHexes(selectedUnitPosition));
      }
    }
  }

  private Collection<Point> getTargetHexes(MapPosition unitPosition) {
    Point hexInFront = mapUtils.getAdjacentHex(unitPosition);
    ImmutableSet<Direction> directions =
        ImmutableSet.of(
            unitPosition.direction().left(),
            unitPosition.direction(),
            unitPosition.direction().right());
    Set<Point> targetHexes = Sets.newHashSet();
    Set<Point> hexes = Sets.newHashSet(hexInFront);
    int range = 4;
    for (int i = 0; i < range; i++) {
      targetHexes.addAll(hexes);
      Set<Point> newHexes =
          hexes.stream()
              .flatMap(p -> directions.stream().map(d -> mapUtils.getAdjacentHex(p, d)))
              .collect(Collectors.toSet());
      hexes.clear();
      hexes = newHexes;
    }
    return targetHexes;
  }

  public void moveForward() {
    Unit selectedUnit = this.selectedUnit.get();
    if (selectedUnit.getEnergy() > 0) {
      selectedUnit.setEnergy(selectedUnit.getEnergy() - 1);
      this.selectedUnit.setValue(null);
      this.selectedUnit.setValue(selectedUnit);
      MapPosition currentPosition = unitPositions.get(selectedUnit);
      unitPositions.put(
          selectedUnit,
          new MapPosition(mapUtils.getAdjacentHex(currentPosition), currentPosition.direction()));
    }
  }

  public void rotateLeft() {
    Unit selectedUnit = this.selectedUnit.get();
    if (selectedUnit.getEnergy() > 0) {
      selectedUnit.setEnergy(selectedUnit.getEnergy() - 1);
      this.selectedUnit.setValue(null);
      this.selectedUnit.setValue(selectedUnit);
      MapPosition currentPosition = unitPositions.get(selectedUnit);
      unitPositions.put(
          selectedUnit,
          new MapPosition(currentPosition.position(), currentPosition.direction().left()));
    }
  }

  public void rotateRight() {
    Unit selectedUnit = this.selectedUnit.get();
    if (selectedUnit.getEnergy() > 0) {
      selectedUnit.setEnergy(selectedUnit.getEnergy() - 1);
      this.selectedUnit.setValue(null);
      this.selectedUnit.setValue(selectedUnit);
      MapPosition currentPosition = unitPositions.get(selectedUnit);
      unitPositions.put(
          selectedUnit,
          new MapPosition(currentPosition.position(), currentPosition.direction().right()));
    }
  }

  public void processAttack() {
    Unit attacker = selectedUnit.getValue();
    Unit defender = targetUnit.getValue();
    if (attacker != null && defender != null && attacker.getEnergy() > 0) {
      attacker.setEnergy(attacker.getEnergy() - 1);
      selectedUnit.setValue(null);
      selectedUnit.setValue(attacker);
      defender.setHealth(defender.getHealth() - attacker.getAttackStrength());
      targetUnit.setValue(null);
      if (defender.getHealth() > 0) {
        targetUnit.setValue(defender);
      } else {
        removeUnit(defender);
      }
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

  public void endTurn() {
    units.forEach(Unit::resetForTurn);
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
