package mekanism.common.network.to_client.container.property;

import io.netty.buffer.ByteBuf;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class IntPropertyData extends PropertyData {

    public static final StreamCodec<ByteBuf, IntPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ByteBufCodecs.VAR_INT, data -> data.value,
          IntPropertyData::new
    );

    private final int value;

    public IntPropertyData(short property, int value) {
        super(PropertyType.INT, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }
}