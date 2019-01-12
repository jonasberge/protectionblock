package de.skyshard.plots;

import org.bukkit.util.Vector;

public enum Direction
{
    NORTH(0, -1), EAST(1, 0), SOUTH(0, 1), WEST(-1, 0);

    private final int xOffset;
    private final int zOffset;

    Direction(int xOffset, int zOffset)
    {
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }

    int getXOffset() { return xOffset; }
    int getZOffset() { return zOffset; }
    Vector getVector() { return new Vector(xOffset, 0, zOffset); }
}
