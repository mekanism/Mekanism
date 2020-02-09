package mekanism.common.network.container;

import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

//TODO: Make a packet for batching the updates together
// Structure of the packet would be:
// windowId
// length of things updating
// property index
// enum type updating
// that type of object (the enum could contain how to write/read it to/from packet)
public abstract class PacketUpdateContainer<PACKET extends PacketUpdateContainer<PACKET>> {

    //Note: windowId gets transferred over the network as an unsigned byte
    protected final short windowId;
    //TODO: Do we want to convert property to also being an unsigned byte as I highly doubt we will have
    // more than an unsigned bytes worth of one type of property
    // This is because property really is an "index" for which tracker of a given type this is for
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

    public static <PACKET extends PacketUpdateContainer<PACKET>> void handle(PACKET message, Supplier<Context> context) {
        Context ctx = context.get();
        if (ctx.getDirection().getReceptionSide().isClient()) {
            //Only handle and mark the packet as handled if we are on the client
            ctx.enqueueWork(() -> {
                PlayerEntity player = Mekanism.proxy.getPlayer(context);
                //Ensure that the container is one of ours and that the window id is the same as we expect it to be
                if (player.openContainer instanceof MekanismContainer && player.openContainer.windowId == message.windowId) {
                    //If so then handle the packet
                    message.handle((MekanismContainer) player.openContainer, message);
                }
            });
            ctx.setPacketHandled(true);
        }
    }

    public static <PACKET extends PacketUpdateContainer<PACKET>> void encode(PACKET pkt, PacketBuffer buffer) {
        pkt.encode(buffer);
    }
}