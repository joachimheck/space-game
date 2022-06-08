package org.heckcorp.spacegame.map.javafx;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class HexDescriptionPane extends FlowPane {

  public void setText(String text) {
    this.text.setText(text);
  }

  public static HexDescriptionPane create() {
    Text text = new Text();
    HexDescriptionPane hexDescriptionPane = new HexDescriptionPane(text);
    hexDescriptionPane.setBackground(
        new Background(new BackgroundFill(Color.gray(.75), CornerRadii.EMPTY, Insets.EMPTY)));
    hexDescriptionPane.getChildren().add(text);
    return hexDescriptionPane;
  }

  public HexDescriptionPane(Text text) {
    this.text = text;
  }

  private final Text text;
}
