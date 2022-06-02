package org.heckcorp.spacegame.map.javafx;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.map.swing.Util;

import java.io.FileNotFoundException;

public class MapCanvas extends Canvas {
    public MapCanvas(HexMap map) throws FileNotFoundException {
        tilePic = new Image(Util.getResource("resource/hex-large-light.png"));
        tileWidth = (int) Math.floor(3.0 * tilePic.getWidth() / 4.0);
        tileHeight = (int) tilePic.getHeight();
        assert tileWidth % 4 == 0;
        assert tileHeight % 2 == 0;

        setWidth(tileWidth * (map.width + 1.0 / 3));
        setHeight(tileHeight * (map.height + 1.0 / 2) + 1);

        GraphicsContext gc = this.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());

        for (int i=0; i<map.width; i++) {
            for (int j=0; j<map.height; j++) {
                drawHex(new Point(i, j), gc);
            }
        }
    }

    /**
     * @param position the position on the screen to draw the hex.
     * @param gc a Graphics to draw in.
     */
    private void drawHex(Point position, GraphicsContext gc) {
        Point2D pixelPos = getHexCorner(position);
        gc.drawImage(tilePic, pixelPos.getX(), pixelPos.getY());
        gc.setStroke(Color.WHITE);
        gc.strokeText(position.x + "," + position.y, pixelPos.getX() + 32, pixelPos.getY() + 16);
    }

    private Point2D getHexCorner(Point position) {
        int pixelX = position.x * tileWidth;
        int pixelY = position.y * tileHeight;
        if (position.x % 2 != 0) {
            pixelY += tileHeight / 2;
        }
        return new Point2D(pixelX, pixelY);
    }

    private record Point(int x, int y) {}


    private final Image tilePic;
    private final int tileHeight;
    private final int tileWidth;
}
