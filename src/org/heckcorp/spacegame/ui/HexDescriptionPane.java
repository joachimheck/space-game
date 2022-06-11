package org.heckcorp.spacegame.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.heckcorp.spacegame.ResourceLoader;
import org.heckcorp.spacegame.model.Model;

import java.io.FileNotFoundException;

public class HexDescriptionPane extends FlowPane {

  public void setSelectedUnitData(String selectedUnitData) {
    this.selectedUnitData.setText(selectedUnitData);
    targetButton.setDisable(false);
    attackButton.setDisable(true);
    turnLeftButton.setDisable(false);
    turnRightButton.setDisable(false);
  }

  public void setTargetUnitData(String targetUnitData) {
    this.targetUnitData.setText(targetUnitData);
    targetButton.setDisable(true);
    attackButton.setDisable(false);
  }

  public void clear() {
    selectedUnitData.setText("");
    targetUnitData.setText("");
    targetButton.setDisable(true);
    attackButton.setDisable(true);
    turnLeftButton.setDisable(true);
    turnRightButton.setDisable(true);
  }

  public static HexDescriptionPane create(Model model) {
    Text selectedUnitData = new Text();
    Text targetUnitData = new Text();
    Button targetButton = new Button("Target");
    targetButton.setOnAction(event -> model.setSelectionMode(Model.SelectionMode.TARGET));
    Button attackButton = new Button("Attack!");
    attackButton.setOnAction(event -> model.processAttack());

    ImageView leftArrow = null;
    ImageView rightArrow = null;
    Button turnLeftButton;
    Button turnRightButton;
    try {
      leftArrow = new ImageView(new Image(ResourceLoader.getResource("resource/left-arrow.png")));
      rightArrow = new ImageView(new Image(ResourceLoader.getResource("resource/right-arrow.png")));
    } catch (FileNotFoundException ignored) {
    }

    if (leftArrow != null && rightArrow != null) {
      turnLeftButton = new Button("", leftArrow);
      turnRightButton = new Button("", rightArrow);
    } else {
      turnLeftButton = new Button("Turn Left");
      turnRightButton = new Button("Turn Right");
    }

    targetButton.setDisable(true);
    attackButton.setDisable(true);
    turnLeftButton.setDisable(true);
    turnRightButton.setDisable(true);
    HexDescriptionPane hexDescriptionPane =
        new HexDescriptionPane(attackButton, targetButton, selectedUnitData, targetUnitData, turnLeftButton, turnRightButton);
    hexDescriptionPane.setBackground(
        new Background(new BackgroundFill(Color.gray(.75), CornerRadii.EMPTY, Insets.EMPTY)));
    hexDescriptionPane.getChildren().add(selectedUnitData);
    hexDescriptionPane.getChildren().add(targetUnitData);
    hexDescriptionPane.getChildren().add(targetButton);
    hexDescriptionPane.getChildren().add(attackButton);
    hexDescriptionPane.getChildren().add(turnLeftButton);
    hexDescriptionPane.getChildren().add(turnRightButton);

    return hexDescriptionPane;
  }

  public HexDescriptionPane(
      Button attackButton, Button targetButton, Text selectedUnitData, Text targetUnitData, Button turnLeftButton, Button turnRightButton) {
    this.attackButton = attackButton;
    this.targetButton = targetButton;
    this.selectedUnitData = selectedUnitData;
    this.targetUnitData = targetUnitData;
    this.turnLeftButton = turnLeftButton;
    this.turnRightButton = turnRightButton;
  }

  private final Button attackButton;
  private final Text selectedUnitData;
  private final Button targetButton;
  private final Text targetUnitData;
  private final Button turnLeftButton;
  private final Button turnRightButton;
}
