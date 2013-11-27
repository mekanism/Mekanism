package mekanism.api.gas;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class Gas
{
	private String name;
	
	private String localizedName;
	
	private Fluid fluid;
	
	private Icon icon;
	
	public Gas(String s)
	{
		name = s;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getLocalizedName()
	{
		return localizedName;
	}
	
	public Gas setLocalizedName(String s)
	{
		localizedName = s;
		
		return this;
	}
	
	public Icon getIcon()
	{
		return icon;
	}
	
	public Gas setIcon(Icon i)
	{
		icon = i;
		
		if(hasFluid())
		{
			fluid.setIcons(getIcon());
		}
		
		return this;
	}
	
	public int getID()
	{
		return GasRegistry.getGasID(this);
	}
	
	public NBTTagCompound write(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("id", getID());
		
		return nbtTags;
	}
	
	public static Gas readFromNBT(NBTTagCompound nbtTags)
	{
		if(nbtTags == null || nbtTags.hasNoTags())
		{
			return null;
		}
		
		return GasRegistry.getGas(nbtTags.getInteger("id"));
	}
	
	public boolean hasFluid()
	{
		return fluid != null;
	}
	
	public void registerFluid()
	{
		if(fluid == null)
		{
			fluid = new Fluid(getName()).setGaseous(true);
			FluidRegistry.registerFluid(fluid);
		}
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
