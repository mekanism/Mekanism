/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.transport.pluggable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.INBTStoreable;
import buildcraft.api.core.ISerializable;
import buildcraft.api.transport.IPipeTile;

/**
 * An IPipePluggable MUST have an empty constructor for client-side
 * rendering!
 */
public abstract class PipePluggable implements INBTStoreable, ISerializable {
	public abstract ItemStack[] getDropItems(IPipeTile pipe);

	public void update(IPipeTile pipe, ForgeDirection direction) {

	}

	public void onAttachedPipe(IPipeTile pipe, ForgeDirection direction) {
		validate(pipe, direction);
	}

	public void onDetachedPipe(IPipeTile pipe, ForgeDirection direction) {
		invalidate();
	}

	public abstract boolean isBlocking(IPipeTile pipe, ForgeDirection direction);

	public void invalidate() {

	}

	public void validate(IPipeTile pipe, ForgeDirection direction) {

	}

	public boolean isSolidOnSide(IPipeTile pipe, ForgeDirection direction) {
		return false;
	}

	public abstract AxisAlignedBB getBoundingBox(ForgeDirection side);

	@SideOnly(Side.CLIENT)
	public abstract IPipePluggableRenderer getRenderer();

	@SideOnly(Side.CLIENT)
	public IPipePluggableDynamicRenderer getDynamicRenderer() {
		return null;
	}

	public boolean requiresRenderUpdate(PipePluggable old) {
		return true;
	}
}
