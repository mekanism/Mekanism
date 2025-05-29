package mekanism.common.network.to_client.container.property;

import io.netty.buffer.ByteBuf;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ShortPropertyData extends PropertyData {

    public static final StreamCodec<ByteBuf, ShortPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ByteBufCodecs.SHORT, data -> data.value,
          ShortPropertyData::new
    );

    private final short value;

    public ShortPropertyData(short property, short value) {
        super(PropertyType.SHORT, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }
}