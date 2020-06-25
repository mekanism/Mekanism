package mekanism.common.network.container;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class PacketUpdateContainerBlockPos extends PacketUpdateContainer<PacketUpdateContainerBlockPos> {

    @Nullable
    private final BlockPos value;

    public PacketUpdateContainerBlockPos(short windowId, short property, @Nullable BlockPos value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerBlockPos(PacketBuffer buffer) {
        super(buffer);
        if (buffer.readBoolean()) {
            this.value = BlockPos.fromLong(buffer.readLong());
        } else {
            this.value = null;
        }
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        if (value == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeLong(value.toLong());
        }
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerBlockPos message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerBlockPos decode(PacketBuffer buf) {
        return new PacketUpdateContainerBlockPos(buf);
    }
}