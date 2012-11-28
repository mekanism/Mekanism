package mekanism.api;

import net.minecraft.src.*;

public interface IEnergyCube 
{
	public EnumTier getTier(ItemStack itemstack);
	
	public void setTier(ItemStack itemstack, EnumTier tier);
	
	public static enum EnumTier
	{
		BASIC("Basic", 1000000, 10000),
		ADVANCED("Advanced", 2500000, 25000),
		ULTIMATE("Ultimate", 5000000, 50000);
		
		public double MAX_ELECTRICITY;
		public double VOLTAGE;
		public int DIVIDER;
		public String name;
		
		public static EnumTier getFromName(String gasName)
		{
			for(EnumTier tier : values())
			{
				if(gasName.contains(tier.name))
				{
					return tier;
				}
			}
			
			System.out.println("[Mekanism] Invalid tier identifier when retrieving with name.");
			return BASIC;
		}
		
		private EnumTier(String s, double maxEnergy, int divider)
		{
			name = s;
			MAX_ELECTRICITY = maxEnergy;
			DIVIDER = divider;
		}
	}
}
