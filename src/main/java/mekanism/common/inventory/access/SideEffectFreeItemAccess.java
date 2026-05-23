package mekanism.common.inventory.access;

import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@NothingNullByDefault//TODO - 26.1: Re-evaluate this and the name
public class SideEffectFreeItemAccess extends SnapshotJournal<ResourceStack<ItemResource>> implements ItemAccess {

    private ItemResource currentType;
    private int stored;

    public SideEffectFreeItemAccess(ItemResource itemType) {
        this(itemType, 1);
    }

    public SideEffectFreeItemAccess(ItemResource itemType, int amount) {
        this.currentType = itemType;
        this.stored = amount;
    }

    @Override
    public ItemResource getResource() {
        return this.currentType;
    }

    @Override
    public int getAmount() {
        return this.stored;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !currentType.isEmpty() && !resource.equals(currentType)) {
            return 0;
        }
        updateSnapshots(transaction);
        //Note: The current type either matches, or is empty and should be updated
        currentType = resource;
        stored += amount;
        return amount;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !resource.equals(currentType) || stored == 0) {
            //Nothing is being extracted, or a different type is being extracted, or nothing is stored
            return 0;
        }
        int removed = Math.min(amount, stored);
        updateSnapshots(transaction);
        stored -= removed;
        if (stored == 0) {//Update to empty if nothing is stored anymore
            currentType = ItemResource.EMPTY;
        }
        return removed;
    }

    @Override
    protected ResourceStack<ItemResource> createSnapshot() {
        return new ResourceStack<>(currentType, stored);
    }

    @Override
    protected void revertToSnapshot(ResourceStack<ItemResource> snapshot) {
        this.currentType = snapshot.resource();
        this.stored = snapshot.amount();
    }
}