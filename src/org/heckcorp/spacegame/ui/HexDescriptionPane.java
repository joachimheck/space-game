package org.heckcorp.spacegame.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.heckcorp.spacegame.ResourceLoader;
import org.heckcorp.spacegame.model.Model;
import org.heckcorp.spacegame.model.Unit;
import org.heckcorp.spacegame.ui.map.ViewResources;

import java.io.FileNotFoundException;

public class HexDescriptionPane extends GridPane {

  public void setSelectedUnitData(Unit unit) {
    selectedUnitDescriptionPane.setUnitData(unit);
    boolean zeroEnergy = unit.getEnergy() == 0;
    targetButton.setDisable(zeroEnergy);
    attackButton.setDisable(true);
    turnLeftButton.setDisable(zeroEnergy);
    forwardButton.setDisable(zeroEnergy);
    turnRightButton.setDisable(zeroEnergy);
  }

  public void setTargetUnitData(Unit selectedUnit, Unit unit) {
    targetUnitDescriptionPane.setUnitData(unit);
    targetButton.setDisable(true);
    attackButton.setDisable(selectedUnit.getEnergy() == 0);
  }

  public void clear() {
    selectedUnitDescriptionPane.clear();
    targetUnitDescriptionPane.clear();
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
    add(selectedUnitDescriptionPane, 0, 0);
    add(new HBox(targetButton, attackButton), 0, 1);
    add(new HBox(turnLeftButton, forwardButton, turnRightButton), 0, 2);
    add(targetUnitDescriptionPane, 0, 3);
    return this;
  }

  public static HexDescriptionPane create(Model model, ViewResources viewResources)
      throws FileNotFoundException {
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
    UnitDescriptionPane selectedUnitDescriptionPane = new UnitDescriptionPane(viewResources);
    UnitDescriptionPane targetUnitDescriptionPane = new UnitDescriptionPane(viewResources);
    return new HexDescriptionPane(
            selectedUnitDescriptionPane,
            targetUnitDescriptionPane,
            attackButton,
            targetButton,
            turnLeftButton,
            forwardButton,
            turnRightButton)
        .initialize(model);
  }

  public HexDescriptionPane(
      UnitDescriptionPane selectedUnitDescriptionPane,
      UnitDescriptionPane targetUnitDescriptionPane,
      Button attackButton,
      Button targetButton,
      Button turnLeftButton,
      Button forwardButton,
      Button turnRightButton) {
    this.selectedUnitDescriptionPane = selectedUnitDescriptionPane;
    this.targetUnitDescriptionPane = targetUnitDescriptionPane;
    this.attackButton = attackButton;
    this.targetButton = targetButton;
    this.turnLeftButton = turnLeftButton;
    this.forwardButton = forwardButton;
    this.turnRightButton = turnRightButton;
  }

  private final UnitDescriptionPane selectedUnitDescriptionPane;
  private final UnitDescriptionPane targetUnitDescriptionPane;
  final Button attackButton;
  final Button forwardButton;
  private final Button targetButton;
  final Button turnLeftButton;
  final Button turnRightButton;
}
