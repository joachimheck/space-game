package org.heckcorp.spacegame.ui.map;

import org.heckcorp.spacegame.ui.map.MouseButton;
import org.heckcorp.spacegame.ui.map.Point;

public interface MapModel {
  void hexClicked(Point hexCoordinates, MouseButton mouseButton);
}
