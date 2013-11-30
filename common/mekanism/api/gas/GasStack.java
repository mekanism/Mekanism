package mekanism.api.gas;

import net.minecraft.nbt.NBTTagCompound;

public class GasStack
{
	private Gas type;
	public int amount;
	
	public GasStack(int id, int quantity)
	{
		type = GasRegistry.getGas(id);
		amount = quantity;
	}
	
	public GasStack(Gas gas, int quantity)
	{
		type = gas;
		amount = quantity;
	}
	
	private GasStack() {}
	
	public Gas getGas()
	{
		return type;
	}
	
	public NBTTagCompound write(NBTTagCompound nbtTags)
	{
		type.write(nbtTags);
		nbtTags.setInteger("amount", amount);
		
		return nbtTags;
	}
	
	private void read(NBTTagCompound nbtTags)
	{
		type = Gas.readFromNBT(nbtTags);
		amount = nbtTags.getInteger("amount");
	}
	
	public static GasStack readFromNBT(NBTTagCompound nbtTags)
	{
		if(nbtTags == null || nbtTags.hasNoTags())
		{
			return null;
		}
		
		GasStack stack = new GasStack();
		stack.read(nbtTags);
		
		if(stack.getGas() == null)
		{
			return null;
		}
		
		return stack;
	}
	
	public GasStack copy()
	{
		return new GasStack(type, amount);
	}
	
	@Override
	public String toString()
	{
		return "[" + type + ", " + amount + "]";
	}
}
