package mekanism.common.item;

import mekanism.common.base.ISustainedTank;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public interface IItemSustainedTank extends ISustainedTank {

    @Override
    default void setFluidStack(FluidStack fluidStack, Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) data[0];
            if (fluidStack == null || fluidStack.getAmount() == 0) {
                ItemDataUtils.removeData(itemStack, "fluidTank");
            } else {
                ItemDataUtils.setCompound(itemStack, "fluidTank", fluidStack.writeToNBT(new CompoundNBT()));
            }
        }
    }

    @Override
    default FluidStack getFluidStack(Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) data[0];
            if (!ItemDataUtils.hasData(itemStack, "fluidTank")) {
                return null;
            }
            return FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "fluidTank"));
        }
        return null;
    }

    @Override
    default boolean hasTank(Object... data) {
        return data[0] instanceof ItemStack && ((ItemStack) data[0]).getItem() instanceof ISustainedTank;
    }
}