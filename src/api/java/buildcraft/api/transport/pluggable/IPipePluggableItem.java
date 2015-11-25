package buildcraft.api.transport.pluggable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IPipe;

public interface IPipePluggableItem {
	PipePluggable createPipePluggable(IPipe pipe, ForgeDirection side, ItemStack stack);
}
