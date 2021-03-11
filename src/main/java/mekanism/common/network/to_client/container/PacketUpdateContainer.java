package mekanism.common.network.to_client.container;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
        ClientPlayerEntity player = Minecraft.getInstance().player;
        //Ensure that the container is one of ours and that the window id is the same as we expect it to be
        if (player != null && player.containerMenu instanceof MekanismContainer && player.containerMenu.containerId == windowId) {
            //If so then handle the packet
            data.forEach(data -> data.handleWindowProperty((MekanismContainer) player.containerMenu));
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeByte(windowId);
        buffer.writeVarInt(data.size());
        for (PropertyData data : data) {
            data.writeToPacket(buffer);
        }
    }

    public static PacketUpdateContainer decode(PacketBuffer buffer) {
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