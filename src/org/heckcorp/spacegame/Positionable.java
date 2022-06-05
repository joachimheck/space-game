package org.heckcorp.spacegame;

import org.heckcorp.spacegame.map.Point;
import org.jetbrains.annotations.Nullable;

public interface Positionable {
    @Nullable Point getPosition();
}
