package org.heckcorp.spacegame.ui;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.heckcorp.spacegame.model.Player;
import org.heckcorp.spacegame.model.Unit;
import org.heckcorp.spacegame.ui.map.Counter;
import org.heckcorp.spacegame.ui.map.ViewResources;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UnitDescriptionPane extends GridPane {
  public void clear() {
    getChildren().clear();
  }

  public void setUnitData(Unit unit) {
    add(getCounter(unit), 0, 0);
    add(new Text("Health: "), 0, 1);
    HBox unitHealth = new HBox();
    for (int i = 0; i < unit.getMaxHealth(); i++) {
      Rectangle rectangle = new Rectangle(20, 20);
      if (i < unit.getHealth()) {
        rectangle.setFill(Color.GREEN);
      }
      unitHealth.getChildren().add(rectangle);
    }
    add(unitHealth, 1, 1);
    add(new Text("Energy: "), 0, 2);
    HBox unitEnergy = new HBox();
    for (int i = 0; i < unit.getMaxEnergy(); i++) {
      Rectangle rectangle = new Rectangle(20, 20);
      if (i < unit.getEnergy()) {
        rectangle.setFill(Color.LIGHTBLUE);
      }
      unitEnergy.getChildren().add(rectangle);
    }
    add(unitEnergy, 1, 2);
    String armorDescription =
        IntStream.range(0, 6)
            .mapToObj(n -> unit.getArmor()[n] + "/" + unit.getMaxArmor()[n])
            .collect(Collectors.joining(" "));
    add(new Text(armorDescription), 0, 3, 2, 1);
  }

  private Counter getCounter(Unit unit) {
    Player.Color color = unit.getOwner().getColor();
    return Counter.build(viewResources, unit.getImageId(), color.r(), color.g(), color.b());
  }

  public UnitDescriptionPane(ViewResources viewResources) {
    this.viewResources = viewResources;
  }

  private final ViewResources viewResources;
}
