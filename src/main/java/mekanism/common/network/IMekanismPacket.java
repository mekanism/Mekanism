package mekanism.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public interface IMekanismPacket {

    void handle(NetworkEvent.Context context);

    void encode(FriendlyByteBuf buffer);
}