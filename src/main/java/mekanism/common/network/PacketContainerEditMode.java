package mekanism.common.network;

import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.network.PacketContainerEditMode.ContainerEditModeMessage;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketContainerEditMode implements IMessageHandler<ContainerEditModeMessage, IMessage>
{
	@Override
	public IMessage onMessage(ContainerEditModeMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		TileEntity tileEntity = message.coord4D.getTileEntity(player.worldObj);
		
		if(tileEntity instanceof IFluidContainerManager)
		{
			((IFluidContainerManager)tileEntity).setContainerEditMode(message.value);
		}
		
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
			dataStream.writeInt(coord4D.xCoord);
			dataStream.writeInt(coord4D.yCoord);
			dataStream.writeInt(coord4D.zCoord);
			dataStream.writeInt(coord4D.dimensionId);
	
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
