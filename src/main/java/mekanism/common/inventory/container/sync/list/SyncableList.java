package mekanism.common.inventory.container.sync.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling lists
 */
public abstract class SyncableList<TYPE> extends SyncableCollection<TYPE, List<TYPE>> {

    protected SyncableList(Supplier<? extends Collection<TYPE>> getter, Consumer<List<TYPE>> setter) {
        super(getter, setter);
    }

    @Override
    public List<TYPE> get() {
        Collection<TYPE> collection = getRaw();
        return collection instanceof List<TYPE> list ? list : new ArrayList<>(collection);
    }
}