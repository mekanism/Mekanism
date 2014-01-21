package mekanism.api.gas;

import net.minecraft.util.StatCollector;

public class OreGas extends Gas
{
	private String oreName;
	
	public OreGas(String s, String name)
	{
		super(s);
		
		oreName = name;
	}
	
	public String getOreName()
	{
		return StatCollector.translateToLocal(oreName);
	}
}
