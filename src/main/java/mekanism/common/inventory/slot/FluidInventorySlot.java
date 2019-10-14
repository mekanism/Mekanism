package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.util.FluidContainerUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> validator = FluidContainerUtils::isFluidContainer;

    //TODO: Rename this maybe? It is basically used as an "input" slot where it accepts either an empty container to try and take stuff
    // OR accepts a fluid container tha that has contents that match the handler for purposes of filling the handler

    /**
     * Fills/Drains the tank depending on if this item has any contents in it
     */
    public static FluidInventorySlot input(@Nonnull IFluidHandler fluidHandler, int x, int y) {
        return new FluidInventorySlot(fluidHandler, alwaysFalse, item -> {
            FluidStack fluidContained = FluidUtil.getFluidContained(item).orElse(FluidStack.EMPTY);
            if (fluidContained.isEmpty()) {
                //We want to try and drain the tank
                return true;
            }
            //True if we can fill the tank with any of our contents
            return fluidHandler.fill(fluidContained, FluidAction.SIMULATE) > 0;
        }, validator, x, y);
    }

    /**
     * Fills the tank from this item
     */
    public static FluidInventorySlot fill(@Nonnull IFluidHandler fluidHandler, Predicate<@NonNull FluidStack> validFluid, int x, int y) {
        return new FluidInventorySlot(fluidHandler, alwaysFalse, item -> {
            FluidStack fluidContained = FluidUtil.getFluidContained(item).orElse(FluidStack.EMPTY);
            //True if we can fill the tank with any of our contents, ignored if the item has no fluid, as it won't pass isValid
            return fluidHandler.fill(fluidContained, FluidAction.SIMULATE) > 0;
        }, item -> {
            if (!FluidContainerUtils.isFluidContainer(item)) {
                return false;
            }
            FluidStack fluidContained = FluidUtil.getFluidContained(item).orElse(FluidStack.EMPTY);
            return !fluidContained.isEmpty() && validFluid.test(fluidContained);
        }, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the fluid tank, or if it is a fluid container and the tank is currently empty
     *
     * Drains the tank into this item.
     */
    public static FluidInventorySlot drain(@Nonnull FluidTank fluidTank, int x, int y) {
        //TODO: Accept a fluid handler in general?
        //TODO: validator NOTE: This is ignoring the fact that for insertion if our item fluid handler is full we really shouldn't accept it
        return new FluidInventorySlot(fluidTank, alwaysFalse, item -> new LazyOptionalHelper<>(FluidUtil.getFluidHandler(item))
              .matches(itemFluidHandler -> fluidTank.isEmpty() || itemFluidHandler.fill(fluidTank.getFluid(), FluidAction.SIMULATE) > 0), validator, x, y);
    }

    @Nonnull
    private final IFluidHandler fluidHandler;

    private FluidInventorySlot(@Nonnull IFluidHandler fluidHandler, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          Predicate<@NonNull ItemStack> validator, int x, int y) {
        super(canExtract, canInsert, validator, x, y);
        this.fluidHandler = fluidHandler;
    }

    //TODO: Make it so that the fluid handler fills
}