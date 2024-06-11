package mekanism.common.attachments.containers;

import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public abstract class ComponentBackedContainer<TYPE, ATTACHED extends IAttachedContainers<TYPE, ATTACHED>> implements IContentsListener {

    protected final ItemStack attachedTo;
    protected final int containerIndex;

    protected ComponentBackedContainer(ItemStack attachedTo, int containerIndex) {
        this.attachedTo = attachedTo;
        this.containerIndex = containerIndex;
    }

    protected abstract TYPE copy(TYPE toCopy);

    protected abstract boolean isEmpty(TYPE value);

    protected abstract ContainerType<?, ATTACHED, ?> containerType();

    protected ATTACHED getAttached() {
        return containerType().getOrEmpty(attachedTo);
    }

    protected TYPE getContents(ATTACHED attached) {
        return attached.getOrDefault(containerIndex);
    }

    protected void setContents(ATTACHED attached, TYPE value) {
        //If we don't actually have an attachment present yet, we need to ensure we try to create a new one
        if (attached.isEmpty()) {
            //If we don't have an attachment, attempt to create a new one
            attached = containerType().createNewAttachment(attachedTo);
            if (attached.isEmpty()) {
                //If we can't figure out how to handle the attachment for the item, just exit
                // Note: We don't need to consider removing an existing attachment as we know we don't have one
                return;
            }
        }
        if (shouldUpdate(attached, value)) {
            attachedTo.set(containerType().getComponentType(), attached.with(containerIndex, copy(value)));
            onContentsChanged();
        }
    }

    protected boolean shouldUpdate(ATTACHED attached, TYPE value) {
        //If both stacks are empty we don't do anything
        //TODO - 1.21: Do we want to do a matches check instead of just seeing if both are empty
        // Or maybe only do that in the non overloaded setStack so as a way to potentially avoid the extra lookup here when we know
        // we only call this method if something has changed
        return !isEmpty(value) || !isEmpty(getContents(attached));
    }

    @Override
    public void onContentsChanged() {
    }
}