package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketScubaTankData implements IMessageHandler<ScubaTankDataMessage, IMessage> {

    @Override
    public IMessage onMessage(ScubaTankDataMessage message, MessageContext context) {
        // Queue up processing on the central thread
        EntityPlayer player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
                  if (message.packetType == ScubaTankPacket.UPDATE) {
                      Mekanism.playerState.setGasmaskState(message.uuid, message.value, false);

                      // If we got this on the server, relay out to all players in the same dimension
                      // TODO: Why is this a dimensional thing?!
                      // because we dont send a packet when a player starts tracking another player (net.minecraftforge.event.entity.player.PlayerEvent.StartTracking)
                      if (!player.world.isRemote) {
                          Mekanism.packetHandler.sendToDimension(message, player.world.provider.getDimension());
                      }
                  } else if (message.packetType == ScubaTankPacket.MODE) {
                      // Use has changed the mode of their gasmask; update it
                      ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                      if (!stack.isEmpty() && stack.getItem() instanceof ItemScubaTank) {
                          ((ItemScubaTank) stack.getItem()).toggleFlowing(stack);
                      }
                  } else if (message.packetType == ScubaTankPacket.FULL) {
                      // This is a full sync; merge into our player state
                      Mekanism.playerState.setActiveGasmasks(message.activeGasmasks);
                  }
              },
              player);
        return null;
    }

    public enum ScubaTankPacket {
        UPDATE,
        FULL,
        MODE
    }

    public static class ScubaTankDataMessage implements IMessage {

        protected ScubaTankPacket packetType;

        protected Set<UUID> activeGasmasks;

        protected UUID uuid;
        protected boolean value;

        public ScubaTankDataMessage() {
        }

        public ScubaTankDataMessage(ScubaTankPacket type) {
            packetType = type;
        }

        public static ScubaTankDataMessage MODE_CHANGE(boolean change) {
            ScubaTankDataMessage m = new ScubaTankDataMessage(ScubaTankPacket.MODE);
            m.value = change;
            return m;
        }

        public static ScubaTankDataMessage UPDATE(UUID uuid, boolean state) {
            ScubaTankDataMessage m = new ScubaTankDataMessage(ScubaTankPacket.UPDATE);
            m.uuid = uuid;
            m.value = state;
            return m;
        }

        public static ScubaTankDataMessage FULL(Set<UUID> activeNames) {
            ScubaTankDataMessage m = new ScubaTankDataMessage((ScubaTankPacket.FULL));
            m.activeGasmasks = activeNames;
            return m;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(packetType.ordinal());

            if (packetType == ScubaTankPacket.MODE) {
                dataStream.writeBoolean(value);
            } else if (packetType == ScubaTankPacket.UPDATE) {
                PacketHandler.writeUUID(dataStream, uuid);
                dataStream.writeBoolean(value);
            } else if (packetType == ScubaTankPacket.FULL) {
                dataStream.writeInt(activeGasmasks.size());
                for (UUID uuid : activeGasmasks) {
                    PacketHandler.writeUUID(dataStream, uuid);
                }
            }
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            packetType = ScubaTankPacket.values()[dataStream.readInt()];

            if (packetType == ScubaTankPacket.MODE) {
                value = dataStream.readBoolean();
            } else if (packetType == ScubaTankPacket.UPDATE) {
                uuid = PacketHandler.readUUID(dataStream);
                value = dataStream.readBoolean();
            } else if (packetType == ScubaTankPacket.FULL) {
                activeGasmasks = new HashSet<>();

                int amount = dataStream.readInt();
                for (int i = 0; i < amount; i++) {
                    activeGasmasks.add(PacketHandler.readUUID(dataStream));
                }
            }
        }
    }
}
