package mekanism.common.tile.component;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.SideData.EnergyState;
import mekanism.common.base.ITileComponent;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class TileComponentConfig implements ITileComponent
{
	public TileEntityContainerBlock tileEntity;
	
	public Map<Integer, byte[]> sideConfigs = new HashMap<Integer, byte[]>();
	public Map<Integer, ArrayList<SideData>> sideOutputs = new HashMap<Integer, ArrayList<SideData>>();
	public Map<Integer, Boolean> ejecting = new HashMap<Integer, Boolean>();
	public Map<Integer, Boolean> canEject = new HashMap<Integer, Boolean>();
	
	public List<TransmissionType> transmissions = new ArrayList<TransmissionType>();
	
	public TileComponentConfig(TileEntityContainerBlock tile, TransmissionType... types)
	{
		tileEntity = tile;
		
		for(TransmissionType type : types)
		{
			addSupported(type);
		}
		
		tile.components.add(this);
	}
	
	public void readFrom(TileComponentConfig config)
	{
		sideConfigs = config.sideConfigs;
		ejecting = config.ejecting;
		canEject = config.canEject;
		transmissions = config.transmissions;
	}
	
	public void addSupported(TransmissionType type)
	{
		if(!transmissions.contains(type))
		{
			transmissions.add(type);
		}
		
		sideOutputs.put(type.ordinal(), new ArrayList<SideData>());
		ejecting.put(type.ordinal(), false);
		canEject.put(type.ordinal(), true);
	}
	
	public EnumSet<ForgeDirection> getSidesForData(TransmissionType type, int facing, int dataIndex)
	{
		EnumSet<ForgeDirection> ret = EnumSet.noneOf(ForgeDirection.class);
		
		for(byte b = 0; b < 6; b++)
		{
			if(getConfig(type)[b] == dataIndex)
			{
				ret.add(ForgeDirection.getOrientation(MekanismUtils.getBaseOrientation(b, facing)));
			}
		}
		
		return ret;
	}
	
	public void setCanEject(TransmissionType type, boolean eject)
	{
		canEject.put(type.ordinal(), eject);
	}
	
	public boolean canEject(TransmissionType type)
	{
		return canEject.get(type.ordinal());
	}
	
	public void fillConfig(TransmissionType type, int data)
	{
		byte sideData = (byte)data;
		
		setConfig(type, new byte[] {sideData, sideData, sideData, sideData, sideData, sideData});
	}
	
	public void setIOEnergyConfig()
	{
		addOutput(TransmissionType.ENERGY, new SideData("None", EnumColor.GREY, EnergyState.OFF));
		addOutput(TransmissionType.ENERGY, new SideData("Input", EnumColor.DARK_GREEN, EnergyState.INPUT));
		addOutput(TransmissionType.ENERGY, new SideData("Output", EnumColor.DARK_RED, EnergyState.OUTPUT));
		
		setConfig(TransmissionType.ENERGY, new byte[] {1, 1, 2, 1, 1, 1});
	}
	
	public void setInputEnergyConfig()
	{
		addOutput(TransmissionType.ENERGY, new SideData("None", EnumColor.GREY, EnergyState.OFF));
		addOutput(TransmissionType.ENERGY, new SideData("Input", EnumColor.DARK_GREEN, EnergyState.INPUT));
		
		setConfig(TransmissionType.ENERGY, new byte[] {1, 1, 1, 1, 1, 1});
		setCanEject(TransmissionType.ENERGY, false);
	}
	
	public void setConfig(TransmissionType type, byte[] config)
	{
		sideConfigs.put(type.ordinal(), config);
	}
	
	public void addOutput(TransmissionType type, SideData data)
	{
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
				try {//TODO remove soon
					if(nbtTags.getByteArray("config" + type.ordinal()).length > 0)
					{
						sideConfigs.put(type.ordinal(), nbtTags.getByteArray("config" + type.ordinal()));
						ejecting.put(type.ordinal(), nbtTags.getBoolean("ejecting" + type.ordinal()));
					}
				} catch(Exception e) {
					try {
						byte[] bytes = new byte[6];
						
						for(int i = 0; i < 6; i++)
						{
							bytes[i] = nbtTags.getByte("config"+i);
							
							if(bytes[i] > 0)
							{
								bytes[i]--;
							}
						}
						
						sideConfigs.put(TransmissionType.ITEM.ordinal(), bytes);
						ejecting.put(TransmissionType.ITEM.ordinal(), nbtTags.getBoolean("ejecting"));
					} catch(Exception e1) {}
				}
			}
		}
	}

	@Override
	public void read(ByteBuf dataStream) 
	{
		transmissions.clear();
		
		int amount = dataStream.readInt();
		
		for(int i = 0; i < amount; i++)
		{
			transmissions.add(TransmissionType.values()[dataStream.readInt()]);
		}
		
		for(TransmissionType type : transmissions)
		{
			byte[] array = new byte[6];
			dataStream.readBytes(array);
			
			sideConfigs.put(type.ordinal(), array);
			ejecting.put(type.ordinal(), dataStream.readBoolean());
		}
	}

	@Override
	public void write(NBTTagCompound nbtTags) 
	{
		for(TransmissionType type : transmissions)
		{
			nbtTags.setByteArray("config" + type.ordinal(), sideConfigs.get(type.ordinal()));
			nbtTags.setBoolean("ejecting" + type.ordinal(), ejecting.get(type.ordinal()));
		}
		
		nbtTags.setBoolean("sideDataStored", true);
	}

	@Override
	public void write(ArrayList data) 
	{
		data.add(transmissions.size());
		
		for(TransmissionType type : transmissions)
		{
			data.add(type.ordinal());
		}
		
		for(TransmissionType type : transmissions)
		{
			data.add(sideConfigs.get(type.ordinal()));
			data.add(ejecting.get(type.ordinal()));
		}
	}
	
	public boolean isEjecting(TransmissionType type)
	{
		return ejecting.get(type.ordinal());
	}

	public void setEjecting(TransmissionType type, boolean eject)
	{
		ejecting.put(type.ordinal(), eject);
		MekanismUtils.saveChunk(tileEntity);
	}
}
