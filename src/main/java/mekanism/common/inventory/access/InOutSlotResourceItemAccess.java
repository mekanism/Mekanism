package mekanism.common.inventory.access;

import java.util.Objects;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.access.InventorySlotItemAccess;
import mekanism.common.inventory.slot.ResourceHandlerSlot.LastTransferDirection;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;

@NothingNullByDefault
public class InOutSlotResourceItemAccess<RESOURCE extends Resource> extends InventorySlotItemAccess {

    private final ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> capability;
    private final Supplier<LastTransferDirection> transferDirectionSupplier;
    private final IInventorySlot output;
    private final RESOURCE currentStoredContents;

    public InOutSlotResourceItemAccess(IInventorySlot input, IInventorySlot output, ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> capability,
          Supplier<LastTransferDirection> transferDirectionSupplier, RESOURCE currentStoredContents) {
        super(input, AutomationType.INTERNAL);
        this.output = Objects.requireNonNull(output, "Output slot may not be null");
        this.capability = capability;
        this.transferDirectionSupplier = transferDirectionSupplier;
        //TODO - 26.1: Does this need to be a supplier?
        this.currentStoredContents = currentStoredContents;
    }

    public ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> getCapability() {
        return capability;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (amount == 0) {
            return 0;
        }
        if (getAmount() == 0) {
            //If we are currently empty, try to determine which slot we should be targeting
            IInventorySlot targetSlot = this.output;
            //Rely on our earlier logic for when we constructed the item access to determine whether the amount is one and that this is a oneByOne item access
            ResourceHandler<RESOURCE> insertedHandler = ItemAccess.forStack(resource.toStack(amount)).getCapability(capability);
            if (insertedHandler != null) {
                try (Transaction simulation = Transaction.open(transaction)) {
                    //TODO - 26.1: Re-evaluate last transfer direction handling
                    LastTransferDirection lastTransferDirection = transferDirectionSupplier.get();
                    if (!currentStoredContents.isEmpty()) {
                        //TODO - 26.1: Re-evaluate this amount
                        int amountToTransfer = amount * FluidType.BUCKET_VOLUME;
                        if (lastTransferDirection == LastTransferDirection.FILL_FROM_ITEM) {
                            if (insertedHandler.extract(currentStoredContents, amountToTransfer, simulation) > 0) {
                                //If anything can be extracted from the handler that is being inserted into our item access,
                                // that means we can treat it as still filling us, and try to insert it back into the input slot
                                targetSlot = this.slot;
                            }
                        } else if (lastTransferDirection == LastTransferDirection.DRAIN_INTO_ITEM && insertedHandler.insert(currentStoredContents, amountToTransfer, simulation) > 0) {
                            //If anything can be inserted into the handler that is being inserted into our item access,
                            // that means we can treat it as still being filled by us, and try to insert it back into the input slot
                            targetSlot = this.slot;
                        }
                    } else if (lastTransferDirection == LastTransferDirection.FILL_FROM_ITEM) {
                        for (int i = 0, size = insertedHandler.size(); i < size; i++) {
                            RESOURCE stored = insertedHandler.getResource(i);
                            if (!stored.isEmpty() && insertedHandler.extract(i, stored, insertedHandler.getAmountAsInt(i), simulation) > 0) {
                                //If anything can be extracted from the handler that is being inserted into our item access,
                                // that means we can treat it as still filling us, and try to insert it back into the input slot
                                targetSlot = this.slot;
                                break;
                            }
                        }
                    }
                }
            }
            return targetSlot.insert(resource, amount, transaction, automationType);
        }
        //Otherwise, try to insert into the input slot, and then fall back to inserting anything that didn't fit into the output slot
        int inserted = super.insert(resource, amount, transaction);
        if (inserted < amount) {
            // Insert any leftover into the rest of the handler
            inserted += this.output.insert(resource, amount - inserted, transaction, automationType);
        }
        return inserted;
    }
}