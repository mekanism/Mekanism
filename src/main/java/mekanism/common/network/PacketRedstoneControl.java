package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mekanism.api.Coord4D;
import mekanism.common.IRedstoneControl;
import mekanism.common.IRedstoneControl.RedstoneControl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketRedstoneControl extends MekanismPacket
{
	public Coord4D coord4D;
	public RedstoneControl value;
	
	public PacketRedstoneControl() {}

	public PacketRedstoneControl(Coord4D coord, RedstoneControl control)
	{
		coord4D = coord;
		value = control;
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeInt(coord4D.xCoord);
		dataStream.writeInt(coord4D.yCoord);
		dataStream.writeInt(coord4D.zCoord);
		dataStream.writeInt(coord4D.dimensionId);

		dataStream.writeInt(value.ordinal());
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		Coord4D obj = Coord4D.read(dataStream);
		RedstoneControl control = RedstoneControl.values()[dataStream.readInt()];

		TileEntity tileEntity = obj.getTileEntity(player.worldObj);

		if(tileEntity instanceof IRedstoneControl)
		{
			((IRedstoneControl)tileEntity).setControlType(control);
		}
	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{

	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{

	}
}
