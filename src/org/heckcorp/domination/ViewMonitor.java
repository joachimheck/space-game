package org.heckcorp.domination;

import java.awt.Point;

public interface ViewMonitor {
    /**
     * Notifies this monitor that a hex has been clicked.
     * @param viewCoordinates
     * @param button
     * @pre viewCoordinates != null
     * @pre button == MouseEvent.BUTTON1 ||
     *      button == MouseEvent.BUTTON2 ||
     *      button == MouseEvent.BUTTON3
     * @pre viewCoordinates is contained within the map.
     */
    public void hexClicked(Point viewCoordinates, int button);
    
    public void setViewingPlayerActive(boolean active); 
}
