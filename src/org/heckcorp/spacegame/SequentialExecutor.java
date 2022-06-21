package org.heckcorp.spacegame;

import javafx.animation.Animation;
import javafx.application.Platform;

import java.util.concurrent.*;

public class SequentialExecutor {
  public void runLaterSequentially(Runnable task) {
    submit(() -> Platform.runLater(task));
  }

  @SuppressWarnings("UnusedReturnValue")
  public Future<?> submit(Runnable task) {
    return executor.submit(task);
  }

  public SequentialExecutor() {
    this.executor = Executors.newSingleThreadExecutor();
  }

  private final ExecutorService executor;

  public void playSequentially(Animation animation) {
    submit(
        () -> {
          CompletableFuture<Void> future = new CompletableFuture<>();
          animation.setOnFinished(e -> future.completeAsync(() -> null));
          animation.play();
          try {
            future.get();
          } catch (InterruptedException | ExecutionException e) {
            // TODO: handle.
            throw new RuntimeException(e);
          }
        });
  }
}
