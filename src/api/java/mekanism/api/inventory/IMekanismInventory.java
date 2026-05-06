package mekanism.api.inventory;

import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault//TODO - 26.1: Docs and generify to support other resource types
public interface IMekanismInventory extends ResourceHandler<ItemResource>, IContentsListener {

    /**
     * Used to check if an instance of {@link IMekanismInventory} actually has an inventory.
     *
     * @return True if we are actually an inventory.
     *
     * @apiNote If for some reason you are comparing to {@link IMekanismInventory} without having gotten the object via the item handler capability, then you must call
     * this method to make sure that it really is an inventory. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean hasInventory() {
        return true;
    }

    /**
     * Returns the list of IInventorySlots that this inventory exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all IInventorySlots that this {@link IMekanismInventory} contains for the given side. If there are no slots for the side or
     * {@link #hasInventory()} is false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all slots in the inventory. This will be used by the container generating code
     * to add all the proper slots that are needed. Additionally, if {@link #hasInventory()} is false, this <em>MUST</em> return an empty list.
     * @since 10.8.0
     */
    List<IInventorySlot> getInventorySlots();

    /**
     * Returns the {@link IInventorySlot} that has the given index from the list of slots on the given side.
     *
     * @param slot The index of the slot to retrieve.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link IInventorySlot} that has the given index from the list of slots on the given side.
     *
     * @since 10.8.0
     */
    @Nullable
    default IInventorySlot getInventorySlot(int slot) {
        //TODO - 26.1: Should we make this throw instead of return null when invalid? That means it would propagate the exception times that resource handler defines
        List<IInventorySlot> slots = getInventorySlots();
        return slot >= 0 && slot < slots.size() ? slots.get(slot) : null;
    }

    //@Override
    default void setStackInSlot(int slot, ItemResource itemType, int amount) {//TODO - 26.1: Re-evaluate, previously was in IItemHandlerModifiable
        IInventorySlot inventorySlot = getInventorySlot(slot);
        if (inventorySlot != null) {
            inventorySlot.setStack(itemType, amount);
        }
    }

    @Override
    default int size() {
        return getInventorySlots().size();
    }

    //@Override
    default ItemStack getStackInSlot(int slot) {//TODO - 26.1: Re-evaluate this method
        IInventorySlot inventorySlot = getInventorySlot(slot);
        return inventorySlot == null ? ItemStack.EMPTY : inventorySlot.getResource().toStack(inventorySlot.getCount());
    }

    @Override
    default ItemResource getResource(int index) {
        IInventorySlot inventorySlot = getInventorySlot(index);
        return inventorySlot == null ? ItemResource.EMPTY : inventorySlot.getResource();
    }

    @Override
    default long getAmountAsLong(int index) {
        IInventorySlot inventorySlot = getInventorySlot(index);
        return inventorySlot == null ? 0 : inventorySlot.getCount();
    }

    default int insert(int index, ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        IInventorySlot inventorySlot = getInventorySlot(index);
        return inventorySlot == null ? 0 : inventorySlot.insert(resource, amount, transaction, automationType);
    }

    default int insert(ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int inserted = 0;
        for (IInventorySlot slot : getInventorySlots()) {
            inserted += slot.insert(resource, amount - inserted, transaction, automationType);
            if (inserted == amount) {
                break;
            }
        }
        return inserted;
    }

    @Override
    default int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        //TODO - 26.1: Evaluate calls to this and extract for all resource handlers and see what can be moved over to indexless interactions
        //TODO - 26.1: Should this fallback for insert and extract use internal or external as the automation type?
        return insert(resource, amount, transaction, AutomationType.INTERNAL);
    }

    @Override
    default int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return insert(resource, amount, transaction, AutomationType.INTERNAL);
    }

    default int extract(int index, ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        IInventorySlot inventorySlot = getInventorySlot(index);
        return inventorySlot == null ? 0 : inventorySlot.extract(resource, amount, transaction, automationType);
    }

    default int extract(ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int extracted = 0;
        for (IInventorySlot slot : getInventorySlots()) {
            extracted += slot.extract(resource, amount - extracted, transaction, automationType);
            if (extracted == amount) {
                break;
            }
        }
        return extracted;
    }

    @Override
    default int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return extract(index, resource, amount, transaction, AutomationType.INTERNAL);
    }

    @Override
    default int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return extract(resource, amount, transaction, AutomationType.INTERNAL);
    }

    @Override
    default long getCapacityAsLong(int index, ItemResource resource) {
        IInventorySlot inventorySlot = getInventorySlot(index);
        return inventorySlot == null ? 0 : inventorySlot.getLimit(ItemResource.EMPTY);
    }

    @Override
    default boolean isValid(int index, ItemResource resource) {
        IInventorySlot inventorySlot = getInventorySlot(index);
        return inventorySlot != null && inventorySlot.isValid(resource);
    }

    /**
     * Sided inventory helper for isEmpty
     *
     * @return true if completely empty on the default side
     *
     * @since 10.4.0
     */
    default boolean isInventoryEmpty() {
        for (IInventorySlot slot : getInventorySlots()) {
            if (!slot.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}