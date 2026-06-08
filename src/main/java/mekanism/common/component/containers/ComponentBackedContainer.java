package mekanism.common.component.containers;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.util.ItemAccessUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public abstract class ComponentBackedContainer<TYPE, ATTACHED extends IAttachedContainers<TYPE, ATTACHED>> {

    protected final ItemAccess attachedAccess;
    protected final int containerIndex;

    protected ComponentBackedContainer(ItemAccess attachedAccess, int containerIndex) {
        this.attachedAccess = attachedAccess;
        this.containerIndex = containerIndex;
    }

    protected abstract boolean isEmpty(TYPE value);

    protected abstract IContainerType<?, ATTACHED> containerType();

    protected ATTACHED getAttached() {
        return containerType().getOrEmpty(attachedAccess.getResource());
    }

    protected abstract TYPE getContents(ATTACHED attached);

    protected boolean setContents(ATTACHED attached, TYPE value, @Nullable TransactionContext transaction, boolean checkChanged) {
        ItemResource attachedTo = attachedAccess.getResource();
        if (attachedTo.isEmpty()) {
            //If the backing item has become empty, just exit and return that we couldn't set the contents
            return false;
        }
        //If we don't actually have an attachment present yet, we need to ensure we try to create a new one
        if (attached.isEmpty()) {
            //If we don't have an attachment, attempt to create a new one
            attached = containerType().createNewAttachment(attachedTo);
            if (attached.isEmpty()) {
                //If we can't figure out how to handle the attachment for the item, just exit
                // Note: We don't need to consider removing an existing attachment as we know we don't have one
                return false;
            }
        }
        if (checkChanged && getContents(attached).equals(value)) {
            //Nothing to change, just return false
            return false;
        }
        //Note: The attached access should handle snapshotting the backing stack
        //If anything changed in the item access, that means it was able to perform the transfer, so return that things changed from the call to setContents
        return ItemAccessUtils.exchange(attachedAccess, attachedTo.with(containerType().getComponentType(), attached.with(containerIndex, value)), transaction);
    }
}