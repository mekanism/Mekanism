package mekanism.api.gas;

import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class Gas
{
	private String name;
	
	private String unlocalizedName;
	
	private Fluid fluid;
	
	private Icon icon;
	
	public Gas(String s)
	{
		unlocalizedName = name = s;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getUnlocalizedName()
	{
		return "gas." + unlocalizedName;
	}
	
	public String getLocalizedName()
	{
		return MekanismUtils.localize(getUnlocalizedName());
	}
	
	public Gas setUnlocalizedName(String s)
	{
		unlocalizedName = s;
		
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
	
	public Fluid getFluid()
	{
		return fluid;
	}
	
	public Gas registerFluid()
	{
		if(fluid == null)
		{
			fluid = new Fluid(getName()).setGaseous(true);
			FluidRegistry.registerFluid(fluid);
		}
		
		return this;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
