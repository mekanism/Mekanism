package mekanism.api.inventory.access;

import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

//TODO - 26.1: Docs and reference HandlerItemAccess (and the strict helper)
@NothingNullByDefault
public class InventorySlotItemAccess implements ItemAccess {

    protected final IInventorySlot slot;
    protected final AutomationType automationType;

    public InventorySlotItemAccess(IInventorySlot slot, AutomationType automationType) {
        this.slot = Objects.requireNonNull(slot, "Slot may not be null");
        this.automationType = Objects.requireNonNull(automationType, "Automation type may not be null");
    }

    @Override
    public ItemResource getResource() {
        return slot.getResource();
    }

    @Override
    public int getAmount() {
        return slot.getCount();
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return slot.insert(resource, amount, transaction, automationType);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return slot.extract(resource, amount, transaction, automationType);
    }
}