package org.heckcorp.spacegame.ui.map;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;

public class MapUtils {
  public Point2D getHexCenter(Point position) {
    double pixelX = position.x() * getMinorRadius();
    double pixelY = position.y() * getHexHeight();
    if (position.x() % 2 != 0) {
      pixelY += getHexHeight() / 2;
    }
    return new Point2D(pixelX + getHexWidth() / 2.0, pixelY + getHexHeight() / 2.0);
  }

  public Point2D getHexLabelPosition(Point position) {
    Point2D center = getHexCenter(position);
    return new Point2D(center.getX() - hexRadius / 2.0, center.getY() - hexRadius * Math.sqrt(3) / 2.0);
  }

  public Polygon getHexagon(Point hexCoordinates) {
    Point2D center = getHexCenter(hexCoordinates);
    double minorRadius = hexRadius * Math.sqrt(3) / 2.0;
    double halfRadius = hexRadius / 2.0;
    return new Polygon(
        center.getX() - halfRadius,
        center.getY() - minorRadius,
        center.getX() + halfRadius,
        center.getY() - minorRadius,
        center.getX() + hexRadius,
        center.getY(),
        center.getX() + halfRadius,
        center.getY() + minorRadius,
        center.getX() - halfRadius,
        center.getY() + minorRadius,
        center.getX() - hexRadius,
        center.getY());
  }

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

  /** Returns three sets of canvas coordinates, one of which corresponds to the clicked-on hex. */
  private Point[] guessHex(Point2D canvasPoint) {
    int columnGuess = (int) (canvasPoint.getX() / getMinorRadius());
    int rowShift = columnGuess % 2 != 0 ? 1 : 0;
    int rowGuess =
        (int) ((canvasPoint.getY() - (rowShift * getHexHeight() / 2.0)) / getHexHeight());

    Point[] guesses = new Point[3];
    guesses[0] = new Point(columnGuess, rowGuess);
    guesses[1] = new Point(columnGuess - 1, rowGuess - 1 + rowShift);
    guesses[2] = new Point(columnGuess - 1, rowGuess + rowShift);

    return guesses;
  }

  public double getMinorRadius() {
    return hexRadius * 3.0 / 2.0;
  }

  public double getHexHeight() {
    return hexRadius * Math.sqrt(3);
  }

  public double getHexWidth() {
    return 2.0 * hexRadius;
  }

  public MapUtils(double hexRadius) {
    this.hexRadius = hexRadius;
  }

  private final double hexRadius;
}
