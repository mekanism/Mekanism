package mekanism.common.network.to_client.container.property;

import io.netty.buffer.ByteBuf;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class LongPropertyData extends PropertyData {

    public static final StreamCodec<ByteBuf, LongPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ByteBufCodecs.VAR_LONG, data -> data.value,
          LongPropertyData::new
    );

    private final long value;

    public LongPropertyData(short property, long value) {
        super(PropertyType.LONG, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }
}