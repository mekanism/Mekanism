package mekanism.common.inventory.container.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

/// Version of [net.minecraft.world.inventory.DataSlot] for handling frequency lists
public class SyncableFrequencyList<FREQUENCY extends Frequency> extends SyncableStreamCodec<Collection<FREQUENCY>> {

    public static <FREQUENCY extends Frequency> SyncableFrequencyList<FREQUENCY> create(FrequencyType<FREQUENCY> type, Supplier<? extends Collection<FREQUENCY>> getter,
          Consumer<List<FREQUENCY>> setter) {
        return new SyncableFrequencyList<>(type, getter, setter);
    }

    private SyncableFrequencyList(FrequencyType<FREQUENCY> type, Supplier<? extends Collection<FREQUENCY>> getter, Consumer<List<FREQUENCY>> setter) {
        super(type.streamCodec().<RegistryFriendlyByteBuf>cast().apply(ByteBufCodecs.collection(ArrayList::new)), getter, collection -> {
            if (collection instanceof List<FREQUENCY> list) {
                setter.accept(list);
            } else {
                setter.accept(new ArrayList<>(collection));
            }
        });
    }

    @Override
    protected int getValueHashCode() {
        //Reimplementation/copy of AbstractList#hashCode, which is what we would have if we wrapped the
        // collection into an ArrayList, but we want to avoid creating so many excess objects, so we just
        // implement it directly here
        int hashCode = 1;
        for (FREQUENCY frequency : get()) {
            hashCode = 31 * hashCode + frequency.hashCode();
        }
        return hashCode;
    }
}