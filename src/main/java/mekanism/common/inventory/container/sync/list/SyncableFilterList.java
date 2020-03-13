package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.annotations.NonNull;
import mekanism.common.content.filter.IFilter;
import mekanism.common.network.container.property.list.FilterListPropertyData;
import mekanism.common.network.container.property.list.ListPropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling filter lists
 */
public class SyncableFilterList<FILTER extends IFilter<?>> extends SyncableList<FILTER> {

    public static <FILTER extends IFilter<?>> SyncableFilterList<FILTER> create(Supplier<@NonNull List<FILTER>> getter, Consumer<@NonNull List<FILTER>> setter) {
        return new SyncableFilterList<>(getter, setter);
    }

    private SyncableFilterList(Supplier<@NonNull List<FILTER>> getter, Consumer<@NonNull List<FILTER>> setter) {
        super(getter, setter);
    }

    @Override
    public ListPropertyData<FILTER> getPropertyData(short property, DirtyType dirtyType) {
        return new FilterListPropertyData<>(property, get());
    }
}