package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.container.property.list.ListPropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling lists
 */
public abstract class SyncableList<TYPE> implements ISyncableData {

    private final Supplier<@NonNull List<TYPE>> getter;
    private final Consumer<@NonNull List<TYPE>> setter;
    private int lastKnownHashCode;

    protected SyncableList(Supplier<@NonNull List<TYPE>> getter, Consumer<@NonNull List<TYPE>> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Nonnull
    public List<TYPE> get() {
        return getter.get();
    }

    public void set(@Nonnull List<TYPE> value) {
        setter.accept(value);
    }

    @Override
    public abstract ListPropertyData<TYPE> getPropertyData(short property, DirtyType dirtyType);

    @Override
    public DirtyType isDirty() {
        List<TYPE> values = get();
        int valuesHashCode = values.hashCode();
        if (lastKnownHashCode == valuesHashCode) {
            return DirtyType.CLEAN;
        }
        //TODO: Create a way to declare changes so we don't have to sync the entire list, when a single element changes
        // Both for removal as well as addition
        lastKnownHashCode = valuesHashCode;
        return DirtyType.DIRTY;
    }
}