package mekanism.common.util;

import net.minecraft.core.Direction;

public class EnumUtils {

    private EnumUtils() {
    }

    /// Cached value of [Direction#values()]. DO NOT MODIFY THIS LIST.
    public static final Direction[] DIRECTIONS = Direction.values();

    /// Cached value of the horizontal directions. DO NOT MODIFY THIS LIST.
    ///
    /// @implNote Index is ordinal() - 2, as the first two elements of [Direction] are [Direction#DOWN] and [Direction#UP]
    public static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
}