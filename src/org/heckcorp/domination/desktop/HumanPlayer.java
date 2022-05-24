package org.heckcorp.domination.desktop;

import java.awt.Color;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.heckcorp.domination.GameView;
import org.heckcorp.domination.Player;
import org.heckcorp.domination.ShadowMap;
import org.heckcorp.domination.Unit;

public class HumanPlayer extends Player {
    /**
     * @param name
     * @param color
     * @param shadowMap
     * @param view
     */
    public HumanPlayer(String name, Color color,
                       ShadowMap shadowMap, GameView view)
    {
        super(name, color, shadowMap, view);
        view.setMainPlayer(this);
    }

    private static final long serialVersionUID = 1L;

    @Override
    public void startTurn() {
        turnOver = false;
    }
    
    @Override
    public void move() throws InterruptedException {
        getLog().entering("HumanPlayer", "move()");
        
        while (getReadyUnit() == null && !turnOver) {
            try {
                assert moving == false;
                lock.lock();
                unitReady.await();
            } finally {
                lock.unlock();
            }
        }
        
        moving = true;
        getLog().finer("Moving ready unit: " + getReadyUnit());
        
        while (moving && !turnOver) {
            try {
                lock.lock();
                // The user moves units here while we wait.
                notMoving.await();
                assert moving == false;
            } finally {
                lock.unlock();
            }
        }

        assert getReadyUnit() == null;
        getLog().exiting("HumanPlayer", "move()");
    }
    
    private boolean moving = false;
    protected Lock lock = new ReentrantLock();
    protected Condition notMoving = lock.newCondition();
    protected Condition unitReady = lock.newCondition();
    protected Condition turnFinished = lock.newCondition();
    private boolean finishingTurn = false;
    private boolean turnOver = false;
    
    @Override
    public void setReadyUnit(Unit readyUnit) {
        super.setReadyUnit(readyUnit);
        try {
            lock.lock();
            unitReady.signal();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void unitActionFinished() {
        super.unitActionFinished();
        
        try {
            lock.lock();
            moving = false;
            notMoving.signal();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void finishTurn() {
        getLog().finer("Finishing turn. Turn over? " + turnOver);
        view.message("All units have moved.  Press <Enter> to end turn.");
        
        finishingTurn  = true;
        
        while (finishingTurn && !turnOver) {
            try {
                lock.lock();
                // The user moves units here while we wait.
                turnFinished.await();
                assert !finishingTurn;
            } catch (InterruptedException e) {
                // Stay in the while loop.
            } finally {
                lock.unlock();
            }
        }

        assert !finishingTurn || turnOver;
    }

    public void turnFinished() {
        try {
            lock.lock();
            turnOver = true;
            finishingTurn = false;
            turnFinished.signal();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Stops a thread waiting for the player to move, if there is
     * one, and ends the turn.
     *
     */
    public void interrupt() {
        log.fine("Interrupting human player.");
        
        try {
            lock.lock();
            turnOver = true;
            unitReady.signal();
            notMoving.signal();
        } finally {
            lock.unlock();
        }
    }
    
}
