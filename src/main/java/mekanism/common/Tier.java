package mekanism.common;

import io.netty.buffer.ByteBuf;
import mekanism.api.EnumColor;
import mekanism.common.multipart.TransmitterType;
import mekanism.common.util.MekanismUtils;
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
		
		public String getLocalizedName()
		{
			return MekanismUtils.localize("tier." + getName());
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

		public double maxEnergy;
		private double baseMaxEnergy;
		
		public double output;
		private double baseOutput;

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
		
		protected void loadConfig()
		{
			maxEnergy = Mekanism.configuration.get("tier", getBaseTier().getName() + "EnergyCubeMaxEnergy", baseMaxEnergy).getDouble();
			output = Mekanism.configuration.get("tier", getBaseTier().getName() + "EnergyCubeOutput", baseOutput).getDouble();
		}
		
		protected void readConfig(ByteBuf dataStream)
		{
			maxEnergy = dataStream.readDouble();
			output = dataStream.readDouble();
		}
		
		protected void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeDouble(maxEnergy);
			dataStream.writeDouble(output);
		}
		
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		private EnergyCubeTier(double max, double out)
		{
			baseMaxEnergy = maxEnergy = max;
			baseOutput = output = out;
		}
	}
	
	public static enum InductionCellTier
	{
		BASIC(1E9D),
		ADVANCED(8E9D),
		ELITE(64E9D),
		ULTIMATE(512E9D);

		public double maxEnergy;
		private double baseMaxEnergy;
		
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}
		
		protected void loadConfig()
		{
			maxEnergy = Mekanism.configuration.get("tier", getBaseTier().getName() + "InductionCellMaxEnergy", baseMaxEnergy).getDouble();
		}
		
		protected void readConfig(ByteBuf dataStream)
		{
			maxEnergy = dataStream.readDouble();
		}
		
		protected void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeDouble(maxEnergy);
		}

		private InductionCellTier(double max)
		{
			baseMaxEnergy = maxEnergy = max;
		}
	}
	
	public static enum InductionProviderTier
	{
		BASIC(64000),
		ADVANCED(512000),
		ELITE(4096000),
		ULTIMATE(32768000);

		public double output;
		private double baseOutput;
		
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}
		
		protected void loadConfig()
		{
			output = Mekanism.configuration.get("tier", getBaseTier().getName() + "InductionProviderOutput", baseOutput).getDouble();
		}
		
		protected void readConfig(ByteBuf dataStream)
		{
			output = dataStream.readDouble();
		}
		
		protected void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeDouble(output);
		}

		private InductionProviderTier(double out)
		{
			baseOutput = output = out;
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
		private int baseCapacity;
		
		public TransmitterType type;

		private CableTier(int capacity, TransmitterType transmitterType)
		{
			baseCapacity = cableCapacity = capacity;
			
			type = transmitterType;
		}
		
		protected void loadConfig()
		{
			cableCapacity = Mekanism.configuration.get("tier", getBaseTier().getName() + "CableCapacity", baseCapacity).getInt();
		}
		
		protected void readConfig(ByteBuf dataStream)
		{
			cableCapacity = dataStream.readInt();
		}
		
		protected void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeInt(cableCapacity);
		}
		
		public static CableTier get(BaseTier tier)
		{
			for(CableTier transmitter : values())
			{
				if(transmitter.getBaseTier() == tier)
				{
					return transmitter;
				}
			}
			
			return BASIC;
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
		private int baseCapacity;
		
		public int pipePullAmount;
		private int basePull;
		
		public TransmitterType type;

		private PipeTier(int capacity, int pullAmount, TransmitterType transmitterType)
		{
			baseCapacity = pipeCapacity = capacity;
			basePull = pipePullAmount = pullAmount;
			
			type = transmitterType;
		}
		
		protected void loadConfig()
		{
			pipeCapacity = Mekanism.configuration.get("tier", getBaseTier().getName() + "PipeCapacity", baseCapacity).getInt();
			pipePullAmount = Mekanism.configuration.get("tier", getBaseTier().getName() + "PipePullAmount", basePull).getInt();
		}
		
		protected void readConfig(ByteBuf dataStream)
		{
			pipeCapacity = dataStream.readInt();
			pipePullAmount = dataStream.readInt();
		}
		
		protected void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeInt(pipeCapacity);
			dataStream.writeInt(pipePullAmount);
		}
		
		public static PipeTier get(BaseTier tier)
		{
			for(PipeTier transmitter : values())
			{
				if(transmitter.getBaseTier() == tier)
				{
					return transmitter;
				}
			}
			
			return BASIC;
		}
	}
	
	/**
	 * The tiers used by Pressurized Tubes and their corresponding values.
	 * @author AidanBrady
	 *
	 */
	public static enum TubeTier
	{
		BASIC(256, 64, TransmitterType.PRESSURIZED_TUBE_BASIC),
		ADVANCED(1024, 256, TransmitterType.PRESSURIZED_TUBE_ADVANCED),
		ELITE(4096, 1024, TransmitterType.PRESSURIZED_TUBE_ELITE),
		ULTIMATE(16384, 4096, TransmitterType.PRESSURIZED_TUBE_ULTIMATE);
		
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		public int tubeCapacity;
		private int baseCapacity;
		
		public int tubePullAmount;
		private int basePull;
		
		public TransmitterType type;

		private TubeTier(int capacity, int pullAmount, TransmitterType transmitterType)
		{
			baseCapacity = tubeCapacity = capacity;
			basePull = tubePullAmount = pullAmount;
			
			type = transmitterType;
		}
		
		protected void loadConfig()
		{
			tubeCapacity = Mekanism.configuration.get("tier", getBaseTier().getName() + "TubeCapacity", baseCapacity).getInt();
			tubePullAmount = Mekanism.configuration.get("tier", getBaseTier().getName() + "TubePullAmount", basePull).getInt();
		}
		
		protected void readConfig(ByteBuf dataStream)
		{
			tubeCapacity = dataStream.readInt();
			tubePullAmount = dataStream.readInt();
		}
		
		protected void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeInt(tubeCapacity);
			dataStream.writeInt(tubePullAmount);
		}
		
		public static TubeTier get(BaseTier tier)
		{
			for(TubeTier transmitter : values())
			{
				if(transmitter.getBaseTier() == tier)
				{
					return transmitter;
				}
			}
			
			return BASIC;
		}
	}
	
	/**
	 * The tiers used by Logistical Transporters and their corresponding values.
	 * @author AidanBrady
	 *
	 */
	public static enum TransporterTier
	{
		BASIC(1, 5, TransmitterType.LOGISTICAL_TRANSPORTER_BASIC),
		ADVANCED(16, 10, TransmitterType.LOGISTICAL_TRANSPORTER_ADVANCED),
		ELITE(32, 20, TransmitterType.LOGISTICAL_TRANSPORTER_ELITE),
		ULTIMATE(64, 50, TransmitterType.LOGISTICAL_TRANSPORTER_ULTIMATE);
		
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		public int pullAmount;
		private int basePull;
		
		public int speed;
		private int baseSpeed;
		
		public TransmitterType type;

		private TransporterTier(int pull, int s, TransmitterType transmitterType)
		{
			basePull = pullAmount = pull;
			baseSpeed = speed = s;
			
			type = transmitterType;
		}
		
		protected void loadConfig()
		{
			pullAmount = Mekanism.configuration.get("tier", getBaseTier().getName() + "TransporterPullAmount", basePull).getInt();
			speed = Mekanism.configuration.get("tier", getBaseTier().getName() + "TransporterSpeed", baseSpeed).getInt();
		}
		
		protected void readConfig(ByteBuf dataStream)
		{
			pullAmount = dataStream.readInt();
			speed = dataStream.readInt();
		}
		
		protected void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeInt(pullAmount);
			dataStream.writeInt(speed);
		}
		
		public static TransporterTier get(BaseTier tier)
		{
			for(TransporterTier transmitter : values())
			{
				if(transmitter.getBaseTier() == tier)
				{
					return transmitter;
				}
			}
			
			return BASIC;
		}
	}
	
	public static void loadConfig()
	{
		for(CableTier tier : CableTier.values())
		{
			tier.loadConfig();
		}
		
		for(InductionCellTier tier : InductionCellTier.values())
		{
			tier.loadConfig();
		}
		
		for(InductionProviderTier tier : InductionProviderTier.values())
		{
			tier.loadConfig();
		}
		
		for(EnergyCubeTier tier : EnergyCubeTier.values())
		{
			tier.loadConfig();
		}
		
		for(PipeTier tier : PipeTier.values())
		{
			tier.loadConfig();
		}
		
		for(TubeTier tier : TubeTier.values())
		{
			tier.loadConfig();
		}
		
		for(TransporterTier tier : TransporterTier.values())
		{
			tier.loadConfig();
		}
	}
	
	public static void readConfig(ByteBuf dataStream)
	{
		for(CableTier tier : CableTier.values())
		{
			tier.readConfig(dataStream);
		}
		
		for(InductionCellTier tier : InductionCellTier.values())
		{
			tier.readConfig(dataStream);
		}
		
		for(InductionProviderTier tier : InductionProviderTier.values())
		{
			tier.readConfig(dataStream);
		}
		
		for(EnergyCubeTier tier : EnergyCubeTier.values())
		{
			tier.readConfig(dataStream);
		}
		
		for(PipeTier tier : PipeTier.values())
		{
			tier.readConfig(dataStream);
		}
		
		for(TubeTier tier : TubeTier.values())
		{
			tier.readConfig(dataStream);
		}
		
		for(TransporterTier tier : TransporterTier.values())
		{
			tier.readConfig(dataStream);
		}
	}
	
	public static void writeConfig(ByteBuf dataStream)
	{
		for(CableTier tier : CableTier.values())
		{
			tier.writeConfig(dataStream);
		}
		
		for(InductionCellTier tier : InductionCellTier.values())
		{
			tier.writeConfig(dataStream);
		}
		
		for(InductionProviderTier tier : InductionProviderTier.values())
		{
			tier.writeConfig(dataStream);
		}
		
		for(EnergyCubeTier tier : EnergyCubeTier.values())
		{
			tier.writeConfig(dataStream);
		}
		
		for(PipeTier tier : PipeTier.values())
		{
			tier.writeConfig(dataStream);
		}
		
		for(TubeTier tier : TubeTier.values())
		{
			tier.writeConfig(dataStream);
		}
		
		for(TransporterTier tier : TransporterTier.values())
		{
			tier.writeConfig(dataStream);
		}
	}
}
