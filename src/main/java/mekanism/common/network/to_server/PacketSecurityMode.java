package mekanism.common.network.to_server;

import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.lib.security.SecurityMode;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

public class PacketSecurityMode implements IMekanismPacket {

    private final InteractionHand currentHand;
    private final SecurityMode value;

    public PacketSecurityMode(InteractionHand hand, SecurityMode control) {
        currentHand = hand;
        value = control;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            ItemStack stack = player.getItemInHand(currentHand);
            if (stack.getItem() instanceof ISecurityItem) {
                ((ISecurityItem) stack.getItem()).setSecurity(stack, value);
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(currentHand);
        buffer.writeEnum(value);
    }

    public static PacketSecurityMode decode(FriendlyByteBuf buffer) {
        return new PacketSecurityMode(buffer.readEnum(InteractionHand.class), buffer.readEnum(SecurityMode.class));
    }
}