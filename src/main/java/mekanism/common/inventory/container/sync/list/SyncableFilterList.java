package mekanism.common.inventory.container.sync.list;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.lib.collection.HashList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling filter lists
 */
public class SyncableFilterList<FILTER extends IFilter<?>> extends SyncableCollection<FILTER, Collection<FILTER>> {

    public static <FILTER extends IFilter<?>> SyncableFilterList<FILTER> create(Supplier<@NotNull Collection<FILTER>> getter, Consumer<@NotNull Collection<FILTER>> setter) {
        return new SyncableFilterList<>(getter, setter);
    }

    private SyncableFilterList(Supplier<@NotNull Collection<FILTER>> getter, Consumer<@NotNull Collection<FILTER>> setter) {
        super(getter, setter);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Collection<FILTER> deserializeList(RegistryFriendlyByteBuf buffer) {
        return buffer.readCollection(HashList::new, buf -> (FILTER) BaseFilter.GENERIC_STREAM_CODEC.decode(buffer));
    }

    @Override
    protected void serializeListElement(RegistryFriendlyByteBuf buffer, FILTER filter) {
        BaseFilter.GENERIC_STREAM_CODEC.encode(buffer, filter);
    }
}