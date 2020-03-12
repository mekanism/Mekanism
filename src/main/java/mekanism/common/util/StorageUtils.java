package mekanism.common.util;

import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
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
        DataHandlerUtils.readTanks(Collections.singletonList(tank), ItemDataUtils.getList(stack, NBTConstants.FLUID_TANKS));
        return tank.getFluid();
    }

    public static void mergeTanks(IExtendedFluidTank tank, IExtendedFluidTank mergeTank) {
        if (tank.isEmpty()) {
            tank.setStack(mergeTank.getFluid());
        } else if (!mergeTank.isEmpty() && tank.isFluidEqual(mergeTank.getFluid())) {
            mergeTank.growStack(tank.getFluidAmount(), Action.EXECUTE);
        }
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void mergeTanks(IChemicalTank<CHEMICAL, STACK> tank,
          IChemicalTank<CHEMICAL, STACK> mergeTank) {
        if (tank.isEmpty()) {
            tank.setStack(mergeTank.getStack());
        } else if (!mergeTank.isEmpty() && tank.isTypeEqual(mergeTank.getStack())) {
            mergeTank.growStack(tank.getStored(), Action.EXECUTE);
        }
    }
}