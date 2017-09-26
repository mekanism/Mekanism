/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.permissions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * This interface is used to restrict where turtles can move or build.
 *
 * Turtles will call these methods before attempting to perform an action, allowing them to be cancelled.
 *
 * @see dan200.computercraft.api.ComputerCraftAPI#registerPermissionProvider(ITurtlePermissionProvider)
 */
public interface ITurtlePermissionProvider
{
    /**
     * Determine whether a block can be entered by a turtle.
     *
     * @param world The world the block exists in
     * @param pos   The location of the block.
     * @return Whether the turtle can move into this block.
     */
    boolean isBlockEnterable( @Nonnull World world, @Nonnull BlockPos pos );

    /**
     * Determine whether a block can be modified by a turtle.
     *
     * This includes breaking and placing blocks.
     *
     * @param world The world the block exists in
     * @param pos   The location of the block.
     * @return Whether the turtle can modify this block.
     */
    boolean isBlockEditable( @Nonnull World world, @Nonnull BlockPos pos );
}
