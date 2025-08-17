package mekanism.common.network.to_client.container.property;

import io.netty.buffer.ByteBuf;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class DoublePropertyData extends PropertyData {

    public static final StreamCodec<ByteBuf, DoublePropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ByteBufCodecs.DOUBLE, data -> data.value,
          DoublePropertyData::new
    );

    private final double value;

    public DoublePropertyData(short property, double value) {
        super(PropertyType.DOUBLE, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }
}