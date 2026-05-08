package mekanism.common.attachments.containers;

import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

@NothingNullByDefault
public abstract class ComponentBackedContainer<TYPE, ATTACHED extends IAttachedContainers<TYPE, ATTACHED>> extends SnapshotJournal<TYPE> implements IContentsListener {

    protected final ItemStack attachedTo;
    protected final int containerIndex;

    protected ComponentBackedContainer(ItemStack attachedTo, int containerIndex) {
        this.attachedTo = attachedTo;
        this.containerIndex = containerIndex;
    }

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
            attachedTo.set(containerType().getComponentType(), attached.with(containerIndex, value));
            //TODO - 26.1: Do we want to be calling onContentsChanged here or should we instead just be marking the snapshot as taken here above setting it
            // and then don't call onContentsChanged here
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

    @Override
    protected TYPE createSnapshot() {
        return getContents(getAttached());
    }

    @Override
    protected void revertToSnapshot(TYPE snapshot) {
        setContents(getAttached(), snapshot);
    }

    @Override
    protected void onRootCommit(TYPE originalState) {
        //TODO - 26.1: Evaluate if shouldUpdate is a good metric for if we should be calling onContentsChanged here
        if (shouldUpdate(getAttached(), originalState)) {
            //Fire content change listeners during root commit if the final state is different from the original one
            onContentsChanged();
        }
    }
}