package mekanism.common.network.to_client.container.property;

import io.netty.buffer.ByteBuf;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ByteArrayPropertyData extends PropertyData {

    public static final StreamCodec<ByteBuf, ByteArrayPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ByteBufCodecs.BYTE_ARRAY, data -> data.value,
          ByteArrayPropertyData::new
    );

    private final byte[] value;

    public ByteArrayPropertyData(short property, byte[] value) {
        super(PropertyType.BYTE_ARRAY, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }
}