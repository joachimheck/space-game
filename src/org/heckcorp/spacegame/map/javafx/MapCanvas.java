package org.heckcorp.spacegame.map.javafx;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.heckcorp.spacegame.map.Point;

public class MapCanvas extends Canvas {
  private MapCanvas(MapUtils mapUtils) {
    this.mapUtils = mapUtils;
  }

  public static MapCanvas build(MapUtils mapUtils, int width, int height) {
    MapCanvas mapCanvas = new MapCanvas(mapUtils);

    mapCanvas.setWidth(mapUtils.getTileWidth() * (width + 1.0 / 3));
    mapCanvas.setHeight(mapUtils.getTileHeight() * (height + 1.0 / 2) + 1);

    GraphicsContext gc = mapCanvas.getGraphicsContext2D();
    gc.setStroke(Color.BLACK);
    gc.fillRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        mapCanvas.drawHex(new Point(i, j), gc);
      }
    }

    return mapCanvas;
  }

  /**
   * @param position the position on the screen to draw the hex.
   * @param gc a GraphicsContext to draw in.
   */
  private void drawHex(Point position, GraphicsContext gc) {
    Point2D pixelPos = mapUtils.getHexCorner(position);
    gc.drawImage(mapUtils.getTilePic(), pixelPos.getX(), pixelPos.getY());
    gc.setStroke(Color.WHITE);
    gc.strokeText(position.x() + "," + position.y(), pixelPos.getX() + 32, pixelPos.getY() + 16);
  }

  private final MapUtils mapUtils;
}
