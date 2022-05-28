package org.heckcorp.domination.desktop.view;

import java.awt.*;

public interface MapViewListener {
    /**
     * Sets the viewport to the specified rectangle.
     * @pre bounds.x >= 0
     * @pre bounds.y >= 0
     * @pre bounds.width > 0
     * @pre bounds.height > 0
     * @pre bounds.x + bounds.width <= map.width
     * @pre bounds.y + bounds.height <= map.height
     */
    void setViewportBounds(Rectangle bounds);
}
