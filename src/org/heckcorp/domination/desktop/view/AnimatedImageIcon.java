package org.heckcorp.domination.desktop.view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AnimatedImageIcon extends ImageIcon {

    public AnimatedImageIcon(BufferedImage[] images) {
        super(images[0]);

        this.images = images;
        timer = new Timer(1000, e -> step());
        timer.setInitialDelay(0);
    }

    public void setAnimated(boolean animated) {
        if (animated) {
            timer.start();
        } else {
            timer.stop();
        }
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setAnimationTime(int time) {
        timer.setDelay(time);
    }

    public void step() {
        currentFrame = currentFrame + 1;

        if (currentFrame >= getFrameCount()) {
            if (loop) {
                currentFrame = 0;
            } else {
                currentFrame = getFrameCount() - 1;
                timer.stop();
            }
        }

        setImageByFrame();
    }

    private void setImageByFrame() {
        setImage(images[currentFrame]);
        component.repaint(x, y, images[0].getWidth(), images[0].getHeight());
    }

    public int getFrameCount() {
        return images.length;
    }

    public void setCurrentFrame(int frame) {
        currentFrame = frame;
        setImageByFrame();
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        super.paintIcon(component, graphics, x, y);
        this.component = component;
        this.x = x;
        this.y = y;
    }

    private final BufferedImage[] images;
    private int currentFrame = 0;
    boolean loop = false;
    private final Timer timer;
    private Component component;
    private int x;
    private int y;
}
