package org.heckcorp.spacegame.map.swing;

import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.HexMap;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MapPane extends JPanel {
    public MapPane() {
        this.resources = UIResources.getInstance();
        this.viewport = new Rectangle(0, 0, 0, 0);

        tileWidth = (int) Math.floor(3.0 * resources.tilePic[0].getWidth() / 4.0);
        tileHeight = resources.tilePic[0].getHeight();
        assert tileWidth % 4 == 0;
        assert tileHeight % 2 == 0;

    }

    public void initialize(@NotNull HexMap map) {
        this.map = map;

        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.darkGray));
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        Dimension viewportSize = getViewportSize(map, new Dimension(width, height));
        setViewportBounds(new Rectangle(viewport.getLocation(), viewportSize));

        Dimension viewportPixelSize = getViewportPixelSize(viewportSize);
        int borderWidth = (width - viewportPixelSize.width) / 2;
        int borderHeight = (height - viewportPixelSize.height) / 2;
        setBorder(BorderFactory.createMatteBorder(borderHeight, borderWidth,
                                                  borderHeight, borderWidth,
                                                  Color.darkGray));
        super.setBounds(x, y, width, height);
    }

    public void setViewportBounds(Rectangle bounds) {
        viewport.setBounds(bounds);
    }

    /**
     * @param screenPoint the pixel coordinates of the point, in this
     *   component's coordinate space.
     * @return the map coordinates of a hex that contains the specified
     *   point, or null if the clicked point is not in a hex.
     * @post result.isVisible()
     */
    public Point getHexCoordinates(Point screenPoint) {
        Point[] guesses = guessHex(screenPoint, viewport);

        double minDistance = screenPoint.distance(guesses[0]);
        Point closest = guesses[0];
        for (Point guess : guesses) {
            if (isInViewport(guess)) {
                Point guessCenter = getViewportHexCenter(guess);
                double distance = screenPoint.distance(guessCenter);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = guess;
                }
            }
        }

        if (isInViewport(closest)) {
            closest = toMapCoordinates(closest);
        } else {
            closest = null;
        }

        return closest;
    }

    /**
     * Returns the pixel coordinates of the center of the hex
     * with the specified map coordinates.
     */
    public Point getHexCenter(Point pos) {
        Point viewPos = toViewportCoordinates(pos);
        int pixelX = viewPos.x * tileWidth + getInsets().left;
        int pixelY = viewPos.y * tileHeight + getInsets().top;
        if ((viewPos.x + viewport.x) % 2 == 0) {
            pixelY += tileHeight / 2;
        }

        return new Point(pixelX + 2*tileWidth/3, pixelY + tileHeight/2);
    }

    public Point getHexCorner(Point position) {
        int pixelX = tileWidth * position.x +
        getInsets().left;
        int pixelY = tileHeight * position.y +
        getInsets().top;
        if ((position.x + viewport.x) % 2 == 0) {
            pixelY += tileHeight / 2;
        }

        return new Point(pixelX, pixelY);
    }

    public Dimension getViewportPixelSize(Dimension viewportSize) {
        int mapViewWidth = viewportSize.width * tileWidth + tileWidth / 3;
        int mapViewHeight = viewportSize.height * tileHeight + tileHeight / 2;
        return new Dimension(mapViewWidth, mapViewHeight);
    }

    /**
     * Returns the viewport size, in hexes.
     * @param size the size of the viewport component, in pixels.
     */
    public Dimension getViewportSize(HexMap map, Dimension size) {
        int hexesAcross = Math.min(map.width,
                                   (size.width - tileWidth / 3) / tileWidth);
        int hexesDown = Math.min(map.height,
                                 (size.height - tileHeight / 2) / tileHeight);
        return new Dimension(hexesAcross, hexesDown);
    }

    /**
     * Returns three sets of viewport coordinates, one of which
     * corresponds to the clicked-on hex.
     */
    public Point[] guessHex(Point screenPoint, Rectangle viewport) {
        int columnGuess = (screenPoint.x - getInsets().left) / tileWidth;
        int rowShift = 0;
        if ((columnGuess + viewport.x) % 2 == 0) {
            rowShift = 1;
        }
        int rowGuess =
            ((screenPoint.y - getInsets().top) - (rowShift * tileHeight / 2)) /
            tileHeight;

        Point[] guesses = new Point[3];
        guesses[0] = new Point(columnGuess, rowGuess);
        guesses[1] = new Point(columnGuess - 1, rowGuess - 1 + rowShift);
        guesses[2] = new Point(columnGuess - 1, rowGuess + rowShift);

        return guesses;
    }

    @Override
    public void paintComponent(Graphics gIn) {
        if (map != null) {
            Point hexPosition = new Point(-1, -1);
            Point screenPosition = new Point(-1, -1);
            Graphics2D g = (Graphics2D) gIn;
            g.setBackground(Color.gray);
            g.clearRect(0, 0, getWidth(), getHeight());

            for (int i=0; i<viewport.width; i++) {
                for (int j=0; j<viewport.height; j++) {
                    screenPosition.move(i, j);
                    hexPosition.move(i + viewport.x, j + viewport.y);
                    drawHex(map.getHex(hexPosition), screenPosition, g);
                }
            }
        } else {
            super.paintComponent(gIn);
        }
    }

    /**
     * @pre the viewport fits entirely inside the map.
     */
    public void setViewportPosition(Point position) {
        assert position.x >= 0;
        assert position.y >= 0;
        assert position.x + viewport.width <= map.width :
            position.x + " + " + viewport.width + " > " + map.width + "!";
        assert position.y + viewport.height <= map.height :
            position.y + " + " + viewport.height + " > " + map.height + "!";

        viewport.setLocation(position);
    }

    /**
     * @param hex the hex to draw.
     * @param position the position on the screen to draw the hex.
     * @param g a Graphics to draw in.
     */
    private void drawHex(Hex hex, Point position, Graphics2D g) {
        Point pixelPos = getHexCorner(position);
        g.drawImage(resources.tilePic[0], pixelPos.x, pixelPos.y, null);
        g.setColor(Color.white);
        g.drawString(hex.getPosition().x + "," + hex.getPosition().y, pixelPos.x + 32, pixelPos.y + 16);
    }

    /**
     * Return true if the hex with the specified coordinates is visible in this viewport.
     */
    public boolean isMapPointInViewport(Point mapPos) {
        return isInViewport(toViewportCoordinates(mapPos));
    }

    /**
     * Return true if the hex position with the specified viewport coordinates lies within the bounds of this viewport.
     * @param viewPos the map coordinates to check.
     */
    public boolean isInViewport(Point viewPos) {
        Rectangle rect = new Rectangle(0, 0, viewport.width, viewport.height);
        return rect.contains(viewPos);
    }

    /**
     * Returns the pixel coordinates, in this component's coordinate space,
     * of the center of the hex at the specified viewport coordinates.
     *
     * @param viewPos the viewport coordinates of the hex.
     * @pre isInViewport(pos)
     */
    public Point getViewportHexCenter(Point viewPos) {
        assert isInViewport(viewPos) : viewPos;

        return getHexCenter(toMapCoordinates(viewPos));
    }

    public void drawHexCenters(Point screenPoint) {
        Graphics g = getGraphics();
        int radius = 5;
        Point[] guesses = guessHex(screenPoint, viewport);

        double minDistance = screenPoint.distance(guesses[0]);
        Point closest = guesses[0];
        for (Point guess : guesses) {
            Point guessCenter = getViewportHexCenter(guess);
            double distance = screenPoint.distance(guessCenter);

            if (distance < minDistance) {
                minDistance = distance;
                closest = guess;
            }

            g.setColor(Color.red);
            g.fillArc(guessCenter.x-radius, guessCenter.y-radius,
                      2*radius, 2*radius, 0, 360);
            g.drawLine(guessCenter.x, guessCenter.y, screenPoint.x, screenPoint.y);
        }

        g.setColor(Color.green);
        g.fillArc(getViewportHexCenter(closest).x-radius, getViewportHexCenter(closest).y-radius,
                  2*radius, 2*radius, 0, 360);
        g.drawLine(getViewportHexCenter(closest).x, getViewportHexCenter(closest).y, screenPoint.x, screenPoint.y);
    }

    /**
     * Converts map coordinates to viewport coordinates.  This method
     * may return values that are outside the viewport bounds.
     */
    private Point toViewportCoordinates(Point pos) {
        return new Point(pos.x - viewport.x, pos.y - viewport.y);
    }

    /**
     * Converts viewport coordinates to map coordinates.
     * @post isInViewport(result)
     */
    private Point toMapCoordinates(Point pos) {
        return new Point(pos.x + viewport.x, pos.y + viewport.y);
    }

    public Rectangle getViewport() {
        return viewport;
    }

    private final int tileHeight;
    private final int tileWidth;
    private HexMap map;
    private final UIResources resources;
    private final Rectangle viewport;
}
