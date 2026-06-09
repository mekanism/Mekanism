package mekanism.common.lib.inventory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public abstract class CollectionTransitRequest extends TransitRequest {

    protected abstract Collection<? extends ItemData> getItemData();

    @Override
    public boolean isEmpty() {
        return getItemData().isEmpty();
    }

    @Override
    public Iterator<ItemData> iterator() {
        return (Iterator<ItemData>) getItemData().iterator();
    }

    @Override
    public void forEach(Consumer<? super ItemData> action) {
        getItemData().forEach(action);
    }

    @Override
    public Spliterator<ItemData> spliterator() {
        return (Spliterator<ItemData>) getItemData().spliterator();
    }
}