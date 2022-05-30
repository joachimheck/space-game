package org.heckcorp.domination.desktop.view;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AnimatedImageIcon extends ImageIcon {
    /**
     * @uml.property  name="images" multiplicity="(0 -1)" dimension="1"
     */
    private final BufferedImage[] images;
    /**
     * @uml.property  name="currentFrame"
     */
    private int currentFrame = 0;
    /**
     * @uml.property  name="loop"
     */
    boolean loop = false;
    /**
     * @uml.property  name="timer"
     * @uml.associationEnd  multiplicity="(1 1)"
     */
    private final Timer timer;

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

    /**
     * @param loop  the loop to set
     * @uml.property  name="loop"
     */
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
                setAnimationState(AnimationState.FINISHED_ANIMATING);
            }
        }

        setImageByFrame();
    }

    private void setImageByFrame() {
        setImage(images[currentFrame]);
        setAnimationState(AnimationState.ANIMATING);
    }

    public int getFrameCount() {
        return images.length;
    }

    /**
     * @param frame  the currentFrame to set
     * @uml.property  name="currentFrame"
     */
    public void setCurrentFrame(int frame) {
        currentFrame = frame;
        setImageByFrame();
    }

    private void setAnimationState(AnimationState animationState) {
        AnimationState oldValue = this.animationState;
        this.animationState = animationState;
        this.propertyChangeSupport.firePropertyChange("AnimationState", oldValue, this.animationState);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private AnimationState animationState;
}
