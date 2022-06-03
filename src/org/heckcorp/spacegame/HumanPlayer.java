package org.heckcorp.spacegame;

import java.awt.*;
import java.io.Serial;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HumanPlayer extends Player {
    public HumanPlayer(String name, Color color, GameView view) {
        super(name, color, view);
    }

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public void startTurn() {
        turnOver = false;
    }

    @Override
    public void move(GameModel model) throws InterruptedException {
        getLog().entering("HumanPlayer", "move()");

        while (getReadyUnit() == null && !turnOver) {
            try {
                assert !moving;
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
                assert !moving;
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
}
