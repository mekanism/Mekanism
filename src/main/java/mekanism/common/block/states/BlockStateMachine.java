package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.base.IBlockType;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.block.BlockMachine;
import mekanism.common.block.BlockMekanismContainer;
import mekanism.common.block.IBlockMekanism;
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

public class BlockStateMachine<BLOCK extends BlockMekanismContainer & IBlockMekanism> extends ExtendedBlockState {

    public static final PropertyBool activeProperty = PropertyBool.create("active");
    public static final PropertyEnum<BaseTier> tierProperty = PropertyEnum.create("tier", BaseTier.class);
    public static final PropertyEnum<RecipeType> recipeProperty = PropertyEnum.create("recipe", RecipeType.class);

    public BlockStateMachine(BLOCK block) {
        //TODO: Should tier stay part of blockstate or be extracted into its own block
        super(block, new IProperty[]{BlockStateFacing.facingProperty, activeProperty, tierProperty, recipeProperty}, new IUnlistedProperty[]{});
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
            return null;
        }
    }

    public enum MachineType implements IStringSerializable, IBlockType {
        ENRICHMENT_CHAMBER(3, TileEntityEnrichmentChamber::new, true, false, true, Plane.HORIZONTAL, true),
        OSMIUM_COMPRESSOR(4, TileEntityOsmiumCompressor::new, true, false, true, Plane.HORIZONTAL, true),
        COMBINER(5, TileEntityCombiner::new, true, false, true, Plane.HORIZONTAL, true),
        CRUSHER(6, TileEntityCrusher::new, true, false, true, Plane.HORIZONTAL, true),
        DIGITAL_MINER(2, TileEntityDigitalMiner::new, true, true, true, Plane.HORIZONTAL, true),

        BASIC_FACTORY(11, TileEntityFactory::new, true, false, true, Plane.HORIZONTAL, true, FactoryTier.BASIC),
        ADVANCED_FACTORY(11, TileEntityAdvancedFactory::new, true, false, true, Plane.HORIZONTAL, true, FactoryTier.ADVANCED),
        ELITE_FACTORY(11, TileEntityEliteFactory::new, true, false, true, Plane.HORIZONTAL, true, FactoryTier.ELITE),

        METALLURGIC_INFUSER(12, TileEntityMetallurgicInfuser::new, true, true, true, Plane.HORIZONTAL, false),
        PURIFICATION_CHAMBER(15, TileEntityPurificationChamber::new, true, false, true, Plane.HORIZONTAL, true),
        ENERGIZED_SMELTER(16, TileEntityEnergizedSmelter::new, true, false, true, Plane.HORIZONTAL, true),
        TELEPORTER(13, TileEntityTeleporter::new, true, false, false, BlockStateUtils.NO_ROTATION, false),
        ELECTRIC_PUMP(17, TileEntityElectricPump::new, true, true, false, Plane.HORIZONTAL, false),
        PERSONAL_CHEST(-1, TileEntityPersonalChest::new, true, true, false, Plane.HORIZONTAL, false),
        CHARGEPAD(-1, TileEntityChargepad::new, true, true, false, Plane.HORIZONTAL, false),
        LOGISTICAL_SORTER(59, TileEntityLogisticalSorter::new, false, true, false, BlockStateUtils.ALL_FACINGS, true),
        ROTARY_CONDENSENTRATOR(7, TileEntityRotaryCondensentrator::new, true, true, false, Plane.HORIZONTAL, false),
        CHEMICAL_OXIDIZER(29, TileEntityChemicalOxidizer::new, true, true, true, Plane.HORIZONTAL, true),
        CHEMICAL_INFUSER(30, TileEntityChemicalInfuser::new, true, true, false, Plane.HORIZONTAL, true),
        CHEMICAL_INJECTION_CHAMBER(31, TileEntityChemicalInjectionChamber::new, true, false, true, Plane.HORIZONTAL, true),
        ELECTROLYTIC_SEPARATOR(32, TileEntityElectrolyticSeparator::new, true, true, false, Plane.HORIZONTAL, true),
        PRECISION_SAWMILL(34, TileEntityPrecisionSawmill::new, true, false, true, Plane.HORIZONTAL, true),
        CHEMICAL_DISSOLUTION_CHAMBER(35, TileEntityChemicalDissolutionChamber::new, true, true, true, Plane.HORIZONTAL, true),
        CHEMICAL_WASHER(36, TileEntityChemicalWasher::new, true, true, false, Plane.HORIZONTAL, true),
        CHEMICAL_CRYSTALLIZER(37, TileEntityChemicalCrystallizer::new, true, true, true, Plane.HORIZONTAL, true),
        SEISMIC_VIBRATOR(39, TileEntitySeismicVibrator::new, true, true, false, Plane.HORIZONTAL, true),
        PRESSURIZED_REACTION_CHAMBER(40, TileEntityPRC::new, true, true, false, Plane.HORIZONTAL, true),
        FLUID_TANK(41, TileEntityFluidTank::new, false, true, false, BlockStateUtils.NO_ROTATION, true),
        FLUIDIC_PLENISHER(42, TileEntityFluidicPlenisher::new, true, true, false, Plane.HORIZONTAL, true),
        LASER(-1, TileEntityLaser::new, true, true, false, BlockStateUtils.ALL_FACINGS, false),
        LASER_AMPLIFIER(44, TileEntityLaserAmplifier::new, false, true, false, BlockStateUtils.ALL_FACINGS, true),
        LASER_TRACTOR_BEAM(45, TileEntityLaserTractorBeam::new, false, true, false, BlockStateUtils.ALL_FACINGS, true),
        QUANTUM_ENTANGLOPORTER(46, TileEntityQuantumEntangloporter::new, true, false, false, BlockStateUtils.ALL_FACINGS, false),
        SOLAR_NEUTRON_ACTIVATOR(47, TileEntitySolarNeutronActivator::new, false, true, false, Plane.HORIZONTAL, true),
        AMBIENT_ACCUMULATOR(48, TileEntityAmbientAccumulator::new, true, false, false, BlockStateUtils.NO_ROTATION, true),
        OREDICTIONIFICATOR(52, TileEntityOredictionificator::new, false, false, false, Plane.HORIZONTAL, true),
        RESISTIVE_HEATER(53, TileEntityResistiveHeater::new, true, false, false, Plane.HORIZONTAL, true),
        FORMULAIC_ASSEMBLICATOR(56, TileEntityFormulaicAssemblicator::new, true, false, true, Plane.HORIZONTAL, true),
        FUELWOOD_HEATER(58, TileEntityFuelwoodHeater::new, false, false, false, Plane.HORIZONTAL, true);

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

        MachineType(int gui, Supplier<TileEntity> tileClass, boolean electric, boolean model, boolean upgrades, Predicate<EnumFacing> predicate, boolean hasActiveTexture) {
            this(gui, tileClass, electric, model, upgrades, predicate, hasActiveTexture, null);
        }

        MachineType(int gui, Supplier<TileEntity> tileClass, boolean electric, boolean model, boolean upgrades, Predicate<EnumFacing> predicate, boolean hasActiveTexture, FactoryTier factoryTier) {
            guiId = gui;
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