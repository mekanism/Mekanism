package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerDataMessage;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFlamethrowerData implements IMessageHandler<FlamethrowerDataMessage, IMessage> {

    @Override
    public IMessage onMessage(FlamethrowerDataMessage message, MessageContext context) {
        // Queue up the processing on the central thread
        EntityPlayer player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            if (message.packetType == FlamethrowerPacket.UPDATE) {
                Mekanism.playerState.setFlamethrowerState(message.uuid, message.value, false);
                // If we got this packet on the server, resend out to all clients in same dimension
                // TODO: Why is this a dimensional thing?!
                // because we dont send a packet when a player starts tracking another player (net.minecraftforge.event.entity.player.PlayerEvent.StartTracking)
                if (!player.world.isRemote) {
                    Mekanism.packetHandler.sendToDimension(message, player.world.provider.getDimension());
                }
            } else if (message.packetType == FlamethrowerPacket.MODE) {
                ItemStack stack = player.getHeldItem(message.currentHand);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemFlamethrower) {
                    ((ItemFlamethrower) stack.getItem()).incrementMode(stack);
                }
            }
        }, player);
        return null;
    }

    public enum FlamethrowerPacket {
        UPDATE,
        MODE;

        @Nullable
        public static FlamethrowerPacket get(int index) {
            if (index < 0 || index >= values().length) {
                return null;
            }
            return values()[index];
        }
    }

    public static class FlamethrowerDataMessage implements IMessage {

        public FlamethrowerPacket packetType;

        public EnumHand currentHand;
        public UUID uuid;
        public boolean value;

        public FlamethrowerDataMessage() {
        }

        public FlamethrowerDataMessage(FlamethrowerPacket type, EnumHand hand, UUID uuid, boolean state) {
            packetType = type;
            if (type == FlamethrowerPacket.UPDATE) {
                this.uuid = uuid;
                value = state;
            } else if (type == FlamethrowerPacket.MODE) {
                currentHand = hand;
            }
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(packetType.ordinal());
            if (packetType == FlamethrowerPacket.UPDATE) {
                PacketHandler.writeUUID(dataStream, uuid);
                dataStream.writeBoolean(value);
            } else {
                dataStream.writeInt(currentHand.ordinal());
            }
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            packetType = FlamethrowerPacket.get(dataStream.readInt());
            if (packetType == FlamethrowerPacket.UPDATE) {
                uuid = PacketHandler.readUUID(dataStream);
                value = dataStream.readBoolean();
            } else if (packetType == FlamethrowerPacket.MODE) {
                currentHand = MekanismUtils.getHandSafe(dataStream.readInt());
            }
        }
    }
}