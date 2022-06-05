package org.heckcorp.spacegame.swing;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.heckcorp.spacegame.Constants;
import org.heckcorp.spacegame.Direction;
import org.heckcorp.spacegame.GameView;
import org.heckcorp.spacegame.Unit;
import org.heckcorp.spacegame.UnitStatus;
import org.heckcorp.spacegame.map.Hex;
import org.heckcorp.spacegame.map.HexMap;
import org.heckcorp.spacegame.map.Point;
import org.heckcorp.spacegame.map.swing.Counter;
import org.heckcorp.spacegame.map.swing.MapPane;
import org.heckcorp.spacegame.map.swing.MapView;
import org.heckcorp.spacegame.map.swing.MiniMap;
import org.heckcorp.spacegame.map.swing.UIResources;
import org.heckcorp.spacegame.map.swing.ViewMonitor;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.logging.Logger;

public class SwingView extends JPanel implements GameView
{
    /**
     * Manages the Swing components of this SwingView.
     *
     * @author Joachim Heck
     */
    public class UIManager extends JPanel {
        public MapView getMapView() {
            return mapView;
        }

        public ImageIcon getUnitIcon(Unit unit) {
            return dataManager.getCounter(unit).getIcon();
        }

        public void message(String message) {
            textArea.insert(message + "\n", textArea.getText().length());
            JScrollBar sb = textScrollPane.getVerticalScrollBar();
            sb.setValue(sb.getMaximum());
        }

        /**
         * Creates all the sprites that will be used to display a unit of
         * the specified type, and sets their positions.
         *
         * @param unit the Unit to create a counter for.
         * @return the counter for the unit.
         */
        private Counter createCounter(Unit unit) {
            java.awt.Point position = new java.awt.Point(0, 0);

            if (mapView.isInitialized() && unit.getPosition() != null) {
                position = mapView.getMapPane().getHexCenter(unit.getPosition());
            }

            return new Counter(UIResources.getInstance().getSpaceshipImages(),
                    unit.getOwner().getColor(), position, unit.getPosition());
        }

        public UIManager() {
            mapView = new MapView();

            hexDescriptionPanel = new HexDescriptionPanel(this);
            textArea = new JTextArea();
            textArea.setRows(5);
            textArea.setEditable(false);
            textArea.setFocusable(false);
            textScrollPane = new JScrollPane(textArea);
            textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            assert getMap() != null;
            miniMap = new MiniMap(getMap());
            miniMap.setMinimumSize(new Dimension(Constants.UI_COMPONENT_SMALL_WIDTH,
                    Constants.UI_COMPONENT_SMALL_HEIGHT));
            mapView.addMapViewListener(miniMap);

            SpringLayout layout = new SpringLayout();
            setLayout(layout);

            add(mapView);
            add(hexDescriptionPanel);
            add(textScrollPane);
            add(miniMap);

            int smWidth = Constants.UI_COMPONENT_SMALL_WIDTH;
            int smHeight = Constants.UI_COMPONENT_SMALL_HEIGHT;
            int lgWidth = Constants.UI_COMPONENT_LARGE_WIDTH;
            int lgHeight = Constants.UI_COMPONENT_LARGE_HEIGHT;

            mapView.setPreferredSize(new Dimension(lgWidth, lgHeight));
            mapView.setMinimumSize(new Dimension(smWidth, smHeight));
            mapView.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            hexDescriptionPanel.setPreferredSize(new Dimension(smWidth, lgHeight));
            hexDescriptionPanel.setMaximumSize(new Dimension(smWidth, Integer.MAX_VALUE));
            textScrollPane.setPreferredSize(new Dimension(lgWidth, smHeight));
            textScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, smHeight));
            miniMap.setPreferredSize(new Dimension(smWidth, smHeight));
            miniMap.setMaximumSize(new Dimension(smWidth, smHeight));
            miniMap.setBorder(BorderFactory.createLineBorder(Color.black));

            SpringLayout.Constraints panelCons = layout.getConstraints(this);
            SpringLayout.Constraints mapViewCons = layout.getConstraints(mapView);
            SpringLayout.Constraints hexCons = layout.getConstraints(hexDescriptionPanel);
            SpringLayout.Constraints textCons = layout.getConstraints(textScrollPane);
            SpringLayout.Constraints miniMapCons = layout.getConstraints(miniMap);

            mapViewCons.setX(Spring.constant(5));
            mapViewCons.setY(Spring.constant(5));

            hexCons.setX(Spring.sum(Spring.constant(5), layout.getConstraint(SpringLayout.EAST, mapView)));
            hexCons.setY(Spring.constant(5));
            hexCons.setWidth(Spring.constant(smWidth));
            hexCons.setHeight(mapViewCons.getHeight());

            textCons.setX(Spring.constant(5));
            textCons.setY(Spring.sum(layout.getConstraint(SpringLayout.SOUTH, mapView), Spring.constant(5)));
            textCons.setWidth(mapViewCons.getWidth());
            textCons.setHeight(Spring.constant(smHeight));

            miniMapCons.setX(hexCons.getX());
            miniMapCons.setY(textCons.getY());
            miniMapCons.setWidth(hexCons.getWidth());
            miniMapCons.setHeight(textCons.getHeight());

            panelCons.setWidth(Spring.sum(layout.getConstraint(SpringLayout.EAST, hexDescriptionPanel),
                    Spring.constant(5)));
            panelCons.setHeight(Spring.sum(layout.getConstraint(SpringLayout.SOUTH, textScrollPane),
                    Spring.constant(5)));
        }

        private final HexDescriptionPanel hexDescriptionPanel;

        private final MapView mapView;

        private final MiniMap miniMap;

        private final JTextArea textArea;

        private final JScrollPane textScrollPane;

        public MiniMap getMiniMap() {
            return miniMap;
        }

        public void initialize(HexMap map, ViewMonitor monitor) {
            mapView.initialize(map, monitor);
            miniMap.initialize(map);
        }

        public void clearTextArea() {
            textArea.setText(null);
        }

    }

    /**
     * Manages the display of game actions such as unit movement.
     *
     * @author Joachim Heck
     */
    private class DisplayManager {
        public void hideAttackArrow() {
            invokeAndWait(() -> resources.getAttackArrow().setHidden(true));
        }

        public void hideSelection() {
            invokeAndWait(() -> resources.getSelection().setHidden(true));
        }

        /**
         * Moves the counter to the specified hex.
         */
        public void moveCounter(final Counter counter, final Hex destHex) {
            MapPane mapPane = mapView.getMapPane();
            java.awt.Point position = mapPane.getHexCenter(destHex.getPosition());
            counter.notifyWhenAnimationComplete(this);
            counter.moveCenterTo(position);

            try {
                // The counter will inform us when it's finished moving.
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
                // Ignore.
            }

            counter.setMapPosition(destHex.getPosition());
            resources.getSelection().setMapPosition(destHex.getPosition());
            resources.getSelection().setHidden(counter.isHidden());

            miniMap.invalidate();
        }

        public void moveToFront(final Counter counter) {
            invokeAndWait(() -> mapView.moveToFront(counter));
        }

        public void setCounterStatus(final Counter counter, final UnitStatus status) {
            if (status == UnitStatus.DESTROYED) {
                Counter explosion = UIResources.getInstance().getExplosion();

                explosion.setCenterLocation(counter.getCenterLocation());
                explosion.setCurrentFrame(0);
                explosion.setAnimated(true);
                explosion.setHidden(false);

                try {
                    // The sprite will inform us when it's finished animating.
                    synchronized(this) {
                        wait();
                    }

                    explosion.setHidden(true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // Ignore.
                }

                miniMap.invalidate();
            } else if (status == UnitStatus.DAMAGED) {
                pause(Constants.PAUSE_TIME);
            } else {
                invokeAndWait(() -> {
                    // Status.DAMAGED - dealt with earlier in the method.
                    // Status.DESTROYED - dealt with earlier in the method.
                    // Status.SKIPPED - do nothing.
                    if (status == UnitStatus.HIDDEN) {
                        counter.setHidden(true);
                        miniMap.invalidate();
                    } else if (status == UnitStatus.REVEALED) {
                        counter.setHidden(false);
                        miniMap.invalidate();
                    } else if (status == UnitStatus.SELECTED) {
                        @Nullable Point mapPosition = counter.getMapPosition();
                        if (!counter.isHidden() && mapPosition != null) {
                            java.awt.Point point = new java.awt.Point(mapPosition.x() - 1, mapPosition.y() - 1);
                            Rectangle rect = new Rectangle(point, new Dimension(2, 2));
                            mapView.centerRectangle(rect);
                            mapView.moveToFront(counter);
                        }

                        Counter selection = UIResources.getInstance().getSelection();
                        assert selection.getMapPosition() == null;
                        selection.setHidden(counter.isHidden());
                        selection.setOnScreen(!counter.isHidden());
                        selection.setAnimated(!counter.isHidden());
                        selection.setMapPosition(counter.getMapPosition());
                        selection.setCenterLocation(counter.getCenterLocation());
                    } else if (status == UnitStatus.UNSELECTED) {
                        Counter selection = UIResources.getInstance().getSelection();

                        assert selection.getMapPosition() != null;
                        selection.setMapPosition(null);
                        selection.setHidden(true);
                        selection.setAnimated(false);
                    } else {
                        assert false : "Unknown unit status!";
                    }
                });
            }
        }

        /**
         * Sets the visibility status flags on all counters to ensure that all
         * counters in visible portions of the map are visible and all others
         * are hidden.
         */
        public void setCounterVisibility() {
            invokeAndWait(miniMap::invalidate);
        }

        public void showAttackArrow(final Hex hexFrom, final Hex hexTo) {
            invokeAndWait(() -> {
                final Direction d = HexMap.getDirection(hexFrom.getPosition(),
                        hexTo.getPosition());
                java.awt.Point pFrom = mapView.getMapPane().getHexCenter(hexFrom.getPosition());
                java.awt.Point pTo = mapView.getMapPane().getHexCenter(hexTo.getPosition());
                final java.awt.Point center = new java.awt.Point(pFrom.x + (pTo.x - pFrom.x)/2,
                        pFrom.y + (pTo.y - pFrom.y)/2);

                Counter attackArrow = UIResources.getInstance().getAttackArrow();
                attackArrow.setCurrentFrame(d.ordinal());
                attackArrow.setCenterLocation(center);
                attackArrow.setHidden(false);
            });
        }

        public void showCounter(final Counter counter) {
            invokeAndWait(() -> {
                counter.setHidden(false);
                counter.revalidate();
                counter.repaint();
                miniMap.invalidate();
            });
        }

        public void invalidateMapViews() {
            invokeAndWait(() -> {
                mapView.invalidate();
                miniMap.invalidate();
            });
        }

        /**
         * Invokes the run() method of runnable on the AWT event dispatching thread,
         * and swallows any runtime exceptions that are generated.
         */
        private void invokeAndWait(Runnable runnable) {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException e) {
                e.printStackTrace();
                // Ignore - what else can I do?
            } catch (InvocationTargetException e) {
                // TODO: throw a new RuntimeException here maybe?
                e.printStackTrace();
                assert false;
            }
        }

        public DisplayManager(MapView mapView, MiniMap miniMap) {
            this.mapView = mapView;
            this.miniMap = miniMap;
        }

        private final MapView mapView;
        private final MiniMap miniMap;

        public void pause(final int millis) {
            invokeAndWait(() -> {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // Ignore
                }
            });
        }

        /**
         * @pre first and second are adjacent
         */
        public void displayPositions(Hex first, Hex second) {
            // Make sure all the hexes surrounding the start point and all
            // those surrounding the end point are visible.
            // TODO: make map non-null.
            assert map != null;
            Set<Hex> adjacent = map.getAdjacentHexes(first);
            adjacent.addAll(map.getAdjacentHexes(second));

            int minx = Integer.MAX_VALUE;
            int miny = Integer.MAX_VALUE;
            int maxx = Integer.MIN_VALUE;
            int maxy = Integer.MIN_VALUE;

            for (Hex hex : adjacent) {
                if (hex.getPosition().x() < minx) {
                    minx = hex.getPosition().x();
                }
                if (hex.getPosition().y() < miny) {
                    miny = hex.getPosition().y();
                }
                if (hex.getPosition().x() > maxx) {
                    maxx = hex.getPosition().x();
                }
                if (hex.getPosition().y() > maxy) {
                    maxy = hex.getPosition().y();
                }
            }

            mapView.centerRectangle(new Rectangle(minx, miny, maxx, maxy));
        }
    }

    /**
     * Manages the non-graphical information for this SwingView, in particular
     * the mapping between Units and Counters.
     */
    private static class ViewDataManager {
        public void addUnit(Unit unit, Counter counter) {
            unitsToCounters.put(unit, counter);
        }

        public Counter getCounter(Unit unit) {
            return unitsToCounters.get(unit);
        }

        BiMap<Unit, Counter> unitsToCounters = HashBiMap.create();
    }

    public void addUnit(Unit unit) {
        log.finest("Adding unit: " + unit);
        Counter counter = uiManager.createCounter(unit);
        dataManager.addUnit(unit, counter);
        uiManager.getMapView().add(counter, MapView.SPRITE_LAYER);
        revalidate();
    }

    /**
     * Initiates an attack.
     *
     * @pre unit != null
     * @pre hex != null
     * @pre unit has been added to this view, and not destroyed
     * @pre unit.canAttack(hex)
     */
    public void attack(final Unit attacker, final Unit target) {
        final Counter targetCounter = dataManager.getCounter(target);

        assert attacker.getHex() != null;
        assert target.getHex() != null;
        displayManager.displayPositions(attacker.getHex(), target.getHex());
        displayManager.moveToFront(targetCounter);
        displayManager.showAttackArrow(attacker.getHex(),
                target.getHex());
        displayManager.pause(Constants.PAUSE_TIME);
        displayManager.hideAttackArrow();
    }

    // TODO: move out of swing view, or at least add another one
    // to uiManager or something, for debugging messages from
    // the view components.
    public void message(String message) {
        uiManager.message(message);
    }

    /**
     * Moves the unit one hex. This method assumes the unit's position has been
     * updated already, so it uses the unit's last hex as the starting point for
     * the move.
     *
     * @pre unit != null
     * @pre direction != null
     * @pre unit has been added to this view, and not destroyed
     */
    public void move(final Unit unit, final Direction direction) {
        assert unit.getLastHex() != null;
        assert map != null;
        Hex destHex = map.getAdjacentHex(unit.getLastHex(), direction);
        displayManager.hideSelection();

        Counter counter = dataManager.getCounter(unit);
        displayManager.showCounter(counter);

        assert unit.getHex() != null;
        displayManager.displayPositions(unit.getLastHex(), unit.getHex());
        displayManager.moveCounter(counter, unit.getHex());
        displayManager.invalidateMapViews();
        displayManager.setCounterVisibility();
        uiManager.hexDescriptionPanel.setHex(destHex);
    }

    /**
     * Selects the specified hex. If a unit or another hex is currently
     * selected, it is first unselected.
     *
     * @pre hex != null
     */
    public void selectHex(Hex hex) {
        uiManager.hexDescriptionPanel.setHex(hex);
    }

    /**
     * Sets this view's monitor.  The view will update the
     * controller whenever the viewport size or position changes.
     * @pre monitor != null
     */
    public void setMonitor(ViewMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Sets this view's current player to the specified player.
     */
    public void setCurrentPlayer(String playerName) {
        uiManager.clearTextArea();
        message("Now moving: " + playerName);
        // TODO: redraw current player label.
    }

    /**
     * Sets the map to be displayed in this view.
     *
     * @pre map != null
     * @pre this view's map is null.
     */
    public void setMap(HexMap map) {
        log.finer("Setting map: " + map);
        this.map = map;
    }

    @Override
    public void initialize() {
        assert map != null;
        assert monitor != null;
        // TODO: make monitor non-null?
        uiManager.initialize(map, monitor);
    }

    /**
     * Sets a status attribute of the unit.
     *
     * @pre unit != null
     * @pre unit has been added to this view, and not destroyed.
     */
    public void setStatus(Unit unit, UnitStatus status) {
        Counter counter = dataManager.getCounter(unit);
        displayManager.setCounterStatus(counter, status);

        if (status == UnitStatus.DESTROYED) {
            uiManager.getMapView().removeCounter(counter);
        }
    }

    public void setWinningPlayer(String playerName, Color playerColor) {
        BufferedImage tempImage = new BufferedImage(500, 50,
                BufferedImage.TRANSLUCENT);
        Graphics2D g = (Graphics2D) tempImage.getGraphics();

        String winnerString = playerName + " wins!";
        g.setFont(g.getFont().deriveFont(36.0f));
        g.setColor(playerColor);
        Rectangle2D bounds = g.getFont().getStringBounds(winnerString,
                g.getFontRenderContext());
        g.drawString(winnerString, (int) -bounds.getX(), (int) -bounds.getY());

        BufferedImage winnerImage = new BufferedImage((int) bounds.getWidth(),
                (int) bounds.getHeight(),
                BufferedImage.TRANSLUCENT);
        Graphics g2 = winnerImage.getGraphics();
//        g2.fillRect(0, 0, winnerImage.getWidth(), winnerImage.getHeight());
        g2.drawImage(tempImage, 0, 0, winnerImage.getWidth(),
                winnerImage.getWidth(), 0, 0, winnerImage.getWidth(),
                winnerImage.getWidth(), this);
        JLabel winnerLabel = new JLabel(new ImageIcon(winnerImage));

        getRootPane().setGlassPane(winnerLabel);
        winnerLabel.setVisible(true);

//        repaint();
    }

    @Nullable
    private HexMap getMap() {
        return map;
    }

    public SwingView() {
        // TODO: add these in as some kind of plug-ins, maybe?
        uiManager = new UIManager();
        dataManager = new ViewDataManager();
        displayManager = new DisplayManager(uiManager.getMapView(),
                uiManager.getMiniMap()
        );

        setLayout(new BorderLayout());
        add(uiManager);

        // When loading a game, the selection may have a position.
        Counter selection = UIResources.getInstance().getSelection();
        selection.setMapPosition(null);
        selection.setHidden(true);
        selection.setAnimated(false);

        log = Logger.getLogger(getClass().getName());
    }

    private final Logger log;
    @Nullable
    private ViewMonitor monitor;

    private final ViewDataManager dataManager;

    private final DisplayManager displayManager;

    @Nullable
    private HexMap map;

    private final UIResources resources = UIResources.getInstance();

    private final UIManager uiManager;
}
