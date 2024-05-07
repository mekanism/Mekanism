package mekanism.common.inventory.container.sync.list;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.container.property.ByteArrayPropertyData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling Collections
 */
public abstract class SyncableCollection<TYPE, COLLECTION extends Collection<TYPE>> implements ISyncableData {

    private final Supplier<? extends @NotNull Collection<TYPE>> getter;
    private final Consumer<@NotNull COLLECTION> setter;
    private int lastKnownHashCode;

    protected SyncableCollection(Supplier<? extends @NotNull Collection<TYPE>> getter, Consumer<@NotNull COLLECTION> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @NotNull
    public Collection<TYPE> get() {
        return getRaw();
    }

    @NotNull
    protected Collection<TYPE> getRaw() {
        return getter.get();
    }

    protected int getValueHashCode() {
        return getRaw().hashCode();
    }

    public void set(RegistryAccess registryAccess, byte[] rawData) {
        setter.accept(PacketUtils.read(registryAccess, rawData, this::deserializeList));
    }

    protected abstract COLLECTION deserializeList(RegistryFriendlyByteBuf buffer);

    protected abstract void serializeListElement(RegistryFriendlyByteBuf buffer, TYPE element);

    @Override
    public ByteArrayPropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        //Note: We write it to a byte array so that we make sure to effectively copy it (force a serialization and deserialization)
        // whenever we send this as a packet rather than potentially allowing the list to leak from one side to the other in single player
        byte[] rawData = FriendlyByteBufUtil.writeCustomData(buffer -> buffer.writeCollection(getRaw(), (buf, element) -> serializeListElement(buffer, element)), registryAccess);
        return new ByteArrayPropertyData(property, rawData);
    }

    @Override
    public DirtyType isDirty() {
        int valuesHashCode = getValueHashCode();
        if (lastKnownHashCode == valuesHashCode) {
            return DirtyType.CLEAN;
        }
        //TODO: Create a way to declare changes so we don't have to sync the entire list, when a single element changes
        // Both for removal as well as addition. Note that GuiFrequencySelector makes some assumptions based on the fact
        // that this is not currently possible so a new list will occur each time
        lastKnownHashCode = valuesHashCode;
        return DirtyType.DIRTY;
    }
}