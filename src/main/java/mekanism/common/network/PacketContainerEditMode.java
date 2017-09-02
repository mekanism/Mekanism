package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.network.PacketContainerEditMode.ContainerEditModeMessage;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketContainerEditMode implements IMessageHandler<ContainerEditModeMessage, IMessage>
{
	@Override
	public IMessage onMessage(ContainerEditModeMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		PacketHandler.handlePacket(() ->
        {
            TileEntity tileEntity = message.coord4D.getTileEntity(player.world);

            if(tileEntity instanceof IFluidContainerManager)
            {
                ((IFluidContainerManager)tileEntity).setContainerEditMode(message.value);
            }
        }, player);
		
		return null;
	}
	
	public static class ContainerEditModeMessage implements IMessage
	{
		public Coord4D coord4D;
		public ContainerEditMode value;
		
		public ContainerEditModeMessage() {}
	
		public ContainerEditModeMessage(Coord4D coord, ContainerEditMode mode)
		{
			coord4D = coord;
			value = mode;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			coord4D.write(dataStream);
	
			dataStream.writeInt(value.ordinal());
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			coord4D = Coord4D.read(dataStream);
			value = ContainerEditMode.values()[dataStream.readInt()];
		}
	}
}
