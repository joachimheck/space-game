package org.heckcorp.spacegame.ui.map;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import static org.heckcorp.spacegame.Constants.COUNTER_SIZE;

public class Counter extends Pane {
  private Counter() {}

  public static Counter build(
      ViewResources viewResources,
      ViewResources.Identifier identifier,
      double r,
      double g,
      double b) {
    Counter counter = new Counter();
    Color backgroundColor = new Color(r, g, b, 1.0);
    counter.setBackground(
        new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
    Image image = viewResources.getImages().get(identifier);
    assert image != null : "@AssumeAssertion(nullness)";
    ImageView imageView = new ImageView(image);
    imageView.setFitHeight(COUNTER_SIZE);
    imageView.setFitWidth(COUNTER_SIZE);
    counter.getChildren().add(imageView);
    counter.setWidth(COUNTER_SIZE);
    counter.setHeight(COUNTER_SIZE);
    return counter;
  }
}
