package org.heckcorp.domination;

import org.heckcorp.domination.desktop.Pathfinder;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This object holds all the hexes from which the playing area is composed.
 * 
 * @author Joachim Heck
 * 
 */
public class HexMap implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * @return  the hexes
     * @uml.property  name="hexes"
     */
    public Hex[][] getHexes() {
        return hexes;
    }

    public HexMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.hexes = new Hex[width][height];
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                hexes[i][j] = new Hex(i, j);
            }
        }
        pathfinder = new Pathfinder(this);
    }
    
    private final Logger log = Logger.getLogger(getClass().getName());
    
    public HexMap(ObjectInputStream in) throws IOException {
        width = in.readInt();
        height = in.readInt();
        
        log.fine("Map dimensions: " + width + "x" + height);

        hexes = new Hex[width][height];
        pathfinder = new Pathfinder(this);
    }
    
    public void write(ObjectOutputStream out) throws IOException {
        out.writeInt(width);
        out.writeInt(height);
    }

    /**
     * @uml.property  name="height"
     */
    public final int height;

    /**
     * @uml.property  name="hexes"
     */
    private final Hex[][] hexes;

    /**
     * @uml.property  name="width"
     */
    public final int width;

    /**
     * @uml.property   name="pathfinder"
     * @uml.associationEnd   multiplicity="(1 1)" inverse="map:org.heckcorp.domination.desktop.Pathfinder"
     */
    private final Pathfinder pathfinder;

    /**
     * @pre adjacent(from, to)
     */
    public static Direction getDirection(Point from, Point to) {
        assert adjacent(from, to);
        
        Direction result = Direction.NORTH;
        
        for (Direction d : Direction.values()) {
            if (getAdjacent(from, d).equals(to)) {
                result = d;
                break;
            }
        }
        
        return result;
    }

    /**
     * Finds all of the hexes within the given range, excluding the specified hex.
     */
    public List<Hex> getHexesInRange(Hex hex, int searchRadius) {
        List<Hex> inRange = new ArrayList<>();
        
        for (int range=1; range<=searchRadius; range++) {
            inRange.addAll(getHexesAtRange(hex, range));
        }

        return inRange;
    }

    /**
     * Returns a set of all the hexes on the map at exactly the
     * specified distance from the specified hex.
     */
    public Set<Hex> getHexesAtRange(Hex hex, int radius) {
        Set<Hex> atRange = new HashSet<>();
        Point current = new Point(hex.getPosition().x,
                                  hex.getPosition().y - radius);
        
        for (Direction side : Direction.values()) {
            // We start out moving southeast, direction 2.
            Direction direction = Direction.values()[(side.value + 2) % 6];
            
            for (int h=0; h<radius; h++) {
                if (isInMap(current)) {
                    atRange.add(hexes[current.x][current.y]);
                }
                
                // Move on to the next hex.
                current = getAdjacent(current, direction);
            }
        }
        
        return atRange;
    }
    
    /**
     * Determins whether the point is within the map.
     */
    public boolean isInMap(Point p) {
        return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
    }

    /**
     * Returns the coordinates adjacent to the specified coordinates,
     * in the specified direction.
     * 
     * @pre direction != null
     */
    public static Point getAdjacent(Point current, Direction direction) {
        assert direction != null;
            
        Point offset = current;
        if (current.x % 2 == 0) {
            // This column is a half-hex lower than its neighbors.
            offset = new Point(current.x, current.y + 1);
        }
        
        Point adjacent = null;
        
        switch (direction) {
        case NORTH:
            adjacent = new Point(current.x, current.y - 1);
            break;
        case NORTHEAST:
            adjacent = new Point(current.x + 1, offset.y - 1);
            break;
        case SOUTHEAST:
            adjacent = new Point(current.x + 1, offset.y);
            break;
        case SOUTH:
            adjacent = new Point(current.x, current.y + 1);
            break;
        case SOUTHWEST:
            adjacent = new Point(current.x - 1, offset.y);
            break;
        case NORTHWEST:
            adjacent = new Point(current.x - 1, offset.y - 1);
            break;
        default:
            assert(false);
        }
        
        return adjacent;
    }

    /**
     * Returns the Hex at the specified coordinates.
     * 
     * @return the Hex at the specified coordinates.
     * 
     * @pre x >= 0 && x < getWidth()
     * @pre y >= 0 && y < getHeight()
     */
    public Hex getHex(int x, int y) {
        return hexes[x][y];
    }

    public Hex getHex(Point position) {
        return getHex(position.x, position.y);
    }

    /**
     * @pre hex != null
     * @pre hex2 != null
     */
    public static boolean isAdjacent(Hex hex, Hex hex2) {
        return Calculator.distance(hex.getPosition(), hex2.getPosition()) == 1;
    }
    
    /**
     * 
     * @return true if the two hex coordinates are adjacent (or the same).
     */
    public static boolean adjacent(Point p1, Point p2) {
        return (p1.x == p2.x && Math.abs(p1.y - p2.y) <= 1.0) ||
                (Math.abs(p1.x - p2.x) == 1.0 &&
                        (p1.x % 2 == 0 && p2.y - p1.y >= 0 && p2.y - p1.y <= 1) ||
                        (p1.x % 2 != 0 && p1.y - p2.y >= 0 && p1.y - p2.y <= 1));
    }

    /**
     * @return  the pathfinder
     * @uml.property  name="pathfinder"
     */
    public Pathfinder getPathfinder() {
        return pathfinder;
    }

    
    public Dimension getSize() {
        return new Dimension(width, height);
    }
    
    /**
     * @pre piece != null
     * @pre position != null
     * @pre isInMap(position)
     */
    public void addGamePiece(GamePiece piece, Point position) {
        assert piece != null;
        assert position != null;
        
        Hex hex = hexes[position.x][position.y];
        
        if (piece instanceof Unit) {
            hex.addUnit((Unit)piece);
        } else {
            assert false;
        }
   }

    public Hex getRandomHex(HexFilter filter) {
        return getRandomHex(filter, 0, 0, 1, 1);
    }

    /**
     * Returns a random hex of the type specified by the filter.
     * @param filter a HexFilter used to choose an acceptable hex.
     * @pre minx > 0 && minx <= 1.0
     * @pre miny > 0 && miny <= 1.0
     * @pre maxx > 0 && maxx <= 1.0
     * @pre maxy > 0 && maxy <= 1.0
     * @pre minx <= maxx
     * @pre miny <= maxy
     * @post filter.accept(result)
     * 
     * TODO: make this find the nearest land hex to the random one. 
     */
    public Hex getRandomHex(HexFilter filter, double minx, double miny,
                            double maxx, double maxy)
    {
        assert minx >= 0 && minx <= 1.0
        && miny >= 0 && miny <= 1.0
        && maxx >= 0 && maxx <= 1.0
        && maxy >= 0 && maxy <= 1.0
        && minx <= maxx
        && miny <= maxy :
            "Invalid constraints: " + minx + "," + miny
            + " -> " + maxx + "," + maxy;
    
        // First, get all the hexes in the region that pass the filter.
        int startX = (int) (width * minx);
        int startY = (int) (height * miny);
        int endX = (int) (width * maxx);
        int endY = (int) (height * maxy);
        List<Hex> hexes = new ArrayList<>();
    
        for (int i=startX; i<endX; i++) {
            for (int j=startY; j<endY; j++) {
                Hex hex = getHex(i, j);
                if (filter.accept(hex)) {
                    hexes.add(hex);
                }
            }
        }
    
        return hexes.get(new Random().nextInt(hexes.size()));
    }

    public Hex getAdjacentHex(Hex hex, Direction direction) {
        Point adjacent = getAdjacent(hex.getPosition(), direction);
        return hexes[adjacent.x][adjacent.y];
    }

    public static Direction getDirection(Hex hex, Hex destHex) {
        return getDirection(hex.getPosition(), destHex.getPosition());
    }
    
    public static Set<Point> getAllAdjacent(Point point) {
        Set<Point> allAdjacent = new HashSet<>();
        
        for (Direction direction : Direction.values()) {
            allAdjacent.add(getAdjacent(point, direction));
        }
        
        return allAdjacent;
    }

    public Set<Hex> getHexes(Set<Point> points) {
        Set<Hex> result = new HashSet<>();

        for (Point point : points) {
            result.add(hexes[point.x][point.y]);
        }

        return result;
    }

    /**
     * @pre hex != null
     */
    public Set<Hex> getAdjacentHexes(Positionable pos) {
        Set<Hex> adjacent = new HashSet<>();
        
        for (Direction direction : Direction.values()) {
            Point p = getAdjacent(pos.getPosition(), direction);
            if (isInMap(p)) {
                adjacent.add(getHex(p));
            }
        }
        
        return adjacent;
    }
}
    