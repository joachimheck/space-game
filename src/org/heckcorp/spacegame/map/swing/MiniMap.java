package org.heckcorp.spacegame.map.swing;

import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.Unit;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class MiniMap extends JPanel implements MapViewListener {
    private final Rectangle viewport = new Rectangle(0, 0);
    private HexMap map;

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
     * Sets the viewport to the specified rectangle.
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
            g.scale(((double)getWidth())/((double)(hexSize * map.width)),
                    ((double)getHeight())/((double)(hexSize * map.height)));

            List<Unit> units = new ArrayList<>();
            for (int x=0; x<map.width; x++) {
                for (int y=0; y<map.height; y++) {
                    Color hexColor = Color.black;

                    Hex hex = map.getHex(x, y);

                    if (hex.getOwner() != null) {
                        if (!hex.getUnits().isEmpty()) {
                            units.add(hex.getUnits().get(0));
                        }
                    }

                    hexColor = hexColor.darker();

                    g.setColor(hexColor);
                    int evenRow = (x % 2 == 0) ? hexSize/2 : 0;
                    g.fillRect(hexSize*x, hexSize*y + evenRow, hexSize, hexSize);
                }
            }

            // Now draw the units.
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
        }

        long time = System.currentTimeMillis() - start;
        assert (time < 1000) : "Slow MiniMap paint loop!";
    }

    public void initialize(HexMap map) {
        this.map = map;
    }
}
