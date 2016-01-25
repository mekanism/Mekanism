/**
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.permissions;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * This interface is used to restrict where turtles can move or build
 * @see dan200.computercraft.api.ComputerCraftAPI#registerPermissionProvider(ITurtlePermissionProvider)
 */
public interface ITurtlePermissionProvider
{
    public boolean isBlockEnterable( World world, BlockPos pos );
    public boolean isBlockEditable( World world, BlockPos pos );
}
