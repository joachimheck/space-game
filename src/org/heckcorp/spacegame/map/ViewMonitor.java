package org.heckcorp.spacegame.map;


public interface ViewMonitor {
    /**
     * Notifies this monitor that a hex has been clicked.
     */
    void hexClicked(Point hexCoordinates, MouseButton button);
}
