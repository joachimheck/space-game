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
    forwardButton.setDisable(false);
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
    forwardButton.setDisable(true);
    turnRightButton.setDisable(true);
  }

  private HexDescriptionPane initialize(Model model) {
    targetButton.setOnAction(event -> model.setSelectionMode(Model.SelectionMode.TARGET));
    attackButton.setOnAction(event -> model.processAttack());
    turnLeftButton.setOnAction(event -> model.rotateLeft());
    forwardButton.setOnAction(event -> model.moveForward());
    turnRightButton.setOnAction(event -> model.rotateRight());
    targetButton.setDisable(true);
    attackButton.setDisable(true);
    turnLeftButton.setDisable(true);
    forwardButton.setDisable(true);
    turnRightButton.setDisable(true);
    setBackground(
        new Background(new BackgroundFill(Color.gray(.75), CornerRadii.EMPTY, Insets.EMPTY)));
    getChildren().add(selectedUnitData);
    getChildren().add(targetUnitData);
    getChildren().add(targetButton);
    getChildren().add(attackButton);
    getChildren().add(turnLeftButton);
    getChildren().add(forwardButton);
    getChildren().add(turnRightButton);
    return this;
  }

  public static HexDescriptionPane create(Model model) throws FileNotFoundException {
    Text selectedUnitData = new Text();
    Text targetUnitData = new Text();
    Button targetButton = new Button("Target");
    Button attackButton = new Button("Attack!");
    Button turnLeftButton =
        new Button(
            "", new ImageView(new Image(ResourceLoader.getResource("resource/left-arrow.png"))));
    Button forwardButton = new Button("", new ImageView(new Image(ResourceLoader.getResource("resource/up-arrow.png"))));
    Button turnRightButton =
        new Button(
            "", new ImageView(new Image(ResourceLoader.getResource("resource/right-arrow.png"))));

    return new HexDescriptionPane(
            attackButton,
            targetButton,
            selectedUnitData,
            targetUnitData,
            turnLeftButton,
            forwardButton,
            turnRightButton)
        .initialize(model);
  }

  public HexDescriptionPane(
      Button attackButton,
      Button targetButton,
      Text selectedUnitData,
      Text targetUnitData,
      Button turnLeftButton,
      Button forwardButton,
      Button turnRightButton) {
    this.attackButton = attackButton;
    this.targetButton = targetButton;
    this.selectedUnitData = selectedUnitData;
    this.targetUnitData = targetUnitData;
    this.turnLeftButton = turnLeftButton;
    this.forwardButton = forwardButton;
    this.turnRightButton = turnRightButton;
  }


  private final Button attackButton;
  private final Button forwardButton;
  private final Text selectedUnitData;
  private final Button targetButton;
  private final Text targetUnitData;
  private final Button turnLeftButton;
  private final Button turnRightButton;
}
