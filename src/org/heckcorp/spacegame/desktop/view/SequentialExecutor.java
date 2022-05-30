package org.heckcorp.spacegame.desktop.view;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

// TODO: simplify this - I don't think it needs to keep running when there are no tasks.
public final class SequentialExecutor implements Executor, Runnable {
    public synchronized void execute(Runnable task) {
        tasks.add(tasks.size(), task);
        notify();
    }

    public void run() {
        do {
            if (tasks.isEmpty()) {
                synchronized(this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // Do nothing.
                    }
                }
            }

            if (run && !tasks.isEmpty()) {
                Runnable task;

                synchronized(this) {
                    task = tasks.remove(0);
                }

                task.run();
            }

        } while (run);
    }

    /**
     * Instructs the executor to terminate after its current task completes.
     *
     */
    public synchronized void stop() {
        run = false;
        notify();
    }

    private SequentialExecutor() {
    }

    public static SequentialExecutor getInstance() {
        if (executor == null) {
            executor = new SequentialExecutor();
            new Thread(executor).start();
        }

        return executor;
    }

    /**
     * @uml.property  name="run"
     */
    private boolean run = true;
    private List<Runnable> tasks = new ArrayList<Runnable>();
    private static SequentialExecutor executor;
}
