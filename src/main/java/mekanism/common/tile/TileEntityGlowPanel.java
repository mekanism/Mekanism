package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileNetwork;
import mekanism.common.block.property.PropertyColor;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

public class TileEntityGlowPanel extends TileEntity implements ITileNetwork
{
	public EnumColor colour = EnumColor.WHITE;
	public EnumFacing side = EnumFacing.DOWN;

	public void setColour(EnumColor newColour)
	{
		colour = newColour;
	}

	public void setOrientation(EnumFacing newSide)
	{
		side = newSide;
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream) throws Exception 
	{
		side = EnumFacing.getFront(dataStream.readInt());
		colour = EnumColor.DYES[dataStream.readInt()];
		
		MekanismUtils.updateBlock(world, pos);
	}
	
	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		data.add(side.ordinal());
		data.add(colour.getMetaValue());
		
		return data;
	}
	
	@Override
	public void validate()
	{
		super.validate();

		if(world.isRemote)
		{
			Mekanism.packetHandler.sendToServer(new DataRequestMessage(Coord4D.get(this)));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		
		nbt.setInteger("side", side.ordinal());
		nbt.setInteger("colour", colour.getMetaValue());
		
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		
		side = EnumFacing.getFront(nbt.getInteger("side"));
		colour = EnumColor.DYES[nbt.getInteger("colour")];
	}
	
	public static int hash(IExtendedBlockState state)
	{
		int hash = 1;
		hash = 31 * hash + state.getValue(PropertyColor.INSTANCE).color.ordinal();
		hash = 31 * hash + state.getValue(BlockStateFacing.facingProperty).ordinal();
		
		return hash;
	}
}
