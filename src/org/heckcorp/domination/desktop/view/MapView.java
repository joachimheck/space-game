package org.heckcorp.domination.desktop.view;

import org.heckcorp.domination.HexMap;
import org.heckcorp.domination.ViewMonitor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * A MapView wraps around a MapPane and adds scrolling and
 * Counter-related functions.
 * 
 * @author Joachim Heck
 */
public class MapView extends JPanel implements AdjustmentListener
{
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getAdjustable() == hScrollBar || e.getAdjustable() == vScrollBar) {
            adjustPositions();
            notifyViewportBounds();
        } else {
            assert false;
        }
    }

    /**
     * Displays the hex with the specified map coordinates on the screen,
     * and centers it, if possible, if it was offscreen.
     * 
     * @param hexPos the map coordinates of the hex to show
     */
    public void centerHex(Point hexPos) {
        Rectangle viewRect = new Rectangle(mapPane.getViewport());

        // Scroll the view and center the hex if possible.
        hScrollBar.setValue(Math.max(0, hexPos.x - viewRect.width/2));
        vScrollBar.setValue(Math.max(0, hexPos.y - viewRect.height/2));
    }
    
    /**
     * Displays the hexes whose map coordinates are inside the specified
     * rectangle on the screen, if possible, and attempts to center the
     * rectangle.
     */
    public void centerRectangle(Rectangle rect) {
        Rectangle newRect = new Rectangle(rect);
        // To make contains() work the way one might expect, add one.
        newRect.width += 1;
        newRect.height += 1;

        if (!mapPane.getViewport().contains(newRect)) {
            Point centerPoint =
                new Point((int) Math.round(newRect.getCenterX()),
                          (int) Math.round(newRect.getCenterY()));
            
            centerHex(centerPoint);
        }
    }

    /**
     * @return  the mapPane
     * @uml.property  name="mapPane"
     */
    public MapPane getMapPane() {
        return mapPane;
    }

    public void add(Component component, Integer layer) {
        layeredPane.add(component, layer);
    }

    public void moveToFront(Counter counter) {
        layeredPane.moveToFront(counter);
    }

    public void removeCounter(Counter counter) {
        layeredPane.remove(counter);
    }


    /**
     * Adjusts the positions of the map and the game pieces to correspond
     * to the window size and scroll bar settings.
     */
    private void adjustPositions() {
        // Move the map.
        mapPane.setViewportPosition(new Point(hScrollBar.getValue(),
                                              vScrollBar.getValue()));

        // Adjust all the unit counter positions.
        for (Component component : layeredPane.getComponents()) {
            if (component instanceof Counter) {
                Counter counter = (Counter) component;

                Point mapPos = counter.getMapPosition();

                if (mapPos != null) {
                    counter.setCenterLocation(mapPane.getHexCenter(mapPos));
                    counter.setOnScreen(mapPane.isMapPointInViewport(mapPos));
                }
            }
        }

        // Adjust the selection rectangle.
        Counter selection = UIResources.getInstance().getSelection();
        Point selectedPos = selection.getMapPosition();
        if (selectedPos != null && mapPane.isMapPointInViewport(selectedPos)) {
            selection.setCenterLocation(mapPane.getHexCenter(selectedPos));
            selection.setOnScreen(true);
        } else {
            selection.setOnScreen(false);
        }
    }

    private final Set<MapViewListener> listeners;

    private void notifyViewportBounds() {
        Rectangle bounds = mapPane.getViewport().getBounds();
        for (MapViewListener listener : listeners) {
            listener.setViewportBounds(bounds);
        }
    }

    public MapView() {
        listeners = new HashSet<>();
        hScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        vScrollBar = new JScrollBar(JScrollBar.VERTICAL);
        hScrollBar.addAdjustmentListener(this);
        vScrollBar.addAdjustmentListener(this);

        // Set up the scroll bars and main image area.
        setLayout(new BorderLayout());

        // Use a layered pane to display units and special effects over the map.
        layeredPane = new JLayeredPane() {
            /**
             * JLayeredPanes don't use layout managers, so to make the image
             * fill the background, we have to set its bounds when the
             * layered pane's bounds are set.
             */
            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x, y, width, height);

                if (mapPane != null) {
                    mapPane.setBounds(x, y, width, height);

                    // When we set the value of the first scroll bar, a call
                    // is made to adjustPositions(), which tries to set the
                    // viewport position based on BOTH scrollbars.  The second
                    // scroll value hasn't been adjusted, though, so we can get
                    // an exception.  To avoid it, we turn off the listener while
                    // the window is resizing.
                    hScrollBar.removeAdjustmentListener(MapView.this);
                    vScrollBar.removeAdjustmentListener(MapView.this);

                    // Recalculate the scroll bar information.
                    int hexesAcross = mapPane.getViewport().width;
                    hScrollBar.getModel().setExtent(hexesAcross);
                    hScrollBar.setValue(Math.min((map.width - hexesAcross),
                                                 hScrollBar.getValue()));
                    int hBlock = Math.max(1, hexesAcross - 2);
                    hScrollBar.setBlockIncrement(hBlock);
                    hScrollBar.setMaximum(map.width);

                    int hexesDown = mapPane.getViewport().height;
                    vScrollBar.getModel().setExtent(hexesDown);
                    vScrollBar.setValue(Math.min(map.height - hexesDown,
                                                 vScrollBar.getValue()));
                    int vBlock = Math.max(1, hexesDown - 2);
                    vScrollBar.setBlockIncrement(vBlock);
                    vScrollBar.setMaximum(map.height);

                    hScrollBar.addAdjustmentListener(MapView.this);
                    vScrollBar.addAdjustmentListener(MapView.this);

                    adjustPositions();
                    
                    notifyViewportBounds();
                }
            }
        };
        layeredPane.setOpaque(true);

        add(layeredPane, BorderLayout.CENTER);
        add(hScrollBar, BorderLayout.SOUTH);
        add(vScrollBar, BorderLayout.EAST);

        // Add some special effects counters.
        UIResources resources = UIResources.getInstance();
        add(resources.getExplosion(), MapView.TOP_SPRITE_LAYER);
        add(resources.getSelection(), MapView.TOP_SPRITE_LAYER);
        add(resources.getAttackArrow(), MapView.TOP_SPRITE_LAYER);
        
        setBackground(Color.gray);
    }
    
    public void addMapViewListener(MapViewListener listener) {
        listeners.add(listener);
    }
    
    /**
     * @pre map != null
     * @pre viewMonitor != null
     */
    public void initialize(@NotNull final HexMap map,
                           @NotNull final ViewMonitor viewMonitor)
    {
        this.map = map;

        // Set up the map view area.
        mapPane = new MapPane();
        mapPane.initialize(map);
        add(mapPane, MAP_LAYER);

        mapPane.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Point hexPos = mapPane.getHexCoordinates(e.getPoint());
                
                if (hexPos != null && map.isInMap(hexPos)) {
                    viewMonitor.hexClicked(hexPos, e.getButton());
                }

                if (e.getButton() == MouseEvent.BUTTON2) {
                    Point viewPoint = SwingUtilities.convertPoint(MapView.this, e.getPoint(), mapPane);
                    mapPane.drawHexCenters(viewPoint);
                }
            }
        });

        layeredPane.setPreferredSize(mapPane.getSize());
    }
    
    /**
     * @uml.property  name="hScrollBar"
     * @uml.associationEnd  multiplicity="(1 1)"
     */
    private final JScrollBar hScrollBar;

    /**
     * @uml.property  name="vScrollBar"
     * @uml.associationEnd  multiplicity="(1 1)"
     */
    private final JScrollBar vScrollBar;

    /**
     * @uml.property  name="layeredPane"
     * @uml.associationEnd  multiplicity="(1 1)"
     */
    private final JLayeredPane layeredPane;

    /**
     * @uml.property  name="map"
     * @uml.associationEnd  multiplicity="(1 1)"
     */
    private HexMap map;

    /**
     * @uml.property   name="mapPane"
     * @uml.associationEnd   multiplicity="(1 1)" inverse="mapPane:org.heckcorp.domination.desktop.view.MapPane"
     */
    private MapPane mapPane = null;

    public static final Integer MAP_LAYER = JLayeredPane.DEFAULT_LAYER;

    public static final Integer SPRITE_LAYER = JLayeredPane.DEFAULT_LAYER + 1;

    public static final Integer TOP_SPRITE_LAYER = JLayeredPane.DEFAULT_LAYER + 2;

    public boolean isInitialized() {
        return mapPane != null;
    }
}
