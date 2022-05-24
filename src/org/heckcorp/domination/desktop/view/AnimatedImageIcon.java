/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.heckcorp.domination.desktop.view;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Observer;
import javax.swing.ImageIcon;
import javax.swing.Timer;

import org.heckcorp.domination.desktop.view.ObservableState.State;

@SuppressWarnings("serial")
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
    
    public AnimatedImageIcon(BufferedImage[] images, ObservableState state) {
        super(images[0]);

        this.images = images;
        this.state = state;
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                step();
            }
        });
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

                state.setChanged();
                state.notifyObservers(State.FINISHED_ANIMATING);
            }
        }
        
        setImageByFrame();
    }

    private void setImageByFrame() {
        setImage(images[currentFrame]);
        state.setChanged();
        state.notifyObservers(State.ANIMATING);
    }

    /**
     * @return  the currentFrame
     * @uml.property  name="currentFrame"
     */
    public int getCurrentFrame() {
        return currentFrame;
    }
    
    public int getFrameCount() {
        return images.length;
    }

    /**
     * @param currentFrame  the currentFrame to set
     * @uml.property  name="currentFrame"
     */
    public void setCurrentFrame(int frame) {
        currentFrame = frame;
        setImageByFrame();
    }
    
    /**
     * @uml.property  name="state"
     * @uml.associationEnd  multiplicity="(1 1)"
     */
    private final ObservableState state;

    public void addObserver(Observer observer) {
        state.addObserver(observer);
    }
}
