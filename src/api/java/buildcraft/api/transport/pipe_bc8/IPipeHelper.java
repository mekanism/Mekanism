package buildcraft.api.transport.pipe_bc8;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableFluid;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableItem;

public interface IPipeHelper {
    IPipeContentsEditableItem getContentsForItem(ItemStack stack);

    IPipeContentsEditableFluid getContentsForFluid(FluidStack fluid);
}
