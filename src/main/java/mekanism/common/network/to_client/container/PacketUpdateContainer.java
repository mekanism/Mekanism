package mekanism.common.network.to_client.container;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketUpdateContainer implements IMekanismPacket {

    //Note: windowId gets transferred over the network as an unsigned byte
    private final short windowId;
    private final List<PropertyData> data;

    public PacketUpdateContainer(short windowId, List<PropertyData> data) {
        this.windowId = windowId;
        this.data = data;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        LocalPlayer player = Minecraft.getInstance().player;
        //Ensure that the container is one of ours and that the window id is the same as we expect it to be
        if (player != null && player.containerMenu instanceof MekanismContainer container && container.containerId == windowId) {
            //If so then handle the packet
            data.forEach(data -> data.handleWindowProperty(container));
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeByte(windowId);
        buffer.writeCollection(data, (buf, data) -> data.writeToPacket(buf));
    }

    public static PacketUpdateContainer decode(FriendlyByteBuf buffer) {
        short windowId = buffer.readUnsignedByte();
        int size = buffer.readVarInt();
        List<PropertyData> data = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            PropertyData propertyData = PropertyData.fromBuffer(buffer);
            if (propertyData != null) {
                data.add(propertyData);
            }
        }
        return new PacketUpdateContainer(windowId, data);
    }
}