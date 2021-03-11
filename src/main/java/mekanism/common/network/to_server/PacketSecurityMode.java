package mekanism.common.network.to_server;

import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.lib.security.SecurityMode;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSecurityMode implements IMekanismPacket {

    private final Hand currentHand;
    private final SecurityMode value;

    public PacketSecurityMode(Hand hand, SecurityMode control) {
        currentHand = hand;
        value = control;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player != null) {
            ItemStack stack = player.getItemInHand(currentHand);
            if (stack.getItem() instanceof ISecurityItem) {
                ((ISecurityItem) stack.getItem()).setSecurity(stack, value);
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(currentHand);
        buffer.writeEnum(value);
    }

    public static PacketSecurityMode decode(PacketBuffer buffer) {
        return new PacketSecurityMode(buffer.readEnum(Hand.class), buffer.readEnum(SecurityMode.class));
    }
}