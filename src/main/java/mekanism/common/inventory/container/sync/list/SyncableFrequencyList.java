package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.annotations.NonNull;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.network.container.property.list.FrequencyListPropertyData;
import mekanism.common.network.container.property.list.ListPropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling frequency lists
 */
public class SyncableFrequencyList<FREQUENCY extends Frequency> extends SyncableList<FREQUENCY> {

    public static <FREQUENCY extends Frequency> SyncableFrequencyList<FREQUENCY> create(Supplier<@NonNull List<FREQUENCY>> getter, Consumer<@NonNull List<FREQUENCY>> setter) {
        return new SyncableFrequencyList<>(getter, setter);
    }

    private SyncableFrequencyList(Supplier<@NonNull List<FREQUENCY>> getter, Consumer<@NonNull List<FREQUENCY>> setter) {
        super(getter, setter);
    }

    @Override
    public ListPropertyData<FREQUENCY> getPropertyData(short property, DirtyType dirtyType) {
        return new FrequencyListPropertyData<>(property, get());
    }
}