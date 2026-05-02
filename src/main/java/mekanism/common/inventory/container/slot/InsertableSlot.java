package mekanism.common.inventory.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

public class InsertableSlot extends Slot implements IInsertableSlot {

    private final SlotSnapshotJournal journal = new SlotSnapshotJournal();

    public InsertableSlot(Container inventory, int index, int x, int y) {
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
    public int insertItem(ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount == 0) {
            //TODO: Should we even be checking isItemValid
            //"Fail quick" if the given stack is empty or we are not valid for the slot
            return 0;
        }
        ItemStack stack = resource.toStack(amount);
        if (!mayPlace(stack)) {
            return 0;
        }
        ItemStack current = getItem();
        int needed = getMaxStackSize(stack) - current.count();
        if (needed <= 0) {
            //Fail if we are a full slot
            return 0;
        }
        if (current.isEmpty() || resource.matches(current)) {
            int toAdd = Math.min(amount, needed);
            //Save a snapshot so that we can roll back if necessary
            journal.updateSnapshots(transaction);
            setDirect(resource.toStack(current.count() + toAdd));
            return toAdd;
        }
        //If we didn't accept this item, then just return the given stack
        return 0;
    }

    private class SlotSnapshotJournal extends SnapshotJournal<ItemStack> {

        @Override
        protected ItemStack createSnapshot() {
            return InsertableSlot.this.getItem();
        }

        @Override
        protected void revertToSnapshot(ItemStack snapshot) {
            InsertableSlot.this.setDirect(snapshot);
        }

        @Override
        protected void onRootCommit(ItemStack originalState) {
            InsertableSlot.this.setChanged();
        }
    }
}