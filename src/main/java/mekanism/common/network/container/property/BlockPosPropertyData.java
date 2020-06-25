package mekanism.common.network.container.property;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.PacketUpdateContainerBlockPos;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class BlockPosPropertyData extends PropertyData {

    @Nullable
    private final BlockPos value;

    public BlockPosPropertyData(short property, @Nullable BlockPos value) {
        super(PropertyType.BLOCK_POS, property);
        this.value = value;
    }

    @Override
    public PacketUpdateContainerBlockPos getSinglePacket(short windowId) {
        return new PacketUpdateContainerBlockPos(windowId, getProperty(), value);
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        if (value == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeBlockPos(value);
        }
    }
}