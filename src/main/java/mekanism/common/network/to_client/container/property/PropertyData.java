package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class PropertyData {

    public static final StreamCodec<RegistryFriendlyByteBuf, PropertyData> GENERIC_STREAM_CODEC = PropertyType.STREAM_CODEC.<RegistryFriendlyByteBuf>cast()
          .dispatch(PropertyData::getType, PropertyType::streamCodec);

    private final PropertyType type;
    private final short property;

    protected PropertyData(PropertyType type, short property) {
        this.type = type;
        this.property = property;
    }

    public PropertyType getType() {
        return type;
    }

    public short getProperty() {
        return property;
    }

    public abstract void handleWindowProperty(MekanismContainer container);
}