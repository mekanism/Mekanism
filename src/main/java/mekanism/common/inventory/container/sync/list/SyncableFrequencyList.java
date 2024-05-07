package mekanism.common.inventory.container.sync.list;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling frequency lists
 */
public class SyncableFrequencyList<FREQUENCY extends Frequency> extends SyncableList<FREQUENCY> {

    public static <FREQUENCY extends Frequency> SyncableFrequencyList<FREQUENCY> create(FrequencyType<FREQUENCY> type, Supplier<? extends @NotNull Collection<FREQUENCY>> getter,
          Consumer<@NotNull List<FREQUENCY>> setter) {
        return new SyncableFrequencyList<>(type, getter, setter);
    }

    private final FrequencyType<FREQUENCY> type;

    private SyncableFrequencyList(FrequencyType<FREQUENCY> type, Supplier<? extends @NotNull Collection<FREQUENCY>> getter, Consumer<@NotNull List<FREQUENCY>> setter) {
        super(getter, setter);
        this.type = type;
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
    protected List<FREQUENCY> deserializeList(RegistryFriendlyByteBuf buffer) {
        return buffer.readList(buf -> type.create(buffer));
    }

    @Override
    protected void serializeListElement(RegistryFriendlyByteBuf buffer, FREQUENCY frequency) {
        type.streamCodec().encode(buffer, frequency);
    }
}