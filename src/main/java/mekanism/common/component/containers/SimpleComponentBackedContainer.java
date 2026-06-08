package mekanism.common.component.containers;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.util.ItemAccessUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public abstract class SimpleComponentBackedContainer<ATTACHED> {

    protected final ItemAccess attachedAccess;

    protected SimpleComponentBackedContainer(ItemAccess attachedAccess) {
        this.attachedAccess = attachedAccess;
    }

    protected abstract boolean isEmpty(ATTACHED value);

    protected abstract IContainerType<?, ATTACHED> containerType();

    protected ATTACHED getAttached() {
        return containerType().getOrEmpty(attachedAccess.getResource());
    }

    protected boolean setContents(ATTACHED value, @Nullable TransactionContext transaction) {
        ItemResource attachedTo = attachedAccess.getResource();
        if (attachedTo.isEmpty()) {
            //If the backing item has become empty, just exit and return that we couldn't set the contents
            return false;
        }
        //Note: The attached access should handle snapshotting the backing stack
        //If anything changed in the item access, that means it was able to perform the transfer, so return that things changed from the call to setContents
        return ItemAccessUtils.exchange(attachedAccess, attachedTo.with(containerType().getComponentType(), value), transaction);
    }
}