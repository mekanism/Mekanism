package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling filter lists
 */
public class SyncableFilterList<FILTER extends IFilter<?>> extends SyncableList<FILTER> {

    public static <FILTER extends IFilter<?>> SyncableFilterList<FILTER> create(Supplier<@NotNull List<FILTER>> getter, Consumer<@NotNull List<FILTER>> setter) {
        return new SyncableFilterList<>(getter, setter);
    }

    private SyncableFilterList(Supplier<@NotNull List<FILTER>> getter, Consumer<@NotNull List<FILTER>> setter) {
        super(getter, setter);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<FILTER> deserializeList(FriendlyByteBuf buffer) {
        return buffer.readList(buf -> (FILTER) BaseFilter.readFromPacket(buf));
    }

    @Override
    protected void serializeListElement(FriendlyByteBuf buffer, FILTER filter) {
        filter.write(buffer);
    }
}