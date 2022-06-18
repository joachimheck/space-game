package org.heckcorp.spacegame;

import org.heckcorp.spacegame.model.MapPosition;
import org.heckcorp.spacegame.model.Model;
import org.heckcorp.spacegame.model.Player;
import org.heckcorp.spacegame.model.Unit;
import org.heckcorp.spacegame.ui.map.MouseButton;

import java.util.Optional;

public class AIPlayer {
  public void setCurrentPlayer(Player player) {
    if (player.getType().equals(Player.Type.COMPUTER)) {
      Optional<Unit> optionalUnit =
          model.unitsProperty().stream().filter(u -> u.getOwner().equals(player)).findFirst();
      if (optionalUnit.isPresent()) {
        Unit unit = optionalUnit.get();
        MapPosition unitPosition = model.unitPositionsProperty().get(unit);
        model.hexClicked(unitPosition.position(), MouseButton.PRIMARY);
        model.rotateLeft();
      }
      model.endTurn();
    }
  }

  public AIPlayer(Model model) {
    this.model = model;
  }

  private final Model model;
}
