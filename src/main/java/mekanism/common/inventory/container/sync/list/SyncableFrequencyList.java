package mekanism.common.inventory.container.sync.list;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.annotations.NonNull;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.network.to_client.container.property.list.FrequencyListPropertyData;
import mekanism.common.network.to_client.container.property.list.ListPropertyData;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling frequency lists
 */
public class SyncableFrequencyList<FREQUENCY extends Frequency> extends SyncableList<FREQUENCY> {

    public static <FREQUENCY extends Frequency> SyncableFrequencyList<FREQUENCY> create(Supplier<@NonNull ? extends Collection<FREQUENCY>> getter,
          Consumer<@NonNull List<FREQUENCY>> setter) {
        return new SyncableFrequencyList<>(getter, setter);
    }

    private SyncableFrequencyList(Supplier<@NonNull ? extends Collection<FREQUENCY>> getter, Consumer<@NonNull List<FREQUENCY>> setter) {
        super(getter, setter);
    }

    @Override
    protected int getValueHashCode() {
        //Reimplementation/copy of AbstractList#hashCode, which is what we would have if we wrapped the
        // collection into an ArrayList, but we want to avoid creating so many excess objects, so we just
        // implement it directly here
        int hashCode = 1;
        for (FREQUENCY frequency : getRaw()) {
            hashCode = 31 * hashCode + frequency.hashCode();
        }
        return hashCode;
    }

    @Override
    public ListPropertyData<FREQUENCY> getPropertyData(short property, DirtyType dirtyType) {
        return new FrequencyListPropertyData<>(property, get());
    }
}