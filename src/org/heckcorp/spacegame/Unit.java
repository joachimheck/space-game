package org.heckcorp.spacegame;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Stores all the game-level information about a unit.
 *
 * @author Joachim Heck
 *
 * @invariant getOwner() never returns null.
 *
 */
public class Unit extends GamePiece implements Serializable {

    /**
     * @author    Joachim Heck
     */
    public enum Health {
        DAMAGED, DESTROYED, HEALTHY
    }

    public enum Type {
        SPACESHIP(0, "SPACESHIP", 1, 1, 1, 8, 1);

        public String toString() {
            return name;
        }

        Type(int id, String name, int attack, int defense, int attacks, int movement, int cost) {
            this.id = id;
            this.name = name;
            this.attack = attack;
            this.defense = defense;
            this.attacks = attacks;
            this.movement = movement;
            this.cost = cost;
        }

        public final int attack;
        public final int attacks;
        public final int cost;
        public final int defense;
        public final int id;
        public final int movement;
        public final String name;
    }

    public Unit attack(Hex hex) {
        assert hex.getOwner() != null && hex.getOwner() != getOwner();

        Unit target = hex.getBestDefender();
        log.fine("Combat with " + target);

        double modifiedAttack =
            2.0 * getAttack()/3 +
            Math.random() * 2.0 * getAttack()/3;
        double modifiedDefense =
            2.0 * target.getDefense()/3 +
            Math.random() * 2.0 * target.getDefense()/3;

        log.fine("Attack: " + modifiedAttack + " -> " + modifiedDefense);

        Unit loser = target;

        if (modifiedAttack > modifiedDefense) {
            log.fine("Attack successful!");
        } else {
            log.fine("Attack failed!");
            loser = this;
        }

        loser.takeDamage();

        decreaseAttacksLeft(1);

        return loser;

    }

    /**
     * True if the specified hex is occupied by the enemy and this unit
     * can attack this turn.
     */
    public boolean canAttack(Hex hex) {
        return getHex().isAdjacentTo(hex) &&
            hex.getOwner() != null && hex.getOwner() != getOwner() &&
            !hex.getUnits().isEmpty() && getMovesLeft() > 0 && getAttacksLeft() > 0;
    }

    public boolean canEnterHex(Hex hex) {
        return hex.getUnits().isEmpty();
    }

    /**
     * @return true if the hex is empty.
     */
    public boolean canMoveAlongPath() {
        boolean result = false;

        if (!getPath().isEmpty()) {
            Hex nextHex = getPath().get(0);
            result = getMovesLeft() > 0 && canEnterHex(nextHex);
        }

        return result;
    }

    public void clearPath() {
        path.clear();
    }

    /**
     * @pre getAttacksLeft() >= amount
     */
    public void decreaseAttacksLeft(int amount) {
        assert attacksLeft >= amount;

        attacksLeft -= amount;
    }

    /**
     * @pre getMovesLeft() >= amount
     */
    public void decreaseMovesLeft(int amount) {
        assert movesLeft >= amount;

        movesLeft -= amount;
    }

    /**
     * @return the attack strength of this unit.
     */
    public int getAttack() {
        if (health == Health.DAMAGED) {
            return type.attack / 2;
        }

        return type.attack;
    }

    /**
     * @return the number of attacks this unit can execute in a turn.
     */
    public int getAttacks() {
        return type.attacks;
    }

    /**
     * @return  the attacksLeft
     * @uml.property  name="attacksLeft"
     */
    public int getAttacksLeft() {
        return attacksLeft;
    }

    /**
     * @return the defense strength of this unit.
     */
    public int getDefense() {
        if (health == Health.DAMAGED) {
            return type.defense / 2;
        }
        return type.defense;
    }

    /**
     * @return  the health
     * @uml.property  name="health"
     */
    public Health getHealth() {
        return health;
    }

    public Hex getLastHex() {
        return lastHex;
    }

    /**
     * @return the number of movement points this unit can expend in a turn.
     */
    public int getMovement() {
        return type.movement;
    }

    /**
     * @return  the movesLeft
     * @uml.property  name="movesLeft"
     */
    public int getMovesLeft() {
        return movesLeft;
    }

    /**
     * Returns the direction in which this unit will attempt to move
     * on its next movement.
     * @return the next direction, or null if the unit has no movement path.
     * @pre !getPath().isEmpty()
     */
    public Direction getNextDirection() {
        return HexMap.getDirection(getHex(), getPath().get(0));
    }

    /**
     * Returns the list of hexes this unit plans to visit.
     */
    @NotNull
    public List<Hex> getPath() {
        return path;
    }

    /**
     * Returns the furthest distance this unit can move and return
     * from this turn.
     */
    public int getRange() {
        return movesLeft;
    }

    public Unit.Type getType() {
        return type;
    }

    public boolean isAsleep() {
        return asleep;
    }

    public boolean isDamaged() {
        return health == Health.DAMAGED;
    }

    public boolean isDestroyed() {
        return health == Health.DESTROYED;
    }

    public boolean isReadyForAction() {
        return getHealth() != Health.DESTROYED &&
            getMovesLeft() > 0 && !isSkipped() && !isAsleep();
    }

    public boolean isSkipped() {
        return skipped ;
    }

    /**
     * Moves this unit one hex along its path.
     * @return true if the unit moved, false otherwise.
     */
    public boolean move() {
        Hex destHex = getPath().get(0);
        assert destHex.isAdjacentTo(getHex());

        if (getMovesLeft() > 0 && (destHex.getOwner() == getOwner() || destHex.isEmpty())) {
            lastHex = getHex();
            getHex().removeUnit(this);
            destHex.addUnit(this);
            hex = destHex;
            movesLeft--;
            getPath().remove(0);
        } else {
            log.finer("Unit couldn't move along path: movesLeft=" +
                      getMovesLeft() + " can enter? " +
                      (destHex.getOwner() != getOwner()) + " empty hex? " +
                      destHex.isEmpty());
        }

        return destHex.getUnits() != null && destHex.getUnits().contains(this);
    }

    /**
     * Resets a unit for the next turn.
     */
    public void reset() {
        setMovesLeft(getMovement());
        setAttacksLeft(getAttacks());
        skipped = false;
    }

    /**
     * @param asleep  the asleep to set
     * @uml.property  name="asleep"
     */
    public void setAsleep(boolean asleep) {
        this.asleep = asleep;
    }
    /**
     * @param attacksLeft  the attacksLeft to set
     * @uml.property  name="attacksLeft"
     */
    public void setAttacksLeft(int attacksLeft) {
        this.attacksLeft = attacksLeft;
    }
    /**
     * Sets the health of this unit.  If the health level is DESTROYED,
     * the unit removes itself from the hex.
     *
     * @param health the health to set
     * @uml.property  name="health"
     */
    public void setHealth(Health health) {
        this.health = health;

        if (this.health == Health.DESTROYED) {
            getHex().removeUnit(this);
            getOwner().removeUnit(this);
        }
    }
    @Override
    public void setHex(Hex hex) {
        super.setHex(hex);
        lastHex = hex;
    }
    /**
     * @param movementLeft the movesLeft to set
     * @uml.property  name="movesLeft"
     */
    public void setMovesLeft(int movementLeft) {
        this.movesLeft = movementLeft;
    }
    public void setPath(List<Hex> path) {
        this.path.clear();
        this.path.addAll(path);
    }
    public void skip() {
        log.fine("Skipping " + this);
        skipped = true;
    }
    public void takeDamage() {
        if (health == Health.HEALTHY) {
            setHealth(Health.DAMAGED);
        } else if (health == Health.DAMAGED) {
            setHealth(Health.DESTROYED);
        } else {
            assert false;
        }
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public int getStartingHitPoints() {
        return startingHitPoints;
    }

    public String toString() {
        return type + " (" + getOwner().getName() + ") at " + getHex().getPosition();
    }

    public Unit(Type type, Player player) {
        setOwner(player);
        this.type = type;

        this.path = new ArrayList<>();

        movesLeft = type.movement;
        attacksLeft = type.attacks;
        hitPoints = startingHitPoints;

        player.addUnit(this);
    }

    private boolean asleep = false;

    private int attacksLeft;

    private Health health = Health.HEALTHY;

    private final int startingHitPoints = 5;

    private int hitPoints;

    private transient Hex lastHex = null;

    private int movesLeft;

    private final List<Hex> path;

    private boolean skipped = false;

    private final Type type;

    private static final Logger log = Logger.getLogger(Unit.class.getName());

    private static final long serialVersionUID = 1L;

}