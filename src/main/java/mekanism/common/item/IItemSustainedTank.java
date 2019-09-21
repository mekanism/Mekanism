package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.api.sustained.ISustainedTank;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public interface IItemSustainedTank extends ISustainedTank {

    @Override
    default void setFluidStack(@Nonnull FluidStack fluidStack, Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) data[0];
            if (fluidStack.isEmpty()) {
                ItemDataUtils.removeData(itemStack, "fluidTank");
            } else {
                ItemDataUtils.setCompound(itemStack, "fluidTank", fluidStack.writeToNBT(new CompoundNBT()));
            }
        }
    }

    @Nonnull
    @Override
    default FluidStack getFluidStack(Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) data[0];
            if (!ItemDataUtils.hasData(itemStack, "fluidTank")) {
                return FluidStack.EMPTY;
            }
            return FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "fluidTank"));
        }
        return FluidStack.EMPTY;
    }

    @Override
    default boolean hasTank(Object... data) {
        return data[0] instanceof ItemStack && ((ItemStack) data[0]).getItem() instanceof ISustainedTank;
    }
}