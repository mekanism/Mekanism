package mekanism.common.registries;

import java.util.EnumSet;
import java.util.function.Supplier;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import mekanism.api.Pos3D;
import mekanism.api.Upgrade;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.block.attribute.Attributes.AttributeCustomResistance;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.attribute.Attributes.AttributeNoMobSpawn;
import mekanism.common.block.attribute.Attributes.AttributeRedstone;
import mekanism.common.block.attribute.Attributes.AttributeRedstoneEmitter;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.BlockShapes;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.BlockTypeTile.BlockTileBuilder;
import mekanism.common.content.blocktype.Factory;
import mekanism.common.content.blocktype.Factory.FactoryBuilder;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.content.blocktype.Machine.MachineBuilder;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.DigitalMinerContainer;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.container.tile.OredictionificatorContainer;
import mekanism.common.inventory.container.tile.PersonalChestTileContainer;
import mekanism.common.inventory.container.tile.QuantumEntangloporterContainer;
import mekanism.common.inventory.container.tile.SecurityDeskContainer;
import mekanism.common.inventory.container.tile.TeleporterContainer;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
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
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionPort;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPressurizedReactionChamber;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.tile.TileEntityThermalEvaporationValve;
import mekanism.common.tile.laser.TileEntityLaser;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.laser.TileEntityLaserTractorBeam;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;

public class MekanismBlockTypes {

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
        .withEnergyConfig(() -> MekanismConfig.general.FROM_H2.get() * 2, MekanismConfig.storage.electrolyticSeparator::get)
        .withCustomShape(BlockShapes.ELECTROLYTIC_SEPARATOR)
        .build();
    // Digital Miner
    public static final Machine<TileEntityDigitalMiner> DIGITAL_MINER = MachineBuilder
        .createMachine(() -> MekanismTileEntityTypes.DIGITAL_MINER, MekanismLang.DESCRIPTION_DIGITAL_MINER)
        .withGui(() -> MekanismContainerTypes.DIGITAL_MINER)
        .withEnergyConfig(MekanismConfig.usage.digitalMiner::get, MekanismConfig.storage.digitalMiner::get)
        .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.ANCHOR))
        .withCustomContainer((tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()), (i, inv, player) -> new DigitalMinerContainer(i, inv, (TileEntityDigitalMiner) tile)))
        .withCustomShape(BlockShapes.DIGITAL_MINER)
        .build();
    // Formulaic Assemblicator
    public static final Machine<TileEntityFormulaicAssemblicator> FORMULAIC_ASSEMBLICATOR = MachineBuilder
        .createMachine(() -> MekanismTileEntityTypes.FORMULAIC_ASSEMBLICATOR, MekanismLang.DESCRIPTION_FORMULAIC_ASSEMBLICATOR)
        .withGui(() -> MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR)
        .withEnergyConfig(MekanismConfig.usage.formulaicAssemblicator, MekanismConfig.storage.formulaicAssemblicator)
        .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY))
        .withCustomContainer((tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()), (i, inv, player) -> new FormulaicAssemblicatorContainer(i, inv, (TileEntityFormulaicAssemblicator) tile)))
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
        .without(AttributeParticleFX.class)
        .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED))
        .withCustomShape(BlockShapes.SOLAR_NEUTRON_ACTIVATOR)
        .build();
    // Teleporter
    public static final Machine<TileEntityTeleporter> TELEPORTER = MachineBuilder
        .createMachine(() -> MekanismTileEntityTypes.TELEPORTER, MekanismLang.DESCRIPTION_TELEPORTER)
        .withGui(() -> MekanismContainerTypes.TELEPORTER)
        .withEnergyConfig(() -> 12500, MekanismConfig.storage.teleporter)
        .withSupportedUpgrades(EnumSet.of(Upgrade.ANCHOR))
        .without(AttributeStateActive.class, AttributeStateFacing.class, AttributeParticleFX.class)
        .withCustomContainer((tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()), (i, inv, player) -> new TeleporterContainer(i, inv, (TileEntityTeleporter) tile)))
        .build();
    // Chargepad
    public static final BlockTypeTile<TileEntityChargepad> CHARGEPAD = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.CHARGEPAD, MekanismLang.DESCRIPTION_CHARGEPAD)
        .withEnergyConfig(() -> 25, MekanismConfig.storage.chargePad)
        .withSound(MekanismSounds.CHARGEPAD).with(new AttributeStateActive(), new AttributeStateFacing())
        .withCustomShape(BlockShapes.CHARGEPAD)
        .build();
    // Laser
    public static final BlockTypeTile<TileEntityLaser> LASER = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.LASER, MekanismLang.DESCRIPTION_LASER)
        .withEnergyConfig(MekanismConfig.usage.laser, MekanismConfig.storage.laser)
        .withSound(MekanismSounds.LASER).with(new AttributeStateActive(), new AttributeStateFacing(BlockStateHelper.facingProperty))
        .withCustomShape(BlockShapes.LASER)
        .build();
    // Laser Amplifier
    public static final BlockTypeTile<TileEntityLaserAmplifier> LASER_AMPLIFIER = BlockTileBuilder.createBlock(() -> MekanismTileEntityTypes.LASER_AMPLIFIER, MekanismLang.DESCRIPTION_LASER_AMPLIFIER)
        .withGui(() -> MekanismContainerTypes.LASER_AMPLIFIER).withEnergyConfig(null, () -> 5E9)
        .with(new AttributeStateFacing(BlockStateHelper.facingProperty),
            new AttributeRedstoneEmitter<>((tile) -> tile.getRedstoneLevel()), new AttributeRedstone(), new AttributeComparator(), new AttributeSecurity())
        .withCustomShape(BlockShapes.LASER_AMPLIFIER).build();
    // Laser Tractor Beam
    public static final BlockTypeTile<TileEntityLaserTractorBeam> LASER_TRACTOR_BEAM = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.LASER_TRACTOR_BEAM, MekanismLang.DESCRIPTION_LASER_TRACTOR_BEAM)
        .withGui(() -> MekanismContainerTypes.LASER_TRACTOR_BEAM)
        .withEnergyConfig(null, () -> 5E9)
        .with(new AttributeStateFacing(BlockStateHelper.facingProperty), new AttributeComparator(), new AttributeSecurity(), new AttributeInventory())
        .withCustomShape(BlockShapes.LASER_AMPLIFIER)
        .build();
    // Resistive Heater
    public static final Machine<TileEntityResistiveHeater> RESISTIVE_HEATER = MachineBuilder
        .createMachine(() -> MekanismTileEntityTypes.RESISTIVE_HEATER, MekanismLang.DESCRIPTION_RESISTIVE_HEATER)
        .withGui(() -> MekanismContainerTypes.RESISTIVE_HEATER)
        .withEnergyConfig(() -> 100, null).without(AttributeComparator.class, AttributeUpgradeSupport.class)
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
        .build();
    // Personal Chest
    public static final BlockTypeTile<TileEntityPersonalChest> PERSONAL_CHEST = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.PERSONAL_CHEST, MekanismLang.DESCRIPTION_PERSONAL_CHEST)
        .withGui(() -> MekanismContainerTypes.PERSONAL_CHEST_BLOCK)
        .withCustomContainer((tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()), (i, inv, player) -> new PersonalChestTileContainer(i, inv, (TileEntityPersonalChest) tile)))
        .with(new AttributeSecurity(), new AttributeInventory(), new AttributeStateActive(), new AttributeStateFacing(), new AttributeCustomResistance(-1F))
        .withCustomShape(BlockShapes.PERSONAL_CHEST)
        .build();
    // Fuelwood Heater
    public static final BlockTypeTile<TileEntityFuelwoodHeater> FUELWOOD_HEATER = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.FUELWOOD_HEATER, MekanismLang.DESCRIPTION_FUELWOOD_HEATER)
        .withGui(() -> MekanismContainerTypes.FUELWOOD_HEATER)
        .with(new AttributeSecurity(), new AttributeInventory(), new AttributeStateActive(), new AttributeStateFacing(), new AttributeParticleFX()
            .add(ParticleTypes.SMOKE, (rand) -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52))
            .add(RedstoneParticleData.REDSTONE_DUST, (rand) -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52)))
        .build();
    // Oredictionificator
    public static final BlockTypeTile<TileEntityOredictionificator> OREDICTIONIFICATOR = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.OREDICTIONIFICATOR, MekanismLang.DESCRIPTION_OREDICTIONIFICATOR)
        .withGui(() -> MekanismContainerTypes.OREDICTIONIFICATOR)
        .withCustomContainer((tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()), (i, inv, player) -> new OredictionificatorContainer(i, inv, (TileEntityOredictionificator) tile)))
        .with(new AttributeSecurity(), new AttributeInventory(), new AttributeStateActive(), new AttributeStateFacing(), new AttributeRedstone())
        .build();
    // Quantum Entangloporter
    public static final Machine<TileEntityQuantumEntangloporter> QUANTUM_ENTANGLOPORTER = MachineBuilder
        .createMachine(() -> MekanismTileEntityTypes.QUANTUM_ENTANGLOPORTER, MekanismLang.DESCRIPTION_QUANTUM_ENTANGLOPORTER)
        .withGui(() -> MekanismContainerTypes.QUANTUM_ENTANGLOPORTER)
        .withEnergyConfig(null, null)
        .withSupportedUpgrades(EnumSet.of(Upgrade.ANCHOR))
        .with(new AttributeStateFacing(BlockStateHelper.facingProperty))
        .without(AttributeStateActive.class, AttributeParticleFX.class, AttributeRedstone.class, AttributeComparator.class)
        .withCustomContainer((tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()), (i, inv, player) -> new QuantumEntangloporterContainer(i, inv, (TileEntityQuantumEntangloporter) tile)))
        .build();
    // Logistical Sorter
    public static final Machine<TileEntityLogisticalSorter> LOGISTICAL_SORTER = MachineBuilder
        .createMachine(() -> MekanismTileEntityTypes.LOGISTICAL_SORTER, MekanismLang.DESCRIPTION_LOGISTICAL_SORTER)
        .withGui(() -> MekanismContainerTypes.LOGISTICAL_SORTER)
        .withSupportedUpgrades(EnumSet.of(Upgrade.MUFFLING))
        .with(new AttributeStateFacing(BlockStateHelper.facingProperty))
        .withCustomContainer((tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()), (i, inv, player) -> new EmptyTileContainer<>(MekanismContainerTypes.LOGISTICAL_SORTER, i, inv, tile)))
        .withCustomShape(BlockShapes.LOGISTICAL_SORTER)
        .withSound(MekanismSounds.LOGISTICAL_SORTER)
        .build();
    // Security Desk
    public static final BlockTypeTile<TileEntitySecurityDesk> SECURITY_DESK = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.SECURITY_DESK, MekanismLang.DESCRIPTION_SECURITY_DESK)
        .withGui(() -> MekanismContainerTypes.SECURITY_DESK)
        .withCustomContainer((tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()), (i, inv, player) -> new SecurityDeskContainer(i, inv, (TileEntitySecurityDesk) tile)))
        .with(new AttributeInventory(), new AttributeStateFacing(), new AttributeCustomResistance(-1F))
        .withCustomShape(BlockShapes.SECURITY_DESK)
        .build();

    // Dynamic Tank
    public static final BlockTypeTile<TileEntityDynamicTank> DYNAMIC_TANK = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.DYNAMIC_TANK, MekanismLang.DESCRIPTION_DYNAMIC_TANK)
        .withGui(() -> MekanismContainerTypes.DYNAMIC_TANK)
        .withCustomContainer((tile) -> new ContainerProvider(MekanismLang.DYNAMIC_TANK, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.DYNAMIC_TANK, i, inv, tile)))
        .with(new AttributeInventory())
        .build();
    // Dynamic Valve
    public static final BlockTypeTile<TileEntityDynamicValve> DYNAMIC_VALVE = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.DYNAMIC_VALVE, MekanismLang.DESCRIPTION_DYNAMIC_VALVE)
        .withGui(() -> MekanismContainerTypes.DYNAMIC_TANK)
        .withCustomContainer((tile) -> new ContainerProvider(MekanismLang.DYNAMIC_TANK, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.DYNAMIC_TANK, i, inv, tile)))
        .with(new AttributeInventory(), new AttributeComparator())
        .build();
    // Boiler Casing
    public static final BlockTypeTile<TileEntityBoilerCasing> BOILER_CASING = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.BOILER_CASING, MekanismLang.DESCRIPTION_BOILER_CASING)
        .withGui(() -> MekanismContainerTypes.THERMOELECTRIC_BOILER)
        .withCustomContainer((tile) -> new ContainerProvider(MekanismLang.BOILER, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.THERMOELECTRIC_BOILER, i, inv, tile)))
        .build();
    // Boiler Valve
    public static final BlockTypeTile<TileEntityBoilerValve> BOILER_VALVE = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.BOILER_VALVE, MekanismLang.DESCRIPTION_BOILER_VALVE)
        .withGui(() -> MekanismContainerTypes.THERMOELECTRIC_BOILER)
        .withCustomContainer((tile) -> new ContainerProvider(MekanismLang.BOILER, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.THERMOELECTRIC_BOILER, i, inv, tile)))
        .with(new AttributeInventory(), new AttributeComparator())
        .build();
    // Superheating Element
    public static final BlockTypeTile<TileEntitySuperheatingElement> SUPERHEATING_ELEMENT =
        BlockTileBuilder.createBlock(() -> MekanismTileEntityTypes.SUPERHEATING_ELEMENT, MekanismLang.DESCRIPTION_SUPERHEATING_ELEMENT).with(new AttributeStateActive()).build();
    // Induction Casing
    public static final BlockTypeTile<TileEntityInductionCasing> INDUCTION_CASING = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.INDUCTION_CASING, MekanismLang.DESCRIPTION_INDUCTION_CASING)
        .withGui(() -> MekanismContainerTypes.INDUCTION_MATRIX)
        .withCustomContainer((tile) -> new ContainerProvider(MekanismLang.MATRIX, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.INDUCTION_MATRIX, i, inv, tile)))
        .with(new AttributeInventory(), new AttributeComparator())
        .build();
    // Induction Port
    public static final BlockTypeTile<TileEntityInductionPort> INDUCTION_PORT = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.INDUCTION_PORT, MekanismLang.DESCRIPTION_INDUCTION_PORT)
        .withGui(() -> MekanismContainerTypes.INDUCTION_MATRIX)
        .withCustomContainer((tile) -> new ContainerProvider(MekanismLang.MATRIX, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.INDUCTION_MATRIX, i, inv, tile)))
        .with(new AttributeInventory(), new AttributeComparator(), new AttributeStateActive())
        .build();
    // Thermal Evaporation Controller
    public static final BlockTypeTile<TileEntityThermalEvaporationController> THERMAL_EVAPORATION_CONTROLLER = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.THERMAL_EVAPORATION_CONTROLLER, MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_CONTROLLER)
        .withGui(() -> MekanismContainerTypes.THERMAL_EVAPORATION_CONTROLLER)
        .with(new AttributeInventory(), new AttributeStateActive(), new AttributeStateFacing(), new AttributeCustomResistance(9F)).build();
    // Thermal Evaporation Valve
    public static final BlockTypeTile<TileEntityThermalEvaporationValve> THERMAL_EVAPORATION_VALVE = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.THERMAL_EVAPORATION_VALVE, MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_VALVE)
        .with(new AttributeComparator(), new AttributeCustomResistance(9F)).build();
    // Thermal Evaporation Block
    public static final BlockTypeTile<TileEntityThermalEvaporationBlock> THERMAL_EVAPORATION_BLOCK = BlockTileBuilder
        .createBlock(() -> MekanismTileEntityTypes.THERMAL_EVAPORATION_BLOCK, MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_BLOCK).with(new AttributeCustomResistance(9F)).build();
    // Steel Casing
    public static final BlockType STEEL_CASING = BlockTileBuilder
        .createBlock(MekanismLang.DESCRIPTION_STEEL_CASING).with(new AttributeCustomResistance(9F)).build();

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

    // Gas Tanks
    public static final Machine<TileEntityGasTank> BASIC_GAS_TANK = createGasTank(GasTankTier.BASIC, () -> MekanismTileEntityTypes.BASIC_GAS_TANK, () -> MekanismBlocks.ADVANCED_GAS_TANK);
    public static final Machine<TileEntityGasTank> ADVANCED_GAS_TANK = createGasTank(GasTankTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_GAS_TANK, () -> MekanismBlocks.ELITE_GAS_TANK);
    public static final Machine<TileEntityGasTank> ELITE_GAS_TANK = createGasTank(GasTankTier.ELITE, () -> MekanismTileEntityTypes.ELITE_GAS_TANK, () -> MekanismBlocks.ULTIMATE_GAS_TANK);
    public static final Machine<TileEntityGasTank> ULTIMATE_GAS_TANK = createGasTank(GasTankTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_GAS_TANK, null);
    public static final Machine<TileEntityGasTank> CREATIVE_GAS_TANK = createGasTank(GasTankTier.CREATIVE, () -> MekanismTileEntityTypes.CREATIVE_GAS_TANK, null);

    static {
        for (FactoryTier tier : FactoryTier.values()) {
            for (FactoryType type : FactoryType.values()) {
                FACTORIES.put(tier, type,
                    FactoryBuilder.createFactory(() -> MekanismTileEntityTypes.getFactoryTile(tier, type), () -> MekanismContainerTypes.FACTORY, type.getBaseMachine(), tier).build());
            }
        }
    }

    public static Factory<?> getFactory(FactoryTier tier, FactoryType type) {
        return FACTORIES.get(tier, type);
    }

    public static final <TILE extends TileEntityInductionCell> BlockTypeTile<TILE> createInductionCell(InductionCellTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile) {
        return BlockTileBuilder.createBlock(tile, MekanismLang.DESCRIPTION_INDUCTION_CELL)
            .withEnergyConfig(() -> tier.getMaxEnergy())
            .with(new AttributeTier<>(tier), new AttributeNoMobSpawn())
            .build();
    }

    public static final <TILE extends TileEntityInductionProvider> BlockTypeTile<TILE> createInductionProvider(InductionProviderTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile) {
        return BlockTileBuilder.createBlock(tile, MekanismLang.DESCRIPTION_INDUCTION_PROVIDER)
            .with(new AttributeTier<>(tier), new AttributeNoMobSpawn())
            .build();
    }

    public static final <TILE extends TileEntityBin> Machine<TILE> createBin(BinTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        return MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_BIN)
            .with(new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock))
            .without(AttributeParticleFX.class, AttributeSecurity.class, AttributeUpgradeSupport.class, AttributeRedstone.class)
            .build();
    }

    public static final <TILE extends TileEntityEnergyCube> Machine<TILE> createEnergyCube(EnergyCubeTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        return MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_ENERGY_CUBE)
            .withGui(() -> MekanismContainerTypes.ENERGY_CUBE)
            .withEnergyConfig(() -> tier.getMaxEnergy())
            .with(new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock), new AttributeStateFacing(BlockStateHelper.facingProperty))
            .without(AttributeParticleFX.class, AttributeStateActive.class, AttributeUpgradeSupport.class)
            .build();
    }

    public static final <TILE extends TileEntityFluidTank> Machine<TILE> createFluidTank(FluidTankTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        return MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_FLUID_TANK)
            .withGui(() -> MekanismContainerTypes.FLUID_TANK)
            .withCustomShape(BlockShapes.FLUID_TANK)
            .with(new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock))
            .without(AttributeParticleFX.class, AttributeStateFacing.class, AttributeRedstone.class, AttributeUpgradeSupport.class)
            .build();
    }

    public static final <TILE extends TileEntityGasTank> Machine<TILE> createGasTank(GasTankTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        return MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_GAS_TANK)
            .withGui(() -> MekanismContainerTypes.GAS_TANK)
            .withCustomShape(BlockShapes.GAS_TANK)
            .with(new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock))
            .without(AttributeParticleFX.class, AttributeStateActive.class, AttributeUpgradeSupport.class)
            .build();
    }
}
