package mekanism.generators.common.tile.reactor;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.Optional.Method;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public class TileEntityReactorLogicAdapter extends TileEntityReactorBlock implements IPeripheral
{
	public ReactorLogic logicType;
	
	public boolean activeCooled;
	
	@Override
	public boolean isFrame()
	{
		return false;
	}
	
	public boolean checkMode(ReactorLogic type)
	{
		if(getReactor() == null || !getReactor().isFormed())
		{
			return false;
		}
		
		switch(type)
		{
			case DISABLED:
				return false;
			case READY:
				return getReactor().getPlasmaTemp() >= getReactor().getIgnitionTemperature(activeCooled);
			case CAPACITY:
				return getReactor().getPlasmaTemp() >= getReactor().getMaxPlasmaTemperature(activeCooled);
			case DEPLETED:
				return (getReactor().getDeuteriumTank().getStored() < getReactor().getInjectionRate()/2) ||
						(getReactor().getTritiumTank().getStored() < getReactor().getInjectionRate()/2);
			default:
				return false;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		logicType = ReactorLogic.values()[nbtTags.getInteger("logicType")];
		activeCooled = nbtTags.getBoolean("activeCooled");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("logicType", logicType.ordinal());
		nbtTags.setBoolean("activeCooled", activeCooled);
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();
			
			if(type == 0)
			{
				activeCooled = !activeCooled;
			}
			else if(type == 1)
			{
				int index = dataStream.readInt();
			}
			
			return;
		}
		
		super.handlePacketData(dataStream);
		
		logicType = ReactorLogic.values()[dataStream.readInt()];
		activeCooled = dataStream.readBoolean();
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(logicType.ordinal());
		data.add(activeCooled);
		
		return data;
	}
	
	@Override
	@Method(modid = "ComputerCraft")
	public String getType()
	{
		return getInventoryName();
	}

	@Override
	@Method(modid = "ComputerCraft")
	public boolean equals(IPeripheral other)
	{
		return this == other;
	}
	
	@Override
	@Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {}

	@Override
	@Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {}
	
	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		List<String> ret = new ArrayList<String>();
		
		for(ReactorLogic type : ReactorLogic.values())
		{
			if(type != ReactorLogic.DISABLED)
			{
				ret.add(type.name);
			}
		}
		
		return (String[])ret.toArray();
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
	{
		if(method >= 0 && method < ReactorLogic.values().length-1)
		{
			ReactorLogic type = ReactorLogic.values()[method+1];
			
			return new Object[] {checkMode(type)};
		}
		else {
			return new Object[] {"Unknown command."};
		}
	}
	
	public static enum ReactorLogic
	{
		DISABLED("disabled"),
		READY("ready"),
		CAPACITY("capacity"),
		DEPLETED("depleted");
		
		private String name;
		
		private ReactorLogic(String s)
		{
			name = s;
		}
		
		public String getLocalizedName()
		{
			return MekanismUtils.localize("reactor." + name);
		}
		
		public String getDescription()
		{
			return MekanismUtils.localize("reactor." + name + ".desc");
		}
	}
}
