package mekanism.common.network.to_server;

import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.SecurityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PacketSecurityMode implements IMekanismPacket {

    private final InteractionHand currentHand;

    public PacketSecurityMode(InteractionHand hand) {
        currentHand = hand;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            SecurityUtils.INSTANCE.incrementSecurityMode(player, player.getItemInHand(currentHand));
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(currentHand);
    }

    public static PacketSecurityMode decode(FriendlyByteBuf buffer) {
        return new PacketSecurityMode(buffer.readEnum(InteractionHand.class));
    }
}