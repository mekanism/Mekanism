package mekanism.api.gas;

import net.minecraft.nbt.NBTTagCompound;

public class GasTank
{
	public GasStack stored;
	
	public int maxGas;
	
	private GasTank() {}
	
	public GasTank(int max)
	{
		maxGas = max;
	}
	
	public void setGas(GasStack stack)
	{
		stored = stack;
	}
	
	public GasStack draw(int amount, boolean doDraw)
	{
		if(stored == null || amount <= 0)
		{
			return null;
		}
		
		GasStack ret = new GasStack(getGas().getGas(), Math.min(getStored(), amount));
		
		if(ret.amount > 0)
		{
			if(doDraw)
			{
				stored.amount -= ret.amount;
				
				if(stored.amount <= 0)
				{
					stored = null;
				}
			}
			
			return ret;
		}
		
		return null;
	}
	
	public int receive(GasStack amount, boolean doReceive)
	{
		if(amount == null || (stored != null && stored.amount == maxGas))
		{
			return 0;
		}
		
		int toFill = Math.min(maxGas-getStored(), amount.amount);
		
		if(doReceive)
		{
			if(stored == null)
			{
				stored = amount;
			}
			else {
				stored.amount = Math.min(maxGas, getStored()+amount.amount);
			}
		}
		
		return toFill;
	}
	
	public boolean canReceive(Gas gas)
	{
		if(getNeeded() == 0 || stored != null && (gas != null && gas != stored.getGas()))
		{
			return false;
		}
		
		return true;
	}
	
	public boolean canDraw(Gas gas)
	{
		if(stored == null || (gas != null && gas != stored.getGas()))
		{
			return false;
		}
		
		return true;
	}
	
	public int getNeeded()
	{
		return getMaxGas()-getStored();
	}
	
	public int getMaxGas()
	{
		return maxGas;
	}
	
	public GasStack getGas()
	{
		return stored;
	}
	
	public int getStored()
	{
		return stored != null ? stored.amount : 0;
	}
	
	public NBTTagCompound write(NBTTagCompound nbtTags)
	{
		if(stored != null)
		{
			nbtTags.setCompoundTag("stored", stored.write(new NBTTagCompound()));
		}
		
		nbtTags.setInteger("maxGas", maxGas);
		
		return nbtTags;
	}
	
	public void read(NBTTagCompound nbtTags)
	{
		if(nbtTags.hasKey("stored"))
		{
			stored = GasStack.readFromNBT(nbtTags);
		}
		
		maxGas = nbtTags.getInteger("maxGas");
	}
	
	public static GasTank readFromNBT(NBTTagCompound nbtTags)
	{
		if(nbtTags == null || nbtTags.hasNoTags())
		{
			return null;
		}
		
		GasTank tank = new GasTank();
		tank.read(nbtTags);
		
		return tank;
	}
}
