package mekanism.common.attachments.containers;

import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class ComponentBackedContainer<TYPE, ATTACHED extends IAttachedContainers<TYPE, ATTACHED>> implements IContentsListener {

    protected final ItemStack attachedTo;
    protected final int containerIndex;

    protected ComponentBackedContainer(ItemStack attachedTo, int containerIndex) {
        this.attachedTo = attachedTo;
        this.containerIndex = containerIndex;
    }

    protected abstract TYPE copy(TYPE toCopy);

    protected abstract boolean isEmpty(TYPE value);

    protected abstract Supplier<? extends DataComponentType<ATTACHED>> dataComponentType();

    @Nullable
    protected ATTACHED getAttached() {
        return attachedTo.get(dataComponentType());
    }

    protected TYPE getContents(ATTACHED attached) {
        return attached.get(containerIndex);
    }

    protected void setContents(TYPE value) {
        //TODO - 1.20.5: Comment about why we have the overload that accepts attachedItems
        ATTACHED attached = getAttached();
        if (attached != null) {
            setContents(attached, value);
        }
        //TODO - 1.20.5: Else initialize to whatever the default size is meant to be?
        // I think we might always end up actually doing so when accessing this from the container type?
        // Though maybe not if it isn't going through capability systems
    }

    protected void setContents(ATTACHED attached, TYPE value) {
        //If both stacks are empty we don't do anything
        if (!isEmpty(value) || !isEmpty(getContents(attached))) {
            //TODO - 1.20.5: Do we want to do a matches check instead of just seeing if both are empty
            // Or maybe only do that in the non overloaded setStack so as a way to potentially avoid the extra lookup here when we know
            // we only call this method if something has changed
            attachedTo.set(dataComponentType(), attached.with(containerIndex, copy(value)));
            onContentsChanged();
        }
    }

    @Override
    public void onContentsChanged() {
    }
}