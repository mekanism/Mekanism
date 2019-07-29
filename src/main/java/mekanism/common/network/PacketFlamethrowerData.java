package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerDataMessage;
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
            } else if (message.packetType == FlamethrowerPacket.FULL) {
                // This is a full sync; merge into our player state
                Mekanism.playerState.setActiveFlamethrowers(message.activeFlamethrowers);
            }
        }, player);
        return null;
    }

    public enum FlamethrowerPacket {
        UPDATE,
        FULL,
        MODE
    }

    public static class FlamethrowerDataMessage implements IMessage {

        public FlamethrowerPacket packetType;

        protected Set<UUID> activeFlamethrowers;

        public EnumHand currentHand;
        public UUID uuid;
        public boolean value;

        public FlamethrowerDataMessage() {
        }

        public FlamethrowerDataMessage(FlamethrowerPacket type) {
            packetType = type;
        }

        public static FlamethrowerDataMessage MODE_CHANGE(EnumHand hand) {
            FlamethrowerDataMessage m = new FlamethrowerDataMessage(FlamethrowerPacket.MODE);
            m.currentHand = hand;
            return m;
        }

        public static FlamethrowerDataMessage UPDATE(UUID uuid, boolean state) {
            FlamethrowerDataMessage m = new FlamethrowerDataMessage(FlamethrowerPacket.UPDATE);
            m.uuid = uuid;
            m.value = state;
            return m;
        }

        public static FlamethrowerDataMessage FULL(Set<UUID> activeNames) {
            FlamethrowerDataMessage m = new FlamethrowerDataMessage(FlamethrowerPacket.FULL);
            m.activeFlamethrowers = activeNames;
            return m;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(packetType.ordinal());
            if (packetType == FlamethrowerPacket.UPDATE) {
                PacketHandler.writeUUID(dataStream, uuid);
                dataStream.writeBoolean(value);
            } else if (packetType == FlamethrowerPacket.MODE) {
                dataStream.writeInt(currentHand.ordinal());
            } else if (packetType == FlamethrowerPacket.FULL) {
                dataStream.writeInt(activeFlamethrowers.size());
                for (UUID uuid : activeFlamethrowers) {
                    PacketHandler.writeUUID(dataStream, uuid);
                }
            }
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            packetType = FlamethrowerPacket.values()[dataStream.readInt()];
            if (packetType == FlamethrowerPacket.UPDATE) {
                uuid = PacketHandler.readUUID(dataStream);
                value = dataStream.readBoolean();
            } else if (packetType == FlamethrowerPacket.MODE) {
                currentHand = EnumHand.values()[dataStream.readInt()];
            } else if (packetType == FlamethrowerPacket.FULL) {
                activeFlamethrowers = new HashSet<>();

                int amount = dataStream.readInt();
                for (int i = 0; i < amount; i++) {
                    activeFlamethrowers.add(PacketHandler.readUUID(dataStream));
                }
            }
        }
    }
}