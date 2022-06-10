package org.heckcorp.spacegame.ui.map;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;

public class MapUtils {

  Point2D getHexCorner(Point position) {
    double pixelX = position.x() * columnWidth;
    double pixelY = position.y() * hexHeight;
    if (position.x() % 2 != 0) {
      pixelY += hexHeight / 2;
    }
    return new Point2D(pixelX, pixelY);
  }

  /** Returns the pixel coordinates of the center of the hex with the specified map coordinates. */
  public Point2D getHexCenter(Point position) {
    double pixelX = position.x() * columnWidth;
    double pixelY = position.y() * hexHeight;
    if (position.x() % 2 != 0) {
      pixelY += hexHeight / 2;
    }
    return new Point2D(pixelX + hexWidth / 2.0, pixelY + hexHeight / 2.0);
  }

  public Polygon getHexagon(Point hexCoordinates) {
    Point2D corner = getHexCorner(hexCoordinates);
    return new Polygon(
        corner.getX() + 32d,
        corner.getY(),
        corner.getX() + 96d,
        corner.getY(),
        corner.getX() + 128d,
        corner.getY() + 55d,
        corner.getX() + 96d,
        corner.getY() + 110d,
        corner.getX() + 32d,
        corner.getY() + 110d,
        corner.getX(),
        corner.getY() + 55d);
  }

  /**
   * @param canvasPoint the pixel coordinates of the point in this component's.
   * @return the map coordinates of a hex that contains the specified point, or null if the clicked
   *     point is not in a hex.
   */
  public Point getHexCoordinates(Point2D canvasPoint) {
    Point[] guesses = guessHex(canvasPoint);

    double minDistance = canvasPoint.distance(new Point2D(guesses[0].x(), guesses[0].y()));
    Point closest = guesses[0];
    for (Point guess : guesses) {
      Point2D guessCenter = getHexCenter(guess);
      double distance = canvasPoint.distance(guessCenter);
      if (distance < minDistance) {
        minDistance = distance;
        closest = guess;
      }
    }

    return new Point(closest.x(), closest.y());
  }

  /**
   * Returns three sets of viewport coordinates, one of which corresponds to the clicked-on hex.
   */
  private Point[] guessHex(Point2D screenPoint) {
    int columnGuess = (int) (screenPoint.getX() / columnWidth);
    int rowShift = columnGuess % 2 != 0 ? 1 : 0;
    int rowGuess = (int) ((screenPoint.getY() - (rowShift * hexHeight / 2.0)) / hexHeight);

    Point[] guesses = new Point[3];
    guesses[0] = new Point(columnGuess, rowGuess);
    guesses[1] = new Point(columnGuess - 1, rowGuess - 1 + rowShift);
    guesses[2] = new Point(columnGuess - 1, rowGuess + rowShift);

    return guesses;
  }

  public MapUtils(double hexRadius) {
    this.hexHeight = hexRadius * Math.sqrt(3);
    this.columnWidth = hexRadius * 3.0 / 2.0;
    this.hexWidth = 2.0 * hexRadius;
  }

  private final double hexHeight;
  private final double hexWidth;
  private final double columnWidth;
}
