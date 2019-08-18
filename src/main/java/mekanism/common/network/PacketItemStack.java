package mekanism.common.network;

import java.util.List;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.IItemNetwork;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class PacketItemStack {

    private List<Object> parameters;
    private PacketBuffer storedBuffer;
    private Hand currentHand;

    public PacketItemStack(Hand hand, List<Object> params) {
        currentHand = hand;
        parameters = params;
    }

    private PacketItemStack(Hand hand, PacketBuffer storedBuffer) {
        currentHand = hand;
        this.storedBuffer = storedBuffer;
    }

    public static void handle(PacketItemStack message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            ItemStack stack = player.getHeldItem(message.currentHand);
            if (!stack.isEmpty() && stack.getItem() instanceof IItemNetwork) {
                IItemNetwork network = (IItemNetwork) stack.getItem();
                try {
                    network.handlePacketData(player.world, stack, message.storedBuffer);
                } catch (Exception e) {
                    Mekanism.logger.error("FIXME: Packet handling error", e);
                }
                message.storedBuffer.release();
            }
        });
    }

    public static void encode(PacketItemStack pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.currentHand);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            PacketHandler.log("Sending ItemStack packet");
        }
        PacketHandler.encode(pkt.parameters.toArray(), buf);
    }

    public static PacketItemStack decode(PacketBuffer buf) {
        return new PacketItemStack(buf.readEnumValue(Hand.class), new PacketBuffer(buf.copy()));
    }
}