package mekanism.common.inventory.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

public class TransactionalSlot extends Slot implements ITransactionalSlot {

    private final SlotSnapshotJournal journal = new SlotSnapshotJournal();

    public TransactionalSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public final void set(@NotNull ItemStack stack) {
        setDirect(stack);
        setChanged();
    }

    protected void setDirect(ItemStack stack) {
        //Copy of super.set but without the call to setChanged
        this.container.setItem(getSlotIndex(), stack);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount == 0) {
            //TODO: Should we be checking isItemValid
            //"Fail quick" if the given stack is empty
            return 0;
        }
        ItemStack stack = resource.toStack(amount);
        if (!mayPlace(stack)) {
            return 0;
        }
        ItemStack current = getItem();
        int needed = getMaxStackSize(stack) - current.count();
        if (needed <= 0 || !current.isEmpty() && !resource.matches(current)) {
            //Fail if we are a full slot or the resource we are inserting doesn't match our current resource
            return 0;
        }
        int toAdd = Math.min(amount, needed);
        //Save a snapshot so that we can roll back if necessary
        journal.updateSnapshots(transaction);
        setDirect(resource.toStack(current.count() + toAdd));
        return toAdd;
    }

    @Override
    public int extract(Player player, ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount == 0 || !mayPickup(player)) {
            //"Fail quick" if the given stack is empty, or we can't pick up from the slot
            return 0;
        }
        ItemStack current = getItem();
        if (!resource.matches(current)) {
            return 0;
        }
        //If we are trying to extract more than we have, just change it so that we are extracting it all
        int toRemove = Math.min(amount, current.count());
        //Save a snapshot so that we can roll back if necessary
        journal.updateSnapshots(transaction);
        setDirect(resource.toStack(current.count() - toRemove));
        return toRemove;
    }

    private class SlotSnapshotJournal extends SnapshotJournal<ItemStack> {

        @Override
        protected ItemStack createSnapshot() {
            return TransactionalSlot.this.getItem();
        }

        @Override
        protected void revertToSnapshot(ItemStack snapshot) {
            TransactionalSlot.this.setDirect(snapshot);
        }

        @Override
        protected void onRootCommit(ItemStack originalState) {
            TransactionalSlot.this.setChanged();
        }
    }
}