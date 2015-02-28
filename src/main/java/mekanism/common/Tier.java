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
		BASIC("Basic", EnumColor.BRIGHT_GREEN),
		ADVANCED("Advanced", EnumColor.DARK_RED),
		ELITE("Elite", EnumColor.DARK_BLUE),
		ULTIMATE("Ultimate", EnumColor.PURPLE),
		CREATIVE("Creative", EnumColor.BLACK);
		
		public String getName()
		{
			return name;
		}
		
		public EnumColor getColor()
		{
			return color;
		}
		
		public boolean isObtainable()
		{
			return this != CREATIVE;
		}
		
		private String name;
		private EnumColor color;
		
		private BaseTier(String s, EnumColor c)
		{
			name = s;
			color = c;
		}
	}
	
	/**
	 * The tiers used by the Energy Cube and their corresponding values.
	 * @author aidancbrady
	 *
	 */
	public static enum EnergyCubeTier
	{
		BASIC(2000000, 800),
		ADVANCED(8000000, 3200),
		ELITE(32000000, 12800),
		ULTIMATE(128000000, 51200),
		CREATIVE(Integer.MAX_VALUE, Integer.MAX_VALUE);

		public double MAX_ELECTRICITY;
		public double OUTPUT;

		public static EnergyCubeTier getFromName(String tierName)
		{
			for(EnergyCubeTier tier : values())
			{
				if(tierName.contains(tier.getBaseTier().getName()))
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

		private EnergyCubeTier(double maxEnergy, double out)
		{
			MAX_ELECTRICITY = maxEnergy;
			OUTPUT = out;
		}
	}
	
	public static enum InductionCellTier
	{
		BASIC(1E9D),
		ADVANCED(8E9D),
		ELITE(64E9D),
		ULTIMATE(512E9D);

		public double MAX_ELECTRICITY;
		
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		private InductionCellTier(double maxEnergy)
		{
			MAX_ELECTRICITY = maxEnergy;
		}
	}
	
	public static enum InductionProviderTier
	{
		BASIC(64000),
		ADVANCED(512000),
		ELITE(4096000),
		ULTIMATE(32768000);

		public double OUTPUT;
		
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		private InductionProviderTier(double out)
		{
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
		BASIC(3, new ResourceLocation("mekanism", "gui/factory/GuiBasicFactory.png")),
		ADVANCED(5, new ResourceLocation("mekanism", "gui/factory/GuiAdvancedFactory.png")),
		ELITE(7, new ResourceLocation("mekanism", "gui/factory/GuiEliteFactory.png"));

		public int processes;
		public ResourceLocation guiLocation;

		public static FactoryTier getFromName(String tierName)
		{
			for(FactoryTier tier : values())
			{
				if(tierName.contains(tier.getBaseTier().getName()))
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

		private FactoryTier(int process, ResourceLocation gui)
		{
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
		BASIC(3200, TransmitterType.UNIVERSAL_CABLE_BASIC),
		ADVANCED(12800, TransmitterType.UNIVERSAL_CABLE_ADVANCED),
		ELITE(64000, TransmitterType.UNIVERSAL_CABLE_ELITE),
		ULTIMATE(320000, TransmitterType.UNIVERSAL_CABLE_ULTIMATE);
		
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
