package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidInventorySlot extends BasicInventorySlot implements IFluidHandlerSlot {

    //TODO: Rename this maybe? It is basically used as an "input" slot where it accepts either an empty container to try and take stuff
    // OR accepts a fluid container tha that has contents that match the handler for purposes of filling the handler

    /**
     * Fills/Drains the tank depending on if this item has any contents in it
     */
    public static FluidInventorySlot input(IExtendedFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        return new FluidInventorySlot(fluidTank, ConstantPredicates.alwaysFalse(), getInputPredicate(fluidTank), listener, x, y);
    }

    protected static Predicate<ItemStack> getInputPredicate(IExtendedFluidTank fluidTank) {
        return stack -> {
            IFluidHandlerItem fluidHandlerItem = tryGetFluidHandlerUnstacked(stack);
            if (fluidHandlerItem != null) {
                boolean hasEmpty = false;
                for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
                    FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                    if (fluidInTank.isEmpty()) {
                        hasEmpty = true;
                    } else if (fluidTank.insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                        //True if the items contents are valid, and we can fill the tank with any of our contents
                        return true;
                    }
                }
                //If we have no valid fluids/can't fill the tank with it
                if (fluidTank.isEmpty()) {
                    //we return if there is at least one empty tank in the item so that we can then drain into it
                    return hasEmpty;
                }
                FluidStack fluid = fluidTank.getFluid();
                if (fluid.getAmount() < FluidType.BUCKET_VOLUME) {
                    //Workaround for buckets not being able to be filled until we have enough of our volume
                    fluid = fluid.copyWithAmount(FluidType.BUCKET_VOLUME);
                } else {
                    fluid = fluid.copy();//avoid handler modifying
                }
                return fluidHandlerItem.fill(fluid, FluidAction.SIMULATE) > 0;
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
        return new FluidInventorySlot(fluidTank, ConstantPredicates.alwaysFalse(), stack -> {
            IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(stack);
            if (fluidHandlerItem != null) {
                boolean mode = modeSupplier.getAsBoolean();
                //Mode == true if fluid to gas
                boolean allEmpty = true;
                for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
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
        }, listener, x, y);
    }

    /**
     * Fills the tank from this item
     */
    public static FluidInventorySlot fill(IExtendedFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        return new FluidInventorySlot(fluidTank, ConstantPredicates.alwaysFalse(), getFillPredicate(fluidTank), listener, x, y);
    }

    public static Predicate<ItemStack> getFillPredicate(IExtendedFluidTank fluidTank) {
        return stack -> {
            IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(stack);
            if (fluidHandlerItem != null) {
                for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
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
        };
    }

    /**
     * Accepts any items that can be filled with the current contents of the fluid tank, or if it is a fluid container and the tank is currently empty
     * <p>
     * Drains the tank into this item.
     */
    public static FluidInventorySlot drain(IExtendedFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid handler cannot be null");
        return new FluidInventorySlot(fluidTank, ConstantPredicates.alwaysFalse(), stack -> {
            IFluidHandlerItem itemFluidHandler = tryGetFluidHandlerUnstacked(stack);
            if (itemFluidHandler != null) {
                FluidStack fluidInTank = fluidTank.getFluid();
                //True if the tanks contents are valid, and we can fill the item with any of the contents
                if (fluidInTank.isEmpty()) {
                    return isNonFullFluidContainer(itemFluidHandler);
                }
                return itemFluidHandler.fill(fluidInTank.copy(), FluidAction.SIMULATE) > 0;
            }
            return false;
        }, listener, x, y);
    }

    @Nullable
    public static IFluidHandlerItem tryGetFluidHandlerUnstacked(ItemStack stack) {
        //If we have more than one item in the input, check if we can fill a single item of it
        // The fluid handler for buckets returns false about being able to accept fluids if they are stacked
        // though we have special handling to only move one item at a time anyway
        // Though we first have to check if it has a capability exposed at all while stacked
        if (stack.getCount() > 1 && Capabilities.FLUID.getCapability(stack) == null) {
            return null;
        }
        ItemStack stackToCheck = stack.getCount() > 1 ? stack.copyWithCount(1) : stack;
        return Capabilities.FLUID.getCapability(stackToCheck);
    }

    //TODO: Should we make this also have the fluid type have to match a desired type???
    public static boolean isNonFullFluidContainer(@Nullable IFluidHandlerItem fluidHandler) {
        if (fluidHandler != null) {
            for (int tank = 0, tanks = fluidHandler.getTanks(); tank < tanks; tank++) {
                if (fluidHandler.getFluidInTank(tank).getAmount() < fluidHandler.getTankCapacity(tank)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected final IExtendedFluidTank fluidTank;
    private boolean isDraining;
    private boolean isFilling;

    protected FluidInventorySlot(IExtendedFluidTank fluidTank, Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        this(fluidTank, canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener, x, y);
        //Note: We pass alwaysTrue as the validator, so that if a mod only exposes a fluid handler on the filled item
        // then we don't have it all of a sudden being invalid after it is emptied
    }

    protected FluidInventorySlot(IExtendedFluidTank fluidTank, Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert,
          Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
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

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = super.serializeNBT(provider);
        //TODO - 1.21: These two states don't get persisted anymore when breaking blocks that have fluid inventory slots
        if (isDraining) {
            nbt.putBoolean(SerializationConstants.DRAINING, true);
        }
        if (isFilling) {
            nbt.putBoolean(SerializationConstants.FILLING, true);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        //Grab the booleans regardless if they are present as if they aren't that means they are false
        isDraining = nbt.getBoolean(SerializationConstants.DRAINING);
        isFilling = nbt.getBoolean(SerializationConstants.FILLING);
        super.deserializeNBT(provider, nbt);
    }
}