package org.heckcorp.domination.desktop.view;

import org.heckcorp.domination.City;
import org.heckcorp.domination.Hex;
import org.heckcorp.domination.Player;
import org.heckcorp.domination.Unit;

import javax.swing.*;
import java.awt.*;
import java.util.TreeSet;

public class HexDescriptionPanel extends JPanel
{

    /**
     * @author Joachim Heck
     */
    public class UnitDescriptionPanel extends JPanel {

        public UnitDescriptionPanel(final Unit unit) {
            this.unit = unit;

            setBorder(BorderFactory.createEtchedBorder());

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(new JLabel(uiManager.getUnitIcon(unit)) {
                @Override
                protected void paintComponent(Graphics g)
                {
                    super.paintComponent(g);

                    g.setColor(unit.getOwner().getColor());
                    g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                    if (unit.isDamaged()) {
                        g.setColor(Color.black);
                        g.drawLine(getWidth(), 0, 0, getHeight());
                    }
                }
            });

            add(Box.createHorizontalGlue());

            JPanel dataPanel = new JPanel();
            dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
            dataPanel.add(new JLabel("A: " + unit.getAttack() + "/"
                    + unit.getType().attack));
            dataPanel.add(new JLabel("D: " + unit.getDefense() + "/"
                    + unit.getType().defense));
            dataPanel.add(new JLabel("M: " + unit.getMovesLeft() + "/"
                    + unit.getMovement()));

            add(dataPanel);

            add(Box.createHorizontalGlue());
        }

        public final Unit unit;
    }

    /**
     * @uml.property name="locationLabel"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private final JLabel locationLabel = new JLabel();

    /**
     * @uml.property name="ownerLabel"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private final JLabel ownerLabel = new JLabel();

    /**
     * @uml.property name="productionLabel"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private final JLabel productionLabel = new JLabel();

    /**
     * @uml.property name="uiManager"
     * @uml.associationEnd multiplicity="(1 1)"
     *                     inverse="hexDescriptionPanel:org.heckcorp.domination.desktop.Domination"
     */
    private final SwingView.UIManager uiManager;

    private final Box unitsBox;

    public HexDescriptionPanel(SwingView.UIManager uiManager) {
        this.uiManager = uiManager;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());

        Box labelBox = new Box(SwingConstants.HORIZONTAL);
        Box leftLabelBox = new Box(SwingConstants.VERTICAL);
        leftLabelBox.add(locationLabel);
        leftLabelBox.add(ownerLabel);
        leftLabelBox.add(productionLabel);
        labelBox.add(leftLabelBox);
        labelBox.add(Box.createHorizontalGlue());

        unitsBox = new Box(SwingConstants.VERTICAL);

        add(labelBox);
        add(unitsBox);

        // Push the units to the top and don't let them stretch.
        add(Box.createVerticalGlue());
    }

    public void setHex(Hex hex) {
        unitsBox.removeAll();

        City city = hex.getCity();
        String hexType = "Hex";

        if (city != null) {
            hexType = "City";
        } else if (hex.terrain == Hex.Terrain.LAND
                || hex.terrain == Hex.Terrain.WATER) {
            hexType = hex.terrain.name;
        } else {
            assert false : "Unknown terrain type: " + hex.terrain;
        }
        Player owner = hex.getOwner();

        if (owner != null) {
            ownerLabel.setText(owner.getName());

            if (city == null) {
                productionLabel.setVisible(false);
            } else {
                productionLabel.setVisible(true);
                productionLabel.setText(city.getProductionType().name
                        + ": " + city.getProductionPoints()
                        + "/"
                        + city.getProductionType().cost);
            }

            TreeSet<Unit> sortedUnits = new TreeSet<>(
                    (o1, o2) -> {
                        int idDiff = o2.getType().ordinal() - o1.getType().ordinal();
                        return idDiff == 0 ? o2.hashCode() - o1.hashCode() : idDiff;
                    });

            sortedUnits.addAll(hex.getUnits());

            for (Unit unit : sortedUnits) {
                unitsBox.add(new UnitDescriptionPanel(unit));
            }
        } else {
            ownerLabel.setText("unoccupied");
            productionLabel.setVisible(false);
        }

        locationLabel.setText(hexType + " " + hex.getPosition().x + "," + hex.getPosition().y);

        // Make sure Swing knows about the new components, and draws them.
        revalidate();
        repaint();
    }
}
