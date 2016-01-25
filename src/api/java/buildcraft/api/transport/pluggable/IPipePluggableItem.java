package buildcraft.api.transport.pluggable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IPipe;

public interface IPipePluggableItem {
    PipePluggable createPipePluggable(IPipe pipe, EnumFacing side, ItemStack stack);
}
