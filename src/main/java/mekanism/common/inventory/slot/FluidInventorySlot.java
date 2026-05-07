package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidInventorySlot extends BasicInventorySlot implements IFluidHandlerSlot {

    //TODO: Rename this maybe? It is basically used as an "input" slot where it accepts either an empty container to try and take stuff
    // OR accepts a fluid container tha that has contents that match the handler for purposes of filling the handler

    /**
     * Fills/Drains the tank depending on if this item has any contents in it
     */
    public static FluidInventorySlot input(IFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        return new FluidInventorySlot(fluidTank, ConstantPredicates.alwaysFalse(), getInputPredicate(fluidTank), listener, x, y);
    }

    protected static Predicate<ItemResource> getInputPredicate(IFluidTank fluidTank) {
        return itemType -> {
            IFluidHandlerItem fluidHandlerItem = tryGetFluidHandlerUnstacked(itemType);
            if (fluidHandlerItem != null) {
                boolean hasEmpty = false;
                for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
                    FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                    if (fluidInTank.isEmpty()) {
                        hasEmpty = true;
                    } else if (fluidTank.insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).amount() < fluidInTank.amount()) {
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
                if (fluid.amount() < FluidType.BUCKET_VOLUME) {
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
    public static FluidInventorySlot rotary(IFluidTank fluidTank, BooleanSupplier modeSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        return new FluidInventorySlot(fluidTank, ConstantPredicates.alwaysFalse(), itemType -> {
            ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(itemType);
            if (fluidHandler != null) {
                boolean mode = modeSupplier.getAsBoolean();
                //Mode == true if fluid to gas
                boolean allEmpty = true;
                for (int tank = 0, tanks = fluidHandler.size(); tank < tanks; tank++) {
                    FluidResource fluidInTank = fluidHandler.getResource(tank);
                    if (!fluidInTank.isEmpty()) {
                        //TODO - 26.1: Are call sites ever in a transactional context?
                        try (Transaction simulation = Transaction.openRoot()) {
                            if (fluidTank.insert(fluidInTank, fluidHandler.getAmountAsInt(tank), simulation, AutomationType.INTERNAL) > 0) {
                                //True if we are the input tank and the items contents are valid and can fill the tank with any of our contents
                                return mode;
                            }
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
    public static FluidInventorySlot fill(IFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        return new FluidInventorySlot(fluidTank, ConstantPredicates.alwaysFalse(), getFillPredicate(fluidTank), listener, x, y);
    }

    public static Predicate<ItemResource> getFillPredicate(IFluidTank fluidTank) {
        //TODO - 26.1: Re-evaluate this method, and if we want to inline to canFill anywhere
        //TODO - 26.1: Figure out item access
        return itemType -> canFill(fluidTank, ItemAccess.forStack(itemType.toStack()));
    }

    public static boolean canFill(IFluidTank fluidTank, ItemAccess itemAccess) {
        ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(itemAccess);
        if (fluidHandler != null) {
            for (int tank = 0, tanks = fluidHandler.size(); tank < tanks; tank++) {
                FluidResource storedType = fluidHandler.getResource(tank);
                if (!storedType.isEmpty()) {
                    //TODO - 26.1: Are call sites ever in a transactional context?
                    try (Transaction simulation = Transaction.openRoot()) {
                        if (fluidTank.insert(storedType, fluidHandler.getAmountAsInt(tank), simulation, AutomationType.INTERNAL) > 0) {
                            //True if we can fill the tank with any of our contents
                            // Note: We need to recheck the fact the fluid is not empty and that it is valid,
                            // in case the item has multiple tanks and only some of the fluids are valid
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Accepts any items that can be filled with the current contents of the fluid tank, or if it is a fluid container and the tank is currently empty
     * <p>
     * Drains the tank into this item.
     */
    public static FluidInventorySlot drain(IFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid handler cannot be null");
        return new FluidInventorySlot(fluidTank, ConstantPredicates.alwaysFalse(), itemType -> {
            IFluidHandlerItem itemFluidHandler = tryGetFluidHandlerUnstacked(itemType);
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
    public static IFluidHandlerItem tryGetFluidHandlerUnstacked(ItemResource itemType) {
        //TODO - 26.1: Figure out how to do fluid caps
        //If we have more than one item in the input, check if we can fill a single item of it
        // The fluid handler for buckets returns false about being able to accept fluids if they are stacked
        // though we have special handling to only move one item at a time anyway
        // Though we first have to check if it has a capability exposed at all while stacked
        /*if (stack.count() > 1 && Capabilities.FLUID_LEGACY.getCapability(ItemAccess.forStack(stack)) == null) {
            return null;
        }
        ItemStack stackToCheck = stack.count() > 1 ? stack.copyWithCount(1) : stack;
        return Capabilities.FLUID_LEGACY.getCapability(ItemAccess.forStack(stackToCheck));*/
        return Capabilities.FLUID_LEGACY.getCapability(itemType);
    }

    //TODO: Should we make this also have the fluid type have to match a desired type???
    public static boolean isNonFullFluidContainer(@Nullable IFluidHandlerItem fluidHandler) {
        if (fluidHandler != null) {
            for (int tank = 0, tanks = fluidHandler.getTanks(); tank < tanks; tank++) {
                if (fluidHandler.getFluidInTank(tank).amount() < fluidHandler.getTankCapacity(tank)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected final IFluidTank fluidTank;
    private boolean isDraining;
    private boolean isFilling;

    protected FluidInventorySlot(IFluidTank fluidTank, Predicate<ItemResource> canExtract, Predicate<ItemResource> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        this(fluidTank, canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener, x, y);
        //Note: We pass alwaysTrue as the validator, so that if a mod only exposes a fluid handler on the filled item
        // then we don't have it all of a sudden being invalid after it is emptied
    }

    protected FluidInventorySlot(IFluidTank fluidTank, Predicate<ItemResource> canExtract, Predicate<ItemResource> canInsert,
          Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, validator, listener, x, y);
        setSlotType(ContainerSlotType.EXTRA);
        this.fluidTank = fluidTank;
    }

    @Override
    public void setContents(ItemResource itemType, int storedAmount) {
        super.setContents(itemType, storedAmount);
        //Reset the cache of if we are currently draining or filling
        isDraining = false;
        isFilling = false;
    }

    @Override
    public IFluidTank getFluidTank() {
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
    public void serialize(ValueOutput output) {
        super.serialize(output);
        //TODO - 1.21: These two states don't get persisted anymore when breaking blocks that have fluid inventory slots
        if (isDraining) {
            output.putBoolean(SerializationConstants.DRAINING, true);
        }
        if (isFilling) {
            output.putBoolean(SerializationConstants.FILLING, true);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        //Grab the booleans regardless if they are present as if they aren't that means they are false
        isDraining = input.getBooleanOr(SerializationConstants.DRAINING, isDraining);
        isFilling = input.getBooleanOr(SerializationConstants.FILLING, isFilling);
        super.deserialize(input);
    }
}