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
        moveAndAttack(unit, target);
      }
      model.endTurn();
    }
  }

  private void moveAndAttack(Unit unit, Unit target) {
    MapPosition targetPosition = model.unitPositionsProperty().get(target);
    while (unit.getEnergy() > 0 && model.winnerProperty().get() == null) {
      MapPosition unitPosition = model.unitPositionsProperty().get(unit);
      if (canAttack(unitPosition, targetPosition)) {
        model.setSelectionMode(Model.SelectionMode.TARGET);
        model.hexClicked(targetPosition.position(), MouseButton.PRIMARY);
        model.processAttack();
      } else if (!isFacing(unitPosition, targetPosition)) {
        rotateOnceTowardTarget(model.unitPositionsProperty().get(unit), targetPosition);
      } else if (mapUtils.distance(unitPosition.position(), targetPosition.position()) > 1) {
        model.moveForward();
      }
    }
  }

  private boolean canAttack(MapPosition unitPosition, MapPosition targetPosition) {
    return mapUtils.getTargetHexes(unitPosition).contains(targetPosition.position());
  }

  private boolean isFacing(MapPosition unitPosition, MapPosition targetPosition) {
    return getHeadingDifference(unitPosition, targetPosition) == 0;
  }

  private void rotateOnceTowardTarget(MapPosition unitPosition, MapPosition targetPosition) {
    int headingDifference = getHeadingDifference(unitPosition, targetPosition);
    if (headingDifference == 0) {
      return;
    }
    if (Math.abs(headingDifference) <= 3) {
      if (headingDifference > 0) {
        model.rotateRight();
      } else {
        model.rotateLeft();
      }
    } else {
      if (headingDifference > 0) {
        model.rotateLeft();
      } else {
        model.rotateRight();
      }
    }
  }

  private int getHeadingDifference(MapPosition unitPosition, MapPosition targetPosition) {
    Point2D unitHexCenter = mapUtils.getHexCenter(unitPosition.position());
    Point2D targetHexCenter = mapUtils.getHexCenter(targetPosition.position());
    double x = targetHexCenter.getX() - unitHexCenter.getX();
    double y = unitHexCenter.getY() - targetHexCenter.getY();
    double angle = 90 + 30 - Math.toDegrees(Math.atan2(y, x));
    int hexDirection = (int) angle / 60;
    int currentDirection = unitPosition.direction().getDirection();
    return (hexDirection - currentDirection) % 6;
  }

  public AIPlayer(Model model, MapUtils mapUtils) {
    this.model = model;
    this.mapUtils = mapUtils;
  }

  private final MapUtils mapUtils;
  private final Model model;
}
