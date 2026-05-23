package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
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
        return new FluidInventorySlot(fluidTank, ConstantPredicates.notExternal(), (itemType, automationType) ->
              automationType.isInternal() || canInput(fluidTank, itemType), listener, x, y);
    }

    protected static boolean canInput(IFluidTank fluidTank, ItemResource itemType) {
        return canInput(fluidTank, InventoryUtils.queryOnlyAccess(itemType));
    }

    protected static boolean canInput(IFluidTank fluidTank, ItemAccess itemAccess) {
        //TODO - 26.1: Figure out fluid handlers, this used to be a one by one
        ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(itemAccess);
        return fluidHandler != null && canInput(fluidHandler, fluidTank);
    }

    public static boolean canInput(ResourceHandler<FluidResource> fluidHandler, IFluidTank fluidTank) {
        boolean hasEmpty = false;
        for (int tank = 0, tanks = fluidHandler.size(); tank < tanks; tank++) {
            FluidResource fluidType = fluidHandler.getResource(tank);
            if (fluidType.isEmpty()) {
                hasEmpty = true;
            } else if (simulateCanInsert(fluidTank, fluidType, fluidHandler.getAmountAsInt(tank))) {
                //True if the items contents are valid, and we can fill the tank with any of our contents
                return true;
            }
        }
        //If we have no valid fluids/can't fill the tank with it
        if (fluidTank.isEmpty()) {
            //we return if there is at least one empty tank in the item so that we can then drain into it
            return hasEmpty;
        }
        try (Transaction simulation = MekanismUtils.openTransactionSafe()) {
            //Note: We try to insert a bucket's amount to work around buckets not being able to be filled with a smaller amount
            // We try to insert more than a bucket though in case we have more, and it lets us get a better estimate on some custom handlers
            //TODO - 26.1: Re-evaluate this, do we want to just pass a bucket volume to it so that it potentially has to do less checking
            int toInsert = Math.max(fluidTank.amountAsInt(), FluidType.BUCKET_VOLUME);
            return fluidHandler.insert(fluidTank.resource(), toInsert, simulation) > 0;
        }
    }

    /**
     * Fills/Drains the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static FluidInventorySlot rotary(IFluidTank fluidTank, BooleanSupplier modeSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        return new FluidInventorySlot(fluidTank, ConstantPredicates.notExternal(), (itemType, automationType) -> {
            if (automationType.isInternal()) {
                return true;
            }
            ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(InventoryUtils.queryOnlyAccess(itemType));
            if (fluidHandler != null) {
                boolean mode = modeSupplier.getAsBoolean();
                //Mode == true if fluid to gas
                boolean allEmpty = true;
                for (int tank = 0, tanks = fluidHandler.size(); tank < tanks; tank++) {
                    FluidResource fluidType = fluidHandler.getResource(tank);
                    if (!fluidType.isEmpty()) {
                        if (simulateCanInsert(fluidTank, fluidType, fluidHandler.getAmountAsInt(tank))) {
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
    public static FluidInventorySlot fill(IFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        return new FluidInventorySlot(fluidTank, ConstantPredicates.notExternal(), (itemType, automationType) ->
              automationType.isInternal() || canFill(fluidTank, itemType), listener, x, y);
    }

    public static boolean canFill(IFluidTank fluidTank, ItemResource itemType) {
        //TODO - 26.1: Figure out item access
        return canFill(fluidTank, InventoryUtils.queryOnlyAccess(itemType));
    }

    public static boolean canFill(IFluidTank fluidTank, ItemAccess itemAccess) {
        ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(itemAccess);
        if (fluidHandler != null) {
            for (int tank = 0, tanks = fluidHandler.size(); tank < tanks; tank++) {
                FluidResource storedType = fluidHandler.getResource(tank);
                if (!storedType.isEmpty() && simulateCanInsert(fluidTank, storedType, fluidHandler.getAmountAsInt(tank))) {
                    //True if we can fill the tank with any of our contents
                    // Note: We need to recheck the fact the fluid is not empty and that it is valid,
                    // in case the item has multiple tanks and only some of the fluids are valid
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean simulateCanInsert(IFluidTank fluidTank, FluidResource fluidType, int amount) {
        /*try (Transaction simulation = MekanismUtils.openTransactionSafe()) {
            return fluidTank.insert(fluidType, amount, simulation, AutomationType.INTERNAL) > 0;
        }*/
        //TODO - 26.1: This used to do a full on simulation, do we need to check to make sure it isn't full or is not checking it actually more accurate for what we want
        // If so we can easily check that it isn't full if the resource type matches, or we might want to go back to simulation,
        // even though that means we might need to be careful about the transactional context
        if (fluidTank.isValidForInsertion(fluidType, AutomationType.INTERNAL)) {
            //Calculate if the fluid is ever valid for insertion into the fluid tank
            //If it is and our tank is currently empty or has the same type of resource
            // that means the items contents are valid, and we can fill the tank with any of our contents
            return fluidTank.isEmpty() || fluidTank.resource().equals(fluidType);
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
        return new FluidInventorySlot(fluidTank, ConstantPredicates.notExternal(), (itemType, automationType) -> {
            if (automationType.isInternal()) {
                return true;
            }
            //TODO - 26.1: Figure out fluid handlers, this used to be a one by one
            ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(InventoryUtils.queryOnlyAccess(itemType));
            if (fluidHandler != null) {
                //True if the tanks contents are valid, and we can fill the item with any of the contents
                if (fluidTank.isEmpty()) {
                    return isNonFullFluidContainer(fluidHandler);
                }
                try (Transaction simulation = MekanismUtils.openTransactionSafe()) {
                    //TODO - 26.1: Do we need to do similar to the canInput that checks for bucket volume?
                    return fluidHandler.insert(fluidTank.resource(), fluidTank.amountAsInt(), simulation) > 0;
                }
            }
            return false;
        }, listener, x, y);
    }

    //TODO - 26.1: Should we make this also have the fluid type have to match a desired type???
    // If not we should inline the call
    public static boolean isNonFullFluidContainer(ResourceHandler<FluidResource> fluidHandler) {
        return !ResourceHandlerUtil.isFull(fluidHandler);
    }

    protected final IFluidTank fluidTank;
    private LastTransferDirection lastTransferDirection = LastTransferDirection.UNKNOWN;

    protected FluidInventorySlot(IFluidTank fluidTank, BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        //Note: We pass alwaysTrue as the validator, so that if a mod only exposes a fluid handler on the filled item
        // then we don't have it all of a sudden being invalid after it is emptied
        super(canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener, x, y);
        setSlotType(ContainerSlotType.EXTRA);
        this.fluidTank = fluidTank;
    }

    @Override
    public IFluidTank getFluidTank() {
        return fluidTank;
    }

    @Override
    public LastTransferDirection getLastTransferDirection() {
        return lastTransferDirection;
    }

    @Override
    public void setLastTransferDirection(LastTransferDirection direction) {
        this.lastTransferDirection = direction;
    }

    @Override
    public void copyContents(IResourceContainer<ItemResource> other) {
        super.copyContents(other);
        if (other instanceof IFluidHandlerSlot otherSlot) {
            setLastTransferDirection(otherSlot.getLastTransferDirection());
        }
    }

    @Override
    public void onContentsChanged(LargeResourceStack<ItemResource> originalState) {
        super.onContentsChanged(originalState);
        if (isEmpty()) {
            //If we are now empty, reset the last transfer direction as it is no longer valid
            resetLastTransferDirection();
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        super.serialize(output);
        //TODO - 1.21: These two states don't get persisted anymore when breaking blocks that have fluid inventory slots
        if (lastTransferDirection != LastTransferDirection.UNKNOWN) {
            output.store(SerializationConstants.LAST_TRANSFER_DIRECTION, LastTransferDirection.CODEC, lastTransferDirection);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        super.deserialize(input);
        setLastTransferDirection(input.read(SerializationConstants.LAST_TRANSFER_DIRECTION, LastTransferDirection.CODEC).orElse(LastTransferDirection.UNKNOWN));
    }
}