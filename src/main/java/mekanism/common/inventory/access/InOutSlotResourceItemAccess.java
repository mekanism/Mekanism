package mekanism.common.inventory.access;

import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.access.InventorySlotItemAccess;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.component.containers.type.ResourceContainerType;
import mekanism.common.inventory.slot.LastTransferDirection;
import mekanism.common.inventory.slot.LastTransferDirection.LastDirectionJournal;
import mekanism.common.util.ItemAccessUtils;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class InOutSlotResourceItemAccess<RESOURCE extends Resource> extends InventorySlotItemAccess {

    private final ItemCapability<ResourceHandler<RESOURCE>, ItemAccess> capability;
    private final LastDirectionJournal transferDirectionSupplier;
    private final IInventorySlot output;
    private final RESOURCE currentStoredContents;
    private final int currentTypeCapacity;

    public InOutSlotResourceItemAccess(IInventorySlot input, IInventorySlot output, ResourceContainerType<RESOURCE, ?> containerType,
          LastDirectionJournal transferDirectionSupplier, IResourceContainer<RESOURCE> container) {
        RESOURCE currentContents = container.resource();
        this(input, output, containerType.capability().item(), transferDirectionSupplier, currentContents, container.capacityAsInt(currentContents));
    }

    public InOutSlotResourceItemAccess(IInventorySlot input, IInventorySlot output, ItemCapability<ResourceHandler<RESOURCE>, ItemAccess> capability,
          LastDirectionJournal transferDirectionSupplier, RESOURCE currentStoredContents, int currentTypeCapacity) {
        super(input, AutomationType.INTERNAL);
        this.output = Objects.requireNonNull(output, "Output slot may not be null");
        this.capability = capability;
        this.transferDirectionSupplier = transferDirectionSupplier;
        this.currentStoredContents = currentStoredContents;
        this.currentTypeCapacity = currentTypeCapacity;
    }

    public ItemCapability<ResourceHandler<RESOURCE>, ItemAccess> getCapability() {
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
            ResourceHandler<RESOURCE> insertedHandler = ItemAccessUtils.sideEffectFreeAccess(resource).getCapability(capability);
            if (insertedHandler != null) {
                try (Transaction simulation = Transaction.open(transaction)) {
                    LastTransferDirection lastTransferDirection = transferDirectionSupplier.getDirection();
                    if (!currentStoredContents.isEmpty()) {
                        if (lastTransferDirection == LastTransferDirection.FILL_FROM_ITEM) {
                            if (insertedHandler.extract(currentStoredContents, currentTypeCapacity, simulation) > 0) {
                                //If anything can be extracted from the handler that is being inserted into our item access,
                                // that means we can treat it as still filling us, and try to insert it back into the input slot
                                targetSlot = this.slot;
                            }
                        } else if (lastTransferDirection == LastTransferDirection.DRAIN_INTO_ITEM && insertedHandler.insert(currentStoredContents, currentTypeCapacity, simulation) > 0) {
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