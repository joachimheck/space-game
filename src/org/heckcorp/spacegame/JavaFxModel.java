package org.heckcorp.spacegame;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.map.MouseButton;
import org.heckcorp.spacegame.map.Point;

import java.util.List;
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
                removeUnit(selectedUnit);
                selectedUnit.setHex(map.getHex(hexCoordinates));
                addUnit(selectedUnit);
            }
        }
    }

    private void removeUnit(Unit selectedUnit) {
        selectedUnit.getHex().removeUnit(selectedUnit);
        currentUnits().setValue(Sets.difference(getUnits(), ImmutableSet.of(selectedUnit)));
    }

    public void addUnit(Unit selectedUnit) {
        selectedUnit.getHex().addUnit(selectedUnit);
        currentUnits().setValue(new ImmutableSet.Builder<Unit>().addAll(getUnits()).add(selectedUnit).build());
    }

    public final ObjectProperty<Optional<Point>> currentSelectedHexPosition() {
        return selectedHexPosition;
    }

    public final ObjectProperty<Set<Unit>> currentUnits() {
        return units;
    }

    public final Set<Unit> getUnits() {
        return units.get();
    }

    public HexMap getMap() {
        return map;
    }

    public JavaFxModel(HexMap map) {
        this.map = map;
    }

    private final HexMap map;

    private final ObjectProperty<Optional<Point>> selectedHexPosition = new SimpleObjectProperty<>(Optional.empty());

    private final ObjectProperty<Set<Unit>> units = new SimpleObjectProperty<>(Sets.newHashSet());
}
