package org.heckcorp.domination;

import java.awt.Dimension;
import java.awt.Point;
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

import org.heckcorp.domination.Hex.Terrain;
import org.heckcorp.domination.desktop.Pathfinder;

/**
 * This object holds all the hexes from which the playing area is composed.
 * 
 * @author Joachim Heck
 * 
 */
public class HexMap implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * @author    Joachim Heck
     */
    private static final class MapInfoGenerator {
        private final HexMap map;
        private int[][] elevations;
        private int maxElevation;
        private int minElevation;

        public MapInfoGenerator(HexMap map) {
            this.map = map;
        }

        /**
         * 
         * @param width
         * @param height
         * @post elevations != null
         */
        private void generateElevations(int width, int height) {
            Random random = new Random();
            double[][] preciseElevations = new double[width][height];
            Set<Point> raisedPoints = new HashSet<Point>();
            Set<Point> recentlyRaisedPoints = new HashSet<Point>();
            
            int initialPointCount = 2 + random.nextInt(3);
            int initialRaise = 5;
            double adjacentHeightFactor = .5;
            
            System.out.println("Map has " + initialPointCount + " initial points.");
            
            // Choose several evenly distributed hexes as continental centers.
            for (int i=0; i<initialPointCount; i++) {
                Point initialPoint = new Point(random.nextInt(width),
                                               random.nextInt(height));

                // Raise the center hexes by an initial amount, then mark them raised.
                preciseElevations[initialPoint.x][initialPoint.y] = initialRaise;
                recentlyRaisedPoints.add(initialPoint);
                raisedPoints.add(initialPoint);
            }

            while (isAnyPointElevated(recentlyRaisedPoints, preciseElevations)) {
                // Raise all unmarked hexes adjacent to marked hexes by a semi-random amount,
                //   based on the average height of the adjacent raised hexes, but always less.
                Set<Point> toRaisePoints = new HashSet<Point>();
                for (Point raised : recentlyRaisedPoints) {
                    for (Direction direction : Direction.values()) {
                        Point adjacent = getAdjacent(raised, direction);
        
                        if (!raisedPoints.contains(adjacent) && map.isInMap(adjacent)) {
                            toRaisePoints.add(adjacent);
                        }
                    }
                }
        
                for (Point toRaise: toRaisePoints) {
                    double totalAdjacentRaisedHeight = 0.0;
                    double adjacentRaisedCount = 0.0;
                    double maxHeight = 0.0;
                    for (Direction direction : Direction.values()) {
                        Point adjacent = getAdjacent(toRaise, direction);
        
                        if (raisedPoints.contains(adjacent)) {
                            double adjacentHeight = preciseElevations[adjacent.x][adjacent.y];
                            adjacentRaisedCount++;
                            totalAdjacentRaisedHeight += adjacentHeight;
                            maxHeight = Math.max(maxHeight, adjacentHeight);
                        }
                    }
        
                    double averageAdjacentRaisedHeight =
                        totalAdjacentRaisedHeight / adjacentRaisedCount;
                    double elevation = 0.0;
                    elevation = averageAdjacentRaisedHeight -
                        (Math.random() * (1.0 - adjacentHeightFactor));
                    
                    preciseElevations[toRaise.x][toRaise.y] = elevation;
                }
        
                // Mark the newly raised hexes.
                raisedPoints.addAll(toRaisePoints);
                recentlyRaisedPoints.clear();
                recentlyRaisedPoints.addAll(toRaisePoints);
            }
            
            minElevation = 0;
            maxElevation = initialRaise;
            elevations = new int[width][height];
            for (int i=0; i<width; i++) {
                for (int j=0; j<height; j++) {
                    elevations[i][j] = (int) Math.round(preciseElevations[i][j]);
                }
            }
        }

        public MapInfo generateMapInfo(int width, int height) {
            generateElevations(width, height);
            findContinents(width, height);
        
//            Random random = new Random();
//            int elevationDiff = maxElevation - minElevation;
//            int waterline = minElevation +
//                random.nextInt(2 * elevationDiff / 5);
            int waterline = 0;
            
            System.out.println("Elevation min/max = " + minElevation +
                               "/" + maxElevation + " waterline = " +
                               waterline);
            
            int terrainCounts[] = new int[Hex.Terrain.values().length];
            Hex.Terrain[][] terrains = new Hex.Terrain[width][height];
            for (int i=0; i<width; i++) {
                for (int j=0; j<height; j++) {
                    Hex.Terrain terrain = Hex.Terrain.LAND;
        
                    if (elevations[i][j] <= waterline) {
                        terrain = Hex.Terrain.WATER;
                    }
        
                    terrains[i][j] = terrain;
                    terrainCounts[terrain.value]++;
                }
            }
            
            int total = 0;
            for (int count : terrainCounts) {
                total += count;
            }

            for (Hex.Terrain terrain : Hex.Terrain.values()) {
                System.out.print(terrain.name + "(" + terrain.value + "): " +
                                   terrainCounts[terrain.value] +
                                   " (" + (int) (100.0 * (double)terrainCounts[terrain.value] / (double)total) +
                                   "%) ");
            }
            System.out.println("");
            
            return new MapInfo(width, height, terrains,
                               elevations, minElevation, maxElevation);
        }

        /**
         * Identifies separate land masses in the elevation map.
         * TODO: we don't do anything with this continent information!
         */
        private void findContinents(int width, int height) {
            int[][] continents = new int[width][height];
            int continentId = 0;
            
            for (int i=0; i<width; i++) {
                for (int j=0; j<height; j++) {
                    if (elevations[i][j] > 0 && continents[i][j] == 0) {
                        // Reserve a new number for the next continent.
                        continentId++;
                        
                        // TODO: discover the whole continent.
                        Set<Point> continent = new HashSet<Point>();
                        Set<Point> recentPoints = new HashSet<Point>();
                        Set<Point> newPoints = new HashSet<Point>();
                        
                        // Prime the system with the point we just discovered.
                        recentPoints.add(new Point(i, j));
                        
                        // Find all land points connected to the one we just found.
                        while (!recentPoints.isEmpty()) {
                            for (Point recent : recentPoints) {
                                for (Direction direction : Direction.values()) {
                                    Point adjacent = getAdjacent(recent, direction);

                                    if (map.isInMap(adjacent) &&
                                        elevations[adjacent.x][adjacent.y] > 0 &&
                                        !recentPoints.contains(adjacent) &&
                                        !continent.contains(adjacent))
                                    {
                                        newPoints.add(adjacent);
                                        continents[adjacent.x][adjacent.y] = continentId;
                                    }
                                }
                            }

                            continent.addAll(recentPoints);
                            recentPoints.clear();
                            recentPoints.addAll(newPoints);
                            newPoints.clear();
                        }
                        
                        System.out.println("Continent " + continentId + " has " +
                                           continent.size() + " hexes.");
                    }
                }
            }
        }

        private static boolean isAnyPointElevated(Set<Point> points, double[][] preciseElevations) {
            for (Point p : points) {
                if (preciseElevations[p.x][p.y] > 0.0) {
                    return true;
                }
            }
        
            return false;
        }

    }

    /**
     * @author  Joachim Heck
     */
    public static final class MapInfo {
        public MapInfo(int width, int height, Hex.Terrain[][] terrains,
                       int[][] elevations, int minElevation, int maxElevation)
        {
            this.width = width;
            this.height = height;
            this.terrains = terrains;
            this.elevations = elevations;
            this.minElevation = minElevation;
            this.maxElevation = maxElevation;
        }
        
        public MapInfo(int width, int height,
                       Terrain[][] terrains, int[][] elevations)
        {
            this.width = width;
            this.height = height;
            this.terrains = terrains;
            this.elevations = elevations;
            
            int minElevation = Integer.MAX_VALUE;
            int maxElevation = Integer.MIN_VALUE;
            
            for (int i=0; i<elevations.length; i++) {
                for (int j=0; j<elevations[0].length; j++) {
                    minElevation = Math.min(elevations[i][j], minElevation);
                    maxElevation = Math.max(elevations[i][j], maxElevation);
                }
            }
            
            this.minElevation = minElevation;
            this.maxElevation = maxElevation;
        }

        public final int width;
        public final int height;
        /**
         * @uml.property  name="terrains"
         * @uml.associationEnd  multiplicity="(0 -1)"
         */
        public final Hex.Terrain[][] terrains;
        public final int[][] elevations;
        public final int minElevation;
        public final int maxElevation;
    }

    /**
     * @return  the hexes
     * @uml.property  name="hexes"
     */
    public Hex[][] getHexes() {
        return hexes;
    }

    public HexMap(int width, int height) {
        this(new MapInfo(width, height, null, null, 0, 0));
    }

    public HexMap(MapInfo mapInfo) {
        this.width = mapInfo.width;
        this.height = mapInfo.height;
        this.hexes = new Hex[width][height];
        
        if (mapInfo.terrains == null) {
            mapInfo = new MapInfoGenerator(this).generateMapInfo(width, height);
        }
        
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                hexes[i][j] = new Hex(i, j, mapInfo.terrains[i][j],
                                      mapInfo.elevations[i][j]);
            }
        }
        
        pathfinder = new Pathfinder(this);
    }
    
    private Logger log = Logger.getLogger(getClass().getName());
    
    public HexMap(ObjectInputStream in) throws IOException {
        width = in.readInt();
        height = in.readInt();
        
        log.fine("Map dimensions: " + width + "x" + height);

        hexes = new Hex[width][height];
        
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                hexes[i][j] = new Hex(i, j, Terrain.values()[in.readInt()], in.readInt());
                log.finer("Map loaded hex at " + i + ", " + j);
            }
        }
        
        pathfinder = new Pathfinder(this);
    }
    
    public void write(ObjectOutputStream out) throws IOException {
        out.writeInt(width);
        out.writeInt(height);
        
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                log.info("Writing hex at " + i + ", " + j);
                out.writeInt(hexes[i][j].terrain.ordinal());
                out.writeInt(hexes[i][j].elevation);
            }
        }
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
     * 
     * @param hex
     * @param searchRadius
     * 
     * @return
     * 
     * @!result.isEmpty()
     */
    public List<Hex> getHexesInRange(Hex hex, int searchRadius) {
        List<Hex> inRange = new ArrayList<Hex>();
        
        for (int range=1; range<=searchRadius; range++) {
            inRange.addAll(getHexesAtRange(hex, range));
        }

        return inRange;
    }

    /**
     * Returns a set of all the hexes on the map at exactly the
     * specified distance from the specified hex.
     * @param hex
     * @param radius
     * @return
     */
    public Set<Hex> getHexesAtRange(Hex hex, int radius) {
        Set<Hex> atRange = new HashSet<Hex>();
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
     * @param p
     * @return
     */
    public boolean isInMap(Point p) {
        return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
    }

    /**
     * Returns the coordinates adjacent to the specified coordinates,
     * in the specified direction.
     * 
     * @param current
     * @param direction
     * @return
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

    public Hex.Terrain[][] getTerrains() {
        Hex.Terrain[][] terrains = new Hex.Terrain[width][height];
        
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                terrains[i][j] = hexes[i][j].terrain;
            }
        }
        
        return terrains;
    }

    /**
     * Returns the Hex at the specified coordinates.
     * 
     * @param x
     * @param y
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
     * @param hex
     * @param hex2
     * @pre hex != null
     * @pre hex2 != null
     * @return
     */
    public static boolean isAdjacent(Hex hex, Hex hex2) {
        return Calculator.distance(hex.getPosition(), hex2.getPosition()) == 1;
    }
    
    /**
     * 
     * @param p1
     * @param p2
     * @return true if the two hex coordinates are adjacent (or the same).
     */
    public static boolean adjacent(Point p1, Point p2) {
        if ((p1.x == p2.x && Math.abs(p1.y - p2.y) <= 1.0) ||
            (Math.abs(p1.x - p2.x) == 1.0 &&
                (p1.x % 2 == 0 && p2.y - p1.y >= 0 && p2.y - p1.y <= 1) ||
                (p1.x % 2 != 0 && p1.y - p2.y >= 0 && p1.y - p2.y <= 1))) {
            return true;
        }
        
        return false;
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
     * @param piece
     * @param position
     * @pre piece != null
     * @pre position != null
     * @pre isInMap(position)
     */
    public void addGamePiece(GamePiece piece, Point position) {
        assert piece != null;
        assert position != null;
        
        Hex hex = hexes[position.x][position.y];
        
        if (piece instanceof City) {
            hex.addCity((City)piece);
        } else if (piece instanceof Unit) {
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
     * @param minx
     * @param miny
     * @param maxx
     * @param maxy
     * @return
     * 
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
        List<Hex> hexes = new ArrayList<Hex>();
    
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
        Set<Point> allAdjacent = new HashSet<Point>();
        
        for (Direction direction : Direction.values()) {
            allAdjacent.add(getAdjacent(point, direction));
        }
        
        return allAdjacent;
    }

    /**
     * Returns the point from the given set that is closest to the
     * given position.
     * @param position
     * @param points
     * @return
     */
    public static Point getClosest(Point position, Set<Point> points) {
        int minDistance = Integer.MAX_VALUE;
        Point closest = null;
        
        for (Point point : points) {
            int distance = Math.min(minDistance, Calculator.distance(position, point));
            if (distance < minDistance) {
                minDistance = distance;
                closest = point;
            }
        }
        
        return closest;
    }

    public Set<Hex> getHexes(Set<Point> borderPoints) {
        Set<Hex> result = new HashSet<Hex>();
        
        for (Point point : borderPoints) {
            result.add(hexes[point.x][point.y]);
        }
        
        return result;
    }

    /**
     * @param pos
     * @return
     * @pre hex != null
     */
    public Set<Hex> getAdjacentHexes(Positionable pos) {
        Set<Hex> adjacent = new HashSet<Hex>();
        
        for (Direction direction : Direction.values()) {
            Point p = getAdjacent(pos.getPosition(), direction);
            if (isInMap(p)) {
                adjacent.add(getHex(p));
            }
        }
        
        return adjacent;
    }
}
    