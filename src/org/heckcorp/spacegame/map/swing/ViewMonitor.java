package org.heckcorp.spacegame.map.swing;


import org.heckcorp.spacegame.map.Point;

public interface ViewMonitor {
    /**
     * Notifies this monitor that a hex has been clicked.
     * @pre button == MouseEvent.BUTTON1 ||
     *      button == MouseEvent.BUTTON2 ||
     *      button == MouseEvent.BUTTON3
     * @pre viewCoordinates is contained within the map.
     */
    void hexClicked(Point viewCoordinates, int button);
}
