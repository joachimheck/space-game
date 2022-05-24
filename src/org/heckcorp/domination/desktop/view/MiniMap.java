package org.heckcorp.domination.desktop.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.heckcorp.domination.City;
import org.heckcorp.domination.Hex;
import org.heckcorp.domination.HexMap;
import org.heckcorp.domination.ShadowMap;
import org.heckcorp.domination.ShadowStatus;
import org.heckcorp.domination.Unit;

@SuppressWarnings("serial")
public final class MiniMap extends JPanel implements MapViewListener {
    private final Rectangle viewport = new Rectangle(0, 0);
    private HexMap map;
    private ShadowMap shadowMap;
    
    @Override
    public Dimension getPreferredSize() {
        Dimension result = new Dimension(100, 100);
        
        if (map != null) {
            result = new Dimension(2*map.width, 2*map.height);
        }
        
        return result;
    }
    
    /**
     * Creates a MiniMap to display the specified HexMap.
     * 
     * @param map the HexMap to display.
     * 
     * @post this.map == map
     */
    public MiniMap(HexMap map) {
        super();
        this.map = map;
    }
    
    /**
     * Sets the viewport's upper left corner to the specified
     * map coordinates.  If the given coordinates would make
     * the viewport extend beyond the map, the viewport will be
     * located at the nearest position inside the map.
     * 
     * @param position
     * @pre position.x >= 0
     * @pre position.y >= 0
     * @post getViewportBounds().x + getViewportBounds().width <= map.width
     * @post getViewportBounds().y + getViewportBounds().height <= map.height
     */
    public void setViewportPosition(Point position) {
        assert position.x >= 0;
        assert position.y >= 0;
        assert position.x + viewport.width <= map.width;
        assert position.y + viewport.height <= map.height;
        
        viewport.setLocation(position);
    }
    
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
    public void setViewportBounds(Rectangle bounds) {
        assert bounds.x >= 0: bounds;
        assert bounds.y >= 0: bounds;
        assert bounds.width > 0: bounds;
        assert bounds.height > 0: bounds;
        assert bounds.x + bounds.width <= map.width: bounds;
        assert bounds.y + bounds.height <= map.height: bounds;
        
        viewport.setBounds(bounds);
    }
    
    /**
     * Returns the viewport rectangle.
     * 
     * @return the viewport rectangle.
     * 
     * @post result.x >= 0
     * @post result.y >= 0
     * @post result.width > 0
     * @post reault.height > 0
     * @post result.x + result.width <= map.width
     * @post result.y + result.height <= map.height
     */
    public Rectangle getViewportBounds() {
        return viewport;
    }
    
    /**
     * Updates the display to reflect the current state of the
     * HexMap and ShadowMap. 
     *
     */
    public void paintComponent(Graphics gIn) {
        long start = System.currentTimeMillis();
        
        super.paintComponent(gIn);
        
        if (map != null) {
            Graphics2D g = (Graphics2D) gIn;

            g.setColor(Color.gray);
            g.fillRect(0, 0, getWidth(), getHeight());

            // The number of pixels across a hex.  Must be divisible by 4!
            int hexSize = 4;
//          double scaleFactor = (double) (getWidth() / (hexSize * map.width));
//          g.scale(scaleFactor, scaleFactor);
            g.scale(((double)getWidth())/((double)(hexSize * map.width)),
                    ((double)getHeight())/((double)(hexSize * map.height)));

            List<Unit> units = new ArrayList<Unit>();
            List<City> cities = new ArrayList<City>();
            for (int x=0; x<map.width; x++) {
                for (int y=0; y<map.height; y++) {
                    Color hexColor = Color.black;

                    ShadowStatus status = shadowMap.getStatus(x, y);

                    if (status.isExplored()) {
                        Hex hex = map.getHex(x, y);

                        if (hex.getOwner() != null) {
                            City city = hex.getCity();

                            if (city != null) {
                                cities.add(city);
                            }

                            if (status.isVisible() && !hex.getUnits().isEmpty()) {
                                units.add(hex.getUnits().get(0));
                            }
                        }

                        if (hex.terrain == Hex.Terrain.LAND) {
                            hexColor = Color.yellow;
                        } else if (hex.terrain == Hex.Terrain.WATER) {
                            hexColor = Color.blue;
                        }

                        if (!status.isVisible()) {
                            hexColor = hexColor.darker();
                        }
                    }

                    g.setColor(hexColor);
                    int evenRow = (x % 2 == 0) ? hexSize/2 : 0;
                    g.fillRect(hexSize*x, hexSize*y + evenRow, hexSize, hexSize);
                }
            }

            // Now draw the cities and units.
            for (City city : cities) {
                g.setColor(city.getOwner().getColor());
                Point position = city.getHex().getPosition();
                int evenRow = (position.x % 2 == 0) ? hexSize/2 : 0;
                g.drawRect(hexSize*position.x, hexSize*position.y + evenRow,
                           hexSize-1, hexSize-1);
            }

            for (Unit unit : units) {
                g.setColor(unit.getOwner().getColor());
                Point position = unit.getHex().getPosition();
                int evenRow = (position.x % 2 == 0) ? hexSize/2 : 0;
                g.fillRect(hexSize*position.x + hexSize/4,
                           hexSize*position.y + hexSize/4 + evenRow,
                           hexSize/2, hexSize/2);
            }

            // Next, draw the viewport rectangle.
            g.setColor(Color.white);
            g.setStroke(new BasicStroke(1));
            int evenRow = (viewport.x % 2 == 0) ? hexSize/2 : 0;
            g.drawRect(hexSize*viewport.x, hexSize*viewport.y + evenRow,
                       hexSize*viewport.width, hexSize*viewport.height);

            // TODO: necessary?
//            repaint();
        }

        long time = System.currentTimeMillis() - start;
        assert (time < 1000) : "Slow MiniMap paint loop!";
    }
    
    public void initialize(HexMap map, ShadowMap shadowMap) {
        this.map = map;
        this.shadowMap = shadowMap;

        assert shadowMap.width == map.width;
        assert shadowMap.height == map.height;
    }
}
