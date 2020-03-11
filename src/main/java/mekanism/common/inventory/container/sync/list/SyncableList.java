package mekanism.common.inventory.container.sync.list;

import java.util.ArrayList;
import java.util.Collections;
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
    @Nonnull
    private List<TYPE> lastKnownValues = Collections.emptyList();

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
        int size = lastKnownValues.size();
        if (size == values.size()) {
            //Validate the elements actually match and we didn't just have one element get replaced
            boolean allMatch = true;
            for (int i = 0; i < size; i++) {
                TYPE lastKnownValue = lastKnownValues.get(i);
                TYPE value = values.get(i);
                if (!lastKnownValue.equals(value)) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                return DirtyType.CLEAN;
            }
        }
        //TODO: Create a way to declare changes so we don't have to sync the entire list, when a single element changes
        // Both for removal as well as addition
        //Note: We copy the values so that we don't always have it being identical.
        lastKnownValues = values.isEmpty() ? Collections.emptyList() : new ArrayList<>(values);
        return DirtyType.DIRTY;
    }
}