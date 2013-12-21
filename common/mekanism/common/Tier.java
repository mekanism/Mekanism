package mekanism.common;

import mekanism.api.EnumColor;
import net.minecraft.util.ResourceLocation;

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
		BASIC("Basic", EnumColor.BRIGHT_GREEN, 2000000, 800),
		ADVANCED("Advanced", EnumColor.DARK_RED, 8000000, 3200),
		ELITE("Elite", EnumColor.DARK_BLUE, 32000000, 12800),
		ULTIMATE("Ultimate", EnumColor.PURPLE, 128000000, 51200);
		
		public double MAX_ELECTRICITY;
		public double OUTPUT;
		public String name;
		public EnumColor color;
		
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
		
		private EnergyCubeTier(String s, EnumColor c, double maxEnergy, double out)
		{
			name = s;
			color = c;
			MAX_ELECTRICITY = maxEnergy;
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
		BASIC("Basic", 3, new ResourceLocation("mekanism", "gui/factory/GuiBasicFactory.png")),
		ADVANCED("Advanced", 5, new ResourceLocation("mekanism", "gui/factory/GuiAdvancedFactory.png")),
		ELITE("Elite", 7, new ResourceLocation("mekanism", "gui/factory/GuiEliteFactory.png"));
		
		public int processes;
		public ResourceLocation guiLocation;
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
		
		private FactoryTier(String s, int process, ResourceLocation gui)
		{
			name = s;
			processes = process;
			guiLocation = gui;
		}
	}
}
