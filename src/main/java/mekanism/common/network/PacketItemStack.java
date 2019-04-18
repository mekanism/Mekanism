package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.IItemNetwork;
import mekanism.common.network.PacketItemStack.ItemStackMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketItemStack implements IMessageHandler<ItemStackMessage, IMessage> {

    @Override
    public IMessage onMessage(ItemStackMessage message, MessageContext context) {
        EntityPlayer player = PacketHandler.getPlayer(context);

        if (player == null) {
            return null;
        }

        PacketHandler.handlePacket(() ->
        {
            ItemStack stack = player.getHeldItem(message.currentHand);

            if (!stack.isEmpty() && stack.getItem() instanceof IItemNetwork) {
                IItemNetwork network = (IItemNetwork) stack.getItem();

                try {
                    network.handlePacketData(stack, message.storedBuffer);
                } catch (Exception e) {
                    Mekanism.logger.error("FIXME: Packet handling error", e);
                }

                message.storedBuffer.release();
            }
        }, player);

        return null;
    }

    public static class ItemStackMessage implements IMessage {

        public EnumHand currentHand;

        public List<Object> parameters;

        public ByteBuf storedBuffer = null;

        public ItemStackMessage() {
        }

        public ItemStackMessage(EnumHand hand, List<Object> params) {
            currentHand = hand;
            parameters = params;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(currentHand.ordinal());

            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

            if (server != null) {
                PacketHandler.log("Sending ItemStack packet");
            }

            PacketHandler.encode(parameters.toArray(), dataStream);
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            currentHand = EnumHand.values()[dataStream.readInt()];

            storedBuffer = dataStream.copy();
        }
    }
}
