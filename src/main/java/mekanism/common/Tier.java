package mekanism.common;

import mekanism.api.EnumColor;
import mekanism.common.multipart.TransmitterType;
import net.minecraft.util.ResourceLocation;

/**
 * Tier information for Mekanism.  This currently includes tiers for Energy Cubes and Smelting Factories.
 * @author aidancbrady
 *
 */
public final class Tier
{
	/** The default tiers used in Mekanism.
	 * @author aidancbrady
	 */
	public static enum BaseTier
	{
		BASIC("Basic"),
		ADVANCED("Advanced"),
		ELITE("Elite"),
		ULTIMATE("Ultimate"),
		CREATIVE("Creative");
		
		public String getName()
		{
			return name;
		}
		
		private String name;
		
		private BaseTier(String s)
		{
			name = s;
		}
	}
	
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
		ULTIMATE("Ultimate", EnumColor.PURPLE, 128000000, 51200),
		CREATIVE("Creative", EnumColor.BLACK, Integer.MAX_VALUE, Integer.MAX_VALUE);

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

			Mekanism.logger.error("Invalid tier identifier when retrieving with name.");
			return BASIC;
		}
		
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
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

			Mekanism.logger.error("Invalid tier identifier when retrieving with name.");
			return BASIC;
		}
		
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		private FactoryTier(String s, int process, ResourceLocation gui)
		{
			name = s;
			processes = process;
			guiLocation = gui;
		}
	}

	/**
	 * The tiers used by Universal Cables and their corresponding values.
	 * @author aidancbrady
	 *
	 */
	public static enum CableTier
	{
		BASIC(500, TransmitterType.UNIVERSAL_CABLE_BASIC),
		ADVANCED(2000, TransmitterType.UNIVERSAL_CABLE_ADVANCED),
		ELITE(8000, TransmitterType.UNIVERSAL_CABLE_ELITE),
		ULTIMATE(32000, TransmitterType.UNIVERSAL_CABLE_ULTIMATE);
		
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		public int cableCapacity;
		public TransmitterType type;

		private CableTier(int capacity, TransmitterType transmitterType)
		{
			cableCapacity = capacity;
			type = transmitterType;
		}
	}

	/**
	 * The tiers used by Mechanical Pipes and their corresponding values.
	 * @author unpairedbracket
	 *
	 */
	public static enum PipeTier
	{
		BASIC(1000, 100, TransmitterType.MECHANICAL_PIPE_BASIC),
		ADVANCED(4000, 400, TransmitterType.MECHANICAL_PIPE_ADVANCED),
		ELITE(16000, 1600, TransmitterType.MECHANICAL_PIPE_ELITE),
		ULTIMATE(64000, 6400, TransmitterType.MECHANICAL_PIPE_ULTIMATE);
		
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		public int pipeCapacity;
		public int pipePullAmount;
		public TransmitterType type;

		private PipeTier(int capacity, int pullAmount, TransmitterType transmitterType)
		{
			pipeCapacity = capacity;
			pipePullAmount = pullAmount;
			type = transmitterType;
		}

		public static PipeTier getTierFromMeta(int meta)
		{
			switch(meta)
			{
				case 4:
					return BASIC;
				case 5:
					return ADVANCED;
				case 6:
					return ELITE;
				case 7:
					return ULTIMATE;
				default:
					return BASIC;
			}
		}
	}
}
