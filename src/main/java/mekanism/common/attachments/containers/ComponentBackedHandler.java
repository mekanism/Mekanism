package mekanism.common.attachments.containers;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.type.IContainerType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public abstract class ComponentBackedHandler<CONTAINER extends ValueIOSerializable, ATTACHED, CONTAINER_TYPE extends IContainerType<CONTAINER, ATTACHED>> {

    private final CONTAINER_TYPE containerType;
    protected final ItemAccess attachedAccess;
    @Nullable
    private final Item initialItemType;

    protected ComponentBackedHandler(CONTAINER_TYPE containerType, ItemAccess attachedAccess, boolean validateItemType) {
        this.containerType = containerType;
        this.attachedAccess = attachedAccess;
        this.initialItemType = validateItemType ? attachedAccess.getResource().getItem() : null;
    }

    protected final CONTAINER_TYPE containerType() {
        return containerType;
    }

    protected ATTACHED getAttached() {
        return containerType.getOrEmpty(attachedAccess.getResource());
    }

    protected boolean isAccessInvalid() {
        //If the amount available via the attached access is now zero, or if we validate the initial type and the item type has changed since we were created
        // consider this handler invalid and have the methods that interact with it NO-OP
        return attachedAccess.getAmount() == 0 || initialItemType != null && !attachedAccess.getResource().is(initialItemType);
    }
}