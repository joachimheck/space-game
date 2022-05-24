package org.heckcorp.domination;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

public class ShadowMap extends Observable implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final ShadowStatus inactiveStatus = new ShadowStatus(true, true);

    private boolean active = true;
    /**
     * @uml.property  name="explored"
     */
    private final BitSet explored;
    /**
     * @uml.property  name="visible"
     */
    private final BitSet visible;
    /**
     * @uml.property  name="width"
     */
    public final int width;
    /**
     * @uml.property  name="height"
     */
    public final int height;

    private final Set<Point> borderPoints;

    public ShadowMap(int width, int height) {
        this.width = width;
        this.height = height;
        explored = new BitSet(width * height);
        visible = new BitSet(width * height);
        borderPoints = new HashSet<Point>();
    }
    
    public ShadowMap(ShadowMap shadowMap) {
        this.width = shadowMap.width;
        this.height = shadowMap.height;
        this.explored =
            shadowMap.explored.get(0, shadowMap.explored.length());
        this.visible =
            shadowMap.visible.get(0, shadowMap.visible.length());
        borderPoints = shadowMap.getBorderPoints();
    }

    /**
     * @param positions
     * @pre for each p in positions, 0 <= p.x < width && 0 <= p.y < height
     */
    public void setExplored(Collection<Point> positions) {
        for (Point point : positions) {
            assert 0 <= point.x && point.x < width;
            assert 0 <= point.y && point.y < height;
            
            int index = point.x * width + point.y;
            explored.set(index);
        }

        // If it's explored, it's not part of the border.
        borderPoints.removeAll(positions);
        
        // Neighbors of the newly explored hexes may now be on the border.
        for (Point position : positions) {
            for (Point maybe : HexMap.getAllAdjacent(position)) {
                if (isInMap(maybe) && !isExplored(maybe) && !borderPoints.contains(maybe)) {
                    for (Point adjacent : HexMap.getAllAdjacent(maybe)) {
                        if (isInMap(adjacent) && isExplored(adjacent)) {
                            borderPoints.add(maybe);
                            break;
                        }
                    }
                }
            }
        }

        change();
    }

    private boolean isInMap(Point point) {
        return 0 <= point.x && point.x < width &&
            0 <= point.y && point.y < height;
    }

    /**
     * @param positions
     * @pre for each p in positions, 0 <= p.x < width && 0 <= p.y < height
     */
    public void setVisible(Collection<Point> positions) {
        for (Point point : positions) {
            assert 0 <= point.x && point.x < width;
            assert 0 <= point.y && point.y < height;
            
            int index = point.x * width + point.y;
            visible.set(index);
        }

        change();
    }

    /**
     * @param positions
     * @pre for each p in positions, 0 <= p.x < width && 0 <= p.y < height
     */
    public void clearVisible(Collection<Point> positions) {
        for (Point point : positions) {
            assert 0 <= point.x && point.x < width;
            assert 0 <= point.y && point.y < height;
            
            int index = point.x * width + point.y;
            visible.clear(index);
        }

        change();
    }
    
    /**
     * @param point
     * @return
     * @pre point != null
     * @pre 0 <= point.x < width && 0 <= point.y < height
     */
    public ShadowStatus getStatus(Point point) {
        return getStatus(point.x, point.y);
    }
    
    /**
     * @param x
     * @param y
     * @return
     * @pre 0 <= x < width && 0 <= y < height
     */
    public ShadowStatus getStatus(int x, int y) {
        int index = x*width + y;
        ShadowStatus status = inactiveStatus;
        if (active) {
            status = new ShadowStatus(explored.get(index), visible.get(index));
        }

        return status;
    }

    /**
     * Clears the visibility status for all hexes.
     *
     */
    public void clearVisible() {
        visible.clear(0, visible.length());

        change();
    }

    private void change() {
        setChanged();
        notifyObservers();
    }

    /**
     * Returns a collection containing every point for which the status
     * in this map differs from the status in the specified map.
     * 
     * @param shadowMap
     * @return
     * 
     * @pre map != null
     * @pre map.width == this.width && map.height == this.height
     * @post result != null
     */
    public Collection<Point> diff(ShadowMap shadowMap) {
        assert shadowMap.width == this.width &&
            shadowMap.height == this.height;
        
        long startTime = System.currentTimeMillis();
        
        Collection<Point> points = new HashSet<Point>();
        
        if (!this.equals(shadowMap)) {
            for (int i=0; i<width; i++) {
                for (int j=0; j<height; j++) {
                    int index = i*width + j;
                    
                    if (explored.get(index) != shadowMap.explored.get(index) ||
                        visible.get(index) != shadowMap.visible.get(index))
                    {
                        points.add(new Point(i, j));
                    }
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("ExplorationMap.diff found " + points.size() +
                           " diffs in " + (endTime - startTime) + " milliseconds.");

        
        return points;
    }
    
    public boolean isExplored(Point p) {
        return getStatus(p.x, p.y).isExplored();
    }

    public boolean isVisible(Point p) {
        return getStatus(p.x, p.y).isVisible();
    }

    public Dimension getSize() {
        return new Dimension(width, height);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Returns all hexes that are unexplored but adjacent to an
     * explored hex.
     * @return
     */
    public Set<Point> getBorderPoints() {
        return borderPoints;
    }
}
