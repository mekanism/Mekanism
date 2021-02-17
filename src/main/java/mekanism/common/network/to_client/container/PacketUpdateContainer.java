package mekanism.common.network.to_client.container;

import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketUpdateContainer implements IMekanismPacket {

    //Note: windowId gets transferred over the network as an unsigned byte
    protected final short windowId;
    protected final short property;
    protected final PropertyData data;

    public PacketUpdateContainer(short windowId, short property, PropertyData data) {
        this.windowId = windowId;
        this.property = property;
        this.data = data;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        //Ensure that the container is one of ours and that the window id is the same as we expect it to be
        if (player != null && player.openContainer instanceof MekanismContainer && player.openContainer.windowId == windowId) {
            //If so then handle the packet
            data.handleWindowProperty((MekanismContainer) player.openContainer);
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeByte(windowId);
        buffer.writeShort(property);
        data.writeToPacket(buffer);
    }

    public static PacketUpdateContainer decode(PacketBuffer buffer) {
        short windowId = buffer.readUnsignedByte();
        short property = buffer.readShort();
        PropertyData data = PropertyData.fromBuffer(buffer);
        return new PacketUpdateContainer(windowId, property, data);
    }
}