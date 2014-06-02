package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.Coord4D;
import mekanism.common.IRedstoneControl;
import mekanism.common.IRedstoneControl.RedstoneControl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PacketRedstoneControl extends MekanismPacket
{
	public Coord4D coord4D;
	public RedstoneControl value;

	public PacketRedstoneControl(Coord4D coord, RedstoneControl control)
	{
		coord4D = coord;
		value = control;
	}

	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		Coord4D obj = Coord4D.read(dataStream);
		RedstoneControl control = RedstoneControl.values()[dataStream.readInt()];

		TileEntity tileEntity = obj.getTileEntity(world);

		if(tileEntity instanceof IRedstoneControl)
		{
			((IRedstoneControl)tileEntity).setControlType(control);
		}
	}

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(coord4D.xCoord);
		dataStream.writeInt(coord4D.yCoord);
		dataStream.writeInt(coord4D.zCoord);
		dataStream.writeInt(coord4D.dimensionId);

		dataStream.writeInt(value.ordinal());
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf buffer)
	{

	}

	@Override
	public void read(ChannelHandlerContext ctx, ByteBuf buffer)
	{

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
