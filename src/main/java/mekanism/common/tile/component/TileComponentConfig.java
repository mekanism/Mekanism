package mekanism.common.tile.component;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.base.ITileComponent;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;

public class TileComponentConfig implements ITileComponent
{
	public TileEntityContainerBlock tileEntity;
	
	public List<byte[]> sideConfigs = new ArrayList<byte[]>();
	public List<ArrayList<SideData>> sideOutputs = new ArrayList<ArrayList<SideData>>();
	
	public List<TransmissionType> transmissions = new ArrayList<TransmissionType>();
	
	public TileComponentConfig(TileEntityContainerBlock tile, TransmissionType... types)
	{
		tileEntity = tile;
		transmissions = Arrays.asList(types);
		
		tile.components.add(this);
	}
	
	public void setConfig(TransmissionType type, byte[] config)
	{
		sideConfigs.set(type.ordinal(), config);
	}
	
	public void addOutput(TransmissionType type, SideData data)
	{
		if(sideOutputs.get(type.ordinal()) == null)
		{
			sideOutputs.set(type.ordinal(), new ArrayList<SideData>());
		}
		
		sideOutputs.get(type.ordinal()).add(data);
	}
	
	public ArrayList<SideData> getOutputs(TransmissionType type)
	{
		return sideOutputs.get(type.ordinal());
	}
	
	public byte[] getConfig(TransmissionType type)
	{
		return sideConfigs.get(type.ordinal());
	}
	
	public SideData getOutput(TransmissionType type, int side, int facing)
	{
		return getOutputs(type).get(getConfig(type)[MekanismUtils.getBaseOrientation(side, facing)]);
	}
	
	public SideData getOutput(TransmissionType type, int side)
	{
		return getOutputs(type).get(getConfig(type)[side]);
	}
	
	public boolean supports(TransmissionType type)
	{
		return transmissions.contains(type);
	}
	
	@Override
	public void tick() {}

	@Override
	public void read(NBTTagCompound nbtTags) 
	{
		if(nbtTags.getBoolean("sideDataStored"))
		{
			for(TransmissionType type : transmissions)
			{
				sideConfigs.set(type.ordinal(), nbtTags.getByteArray("config" + type.ordinal()));
			}
		}
	}

	@Override
	public void read(ByteBuf dataStream) 
	{
		for(TransmissionType type : transmissions)
		{
			dataStream.readBytes(sideConfigs.get(type.ordinal()));
		}
	}

	@Override
	public void write(NBTTagCompound nbtTags) 
	{
		for(TransmissionType type : transmissions)
		{
			nbtTags.setByteArray("config" + type.ordinal(), sideConfigs.get(type.ordinal()));
		}
		
		nbtTags.setBoolean("sideDataStored", true);
	}

	@Override
	public void write(ArrayList data) 
	{
		for(TransmissionType type : transmissions)
		{
			data.add(sideConfigs.get(type.ordinal()));
		}
	}
}
