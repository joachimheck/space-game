package org.heckcorp.spacegame.ui.map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import org.heckcorp.spacegame.Constants;
import org.heckcorp.spacegame.model.Direction;
import org.heckcorp.spacegame.model.MapPosition;

import java.util.Set;
import java.util.stream.Collectors;

import static org.heckcorp.spacegame.Constants.MAP_HEIGHT;
import static org.heckcorp.spacegame.Constants.MAP_WIDTH;

public class MapUtils {

  public int distance(Point p1, Point p2) {
    int distance;

    if (p1 == p2) {
      distance = 0;
    } else if (p1.x() == p2.x()) {
      distance = p1.y() - p2.y();
    } else {
      // even->odd 0/+1
      // odd-even -1/0

      int xDiff = Math.abs(p1.x() - p2.x());

      int minMod = xDiff-1;
      int maxMod = xDiff-1;

      if (p1.x() % 2 == 0 && p2.x() % 2 != 0) {
        minMod = minMod - 1;
      } else if (p1.x() % 2 != 0 && p2.x() % 2 == 0) {
        maxMod = maxMod - 1;
      }

      // By moving diagonally, we can move from p1.y() to between
      // p1.y()-minMod and p1.y()+maxMod.
      int minY = p1.y() - minMod;
      int maxY = p1.y() + maxMod;
      int yDiff = 0;

      if (p2.y() < minY || p2.y() > maxY) {
        yDiff = Math.min(Math.abs(minY - p2.y()), Math.abs(maxY - p2.y()));
      }

      distance = xDiff + yDiff;
    }

    if (distance < 0) {
      distance = distance * -1;
    }

    return distance;
  }

  public Point getAdjacentHex(MapPosition mapPosition) {
    return getAdjacentHex(mapPosition.position(), mapPosition.direction());
  }

  public Point getAdjacentHex(Point pos, Direction direction) {
    return switch(direction) {
      case NORTH -> new Point(pos.x(), pos.y() - 1);
      case NORTHEAST -> new Point(pos.x() + 1, pos.x() % 2 != 0 ? pos.y() : pos.y() - 1);
      case SOUTHEAST -> new Point(pos.x() + 1, pos.x() % 2 != 0 ? pos.y() + 1 : pos.y());
      case SOUTH -> new Point(pos.x(), pos.y() + 1);
      case SOUTHWEST -> new Point(pos.x() - 1, pos.x() % 2 != 0 ? pos.y() + 1 : pos.y());
      case NORTHWEST -> new Point(pos.x() - 1, pos.x() % 2 != 0 ? pos.y() : pos.y() - 1);
    };
  }

  public double getColumnWidth() {
    return hexRadius * 3.0 / 2.0;
  }

  public Point2D getHexCenter(Point position) {
    double pixelX = position.x() * getColumnWidth();
    double pixelY = position.y() * getMinorRadius() * 2.0;
    if (position.x() % 2 != 0) {
      pixelY += getMinorRadius();
    }
    return new Point2D(pixelX + hexRadius, pixelY + getMinorRadius());
  }

  public Point2D getHexLabelPosition(Point position) {
    Point2D center = getHexCenter(position);
    return new Point2D(
        center.getX() - hexRadius / 2.0, center.getY() - hexRadius * Math.sqrt(3) / 2.0);
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

  public double getMinorRadius() {
    return hexRadius * Math.sqrt(3.0) / 2.0;
  }

  public Set<Point> getTargetHexes(MapPosition unitPosition) {
    Point hexInFront = getAdjacentHex(unitPosition);
    ImmutableSet<Direction> directions =
        ImmutableSet.of(
            unitPosition.direction().left(),
            unitPosition.direction(),
            unitPosition.direction().right());
    Set<Point> targetHexes = Sets.newHashSet();
    Set<Point> hexes = Sets.newHashSet(hexInFront);
    for (int i = 0; i < Constants.WEAPON_RANGE; i++) {
      targetHexes.addAll(hexes);
      Set<Point> newHexes =
          hexes.stream()
              .flatMap(p -> directions.stream().map(d -> getAdjacentHex(p, d)))
              .collect(Collectors.toSet());
      hexes.clear();
      hexes = newHexes;
    }
    return targetHexes.stream().filter(this::isInsideMap).collect(Collectors.toSet());
  }

  public boolean isInsideMap(Point point) {
    return point.x() >= 0 && point.x() < MAP_WIDTH && point.y() >= 0 && point.y() < MAP_HEIGHT;
  }

  /** Returns three sets of canvas coordinates, one of which corresponds to the clicked-on hex. */
  private Point[] guessHex(Point2D canvasPoint) {
    int columnGuess = (int) (canvasPoint.getX() / getColumnWidth());
    int rowShift = columnGuess % 2 != 0 ? 1 : 0;
    int rowGuess =
        (int) ((canvasPoint.getY() - (rowShift * getMinorRadius())) / (getMinorRadius() * 2.0));

    Point[] guesses = new Point[3];
    guesses[0] = new Point(columnGuess, rowGuess);
    guesses[1] = new Point(columnGuess - 1, rowGuess - 1 + rowShift);
    guesses[2] = new Point(columnGuess - 1, rowGuess + rowShift);

    return guesses;
  }

  public MapUtils(double hexRadius) {
    this.hexRadius = hexRadius;
  }

  private final double hexRadius;
}
