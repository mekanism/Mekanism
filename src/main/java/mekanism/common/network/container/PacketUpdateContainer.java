package mekanism.common.network.container;

import java.util.function.Supplier;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public abstract class PacketUpdateContainer<PACKET extends PacketUpdateContainer<PACKET>> {

    //Note: windowId gets transferred over the network as an unsigned byte
    protected final short windowId;
    //TODO: Debate passing this over the network as an unsigned byte as this is value is bounded by
    // our max number of properties in a single container which I don't think will ever be super high
    protected final short property;

    protected PacketUpdateContainer(short windowId, short property) {
        this.windowId = windowId;
        this.property = property;
    }

    protected PacketUpdateContainer(PacketBuffer buffer) {
        this.windowId = buffer.readUnsignedByte();
        this.property = buffer.readShort();
    }

    protected void encode(PacketBuffer buffer) {
        buffer.writeByte(windowId);
        buffer.writeShort(property);
    }

    protected abstract void handle(MekanismContainer container, PACKET packet);

    public static void handle(PacketUpdateContainer message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            PlayerEntity player = BasePacketHandler.getPlayer(context);
            //Ensure that the container is one of ours and that the window id is the same as we expect it to be
            if (player.openContainer instanceof MekanismContainer && player.openContainer.windowId == message.windowId) {
                //If so then handle the packet
                message.handle((MekanismContainer) player.openContainer, message);
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketUpdateContainer<?> pkt, PacketBuffer buffer) {
        pkt.encode(buffer);
    }
}