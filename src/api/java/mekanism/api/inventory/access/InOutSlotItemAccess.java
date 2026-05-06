package mekanism.api.inventory.access;

import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

//TODO - 26.1: Docs and reference HandlerItemAccess
@NothingNullByDefault
public class InOutSlotItemAccess extends InventorySlotItemAccess {

    protected final IInventorySlot output;

    public InOutSlotItemAccess(IInventorySlot input, IInventorySlot output) {
        super(input, AutomationType.INTERNAL);
        this.output = Objects.requireNonNull(output, "Output slot may not be null");
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        //TODO - 26.1: Should we only allow inserting to the output slot, and extracting from the input slot?
        // Or do we need some way to specify logic to have it get chosen
        int inserted = super.insert(resource, amount, transaction);
        if (inserted < amount) {
            // Insert any leftover into the rest of the handler
            inserted += this.output.insert(resource, amount - inserted, transaction, automationType);
        }
        return inserted;
    }
}