package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.base.IBlockType;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.block.BlockMachine;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.FactoryTier;
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
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEliteFactory;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.tile.TileEntityLaser;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateMachine extends ExtendedBlockState {

    public static final PropertyBool activeProperty = PropertyBool.create("active");
    public static final PropertyEnum<BaseTier> tierProperty = PropertyEnum.create("tier", BaseTier.class);
    public static final PropertyEnum<RecipeType> recipeProperty = PropertyEnum.create("recipe", RecipeType.class);

    public BlockStateMachine(BlockMachine block, PropertyEnum<?> typeProperty) {
        super(block, new IProperty[]{BlockStateFacing.facingProperty, typeProperty, activeProperty, tierProperty, recipeProperty}, new IUnlistedProperty[]{});
    }

    public enum MachineBlock {
        MACHINE_BLOCK_1,
        MACHINE_BLOCK_2,
        MACHINE_BLOCK_3;

        PropertyEnum<MachineType> machineTypeProperty;

        public PropertyEnum<MachineType> getProperty() {
            if (machineTypeProperty == null) {
                machineTypeProperty = PropertyEnum.create("type", MachineType.class, input -> input != null && input.typeBlock == this && input.isValidMachine());
            }
            return machineTypeProperty;
        }

        public Block getBlock() {
            switch (this) {
                case MACHINE_BLOCK_1:
                    return MekanismBlocks.MachineBlock;
                case MACHINE_BLOCK_2:
                    return MekanismBlocks.MachineBlock2;
                case MACHINE_BLOCK_3:
                    return MekanismBlocks.MachineBlock3;
                default:
                    return null;
            }
        }
    }

    public enum MachineType implements IStringSerializable, IBlockType {
        ENRICHMENT_CHAMBER(MachineBlock.MACHINE_BLOCK_1, 0, "EnrichmentChamber", 3, TileEntityEnrichmentChamber::new, true, false, true, Plane.HORIZONTAL, true),
        OSMIUM_COMPRESSOR(MachineBlock.MACHINE_BLOCK_1, 1, "OsmiumCompressor", 4, TileEntityOsmiumCompressor::new, true, false, true, Plane.HORIZONTAL, true),
        COMBINER(MachineBlock.MACHINE_BLOCK_1, 2, "Combiner", 5, TileEntityCombiner::new, true, false, true, Plane.HORIZONTAL, true),
        CRUSHER(MachineBlock.MACHINE_BLOCK_1, 3, "Crusher", 6, TileEntityCrusher::new, true, false, true, Plane.HORIZONTAL, true),
        DIGITAL_MINER(MachineBlock.MACHINE_BLOCK_1, 4, "DigitalMiner", 2, TileEntityDigitalMiner::new, true, true, true, Plane.HORIZONTAL, true),
        BASIC_FACTORY(MachineBlock.MACHINE_BLOCK_1, 5, "Factory", 11, TileEntityFactory::new, true, false, true, Plane.HORIZONTAL, true, FactoryTier.BASIC),
        ADVANCED_FACTORY(MachineBlock.MACHINE_BLOCK_1, 6, "Factory", 11, TileEntityAdvancedFactory::new, true, false, true, Plane.HORIZONTAL, true, FactoryTier.ADVANCED),
        ELITE_FACTORY(MachineBlock.MACHINE_BLOCK_1, 7, "Factory", 11, TileEntityEliteFactory::new, true, false, true, Plane.HORIZONTAL, true, FactoryTier.ELITE),
        METALLURGIC_INFUSER(MachineBlock.MACHINE_BLOCK_1, 8, "MetallurgicInfuser", 12, TileEntityMetallurgicInfuser::new, true, true, true, Plane.HORIZONTAL, false),
        PURIFICATION_CHAMBER(MachineBlock.MACHINE_BLOCK_1, 9, "PurificationChamber", 15, TileEntityPurificationChamber::new, true, false, true, Plane.HORIZONTAL, true),
        ENERGIZED_SMELTER(MachineBlock.MACHINE_BLOCK_1, 10, "EnergizedSmelter", 16, TileEntityEnergizedSmelter::new, true, false, true, Plane.HORIZONTAL, true),
        TELEPORTER(MachineBlock.MACHINE_BLOCK_1, 11, "Teleporter", 13, TileEntityTeleporter::new, true, false, false, BlockStateUtils.NO_ROTATION, false),
        ELECTRIC_PUMP(MachineBlock.MACHINE_BLOCK_1, 12, "ElectricPump", 17, TileEntityElectricPump::new, true, true, false, Plane.HORIZONTAL, false),
        PERSONAL_CHEST(MachineBlock.MACHINE_BLOCK_1, 13, "PersonalChest", -1, TileEntityPersonalChest::new, true, true, false, Plane.HORIZONTAL, false),
        CHARGEPAD(MachineBlock.MACHINE_BLOCK_1, 14, "Chargepad", -1, TileEntityChargepad::new, true, true, false, Plane.HORIZONTAL, false),
        LOGISTICAL_SORTER(MachineBlock.MACHINE_BLOCK_1, 15, "LogisticalSorter", 59, TileEntityLogisticalSorter::new, false, true, false, BlockStateUtils.ALL_FACINGS, true),
        ROTARY_CONDENSENTRATOR(MachineBlock.MACHINE_BLOCK_2, 0, "RotaryCondensentrator", 7, TileEntityRotaryCondensentrator::new, true, true, false, Plane.HORIZONTAL, false),
        CHEMICAL_OXIDIZER(MachineBlock.MACHINE_BLOCK_2, 1, "ChemicalOxidizer", 29, TileEntityChemicalOxidizer::new, true, true, true, Plane.HORIZONTAL, true),
        CHEMICAL_INFUSER(MachineBlock.MACHINE_BLOCK_2, 2, "ChemicalInfuser", 30, TileEntityChemicalInfuser::new, true, true, false, Plane.HORIZONTAL, true),
        CHEMICAL_INJECTION_CHAMBER(MachineBlock.MACHINE_BLOCK_2, 3, "ChemicalInjectionChamber", 31, TileEntityChemicalInjectionChamber::new, true, false, true, Plane.HORIZONTAL, true),
        ELECTROLYTIC_SEPARATOR(MachineBlock.MACHINE_BLOCK_2, 4, "ElectrolyticSeparator", 32, TileEntityElectrolyticSeparator::new, true, true, false, Plane.HORIZONTAL, true),
        PRECISION_SAWMILL(MachineBlock.MACHINE_BLOCK_2, 5, "PrecisionSawmill", 34, TileEntityPrecisionSawmill::new, true, false, true, Plane.HORIZONTAL, true),
        CHEMICAL_DISSOLUTION_CHAMBER(MachineBlock.MACHINE_BLOCK_2, 6, "ChemicalDissolutionChamber", 35, TileEntityChemicalDissolutionChamber::new, true, true, true, Plane.HORIZONTAL, true),
        CHEMICAL_WASHER(MachineBlock.MACHINE_BLOCK_2, 7, "ChemicalWasher", 36, TileEntityChemicalWasher::new, true, true, false, Plane.HORIZONTAL, true),
        CHEMICAL_CRYSTALLIZER(MachineBlock.MACHINE_BLOCK_2, 8, "ChemicalCrystallizer", 37, TileEntityChemicalCrystallizer::new, true, true, true, Plane.HORIZONTAL, true),
        SEISMIC_VIBRATOR(MachineBlock.MACHINE_BLOCK_2, 9, "SeismicVibrator", 39, TileEntitySeismicVibrator::new, true, true, false, Plane.HORIZONTAL, true),
        PRESSURIZED_REACTION_CHAMBER(MachineBlock.MACHINE_BLOCK_2, 10, "PressurizedReactionChamber", 40, TileEntityPRC::new, true, true, false, Plane.HORIZONTAL, true),
        FLUID_TANK(MachineBlock.MACHINE_BLOCK_2, 11, "FluidTank", 41, TileEntityFluidTank::new, false, true, false, BlockStateUtils.NO_ROTATION, true),
        FLUIDIC_PLENISHER(MachineBlock.MACHINE_BLOCK_2, 12, "FluidicPlenisher", 42, TileEntityFluidicPlenisher::new, true, true, false, Plane.HORIZONTAL, true),
        LASER(MachineBlock.MACHINE_BLOCK_2, 13, "Laser", -1, TileEntityLaser::new, true, true, false, BlockStateUtils.ALL_FACINGS, false),
        LASER_AMPLIFIER(MachineBlock.MACHINE_BLOCK_2, 14, "LaserAmplifier", 44, TileEntityLaserAmplifier::new, false, true, false, BlockStateUtils.ALL_FACINGS, true),
        LASER_TRACTOR_BEAM(MachineBlock.MACHINE_BLOCK_2, 15, "LaserTractorBeam", 45, TileEntityLaserTractorBeam::new, false, true, false, BlockStateUtils.ALL_FACINGS, true),
        QUANTUM_ENTANGLOPORTER(MachineBlock.MACHINE_BLOCK_3, 0, "QuantumEntangloporter", 46, TileEntityQuantumEntangloporter::new, true, false, false, BlockStateUtils.ALL_FACINGS, false),
        SOLAR_NEUTRON_ACTIVATOR(MachineBlock.MACHINE_BLOCK_3, 1, "SolarNeutronActivator", 47, TileEntitySolarNeutronActivator::new, false, true, false, Plane.HORIZONTAL, true),
        AMBIENT_ACCUMULATOR(MachineBlock.MACHINE_BLOCK_3, 2, "AmbientAccumulator", 48, TileEntityAmbientAccumulator::new, true, false, false, BlockStateUtils.NO_ROTATION, true),
        OREDICTIONIFICATOR(MachineBlock.MACHINE_BLOCK_3, 3, "Oredictionificator", 52, TileEntityOredictionificator::new, false, false, false, Plane.HORIZONTAL, true),
        RESISTIVE_HEATER(MachineBlock.MACHINE_BLOCK_3, 4, "ResistiveHeater", 53, TileEntityResistiveHeater::new, true, false, false, Plane.HORIZONTAL, true),
        FORMULAIC_ASSEMBLICATOR(MachineBlock.MACHINE_BLOCK_3, 5, "FormulaicAssemblicator", 56, TileEntityFormulaicAssemblicator::new, true, false, true, Plane.HORIZONTAL, true),
        FUELWOOD_HEATER(MachineBlock.MACHINE_BLOCK_3, 6, "FuelwoodHeater", 58, TileEntityFuelwoodHeater::new, false, false, false, Plane.HORIZONTAL, true);

        public MachineBlock typeBlock;
        public int meta;
        public String blockName;
        public int guiId;
        public Supplier<TileEntity> tileEntitySupplier;
        public boolean isElectric;
        public boolean hasModel;
        public boolean supportsUpgrades;
        public Predicate<EnumFacing> facingPredicate;
        public boolean activable;
        public FactoryTier factoryTier;

        MachineType(MachineBlock block, int i, String s, int j, Supplier<TileEntity> tileClass, boolean electric, boolean model, boolean upgrades,
              Predicate<EnumFacing> predicate, boolean hasActiveTexture) {
            this(block, i, s, j, tileClass, electric, model, upgrades, predicate, hasActiveTexture, null);
        }

        MachineType(MachineBlock block, int i, String s, int j, Supplier<TileEntity> tileClass, boolean electric, boolean model, boolean upgrades,
              Predicate<EnumFacing> predicate, boolean hasActiveTexture, FactoryTier factoryTier) {
            typeBlock = block;
            meta = i;
            blockName = s;
            guiId = j;
            tileEntitySupplier = tileClass;
            isElectric = electric;
            hasModel = model;
            supportsUpgrades = upgrades;
            facingPredicate = predicate;
            activable = hasActiveTexture;
            this.factoryTier = factoryTier;
        }

        private static final List<MachineType> VALID_MACHINES = new ArrayList<>();

        static {
            Arrays.stream(MachineType.values()).filter(MachineType::isValidMachine).forEach(VALID_MACHINES::add);
        }

        public static List<MachineType> getValidMachines() {
            return VALID_MACHINES;
        }

        public static MachineType get(Block block, int meta) {
            if (block instanceof BlockMachine) {
                return get(((BlockMachine) block).getMachineBlock(), meta);
            }
            return null;
        }

        public static MachineType get(MachineBlock block, int meta) {
            for (MachineType type : values()) {
                if (type.meta == meta && type.typeBlock == block) {
                    return type;
                }
            }
            return null;
        }


        public static MachineType get(ItemStack stack) {
            return get(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage());
        }

        @Override
        public String getBlockName() {
            return blockName;
        }

        @Override
        public boolean isEnabled() {
            return MekanismConfig.current().general.machinesManager.isEnabled(this);
        }

        public boolean isValidMachine() {
            return this != AMBIENT_ACCUMULATOR;
        }

        public TileEntity create() {
            return this.tileEntitySupplier != null ? this.tileEntitySupplier.get() : null;
        }

        public double getUsage() {
            switch (this) {
                case ENRICHMENT_CHAMBER:
                    return MekanismConfig.current().usage.enrichmentChamber.val();
                case OSMIUM_COMPRESSOR:
                    return MekanismConfig.current().usage.osmiumCompressor.val();
                case COMBINER:
                    return MekanismConfig.current().usage.combiner.val();
                case CRUSHER:
                    return MekanismConfig.current().usage.crusher.val();
                case DIGITAL_MINER:
                    return MekanismConfig.current().usage.digitalMiner.val();
                case METALLURGIC_INFUSER:
                    return MekanismConfig.current().usage.metallurgicInfuser.val();
                case PURIFICATION_CHAMBER:
                    return MekanismConfig.current().usage.purificationChamber.val();
                case ENERGIZED_SMELTER:
                    return MekanismConfig.current().usage.energizedSmelter.val();
                case TELEPORTER:
                    return 12500;
                case ELECTRIC_PUMP:
                    return MekanismConfig.current().usage.electricPump.val();
                case PERSONAL_CHEST:
                    return 30;
                case CHARGEPAD:
                    return 25;
                case LOGISTICAL_SORTER:
                    return 0;
                case ROTARY_CONDENSENTRATOR:
                    return MekanismConfig.current().usage.rotaryCondensentrator.val();
                case CHEMICAL_OXIDIZER:
                    return MekanismConfig.current().usage.oxidationChamber.val();
                case CHEMICAL_INFUSER:
                    return MekanismConfig.current().usage.chemicalInfuser.val();
                case CHEMICAL_INJECTION_CHAMBER:
                    return MekanismConfig.current().usage.chemicalInjectionChamber.val();
                case ELECTROLYTIC_SEPARATOR:
                    return MekanismConfig.current().general.FROM_H2.val() * 2;
                case PRECISION_SAWMILL:
                    return MekanismConfig.current().usage.precisionSawmill.val();
                case CHEMICAL_DISSOLUTION_CHAMBER:
                    return MekanismConfig.current().usage.chemicalDissolutionChamber.val();
                case CHEMICAL_WASHER:
                    return MekanismConfig.current().usage.chemicalWasher.val();
                case CHEMICAL_CRYSTALLIZER:
                    return MekanismConfig.current().usage.chemicalCrystallizer.val();
                case SEISMIC_VIBRATOR:
                    return MekanismConfig.current().usage.seismicVibrator.val();
                case PRESSURIZED_REACTION_CHAMBER:
                    return MekanismConfig.current().usage.pressurizedReactionBase.val();
                case FLUID_TANK:
                    return 0;
                case FLUIDIC_PLENISHER:
                    return MekanismConfig.current().usage.fluidicPlenisher.val();
                case LASER:
                    return MekanismConfig.current().usage.laser.val();
                case LASER_AMPLIFIER:
                    return 0;
                case LASER_TRACTOR_BEAM:
                    return 0;
                case QUANTUM_ENTANGLOPORTER:
                    return 0;
                case SOLAR_NEUTRON_ACTIVATOR:
                    return 0;
                case AMBIENT_ACCUMULATOR:
                    return 0;
                case RESISTIVE_HEATER:
                    return 100;
                case FORMULAIC_ASSEMBLICATOR:
                    return MekanismConfig.current().usage.formulaicAssemblicator.val();
                default:
                    return 0;
            }
        }

        private double getConfigStorage() {
            switch (this) {
                case ENRICHMENT_CHAMBER:
                    return MekanismConfig.current().storage.enrichmentChamber.val();
                case OSMIUM_COMPRESSOR:
                    return MekanismConfig.current().storage.osmiumCompressor.val();
                case COMBINER:
                    return MekanismConfig.current().storage.combiner.val();
                case CRUSHER:
                    return MekanismConfig.current().storage.crusher.val();
                case DIGITAL_MINER:
                    return MekanismConfig.current().storage.digitalMiner.val();
                case METALLURGIC_INFUSER:
                    return MekanismConfig.current().storage.metallurgicInfuser.val();
                case PURIFICATION_CHAMBER:
                    return MekanismConfig.current().storage.purificationChamber.val();
                case ENERGIZED_SMELTER:
                    return MekanismConfig.current().storage.energizedSmelter.val();
                case TELEPORTER:
                    return MekanismConfig.current().storage.teleporter.val();
                case ELECTRIC_PUMP:
                    return MekanismConfig.current().storage.electricPump.val();
                case CHARGEPAD:
                    return MekanismConfig.current().storage.chargePad.val();
                case ROTARY_CONDENSENTRATOR:
                    return MekanismConfig.current().storage.rotaryCondensentrator.val();
                case CHEMICAL_OXIDIZER:
                    return MekanismConfig.current().storage.oxidationChamber.val();
                case CHEMICAL_INFUSER:
                    return MekanismConfig.current().storage.chemicalInfuser.val();
                case CHEMICAL_INJECTION_CHAMBER:
                    return MekanismConfig.current().storage.chemicalInjectionChamber.val();
                case ELECTROLYTIC_SEPARATOR:
                    return MekanismConfig.current().storage.electrolyticSeparator.val();
                case PRECISION_SAWMILL:
                    return MekanismConfig.current().storage.precisionSawmill.val();
                case CHEMICAL_DISSOLUTION_CHAMBER:
                    return MekanismConfig.current().storage.chemicalDissolutionChamber.val();
                case CHEMICAL_WASHER:
                    return MekanismConfig.current().storage.chemicalWasher.val();
                case CHEMICAL_CRYSTALLIZER:
                    return MekanismConfig.current().storage.chemicalCrystallizer.val();
                case SEISMIC_VIBRATOR:
                    return MekanismConfig.current().storage.seismicVibrator.val();
                case PRESSURIZED_REACTION_CHAMBER:
                    return MekanismConfig.current().storage.pressurizedReactionBase.val();
                case FLUIDIC_PLENISHER:
                    return MekanismConfig.current().storage.fluidicPlenisher.val();
                case LASER:
                    return MekanismConfig.current().storage.laser.val();
                case FORMULAIC_ASSEMBLICATOR:
                    return MekanismConfig.current().storage.formulaicAssemblicator.val();
                default:
                    return 400 * getUsage();
            }
        }

        public double getStorage() {
            return Math.max(getConfigStorage(), getUsage());
        }

        public String getDescription() {
            return LangUtils.localize("tooltip." + blockName);
        }

        public ItemStack getStack() {
            return new ItemStack(typeBlock.getBlock(), 1, meta);
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return getName();
        }

        public boolean canRotateTo(EnumFacing side) {
            return facingPredicate.test(side);
        }

        public boolean hasRotations() {
            return !facingPredicate.equals(BlockStateUtils.NO_ROTATION);
        }

        public boolean hasActiveTexture() {
            return activable;
        }

        public boolean isFactory() {
            return factoryTier != null;
        }
    }

    public static class MachineBlockStateMapper extends StateMapperBase {

        @Nonnull
        @Override
        protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
            BlockMachine block = (BlockMachine) state.getBlock();
            MachineType type = state.getValue(block.getTypeProperty());
            StringBuilder builder = new StringBuilder();
            String nameOverride = null;

            if (type.hasActiveTexture()) {
                builder.append(activeProperty.getName());
                builder.append("=");
                builder.append(state.getValue(activeProperty));
            }

            if (type.hasRotations()) {
                EnumFacing facing = state.getValue(BlockStateFacing.facingProperty);
                if (!type.canRotateTo(facing)) {
                    facing = EnumFacing.NORTH;
                }
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(BlockStateFacing.facingProperty.getName());
                builder.append("=");
                builder.append(facing.getName());
            }

            if (type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY || type == MachineType.ELITE_FACTORY) {
                RecipeType recipe = state.getValue(recipeProperty);
                nameOverride = type.getName() + "_" + recipe.getName();
            }

            if (builder.length() == 0) {
                builder.append("normal");
            }
            ResourceLocation baseLocation = new ResourceLocation(Mekanism.MODID, nameOverride != null ? nameOverride : type.getName());
            return new ModelResourceLocation(baseLocation, builder.toString());
        }
    }
}