package org.heckcorp.spacegame;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.heckcorp.spacegame.map.MouseButton;
import org.heckcorp.spacegame.map.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaFxModel {
    public void hexClicked(Point hexCoordinates, MouseButton mouseButton) {
        if (mouseButton == MouseButton.PRIMARY) {
            selectedHexPosition.setValue(Optional.empty());
            selectedHexPosition.setValue(Optional.of(hexCoordinates));
        } else if (mouseButton == MouseButton.SECONDARY) {
            moveSelectedUnit(hexCoordinates);
            selectedHexPosition.setValue(Optional.empty());
        }
    }

    private void moveSelectedUnit(Point hexCoordinates) {
        if (selectedHexPosition.get().isPresent()) {
            List<Unit> units = getUnitsAt(selectedHexPosition.get().get());
            if (!units.isEmpty()) {
                Unit selectedUnit = units.get(0);
                Map<Unit, Point> updated = new HashMap<>(unitPositions.get());
                updated.put(selectedUnit, hexCoordinates);
                unitPositions.setValue(updated);
            }
        }
    }

    private List<Unit> getUnitsAt(Point point) {
        return unitPositions.get().entrySet().stream()
                .filter(e -> e.getValue().equals(point))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void addUnit(Unit unit, Point hexPosition) {
        unitsProperty().setValue(new ImmutableSet.Builder<Unit>().addAll(getUnits()).add(unit).build());
        unitPositions.get().put(unit, hexPosition);
    }

    @SuppressWarnings("unused")
    private void removeUnit(Unit unit) {
        unitsProperty().setValue(Sets.difference(getUnits(), ImmutableSet.of(unit)));
        unitPositions.get().remove(unit);
    }

    public final ObjectProperty<Optional<Point>> selectedHexPositionProperty() {
        return selectedHexPosition;
    }

    public final ObjectProperty<Map<Unit, Point>> unitPositionsProperty() {
        return unitPositions;
    }

    public final ObjectProperty<Set<Unit>> unitsProperty() {
        return units;
    }

    public final Set<Unit> getUnits() {
        return units.get();
    }

    private final ObjectProperty<Optional<Point>> selectedHexPosition = new SimpleObjectProperty<>(Optional.empty());

    private final ObjectProperty<Set<Unit>> units = new SimpleObjectProperty<>(Sets.newHashSet());

    private final ObjectProperty<Map<Unit, Point>> unitPositions = new SimpleObjectProperty<>(Maps.newHashMap());
}
