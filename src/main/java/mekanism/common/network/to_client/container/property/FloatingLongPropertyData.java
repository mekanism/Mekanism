package mekanism.common.network.to_client.container.property;

import io.netty.buffer.ByteBuf;
import mekanism.api.math.FloatingLong;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class FloatingLongPropertyData extends PropertyData {

    public static final StreamCodec<ByteBuf, FloatingLongPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          FloatingLong.STREAM_CODEC, data -> data.value,
          FloatingLongPropertyData::new
    );

    @NotNull
    private final FloatingLong value;

    public FloatingLongPropertyData(short property, @NotNull FloatingLong value) {
        super(PropertyType.FLOATING_LONG, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }
}