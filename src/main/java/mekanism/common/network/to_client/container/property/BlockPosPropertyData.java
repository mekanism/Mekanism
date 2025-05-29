package mekanism.common.network.to_client.container.property;

import io.netty.buffer.ByteBuf;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class BlockPosPropertyData extends PropertyData {

    public static final StreamCodec<ByteBuf, BlockPosPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          BlockPos.STREAM_CODEC, data -> data.value,
          BlockPosPropertyData::new
    );

    @Nullable
    private final BlockPos value;

    public BlockPosPropertyData(short property, @Nullable BlockPos value) {
        super(PropertyType.BLOCK_POS, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }
}