package mekanism.api;

/**
 * Tier information for Mekanism.  This currently includes tiers for Energy Cubes and Smelting Factories.
 * @author aidancbrady
 *
 */
public final class Tier 
{
	/**
	 * The tiers used by the Energy Cube and their corresponding values.
	 * @author aidancbrady
	 *
	 */
	public static enum EnergyCubeTier
	{
		BASIC("Basic", 1000000, 10000, 128),
		ADVANCED("Advanced", 2500000, 25000, 256),
		ELITE("Elite", 5000000, 50000, 512);
		
		public double MAX_ELECTRICITY;
		public double VOLTAGE;
		public int DIVIDER;
		public int OUTPUT;
		public String name;
		
		public static EnergyCubeTier getFromName(String tierName)
		{
			for(EnergyCubeTier tier : values())
			{
				if(tierName.contains(tier.name))
				{
					return tier;
				}
			}
			
			System.out.println("[Mekanism] Invalid tier identifier when retrieving with name.");
			return BASIC;
		}
		
		private EnergyCubeTier(String s, double maxEnergy, int divider, int out)
		{
			name = s;
			MAX_ELECTRICITY = maxEnergy;
			DIVIDER = divider;
			OUTPUT = out;
		}
	}
	
	/**
	 * The tiers used by the Smelting Factory and their corresponding values.
	 * @author aidancbrady
	 *
	 */
	public static enum SmeltingFactoryTier
	{
		BASIC("Basic", 3, "GuiBasicSmeltingFactory.png"),
		ADVANCED("Advanced", 5, "GuiAdvancedSmeltingFactory.png"),
		ELITE("Elite", 7, "GuiEliteSmeltingFactory.png");
		
		public int processes;
		public String guiTexturePath;
		public String name;
		
		public static SmeltingFactoryTier getFromName(String tierName)
		{
			for(SmeltingFactoryTier tier : values())
			{
				if(tierName.contains(tier.name))
				{
					return tier;
				}
			}
			
			System.out.println("[Mekanism] Invalid tier identifier when retrieving with name.");
			return BASIC;
		}
		
		private SmeltingFactoryTier(String s, int process, String gui)
		{
			name = s;
			processes = process;
			guiTexturePath = gui;
		}
	}
}
