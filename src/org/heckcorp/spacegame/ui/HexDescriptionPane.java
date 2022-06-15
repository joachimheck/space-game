package org.heckcorp.spacegame.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.heckcorp.spacegame.ResourceLoader;
import org.heckcorp.spacegame.model.Model;
import org.heckcorp.spacegame.model.Unit;

import java.io.FileNotFoundException;

public class HexDescriptionPane extends FlowPane {

  public void setSelectedUnitData(Unit unit) {
    String unitDescription = getUnitDescription(unit);
    this.selectedUnitData.setText(unitDescription);
    boolean zeroEnergy = unit.getEnergy() == 0;
    targetButton.setDisable(zeroEnergy);
    attackButton.setDisable(true);
    turnLeftButton.setDisable(zeroEnergy);
    forwardButton.setDisable(zeroEnergy);
    turnRightButton.setDisable(zeroEnergy);
  }

  public void setTargetUnitData(Unit selectedUnit, Unit unit) {
    String unitDescription = getUnitDescription(unit);
    this.targetUnitData.setText(unitDescription);
    targetButton.setDisable(true);
    attackButton.setDisable(selectedUnit.getEnergy() == 0);
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

  String getUnitDescription(@NonNull Unit unit) {
    return String.format(
            "%s's unit: health %d/%d energy %d/%d",
            unit.getOwner().getName(),
            unit.getHealth(),
            unit.getMaxHealth(),
            unit.getEnergy(),
            unit.getMaxEnergy());
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
    HBox attackRow = new HBox(targetButton, attackButton);
    getChildren().add(attackRow);
    HBox controlsRow = new HBox(turnLeftButton, forwardButton, turnRightButton);
    getChildren().add(controlsRow);
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
    Button forwardButton =
        new Button(
            "", new ImageView(new Image(ResourceLoader.getResource("resource/up-arrow.png"))));
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
