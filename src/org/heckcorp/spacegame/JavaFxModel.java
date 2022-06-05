package org.heckcorp.spacegame;

import com.google.common.collect.Sets;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.map.MouseButton;
import org.heckcorp.spacegame.map.Point;

import java.util.Optional;
import java.util.Set;

public class JavaFxModel {
    public void hexClicked(Point hexCoordinates, MouseButton mouseButton) {
        selectedHexPosition.setValue(Optional.empty());
        if (mouseButton == MouseButton.PRIMARY) {
            selectedHexPosition.setValue(Optional.of(hexCoordinates));
        }
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
