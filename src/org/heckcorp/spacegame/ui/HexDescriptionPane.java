package org.heckcorp.spacegame.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.heckcorp.spacegame.model.Model;

public class HexDescriptionPane extends FlowPane {

  public void setSelectedUnitData(String selectedUnitData) {
    this.selectedUnitData.setText(selectedUnitData);
    targetButton.setVisible(true);
    attackButton.setVisible(false);
  }

  public void setTargetUnitData(String targetUnitData) {
    this.targetUnitData.setText(targetUnitData);
    targetButton.setVisible(false);
    attackButton.setVisible(true);
  }

  public void clear() {
    selectedUnitData.setText("");
    targetUnitData.setText("");
    targetButton.setVisible(false);
    attackButton.setVisible(false);
  }

  public static HexDescriptionPane create(Model model) {
    Text selectedUnitData = new Text();
    Text targetUnitData = new Text();
    Button targetButton = new Button("Target");
    targetButton.setOnAction(event -> model.setSelectionMode(Model.SelectionMode.TARGET));
    Button attackButton = new Button("Attack!");
    attackButton.setOnAction(event -> model.processAttack());

    targetButton.setVisible(false);
    attackButton.setVisible(false);
    HexDescriptionPane hexDescriptionPane =
        new HexDescriptionPane(attackButton, targetButton, selectedUnitData, targetUnitData);
    hexDescriptionPane.setBackground(
        new Background(new BackgroundFill(Color.gray(.75), CornerRadii.EMPTY, Insets.EMPTY)));
    hexDescriptionPane.getChildren().add(selectedUnitData);
    hexDescriptionPane.getChildren().add(targetUnitData);
    hexDescriptionPane.getChildren().add(targetButton);
    hexDescriptionPane.getChildren().add(attackButton);

    return hexDescriptionPane;
  }

  public HexDescriptionPane(Button attackButton, Button targetButton, Text selectedUnitData, Text targetUnitData) {
    this.attackButton = attackButton;
    this.targetButton = targetButton;
    this.selectedUnitData = selectedUnitData;
    this.targetUnitData = targetUnitData;
  }

  private final Button attackButton;
  private final Text selectedUnitData;
  private final Button targetButton;
  private final Text targetUnitData;
}
