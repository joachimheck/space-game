package org.heckcorp.spacegame.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.heckcorp.spacegame.ResourceLoader;
import org.heckcorp.spacegame.model.Model;
import org.heckcorp.spacegame.model.Player;
import org.heckcorp.spacegame.model.Unit;
import org.heckcorp.spacegame.ui.map.MapUtils;
import org.heckcorp.spacegame.ui.map.ViewResources;

import java.io.FileNotFoundException;

public class DescriptionPane extends GridPane {
  final Text currentPlayerText;

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
  private final UnitDescriptionPane selectedUnitDescriptionPane;
  private final UnitDescriptionPane targetUnitDescriptionPane;

  public DescriptionPane(
      Text currentPlayerText,
      UnitDescriptionPane selectedUnitDescriptionPane,
      UnitDescriptionPane targetUnitDescriptionPane,
      Button attackButton,
      Button targetButton,
      Button turnLeftButton,
      Button forwardButton,
      Button turnRightButton) {
    this.currentPlayerText = currentPlayerText;
    this.selectedUnitDescriptionPane = selectedUnitDescriptionPane;
    this.targetUnitDescriptionPane = targetUnitDescriptionPane;
    this.attackButton = attackButton;
    this.targetButton = targetButton;
    this.turnLeftButton = turnLeftButton;
    this.forwardButton = forwardButton;
    this.turnRightButton = turnRightButton;
  }

  private static Button createButton(String filename) throws FileNotFoundException {
    return new Button("", new ImageView(new Image(ResourceLoader.getResource(filename))));
  }

  private static String getCurrentPlayerText(Player player) {
    return "Current player: " + player.getName();
  }

  final Button attackButton;

  public static DescriptionPane create(Model model, MapUtils mapUtils, ViewResources viewResources)
      throws FileNotFoundException {
    Button targetButton = new Button("Target");
    Button attackButton = new Button("Attack!");
    Button turnLeftButton = createButton("resource/left-arrow.png");
    Button forwardButton = createButton("resource/up-arrow.png");
    Button turnRightButton = createButton("resource/right-arrow.png");
    UnitDescriptionPane selectedUnitDescriptionPane =
        new UnitDescriptionPane(mapUtils, viewResources);
    UnitDescriptionPane targetUnitDescriptionPane =
        new UnitDescriptionPane(mapUtils, viewResources);
    return new DescriptionPane(
            new Text(getCurrentPlayerText(model.currentPlayerProperty().get())),
            selectedUnitDescriptionPane,
            targetUnitDescriptionPane,
            attackButton,
            targetButton,
            turnLeftButton,
            forwardButton,
            turnRightButton)
        .initialize(model);
  }
  final Button forwardButton;

  public void setCurrentPlayer(Player player) {
    currentPlayerText.setText(getCurrentPlayerText(player));
  }
  private final Button targetButton;

  private DescriptionPane initialize(Model model) {
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
    add(currentPlayerText, 0, 0);
    add(selectedUnitDescriptionPane, 0, 1);
    add(new HBox(targetButton, attackButton), 0, 2);
    add(new HBox(turnLeftButton, forwardButton, turnRightButton), 0, 3);
    add(targetUnitDescriptionPane, 0, 4);
    return this;
  }
  final Button turnLeftButton;
  final Button turnRightButton;
}
