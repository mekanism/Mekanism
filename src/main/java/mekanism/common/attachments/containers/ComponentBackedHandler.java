package mekanism.common.attachments.containers;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.type.IContainerType;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.access.ItemAccess;

@NothingNullByDefault
public abstract class ComponentBackedHandler<CONTAINER extends ValueIOSerializable, ATTACHED, CONTAINER_TYPE extends IContainerType<CONTAINER, ATTACHED>> {

    private final CONTAINER_TYPE containerType;
    protected final ItemAccess attachedAccess;

    protected ComponentBackedHandler(CONTAINER_TYPE containerType, ItemAccess attachedAccess) {
        this.containerType = containerType;
        this.attachedAccess = attachedAccess;
    }

    protected final CONTAINER_TYPE containerType() {
        return containerType;
    }

    protected ATTACHED getAttached() {
        return containerType.getOrEmpty(attachedAccess);
    }

    protected boolean isAccessInvalid() {
        //TODO - 26.1: Should we have a predicate that checks the item type to see if it is still valid?
        // Probably, or maybe just store the initial item the access was on and only support it changing components but not the core type?
        return attachedAccess.getAmount() == 0;
    }
}