package mekanism.common.network.to_client.container.property;

import io.netty.buffer.ByteBuf;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class FloatPropertyData extends PropertyData {

    public static final StreamCodec<ByteBuf, FloatPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ByteBufCodecs.FLOAT, data -> data.value,
          FloatPropertyData::new
    );

    private final float value;

    public FloatPropertyData(short property, float value) {
        super(PropertyType.FLOAT, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }
}