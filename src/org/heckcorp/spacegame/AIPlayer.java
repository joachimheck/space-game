package org.heckcorp.spacegame;

import javafx.geometry.Point2D;
import org.heckcorp.spacegame.model.MapPosition;
import org.heckcorp.spacegame.model.Model;
import org.heckcorp.spacegame.model.Player;
import org.heckcorp.spacegame.model.Unit;
import org.heckcorp.spacegame.ui.map.MapUtils;
import org.heckcorp.spacegame.ui.map.MouseButton;

import java.util.Optional;

public class AIPlayer {
  public void setCurrentPlayer(Player player) {
    if (player.getType().equals(Player.Type.COMPUTER)) {
      Optional<Unit> optionalUnit =
          model.unitsProperty().stream().filter(u -> u.getOwner().equals(player)).findFirst();
      Optional<Unit> optionalTarget =
          model.unitsProperty().stream().filter(u -> !u.getOwner().equals(player)).findFirst();
      if (optionalUnit.isPresent() && optionalTarget.isPresent()) {
        Unit unit = optionalUnit.get();
        Unit target = optionalTarget.get();
        MapPosition unitPosition = model.unitPositionsProperty().get(unit);
        model.hexClicked(unitPosition.position(), MouseButton.PRIMARY);
        if (unit.getEnergy() > 0) {
          faceTarget(unit, target);
        }
      }
      model.endTurn();
    }
  }

  private void faceTarget(Unit unit, Unit target) {
    MapPosition targetPosition = model.unitPositionsProperty().get(target);
    boolean rotated;
    if (unit.getEnergy() > 0) {
      do {
        MapPosition unitPosition = model.unitPositionsProperty().get(unit);
        rotated = rotateOnceTowardTarget(unitPosition, targetPosition);
      } while (unit.getEnergy() > 0 && rotated);
    }
  }

  private boolean rotateOnceTowardTarget(MapPosition unitPosition, MapPosition targetPosition) {
    Point2D unitHexCenter = mapUtils.getHexCenter(unitPosition.position());
    Point2D targetHexCenter = mapUtils.getHexCenter(targetPosition.position());
    double x = targetHexCenter.getX() - unitHexCenter.getX();
    double y = unitHexCenter.getY() - targetHexCenter.getY();
    double angle = 90 + 30 - Math.toDegrees(Math.atan2(y, x));
    int hexDirection = (int) angle / 60;
    int currentDirection = unitPosition.direction().getDirection();
    int difference = (hexDirection - currentDirection) % 6;
    if (difference == 0) {
      return false;
    }
    if (Math.abs(difference) <= 3) {
      if (difference > 0) {
        model.rotateRight();
      } else {
        model.rotateLeft();
      }
    } else {
      if (difference > 0) {
        model.rotateLeft();
      } else {
        model.rotateRight();
      }
    }
    return true;
  }

  public AIPlayer(Model model, MapUtils mapUtils) {
    this.model = model;
    this.mapUtils = mapUtils;
  }

  private final MapUtils mapUtils;
  private final Model model;
}
