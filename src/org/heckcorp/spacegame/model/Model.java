package org.heckcorp.spacegame.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.heckcorp.spacegame.map.MouseButton;
import org.heckcorp.spacegame.map.Point;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Model {
    public void hexClicked(Point hexCoordinates, MouseButton mouseButton) {
        if (mouseButton == MouseButton.PRIMARY) {
            selectedHexPosition.setValue(hexCoordinates);
            if (!getUnitsAt(hexCoordinates).isEmpty()) {
                selectedUnit.setValue(getUnitsAt(hexCoordinates).get(0));
            }
        } else if (mouseButton == MouseButton.SECONDARY) {
            moveSelectedUnit(hexCoordinates);
            // TODO: Fix this warning.
            selectedHexPosition.setValue(null);
        }
    }

    private void moveSelectedUnit(Point hexCoordinates) {
        @Nullable Point selectedCoordinates = selectedHexPosition.get();
        // TODO: Fix this warning.
        if (selectedCoordinates != null) {
            List<Unit> units = getUnitsAt(selectedCoordinates);
            if (!units.isEmpty()) {
                Unit selectedUnit = units.get(0);
                Map<Unit, Point> updated = new HashMap<>(unitPositions.get());
                updated.put(selectedUnit, hexCoordinates);
                unitPositions.setValue(updated);
            }
        }
    }

    private List<Unit> getUnitsAt(Point point) {
        return unitPositions.getValue().entrySet().stream()
                .filter(e -> e.getValue().equals(point))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void addPlayer(Player player) {
        players.setValue(new ImmutableSet.Builder<Player>().addAll(getPlayers()).add(player).build());
    }
    public void addUnit(Unit unit, Point hexPosition) {
        unitPositions.get().put(unit, hexPosition);
        unitsProperty().setValue(new ImmutableSet.Builder<Unit>().addAll(getUnits()).add(unit).build());
    }

    @SuppressWarnings("unused")
    private void removeUnit(Unit unit) {
        unitPositions.get().remove(unit);
        unitsProperty().setValue(Sets.difference(getUnits(), ImmutableSet.of(unit)));
    }

    public final ObjectProperty<Point> selectedHexPositionProperty() {
        return selectedHexPosition;
    }

    public final ObjectProperty<Unit> selectedUnit() {
        return selectedUnit;
    }

    public final ObjectProperty<Map<Unit, Point>> unitPositionsProperty() {
        return unitPositions;
    }

    public final ObjectProperty<Set<Unit>> unitsProperty() {
        return units;
    }

    public final Set<Player> getPlayers() {
        return players.get();
    }

    public final Set<Unit> getUnits() {
        return units.get();
    }

    private final ObjectProperty<Set<Player>> players = new SimpleObjectProperty<>(Sets.newHashSet());
    private final ObjectProperty<Point> selectedHexPosition = new SimpleObjectProperty<>();
    private final ObjectProperty<Unit> selectedUnit = new SimpleObjectProperty<>();
    private final ObjectProperty<Set<Unit>> units = new SimpleObjectProperty<>(Sets.newHashSet());
    private final ObjectProperty<Map<Unit, Point>> unitPositions = new SimpleObjectProperty<>(Maps.newHashMap());
}
