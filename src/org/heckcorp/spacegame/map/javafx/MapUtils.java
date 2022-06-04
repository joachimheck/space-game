package org.heckcorp.spacegame.map.javafx;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import org.heckcorp.spacegame.map.swing.Util;

import java.io.FileNotFoundException;

public class MapUtils {

    public int getTileHeight() {
        return tileHeight;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public Image getTilePic() {
        return tilePic;
    }

    public MapUtils() throws FileNotFoundException {
        tilePic = new Image(Util.getResource("resource/hex-large-light.png"));
        tileWidth = (int) Math.floor(3.0 * tilePic.getWidth() / 4.0);
        tileHeight = (int) tilePic.getHeight();
        assert tileWidth % 4 == 0;
        assert tileHeight % 2 == 0;
    }

    Point2D getHexCorner(Point position) {
        int pixelX = position.x * tileWidth;
        int pixelY = position.y * tileHeight;
        if (position.x % 2 != 0) {
            pixelY += tileHeight / 2;
        }
        return new Point2D(pixelX, pixelY);
    }

    /**
     * Returns the pixel coordinates of the center of the hex
     * with the specified map coordinates.
     */
    public Point2D getHexCenter(Point position) {
        int pixelX = position.x * tileWidth;
        int pixelY = position.y * tileHeight;
        if (position.x % 2 != 0) {
            pixelY += tileHeight / 2;
        }
        return new Point2D(pixelX + tilePic.getWidth() / 2.0, pixelY + tileHeight / 2.0);
    }

    /**
     * @param canvasPoint the pixel coordinates of the point in this component's.
     * @return the map coordinates of a hex that contains the specified
     *   point, or null if the clicked point is not in a hex.
     */
    public MapUtils.Point getHexCoordinates(Point2D canvasPoint) {
        MapUtils.Point[] guesses = guessHex(canvasPoint);

        double minDistance = canvasPoint.distance(new Point2D(guesses[0].x(), guesses[0].y()));
        MapUtils.Point closest = guesses[0];
        for (MapUtils.Point guess : guesses) {
            Point2D guessCenter = getHexCenter(guess);
            double distance = canvasPoint.distance(guessCenter);
            if (distance < minDistance) {
                minDistance = distance;
                closest = guess;
            }
        }

        return new MapUtils.Point(closest.x(),  closest.y());
    }

    /**
     * Returns three sets of viewport coordinates, one of which corresponds to the clicked-on hex.
     */
    public MapUtils.Point[] guessHex(Point2D screenPoint) {
        int columnGuess = (int) (screenPoint.getX() / tileWidth);
        int rowShift = columnGuess % 2 != 0 ? 1 : 0;
        int rowGuess = (int) (screenPoint.getY() - (rowShift * tileHeight / 2)) / tileHeight;

        MapUtils.Point[] guesses = new MapUtils.Point[3];
        guesses[0] = new MapUtils.Point(columnGuess, rowGuess);
        guesses[1] = new MapUtils.Point(columnGuess - 1, rowGuess - 1 + rowShift);
        guesses[2] = new MapUtils.Point(columnGuess - 1, rowGuess + rowShift);

        return guesses;
    }

    public record Point(int x, int y) { }

    private final int tileHeight;
    private final int tileWidth;
    private final Image tilePic;
}