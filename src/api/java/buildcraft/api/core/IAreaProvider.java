/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.core;

import net.minecraft.util.BlockPos;

/** To be implemented by TileEntities able to provide a square area on the world, typically BuildCraft markers. */
public interface IAreaProvider {
    BlockPos min();

    BlockPos max();

    /** Remove from the world all objects used to define the area. */
    void removeFromWorld();
}
