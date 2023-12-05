package mekanism.common.network.to_server;

import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.lib.security.SecurityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;

public class PacketSecurityMode implements IMekanismPacket {

    private final InteractionHand currentHand;
    private final boolean increment;

    public PacketSecurityMode(InteractionHand hand, boolean increment) {
        this.currentHand = hand;
        this.increment = increment;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            ItemStack stack = player.getItemInHand(currentHand);
            if (increment) {
                SecurityUtils.get().incrementSecurityMode(player, IItemSecurityUtils.INSTANCE.securityCapability(stack));
            } else {
                SecurityUtils.get().decrementSecurityMode(player, IItemSecurityUtils.INSTANCE.securityCapability(stack));
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(currentHand);
        buffer.writeBoolean(increment);
    }

    public static PacketSecurityMode decode(FriendlyByteBuf buffer) {
        return new PacketSecurityMode(buffer.readEnum(InteractionHand.class), buffer.readBoolean());
    }
}