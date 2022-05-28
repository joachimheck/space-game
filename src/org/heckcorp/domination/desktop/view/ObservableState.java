package org.heckcorp.domination.desktop.view;

import java.util.Observable;

public final class ObservableState extends Observable {
    /**
     * @author   Joachim Heck
     */
    public enum State {
        FINISHED_MOVING, MOVING, FINISHED_ANIMATING, ANIMATING
    }

    @Override
    public synchronized void setChanged() {
        super.setChanged();
    }
}