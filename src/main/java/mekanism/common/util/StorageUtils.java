package mekanism.common.util;

import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.DataHandlerUtils;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class StorageUtils {

    /**
     * Gets the fluid if one is stored from an item's tank going off the basis there is a single tank. This is for cases when we may not actually have a fluid handler
     * attached to our item but it may have stored data in its tank from when it was a block
     */
    @Nonnull
    public static FluidStack getStoredFluidFromNBT(ItemStack stack) {
        BasicFluidTank tank = BasicFluidTank.create(Integer.MAX_VALUE, null);
        DataHandlerUtils.readTanks(Collections.singletonList(tank), ItemDataUtils.getList(stack, "FluidTanks"));
        return tank.getFluid();
    }
}