package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;

public class RegistryEntryPropertyData<V> extends PropertyData {

    private final Registry<V> registry;
    private final V value;
    private int backingId;

    public RegistryEntryPropertyData(short property, Registry<V> registry, V value) {
        super(PropertyType.REGISTRY_ENTRY, property);
        this.registry = registry;
        this.value = value;
    }

    private RegistryEntryPropertyData(short property, int backingId) {
        this(property, null, null);
        this.backingId = backingId;
    }

    public static <V> RegistryEntryPropertyData<V> readRegistryEntry(short property, FriendlyByteBuf buffer) {
        //Note: We only read the id as writeId just writes the id for the registry as a varint,
        // and we don't have easy access to the registry on the receiving side, so when we handle the property
        // we will let the SyncableRegistryEntry handle looking up the id in the registry
        // (the same as FriendlyByteBuf#readId would be doing)
        return new RegistryEntryPropertyData<>(property, buffer.readVarInt());
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), backingId);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        super.writeToPacket(buffer);
        buffer.writeId(registry, value);
    }
}