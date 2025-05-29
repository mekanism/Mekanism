package mekanism.common.network.to_client.container.property;

import io.netty.buffer.ByteBuf;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class BytePropertyData extends PropertyData {

    public static final StreamCodec<ByteBuf, BytePropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ByteBufCodecs.BYTE, data -> data.value,
          BytePropertyData::new
    );

    private final byte value;

    public BytePropertyData(short property, byte value) {
        super(PropertyType.BYTE, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }
}