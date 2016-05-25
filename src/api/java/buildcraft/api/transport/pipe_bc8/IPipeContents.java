package buildcraft.api.transport.pipe_bc8;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/** Provides a simple abstraction for items and fluids. This is intended to be useful for pipes which cover both kinds,
 * without having lots of <code>if (item) ... else ... </code> */
public interface IPipeContents {
    EnumContentsJourneyPart getJourneyPart();

    EnumFacing getDirection();

    /** This should NEVER return numbers less than or equal to 0!
     * 
     * @return The number of minecraft blocks this contents moves per minecraft ticks */
    double getSpeed();

    public interface IPipeContentsItem extends IPipeContents {
        ItemStack cloneItemStack();

        IPipePropertyProvider getProperties();
    }

    public interface IPipeContentsFluid extends IPipeContents {
        int getAmount();

        Fluid getFluid();

        NBTTagCompound getNBT();

        FluidStack cloneFluidStack();
    }
}
