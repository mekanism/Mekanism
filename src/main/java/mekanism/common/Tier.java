package mekanism.common;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.util.LangUtils;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

/**
 * Tier information for Mekanism.  This currently includes tiers for Energy Cubes and Smelting Factories.
 * @author aidancbrady
 *
 */
public final class Tier
{
	private static List<ITier> tierTypes = new ArrayList<>();
	
	private static boolean initiated = false;
	
	/** The default tiers used in Mekanism.
	 * @author aidancbrady
	 */
	public enum BaseTier implements IStringSerializable
	{
		BASIC("Basic", EnumColor.BRIGHT_GREEN),
		ADVANCED("Advanced", EnumColor.DARK_RED),
		ELITE("Elite", EnumColor.DARK_BLUE),
		ULTIMATE("Ultimate", EnumColor.PURPLE),
		CREATIVE("Creative", EnumColor.BLACK);
		
		public String getSimpleName()
		{
			return name;
		}
		
		public String getLocalizedName()
		{
			return LangUtils.localize("tier." + getSimpleName());
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
		
		BaseTier(String s, EnumColor c)
		{
			name = s;
			color = c;
		}
		
		@Override
		public String getName()
		{
			return name().toLowerCase();
		}
	}
	
	public enum EnergyCubeTier implements ITier, IStringSerializable
	{
		BASIC(2000000, 800),
		ADVANCED(8000000, 3200),
		ELITE(32000000, 12800),
		ULTIMATE(128000000, 51200),
		CREATIVE(Double.MAX_VALUE, Double.MAX_VALUE);

		public double maxEnergy;
		private double baseMaxEnergy;
		
		public double output;
		private double baseOutput;
		
		EnergyCubeTier(double max, double out)
		{
			baseMaxEnergy = maxEnergy = max;
			baseOutput = output = out;
		}
		
		@Override
		public void loadConfig()
		{
			if(this != CREATIVE)
			{
				maxEnergy = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "EnergyCubeMaxEnergy", baseMaxEnergy).getDouble();
				output = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "EnergyCubeOutput", baseOutput).getDouble();
			}
		}
		
		@Override
		public void readConfig(ByteBuf dataStream)
		{
			if(this != CREATIVE)
			{
				maxEnergy = dataStream.readDouble();
				output = dataStream.readDouble();
			}
		}
		
		@Override
		public void writeConfig(ByteBuf dataStream)
		{
			if(this != CREATIVE)
			{
				dataStream.writeDouble(maxEnergy);
				dataStream.writeDouble(output);
			}
		}
		
		@Override
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		@Override
		public String getName()
		{
			return name().toLowerCase();
		}
	}
	
	public enum InductionCellTier implements ITier
	{
		BASIC(1E9D),
		ADVANCED(8E9D),
		ELITE(64E9D),
		ULTIMATE(512E9D);

		public double maxEnergy;
		private double baseMaxEnergy;
		
		InductionCellTier(double max)
		{
			baseMaxEnergy = maxEnergy = max;
		}
		
		@Override
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}
		
		@Override
		public void loadConfig()
		{
			maxEnergy = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "InductionCellMaxEnergy", baseMaxEnergy).getDouble();
		}
		
		@Override
		public void readConfig(ByteBuf dataStream)
		{
			maxEnergy = dataStream.readDouble();
		}
		
		@Override
		public void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeDouble(maxEnergy);
		}
	}
	
	public enum InductionProviderTier implements ITier
	{
		BASIC(64000),
		ADVANCED(512000),
		ELITE(4096000),
		ULTIMATE(32768000);

		public double output;
		private double baseOutput;
		
		InductionProviderTier(double out)
		{
			baseOutput = output = out;
		}
		
		@Override
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}
		
		@Override
		public void loadConfig()
		{
			output = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "InductionProviderOutput", baseOutput).getDouble();
		}
		
		@Override
		public void readConfig(ByteBuf dataStream)
		{
			output = dataStream.readDouble();
		}
		
		@Override
		public void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeDouble(output);
		}
	}

	public enum FactoryTier
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
				if(tierName.contains(tier.getBaseTier().getSimpleName()))
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

		FactoryTier(int process, ResourceLocation gui)
		{
			processes = process;
			guiLocation = gui;
		}
	}

	public enum CableTier implements ITier
	{
		BASIC(3200),
		ADVANCED(12800),
		ELITE(64000),
		ULTIMATE(320000);

		public int cableCapacity;
		private int baseCapacity;

		CableTier(int capacity)
		{
			baseCapacity = cableCapacity = capacity;
		}
		
		@Override
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		@Override
		public void loadConfig()
		{
			cableCapacity = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "CableCapacity", baseCapacity).getInt();
		}

		@Override
		public void readConfig(ByteBuf dataStream)
		{
			cableCapacity = dataStream.readInt();
		}

		@Override
		public void writeConfig(ByteBuf dataStream)
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

	public enum PipeTier implements ITier
	{
		BASIC(1000, 100),
		ADVANCED(4000, 400),
		ELITE(16000, 1600),
		ULTIMATE(64000, 6400);

		public int pipeCapacity;
		private int baseCapacity;
		
		public int pipePullAmount;
		private int basePull;

		PipeTier(int capacity, int pullAmount)
		{
			baseCapacity = pipeCapacity = capacity;
			basePull = pipePullAmount = pullAmount;
		}

		@Override
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		@Override
		public void loadConfig()
		{
			pipeCapacity = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "PipeCapacity", baseCapacity).getInt();
			pipePullAmount = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "PipePullAmount", basePull).getInt();
		}

		@Override
		public void readConfig(ByteBuf dataStream)
		{
			pipeCapacity = dataStream.readInt();
			pipePullAmount = dataStream.readInt();
		}

		@Override
		public void writeConfig(ByteBuf dataStream)
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

	public enum TubeTier implements ITier
	{
		BASIC(256, 64),
		ADVANCED(1024, 256),
		ELITE(4096, 1024),
		ULTIMATE(16384, 4096);

		public int tubeCapacity;
		private int baseCapacity;
		
		public int tubePullAmount;
		private int basePull;

		TubeTier(int capacity, int pullAmount)
		{
			baseCapacity = tubeCapacity = capacity;
			basePull = tubePullAmount = pullAmount;
		}

		@Override
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		@Override
		public void loadConfig()
		{
			tubeCapacity = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "TubeCapacity", baseCapacity).getInt();
			tubePullAmount = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "TubePullAmount", basePull).getInt();
		}

		@Override
		public void readConfig(ByteBuf dataStream)
		{
			tubeCapacity = dataStream.readInt();
			tubePullAmount = dataStream.readInt();
		}

		@Override
		public void writeConfig(ByteBuf dataStream)
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
	
	public enum TransporterTier implements ITier
	{
		BASIC(1, 5),
		ADVANCED(16, 10),
		ELITE(32, 20),
		ULTIMATE(64, 50);

		public int pullAmount;
		private int basePull;
		
		public int speed;
		private int baseSpeed;

		TransporterTier(int pull, int s)
		{
			basePull = pullAmount = pull;
			baseSpeed = speed = s;
		}

		@Override
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		@Override
		public void loadConfig()
		{
			pullAmount = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "TransporterPullAmount", basePull).getInt();
			speed = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "TransporterSpeed", baseSpeed).getInt();
		}

		@Override
		public void readConfig(ByteBuf dataStream)
		{
			pullAmount = dataStream.readInt();
			speed = dataStream.readInt();
		}

		@Override
		public void writeConfig(ByteBuf dataStream)
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
	
	public enum ConductorTier implements ITier
	{
		BASIC(5, 1, 10, new ColourRGBA(0.2, 0.2, 0.2, 1)),
		ADVANCED(5, 1, 400, new ColourRGBA(0.2, 0.2, 0.2, 1)),
		ELITE(5, 1, 8000, new ColourRGBA(0.2, 0.2, 0.2, 1)),
		ULTIMATE(5, 1, 100000, new ColourRGBA(0.2, 0.2, 0.2, 1));

		public double inverseConduction;
		private double baseConduction;
		
		public double inverseHeatCapacity;
		private double baseHeatCapacity;
		
		public double inverseConductionInsulation;
		private double baseConductionInsulation;
		
		public ColourRGBA baseColour;

		ConductorTier(double inversek, double inverseC, double insulationInversek, ColourRGBA colour)
		{
			baseConduction = inverseConduction = inversek;
			baseHeatCapacity = inverseHeatCapacity = inverseC;
			baseConductionInsulation = inverseConductionInsulation = insulationInversek;
			
			baseColour = colour;
		}

		@Override
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		@Override
		public void loadConfig()
		{
			inverseConduction = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "ConductorInverseConduction", baseConduction).getDouble();
			inverseHeatCapacity = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "ConductorHeatCapacity", baseHeatCapacity).getDouble();
			inverseConductionInsulation = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "ConductorConductionInsulation", baseConductionInsulation).getDouble();
		}

		@Override
		public void readConfig(ByteBuf dataStream)
		{
			inverseConduction = dataStream.readDouble();
			inverseHeatCapacity = dataStream.readDouble();
			inverseConductionInsulation = dataStream.readDouble();
		}

		@Override
		public void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeDouble(inverseConduction);
			dataStream.writeDouble(inverseHeatCapacity);
			dataStream.writeDouble(inverseConductionInsulation);
		}
		
		public static ConductorTier get(BaseTier tier)
		{
			for(ConductorTier transmitter : values())
			{
				if(transmitter.getBaseTier() == tier)
				{
					return transmitter;
				}
			}
			
			return BASIC;
		}
	}

	public enum FluidTankTier implements ITier
	{
		BASIC(14000, 400),
		ADVANCED(28000, 800),
		ELITE(56000, 1600),
		ULTIMATE(112000, 3200),
		CREATIVE(Integer.MAX_VALUE, Integer.MAX_VALUE/2);

		public int storage;
		private int baseStorage;
		
		public int output;
		private int baseOutput;

		FluidTankTier(int s, int o)
		{
			baseStorage = storage = s;
			baseOutput = output = o;
		}
		
		@Override
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		@Override
		public void loadConfig()
		{
			storage = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "FluidTankStorage", baseStorage).getInt();
			output = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "FluidTankOutput", baseOutput).getInt();
		}

		@Override
		public void readConfig(ByteBuf dataStream)
		{
			storage = dataStream.readInt();
			output = dataStream.readInt();
		}

		@Override
		public void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeInt(storage);
			dataStream.writeInt(output);
		}
	}

	public enum GasTankTier implements ITier, IStringSerializable
	{
		BASIC(64000, 256),
		ADVANCED(128000, 512),
		ELITE(256000, 1028),
		ULTIMATE(512000, 2056),
		CREATIVE(Integer.MAX_VALUE, Integer.MAX_VALUE/2);

		public int storage;
		private int baseStorage;
		
		public int output;
		private int baseOutput;

		GasTankTier(int s, int o)
		{
			baseStorage = storage = s;
			baseOutput = output = o;
		}
		
		@Override
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		@Override
		public void loadConfig()
		{
			storage = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "GasTankStorage", baseStorage).getInt();
			output = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "GasTankOutput", baseOutput).getInt();
		}

		@Override
		public void readConfig(ByteBuf dataStream)
		{
			storage = dataStream.readInt();
			output = dataStream.readInt();
		}
		
		@Override
		public void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeInt(storage);
			dataStream.writeInt(output);
		}
		
		@Override
		public String getName()
		{
			return name().toLowerCase();
		}
	}
	
	public enum BinTier implements ITier
	{
		BASIC(4096),
		ADVANCED(8192),
		ELITE(32768),
		ULTIMATE(262144),
		CREATIVE(Integer.MAX_VALUE);

		public int storage;
		private int baseStorage;

		BinTier(int s)
		{
			baseStorage = storage = s;
		}
		
		@Override
		public BaseTier getBaseTier()
		{
			return BaseTier.values()[ordinal()];
		}

		@Override
		public void loadConfig()
		{
			storage = Mekanism.configuration.get("tier", getBaseTier().getSimpleName() + "BinStorage", baseStorage).getInt();
		}

		@Override
		public void readConfig(ByteBuf dataStream)
		{
			storage = dataStream.readInt();
		}
		
		@Override
		public void writeConfig(ByteBuf dataStream)
		{
			dataStream.writeInt(storage);
		}
	}

	public static void init()
	{
		if(initiated)
		{
			return;
		}

		for(Class<?> c : Tier.class.getDeclaredClasses())
		{
			if(c.isEnum())
			{
				try {
					for(Object obj : c.getEnumConstants())
					{
						if(obj instanceof ITier)
						{
							tierTypes.add((ITier)obj);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		initiated = true;
	}
	
	public static void loadConfig()
	{
		for(ITier tier : tierTypes)
		{
			tier.loadConfig();
		}
	}
	
	public static void readConfig(ByteBuf dataStream)
	{
		for(ITier tier : tierTypes)
		{
			tier.readConfig(dataStream);
		}
	}
	
	public static void writeConfig(ByteBuf dataStream)
	{
		for(ITier tier : tierTypes)
		{
			tier.writeConfig(dataStream);
		}
	}
	
	public interface ITier
	{
		BaseTier getBaseTier();
		
		void loadConfig();
		
		void readConfig(ByteBuf dataStream);
		
		void writeConfig(ByteBuf dataStream);
	}
}
