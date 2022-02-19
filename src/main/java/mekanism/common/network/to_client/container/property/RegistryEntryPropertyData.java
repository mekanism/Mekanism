package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegistryEntryPropertyData<V extends IForgeRegistryEntry<V>> extends PropertyData {

    private final V value;

    public RegistryEntryPropertyData(short property, V value) {
        super(PropertyType.REGISTRY_ENTRY, property);
        this.value = value;
    }

    public static <V extends IForgeRegistryEntry<V>> RegistryEntryPropertyData<V> readRegistryEntry(short property, PacketBuffer buffer) {
        return new RegistryEntryPropertyData<V>(property, buffer.readRegistryId());
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        buffer.writeRegistryId(value);
    }
}