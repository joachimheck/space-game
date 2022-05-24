package org.heckcorp.domination.desktop.view;

import java.awt.Point;
import java.awt.Rectangle;

public interface MapViewListener {
    /**
     * @param position
     * @pre position != null
     * @pre the viewport fits entirely inside the map.
     */
    public void setViewportPosition(Point position);
    
    /**
     * Sets the viewport to the specified rectangle.
     * @param bounds
     * @pre bounds.x >= 0
     * @pre bounds.y >= 0
     * @pre bounds.width > 0
     * @pre bounds.height > 0
     * @pre bounds.x + bounds.width <= map.width
     * @pre bounds.y + bounds.height <= map.height
     */
    public void setViewportBounds(Rectangle bounds);
}
