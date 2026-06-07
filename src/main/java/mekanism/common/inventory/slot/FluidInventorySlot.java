package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.ItemAccessUtils;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidInventorySlot extends ResourceHandlerSlot {

    /**
     * Fills/Drains the tank depending on if this item has any contents in it
     */
    public static FluidInventorySlot input(IFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        //TODO: Rename this method maybe? It is basically used as an "input" slot where it accepts either an empty container to try and take stuff
        // OR accepts a fluid container tha that has contents that match the handler for purposes of filling the handler
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        return new FluidInventorySlot(fluidTank, (itemType, automationType) -> !automationType.isExternal() || !canInput(fluidTank, ItemAccessUtils.sideEffectFreeAccess(itemType), Capabilities.FLUID.item()),
              (itemType, automationType) -> automationType.isInternal() || canInput(fluidTank, ItemAccessUtils.sideEffectFreeAccess(itemType), Capabilities.FLUID.item()), listener, x, y);
    }

    /**
     * Fills/Drains the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static FluidInventorySlot rotary(IFluidTank fluidTank, BooleanSupplier isProcessingResource, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Objects.requireNonNull(isProcessingResource, "The supplier that determines whether the resource is being processed cannot be null");
        return new FluidInventorySlot(fluidTank, (itemType, automationType) -> !automationType.isExternal() || !canRotaryInsert(fluidTank, itemType, Capabilities.FLUID.item(), isProcessingResource),
              (itemType, automationType) -> automationType.isInternal() || canRotaryInsert(fluidTank, itemType, Capabilities.FLUID.item(), isProcessingResource), listener, x, y);
    }

    /**
     * Fills the tank from this item
     */
    public static FluidInventorySlot fill(IFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        return new FluidInventorySlot(fluidTank, (itemType, automationType) -> !automationType.isExternal() || !canFill(fluidTank, ItemAccessUtils.sideEffectFreeAccess(itemType), Capabilities.FLUID.item()),
              (itemType, automationType) -> automationType.isInternal() || canFill(fluidTank, ItemAccessUtils.sideEffectFreeAccess(itemType), Capabilities.FLUID.item()), listener, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the fluid tank, or if it is a fluid container and the tank is currently empty
     * <p>
     * Drains the tank into this item.
     */
    public static FluidInventorySlot drain(IFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid handler cannot be null");
        return new FluidInventorySlot(fluidTank, (itemType, automationType) -> !automationType.isExternal() || !canDrain(fluidTank, ItemAccessUtils.sideEffectFreeAccess(itemType), Capabilities.FLUID.item()),
              (itemType, automationType) -> automationType.isInternal() || canDrain(fluidTank, ItemAccessUtils.sideEffectFreeAccess(itemType), Capabilities.FLUID.item()), listener, x, y);
    }

    protected final IFluidTank fluidTank;

    protected FluidInventorySlot(IFluidTank fluidTank, BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, listener, x, y);
        setSlotType(ContainerSlotType.EXTRA);
        this.fluidTank = fluidTank;
    }

    public IFluidTank getFluidTank() {
        return fluidTank;
    }

    public void handleTank(IInventorySlot outputSlot, ContainerEditMode editMode, @Nullable TransactionContext transaction) {
        handleContainer(getFluidTank(), outputSlot, editMode, ContainerType.FLUID, transaction);
    }

    /// Drains the container into the slot
    ///
    /// @param outputSlot The slot to move our container to after draining the resource container.
    public void drainTankIntoSlot(IInventorySlot outputSlot, @Nullable TransactionContext transaction) {
        drainContainerIntoSlot(getFluidTank(), outputSlot, ContainerType.FLUID, transaction);
    }

    /// Fills the container from the slot
    ///
    /// @param outputSlot The slot to move our container to after draining the item.
    public void fillTankFromSlot(IInventorySlot outputSlot, @Nullable TransactionContext transaction) {
        fillContainerFromSlot(getFluidTank(), outputSlot, ContainerType.FLUID, transaction);
    }

    /// Fills tank from slot, does not try converting the item via any conversions conversion
    public boolean fillTankFromSlot(@Nullable TransactionContext transaction) {
        return fillContainerFromSlot(getFluidTank(), ContainerType.FLUID, transaction);
    }
}