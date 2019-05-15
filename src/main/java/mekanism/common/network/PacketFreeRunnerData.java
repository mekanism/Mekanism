package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemFreeRunners.FreeRunnerMode;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.util.EnumUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFreeRunnerData implements IMessageHandler<PacketFreeRunnerData.FreeRunnerDataMessage, IMessage> {

    @Override
    public IMessage onMessage(FreeRunnerDataMessage message, MessageContext ctx) {
        EntityPlayer entityPlayer = PacketHandler.getPlayer(ctx);

        PacketHandler.handlePacket(() -> {
            if (message.packetType == FreeRunnerPacket.UPDATE) {
                if (message.value) {
                    Mekanism.freeRunnerOn.add(message.uuid);
                } else {
                    Mekanism.freeRunnerOn.remove(message.uuid);
                }
                if (!entityPlayer.world.isRemote) {
                    Mekanism.packetHandler.sendToDimension(new FreeRunnerDataMessage(FreeRunnerPacket.UPDATE, message.uuid, message.value),
                          entityPlayer.world.provider.getDimension());
                }
            } else if (message.packetType == FreeRunnerPacket.MODE) {
                ItemStack stack = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.FEET);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemFreeRunners) {
                    if (!message.value) {
                        ((ItemFreeRunners) stack.getItem()).incrementMode(stack);
                    } else {
                        ((ItemFreeRunners) stack.getItem()).setMode(stack, FreeRunnerMode.DISABLED);
                    }
                }
            }
        }, entityPlayer);
        return null;
    }

    public enum FreeRunnerPacket {
        UPDATE,
        FULL,
        MODE
    }

    public static class FreeRunnerDataMessage implements IMessage {

        public FreeRunnerPacket packetType;

        public UUID uuid;
        public boolean value;

        public FreeRunnerDataMessage() {
        }

        public FreeRunnerDataMessage(FreeRunnerPacket packetType, UUID uuid, boolean value) {
            this.packetType = packetType;
            this.value = value;
            if (packetType == FreeRunnerPacket.UPDATE) {
                this.uuid = uuid;
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(packetType.ordinal());
            if (packetType == FreeRunnerPacket.MODE) {
                buf.writeBoolean(value);
            } else if (packetType == FreeRunnerPacket.UPDATE) {
                PacketHandler.writeUUID(buf, uuid);
                buf.writeBoolean(value);
            } else if (packetType == FreeRunnerPacket.FULL) {
                buf.writeInt(Mekanism.freeRunnerOn.size());
                synchronized (Mekanism.freeRunnerOn) {
                    for (UUID uuidToSend : Mekanism.freeRunnerOn) {
                        PacketHandler.writeUUID(buf, uuidToSend);
                    }
                }
            }
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            packetType = EnumUtils.getEnumSafe(FreeRunnerPacket.values(), buf.readInt());
            if (packetType == FreeRunnerPacket.MODE) {
                value = buf.readBoolean();
            } else if (packetType == FreeRunnerPacket.UPDATE) {
                uuid = PacketHandler.readUUID(buf);
                value = buf.readBoolean();
            } else if (packetType == FreeRunnerPacket.FULL) {
                Mekanism.freeRunnerOn.clear();
                int amount = buf.readInt();
                for (int i = 0; i < amount; i++) {
                    Mekanism.freeRunnerOn.add(PacketHandler.readUUID(buf));
                }
            }
        }
    }
}