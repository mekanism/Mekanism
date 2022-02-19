package mekanism.common.inventory.container.sync.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.to_client.container.property.list.ListPropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling lists
 */
public abstract class SyncableList<TYPE> implements ISyncableData {

    private final Supplier<@NonNull ? extends Collection<TYPE>> getter;
    private final Consumer<@NonNull List<TYPE>> setter;
    private int lastKnownHashCode;

    protected SyncableList(Supplier<@NonNull ? extends Collection<TYPE>> getter, Consumer<@NonNull List<TYPE>> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Nonnull
    public List<TYPE> get() {
        Collection<TYPE> collection = getRaw();
        if (collection instanceof List) {
            return (List<TYPE>) collection;
        }
        return new ArrayList<>(collection);
    }

    @Nonnull
    protected Collection<TYPE> getRaw() {
        return getter.get();
    }

    protected int getValueHashCode() {
        return getRaw().hashCode();
    }

    public void set(@Nonnull List<TYPE> value) {
        setter.accept(value);
    }

    @Override
    public abstract ListPropertyData<TYPE> getPropertyData(short property, DirtyType dirtyType);

    @Override
    public DirtyType isDirty() {
        int valuesHashCode = getValueHashCode();
        if (lastKnownHashCode == valuesHashCode) {
            return DirtyType.CLEAN;
        }
        //TODO: Create a way to declare changes so we don't have to sync the entire list, when a single element changes
        // Both for removal as well as addition. Note that GuiFrequencySelector makes some assumptions based on the fact
        // that this is not currently possible so a new list will occur each time
        lastKnownHashCode = valuesHashCode;
        return DirtyType.DIRTY;
    }
}