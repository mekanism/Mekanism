package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.transaction.RateLimitTracker;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.ItemAccessUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class HybridInventorySlot extends ResourceHandlerSlot {

    public static HybridInventorySlot input(MergedTank mergedTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(mergedTank, "Merged tank cannot be null");
        //Extract predicate, always allow the player to manually extract or if the insert predicate no longer matches allow for it to be extracted
        return new HybridInventorySlot(mergedTank, (itemType, automationType) -> {
            if (!automationType.isExternal()) {
                return true;
            }
            ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(itemType);
            return switch (mergedTank.getCurrentType()) {
                case FLUID -> !canInput(mergedTank.getFluidTank(), itemAccess, Capabilities.FLUID.item());
                case CHEMICAL -> !canInput(mergedTank.getChemicalTank(), itemAccess, Capabilities.CHEMICAL.item());
                //Tank is empty, check if any insert predicate is valid
                case EMPTY -> !canInput(mergedTank.getFluidTank(), itemAccess, Capabilities.FLUID.item()) &&
                              !canInput(mergedTank.getChemicalTank(), itemAccess, Capabilities.CHEMICAL.item());
            };
        }, (itemType, automationType) -> {
            if (automationType.isInternal()) {
                return true;
            }
            ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(itemType);
            return switch (mergedTank.getCurrentType()) {
                case FLUID -> canInput(mergedTank.getFluidTank(), itemAccess, Capabilities.FLUID.item());
                case CHEMICAL -> canInput(mergedTank.getChemicalTank(), itemAccess, Capabilities.CHEMICAL.item());
                //Tank is empty, check if any insert predicate is valid
                case EMPTY -> canInput(mergedTank.getFluidTank(), itemAccess, Capabilities.FLUID.item()) ||
                              canInput(mergedTank.getChemicalTank(), itemAccess, Capabilities.CHEMICAL.item());
            };
        }, null, null, listener, x, y);
    }

    private final MergedTank mergedTank;

    private HybridInventorySlot(MergedTank mergedTank, BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          @Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, insertionRateLimiter, extractionRateLimiter, listener, x, y);
        this.mergedTank = mergedTank;
    }

    public void handleTank(IInventorySlot outputSlot, ContainerEditMode editMode, @Nullable TransactionContext transaction) {
        CurrentType type = mergedTank.getCurrentType();
        IFluidTank fluidTank = mergedTank.getFluidTank();
        if (type == CurrentType.EMPTY) {
            handleContainer(fluidTank, outputSlot, editMode, ContainerType.FLUID, transaction);
            if (fluidTank.isEmpty()) {
                handleContainer(mergedTank.getChemicalTank(), outputSlot, editMode, ContainerType.CHEMICAL, transaction);
            }
        } else if (type == CurrentType.FLUID) {
            handleContainer(fluidTank, outputSlot, editMode, ContainerType.FLUID, transaction);
        } else {//Chemicals
            handleContainer(mergedTank.getChemicalTank(), outputSlot, editMode, ContainerType.CHEMICAL, transaction);
        }
    }
}
