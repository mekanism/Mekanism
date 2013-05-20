package mekanism.common;

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
		BASIC("Basic", 1000000, 120, 320),
		ADVANCED("Advanced", 5000000, 240, 540),
		ELITE("Elite", 10000000, 240, 1280),
		ULTIMATE("Ultimate", 25000000, 480, 2560);
		
		public double MAX_ELECTRICITY;
		public double VOLTAGE;
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
		
		private EnergyCubeTier(String s, double maxEnergy, double voltage, int out)
		{
			name = s;
			MAX_ELECTRICITY = maxEnergy;
			VOLTAGE = voltage;
			OUTPUT = out;
		}
	}
	
	/**
	 * The tiers used by the Factory and their corresponding values.
	 * @author aidancbrady
	 *
	 */
	public static enum FactoryTier
	{
		BASIC("Basic", 3, "GuiBasicFactory.png"),
		ADVANCED("Advanced", 5, "GuiAdvancedFactory.png"),
		ELITE("Elite", 7, "GuiEliteFactory.png");
		
		public int processes;
		public String guiTexturePath;
		public String name;
		
		public static FactoryTier getFromName(String tierName)
		{
			for(FactoryTier tier : values())
			{
				if(tierName.contains(tier.name))
				{
					return tier;
				}
			}
			
			System.out.println("[Mekanism] Invalid tier identifier when retrieving with name.");
			return BASIC;
		}
		
		private FactoryTier(String s, int process, String gui)
		{
			name = s;
			processes = process;
			guiTexturePath = gui;
		}
	}
}
