package org.heckcorp.spacegame.map.javafx;

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
  }

  public void setTargetUnitData(String targetUnitData) {
    this.targetUnitData.setText(targetUnitData);
  }

  public void showTargetButton(boolean visible) {
    targetButton.setVisible(visible);
  }

  public void clear() {
    setSelectedUnitData("");
    setTargetUnitData("");
    showTargetButton(false);
  }

  public static HexDescriptionPane create(Model model) {
    Text selectedUnitData = new Text();
    Text targetUnitData = new Text();
    Button targetButton = new Button("Target");
    targetButton.setVisible(false);
    HexDescriptionPane hexDescriptionPane =
        new HexDescriptionPane(targetButton, selectedUnitData, targetUnitData);
    hexDescriptionPane.setBackground(
        new Background(new BackgroundFill(Color.gray(.75), CornerRadii.EMPTY, Insets.EMPTY)));
    hexDescriptionPane.getChildren().add(selectedUnitData);
    hexDescriptionPane.getChildren().add(targetUnitData);

    targetButton.setOnAction(event -> model.setSelectionMode(Model.SelectionMode.TARGET));
    hexDescriptionPane.getChildren().add(targetButton);

    return hexDescriptionPane;
  }

  public HexDescriptionPane(Button targetButton, Text selectedUnitData, Text targetUnitData) {
    this.targetButton = targetButton;
    this.selectedUnitData = selectedUnitData;
    this.targetUnitData = targetUnitData;
  }

  private final Button targetButton;
  private final Text selectedUnitData;
  private final Text targetUnitData;
}
