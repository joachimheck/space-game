package org.heckcorp.spacegame.desktop.view;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class UIResources {
    /**
     * @post tilePic != null
     * @post selectionPix != null && selectionPix.length > 0
     * @post explosionSprite != null
     * @post selectionSprite != null
     */
    private UIResources() throws IOException {
        // Map images.
        tilePic = Util.getImages("resource/hex-large-light.png", 1, 1);
        selectedHexPix = Util.getImages("resource/selection-hex.png", 4, 1);
        spaceshipPic = Util.getImages("resource/spaceship.png", 1, 1);

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
     * @pre piece != null
     * @post result != null
     */
    public BufferedImage[] getSpaceshipImages() {
        return spaceshipPic;
    }

    public static void initializeResources() throws IOException {
        if (instance == null) {
            instance = new UIResources();
        }
    }

    public Counter getSelection() {
        return selection;
    }
    public Counter getAttackArrow() {
        return attackArrow;
    }
    public Counter getExplosion() {
        return explosion;
    }

    public final BufferedImage[] tilePic;
    public final BufferedImage[] spaceshipPic;
    public final BufferedImage[] selectedHexPix;
    private static UIResources instance = null;
    private final Counter selection;
    private final Counter explosion;
    private final Counter attackArrow;
}
