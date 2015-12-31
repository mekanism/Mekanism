package mekanism.generators.common.tile.reactor;

import dan200.computercraft.api.lua.LuaException;
import io.netty.buffer.ByteBuf;
import mekanism.common.integration.IComputerIntegration;
import mekanism.common.util.LangUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;

public class TileEntityReactorLogicAdapter extends TileEntityReactorBlock implements IComputerIntegration
{
	public ReactorLogic logicType = ReactorLogic.DISABLED;
	
	public boolean activeCooled;
	
	public boolean prevOutputting;
	
	public TileEntityReactorLogicAdapter()
	{
		super();
		fullName = "ReactorLogicAdapter";
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			boolean outputting = checkMode();
			
			if(outputting != prevOutputting)
			{
				worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
			}
			
			prevOutputting = outputting;
		}
	}
	
	@Override
	public boolean isFrame()
	{
		return false;
	}
	
	public boolean checkMode()
	{
		if(worldObj.isRemote)
		{
			return prevOutputting;
		}
		
		if(getReactor() == null || !getReactor().isFormed())
		{
			return false;
		}
		
		switch(logicType)
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
				logicType = ReactorLogic.values()[dataStream.readInt()];
			}
			
			return;
		}
		
		super.handlePacketData(dataStream);
		
		logicType = ReactorLogic.values()[dataStream.readInt()];
		activeCooled = dataStream.readBoolean();
		prevOutputting = dataStream.readBoolean();
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(logicType.ordinal());
		data.add(activeCooled);
		data.add(prevOutputting);
		
		return data;
	}

    private static final String[] methods = new String[] {"isIgnited", "canIgnite", "getPlasmaHeat", "getMaxPlasmaHeat", "getCaseHeat", "getMaxCaseHeat", "getInjectionRate", "setInjectionRate", "hasFuel", "getProducing"};
	
	@Override
	public String[] getMethods()
	{
		return methods;
	}

	@Override
	public Object[] invoke(int method, Object[] arguments) throws LuaException, InterruptedException
	{
		if(getReactor() == null || !getReactor().isFormed())
		{
			return new Object[] {"Unformed."};
		}
		
		switch(method)
		{
			case 0:
				return new Object[] {getReactor().isBurning()};
			case 1:
				return new Object[] {getReactor().getPlasmaTemp() >= getReactor().getIgnitionTemperature(activeCooled)};
			case 2:
				return new Object[] {getReactor().getPlasmaTemp()};
			case 3:
				return new Object[] {getReactor().getMaxPlasmaTemperature(activeCooled)};
			case 4:
				return new Object[] {getReactor().getCaseTemp()};
			case 5:
				return new Object[] {getReactor().getMaxCasingTemperature(activeCooled)};
			case 6:
				return new Object[] {getReactor().getInjectionRate()};
			case 7:
				if(arguments[0] instanceof Double)
				{
					getReactor().setInjectionRate(((Double)arguments[0]).intValue());
					return new Object[] {"Injection rate set."};
				}
				else {
					return new Object[] {"Invalid parameters."};
				}
			case 8:
				return new Object[] {(getReactor().getDeuteriumTank().getStored() >= getReactor().getInjectionRate()/2) &&
						(getReactor().getTritiumTank().getStored() >= getReactor().getInjectionRate()/2)};
            case 9:
                return new Object[] {getReactor().getPassiveGeneration(false, true)};
			default:
				return new Object[] {"Unknown command."};
		}
	}
	
	public static enum ReactorLogic
	{
		DISABLED("disabled", new ItemStack(Items.gunpowder)),
		READY("ready", new ItemStack(Items.redstone)),
		CAPACITY("capacity", new ItemStack(Items.redstone)),
		DEPLETED("depleted", new ItemStack(Items.redstone));
		
		private String name;
		private ItemStack renderStack;
		
		private ReactorLogic(String s, ItemStack stack)
		{
			name = s;
			renderStack = stack;
		}
		
		public ItemStack getRenderStack()
		{
			return renderStack;
		}
		
		public String getLocalizedName()
		{
			return LangUtils.localize("reactor." + name);
		}
		
		public String getDescription()
		{
			return LangUtils.localize("reactor." + name + ".desc");
		}
	}
}
