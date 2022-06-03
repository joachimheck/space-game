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

    public record Point(int x, int y) { }

    private final int tileHeight;
    private final int tileWidth;
    private final Image tilePic;
}
