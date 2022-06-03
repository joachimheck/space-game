package org.heckcorp.spacegame.map.javafx;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.map.javafx.MapUtils.Point;

public class MapCanvas extends Canvas {
    public MapCanvas(HexMap map, MapUtils mapUtils) {
        this.mapUtils = mapUtils;

        setWidth(mapUtils.getTileWidth() * (map.width + 1.0 / 3));
        setHeight(mapUtils.getTileHeight() * (map.height + 1.0 / 2) + 1);

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
        Point2D pixelPos = mapUtils.getHexCorner(position);
        gc.drawImage(mapUtils.getTilePic(), pixelPos.getX(), pixelPos.getY());
        gc.setStroke(Color.WHITE);
        gc.strokeText(position.x() + "," + position.y(), pixelPos.getX() + 32, pixelPos.getY() + 16);
    }

    private final MapUtils mapUtils;
}
