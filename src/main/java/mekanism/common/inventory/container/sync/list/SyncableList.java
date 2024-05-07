package mekanism.common.inventory.container.sync.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling lists
 */
public abstract class SyncableList<TYPE> extends SyncableCollection<TYPE, List<TYPE>> {

    protected SyncableList(Supplier<? extends @NotNull Collection<TYPE>> getter, Consumer<@NotNull List<TYPE>> setter) {
        super(getter, setter);
    }

    @NotNull
    @Override
    public List<TYPE> get() {
        Collection<TYPE> collection = getRaw();
        return collection instanceof List<TYPE> list ? list : new ArrayList<>(collection);
    }
}