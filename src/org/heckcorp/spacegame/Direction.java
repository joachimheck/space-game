package org.heckcorp.spacegame;

/**
 * @author  Joachim Heck
 */
public enum Direction {
    NORTH(0, "North"),
    NORTHEAST(1, "Northeast"),
    SOUTHEAST(2, "Southeast"),
    SOUTH(3, "South"),
    SOUTHWEST(4, "Southwest"),
    NORTHWEST(5, "Northwest");

    Direction(int value, String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * @uml.property  name="value"
     */
    public final int value;
    /**
     * @uml.property  name="name"
     */
    public final String name;
}
