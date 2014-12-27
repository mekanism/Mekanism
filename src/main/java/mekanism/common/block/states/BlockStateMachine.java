package mekanism.common.block.states;

import mekanism.api.MekanismConfig.general;
import mekanism.api.MekanismConfig.usage;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine;
import mekanism.common.tile.TileEntityAdvancedFactory;
import mekanism.common.tile.TileEntityAmbientAccumulator;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEliteFactory;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityEntangledBlock;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityGasCentrifuge;
import mekanism.common.tile.TileEntityLaser;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityPortableTank;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class BlockStateMachine extends BlockStateFacing
{
	public static final PropertyBool activeProperty = PropertyBool.create("active");

	public BlockStateMachine(BlockMachine block, PropertyEnum typeProperty)
	{
		super(block, typeProperty, activeProperty);
	}

	public static enum MachineBlock
	{
		MACHINE_BLOCK_1,
		MACHINE_BLOCK_2,
		MACHINE_BLOCK_3;

		public BlockMachine implBlock;
		public PropertyEnum predicatedProperty;

		public void setProperty()
		{
			predicatedProperty = PropertyEnum.create("type", MachineBlockType.class, new MachineBlockPredicate(this));
		}

		public void setImplBlock(BlockMachine block)
		{
			implBlock = block;
		}
	}

	public static enum MachineBlockType implements IStringSerializable
	{
		ENRICHMENT_CHAMBER(MachineBlock.MACHINE_BLOCK_1, 0, 3, TileEntityEnrichmentChamber.class, true, false, true, Plane.HORIZONTAL, true),
		OSMIUM_COMPRESSOR(MachineBlock.MACHINE_BLOCK_1, 1, 4, TileEntityOsmiumCompressor.class, true, false, true, Plane.HORIZONTAL, true),
		COMBINER(MachineBlock.MACHINE_BLOCK_1, 2, 5, TileEntityCombiner.class, true, false, true, Plane.HORIZONTAL, true),
		CRUSHER(MachineBlock.MACHINE_BLOCK_1, 3, 6, TileEntityCrusher.class, true, false, true, Plane.HORIZONTAL, true),
		DIGITAL_MINER(MachineBlock.MACHINE_BLOCK_1, 4, 2, TileEntityDigitalMiner.class, true, true, true, Plane.HORIZONTAL, true),
		BASIC_FACTORY(MachineBlock.MACHINE_BLOCK_1, 5, 11, TileEntityFactory.class, true, false, true, Plane.HORIZONTAL, true),
		ADVANCED_FACTORY(MachineBlock.MACHINE_BLOCK_1, 6, 11, TileEntityAdvancedFactory.class, true, false, true, Plane.HORIZONTAL, true),
		ELITE_FACTORY(MachineBlock.MACHINE_BLOCK_1, 7, 11, TileEntityEliteFactory.class, true, false, true, Plane.HORIZONTAL, true),
		METALLURGIC_INFUSER(MachineBlock.MACHINE_BLOCK_1, 8, 12, TileEntityMetallurgicInfuser.class, true, true, true, Plane.HORIZONTAL, false),
		PURIFICATION_CHAMBER(MachineBlock.MACHINE_BLOCK_1, 9, 15, TileEntityPurificationChamber.class, true, false, true, Plane.HORIZONTAL, true),
		ENERGIZED_SMELTER(MachineBlock.MACHINE_BLOCK_1, 10, 16, TileEntityEnergizedSmelter.class, true, false, true, Plane.HORIZONTAL, true),
		TELEPORTER(MachineBlock.MACHINE_BLOCK_1, 11, 13, TileEntityTeleporter.class, true, false, false, Predicates.<EnumFacing>alwaysFalse(), false),
		ELECTRIC_PUMP(MachineBlock.MACHINE_BLOCK_1, 12, 17, TileEntityElectricPump.class, true, true, false, Plane.HORIZONTAL, false),
		ELECTRIC_CHEST(MachineBlock.MACHINE_BLOCK_1, 13, -1, TileEntityElectricChest.class, true, true, false, Plane.HORIZONTAL, false),
		CHARGEPAD(MachineBlock.MACHINE_BLOCK_1, 14, -1, TileEntityChargepad.class, true, true, false, Plane.HORIZONTAL, false),
		LOGISTICAL_SORTER(MachineBlock.MACHINE_BLOCK_1, 15, -1, TileEntityLogisticalSorter.class, false, true, false, Plane.HORIZONTAL, true),
		ROTARY_CONDENSENTRATOR(MachineBlock.MACHINE_BLOCK_2, 0, 7, TileEntityRotaryCondensentrator.class, true, true, false, Plane.HORIZONTAL, false),
		CHEMICAL_OXIDIZER(MachineBlock.MACHINE_BLOCK_2, 1, 29, TileEntityChemicalOxidizer.class, true, true, true, Plane.HORIZONTAL, true),
		CHEMICAL_INFUSER(MachineBlock.MACHINE_BLOCK_2, 2, 30, TileEntityChemicalInfuser.class, true, true, false, Plane.HORIZONTAL, true),
		CHEMICAL_INJECTION_CHAMBER(MachineBlock.MACHINE_BLOCK_2, 3, 31, TileEntityChemicalInjectionChamber.class, true, false, true, Plane.HORIZONTAL, true),
		ELECTROLYTIC_SEPARATOR(MachineBlock.MACHINE_BLOCK_2, 4, 32, TileEntityElectrolyticSeparator.class, true, true, false, Plane.HORIZONTAL, true),
		PRECISION_SAWMILL(MachineBlock.MACHINE_BLOCK_2, 5, 34, TileEntityPrecisionSawmill.class, true, false, true, Plane.HORIZONTAL, true),
		CHEMICAL_DISSOLUTION_CHAMBER(MachineBlock.MACHINE_BLOCK_2, 6, 35, TileEntityChemicalDissolutionChamber.class, true, true, true, Plane.HORIZONTAL, true),
		CHEMICAL_WASHER(MachineBlock.MACHINE_BLOCK_2, 7, 36, TileEntityChemicalWasher.class, true, true, false, Plane.HORIZONTAL, true),
		CHEMICAL_CRYSTALLIZER(MachineBlock.MACHINE_BLOCK_2, 8, 37, TileEntityChemicalCrystallizer.class, true, true, true, Plane.HORIZONTAL, true),
		SEISMIC_VIBRATOR(MachineBlock.MACHINE_BLOCK_2, 9, 39, TileEntitySeismicVibrator.class, true, true, false, Plane.HORIZONTAL, true),
		PRESSURIZED_REACTION_CHAMBER(MachineBlock.MACHINE_BLOCK_2, 10, 40, TileEntityPRC.class, true, true, false, Plane.HORIZONTAL, true),
		PORTABLE_TANK(MachineBlock.MACHINE_BLOCK_2, 11, 41, TileEntityPortableTank.class, false, true, false, Predicates.<EnumFacing>alwaysFalse(), true),
		FLUIDIC_PLENISHER(MachineBlock.MACHINE_BLOCK_2, 12, 42, TileEntityFluidicPlenisher.class, true, true, false, Plane.HORIZONTAL, true),
		LASER(MachineBlock.MACHINE_BLOCK_2, 13, -1, TileEntityLaser.class, true, true, false, Predicates.<EnumFacing>alwaysTrue(), true),
		LASER_AMPLIFIER(MachineBlock.MACHINE_BLOCK_2, 14, 44, TileEntityLaserAmplifier.class, false, true, false, Predicates.<EnumFacing>alwaysTrue(), true),
		LASER_TRACTOR_BEAM(MachineBlock.MACHINE_BLOCK_2, 15, 45, TileEntityLaserTractorBeam.class, false, true, false, Predicates.<EnumFacing>alwaysTrue(), true),
		AMBIENT_ACCUMULATOR(MachineBlock.MACHINE_BLOCK_3, 0, 46, TileEntityAmbientAccumulator.class, true, false, false, Plane.HORIZONTAL, true),
		ENTANGLED_BLOCK(MachineBlock.MACHINE_BLOCK_3, 1, 47, TileEntityEntangledBlock.class, true, false, false, Plane.HORIZONTAL, true),
		GAS_CENTRIFUGE(MachineBlock.MACHINE_BLOCK_3, 2, 48, TileEntityGasCentrifuge.class, true, false, false, Plane.HORIZONTAL, true);

		public MachineBlock machineBlock;
		public int meta;
		public int guiId;
		public double baseEnergy;
		public Class<? extends TileEntity> tileEntityClass;
		public boolean isElectric;
		public boolean hasModel;
		public boolean supportsUpgrades;
		public Predicate<EnumFacing> facingPredicate;
		public boolean activable;

		private MachineBlockType(MachineBlock block, int metadata, int gui, Class<? extends TileEntity> tileClass, boolean electric, boolean model, boolean upgrades, Predicate<EnumFacing> facingAllowed, boolean activeState)
		{
			machineBlock = block;
			meta = metadata;
			guiId = gui;
			tileEntityClass = tileClass;
			isElectric = electric;
			hasModel = model;
			supportsUpgrades = upgrades;
			facingPredicate = facingAllowed;
			activable = activeState;
		}

		public static MachineBlockType get(Block block, int meta)
		{
			if(block instanceof BlockMachine)
				return getBlockType(((BlockMachine)block).getMachineBlock(), meta);
			return null;
		}

		public static  MachineBlockType getBlockType(MachineBlock blockID, int metadata)
		{
			MachineBlockType firstTry = values()[blockID.ordinal() << 4 | metadata];
			if(firstTry.machineBlock == blockID && firstTry.meta == metadata)
				return firstTry;
			for(MachineBlockType type : values())
			{
				if(type.machineBlock == blockID && type.meta == metadata)
					return type;
			}
			return null;
		}


		public TileEntity create()
		{
			try {
				return tileEntityClass.newInstance();
			} catch(Exception e) {
				Mekanism.logger.error("Unable to indirectly create tile entity.");
				e.printStackTrace();
				return null;
			}
		}

		/** Used for getting the base energy storage. */
		public double getUsage()
		{
			switch(this)
			{
				case ENRICHMENT_CHAMBER:
					return usage.enrichmentChamberUsage;
				case OSMIUM_COMPRESSOR:
					return usage.osmiumCompressorUsage;
				case COMBINER:
					return usage.combinerUsage;
				case CRUSHER:
					return usage.crusherUsage;
				case DIGITAL_MINER:
					return usage.digitalMinerUsage;
				case BASIC_FACTORY:
					return usage.factoryUsage * 3;
				case ADVANCED_FACTORY:
					return usage.factoryUsage * 5;
				case ELITE_FACTORY:
					return usage.factoryUsage * 7;
				case METALLURGIC_INFUSER:
					return usage.metallurgicInfuserUsage;
				case PURIFICATION_CHAMBER:
					return usage.purificationChamberUsage;
				case ENERGIZED_SMELTER:
					return usage.energizedSmelterUsage;
				case TELEPORTER:
					return 12500;
				case ELECTRIC_PUMP:
					return usage.electricPumpUsage;
				case ELECTRIC_CHEST:
					return 30;
				case CHARGEPAD:
					return 25;
				case LOGISTICAL_SORTER:
					return 0;
				case ROTARY_CONDENSENTRATOR:
					return usage.rotaryCondensentratorUsage;
				case CHEMICAL_OXIDIZER:
					return usage.oxidationChamberUsage;
				case CHEMICAL_INFUSER:
					return usage.chemicalInfuserUsage;
				case CHEMICAL_INJECTION_CHAMBER:
					return usage.chemicalInjectionChamberUsage;
				case ELECTROLYTIC_SEPARATOR:
					return general.FROM_H2 * 2;
				case PRECISION_SAWMILL:
					return usage.precisionSawmillUsage;
				case CHEMICAL_DISSOLUTION_CHAMBER:
					return usage.chemicalDissolutionChamberUsage;
				case CHEMICAL_WASHER:
					return usage.chemicalWasherUsage;
				case CHEMICAL_CRYSTALLIZER:
					return usage.chemicalCrystallizerUsage;
				case SEISMIC_VIBRATOR:
					return usage.seismicVibratorUsage;
				case PRESSURIZED_REACTION_CHAMBER:
					return usage.pressurizedReactionBaseUsage;
				case PORTABLE_TANK:
					return 0;
				case FLUIDIC_PLENISHER:
					return usage.fluidicPlenisherUsage;
				case LASER:
					return usage.laserUsage;
				case LASER_AMPLIFIER:
					return 0;
				case LASER_TRACTOR_BEAM:
					return 0;
				case AMBIENT_ACCUMULATOR:
					return 0;
				case ENTANGLED_BLOCK:
					return 0;
				case GAS_CENTRIFUGE:
					return usage.gasCentrifugeUsage;
				default:
					return 0;
			}
		}

		public static void updateAllUsages()
		{
			for(MachineBlockType type : values())
			{
				type.updateUsage();
			}
		}

		public void updateUsage()
		{
			baseEnergy = 400 * getUsage();
		}

		public String getDescription()
		{
			return MekanismUtils.localize("tooltip." + name().toLowerCase());
		}

		public ItemStack getStack()
		{
			return getStack(1);
		}

		public ItemStack getStack(int amount)
		{
			return new ItemStack(machineBlock.implBlock, amount, meta);
		}

		public static MachineBlockType get(ItemStack stack)
		{

			return get(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage());
		}

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}

		@Override
		public String getName()
		{
			return name().toLowerCase();
		}

		public boolean canRotateTo(EnumFacing side)
		{
			return facingPredicate.apply(side);
		}

		public boolean hasRotations()
		{
			return !facingPredicate.equals(Predicates.alwaysFalse());
		}

		public boolean hasActiveTexture()
		{
			return activable;
		}
	}

	public static class MachineBlockPredicate implements Predicate<MachineBlockType>
	{
		public MachineBlock machineBlock;

		public MachineBlockPredicate(MachineBlock type)
		{
			machineBlock = type;
		}

		@Override
		public boolean apply(MachineBlockType input)
		{
			return input.machineBlock == machineBlock;
		}
	}

	public static class MachineBlockStateMapper extends StateMapperBase
	{
		@Override
		protected ModelResourceLocation getModelResourceLocation(IBlockState state)
		{
			BlockMachine block = (BlockMachine)state.getBlock();
			MachineBlockType type = (MachineBlockType)state.getValue(block.getProperty());
			StringBuilder builder = new StringBuilder();
			if(type.hasRotations())
			{
				EnumFacing facing = (EnumFacing)state.getValue(facingProperty);
				if(type.canRotateTo(facing))
				{
					builder.append(facingProperty.getName());
					builder.append("=");
					builder.append(facing.getName());
				}
				else
				{
					return new ModelResourceLocation("builtin/missing", "missing");
				}
			}
			if(type.hasActiveTexture())
			{
				String active = (Boolean)state.getValue(activeProperty) ? "active" : "inactive";
				if(builder.length() > 0)
				{
					builder.append(",");
				}
				builder.append(active);
			}

			if(builder.length() == 0)
			{
				builder.append("normal");
			}

			ResourceLocation baseLocation = new ResourceLocation("mekanism", type.getName());
			return new ModelResourceLocation(baseLocation, builder.toString());
		}
	}
}
