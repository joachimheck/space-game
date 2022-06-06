package org.heckcorp.spacegame;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.map.MouseButton;
import org.heckcorp.spacegame.map.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
            Hex selectedHex = map.getHex(selectedHexPosition.get().get());
            List<Unit> units = selectedHex.getUnits();
            if (!units.isEmpty()) {
                Unit selectedUnit = units.get(0);
                selectedUnit.getHex().removeUnit(selectedUnit);
                selectedUnit.setHex(map.getHex(hexCoordinates));
                Map<Unit, Point> updated = new HashMap<>(unitPositions.get());
                updated.put(selectedUnit, hexCoordinates);
                unitPositions.setValue(updated);
                selectedUnit.getHex().addUnit(selectedUnit);
            }
        }
    }

    public void addUnit(Unit unit) {
        unit.getHex().addUnit(unit);
        unitsProperty().setValue(new ImmutableSet.Builder<Unit>().addAll(getUnits()).add(unit).build());
        unitPositions.get().put(unit, unit.getHex().getPosition());
    }

    @SuppressWarnings("unused")
    private void removeUnit(Unit unit) {
        unit.getHex().removeUnit(unit);
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

    public HexMap getMap() {
        return map;
    }

    public final Set<Unit> getUnits() {
        return units.get();
    }

    public JavaFxModel(HexMap map) {
        this.map = map;
    }

    private final HexMap map;

    private final ObjectProperty<Optional<Point>> selectedHexPosition = new SimpleObjectProperty<>(Optional.empty());

    private final ObjectProperty<Set<Unit>> units = new SimpleObjectProperty<>(Sets.newHashSet());

    public Map<Unit, Point> getUnitPositions() {
        return unitPositions.get();
    }

    private final ObjectProperty<Map<Unit, Point>> unitPositions = new SimpleObjectProperty<>(Maps.newHashMap());
}
