package org.heckcorp.spacegame.map.swing;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class Counter extends JLabel {
    public Counter(BufferedImage[] unitPix, @Nullable Color borderColor, Point hexCenter, @Nullable Point mapPosition) {
        this.mapPosition = mapPosition;
        icon = new AnimatedImageIcon(unitPix);

        icon.setAnimated(false);
        icon.setLoop(true);
        icon.setAnimationTime(ANIMATION_TIME);
        icon.setImageObserver(this);

        super.setIcon(icon);

        this.borderColor = borderColor;

        this.oldLocation = new Point(0, 0);

        setSize(icon.getIconWidth(), icon.getIconHeight());
        setCenterLocation(hexCenter);

        setSpeed(MOVE_SPEED);

        int frameRate = 100;
        int timerDelay = (int) (1000.0 / (double) frameRate);
        moveTimer = new Timer(timerDelay, e -> updateCounterLocation());
    }

    public void setAnimated(boolean b) {
        icon.setAnimated(b);
    }

    public AnimatedImageIcon getIcon() {
        return (AnimatedImageIcon) super.getIcon();
    }

    public Point getCenterLocation() {
        return new Point(mapLocation.x + (getWidth() / 2),
            mapLocation.y + (getHeight() / 2));
    }

    public void setCenterLocation(Point location) {
        setMapLocation(new Point(location.x - (getWidth() / 2),
                location.y - (getHeight() / 2)));
    }

    public void moveCenterTo(final Point destination) {
        moveTo(new Point(destination.x - (getWidth() / 2),
                destination.y - (getHeight() / 2)));
    }

    public synchronized void moveTo(final Point destination) {
        setAnimated(true);
        this.destination = destination;
        realLocation = new Point2D.Double(mapLocation.x, mapLocation.y);

        Point2D movementVector = new Point2D.Double(destination.x -
                getLocation().x, destination.y - getLocation().y);

        double length = Math.sqrt((movementVector.getX() * movementVector.getX()) +
                (movementVector.getY() * movementVector.getY()));
        normal = new Point2D.Double(movementVector.getX() / length,
                movementVector.getY() / length);
        lastTime = System.currentTimeMillis();
        moveTimer.start();
    }

    public synchronized void setMapLocation(Point location) {
        oldLocation.setLocation(mapLocation);
        mapLocation.setLocation(location);
        super.setLocation(mapLocation.x + offset.x, mapLocation.y + offset.y);

        if (mapLocation.equals(destination)) {
            destination = null;
            normal = null;

            setAnimated(false);

            // It's important to stop the timer before calling notifyObservers.
            // Otherwise, we set destination and end up having multiple timers
            // running simultaneously, speeding up movement with every hex.
            moveTimer.stop();
            if (toNotify != null) {
                // TODO: use javafx Animation to avoid doing the thread stuff myself? Would that help?
                //noinspection SynchronizeOnNonFinalField
                synchronized (toNotify) {
                    toNotify.notify();
                }
            }
        }
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    private void updateCounterLocation() {
        if (destination != null && normal != null) {
            // Distance = speed * time
            long current = System.currentTimeMillis();
            long elapsed = current - lastTime;
            lastTime = current;

            // TODO: this seems to be smoother when I just pretend the timer
            // runs regularly - shouldn't that make it rougher?
            //            double distance = speed * ((double) moveTimer.getDelay() / 1000.0);
            double distance = speed * ((double) elapsed / 1000.0);
            Point2D movementVector = new Point2D.Double(
                    distance * normal.getX(), distance * normal.getY());
            double newx = realLocation.getX() + movementVector.getX();
            double newy = realLocation.getY() + movementVector.getY();
            realLocation.setLocation(newx, newy);

            Point newLocation = new Point((int) Math.round(realLocation.getX()),
                    (int) Math.round(realLocation.getY()));

            // Detect that the counter has reached the destination.
            // If it's overshot, we consider it there.
            if (((normal.getX() > 0) && (newLocation.x > destination.x)) ||
                    ((normal.getX() < 0) && (newLocation.x < destination.x)) ||
                    ((normal.getY() > 0) && (newLocation.y > destination.y)) ||
                    ((normal.getY() < 0) && (newLocation.y < destination.y))) {
                newLocation.setLocation(destination);
            }

            Counter.this.setMapLocation(newLocation);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (borderColor != null) {
            g.setColor(borderColor);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }

    public void setLoop(boolean b) {
        icon.setLoop(b);
    }

    public void setAnimationTime(int time) {
        icon.setAnimationTime(time);
    }

    public void setCurrentFrame(int frame) {
        icon.setCurrentFrame(frame);
    }

    public void setOnScreen(boolean onScreen) {
        this.onScreen = onScreen;

        if (!onScreen) {
            setVisible(false);
        } else if (!hidden) {
            setVisible(true);
        }
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;

        if (hidden) {
            setVisible(false);
        } else if (onScreen) {
            setVisible(true);
        }
    }

    /**
     * Returns the map position of this Counter.
     */
    @Nullable
    public Point getMapPosition() {
        return mapPosition;
    }

    public void setMapPosition(@Nullable Point mapPosition) {
        this.mapPosition = mapPosition;
    }

    public void notifyWhenAnimationComplete(Object toNotify) {
        this.toNotify = toNotify;
    }

    private static final int ANIMATION_TIME = 100;

    /**
     * The movement speed of the Counter, in pixels per second.
     */
    private static final int MOVE_SPEED = 75;
    @Nullable
    private final Color borderColor;
    @Nullable
    private Point destination;

    /**
     * The location of this Counter on an imaginary component that contains the entire map.
     */
    private final Point mapLocation = new Point(0, 0);
    private final Timer moveTimer;

    /**
     * The normal vector of this SpriteSet's movement direction. Null iff destination is null.
     */
    @Nullable
    private Point2D normal;
    private final Point oldLocation;

    /**
     * This SpriteSet's location in double precision, used during movement calculations.
     */
    private Point2D realLocation = new Point2D.Double(0.0, 0.0);

    /**
     * Speed the Sprite should move, in pixels per second.
     */
    private int speed;
    private final AnimatedImageIcon icon;
    long lastTime = 0L;

    /**
     * This vector is added to the Counter's screen position when it is displayed on the screen.
     */
    private final Point offset = new Point(0, 0);
    private boolean onScreen = true;
    private boolean hidden = false;
    @Nullable
    private Point mapPosition;
    @Nullable
    private Object toNotify;
}
