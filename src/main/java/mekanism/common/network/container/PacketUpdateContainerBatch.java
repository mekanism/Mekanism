package mekanism.common.network.container;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.property.PropertyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketUpdateContainerBatch {

    //Note: windowId gets transferred over the network as an unsigned byte
    private final short windowId;
    private final List<PropertyData> data;

    public PacketUpdateContainerBatch(short windowId, List<PropertyData> data) {
        this.windowId = windowId;
        this.data = data;
    }

    public static void handle(PacketUpdateContainerBatch message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            //Ensure that the container is one of ours and that the window id is the same as we expect it to be
            if (player != null && player.openContainer instanceof MekanismContainer && player.openContainer.windowId == message.windowId) {
                //If so then handle the packet
                message.data.forEach(data -> data.handleWindowProperty((MekanismContainer) player.openContainer));
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketUpdateContainerBatch pkt, PacketBuffer buffer) {
        buffer.writeByte(pkt.windowId);
        buffer.writeVarInt(pkt.data.size());
        for (PropertyData data : pkt.data) {
            data.writeToPacket(buffer);
        }
    }

    public static PacketUpdateContainerBatch decode(PacketBuffer buffer) {
        short windowId = buffer.readUnsignedByte();
        int size = buffer.readVarInt();
        List<PropertyData> data = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            PropertyData propertyData = PropertyData.fromBuffer(buffer);
            if (propertyData != null) {
                data.add(propertyData);
            }
        }
        return new PacketUpdateContainerBatch(windowId, data);
    }
}