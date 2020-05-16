package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.lib.security.ISecurityTile.SecurityMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketSecurityMode {

    private final Hand currentHand;
    private final SecurityMode value;

    public PacketSecurityMode(Hand hand, SecurityMode control) {
        currentHand = hand;
        value = control;
    }

    public static void handle(PacketSecurityMode message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            ItemStack stack = player.getHeldItem(message.currentHand);
            if (stack.getItem() instanceof ISecurityItem) {
                ((ISecurityItem) stack.getItem()).setSecurity(stack, message.value);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketSecurityMode pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.currentHand);
        buf.writeEnumValue(pkt.value);
    }

    public static PacketSecurityMode decode(PacketBuffer buf) {
        return new PacketSecurityMode(buf.readEnumValue(Hand.class), buf.readEnumValue(SecurityMode.class));
    }
}