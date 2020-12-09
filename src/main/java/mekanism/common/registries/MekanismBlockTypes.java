package mekanism.common.registries;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.EnumSet;
import java.util.function.Supplier;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.AttributeCustomSelectionBox;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.attribute.AttributeStateBoilerValveMode;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeStateFacing.FacePlacementType;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.block.attribute.Attributes.AttributeCustomResistance;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.attribute.Attributes.AttributeMobSpawn;
import mekanism.common.block.attribute.Attributes.AttributeMultiblock;
import mekanism.common.block.attribute.Attributes.AttributeRedstone;
import mekanism.common.block.attribute.Attributes.AttributeRedstoneEmitter;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.BlockShapes;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockType.BlockTypeBuilder;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.BlockTypeTile.BlockTileBuilder;
import mekanism.common.content.blocktype.Factory;
import mekanism.common.content.blocktype.Factory.FactoryBuilder;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.content.blocktype.Machine.MachineBuilder;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.container.tile.ModificationStationContainer;
import mekanism.common.inventory.container.tile.QIODashboardContainer;
import mekanism.common.inventory.container.tile.ThermoelectricBoilerContainer;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityIndustrialAlarm;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityRadioactiveWasteBarrel;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.laser.TileEntityLaser;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.laser.TileEntityLaserTractorBeam;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.machine.TileEntityChemicalInfuser;
import mekanism.common.tile.machine.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.machine.TileEntityChemicalOxidizer;
import mekanism.common.tile.machine.TileEntityChemicalWasher;
import mekanism.common.tile.machine.TileEntityCombiner;
import mekanism.common.tile.machine.TileEntityCrusher;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityElectricPump;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import mekanism.common.tile.machine.TileEntityEnergizedSmelter;
import mekanism.common.tile.machine.TileEntityEnrichmentChamber;
import mekanism.common.tile.machine.TileEntityFluidicPlenisher;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.machine.TileEntityFuelwoodHeater;
import mekanism.common.tile.machine.TileEntityIsotopicCentrifuge;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.tile.machine.TileEntityOsmiumCompressor;
import mekanism.common.tile.machine.TileEntityPrecisionSawmill;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import mekanism.common.tile.machine.TileEntityPurificationChamber;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.tile.multiblock.TileEntityBoilerValve;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.tile.multiblock.TileEntityDynamicValve;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.tile.multiblock.TileEntityInductionPort;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.tile.multiblock.TileEntitySPSPort;
import mekanism.common.tile.multiblock.TileEntityStructuralGlass;
import mekanism.common.tile.multiblock.TileEntitySuperchargedCoil;
import mekanism.common.tile.multiblock.TileEntitySuperheatingElement;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationValve;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import mekanism.common.tile.qio.TileEntityQIOImporter;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.util.EnumUtils;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.properties.BlockStateProperties;

public class MekanismBlockTypes {

    private MekanismBlockTypes() {
    }

    //TODO: Name these a lot better
    private static final FloatingLong LARGE_LASER = FloatingLong.createConst(5_000_000_000L);
    private static final FloatingLong TELEPORTER_USAGE = FloatingLong.createConst(12_500);
    private static final FloatingLong HUNDRED = FloatingLong.createConst(100);
    private static final FloatingLong TWENTY_FIVE = FloatingLong.createConst(25);

    private static final Table<FactoryTier, FactoryType, Factory<?>> FACTORIES = HashBasedTable.create();

    // Enrichment Chamber
    public static final FactoryMachine<TileEntityEnrichmentChamber> ENRICHMENT_CHAMBER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.ENRICHMENT_CHAMBER, MekanismLang.DESCRIPTION_ENRICHMENT_CHAMBER, FactoryType.ENRICHING)
          .withGui(() -> MekanismContainerTypes.ENRICHMENT_CHAMBER)
          .withSound(MekanismSounds.ENRICHMENT_CHAMBER)
          .withEnergyConfig(MekanismConfig.usage.enrichmentChamber, MekanismConfig.storage.enrichmentChamber)
          .build();
    // Crusher
    public static final FactoryMachine<TileEntityCrusher> CRUSHER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.CRUSHER, MekanismLang.DESCRIPTION_CRUSHER, FactoryType.CRUSHING)
          .withGui(() -> MekanismContainerTypes.CRUSHER)
          .withSound(MekanismSounds.CRUSHER)
          .withEnergyConfig(MekanismConfig.usage.crusher, MekanismConfig.storage.crusher)
          .build();
    // Energized Smelter
    public static final FactoryMachine<TileEntityEnergizedSmelter> ENERGIZED_SMELTER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.ENERGIZED_SMELTER, MekanismLang.DESCRIPTION_ENERGIZED_SMELTER, FactoryType.SMELTING)
          .withGui(() -> MekanismContainerTypes.ENERGIZED_SMELTER)
          .withSound(MekanismSounds.ENERGIZED_SMELTER)
          .withEnergyConfig(MekanismConfig.usage.energizedSmelter, MekanismConfig.storage.energizedSmelter)
          .build();
    // Precision Sawmill
    public static final FactoryMachine<TileEntityPrecisionSawmill> PRECISION_SAWMILL = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.PRECISION_SAWMILL, MekanismLang.DESCRIPTION_PRECISION_SAWMILL, FactoryType.SAWING)
          .withGui(() -> MekanismContainerTypes.PRECISION_SAWMILL)
          .withSound(MekanismSounds.PRECISION_SAWMILL)
          .withEnergyConfig(MekanismConfig.usage.precisionSawmill, MekanismConfig.storage.precisionSawmill)
          .build();
    // Osmium Compressor
    public static final FactoryMachine<TileEntityOsmiumCompressor> OSMIUM_COMPRESSOR = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.OSMIUM_COMPRESSOR, MekanismLang.DESCRIPTION_OSMIUM_COMPRESSOR, FactoryType.COMPRESSING)
          .withGui(() -> MekanismContainerTypes.OSMIUM_COMPRESSOR)
          .withSound(MekanismSounds.OSMIUM_COMPRESSOR)
          .withEnergyConfig(MekanismConfig.usage.osmiumCompressor, MekanismConfig.storage.osmiumCompressor)
          .build();
    // Combiner
    public static final FactoryMachine<TileEntityCombiner> COMBINER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.COMBINER, MekanismLang.DESCRIPTION_COMBINER, FactoryType.COMBINING)
          .withGui(() -> MekanismContainerTypes.COMBINER)
          .withSound(MekanismSounds.COMBINER)
          .withEnergyConfig(MekanismConfig.usage.combiner, MekanismConfig.storage.combiner)
          .build();
    // Metallurgic Infuser
    public static final FactoryMachine<TileEntityMetallurgicInfuser> METALLURGIC_INFUSER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.METALLURGIC_INFUSER, MekanismLang.DESCRIPTION_METALLURGIC_INFUSER, FactoryType.INFUSING)
          .withGui(() -> MekanismContainerTypes.METALLURGIC_INFUSER)
          .withSound(MekanismSounds.METALLURGIC_INFUSER)
          .withEnergyConfig(MekanismConfig.usage.metallurgicInfuser, MekanismConfig.storage.metallurgicInfuser)
          .withCustomShape(BlockShapes.METALLURGIC_INFUSER).build();
    // Purification Chamber
    public static final FactoryMachine<TileEntityPurificationChamber> PURIFICATION_CHAMBER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.PURIFICATION_CHAMBER, MekanismLang.DESCRIPTION_PURIFICATION_CHAMBER, FactoryType.PURIFYING)
          .withGui(() -> MekanismContainerTypes.PURIFICATION_CHAMBER)
          .withSound(MekanismSounds.PURIFICATION_CHAMBER)
          .withEnergyConfig(MekanismConfig.usage.purificationChamber, MekanismConfig.storage.purificationChamber)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS))
          .build();
    // Chemical Injection Chamber
    public static final FactoryMachine<TileEntityChemicalInjectionChamber> CHEMICAL_INJECTION_CHAMBER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.CHEMICAL_INJECTION_CHAMBER, MekanismLang.DESCRIPTION_CHEMICAL_INJECTION_CHAMBER, FactoryType.INJECTING)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER)
          .withSound(MekanismSounds.CHEMICAL_INJECTION_CHAMBER)
          .withEnergyConfig(MekanismConfig.usage.chemicalInjectionChamber, MekanismConfig.storage.chemicalInjectionChamber)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS))
          .build();

    // Pressurized Reaction Chamber
    public static final Machine<TileEntityPressurizedReactionChamber> PRESSURIZED_REACTION_CHAMBER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.PRESSURIZED_REACTION_CHAMBER, MekanismLang.DESCRIPTION_PRESSURIZED_REACTION_CHAMBER)
          .withGui(() -> MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER)
          .withSound(MekanismSounds.PRESSURIZED_REACTION_CHAMBER)
          .withEnergyConfig(MekanismConfig.usage.pressurizedReactionBase, MekanismConfig.storage.pressurizedReactionBase)
          .withCustomShape(BlockShapes.PRESSURIZED_REACTION_CHAMBER)
          .build();
    // Chemical Crystallizer
    public static final Machine<TileEntityChemicalCrystallizer> CHEMICAL_CRYSTALLIZER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_CRYSTALLIZER, MekanismLang.DESCRIPTION_CHEMICAL_CRYSTALLIZER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_CRYSTALLIZER)
          .withSound(MekanismSounds.CHEMICAL_CRYSTALLIZER)
          .withEnergyConfig(MekanismConfig.usage.chemicalCrystallizer, MekanismConfig.storage.chemicalCrystallizer)
          .withCustomShape(BlockShapes.CHEMICAL_CRYSTALLIZER)
          .build();
    // Chemical Dissolution Chamber
    public static final Machine<TileEntityChemicalDissolutionChamber> CHEMICAL_DISSOLUTION_CHAMBER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_DISSOLUTION_CHAMBER, MekanismLang.DESCRIPTION_CHEMICAL_DISSOLUTION_CHAMBER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_DISSOLUTION_CHAMBER)
          .withSound(MekanismSounds.CHEMICAL_DISSOLUTION_CHAMBER)
          .withEnergyConfig(MekanismConfig.usage.chemicalDissolutionChamber, MekanismConfig.storage.chemicalDissolutionChamber)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS))
          .withCustomShape(BlockShapes.CHEMICAL_DISSOLUTION_CHAMBER)
          .build();
    // Chemical Infuser
    public static final Machine<TileEntityChemicalInfuser> CHEMICAL_INFUSER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_INFUSER, MekanismLang.DESCRIPTION_CHEMICAL_INFUSER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_INFUSER)
          .withSound(MekanismSounds.CHEMICAL_INFUSER)
          .withEnergyConfig(MekanismConfig.usage.chemicalInfuser, MekanismConfig.storage.chemicalInfuser)
          .withCustomShape(BlockShapes.CHEMICAL_INFUSER)
          .build();
    // Chemical Oxidizer
    public static final Machine<TileEntityChemicalOxidizer> CHEMICAL_OXIDIZER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_OXIDIZER, MekanismLang.DESCRIPTION_CHEMICAL_OXIDIZER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_OXIDIZER)
          .withSound(MekanismSounds.CHEMICAL_OXIDIZER)
          .withEnergyConfig(MekanismConfig.usage.oxidationChamber, MekanismConfig.storage.oxidationChamber)
          .withCustomShape(BlockShapes.CHEMICAL_OXIDIZER)
          .build();
    // Chemical Washer
    public static final Machine<TileEntityChemicalWasher> CHEMICAL_WASHER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_WASHER, MekanismLang.DESCRIPTION_CHEMICAL_WASHER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_WASHER)
          .withSound(MekanismSounds.CHEMICAL_WASHER)
          .withEnergyConfig(MekanismConfig.usage.chemicalWasher, MekanismConfig.storage.chemicalWasher)
          .withCustomShape(BlockShapes.CHEMICAL_WASHER)
          .build();
    // Rotary Condensentrator
    public static final Machine<TileEntityRotaryCondensentrator> ROTARY_CONDENSENTRATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ROTARY_CONDENSENTRATOR, MekanismLang.DESCRIPTION_ROTARY_CONDENSENTRATOR)
          .withGui(() -> MekanismContainerTypes.ROTARY_CONDENSENTRATOR)
          .withSound(MekanismSounds.ROTARY_CONDENSENTRATOR)
          .withEnergyConfig(MekanismConfig.usage.rotaryCondensentrator, MekanismConfig.storage.rotaryCondensentrator)
          .withCustomShape(BlockShapes.ROTARY_CONDENSENTRATOR)
          .build();
    // Electrolytic Separator
    public static final Machine<TileEntityElectrolyticSeparator> ELECTROLYTIC_SEPARATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ELECTROLYTIC_SEPARATOR, MekanismLang.DESCRIPTION_ELECTROLYTIC_SEPARATOR)
          .withGui(() -> MekanismContainerTypes.ELECTROLYTIC_SEPARATOR)
          .withSound(MekanismSounds.ELECTROLYTIC_SEPARATOR)
          .withEnergyConfig(() -> MekanismConfig.general.FROM_H2.get().multiply(2), MekanismConfig.storage.electrolyticSeparator)
          .withCustomShape(BlockShapes.ELECTROLYTIC_SEPARATOR)
          .build();
    // Digital Miner
    public static final Machine<TileEntityDigitalMiner> DIGITAL_MINER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.DIGITAL_MINER, MekanismLang.DESCRIPTION_DIGITAL_MINER)
          .withGui(() -> MekanismContainerTypes.DIGITAL_MINER)
          .withEnergyConfig(MekanismConfig.usage.digitalMiner, MekanismConfig.storage.digitalMiner)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.ANCHOR))
          .withCustomShape(BlockShapes.DIGITAL_MINER)
          .with(new AttributeCustomSelectionBox(false))
          .build();
    // Formulaic Assemblicator
    public static final Machine<TileEntityFormulaicAssemblicator> FORMULAIC_ASSEMBLICATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.FORMULAIC_ASSEMBLICATOR, MekanismLang.DESCRIPTION_FORMULAIC_ASSEMBLICATOR)
          .withGui(() -> MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR)
          .withEnergyConfig(MekanismConfig.usage.formulaicAssemblicator, MekanismConfig.storage.formulaicAssemblicator)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY))
          .withCustomContainerProvider(tile -> (i, inv, player) -> new FormulaicAssemblicatorContainer(i, inv, (TileEntityFormulaicAssemblicator) tile))
          .build();
    // Electric Pump
    public static final Machine<TileEntityElectricPump> ELECTRIC_PUMP = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ELECTRIC_PUMP, MekanismLang.DESCRIPTION_ELECTRIC_PUMP)
          .withGui(() -> MekanismContainerTypes.ELECTRIC_PUMP)
          .withEnergyConfig(MekanismConfig.usage.electricPump, MekanismConfig.storage.electricPump)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.FILTER))
          .withCustomShape(BlockShapes.ELECTRIC_PUMP)
          .build();
    // Fluidic Plenisher
    public static final Machine<TileEntityFluidicPlenisher> FLUIDIC_PLENISHER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.FLUIDIC_PLENISHER, MekanismLang.DESCRIPTION_FLUIDIC_PLENISHER)
          .withGui(() -> MekanismContainerTypes.FLUIDIC_PLENISHER)
          .withEnergyConfig(MekanismConfig.usage.fluidicPlenisher, MekanismConfig.storage.fluidicPlenisher)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY))
          .withCustomShape(BlockShapes.FLUIDIC_PLENISHER)
          .build();
    // Solar Neutron Activator
    public static final Machine<TileEntitySolarNeutronActivator> SOLAR_NEUTRON_ACTIVATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.SOLAR_NEUTRON_ACTIVATOR, MekanismLang.DESCRIPTION_SOLAR_NEUTRON_ACTIVATOR)
          .withGui(() -> MekanismContainerTypes.SOLAR_NEUTRON_ACTIVATOR)
          .without(AttributeParticleFX.class, AttributeUpgradeSupport.class)
          .withCustomShape(BlockShapes.SOLAR_NEUTRON_ACTIVATOR)
          .with(new AttributeCustomSelectionBox(true))
          .build();
    // Teleporter
    public static final Machine<TileEntityTeleporter> TELEPORTER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.TELEPORTER, MekanismLang.DESCRIPTION_TELEPORTER)
          .withGui(() -> MekanismContainerTypes.TELEPORTER)
          .withEnergyConfig(() -> TELEPORTER_USAGE, MekanismConfig.storage.teleporter)
          .withSupportedUpgrades(EnumSet.of(Upgrade.ANCHOR))
          .without(AttributeStateActive.class, AttributeStateFacing.class, AttributeParticleFX.class)
          .withLight(10)
          .build();
    // Chargepad
    public static final BlockTypeTile<TileEntityChargepad> CHARGEPAD = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.CHARGEPAD, MekanismLang.DESCRIPTION_CHARGEPAD)
          .withEnergyConfig(() -> TWENTY_FIVE, MekanismConfig.storage.chargePad)
          .withSound(MekanismSounds.CHARGEPAD).with(new AttributeStateActive(), new AttributeStateFacing())
          .withCustomShape(BlockShapes.CHARGEPAD)
          .build();
    // Laser
    public static final BlockTypeTile<TileEntityLaser> LASER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.LASER, MekanismLang.DESCRIPTION_LASER)
          .withEnergyConfig(MekanismConfig.usage.laser, MekanismConfig.storage.laser)
          .withSound(MekanismSounds.LASER).with(new AttributeStateActive(), new AttributeStateFacing(BlockStateProperties.FACING), new AttributeSecurity())
          .withCustomShape(BlockShapes.LASER)
          .build();
    // Laser Amplifier
    public static final BlockTypeTile<TileEntityLaserAmplifier> LASER_AMPLIFIER = BlockTileBuilder.createBlock(() -> MekanismTileEntityTypes.LASER_AMPLIFIER, MekanismLang.DESCRIPTION_LASER_AMPLIFIER)
          .withGui(() -> MekanismContainerTypes.LASER_AMPLIFIER).withEnergyConfig(null, () -> LARGE_LASER)
          .with(new AttributeStateFacing(BlockStateProperties.FACING),
                new AttributeRedstoneEmitter<>(TileEntityMekanism::getRedstoneLevel), new AttributeRedstone(), new AttributeComparator(), new AttributeSecurity())
          .withCustomShape(BlockShapes.LASER_AMPLIFIER).build();
    // Laser Tractor Beam
    public static final BlockTypeTile<TileEntityLaserTractorBeam> LASER_TRACTOR_BEAM = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.LASER_TRACTOR_BEAM, MekanismLang.DESCRIPTION_LASER_TRACTOR_BEAM)
          .withGui(() -> MekanismContainerTypes.LASER_TRACTOR_BEAM)
          .withEnergyConfig(null, () -> LARGE_LASER)
          .with(new AttributeStateFacing(BlockStateProperties.FACING), new AttributeComparator(), new AttributeSecurity(), new AttributeInventory())
          .withCustomShape(BlockShapes.LASER_AMPLIFIER)
          .build();
    // Resistive Heater
    public static final Machine<TileEntityResistiveHeater> RESISTIVE_HEATER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.RESISTIVE_HEATER, MekanismLang.DESCRIPTION_RESISTIVE_HEATER)
          .withGui(() -> MekanismContainerTypes.RESISTIVE_HEATER)
          .withEnergyConfig(() -> HUNDRED, null).without(AttributeComparator.class, AttributeUpgradeSupport.class)
          .withCustomShape(BlockShapes.RESISTIVE_HEATER)
          .withSound(MekanismSounds.RESISTIVE_HEATER)
          .build();
    // Seismic Vibrator
    public static final Machine<TileEntitySeismicVibrator> SEISMIC_VIBRATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.SEISMIC_VIBRATOR, MekanismLang.DESCRIPTION_SEISMIC_VIBRATOR)
          .withGui(() -> MekanismContainerTypes.SEISMIC_VIBRATOR)
          .withEnergyConfig(MekanismConfig.usage.seismicVibrator, MekanismConfig.storage.seismicVibrator)
          .without(AttributeComparator.class, AttributeParticleFX.class, AttributeUpgradeSupport.class)
          .withCustomShape(BlockShapes.SEISMIC_VIBRATOR)
          .with(new AttributeCustomSelectionBox(true))
          .build();
    // Personal Chest
    public static final BlockTypeTile<TileEntityPersonalChest> PERSONAL_CHEST = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.PERSONAL_CHEST, MekanismLang.DESCRIPTION_PERSONAL_CHEST)
          .withGui(() -> MekanismContainerTypes.PERSONAL_CHEST_BLOCK)
          .with(new AttributeSecurity(), new AttributeInventory(), new AttributeStateActive(), new AttributeStateFacing(), new AttributeCustomResistance(-1))
          .withCustomShape(BlockShapes.PERSONAL_CHEST)
          .build();
    // Fuelwood Heater
    public static final BlockTypeTile<TileEntityFuelwoodHeater> FUELWOOD_HEATER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.FUELWOOD_HEATER, MekanismLang.DESCRIPTION_FUELWOOD_HEATER)
          .withGui(() -> MekanismContainerTypes.FUELWOOD_HEATER)
          .with(new AttributeSecurity(), new AttributeInventory(), new AttributeStateActive(), new AttributeStateFacing(), new AttributeParticleFX()
                .add(ParticleTypes.SMOKE, rand -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52))
                .add(RedstoneParticleData.REDSTONE_DUST, rand -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52)))
          .build();
    // Oredictionificator
    public static final BlockTypeTile<TileEntityOredictionificator> OREDICTIONIFICATOR = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.OREDICTIONIFICATOR, MekanismLang.DESCRIPTION_OREDICTIONIFICATOR)
          .withGui(() -> MekanismContainerTypes.OREDICTIONIFICATOR)
          .with(new AttributeSecurity(), new AttributeInventory(), new AttributeStateActive(), new AttributeStateFacing(), new AttributeRedstone())
          .build();
    // Quantum Entangloporter
    public static final Machine<TileEntityQuantumEntangloporter> QUANTUM_ENTANGLOPORTER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.QUANTUM_ENTANGLOPORTER, MekanismLang.DESCRIPTION_QUANTUM_ENTANGLOPORTER)
          .withGui(() -> MekanismContainerTypes.QUANTUM_ENTANGLOPORTER)
          .withEnergyConfig(null, null)
          .withSupportedUpgrades(EnumSet.of(Upgrade.ANCHOR))
          .with(new AttributeStateFacing(BlockStateProperties.FACING))
          .without(AttributeStateActive.class, AttributeParticleFX.class, AttributeRedstone.class, AttributeComparator.class)
          .withCustomShape(BlockShapes.QUANTUM_ENTANGLOPORTER)
          .build();
    // Logistical Sorter
    public static final Machine<TileEntityLogisticalSorter> LOGISTICAL_SORTER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.LOGISTICAL_SORTER, MekanismLang.DESCRIPTION_LOGISTICAL_SORTER)
          .withGui(() -> MekanismContainerTypes.LOGISTICAL_SORTER)
          .withSupportedUpgrades(EnumSet.of(Upgrade.MUFFLING))
          .with(new AttributeStateFacing(BlockStateProperties.FACING))
          .withCustomShape(BlockShapes.LOGISTICAL_SORTER)
          .withSound(MekanismSounds.LOGISTICAL_SORTER)
          .build();
    // Security Desk
    public static final BlockTypeTile<TileEntitySecurityDesk> SECURITY_DESK = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.SECURITY_DESK, MekanismLang.DESCRIPTION_SECURITY_DESK)
          .withGui(() -> MekanismContainerTypes.SECURITY_DESK)
          .with(new AttributeInventory(), new AttributeStateFacing(), new AttributeCustomResistance(-1), new AttributeSecurity())
          .withCustomShape(BlockShapes.SECURITY_DESK)
          .with(new AttributeCustomSelectionBox(false))
          .build();
    // Modification Station
    public static final BlockTypeTile<TileEntityModificationStation> MODIFICATION_STATION = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.MODIFICATION_STATION, MekanismLang.DESCRIPTION_MODIFICATION_STATION)
          .withGui(() -> MekanismContainerTypes.MODIFICATION_STATION)
          .withEnergyConfig(MekanismConfig.usage.modificationStation, MekanismConfig.storage.modificationStation)
          .withCustomContainerProvider(tile -> (i, inv, player) -> new ModificationStationContainer(i, inv, (TileEntityModificationStation) tile))
          .with(new AttributeInventory(), new AttributeStateFacing(false), new AttributeRedstone(), new AttributeSecurity())
          .withCustomShape(BlockShapes.MODIFICATION_STATION)
          .with(new AttributeCustomSelectionBox(false))
          .build();
    // Isotopic Centrifuge
    public static final Machine<TileEntityIsotopicCentrifuge> ISOTOPIC_CENTRIFUGE = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ISOTOPIC_CENTRIFUGE, MekanismLang.DESCRIPTION_ISOTOPIC_CENTRIFUGE)
          .withGui(() -> MekanismContainerTypes.ISOTOPIC_CENTRIFUGE)
          .withEnergyConfig(MekanismConfig.usage.isotopicCentrifuge, MekanismConfig.storage.isotopicCentrifuge)
          .withSound(MekanismSounds.ISOTOPIC_CENTRIFUGE)
          .withCustomShape(BlockShapes.ISOTOPIC_CENTRIFUGE)
          .build();
    // Nutritional Liquifier
    public static final Machine<TileEntityNutritionalLiquifier> NUTRITIONAL_LIQUIFIER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.NUTRITIONAL_LIQUIFIER, MekanismLang.DESCRIPTION_NUTRITIONAL_LIQUIFIER)
          .withGui(() -> MekanismContainerTypes.NUTRITIONAL_LIQUIFIER)
          .withEnergyConfig(MekanismConfig.usage.nutritionalLiquifier, MekanismConfig.storage.nutritionalLiquifier)
          .withSound(MekanismSounds.NUTRITIONAL_LIQUIFIER)
          .build();
    // Antiprotonic Nucleosynthesizer
    public static final Machine<TileEntityAntiprotonicNucleosynthesizer> ANTIPROTONIC_NUCLEOSYNTHESIZER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER, MekanismLang.DESCRIPTION_ANTIPROTONIC_NUCLEOSYNTHESIZER)
          .withGui(() -> MekanismContainerTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER)
          .withEnergyConfig(MekanismConfig.usage.antiprotonicNucleosynthesizer, MekanismConfig.storage.antiprotonicNucleosynthesizer)
          .withSound(MekanismSounds.ANTIPROTONIC_NUCLEOSYNTHESIZER)
          .withSupportedUpgrades(EnumSet.of(Upgrade.MUFFLING))
          .withCustomShape(BlockShapes.ANTIPROTONIC_NUCLEOSYNTHESIZER)
          .build();

    // QIO Drive Array
    public static final BlockTypeTile<TileEntityQIODriveArray> QIO_DRIVE_ARRAY = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.QIO_DRIVE_ARRAY, MekanismLang.DESCRIPTION_QIO_DRIVE_ARRAY)
          .withGui(() -> MekanismContainerTypes.QIO_DRIVE_ARRAY)
          .withCustomShape(BlockShapes.QIO_DRIVE_ARRAY)
          .with(new AttributeStateFacing(), new AttributeSecurity(), new AttributeInventory(), new AttributeStateActive())
          .build();
    // QIO Dashboard
    public static final BlockTypeTile<TileEntityQIODashboard> QIO_DASHBOARD = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.QIO_DASHBOARD, MekanismLang.DESCRIPTION_QIO_DASHBOARD)
          .withGui(() -> MekanismContainerTypes.QIO_DASHBOARD)
          .withCustomShape(BlockShapes.QIO_DASHBOARD)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE), new AttributeSecurity(), new AttributeStateActive())
          .withCustomContainerProvider(tile -> (i, inv, player) -> new QIODashboardContainer(i, inv, (TileEntityQIODashboard) tile))
          .build();
    // QIO Importer
    public static final BlockTypeTile<TileEntityQIOImporter> QIO_IMPORTER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.QIO_IMPORTER, MekanismLang.DESCRIPTION_QIO_IMPORTER)
          .withGui(() -> MekanismContainerTypes.QIO_IMPORTER)
          .withCustomShape(BlockShapes.QIO_IMPORTER)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE), new AttributeSecurity(), new AttributeRedstone(), new AttributeStateActive())
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED))
          .build();
    // QIO Exporter
    public static final BlockTypeTile<TileEntityQIOExporter> QIO_EXPORTER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.QIO_EXPORTER, MekanismLang.DESCRIPTION_QIO_EXPORTER)
          .withGui(() -> MekanismContainerTypes.QIO_EXPORTER)
          .withCustomShape(BlockShapes.QIO_EXPORTER)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE), new AttributeSecurity(), new AttributeRedstone(), new AttributeStateActive())
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED))
          .build();
    // QIO Redstone Adapter
    public static final BlockTypeTile<TileEntityQIORedstoneAdapter> QIO_REDSTONE_ADAPTER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.QIO_REDSTONE_ADAPTER, MekanismLang.DESCRIPTION_QIO_REDSTONE_ADAPTER)
          .withGui(() -> MekanismContainerTypes.QIO_REDSTONE_ADAPTER)
          .withCustomShape(BlockShapes.QIO_REDSTONE_ADAPTER)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE), new AttributeSecurity(), new AttributeStateActive())
          .with(new AttributeRedstoneEmitter<>(tile -> tile.isPowering() ? 15 : 0))
          .build();

    // Dynamic Tank
    public static final BlockTypeTile<TileEntityDynamicTank> DYNAMIC_TANK = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.DYNAMIC_TANK, MekanismLang.DESCRIPTION_DYNAMIC_TANK)
          .withGui(() -> MekanismContainerTypes.DYNAMIC_TANK)
          .withNamedContainerProvider(tile -> new ContainerProvider(MekanismLang.DYNAMIC_TANK, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.DYNAMIC_TANK, i, inv, tile)))
          .with(new AttributeInventory(), new AttributeMultiblock(), AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Dynamic Valve
    public static final BlockTypeTile<TileEntityDynamicValve> DYNAMIC_VALVE = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.DYNAMIC_VALVE, MekanismLang.DESCRIPTION_DYNAMIC_VALVE)
          .withGui(() -> MekanismContainerTypes.DYNAMIC_TANK)
          .withNamedContainerProvider(tile -> new ContainerProvider(MekanismLang.DYNAMIC_TANK, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.DYNAMIC_TANK, i, inv, tile)))
          .with(new AttributeInventory(), new AttributeComparator(), new AttributeMultiblock(), AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Boiler Casing
    public static final BlockTypeTile<TileEntityBoilerCasing> BOILER_CASING = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.BOILER_CASING, MekanismLang.DESCRIPTION_BOILER_CASING)
          .withGui(() -> MekanismContainerTypes.THERMOELECTRIC_BOILER)
          .withNamedContainerProvider(tile -> new ContainerProvider(MekanismLang.BOILER, (i, inv, player) -> new ThermoelectricBoilerContainer(i, inv, (TileEntityBoilerCasing) tile)))
          .with(new AttributeMultiblock(), AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Boiler Valve
    public static final BlockTypeTile<TileEntityBoilerValve> BOILER_VALVE = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.BOILER_VALVE, MekanismLang.DESCRIPTION_BOILER_VALVE)
          .withGui(() -> MekanismContainerTypes.THERMOELECTRIC_BOILER)
          .withNamedContainerProvider(tile -> new ContainerProvider(MekanismLang.BOILER, (i, inv, player) -> new ThermoelectricBoilerContainer(i, inv, (TileEntityBoilerCasing) tile)))
          .with(new AttributeInventory(), new AttributeComparator(), new AttributeStateBoilerValveMode(), new AttributeMultiblock(), AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Pressure Disperser
    public static final BlockTypeTile<TileEntityPressureDisperser> PRESSURE_DISPERSER =
          BlockTileBuilder.createBlock(() -> MekanismTileEntityTypes.PRESSURE_DISPERSER, MekanismLang.DESCRIPTION_PRESSURE_DISPERSER)
                .with(new AttributeMultiblock(), AttributeMobSpawn.NEVER)
                .build();
    // Superheating Element
    public static final BlockTypeTile<TileEntitySuperheatingElement> SUPERHEATING_ELEMENT =
          BlockTileBuilder.createBlock(() -> MekanismTileEntityTypes.SUPERHEATING_ELEMENT, MekanismLang.DESCRIPTION_SUPERHEATING_ELEMENT)
                .with(new AttributeStateActive(), new AttributeMultiblock(), AttributeMobSpawn.NEVER)
                .build();
    // Induction Casing
    public static final BlockTypeTile<TileEntityInductionCasing> INDUCTION_CASING = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.INDUCTION_CASING, MekanismLang.DESCRIPTION_INDUCTION_CASING)
          .withGui(() -> MekanismContainerTypes.INDUCTION_MATRIX)
          .withNamedContainerProvider(tile -> new ContainerProvider(MekanismLang.MATRIX, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.INDUCTION_MATRIX, i, inv, tile)))
          .with(new AttributeInventory(), new AttributeComparator(), new AttributeMultiblock(), AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Induction Port
    public static final BlockTypeTile<TileEntityInductionPort> INDUCTION_PORT = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.INDUCTION_PORT, MekanismLang.DESCRIPTION_INDUCTION_PORT)
          .withGui(() -> MekanismContainerTypes.INDUCTION_MATRIX)
          .withNamedContainerProvider(tile -> new ContainerProvider(MekanismLang.MATRIX, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.INDUCTION_MATRIX, i, inv, tile)))
          .with(new AttributeInventory(), new AttributeComparator(), new AttributeStateActive(), new AttributeMultiblock(), AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Thermal Evaporation Controller
    public static final BlockTypeTile<TileEntityThermalEvaporationController> THERMAL_EVAPORATION_CONTROLLER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.THERMAL_EVAPORATION_CONTROLLER, MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_CONTROLLER)
          .withGui(() -> MekanismContainerTypes.THERMAL_EVAPORATION_CONTROLLER)
          .with(new AttributeInventory(), new AttributeStateActive(), new AttributeStateFacing(), new AttributeCustomResistance(9), new AttributeMultiblock(), AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Thermal Evaporation Valve
    public static final BlockTypeTile<TileEntityThermalEvaporationValve> THERMAL_EVAPORATION_VALVE = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.THERMAL_EVAPORATION_VALVE, MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_VALVE)
          .with(new AttributeComparator(), new AttributeCustomResistance(9), new AttributeMultiblock(), AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Thermal Evaporation Block
    public static final BlockTypeTile<TileEntityThermalEvaporationBlock> THERMAL_EVAPORATION_BLOCK = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.THERMAL_EVAPORATION_BLOCK, MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_BLOCK)
          .with(new AttributeCustomResistance(9), new AttributeMultiblock(), AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Steel Casing
    public static final BlockType STEEL_CASING = BlockTypeBuilder
          .createBlock(MekanismLang.DESCRIPTION_STEEL_CASING)
          .build();
    // Radioactive Waste Barrel
    public static final BlockTypeTile<TileEntityRadioactiveWasteBarrel> RADIOACTIVE_WASTE_BARREL = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.RADIOACTIVE_WASTE_BARREL, MekanismLang.DESCRIPTION_RADIOACTIVE_WASTE_BARREL)
          .with(new AttributeStateActive(), new AttributeStateFacing(), new AttributeComparator())
          .withCustomShape(BlockShapes.RADIOACTIVE_WASTE_BARREL)
          .build();
    // Industrial Alarm
    public static final BlockTypeTile<TileEntityIndustrialAlarm> INDUSTRIAL_ALARM = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.INDUSTRIAL_ALARM, MekanismLang.DESCRIPTION_INDUSTRIAL_ALARM)
          .withSound(MekanismSounds.INDUSTRIAL_ALARM)
          .withCustomShape(BlockShapes.INDUSTRIAL_ALARM)
          .with(new AttributeStateActive(), new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE), new AttributeRedstone())
          .build();
    // Structural Glass
    public static final BlockTypeTile<TileEntityStructuralGlass> STRUCTURAL_GLASS = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.STRUCTURAL_GLASS, MekanismLang.DESCRIPTION_STRUCTURAL_GLASS)
          .with(new AttributeMultiblock(), AttributeMobSpawn.NEVER)
          .build();
    // SPS Casing
    public static final BlockTypeTile<TileEntitySPSCasing> SPS_CASING = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.SPS_CASING, MekanismLang.DESCRIPTION_SPS_CASING)
          .withGui(() -> MekanismContainerTypes.SPS)
          .withSound(MekanismSounds.SPS)
          .with(new AttributeMultiblock(), AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // SPS Port
    public static final BlockTypeTile<TileEntitySPSPort> SPS_PORT = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.SPS_PORT, MekanismLang.DESCRIPTION_SPS_PORT)
          .withGui(() -> MekanismContainerTypes.SPS)
          .withSound(MekanismSounds.SPS)
          .withEnergyConfig(() -> FloatingLong.ZERO, MekanismConfig.storage.spsPort)
          .with(new AttributeMultiblock(), AttributeMobSpawn.WHEN_NOT_FORMED, new AttributeStateActive())
          .build();
    // Supercharged Coil
    public static final BlockTypeTile<TileEntitySuperchargedCoil> SUPERCHARGED_COIL = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.SUPERCHARGED_COIL, MekanismLang.DESCRIPTION_SUPERCHARGED_COIL)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE))
          .withCustomShape(BlockShapes.SUPERCHARGED_COIL)
          .build();

    // Induction Cells
    public static final BlockTypeTile<TileEntityInductionCell> BASIC_INDUCTION_CELL = createInductionCell(InductionCellTier.BASIC, () -> MekanismTileEntityTypes.BASIC_INDUCTION_CELL);
    public static final BlockTypeTile<TileEntityInductionCell> ADVANCED_INDUCTION_CELL = createInductionCell(InductionCellTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_INDUCTION_CELL);
    public static final BlockTypeTile<TileEntityInductionCell> ELITE_INDUCTION_CELL = createInductionCell(InductionCellTier.ELITE, () -> MekanismTileEntityTypes.ELITE_INDUCTION_CELL);
    public static final BlockTypeTile<TileEntityInductionCell> ULTIMATE_INDUCTION_CELL = createInductionCell(InductionCellTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_INDUCTION_CELL);

    // Induction Provider
    public static final BlockTypeTile<TileEntityInductionProvider> BASIC_INDUCTION_PROVIDER = createInductionProvider(InductionProviderTier.BASIC, () -> MekanismTileEntityTypes.BASIC_INDUCTION_PROVIDER);
    public static final BlockTypeTile<TileEntityInductionProvider> ADVANCED_INDUCTION_PROVIDER = createInductionProvider(InductionProviderTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_INDUCTION_PROVIDER);
    public static final BlockTypeTile<TileEntityInductionProvider> ELITE_INDUCTION_PROVIDER = createInductionProvider(InductionProviderTier.ELITE, () -> MekanismTileEntityTypes.ELITE_INDUCTION_PROVIDER);
    public static final BlockTypeTile<TileEntityInductionProvider> ULTIMATE_INDUCTION_PROVIDER = createInductionProvider(InductionProviderTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_INDUCTION_PROVIDER);

    // Bins
    public static final Machine<TileEntityBin> BASIC_BIN = createBin(BinTier.BASIC, () -> MekanismTileEntityTypes.BASIC_BIN, () -> MekanismBlocks.ADVANCED_BIN);
    public static final Machine<TileEntityBin> ADVANCED_BIN = createBin(BinTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_BIN, () -> MekanismBlocks.ELITE_BIN);
    public static final Machine<TileEntityBin> ELITE_BIN = createBin(BinTier.ELITE, () -> MekanismTileEntityTypes.ELITE_BIN, () -> MekanismBlocks.ULTIMATE_BIN);
    public static final Machine<TileEntityBin> ULTIMATE_BIN = createBin(BinTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_BIN, null);
    public static final Machine<TileEntityBin> CREATIVE_BIN = createBin(BinTier.CREATIVE, () -> MekanismTileEntityTypes.CREATIVE_BIN, null);

    // Energy Cubes
    public static final Machine<TileEntityEnergyCube> BASIC_ENERGY_CUBE = createEnergyCube(EnergyCubeTier.BASIC, () -> MekanismTileEntityTypes.BASIC_ENERGY_CUBE, () -> MekanismBlocks.ADVANCED_ENERGY_CUBE);
    public static final Machine<TileEntityEnergyCube> ADVANCED_ENERGY_CUBE = createEnergyCube(EnergyCubeTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_ENERGY_CUBE, () -> MekanismBlocks.ELITE_ENERGY_CUBE);
    public static final Machine<TileEntityEnergyCube> ELITE_ENERGY_CUBE = createEnergyCube(EnergyCubeTier.ELITE, () -> MekanismTileEntityTypes.ELITE_ENERGY_CUBE, () -> MekanismBlocks.ULTIMATE_ENERGY_CUBE);
    public static final Machine<TileEntityEnergyCube> ULTIMATE_ENERGY_CUBE = createEnergyCube(EnergyCubeTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_ENERGY_CUBE, null);
    public static final Machine<TileEntityEnergyCube> CREATIVE_ENERGY_CUBE = createEnergyCube(EnergyCubeTier.CREATIVE, () -> MekanismTileEntityTypes.CREATIVE_ENERGY_CUBE, null);

    // Fluid Tanks
    public static final Machine<TileEntityFluidTank> BASIC_FLUID_TANK = createFluidTank(FluidTankTier.BASIC, () -> MekanismTileEntityTypes.BASIC_FLUID_TANK, () -> MekanismBlocks.ADVANCED_FLUID_TANK);
    public static final Machine<TileEntityFluidTank> ADVANCED_FLUID_TANK = createFluidTank(FluidTankTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_FLUID_TANK, () -> MekanismBlocks.ELITE_FLUID_TANK);
    public static final Machine<TileEntityFluidTank> ELITE_FLUID_TANK = createFluidTank(FluidTankTier.ELITE, () -> MekanismTileEntityTypes.ELITE_FLUID_TANK, () -> MekanismBlocks.ULTIMATE_FLUID_TANK);
    public static final Machine<TileEntityFluidTank> ULTIMATE_FLUID_TANK = createFluidTank(FluidTankTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_FLUID_TANK, null);
    public static final Machine<TileEntityFluidTank> CREATIVE_FLUID_TANK = createFluidTank(FluidTankTier.CREATIVE, () -> MekanismTileEntityTypes.CREATIVE_FLUID_TANK, null);

    // Chemical Tanks
    public static final Machine<TileEntityChemicalTank> BASIC_CHEMICAL_TANK = createChemicalTank(ChemicalTankTier.BASIC, () -> MekanismTileEntityTypes.BASIC_CHEMICAL_TANK, () -> MekanismBlocks.ADVANCED_CHEMICAL_TANK);
    public static final Machine<TileEntityChemicalTank> ADVANCED_CHEMICAL_TANK = createChemicalTank(ChemicalTankTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_CHEMICAL_TANK, () -> MekanismBlocks.ELITE_CHEMICAL_TANK);
    public static final Machine<TileEntityChemicalTank> ELITE_CHEMICAL_TANK = createChemicalTank(ChemicalTankTier.ELITE, () -> MekanismTileEntityTypes.ELITE_CHEMICAL_TANK, () -> MekanismBlocks.ULTIMATE_CHEMICAL_TANK);
    public static final Machine<TileEntityChemicalTank> ULTIMATE_CHEMICAL_TANK = createChemicalTank(ChemicalTankTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_CHEMICAL_TANK, null);
    public static final Machine<TileEntityChemicalTank> CREATIVE_CHEMICAL_TANK = createChemicalTank(ChemicalTankTier.CREATIVE, () -> MekanismTileEntityTypes.CREATIVE_CHEMICAL_TANK, null);

    static {
        for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
            for (FactoryType type : EnumUtils.FACTORY_TYPES) {
                FACTORIES.put(tier, type,
                      FactoryBuilder.createFactory(() -> MekanismTileEntityTypes.getFactoryTile(tier, type), () -> MekanismContainerTypes.FACTORY, type.getBaseMachine(), tier).build());
            }
        }
    }

    public static Factory<?> getFactory(FactoryTier tier, FactoryType type) {
        return FACTORIES.get(tier, type);
    }

    private static <TILE extends TileEntityInductionCell> BlockTypeTile<TILE> createInductionCell(InductionCellTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile) {
        return BlockTileBuilder.createBlock(tile, MekanismLang.DESCRIPTION_INDUCTION_CELL)
              .withEnergyConfig(tier::getMaxEnergy)
              .with(new AttributeTier<>(tier), AttributeMobSpawn.NEVER)
              .build();
    }

    private static <TILE extends TileEntityInductionProvider> BlockTypeTile<TILE> createInductionProvider(InductionProviderTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile) {
        return BlockTileBuilder.createBlock(tile, MekanismLang.DESCRIPTION_INDUCTION_PROVIDER)
              .with(new AttributeTier<>(tier), AttributeMobSpawn.NEVER)
              .build();
    }

    private static <TILE extends TileEntityBin> Machine<TILE> createBin(BinTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        return MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_BIN)
              .with(new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock))
              .without(AttributeParticleFX.class, AttributeSecurity.class, AttributeUpgradeSupport.class, AttributeRedstone.class)
              .build();
    }

    private static <TILE extends TileEntityEnergyCube> Machine<TILE> createEnergyCube(EnergyCubeTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        return MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_ENERGY_CUBE)
              .withGui(() -> MekanismContainerTypes.ENERGY_CUBE)
              .withEnergyConfig(tier::getMaxEnergy)
              .with(new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock), new AttributeStateFacing(BlockStateProperties.FACING))
              .without(AttributeParticleFX.class, AttributeStateActive.class, AttributeUpgradeSupport.class)
              .build();
    }

    private static <TILE extends TileEntityFluidTank> Machine<TILE> createFluidTank(FluidTankTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        return MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_FLUID_TANK)
              .withGui(() -> MekanismContainerTypes.FLUID_TANK)
              .withCustomShape(BlockShapes.FLUID_TANK)
              .with(new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock))
              .without(AttributeParticleFX.class, AttributeStateFacing.class, AttributeRedstone.class, AttributeUpgradeSupport.class)
              .build();
    }

    private static <TILE extends TileEntityChemicalTank> Machine<TILE> createChemicalTank(ChemicalTankTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        return MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_CHEMICAL_TANK)
              .withGui(() -> MekanismContainerTypes.CHEMICAL_TANK)
              .withCustomShape(BlockShapes.CHEMICAL_TANK)
              .with(new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock))
              .without(AttributeParticleFX.class, AttributeStateActive.class, AttributeUpgradeSupport.class)
              .build();
    }
}
