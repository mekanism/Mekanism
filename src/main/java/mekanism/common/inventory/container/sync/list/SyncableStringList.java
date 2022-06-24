package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.network.to_client.container.property.list.ListPropertyData;
import mekanism.common.network.to_client.container.property.list.StringListPropertyData;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling string lists
 */
public class SyncableStringList extends SyncableList<String> {

    public static SyncableStringList create(Supplier<@NotNull List<String>> getter, Consumer<@NotNull List<String>> setter) {
        return new SyncableStringList(getter, setter);
    }

    private SyncableStringList(Supplier<@NotNull List<String>> getter, Consumer<@NotNull List<String>> setter) {
        super(getter, setter);
    }

    @Override
    public ListPropertyData<String> getPropertyData(short property, DirtyType dirtyType) {
        return new StringListPropertyData(property, get());
    }
}