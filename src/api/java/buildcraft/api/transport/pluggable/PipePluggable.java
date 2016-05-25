/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.transport.pluggable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.INBTStoreable;
import buildcraft.api.core.ISerializable;
import buildcraft.api.transport.IPipeTile;

/** An IPipePluggable MUST have an empty constructor for client-side rendering! */
public abstract class PipePluggable implements INBTStoreable, ISerializable {
    public abstract ItemStack[] getDropItems(IPipeTile pipe);

    public void update(IPipeTile pipe, EnumFacing direction) {

    }

    public void onAttachedPipe(IPipeTile pipe, EnumFacing direction) {
        validate(pipe, direction);
    }

    public void onDetachedPipe(IPipeTile pipe, EnumFacing direction) {
        invalidate();
    }

    public abstract boolean isBlocking(IPipeTile pipe, EnumFacing direction);

    public void invalidate() {

    }

    public void validate(IPipeTile pipe, EnumFacing direction) {

    }

    public boolean isSolidOnSide(IPipeTile pipe, EnumFacing direction) {
        return false;
    }

    public abstract AxisAlignedBB getBoundingBox(EnumFacing side);

    @SideOnly(Side.CLIENT)
    public IPipePluggableDynamicRenderer getDynamicRenderer() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public abstract PluggableModelKey<?> getModelRenderKey(BlockRenderLayer layer, EnumFacing side);

    public boolean requiresRenderUpdate(PipePluggable old) {
        return true;
    }
}
