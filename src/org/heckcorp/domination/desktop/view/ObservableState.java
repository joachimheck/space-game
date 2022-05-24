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
/**
 * 
 */
package org.heckcorp.domination.desktop.view;

import java.util.Observable;

public final class ObservableState extends Observable {
    /**
     * @author   Joachim Heck
     */
    public enum State {
        FINISHED_MOVING, MOVING, FINISHED_ANIMATING, ANIMATING;
    }

    @Override
    public synchronized void setChanged() {
        super.setChanged();
    }
}