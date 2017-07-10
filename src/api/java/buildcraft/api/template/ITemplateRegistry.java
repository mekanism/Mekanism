/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution.
 */

package buildcraft.api.template;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.EnumHandlerPriority;

public interface ITemplateRegistry {
    /** Adds a handler with a {@link EnumHandlerPriority} of {@linkplain EnumHandlerPriority#NORMAL} */
    default void addHandler(ITemplateHandler handler) {
        addHandler(handler, EnumHandlerPriority.NORMAL);
    }

    void addHandler(ITemplateHandler handler, EnumHandlerPriority priority);

    boolean handle(World world, BlockPos pos, EntityPlayer player, ItemStack stack);
}
