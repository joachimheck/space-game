package org.heckcorp.spacegame.map.javafx;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

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
    counter.getChildren().add(new ImageView(image));
    counter.setMinWidth(image.getWidth());
    counter.setMinHeight(image.getHeight());
    counter.setMaxWidth(image.getWidth());
    counter.setMaxHeight(image.getHeight());
    counter.setWidth(image.getWidth());
    counter.setHeight(image.getHeight());
    return counter;
  }
}
