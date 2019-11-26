package mekanism.common.inventory.slot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> isFluidContainer = FluidContainerUtils::isFluidContainer;

    //TODO: Rename this maybe? It is basically used as an "input" slot where it accepts either an empty container to try and take stuff
    // OR accepts a fluid container tha that has contents that match the handler for purposes of filling the handler

    /**
     * Fills/Drains the tank depending on if this item has any contents in it
     */
    public static FluidInventorySlot input(IFluidHandler fluidHandler, Predicate<@NonNull FluidStack> isValidFluid, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(fluidHandler, "Fluid handler cannot be null");
        Objects.requireNonNull(isValidFluid, "Fluid validity check cannot be null");
        return new FluidInventorySlot(fluidHandler, isValidFluid, alwaysFalse, stack -> {
            FluidStack fluidContained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            if (fluidContained.isEmpty()) {
                //We want to try and drain the tank
                return true;
            }
            //True if the items contents are valid and we can fill the tank with any of our contents
            return isValidFluid.test(fluidContained) && fluidHandler.fill(fluidContained, FluidAction.SIMULATE) > 0;
        }, isFluidContainer, inventory, x, y);
    }

    /**
     * Fills/Drains the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static FluidInventorySlot rotary(IFluidHandler fluidHandler, Predicate<@NonNull FluidStack> isValidFluid, BooleanSupplier modeSupplier,
          @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(fluidHandler, "Fluid handler cannot be null");
        Objects.requireNonNull(isValidFluid, "Fluid validity check cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        return new FluidInventorySlot(fluidHandler, isValidFluid, alwaysFalse, stack -> {
            FluidStack fluidContained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            boolean mode = modeSupplier.getAsBoolean();
            //Mode == true if fluid to gas
            if (fluidContained.isEmpty()) {
                //We want to try and drain the tank AND we are not the input tank
                return !mode;
            }
            //True if we are the input tank and the items contents are valid and can fill the tank with any of our contents
            return mode && isValidFluid.test(fluidContained) && fluidHandler.fill(fluidContained, FluidAction.SIMULATE) > 0;
        }, stack -> {
            LazyOptionalHelper<IFluidHandlerItem> capabilityHelper = new LazyOptionalHelper<>(FluidUtil.getFluidHandler(stack));
            if (capabilityHelper.isPresent()) {
                if (modeSupplier.getAsBoolean()) {
                    //Input tank, so we want to fill it
                    FluidStack fluidContained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
                    return !fluidContained.isEmpty() && isValidFluid.test(fluidContained);
                }
                //Output tank, so we want to drain
                return isNonFullFluidContainer(capabilityHelper);
            }
            return false;
        }, inventory, x, y);
    }

    /**
     * Fills the tank from this item
     */
    public static FluidInventorySlot fill(IFluidHandler fluidHandler, Predicate<@NonNull FluidStack> isValidFluid, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(fluidHandler, "Fluid handler cannot be null");
        Objects.requireNonNull(isValidFluid, "Fluid validity check cannot be null");
        return new FluidInventorySlot(fluidHandler, isValidFluid, alwaysFalse, stack -> {
            FluidStack fluidContained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            //True if we can fill the tank with any of our contents, ignored if the item has no fluid, as it won't pass isValid
            return fluidHandler.fill(fluidContained, FluidAction.SIMULATE) > 0;
        }, stack -> {
            if (!FluidContainerUtils.isFluidContainer(stack)) {
                return false;
            }
            FluidStack fluidContained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            return !fluidContained.isEmpty() && isValidFluid.test(fluidContained);
        }, inventory, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the fluid tank, or if it is a fluid container and the tank is currently empty
     *
     * Drains the tank into this item.
     */
    public static FluidInventorySlot drain(FluidTank fluidTank, @Nullable IMekanismInventory inventory, int x, int y) {
        //TODO: Accept a fluid handler in general?
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        return new FluidInventorySlot(fluidTank, alwaysFalse, stack -> new LazyOptionalHelper<>(FluidUtil.getFluidHandler(stack))
              .matches(itemFluidHandler -> fluidTank.isEmpty() || itemFluidHandler.fill(fluidTank.getFluid(), FluidAction.SIMULATE) > 0),
              stack -> isNonFullFluidContainer(new LazyOptionalHelper<>(FluidUtil.getFluidHandler(stack))), inventory, x, y);
    }

    //TODO: Should we make this also have the fluid type have to match a desired type???
    private static boolean isNonFullFluidContainer(LazyOptionalHelper<IFluidHandlerItem> capabilityHelper) {
        return capabilityHelper.getIfPresentElse(fluidHandler -> {
            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                if (fluidHandler.getFluidInTank(tank).getAmount() < fluidHandler.getTankCapacity(tank)) {
                    return true;
                }
            }
            return false;
        }, false);
    }

    private final Predicate<@NonNull FluidStack> isValidFluid;
    private final IFluidHandler fluidHandler;

    private FluidInventorySlot(IFluidHandler fluidHandler, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        //TODO: Decide if this should be always true or always false for being a valid fluid. This is current only used by the draining method
        this(fluidHandler, fluid -> false, canExtract, canInsert, validator, inventory, x, y);
    }

    private FluidInventorySlot(IFluidHandler fluidHandler, Predicate<@NonNull FluidStack> isValidFluid, Predicate<@NonNull ItemStack> canExtract,
          Predicate<@NonNull ItemStack> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
        this.fluidHandler = fluidHandler;
        this.isValidFluid = isValidFluid;
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.EXTRA;
    }

    //TODO: Use the fillTank and drainTank methods
    //TODO: FIXME - Neither of these methods are currently moving the item to the output slot
    //TODO: Make sure we properly handle containers if they are stacked but the empty type doesn't stack?
    // or more likely in the case of draining... if our empty container stacks but the filled one doesn't (for example buckets)
    //TODO: Check to see if all fluid slots have a separate output slot? Because if so we maybe could make that a requirement
    // though I am unsure if we want to add that restriction

    /**
     * Fills tank from slot, does not try converting the item via gas conversion
     */
    public void fillTank() {
        int tanks = fluidHandler.getTanks();
        if (!isEmpty() && tanks > 0) {
            //Try filling from the tank's item
            Optional<IFluidHandlerItem> capability = LazyOptionalHelper.toOptional(FluidUtil.getFluidHandler(current));
            if (capability.isPresent()) {
                IFluidHandlerItem itemFluidHandler = capability.get();
                int itemTanks = itemFluidHandler.getTanks();
                if (itemTanks == 1) {
                    //If we only have one tank just directly check against that fluid instead of performing extra calculations to properly handle multiple tanks
                    FluidStack fluidInItem = itemFluidHandler.getFluidInTank(0);
                    if (!fluidInItem.isEmpty() && isValidFluid.test(fluidInItem)) {
                        //If we have a fluid that is valid for our fluid handler, attempt to drain it into our fluid handler
                        if (fillHandlerFromOther(fluidHandler, itemFluidHandler, fluidInItem)) {
                            onContentsChanged();
                        }
                    }
                } else if (itemTanks > 1) {
                    //If we have more than one tank in our item then handle calculating the different drains that will occur for filling our fluid handler
                    // We start by gathering all the fluids in the item that we are able to drain and are valid for the tank,
                    // combining same fluid types into a single fluid stack
                    Map<FluidInfo, FluidStack> knownFluids = new HashMap<>();
                    for (int itemTank = 0; itemTank < itemTanks; itemTank++) {
                        FluidStack fluidInItem = itemFluidHandler.getFluidInTank(itemTank);
                        if (!fluidInItem.isEmpty()) {
                            FluidInfo info = new FluidInfo(fluidInItem);
                            FluidStack knownFluid = knownFluids.get(info);
                            //If we have a fluid that can be drained from the item and is valid then we add it to our known fluids
                            // Note: We only bother checking if it can be drained if we do not already have it as a known fluid
                            if (knownFluid == null) {
                                if (!itemFluidHandler.drain(fluidInItem, FluidAction.SIMULATE).isEmpty() && isValidFluid.test(fluidInItem)) {
                                    knownFluids.put(info, fluidInItem.copy());
                                }
                            } else {
                                knownFluid.grow(fluidInItem.getAmount());
                            }
                        }
                    }
                    if (!knownFluids.isEmpty()) {
                        //If we found any fluids that we can drain, attempt to drain them into our item
                        boolean changed = false;
                        for (FluidStack knownFluid : knownFluids.values()) {
                            if (fillHandlerFromOther(fluidHandler, itemFluidHandler, knownFluid)) {
                                changed = true;
                            }
                        }
                        if (changed) {
                            onContentsChanged();
                        }
                    }
                }
            }
        }
    }

    /**
     * Drains tank into slot
     *
     * @param outputSlot The slot to move our container to after draining the tank.
     */
    public void drainTank(IInventorySlot outputSlot) {
        int tanks = fluidHandler.getTanks();
        //Verify we have an item, we have tanks that may need to be drained, and that our item is a fluid handler
        if (!isEmpty() && tanks > 0 && FluidUtil.getFluidHandler(current).isPresent()) {
            if (tanks == 1) {
                //If we only have one tank just directly check against that fluid instead of performing extra calculations
                // that ensure we only execute drain once per fluid type
                FluidStack fluidInTank = fluidHandler.getFluidInTank(0);
                if (!fluidInTank.isEmpty()) {
                    //If we have a fluid attempt to drain it into our item
                    fillItemAndMove(outputSlot, fluidInTank);
                }
            } else {
                //Otherwise try handling draining out of all tanks
                // We start by gathering all the fluids in the fluid handler that we are able to drain,
                // combining same fluid types into a single fluid stack
                Map<FluidInfo, FluidStack> knownFluids = new HashMap<>();
                for (int tank = 0; tank < tanks; tank++) {
                    FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                    if (!fluidInTank.isEmpty()) {
                        FluidInfo info = new FluidInfo(fluidInTank);
                        FluidStack knownFluid = knownFluids.get(info);
                        //If we have a fluid and it can be drained from the fluid handler then we add it to our known fluids
                        // Note: We only bother checking if it can be drained if we do not already have it as a known fluid
                        if (knownFluid == null) {
                            if (!fluidHandler.drain(fluidInTank, FluidAction.SIMULATE).isEmpty()) {
                                knownFluids.put(info, fluidInTank.copy());
                            }
                        } else {
                            knownFluid.grow(fluidInTank.getAmount());
                        }
                    }
                }
                //If we found any fluids that we can drain, attempt to drain them into our item
                for (FluidStack knownFluid : knownFluids.values()) {
                    if (fillItemAndMove(outputSlot, knownFluid) && isEmpty()) {
                        //If we moved the item after filling it and we now don't have an item to try and fill
                        // then just exit instead of checking the other types of fluids
                        //TODO: Eventually fix the case where the item we are filling can accept multiple different
                        // types of fluids so we may not actually want to move it immediately
                        break;
                    }
                }
            }
        }
    }

    /**
     * Fills our item and moves it to the given output slot. If it won't be able to move to the output slot, then we do not move it or drain our tank into the item.
     *
     * @param outSlot The slot our item will be moved to afterwards
     * @param fluid   The fluid we are filling the item with. This should be known to not be empty.
     *
     * @return True if we can drain the fluid from our fluid handler and the item after being filled can (and was) moved to the output slot, false otherwise
     */
    private boolean fillItemAndMove(IInventorySlot outSlot, FluidStack fluid) {
        FluidStack simulatedDrain = fluidHandler.drain(fluid, FluidAction.SIMULATE);
        if (simulatedDrain.isEmpty()) {
            //If we cannot actually drain from our fluid handler then just exit early
            return false;
        }
        FluidStack toDrain = FluidStack.EMPTY;
        ItemStack inputCopy = StackUtils.size(current, 1);
        Optional<IFluidHandlerItem> cap = LazyOptionalHelper.toOptional(FluidUtil.getFluidHandler(inputCopy));
        if (cap.isPresent()) {
            IFluidHandlerItem fluidHandlerItem = cap.get();
            //Fill the stack, note our stack is a copy so this is how we simulate to get the proper "container" item
            // and it does not actually matter that we are directly executing on the item
            toDrain = new FluidStack(fluid, fluidHandlerItem.fill(fluid, FluidAction.EXECUTE));
            //Updates our item we are trying to move to be the one that is filled
            inputCopy = fluidHandlerItem.getContainer();
        }
        if (outSlot.isEmpty()) {
            outSlot.setStack(inputCopy);
        } else {
            ItemStack outputStack = outSlot.getStack();
            if (!ItemHandlerHelper.canItemStacksStack(outputStack, inputCopy) || outputStack.getCount() >= outSlot.getLimit(outputStack)) {
                //We won't be able to move our container to the output slot so exit
                return false;
            }
            if (outSlot.growStack(1, Action.EXECUTE) != 1) {
                //TODO: Print warning about failing to increase size of stack
            }
        }
        //Note: We do not need to call onContentsChanged, because it will be done due to the stack changing from calling shrinkStack
        if (shrinkStack(1, Action.EXECUTE) != 1) {
            //TODO: Print warning about failing to shrink size of stack
        }
        if (!toDrain.isEmpty()) {
            //Actually drain the fluid from our handler
            fluidHandler.drain(toDrain, FluidAction.EXECUTE);
        }
        return true;
    }

    /**
     * Tries to drain the specified fluid from one fluid handler, while filling another fluid handler.
     *
     * @param handlerToFill  The fluid handler to fill
     * @param handlerToDrain The fluid handler to drain
     * @param fluid          The fluid to attempt to transfer
     *
     * @return True if we managed to transfer any contents, false otherwise
     */
    private boolean fillHandlerFromOther(IFluidHandler handlerToFill, IFluidHandler handlerToDrain, FluidStack fluid) {
        //Check how much of this fluid type we are actually able to drain from the handler we are draining
        FluidStack simulatedDrain = handlerToDrain.drain(fluid, FluidAction.SIMULATE);
        if (!simulatedDrain.isEmpty()) {
            //Check how much of it we will be able to put into the handler we are filling
            int simulatedFill = handlerToFill.fill(simulatedDrain, FluidAction.SIMULATE);
            if (simulatedFill > 0) {
                //Drain the handler to drain, filling the handler to fill while we are at it
                handlerToFill.fill(handlerToDrain.drain(new FluidStack(fluid, simulatedFill), FluidAction.EXECUTE), FluidAction.EXECUTE);
                return true;
            }
        }
        return false;
    }

    /**
     * Helper class to make comparing fluids ignoring amount easier
     */
    private static class FluidInfo {

        private final FluidStack fluidStack;

        public FluidInfo(FluidStack fluidStack) {
            this.fluidStack = fluidStack;
        }

        @Override
        public boolean equals(Object other) {
            return other == this || other instanceof FluidInfo && fluidStack.isFluidEqual(((FluidInfo) other).fluidStack);
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + fluidStack.getFluid().hashCode();
            if (fluidStack.hasTag()) {
                code = 31 * code + fluidStack.getTag().hashCode();
            }
            return code;
        }
    }
}