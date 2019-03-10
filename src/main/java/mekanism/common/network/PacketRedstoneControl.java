package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.network.PacketRedstoneControl.RedstoneControlMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRedstoneControl implements IMessageHandler<RedstoneControlMessage, IMessage> {

    @Override
    public IMessage onMessage(RedstoneControlMessage message, MessageContext context) {
        EntityPlayer player = PacketHandler.getPlayer(context);

        PacketHandler.handlePacket(() ->
        {
            TileEntity tileEntity = message.coord4D.getTileEntity(player.world);

            if (tileEntity instanceof IRedstoneControl) {
                ((IRedstoneControl) tileEntity).setControlType(message.value);
            }
        }, player);

        return null;
    }

    public static class RedstoneControlMessage implements IMessage {

        public Coord4D coord4D;
        public RedstoneControl value;

        public RedstoneControlMessage() {
        }

        public RedstoneControlMessage(Coord4D coord, RedstoneControl control) {
            coord4D = coord;
            value = control;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            coord4D.write(dataStream);

            dataStream.writeInt(value.ordinal());
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            coord4D = Coord4D.read(dataStream);
            value = RedstoneControl.values()[dataStream.readInt()];
        }
    }
}
