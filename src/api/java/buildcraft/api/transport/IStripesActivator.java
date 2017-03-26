/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.transport;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IStripesActivator {
    boolean sendItem(@Nonnull ItemStack itemStack, EnumFacing from);

    void dropItem(@Nonnull ItemStack itemStack, EnumFacing from);
}
