package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
    protected List<String> deserializeList(RegistryFriendlyByteBuf buffer) {
        return buffer.readList(FriendlyByteBuf::readUtf);
    }

    @Override
    protected void serializeListElement(RegistryFriendlyByteBuf buffer, String value) {
        buffer.writeUtf(value);
    }
}