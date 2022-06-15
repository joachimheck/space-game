package org.heckcorp.spacegame.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import org.heckcorp.spacegame.Constants;
import org.heckcorp.spacegame.ui.map.MapModel;
import org.heckcorp.spacegame.ui.map.MapUtils;
import org.heckcorp.spacegame.ui.map.MouseButton;
import org.heckcorp.spacegame.ui.map.Point;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Model implements MapModel {

  public void addUnit(Unit unit, MapPosition mapPosition) {
    unitPositions.get().put(unit, mapPosition);
    units.add(unit);
  }

  public void endTurn() {
    units.forEach(Unit::resetForTurn);
    selectUnit(selectedUnit.get());
  }

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
          targetHexes.clear();
          targetUnit.setValue(units.get(0));
        }
        selectionMode = SelectionMode.SELECT;
      }
    } else if (mouseButton == MouseButton.SECONDARY) {
      selectionMode = SelectionMode.SELECT;
      selectedUnit.setValue(null);
      selectedHexPosition.setValue(null);
      targetHexes.clear();
    }
  }

  public void moveForward() {
    doMove(p -> new MapPosition(mapUtils.getAdjacentHex(p), p.direction()));
  }

  public void processAttack() {
    Unit attacker = selectedUnit.getValue();
    Unit defender = targetUnit.getValue();
    if (attacker != null && defender != null && attacker.getEnergy() > 0) {
      targetHexes.clear();
      attacker.setEnergy(attacker.getEnergy() - 1);
      selectUnit(attacker);
      defender.setHealth(defender.getHealth() - attacker.getAttackStrength());
      targetUnit.setValue(null);
      if (defender.getHealth() > 0) {
        targetUnit.setValue(defender);
      } else {
        removeUnit(defender);
      }
    }
  }

  public void rotateLeft() {
    doMove(p -> new MapPosition(p.position(), p.direction().left()));
  }

  public void rotateRight() {
    doMove(p -> new MapPosition(p.position(), p.direction().right()));
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

  private void doMove(UnaryOperator<MapPosition> moveOp) {
    Unit selectedUnit = this.selectedUnit.get();
    if (selectedUnit.getEnergy() > 0) {
      targetHexes.clear();
      selectedUnit.setEnergy(selectedUnit.getEnergy() - 1);
      selectUnit(selectedUnit);
      MapPosition currentPosition = unitPositions.get(selectedUnit);
      unitPositions.put(selectedUnit, moveOp.apply(currentPosition));
      selectHex(unitPositions.get(selectedUnit).position());
    }
  }

  private Set<Point> getTargetHexes(MapPosition unitPosition) {
    Point hexInFront = mapUtils.getAdjacentHex(unitPosition);
    ImmutableSet<Direction> directions =
        ImmutableSet.of(
            unitPosition.direction().left(),
            unitPosition.direction(),
            unitPosition.direction().right());
    Set<Point> targetHexes = Sets.newHashSet();
    Set<Point> hexes = Sets.newHashSet(hexInFront);
    for (int i = 0; i < Constants.WEAPON_RANGE; i++) {
      targetHexes.addAll(hexes);
      Set<Point> newHexes =
          hexes.stream()
              .flatMap(p -> directions.stream().map(d -> mapUtils.getAdjacentHex(p, d)))
              .collect(Collectors.toSet());
      hexes.clear();
      hexes = newHexes;
    }
    return targetHexes.stream().filter(mapUtils::isInsideMap).collect(Collectors.toSet());
  }

  private List<Unit> getUnitsAt(Point point) {
    return unitPositions.getValue().entrySet().stream()
        .filter(e -> e.getValue().position().equals(point))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  private void removeUnit(Unit unit) {
    unitPositions.get().remove(unit);
    units.remove(unit);

    Set<Player> remainingPlayers = units.stream().map(Unit::getOwner).collect(Collectors.toSet());
    if (remainingPlayers.size() == 1) {
      winner.setValue(remainingPlayers.iterator().next().getName());
    }
  }

  private void selectHex(Point hexCoordinates) {
    selectedHexPosition.setValue(hexCoordinates);
    selectedUnit.setValue(null);
    targetUnit.setValue(null);
    List<Unit> units = getUnitsAt(hexCoordinates);
    if (!units.isEmpty()) {
      selectedUnit.setValue(units.get(0));
    }
  }

  private void selectUnit(Unit unit) {
    selectedUnit.setValue(null);
    selectedUnit.setValue(unit);
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

  public Model(MapUtils mapUtils) {
    this.mapUtils = mapUtils;
  }

  private final MapUtils mapUtils;
  private final ObjectProperty<Point> selectedHexPosition = new SimpleObjectProperty<>();
  private final ObjectProperty<Unit> selectedUnit = new SimpleObjectProperty<>();
  private final SetProperty<Point> targetHexes =
      new SimpleSetProperty<>(FXCollections.observableSet());
  private final ObjectProperty<Unit> targetUnit = new SimpleObjectProperty<>();
  private final SetProperty<Unit> units = new SimpleSetProperty<>(FXCollections.observableSet());
  private final MapProperty<Unit, MapPosition> unitPositions =
      new SimpleMapProperty<>(FXCollections.observableMap(Maps.newHashMap()));
  private final ObjectProperty<String> winner = new SimpleObjectProperty<>();
  private SelectionMode selectionMode = SelectionMode.SELECT;

  public enum SelectionMode {
    SELECT,
    TARGET
  }
}
