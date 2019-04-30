package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketSecurityMode.SecurityModeMessage;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSecurityMode implements IMessageHandler<SecurityModeMessage, IMessage> {

    @Override
    public IMessage onMessage(SecurityModeMessage message, MessageContext context) {
        EntityPlayer player = PacketHandler.getPlayer(context);

        PacketHandler.handlePacket(() ->
        {
            if (message.packetType == SecurityPacketType.BLOCK) {
                TileEntity tileEntity = message.coord4D.getTileEntity(player.world);

                if (tileEntity instanceof ISecurityTile) {
                    UUID owner = ((ISecurityTile) tileEntity).getSecurity().getOwnerUUID();

                    if (owner != null && player.getUniqueID().equals(owner)) {
                        ((ISecurityTile) tileEntity).getSecurity().setMode(message.value);
                        tileEntity.markDirty();
                    }
                }
            } else {
                ItemStack stack = player.getHeldItem(message.currentHand);

                if (stack.getItem() instanceof ISecurityItem) {
                    ((ISecurityItem) stack.getItem()).setSecurity(stack, message.value);
                }
            }
        }, player);

        return null;
    }

    public enum SecurityPacketType {
        BLOCK,
        ITEM
    }

    public static class SecurityModeMessage implements IMessage {

        public SecurityPacketType packetType;
        public Coord4D coord4D;
        public EnumHand currentHand;
        public SecurityMode value;

        public SecurityModeMessage() {
        }

        public SecurityModeMessage(Coord4D coord, SecurityMode control) {
            packetType = SecurityPacketType.BLOCK;

            coord4D = coord;
            value = control;
        }

        public SecurityModeMessage(EnumHand hand, SecurityMode control) {
            packetType = SecurityPacketType.ITEM;

            currentHand = hand;
            value = control;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(packetType.ordinal());

            if (packetType == SecurityPacketType.BLOCK) {
                coord4D.write(dataStream);
            } else {
                dataStream.writeInt(currentHand.ordinal());
            }

            dataStream.writeInt(value.ordinal());
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            packetType = SecurityPacketType.values()[dataStream.readInt()];

            if (packetType == SecurityPacketType.BLOCK) {
                coord4D = Coord4D.read(dataStream);
            } else {
                currentHand = EnumHand.values()[dataStream.readInt()];
            }

            value = SecurityMode.values()[dataStream.readInt()];
        }
    }
}
