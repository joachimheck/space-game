package org.heckcorp.spacegame.map;


public interface ViewMonitor {
    /**
     * Notifies this monitor that a hex has been clicked.
     */
    void hexClicked(Point viewCoordinates, MouseButton button);
}
