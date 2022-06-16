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
import org.heckcorp.spacegame.model.Player;
import org.heckcorp.spacegame.model.Unit;
import org.heckcorp.spacegame.ui.map.Counter;
import org.heckcorp.spacegame.ui.map.ViewResources;

import java.io.FileNotFoundException;

public class HexDescriptionPane extends GridPane {

  public void setSelectedUnitHealth(Unit unit) {
    selectedUnitEnergy.setText("Energy: " + unit.getEnergy() + "/" + unit.getMaxEnergy());
    selectedUnitHealth.setText("Health: " + unit.getHealth() + "/" + unit.getMaxHealth());
    selectedUnitCounterHolder.getChildren().clear();
    selectedUnitCounterHolder.getChildren().add(getCounter(unit));
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
    targetUnitCounterHolder.getChildren().clear();
    targetUnitCounterHolder.getChildren().add(getCounter(unit));
    targetButton.setDisable(true);
    attackButton.setDisable(selectedUnit.getEnergy() == 0);
  }

  public void clear() {
    selectedUnitEnergy.setText("");
    selectedUnitHealth.setText("");
    selectedUnitCounterHolder.getChildren().clear();
    targetUnitData.setText("");
    targetUnitCounterHolder.getChildren().clear();
    targetButton.setDisable(true);
    attackButton.setDisable(true);
    turnLeftButton.setDisable(true);
    forwardButton.setDisable(true);
    turnRightButton.setDisable(true);
  }

  private Counter getCounter(Unit unit) {
    Player.Color color = unit.getOwner().getColor();
    return Counter.build(viewResources, unit.getImageId(), color.r(), color.g(), color.b());
  }

  private String getUnitDescription(@NonNull Unit unit) {
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
    add(new HBox(selectedUnitCounterHolder), 0, 0);
    add(new HBox(selectedUnitHealth), 0, 1);
    add(new HBox(selectedUnitEnergy), 0, 2);
    add(new HBox(targetButton, attackButton), 0, 3);
    add(new HBox(turnLeftButton, forwardButton, turnRightButton), 0, 4);
    add(new HBox(targetUnitCounterHolder), 0, 5);
    add(new HBox(targetUnitData), 0, 6);
    return this;
  }

  public static HexDescriptionPane create(Model model, ViewResources viewResources)
      throws FileNotFoundException {
    Text selectedUnitEnergy = new Text();
    Text selectedUnitHealth = new Text();
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
            viewResources,
            attackButton,
            targetButton,
            selectedUnitEnergy,
            selectedUnitHealth,
            targetUnitData,
            turnLeftButton,
            forwardButton,
            turnRightButton)
        .initialize(model);
  }

  public HexDescriptionPane(
      ViewResources viewResources,
      Button attackButton,
      Button targetButton,
      Text selectedUnitEnergy,
      Text selectedUnitHealth,
      Text targetUnitData,
      Button turnLeftButton,
      Button forwardButton,
      Button turnRightButton) {
    this.viewResources = viewResources;
    this.attackButton = attackButton;
    this.targetButton = targetButton;
    this.selectedUnitEnergy = selectedUnitEnergy;
    this.selectedUnitHealth = selectedUnitHealth;
    this.targetUnitData = targetUnitData;
    this.turnLeftButton = turnLeftButton;
    this.forwardButton = forwardButton;
    this.turnRightButton = turnRightButton;
    selectedUnitCounterHolder = new FlowPane();
    targetUnitCounterHolder = new FlowPane();
  }

  private final Button attackButton;
  private final Button forwardButton;
  private final Text selectedUnitEnergy;
  private final Text selectedUnitHealth;
  private final Button targetButton;
  private final FlowPane targetUnitCounterHolder;
  private final Text targetUnitData;
  private final Button turnLeftButton;
  private final Button turnRightButton;
  private final FlowPane selectedUnitCounterHolder;
  private final ViewResources viewResources;
}
