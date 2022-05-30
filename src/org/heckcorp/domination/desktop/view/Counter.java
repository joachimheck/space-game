package org.heckcorp.domination.desktop.view;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Observer;


public class Counter extends JLabel {
    public Counter(BufferedImage[] unitPix, Color borderColor, Point hexCenter, Point mapPosition) {
        this.mapPosition = mapPosition;

        state = new ObservableState();
        icon = new AnimatedImageIcon(unitPix, state);

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

    public void deleteObserver(Observer o) {
        state.deleteObserver(o);
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

    public void addObserver(Observer observer) {
        state.addObserver(observer);
    }

    public void setCenterLocation(Point location) {
        setMapLocation(new Point(location.x - (getWidth() / 2),
                location.y - (getHeight() / 2)));
    }

    /**
     * @pre  damagedSprite != null
     * @uml.property  name="damaged"
     */
    public void setDamaged(boolean damaged) {
        this.damaged = damaged;
        repaint();
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
            // Otherwise we set destination and end up having multiple timers
            // running simultaneously, speeding up movement with every hex.
            moveTimer.stop();

            state.setChanged();
            state.notifyObservers(ObservableState.State.FINISHED_MOVING);
        } else if (destination != null) {
            state.setChanged();
            state.notifyObservers(ObservableState.State.MOVING);
        }
    }

    /**
     * @param speed  the speed to set
     * @uml.property  name="speed"
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    private void updateCounterLocation() {
        if (destination != null) {
            // Distance = speed * time
            long current = System.currentTimeMillis();
            long elapsed = current - lastTime;
            lastTime = current;

            // TODO: this seems to be smoother when I just pretend the timer
            // runs regularly - shouldn't that make it rougher?
            //            double distance = speed * ((double) moveTimer.getDelay() / 1000.0);
            double distance = speed * ((double) elapsed / 1000.0);
            Point2D movementVector = new Point2D.Double(distance * normal.getX(),
                    distance * normal.getY());
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

        if (damaged) {
            g.setColor(Color.black);
            g.drawLine(getWidth(), 0, 0, getHeight());
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

    /**
     * @param borderColor  the borderColor to set
     * @uml.property  name="borderColor"
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @param onScreen  the onScreen to set
     * @uml.property  name="onScreen"
     */
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

    /**
     * @param hidden  the hidden to set
     * @uml.property  name="hidden"
     */
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
     * @post result != null
     */
    public Point getMapPosition() {
        return mapPosition;
    }

    public void setMapPosition(@Nullable Point mapPosition) {
        this.mapPosition = mapPosition;
    }


    private static final int ANIMATION_TIME = 100;

    /**
     * The movement speed of the Counter, in pixels per second.
     */
    private static final int MOVE_SPEED = 75;
    /**
     * @uml.property  name="borderColor"
     * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.awt.Color" qualifier="key:org.heckcorp.domination.game.Unit$Type [Ljava.awt.image.BufferedImage;"
     */
    private Color borderColor;
    /**
     * @uml.property  name="destination"
     */
    private Point destination;

    /**
     * The location of this Counter on an imaginary component that contains the entire map.
     * @uml.property  name="mapLocation"
     * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.awt.Color" qualifier="key:org.heckcorp.domination.game.Unit$Type [Ljava.awt.image.BufferedImage;"
     */
    private final Point mapLocation = new Point(0, 0);

    /**
     * @uml.property  name="moveTimer"
     * @uml.associationEnd  multiplicity="(1 1)"
     */
    private final Timer moveTimer;

    /**
     * The normal vector of this SpriteSet's movement direction. Null iff destination is null.
     * @uml.property  name="normal"
     */
    private Point2D normal;
    /**
     * @uml.property  name="oldLocation"
     * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.awt.Color" qualifier="key:org.heckcorp.domination.game.Unit$Type [Ljava.awt.image.BufferedImage;"
     */
    private final Point oldLocation;

    /**
     * This SpriteSet's location in double precision, used during movement calculations.
     * @uml.property  name="realLocation"
     */
    private Point2D realLocation;

    /**
     * Speed the Sprite should move, in pixels per second.
     * @uml.property  name="speed"
     */
    private int speed;
    /**
     * @uml.property  name="icon"
     * @uml.associationEnd  multiplicity="(1 1)"
     */
    private final AnimatedImageIcon icon;
    /**
     * @uml.property  name="damaged"
     */
    private boolean damaged = false;
    /**
     * @uml.property  name="state"
     * @uml.associationEnd  multiplicity="(1 1)"
     */
    private final ObservableState state;
    /**
     * @uml.property  name="lastTime"
     */
    long lastTime = 0L;

    /**
     * This vector is added to the Counter's screen position when it is displayed on the screen.
     * @uml.property  name="offset"
     * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.awt.Color" qualifier="key:org.heckcorp.domination.game.Unit$Type [Ljava.awt.image.BufferedImage;"
     */
    private final Point offset = new Point(0, 0);
    /**
     * @uml.property  name="onScreen"
     */
    private boolean onScreen = true;
    /**
     * @uml.property  name="hidden"
     */
    private boolean hidden = false;

    private Point mapPosition;
}
