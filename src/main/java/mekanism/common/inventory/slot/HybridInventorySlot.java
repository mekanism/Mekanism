package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.util.InventoryUtils;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class HybridInventorySlot extends BasicInventorySlot implements IFluidHandlerSlot {

    public static HybridInventorySlot inputOrDrain(MergedTank mergedTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(mergedTank, "Merged tank cannot be null");
        //Extract predicate, always allow the player to manually extract or if the insert predicate no longer matches allow for it to be extracted
        return new HybridInventorySlot(mergedTank, (itemType, automationType) -> {
            if (!automationType.isExternal()) {
                return true;
            }
            ItemAccess itemAccess = InventoryUtils.queryOnlyAccess(itemType);
            return switch (mergedTank.getCurrentType()) {
                case FLUID -> !FluidInventorySlot.canInput(mergedTank.getFluidTank(), itemAccess);
                case CHEMICAL -> !ChemicalInventorySlot.canDrainInsert(mergedTank.getChemicalTank(), itemAccess);
                //Tank is empty, check if any insert predicate is valid
                case EMPTY -> !FluidInventorySlot.canInput(mergedTank.getFluidTank(), itemAccess) && !ChemicalInventorySlot.canDrainInsert(mergedTank.getChemicalTank(), itemAccess);
            };
        }, (itemType, automationType) -> {
            if (automationType.isInternal()) {
                return true;
            }
            ItemAccess itemAccess = InventoryUtils.queryOnlyAccess(itemType);
            return switch (mergedTank.getCurrentType()) {
                case FLUID -> FluidInventorySlot.canInput(mergedTank.getFluidTank(), itemAccess);
                case CHEMICAL -> ChemicalInventorySlot.canDrainInsert(mergedTank.getChemicalTank(), itemAccess);
                //Tank is empty, check if any insert predicate is valid
                case EMPTY -> FluidInventorySlot.canInput(mergedTank.getFluidTank(), itemAccess) || ChemicalInventorySlot.canDrainInsert(mergedTank.getChemicalTank(), itemAccess);
            };
        }, listener, x, y);
    }

    public static HybridInventorySlot outputOrFill(MergedTank mergedTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(mergedTank, "Merged tank cannot be null");
        return new HybridInventorySlot(mergedTank, (itemType, automationType) -> {
            if (mergedTank.getCurrentType() == CurrentType.FLUID) {
                //Always allow extracting from a "fluid output" slot
                return true;
            }
            return ChemicalInventorySlot.fillExtractCheck(mergedTank.getChemicalTank(), itemType, automationType);
        }, (itemType, automationType) -> {
            if (automationType.isInternal()) {
                return true;
            }
            ItemAccess itemAccess = InventoryUtils.queryOnlyAccess(itemType);
            return switch (mergedTank.getCurrentType()) {
                //Only allow inserting internally for "fluid output" slots
                case FLUID -> automationType.isInternal();
                case CHEMICAL -> ChemicalInventorySlot.fillInsertCheck(mergedTank.getChemicalTank(), itemAccess);
                case EMPTY -> {
                    //Tank is empty, if the item is a fluid handler, and it is an internal check allow it
                    if (automationType.isInternal() && Capabilities.FLUID.hasCapability(itemAccess)) {
                        yield true;
                    }
                    //otherwise, only allow it if one of the chemical insert predicates matches
                    yield ChemicalInventorySlot.fillInsertCheck(mergedTank.getChemicalTank(), itemAccess);
                }
            };
        }, listener, x, y);
    }

    private final MergedTank mergedTank;
    // used by IFluidHandlerSlot
    private LastTransferDirection lastTransferDirection = LastTransferDirection.UNKNOWN;

    private HybridInventorySlot(MergedTank mergedTank, BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener, x, y);
        this.mergedTank = mergedTank;
    }

    @Override
    public IFluidTank getFluidTank() {
        return mergedTank.getFluidTank();
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
        if (lastTransferDirection != LastTransferDirection.UNKNOWN) {
            output.store(SerializationConstants.LAST_TRANSFER_DIRECTION, LastTransferDirection.CODEC, lastTransferDirection);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        super.deserialize(input);
        setLastTransferDirection(input.read(SerializationConstants.LAST_TRANSFER_DIRECTION, LastTransferDirection.CODEC).orElse(LastTransferDirection.UNKNOWN));
    }

    public void drainChemicalTank() {
        ChemicalInventorySlot.drainTank(this, mergedTank.getChemicalTank(), itemAccess());
    }

    public void fillChemicalTank() {
        ChemicalInventorySlot.fillTank(this, mergedTank.getChemicalTank(), itemAccess());
    }
}
