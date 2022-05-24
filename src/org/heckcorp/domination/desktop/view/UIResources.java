package org.heckcorp.domination.desktop.view;


import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.heckcorp.domination.City;
import org.heckcorp.domination.GamePiece;
import org.heckcorp.domination.Unit;

public class UIResources {
    /**
     * 
     * @throws IOException
     * 
     * @post tilePix != null && tilePix.length > 0
     * @post unitPixByType != null && unitPixByType.size() > 0
     * @post unitBorderPix != null && unitBorderPix.length > 0
     * @post selectionPix != null && selectionPix.length > 0
     * @post explosionSprite != null
     * @post selectionSprite != null
     * @post damagedPic != null
     */
    private UIResources() throws IOException {
        // Map images.
        tilePix = Util.getImages("resource/hexes.png", 2, 1);
        selectedHexPix = Util.getImages("resource/selection-hex.png", 4, 1);
        cityPic = Util.getImage("resource/city.png");
        lightFog = Util.getImage("resource/hex-fog-light.png");
        darkFog = Util.getImage("resource/hex-fog-dark.png");

        // Unit images.
        unitPixByType = new HashMap<Unit.Type, BufferedImage[]>();
        unitPixByType.put(Unit.Type.SOLDIER,
                          Util.getImages("resource/soldier.png", 4, 1));
        unitPixByType.put(Unit.Type.TANK,
                          Util.getImages("resource/tank.png", 4, 1));
        unitPixByType.put(Unit.Type.BOMBER,
                          Util.getImages("resource/bomber.png", 1, 1));

        // Effects.
        explosion = new Counter(Util.getImages("resource/explosion.png", 3, 1),
                                null, new Point(0, 0), null);
        explosion.setHidden(true);
        explosion.setAnimated(false);
        explosion.setLoop(false);
        explosion.setAnimationTime(600);
        
        selection = new Counter(Util.getImages("resource/selection.png", 4, 1),
                                null, new Point(0, 0), null);
        selection.setHidden(true);
        selection.setAnimated(false);
        selection.setLoop(true);
        selection.setAnimationTime(300);
        
        attackArrow = new Counter(Util.getImages("resource/attack-arrow.png", 6, 1),
                                  null, new Point(0, 0), null);
        attackArrow.setHidden(true);
        attackArrow.setAnimated(false);
        attackArrow.setLoop(false);
        attackArrow.setAnimationTime(100);

    }

    /**
     * @uml.property  name="cityPic"
     */
    public final BufferedImage cityPic;

    /**
     * @uml.property  name="unitPixByType"
     * @uml.associationEnd  qualifier="BOMBER:org.heckcorp.domination.game.Unit$Type [Ljava.awt.image.BufferedImage;"
     */
    public final Map<Unit.Type, BufferedImage[]> unitPixByType;
    /**
     * @uml.property  name="lightFog"
     */
    public final BufferedImage lightFog;
    /**
     * @uml.property  name="darkFog"
     */
    public final BufferedImage darkFog;
    /**
     * @uml.property  name="tilePix" multiplicity="(0 -1)" dimension="1"
     */
    public final BufferedImage[] tilePix;
    /**
     * @uml.property  name="selectedHexPix" multiplicity="(0 -1)" dimension="1"
     */
    public final BufferedImage[] selectedHexPix;

    private static UIResources instance = null;

    public static void initializeResources() throws IOException {
        if (instance == null) {
            instance = new UIResources();
        }
    }

    /**
     * @return
     * @pre initializeResources() must be called first.
     * @post result != null
     */
    public static UIResources getInstance() {
        assert instance != null : "UIResources.initializeResources " +
            "must be called before getInstance()!";
        return instance;
    }

    /**
     * Returns an array of images representing the specified
     * game piece.
     * @param piece
     * @return
     * @pre piece != null
     * @post result != null
     */
    public BufferedImage[] getPictures(GamePiece piece) {
        BufferedImage[] result = null;
        
        if (piece instanceof Unit) {
            result = unitPixByType.get(((Unit)piece).getType());
        } else if (piece instanceof City) {
            result = new BufferedImage[] { cityPic };
        } else {
            assert false;
        }
        
        return result;
    }

    public Counter getSelection() {
        return selection;
    }
    
    private final Counter selection;
    private final Counter explosion;
    private final Counter attackArrow;

    public Counter getAttackArrow() {
        return attackArrow;
    }

    public Counter getExplosion() {
        return explosion;
    }
    
}
