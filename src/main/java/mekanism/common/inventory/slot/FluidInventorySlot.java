package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidInventorySlot extends BasicInventorySlot implements IFluidHandlerSlot {

    //TODO: Rename this maybe? It is basically used as an "input" slot where it accepts either an empty container to try and take stuff
    // OR accepts a fluid container tha that has contents that match the handler for purposes of filling the handler

    /**
     * Fills/Drains the tank depending on if this item has any contents in it
     */
    public static FluidInventorySlot input(IExtendedFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        return new FluidInventorySlot(fluidTank, alwaysFalse, getInputPredicate(fluidTank), stack -> FluidUtil.getFluidHandler(stack).isPresent(), listener, x, y);
    }

    protected static Predicate<ItemStack> getInputPredicate(IExtendedFluidTank fluidTank) {
        return stack -> {
            //If we have more than one item in the input, check if we can fill a single item of it
            // The fluid handler for buckets returns false about being able to accept fluids if they are stacked
            // though we have special handling to only move one item at a time anyways
            Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack.getCount() > 1 ? StackUtils.size(stack, 1) : stack).resolve();
            if (cap.isPresent()) {
                IFluidHandlerItem fluidHandlerItem = cap.get();
                boolean hasEmpty = false;
                for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                    FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                    if (fluidInTank.isEmpty()) {
                        hasEmpty = true;
                    } else if (fluidTank.insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                        //True if the items contents are valid and we can fill the tank with any of our contents
                        return true;
                    }
                }
                //If we have no valid fluids/can't fill the tank with it
                if (fluidTank.isEmpty()) {
                    //we return if there is at least one empty tank in the item so that we can then drain into it
                    return hasEmpty;
                }
                return fluidHandlerItem.fill(fluidTank.getFluid(), FluidAction.SIMULATE) > 0;
            }
            return false;
        };
    }

    /**
     * Fills/Drains the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static FluidInventorySlot rotary(IExtendedFluidTank fluidTank, BooleanSupplier modeSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        return new FluidInventorySlot(fluidTank, alwaysFalse, stack -> {
            Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack).resolve();
            if (cap.isPresent()) {
                boolean mode = modeSupplier.getAsBoolean();
                //Mode == true if fluid to gas
                IFluidHandlerItem fluidHandlerItem = cap.get();
                boolean allEmpty = true;
                for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                    FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                    if (!fluidInTank.isEmpty()) {
                        if (fluidTank.insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                            //True if we are the input tank and the items contents are valid and can fill the tank with any of our contents
                            return mode;
                        }
                        allEmpty = false;
                    }
                }
                //We want to try and drain the tank AND we are not the input tank
                return allEmpty && !mode;
            }
            return false;
        }, stack -> {
            LazyOptional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(stack);
            if (capability.isPresent()) {
                if (modeSupplier.getAsBoolean()) {
                    //Input tank, so we want to fill it
                    IFluidHandlerItem fluidHandlerItem = capability.resolve().get();
                    for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                        FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                        if (!fluidInTank.isEmpty() && fluidTank.isFluidValid(fluidInTank)) {
                            return true;
                        }
                    }
                    return false;
                }
                //Output tank, so we want to drain
                return isNonFullFluidContainer(capability);
            }
            return false;
        }, listener, x, y);
    }

    /**
     * Fills the tank from this item
     */
    public static FluidInventorySlot fill(IExtendedFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        return new FluidInventorySlot(fluidTank, alwaysFalse, stack -> {
            Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack).resolve();
            if (cap.isPresent()) {
                IFluidHandlerItem fluidHandlerItem = cap.get();
                for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                    FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                    if (!fluidInTank.isEmpty() && fluidTank.insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                        //True if we can fill the tank with any of our contents
                        // Note: We need to recheck the fact the fluid is not empty and that it is valid,
                        // in case the item has multiple tanks and only some of the fluids are valid
                        return true;
                    }
                }
            }
            return false;
        }, stack -> {
            //Allow for any fluid containers, but we have a more restrictive canInsert so that we don't insert all items
            //TODO: Check the other ones to see if we need something like this for them
            return FluidUtil.getFluidHandler(stack).isPresent();
        }, listener, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the fluid tank, or if it is a fluid container and the tank is currently empty
     *
     * Drains the tank into this item.
     */
    public static FluidInventorySlot drain(IExtendedFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid handler cannot be null");
        return new FluidInventorySlot(fluidTank, alwaysFalse, stack -> {
            //If we have more than one item in the input, check if we can fill a single item of it
            // The fluid handler for buckets returns false about being able to accept fluids if they are stacked
            // though we have special handling to only move one item at a time anyways
            LazyOptional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack.getCount() > 1 ? StackUtils.size(stack, 1) : stack);
            if (cap.isPresent()) {
                FluidStack fluidInTank = fluidTank.getFluid();
                if (fluidInTank.isEmpty()) {
                    return true;
                }
                IFluidHandlerItem itemFluidHandler = cap.resolve().get();
                //True if the tanks contents are valid and we can fill the item with any of the contents
                return itemFluidHandler.fill(fluidInTank, FluidAction.SIMULATE) > 0;
            }
            return false;
        }, stack -> isNonFullFluidContainer(FluidUtil.getFluidHandler(stack)), listener, x, y);
    }

    //TODO: Should we make this also have the fluid type have to match a desired type???
    private static boolean isNonFullFluidContainer(LazyOptional<IFluidHandlerItem> capability) {
        Optional<IFluidHandlerItem> cap = capability.resolve();
        if (cap.isPresent()) {
            IFluidHandlerItem fluidHandler = cap.get();
            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                if (fluidHandler.getFluidInTank(tank).getAmount() < fluidHandler.getTankCapacity(tank)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    protected final IExtendedFluidTank fluidTank;
    private boolean isDraining;
    private boolean isFilling;

    protected FluidInventorySlot(IExtendedFluidTank fluidTank, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          Predicate<@NonNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, validator, listener, x, y);
        setSlotType(ContainerSlotType.EXTRA);
        this.fluidTank = fluidTank;
    }

    @Override
    public void setStack(ItemStack stack) {
        super.setStack(stack);
        //Reset the cache of if we are currently draining or filling
        isDraining = false;
        isFilling = false;
    }

    @Override
    public IExtendedFluidTank getFluidTank() {
        return fluidTank;
    }

    @Override
    public boolean isDraining() {
        return isDraining;
    }

    @Override
    public boolean isFilling() {
        return isFilling;
    }

    @Override
    public void setDraining(boolean draining) {
        isDraining = draining;
    }

    @Override
    public void setFilling(boolean filling) {
        isFilling = filling;
    }
}