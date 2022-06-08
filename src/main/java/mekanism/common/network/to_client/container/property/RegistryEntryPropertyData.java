package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistry;

public class RegistryEntryPropertyData<V> extends PropertyData {

    private final IForgeRegistry<V> registry;
    private final V value;

    public RegistryEntryPropertyData(short property, IForgeRegistry<V> registry, V value) {
        super(PropertyType.REGISTRY_ENTRY, property);
        this.registry = registry;
        this.value = value;
    }

    public static <V> RegistryEntryPropertyData<V> readRegistryEntry(short property, FriendlyByteBuf buffer) {
        //Unused registry just do null for now
        return new RegistryEntryPropertyData<>(property, null, buffer.readRegistryId());
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        super.writeToPacket(buffer);
        buffer.writeRegistryId(registry, value);
    }
}