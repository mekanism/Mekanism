package mekanism.common.network.container;

import java.util.function.Supplier;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.property.PropertyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketUpdateContainer {

    //Note: windowId gets transferred over the network as an unsigned byte
    protected final short windowId;
    protected final short property;
    protected final PropertyData data;

    public PacketUpdateContainer(short windowId, short property, PropertyData data) {
        this.windowId = windowId;
        this.property = property;
        this.data = data;
    }

    public static void handle(PacketUpdateContainer message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            //Ensure that the container is one of ours and that the window id is the same as we expect it to be
            if (player != null && player.openContainer instanceof MekanismContainer && player.openContainer.windowId == message.windowId) {
                //If so then handle the packet
                message.data.handleWindowProperty((MekanismContainer) player.openContainer);
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketUpdateContainer pkt, PacketBuffer buffer) {
        buffer.writeByte(pkt.windowId);
        buffer.writeShort(pkt.property);
        pkt.data.writeToPacket(buffer);
    }

    public static PacketUpdateContainer decode(PacketBuffer buffer) {
        short windowId = buffer.readUnsignedByte();
        short property = buffer.readShort();
        PropertyData data = PropertyData.fromBuffer(buffer);
        return new PacketUpdateContainer(windowId, property, data);
    }
}