package mekanism.common.registries;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.function.Supplier;
import mekanism.api.Upgrade;
import mekanism.api.math.MathUtils;
import mekanism.api.text.ILangEntry;
import mekanism.api.tier.ITier;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockPersonalStorage;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomSelectionBox;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeHasBounding.HandleBoundingBlock;
import mekanism.common.block.attribute.AttributeHasBounding.TriBooleanFunction;
import mekanism.common.block.attribute.AttributeMultiblock;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.attribute.AttributeStateBoilerValveMode;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeStateFacing.FacePlacementType;
import mekanism.common.block.attribute.AttributeStateOpen;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.block.attribute.Attributes.AttributeCustomResistance;
import mekanism.common.block.attribute.Attributes.AttributeMobSpawn;
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
import mekanism.common.lib.math.Pos3D;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.CableTier;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tier.PipeTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityIndustrialAlarm;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.tile.TileEntityPersonalBarrel;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityRadioactiveWasteBarrel;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntityTeleporter;
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
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
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
import mekanism.common.tile.machine.TileEntityPaintingMachine;
import mekanism.common.tile.machine.TileEntityPigmentExtractor;
import mekanism.common.tile.machine.TileEntityPigmentMixer;
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
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityRestrictiveTransporter;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class MekanismBlockTypes {

    private MekanismBlockTypes() {
    }

    private static final Table<FactoryTier, FactoryType, Factory<?>> FACTORIES = HashBasedTable.create();

    // Enrichment Chamber
    public static final FactoryMachine<TileEntityEnrichmentChamber> ENRICHMENT_CHAMBER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.ENRICHMENT_CHAMBER, MekanismLang.DESCRIPTION_ENRICHMENT_CHAMBER, FactoryType.ENRICHING)
          .withGui(() -> MekanismContainerTypes.ENRICHMENT_CHAMBER)
          .withSound(MekanismSounds.ENRICHMENT_CHAMBER)
          .withEnergyConfig(MekanismConfig.usage.enrichmentChamber, MekanismConfig.storage.enrichmentChamber)
          .with(AttributeSideConfig.ELECTRIC_MACHINE)
          .withComputerSupport("enrichmentChamber")
          .build();
    // Crusher
    public static final FactoryMachine<TileEntityCrusher> CRUSHER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.CRUSHER, MekanismLang.DESCRIPTION_CRUSHER, FactoryType.CRUSHING)
          .withGui(() -> MekanismContainerTypes.CRUSHER)
          .withSound(MekanismSounds.CRUSHER)
          .withEnergyConfig(MekanismConfig.usage.crusher, MekanismConfig.storage.crusher)
          .with(AttributeSideConfig.ELECTRIC_MACHINE)
          .withComputerSupport("crusher")
          .build();
    // Energized Smelter
    public static final FactoryMachine<TileEntityEnergizedSmelter> ENERGIZED_SMELTER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.ENERGIZED_SMELTER, MekanismLang.DESCRIPTION_ENERGIZED_SMELTER, FactoryType.SMELTING)
          .withGui(() -> MekanismContainerTypes.ENERGIZED_SMELTER)
          .withSound(MekanismSounds.ENERGIZED_SMELTER)
          .withEnergyConfig(MekanismConfig.usage.energizedSmelter, MekanismConfig.storage.energizedSmelter)
          .with(AttributeSideConfig.ELECTRIC_MACHINE)
          .withComputerSupport("energizedSmelter")
          .build();
    // Precision Sawmill
    public static final FactoryMachine<TileEntityPrecisionSawmill> PRECISION_SAWMILL = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.PRECISION_SAWMILL, MekanismLang.DESCRIPTION_PRECISION_SAWMILL, FactoryType.SAWING)
          .withGui(() -> MekanismContainerTypes.PRECISION_SAWMILL)
          .withSound(MekanismSounds.PRECISION_SAWMILL)
          .withEnergyConfig(MekanismConfig.usage.precisionSawmill, MekanismConfig.storage.precisionSawmill)
          .withSideConfig(TransmissionType.ITEM, TransmissionType.ENERGY)
          .withComputerSupport("precisionSawmill")
          .build();
    // Osmium Compressor
    public static final FactoryMachine<TileEntityOsmiumCompressor> OSMIUM_COMPRESSOR = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.OSMIUM_COMPRESSOR, MekanismLang.DESCRIPTION_OSMIUM_COMPRESSOR, FactoryType.COMPRESSING)
          .withGui(() -> MekanismContainerTypes.OSMIUM_COMPRESSOR)
          .withSound(MekanismSounds.OSMIUM_COMPRESSOR)
          .withEnergyConfig(MekanismConfig.usage.osmiumCompressor, MekanismConfig.storage.osmiumCompressor)
          .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
          .withComputerSupport("osmiumCompressor")
          .build();
    // Combiner
    public static final FactoryMachine<TileEntityCombiner> COMBINER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.COMBINER, MekanismLang.DESCRIPTION_COMBINER, FactoryType.COMBINING)
          .withGui(() -> MekanismContainerTypes.COMBINER)
          .withSound(MekanismSounds.COMBINER)
          .withEnergyConfig(MekanismConfig.usage.combiner, MekanismConfig.storage.combiner)
          .withSideConfig(TransmissionType.ITEM, TransmissionType.ENERGY)
          .withComputerSupport("combiner")
          .build();
    // Metallurgic Infuser
    public static final FactoryMachine<TileEntityMetallurgicInfuser> METALLURGIC_INFUSER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.METALLURGIC_INFUSER, MekanismLang.DESCRIPTION_METALLURGIC_INFUSER, FactoryType.INFUSING)
          .withGui(() -> MekanismContainerTypes.METALLURGIC_INFUSER)
          .withSound(MekanismSounds.METALLURGIC_INFUSER)
          .withEnergyConfig(MekanismConfig.usage.metallurgicInfuser, MekanismConfig.storage.metallurgicInfuser)
          .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
          .withCustomShape(BlockShapes.METALLURGIC_INFUSER)
          .withComputerSupport("metallurgicInfuser")
          .build();
    // Purification Chamber
    public static final FactoryMachine<TileEntityPurificationChamber> PURIFICATION_CHAMBER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.PURIFICATION_CHAMBER, MekanismLang.DESCRIPTION_PURIFICATION_CHAMBER, FactoryType.PURIFYING)
          .withGui(() -> MekanismContainerTypes.PURIFICATION_CHAMBER)
          .withSound(MekanismSounds.PURIFICATION_CHAMBER)
          .withEnergyConfig(MekanismConfig.usage.purificationChamber, MekanismConfig.storage.purificationChamber)
          .with(AttributeUpgradeSupport.DEFAULT_ADVANCED_MACHINE_UPGRADES)
          .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
          .withComputerSupport("purificationChamber")
          .build();
    // Chemical Injection Chamber
    public static final FactoryMachine<TileEntityChemicalInjectionChamber> CHEMICAL_INJECTION_CHAMBER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.CHEMICAL_INJECTION_CHAMBER, MekanismLang.DESCRIPTION_CHEMICAL_INJECTION_CHAMBER, FactoryType.INJECTING)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER)
          .withSound(MekanismSounds.CHEMICAL_INJECTION_CHAMBER)
          .withEnergyConfig(MekanismConfig.usage.chemicalInjectionChamber, MekanismConfig.storage.chemicalInjectionChamber)
          .with(AttributeUpgradeSupport.DEFAULT_ADVANCED_MACHINE_UPGRADES)
          .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
          .withComputerSupport("chemicalInjectionChamber")
          .build();
    // Pressurized Reaction Chamber
    public static final Machine<TileEntityPressurizedReactionChamber> PRESSURIZED_REACTION_CHAMBER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.PRESSURIZED_REACTION_CHAMBER, MekanismLang.DESCRIPTION_PRESSURIZED_REACTION_CHAMBER)
          .withGui(() -> MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER)
          .withSound(MekanismSounds.PRESSURIZED_REACTION_CHAMBER)
          .withEnergyConfig(MekanismConfig.usage.pressurizedReactionBase, MekanismConfig.storage.pressurizedReactionBase)
          .withSideConfig(TransmissionType.ITEM, TransmissionType.CHEMICAL, TransmissionType.FLUID, TransmissionType.ENERGY)
          .withCustomShape(BlockShapes.PRESSURIZED_REACTION_CHAMBER)
          .withComputerSupport("pressurizedReactionChamber")
          .build();
    // Chemical Crystallizer
    public static final Machine<TileEntityChemicalCrystallizer> CHEMICAL_CRYSTALLIZER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_CRYSTALLIZER, MekanismLang.DESCRIPTION_CHEMICAL_CRYSTALLIZER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_CRYSTALLIZER)
          .withSound(MekanismSounds.CHEMICAL_CRYSTALLIZER)
          .withEnergyConfig(MekanismConfig.usage.chemicalCrystallizer, MekanismConfig.storage.chemicalCrystallizer)
          .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
          .withCustomShape(BlockShapes.CHEMICAL_CRYSTALLIZER)
          .withComputerSupport("chemicalCrystallizer")
          .build();
    // Chemical Dissolution Chamber
    public static final Machine<TileEntityChemicalDissolutionChamber> CHEMICAL_DISSOLUTION_CHAMBER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_DISSOLUTION_CHAMBER, MekanismLang.DESCRIPTION_CHEMICAL_DISSOLUTION_CHAMBER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_DISSOLUTION_CHAMBER)
          .withSound(MekanismSounds.CHEMICAL_DISSOLUTION_CHAMBER)
          .withEnergyConfig(MekanismConfig.usage.chemicalDissolutionChamber, MekanismConfig.storage.chemicalDissolutionChamber)
          .with(AttributeUpgradeSupport.DEFAULT_ADVANCED_MACHINE_UPGRADES)
          .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
          .withCustomShape(BlockShapes.CHEMICAL_DISSOLUTION_CHAMBER)
          .withComputerSupport("chemicalDissolutionChamber")
          .build();
    // Chemical Infuser
    public static final Machine<TileEntityChemicalInfuser> CHEMICAL_INFUSER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_INFUSER, MekanismLang.DESCRIPTION_CHEMICAL_INFUSER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_INFUSER)
          .withSound(MekanismSounds.CHEMICAL_INFUSER)
          .withEnergyConfig(MekanismConfig.usage.chemicalInfuser, MekanismConfig.storage.chemicalInfuser)
          .withSideConfig(TransmissionType.CHEMICAL, TransmissionType.ITEM, TransmissionType.ENERGY)
          .withCustomShape(BlockShapes.CHEMICAL_INFUSER)
          .withComputerSupport("chemicalInfuser")
          .build();
    // Chemical Oxidizer
    public static final Machine<TileEntityChemicalOxidizer> CHEMICAL_OXIDIZER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_OXIDIZER, MekanismLang.DESCRIPTION_CHEMICAL_OXIDIZER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_OXIDIZER)
          .withSound(MekanismSounds.CHEMICAL_OXIDIZER)
          .withEnergyConfig(MekanismConfig.usage.chemicalOxidizer, MekanismConfig.storage.chemicalOxidizer)
          .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
          .withCustomShape(BlockShapes.CHEMICAL_OXIDIZER)
          .withComputerSupport("chemicalOxidizer")
          .build();
    // Chemical Washer
    public static final Machine<TileEntityChemicalWasher> CHEMICAL_WASHER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_WASHER, MekanismLang.DESCRIPTION_CHEMICAL_WASHER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_WASHER)
          .withSound(MekanismSounds.CHEMICAL_WASHER)
          .withEnergyConfig(MekanismConfig.usage.chemicalWasher, MekanismConfig.storage.chemicalWasher)
          .withSideConfig(TransmissionType.CHEMICAL, TransmissionType.FLUID, TransmissionType.ITEM, TransmissionType.ENERGY)
          .withCustomShape(BlockShapes.CHEMICAL_WASHER)
          .withComputerSupport("chemicalWasher")
          .build();
    // Rotary Condensentrator
    public static final Machine<TileEntityRotaryCondensentrator> ROTARY_CONDENSENTRATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ROTARY_CONDENSENTRATOR, MekanismLang.DESCRIPTION_ROTARY_CONDENSENTRATOR)
          .withGui(() -> MekanismContainerTypes.ROTARY_CONDENSENTRATOR)
          .withSound(MekanismSounds.ROTARY_CONDENSENTRATOR)
          .withEnergyConfig(MekanismConfig.usage.rotaryCondensentrator, MekanismConfig.storage.rotaryCondensentrator)
          .withSideConfig(TransmissionType.CHEMICAL, TransmissionType.FLUID, TransmissionType.ITEM, TransmissionType.ENERGY)
          .withCustomShape(BlockShapes.ROTARY_CONDENSENTRATOR)
          .withComputerSupport("rotaryCondensentrator")
          .build();
    // Electrolytic Separator
    public static final Machine<TileEntityElectrolyticSeparator> ELECTROLYTIC_SEPARATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ELECTROLYTIC_SEPARATOR, MekanismLang.DESCRIPTION_ELECTROLYTIC_SEPARATOR)
          .withGui(() -> MekanismContainerTypes.ELECTROLYTIC_SEPARATOR)
          .withSound(MekanismSounds.ELECTROLYTIC_SEPARATOR)
          .withEnergyConfig(() -> MathUtils.multiplyClamped(2, ChemicalUtil.hydrogenEnergyDensity()), MekanismConfig.storage.electrolyticSeparator)
          .withSideConfig(TransmissionType.FLUID, TransmissionType.CHEMICAL, TransmissionType.ITEM, TransmissionType.ENERGY)
          .withCustomShape(BlockShapes.ELECTROLYTIC_SEPARATOR)
          .withComputerSupport("electrolyticSeparator")
          .build();
    // Digital Miner
    public static final Machine<TileEntityDigitalMiner> DIGITAL_MINER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.DIGITAL_MINER, MekanismLang.DESCRIPTION_DIGITAL_MINER)
          .withGui(() -> MekanismContainerTypes.DIGITAL_MINER)
          .withEnergyConfig(MekanismConfig.usage.digitalMiner, MekanismConfig.storage.digitalMiner)
          .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.ANCHOR, Upgrade.STONE_GENERATOR)
          .withCustomShape(BlockShapes.DIGITAL_MINER)
          .with(AttributeCustomSelectionBox.JSON)
          .withBounding(new HandleBoundingBlock() {
              @Override
              public <DATA> boolean handle(Level level, BlockPos pos, BlockState state, DATA data, TriBooleanFunction<Level, BlockPos, DATA> consumer) {
                  BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                  for (int x = -1; x <= 1; x++) {
                      for (int y = 0; y <= 1; y++) {
                          for (int z = -1; z <= 1; z++) {
                              if (x != 0 || y != 0 || z != 0) {
                                  mutable.setWithOffset(pos, x, y, z);
                                  if (!consumer.accept(level, mutable, data)) {
                                      return false;
                                  }
                              }
                          }
                      }
                  }
                  return true;
              }
          })
          .withComputerSupport("digitalMiner")
          .replace(Attributes.ACTIVE)
          .build();
    // Formulaic Assemblicator
    public static final Machine<TileEntityFormulaicAssemblicator> FORMULAIC_ASSEMBLICATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.FORMULAIC_ASSEMBLICATOR, MekanismLang.DESCRIPTION_FORMULAIC_ASSEMBLICATOR)
          .withGui(() -> MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR)
          .withEnergyConfig(MekanismConfig.usage.formulaicAssemblicator, MekanismConfig.storage.formulaicAssemblicator)
          .with(AttributeUpgradeSupport.SPEED_ENERGY)
          .withSideConfig(TransmissionType.ITEM, TransmissionType.ENERGY)
          .withComputerSupport("formulaicAssemblicator")
          .build();
    // Electric Pump
    public static final Machine<TileEntityElectricPump> ELECTRIC_PUMP = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ELECTRIC_PUMP, MekanismLang.DESCRIPTION_ELECTRIC_PUMP)
          .withGui(() -> MekanismContainerTypes.ELECTRIC_PUMP)
          .withEnergyConfig(MekanismConfig.usage.electricPump, MekanismConfig.storage.electricPump)
          .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.FILTER)
          .withCustomShape(BlockShapes.ELECTRIC_PUMP)
          .withComputerSupport("electricPump")
          .replace(Attributes.ACTIVE)
          .build();
    // Fluidic Plenisher
    public static final Machine<TileEntityFluidicPlenisher> FLUIDIC_PLENISHER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.FLUIDIC_PLENISHER, MekanismLang.DESCRIPTION_FLUIDIC_PLENISHER)
          .withGui(() -> MekanismContainerTypes.FLUIDIC_PLENISHER)
          .withEnergyConfig(MekanismConfig.usage.fluidicPlenisher, MekanismConfig.storage.fluidicPlenisher)
          .with(AttributeUpgradeSupport.SPEED_ENERGY)
          .withCustomShape(BlockShapes.FLUIDIC_PLENISHER)
          .withComputerSupport("fluidicPlenisher")
          .replace(Attributes.ACTIVE)
          .build();
    // Solar Neutron Activator
    public static final Machine<TileEntitySolarNeutronActivator> SOLAR_NEUTRON_ACTIVATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.SOLAR_NEUTRON_ACTIVATOR, MekanismLang.DESCRIPTION_SOLAR_NEUTRON_ACTIVATOR)
          .withGui(() -> MekanismContainerTypes.SOLAR_NEUTRON_ACTIVATOR)
          .without(AttributeParticleFX.class, AttributeUpgradeSupport.class)
          .withCustomShape(BlockShapes.SOLAR_NEUTRON_ACTIVATOR)
          .with(AttributeCustomSelectionBox.JSON)
          .withSideConfig(TransmissionType.CHEMICAL, TransmissionType.ITEM)
          .with(AttributeHasBounding.ABOVE_ONLY)
          .withComputerSupport("solarNeutronActivator")
          .replace(Attributes.ACTIVE)
          .build();
    // Teleporter
    public static final Machine<TileEntityTeleporter> TELEPORTER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.TELEPORTER, MekanismLang.DESCRIPTION_TELEPORTER)
          .withGui(() -> MekanismContainerTypes.TELEPORTER)
          .withEnergyConfig(MekanismConfig.storage.teleporter)
          .with(AttributeUpgradeSupport.ANCHOR_ONLY)
          .without(AttributeStateActive.class, AttributeStateFacing.class, AttributeParticleFX.class)
          .withLight(3)
          .withComputerSupport("teleporter")
          .build();
    // Chargepad
    public static final BlockTypeTile<TileEntityChargepad> CHARGEPAD = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.CHARGEPAD, MekanismLang.DESCRIPTION_CHARGEPAD)
          .withEnergyConfig(MekanismConfig.usage.chargePad, MekanismConfig.storage.chargepad)
          .withSound(MekanismSounds.CHARGEPAD)
          .with(Attributes.ACTIVE_LIGHT, new AttributeStateFacing())
          .with(new AttributeParticleFX().add(DustParticleOptions.REDSTONE, rand -> new Pos3D(rand.nextDouble() - 0.5, -0.35, rand.nextDouble() - 0.5)))
          .withCustomShape(BlockShapes.CHARGEPAD)
          .withComputerSupport("chargepad")
          .build();
    // Laser
    public static final BlockTypeTile<TileEntityLaser> LASER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.LASER, MekanismLang.DESCRIPTION_LASER)
          .withEnergyConfig(MekanismConfig.usage.laser, MekanismConfig.storage.laser)
          .withSound(MekanismSounds.LASER)
          .with(Attributes.ACTIVE, new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE), Attributes.SECURITY)
          .withCustomShape(BlockShapes.LASER)
          .withComputerSupport("laser")
          .build();
    // Laser Amplifier
    public static final BlockTypeTile<TileEntityLaserAmplifier> LASER_AMPLIFIER = BlockTileBuilder.createBlock(() -> MekanismTileEntityTypes.LASER_AMPLIFIER, MekanismLang.DESCRIPTION_LASER_AMPLIFIER)
          .withGui(() -> MekanismContainerTypes.LASER_AMPLIFIER)
          .withEnergyConfig(MekanismConfig.storage.laserAmplifier)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE),
                new AttributeRedstoneEmitter<>((tile, side) -> tile.getRedstoneLevel()), Attributes.REDSTONE, Attributes.COMPARATOR, Attributes.SECURITY)
          .withCustomShape(BlockShapes.LASER_AMPLIFIER)
          .withComputerSupport("laserAmplifier")
          .build();
    // Laser Tractor Beam
    public static final BlockTypeTile<TileEntityLaserTractorBeam> LASER_TRACTOR_BEAM = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.LASER_TRACTOR_BEAM, MekanismLang.DESCRIPTION_LASER_TRACTOR_BEAM)
          .withGui(() -> MekanismContainerTypes.LASER_TRACTOR_BEAM)
          .withEnergyConfig(MekanismConfig.storage.laserTractorBeam)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE), Attributes.COMPARATOR, Attributes.SECURITY, Attributes.INVENTORY)
          .withCustomShape(BlockShapes.LASER_AMPLIFIER)
          .withComputerSupport("laserTractorBeam")
          .build();
    // Resistive Heater
    public static final Machine<TileEntityResistiveHeater> RESISTIVE_HEATER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.RESISTIVE_HEATER, MekanismLang.DESCRIPTION_RESISTIVE_HEATER)
          .withGui(() -> MekanismContainerTypes.RESISTIVE_HEATER)
          .withEnergyConfig(() -> TileEntityResistiveHeater.BASE_USAGE, null)
          .without(AttributeComparator.class)
          .withCustomShape(BlockShapes.RESISTIVE_HEATER)
          .withSound(MekanismSounds.RESISTIVE_HEATER)
          .withComputerSupport("resistiveHeater")
          .replace(AttributeUpgradeSupport.MUFFLING_ONLY)
          .replace(Attributes.ACTIVE_MELT_LIGHT)
          .build();
    // Seismic Vibrator
    public static final Machine<TileEntitySeismicVibrator> SEISMIC_VIBRATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.SEISMIC_VIBRATOR, MekanismLang.DESCRIPTION_SEISMIC_VIBRATOR)
          .withGui(() -> MekanismContainerTypes.SEISMIC_VIBRATOR)
          .withEnergyConfig(MekanismConfig.usage.seismicVibrator, MekanismConfig.storage.seismicVibrator)
          .without(AttributeComparator.class, AttributeParticleFX.class, AttributeUpgradeSupport.class)
          .withCustomShape(BlockShapes.SEISMIC_VIBRATOR)
          .with(AttributeCustomSelectionBox.JAVA)
          .with(AttributeHasBounding.ABOVE_ONLY)
          .withComputerSupport("seismicVibrator")
          .build();
    // Personal Barrel
    public static final BlockTypeTile<TileEntityPersonalBarrel> PERSONAL_BARREL = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.PERSONAL_BARREL, MekanismLang.DESCRIPTION_PERSONAL_BARREL)
          .withGui(() -> MekanismContainerTypes.PERSONAL_STORAGE_BLOCK)
          .with(Attributes.SECURITY, BlockPersonalStorage.PERSONAL_STORAGE_INVENTORY, AttributeStateOpen.INSTANCE, new AttributeStateFacing(BlockStateProperties.FACING), new AttributeCustomResistance(-1))
          .withComputerSupport("personalBarrel")
          .build();
    // Personal Chest
    public static final BlockTypeTile<TileEntityPersonalChest> PERSONAL_CHEST = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.PERSONAL_CHEST, MekanismLang.DESCRIPTION_PERSONAL_CHEST)
          .withGui(() -> MekanismContainerTypes.PERSONAL_STORAGE_BLOCK)
          .with(Attributes.SECURITY, BlockPersonalStorage.PERSONAL_STORAGE_INVENTORY, new AttributeStateFacing(), new AttributeCustomResistance(-1))
          .withCustomShape(BlockShapes.PERSONAL_CHEST)
          .withComputerSupport("personalChest")
          .build();
    // Fuelwood Heater
    public static final BlockTypeTile<TileEntityFuelwoodHeater> FUELWOOD_HEATER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.FUELWOOD_HEATER, MekanismLang.DESCRIPTION_FUELWOOD_HEATER)
          .withGui(() -> MekanismContainerTypes.FUELWOOD_HEATER)
          .with(Attributes.SECURITY, Attributes.INVENTORY, Attributes.ACTIVE_MELT_LIGHT, new AttributeStateFacing(), new AttributeParticleFX()
                .add(ParticleTypes.SMOKE, rand -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52))
                .add(DustParticleOptions.REDSTONE, rand -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52)))
          .withComputerSupport("fuelwoodHeater")
          .build();
    // Oredictionificator
    public static final BlockTypeTile<TileEntityOredictionificator> OREDICTIONIFICATOR = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.OREDICTIONIFICATOR, MekanismLang.DESCRIPTION_OREDICTIONIFICATOR)
          .withGui(() -> MekanismContainerTypes.OREDICTIONIFICATOR)
          .with(Attributes.SECURITY, Attributes.INVENTORY, Attributes.ACTIVE, new AttributeStateFacing(), Attributes.REDSTONE)
          .withSideConfig(TransmissionType.ITEM)
          .withComputerSupport("oredictionificator")
          .build();
    // Quantum Entangloporter
    public static final BlockTypeTile<TileEntityQuantumEntangloporter> QUANTUM_ENTANGLOPORTER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.QUANTUM_ENTANGLOPORTER, MekanismLang.DESCRIPTION_QUANTUM_ENTANGLOPORTER)
          .withGui(() -> MekanismContainerTypes.QUANTUM_ENTANGLOPORTER)
          .with(AttributeUpgradeSupport.ANCHOR_ONLY)
          .with(new AttributeStateFacing(BlockStateProperties.FACING), Attributes.INVENTORY, Attributes.SECURITY, Attributes.REDSTONE)
          .withSideConfig(TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.CHEMICAL, TransmissionType.ENERGY, TransmissionType.HEAT)
          .withCustomShape(BlockShapes.QUANTUM_ENTANGLOPORTER)
          .withComputerSupport("quantumEntangloporter")
          .build();
    // Logistical Sorter
    public static final Machine<TileEntityLogisticalSorter> LOGISTICAL_SORTER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.LOGISTICAL_SORTER, MekanismLang.DESCRIPTION_LOGISTICAL_SORTER)
          .withGui(() -> MekanismContainerTypes.LOGISTICAL_SORTER)
          .with(AttributeUpgradeSupport.MUFFLING_ONLY)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE))
          .withCustomShape(BlockShapes.LOGISTICAL_SORTER)
          .withSound(MekanismSounds.LOGISTICAL_SORTER)
          .withComputerSupport("logisticalSorter")
          .replace(Attributes.ACTIVE)
          .build();
    // Security Desk
    public static final BlockTypeTile<TileEntitySecurityDesk> SECURITY_DESK = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.SECURITY_DESK, MekanismLang.DESCRIPTION_SECURITY_DESK)
          .withGui(() -> MekanismContainerTypes.SECURITY_DESK)
          .with(Attributes.INVENTORY, new AttributeStateFacing(), new AttributeCustomResistance(-1), Attributes.SECURITY)
          .withCustomShape(BlockShapes.SECURITY_DESK)
          .with(AttributeCustomSelectionBox.JSON)
          .with(AttributeHasBounding.ABOVE_ONLY)
          .withComputerSupport("securityDesk")
          .build();
    // Modification Station
    public static final BlockTypeTile<TileEntityModificationStation> MODIFICATION_STATION = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.MODIFICATION_STATION, MekanismLang.DESCRIPTION_MODIFICATION_STATION)
          .withGui(() -> MekanismContainerTypes.MODIFICATION_STATION)
          .withEnergyConfig(MekanismConfig.usage.modificationStation, MekanismConfig.storage.modificationStation)
          .with(Attributes.INVENTORY, new AttributeStateFacing(false), Attributes.REDSTONE, Attributes.SECURITY)
          .withCustomShape(BlockShapes.MODIFICATION_STATION)
          .with(AttributeCustomSelectionBox.JSON)
          .withBounding(new HandleBoundingBlock() {
              @Override
              public <DATA> boolean handle(Level level, BlockPos pos, BlockState state, DATA data, TriBooleanFunction<Level, BlockPos, DATA> consumer) {
                  BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                  if (!consumer.accept(level, mutable.setWithOffset(pos, Direction.UP), data)) {
                      return false;
                  }
                  mutable.setWithOffset(pos, MekanismUtils.getRight(Attribute.getFacing(state)));
                  if (!consumer.accept(level, mutable, data)) {
                      return false;
                  }
                  return consumer.accept(level, mutable.move(Direction.UP), data);
              }
          })
          .withComputerSupport("modificationStation")
          .build();
    // Isotopic Centrifuge
    public static final Machine<TileEntityIsotopicCentrifuge> ISOTOPIC_CENTRIFUGE = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ISOTOPIC_CENTRIFUGE, MekanismLang.DESCRIPTION_ISOTOPIC_CENTRIFUGE)
          .withGui(() -> MekanismContainerTypes.ISOTOPIC_CENTRIFUGE)
          .withEnergyConfig(MekanismConfig.usage.isotopicCentrifuge, MekanismConfig.storage.isotopicCentrifuge)
          .withSideConfig(TransmissionType.CHEMICAL, TransmissionType.ITEM, TransmissionType.ENERGY)
          .withSound(MekanismSounds.ISOTOPIC_CENTRIFUGE)
          .withCustomShape(BlockShapes.ISOTOPIC_CENTRIFUGE)
          .with(AttributeHasBounding.ABOVE_ONLY)
          .withComputerSupport("isotopicCentrifuge")
          .build();
    // Nutritional Liquifier
    public static final Machine<TileEntityNutritionalLiquifier> NUTRITIONAL_LIQUIFIER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.NUTRITIONAL_LIQUIFIER, MekanismLang.DESCRIPTION_NUTRITIONAL_LIQUIFIER)
          .withGui(() -> MekanismContainerTypes.NUTRITIONAL_LIQUIFIER)
          .withEnergyConfig(MekanismConfig.usage.nutritionalLiquifier, MekanismConfig.storage.nutritionalLiquifier)
          .withSideConfig(TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.ENERGY)
          .withSound(MekanismSounds.NUTRITIONAL_LIQUIFIER)
          .withComputerSupport("nutritionalLiquifier")
          .build();
    // Antiprotonic Nucleosynthesizer
    public static final Machine<TileEntityAntiprotonicNucleosynthesizer> ANTIPROTONIC_NUCLEOSYNTHESIZER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER, MekanismLang.DESCRIPTION_ANTIPROTONIC_NUCLEOSYNTHESIZER)
          .withGui(() -> MekanismContainerTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER)
          .withEnergyConfig(MekanismConfig.usage.antiprotonicNucleosynthesizer, MekanismConfig.storage.antiprotonicNucleosynthesizer)
          .withSound(MekanismSounds.ANTIPROTONIC_NUCLEOSYNTHESIZER)
          .with(AttributeUpgradeSupport.MUFFLING_ONLY)
          .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
          .withCustomShape(BlockShapes.ANTIPROTONIC_NUCLEOSYNTHESIZER)
          .withComputerSupport("antiprotonicNucleosynthesizer")
          .build();
    // Pigment Extractor
    public static final Machine<TileEntityPigmentExtractor> PIGMENT_EXTRACTOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.PIGMENT_EXTRACTOR, MekanismLang.DESCRIPTION_PIGMENT_EXTRACTOR)
          .withGui(() -> MekanismContainerTypes.PIGMENT_EXTRACTOR)
          .withSound(MekanismSounds.PIGMENT_EXTRACTOR)
          .withEnergyConfig(MekanismConfig.usage.pigmentExtractor, MekanismConfig.storage.pigmentExtractor)
          .with(AttributeUpgradeSupport.DEFAULT_MACHINE_UPGRADES)
          .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
          .withComputerSupport("pigmentExtractor")
          .build();
    // Pigment Mixer
    public static final Machine<TileEntityPigmentMixer> PIGMENT_MIXER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.PIGMENT_MIXER, MekanismLang.DESCRIPTION_PIGMENT_MIXER)
          .withGui(() -> MekanismContainerTypes.PIGMENT_MIXER)
          .withSound(MekanismSounds.PIGMENT_MIXER)
          .withEnergyConfig(MekanismConfig.usage.pigmentMixer, MekanismConfig.storage.pigmentMixer)
          .with(AttributeUpgradeSupport.DEFAULT_MACHINE_UPGRADES)
          .withSideConfig(TransmissionType.CHEMICAL, TransmissionType.ITEM, TransmissionType.ENERGY)
          .withCustomShape(BlockShapes.PIGMENT_MIXER)
          .with(AttributeCustomSelectionBox.JAVA)
          .with(AttributeHasBounding.ABOVE_ONLY)
          .withComputerSupport("pigmentMixer")
          .build();
    // Painting Machine
    public static final Machine<TileEntityPaintingMachine> PAINTING_MACHINE = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.PAINTING_MACHINE, MekanismLang.DESCRIPTION_PAINTING_MACHINE)
          .withGui(() -> MekanismContainerTypes.PAINTING_MACHINE)
          .withSound(MekanismSounds.PAINTING_MACHINE)
          .withEnergyConfig(MekanismConfig.usage.paintingMachine, MekanismConfig.storage.paintingMachine)
          .with(AttributeUpgradeSupport.DEFAULT_MACHINE_UPGRADES)
          .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
          .withComputerSupport("paintingMachine")
          .build();
    // Dimensional Stabilizer
    public static final Machine<TileEntityDimensionalStabilizer> DIMENSIONAL_STABILIZER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.DIMENSIONAL_STABILIZER, MekanismLang.DESCRIPTION_DIMENSIONAL_STABILIZER)
          .withGui(() -> MekanismContainerTypes.DIMENSIONAL_STABILIZER)
          .without(AttributeStateFacing.class, AttributeParticleFX.class)
          .withEnergyConfig(MekanismConfig.usage.dimensionalStabilizer, MekanismConfig.storage.dimensionalStabilizer)
          .with(AttributeUpgradeSupport.ENERGY_ONLY)
          .withComputerSupport("dimensionalStabilizer")
          .build();

    // QIO Drive Array
    public static final BlockTypeTile<TileEntityQIODriveArray> QIO_DRIVE_ARRAY = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.QIO_DRIVE_ARRAY, MekanismLang.DESCRIPTION_QIO_DRIVE_ARRAY)
          .withGui(() -> MekanismContainerTypes.QIO_DRIVE_ARRAY)
          .withCustomShape(BlockShapes.QIO_DRIVE_ARRAY)
          .with(new AttributeStateFacing(), Attributes.SECURITY, Attributes.INVENTORY, Attributes.ACTIVE)
          .withComputerSupport("qioDriveArray")
          .build();
    // QIO Dashboard
    public static final BlockTypeTile<TileEntityQIODashboard> QIO_DASHBOARD = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.QIO_DASHBOARD, MekanismLang.DESCRIPTION_QIO_DASHBOARD)
          .withGui(() -> MekanismContainerTypes.QIO_DASHBOARD)
          .withCustomShape(BlockShapes.QIO_DASHBOARD)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE), Attributes.SECURITY, Attributes.ACTIVE, Attributes.INVENTORY)
          .withComputerSupport("qioDashboard")
          .build();
    // QIO Importer
    public static final BlockTypeTile<TileEntityQIOImporter> QIO_IMPORTER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.QIO_IMPORTER, MekanismLang.DESCRIPTION_QIO_IMPORTER)
          .withGui(() -> MekanismContainerTypes.QIO_IMPORTER)
          .withCustomShape(BlockShapes.QIO_IMPORTER)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE), Attributes.SECURITY, Attributes.REDSTONE, Attributes.ACTIVE)
          .with(AttributeUpgradeSupport.SPEED_ONLY)
          .withComputerSupport("qioImporter")
          .build();
    // QIO Exporter
    public static final BlockTypeTile<TileEntityQIOExporter> QIO_EXPORTER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.QIO_EXPORTER, MekanismLang.DESCRIPTION_QIO_EXPORTER)
          .withGui(() -> MekanismContainerTypes.QIO_EXPORTER)
          .withCustomShape(BlockShapes.QIO_EXPORTER)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE), Attributes.SECURITY, Attributes.REDSTONE, Attributes.ACTIVE, Attributes.INVENTORY)
          .with(AttributeUpgradeSupport.SPEED_ONLY)
          .withComputerSupport("qioExporter")
          .build();
    // QIO Redstone Adapter
    public static final BlockTypeTile<TileEntityQIORedstoneAdapter> QIO_REDSTONE_ADAPTER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.QIO_REDSTONE_ADAPTER, MekanismLang.DESCRIPTION_QIO_REDSTONE_ADAPTER)
          .withGui(() -> MekanismContainerTypes.QIO_REDSTONE_ADAPTER)
          .withCustomShape(BlockShapes.QIO_REDSTONE_ADAPTER)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE), Attributes.SECURITY, Attributes.ACTIVE)
          .with(new AttributeRedstoneEmitter<>(TileEntityQIORedstoneAdapter::getRedstoneLevel))
          .withComputerSupport("qioRedstoneAdapter")
          .build();

    // Dynamic Tank
    public static final BlockTypeTile<TileEntityDynamicTank> DYNAMIC_TANK = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.DYNAMIC_TANK, MekanismLang.DESCRIPTION_DYNAMIC_TANK)
          .withGui(() -> MekanismContainerTypes.DYNAMIC_TANK, MekanismLang.DYNAMIC_TANK)
          .with(Attributes.INVENTORY)
          .externalMultiblock()
          .build();
    // Dynamic Valve
    public static final BlockTypeTile<TileEntityDynamicValve> DYNAMIC_VALVE = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.DYNAMIC_VALVE, MekanismLang.DESCRIPTION_DYNAMIC_VALVE)
          .withGui(() -> MekanismContainerTypes.DYNAMIC_TANK, MekanismLang.DYNAMIC_TANK)
          .with(Attributes.INVENTORY, Attributes.COMPARATOR)
          .externalMultiblock()
          .withComputerSupport("dynamicValve")
          .build();
    // Boiler Casing
    public static final BlockTypeTile<TileEntityBoilerCasing> BOILER_CASING = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.BOILER_CASING, MekanismLang.DESCRIPTION_BOILER_CASING)
          .withGui(() -> MekanismContainerTypes.THERMOELECTRIC_BOILER, MekanismLang.BOILER)
          .externalMultiblock()
          .build();
    // Boiler Valve
    public static final BlockTypeTile<TileEntityBoilerValve> BOILER_VALVE = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.BOILER_VALVE, MekanismLang.DESCRIPTION_BOILER_VALVE)
          .withGui(() -> MekanismContainerTypes.THERMOELECTRIC_BOILER, MekanismLang.BOILER)
          .with(Attributes.INVENTORY, Attributes.COMPARATOR, new AttributeStateBoilerValveMode())
          .externalMultiblock()
          .withComputerSupport("boilerValve")
          .build();
    // Pressure Disperser
    public static final BlockTypeTile<TileEntityPressureDisperser> PRESSURE_DISPERSER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.PRESSURE_DISPERSER, MekanismLang.DESCRIPTION_PRESSURE_DISPERSER)
          .internalMultiblock()
          .build();
    // Superheating Element
    public static final BlockTypeTile<TileEntitySuperheatingElement> SUPERHEATING_ELEMENT = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.SUPERHEATING_ELEMENT, MekanismLang.DESCRIPTION_SUPERHEATING_ELEMENT)
          .with(Attributes.ACTIVE_LIGHT)
          .internalMultiblock()
          .build();
    // Induction Casing
    public static final BlockTypeTile<TileEntityInductionCasing> INDUCTION_CASING = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.INDUCTION_CASING, MekanismLang.DESCRIPTION_INDUCTION_CASING)
          .withGui(() -> MekanismContainerTypes.INDUCTION_MATRIX, MekanismLang.MATRIX)
          .with(Attributes.INVENTORY, Attributes.COMPARATOR)
          .externalMultiblock()
          .build();
    // Induction Port
    public static final BlockTypeTile<TileEntityInductionPort> INDUCTION_PORT = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.INDUCTION_PORT, MekanismLang.DESCRIPTION_INDUCTION_PORT)
          .withGui(() -> MekanismContainerTypes.INDUCTION_MATRIX, MekanismLang.MATRIX)
          .with(Attributes.INVENTORY, Attributes.COMPARATOR, Attributes.ACTIVE)
          .externalMultiblock()
          .withComputerSupport("inductionPort")
          .build();
    // Thermal Evaporation Controller
    public static final BlockTypeTile<TileEntityThermalEvaporationController> THERMAL_EVAPORATION_CONTROLLER = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.THERMAL_EVAPORATION_CONTROLLER, MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_CONTROLLER)
          .withGui(() -> MekanismContainerTypes.THERMAL_EVAPORATION_CONTROLLER, MekanismLang.EVAPORATION_PLANT)
          .with(Attributes.INVENTORY, Attributes.ACTIVE, new AttributeStateFacing(), new AttributeCustomResistance(9))
          .externalMultiblock()
          .withComputerSupport("thermalEvaporationController")
          .build();
    // Thermal Evaporation Valve
    public static final BlockTypeTile<TileEntityThermalEvaporationValve> THERMAL_EVAPORATION_VALVE = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.THERMAL_EVAPORATION_VALVE, MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_VALVE)
          .with(Attributes.COMPARATOR, new AttributeCustomResistance(9))
          .externalMultiblock()
          .withComputerSupport("thermalEvaporationValve")
          .build();
    // Thermal Evaporation Block
    public static final BlockTypeTile<TileEntityThermalEvaporationBlock> THERMAL_EVAPORATION_BLOCK = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.THERMAL_EVAPORATION_BLOCK, MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_BLOCK)
          .with(new AttributeCustomResistance(9))
          .externalMultiblock()
          .build();
    // Teleporter Frame
    public static final BlockType TELEPORTER_FRAME = BlockTypeBuilder
          .createBlock(MekanismLang.DESCRIPTION_TELEPORTER_FRAME)
          .withLight(3)
          .build();
    // Steel Casing
    public static final BlockType STEEL_CASING = BlockTypeBuilder
          .createBlock(MekanismLang.DESCRIPTION_STEEL_CASING)
          .build();
    // Radioactive Waste Barrel
    public static final BlockTypeTile<TileEntityRadioactiveWasteBarrel> RADIOACTIVE_WASTE_BARREL = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.RADIOACTIVE_WASTE_BARREL, MekanismLang.DESCRIPTION_RADIOACTIVE_WASTE_BARREL)
          .with(Attributes.ACTIVE_LIGHT, new AttributeStateFacing(), Attributes.COMPARATOR)
          .withCustomShape(BlockShapes.RADIOACTIVE_WASTE_BARREL)
          .withComputerSupport("radioactiveWasteBarrel")
          .build();
    // Industrial Alarm
    public static final BlockTypeTile<TileEntityIndustrialAlarm> INDUSTRIAL_ALARM = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.INDUSTRIAL_ALARM, MekanismLang.DESCRIPTION_INDUSTRIAL_ALARM)
          .withSound(MekanismSounds.INDUSTRIAL_ALARM)
          .withCustomShape(BlockShapes.INDUSTRIAL_ALARM)
          .with(Attributes.ACTIVE_FULL_LIGHT, new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE), Attributes.REDSTONE)
          .withComputerSupport("industrialAlarm")
          .build();
    // Structural Glass
    public static final BlockTypeTile<TileEntityStructuralGlass> STRUCTURAL_GLASS = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.STRUCTURAL_GLASS, MekanismLang.DESCRIPTION_STRUCTURAL_GLASS)
          .with(AttributeMultiblock.STRUCTURAL, AttributeMobSpawn.NEVER)
          .build();
    // SPS Casing
    public static final BlockTypeTile<TileEntitySPSCasing> SPS_CASING = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.SPS_CASING, MekanismLang.DESCRIPTION_SPS_CASING)
          .withGui(() -> MekanismContainerTypes.SPS, MekanismLang.SPS)
          .withSound(MekanismSounds.SPS)
          .externalMultiblock()
          .build();
    // SPS Port
    public static final BlockTypeTile<TileEntitySPSPort> SPS_PORT = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.SPS_PORT, MekanismLang.DESCRIPTION_SPS_PORT)
          .withGui(() -> MekanismContainerTypes.SPS, MekanismLang.SPS)
          .withSound(MekanismSounds.SPS)
          .withEnergyConfig(MekanismConfig.storage.spsPort)
          .with(Attributes.ACTIVE, Attributes.COMPARATOR)
          .externalMultiblock()
          .withComputerSupport("spsPort")
          .build();
    // Supercharged Coil
    public static final BlockTypeTile<TileEntitySuperchargedCoil> SUPERCHARGED_COIL = BlockTileBuilder
          .createBlock(() -> MekanismTileEntityTypes.SUPERCHARGED_COIL, MekanismLang.DESCRIPTION_SUPERCHARGED_COIL)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE))
          .withCustomShape(BlockShapes.SUPERCHARGED_COIL)
          .internalMultiblock()
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

    //Transmitters
    public static final BlockTypeTile<TileEntityUniversalCable> BASIC_UNIVERSAL_CABLE = createCable(CableTier.BASIC, () -> MekanismTileEntityTypes.BASIC_UNIVERSAL_CABLE);
    public static final BlockTypeTile<TileEntityUniversalCable> ADVANCED_UNIVERSAL_CABLE = createCable(CableTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_UNIVERSAL_CABLE);
    public static final BlockTypeTile<TileEntityUniversalCable> ELITE_UNIVERSAL_CABLE = createCable(CableTier.ELITE, () -> MekanismTileEntityTypes.ELITE_UNIVERSAL_CABLE);
    public static final BlockTypeTile<TileEntityUniversalCable> ULTIMATE_UNIVERSAL_CABLE = createCable(CableTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_UNIVERSAL_CABLE);

    public static final BlockTypeTile<TileEntityMechanicalPipe> BASIC_MECHANICAL_PIPE = createPipe(PipeTier.BASIC, () -> MekanismTileEntityTypes.BASIC_MECHANICAL_PIPE);
    public static final BlockTypeTile<TileEntityMechanicalPipe> ADVANCED_MECHANICAL_PIPE = createPipe(PipeTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_MECHANICAL_PIPE);
    public static final BlockTypeTile<TileEntityMechanicalPipe> ELITE_MECHANICAL_PIPE = createPipe(PipeTier.ELITE, () -> MekanismTileEntityTypes.ELITE_MECHANICAL_PIPE);
    public static final BlockTypeTile<TileEntityMechanicalPipe> ULTIMATE_MECHANICAL_PIPE = createPipe(PipeTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_MECHANICAL_PIPE);

    public static final BlockTypeTile<TileEntityPressurizedTube> BASIC_PRESSURIZED_TUBE = createTube(TubeTier.BASIC, () -> MekanismTileEntityTypes.BASIC_PRESSURIZED_TUBE);
    public static final BlockTypeTile<TileEntityPressurizedTube> ADVANCED_PRESSURIZED_TUBE = createTube(TubeTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_PRESSURIZED_TUBE);
    public static final BlockTypeTile<TileEntityPressurizedTube> ELITE_PRESSURIZED_TUBE = createTube(TubeTier.ELITE, () -> MekanismTileEntityTypes.ELITE_PRESSURIZED_TUBE);
    public static final BlockTypeTile<TileEntityPressurizedTube> ULTIMATE_PRESSURIZED_TUBE = createTube(TubeTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_PRESSURIZED_TUBE);

    public static final BlockTypeTile<TileEntityLogisticalTransporter> BASIC_LOGISTICAL_TRANSPORTER = createTransporter(TransporterTier.BASIC, () -> MekanismTileEntityTypes.BASIC_LOGISTICAL_TRANSPORTER);
    public static final BlockTypeTile<TileEntityLogisticalTransporter> ADVANCED_LOGISTICAL_TRANSPORTER = createTransporter(TransporterTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_LOGISTICAL_TRANSPORTER);
    public static final BlockTypeTile<TileEntityLogisticalTransporter> ELITE_LOGISTICAL_TRANSPORTER = createTransporter(TransporterTier.ELITE, () -> MekanismTileEntityTypes.ELITE_LOGISTICAL_TRANSPORTER);
    public static final BlockTypeTile<TileEntityLogisticalTransporter> ULTIMATE_LOGISTICAL_TRANSPORTER = createTransporter(TransporterTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_LOGISTICAL_TRANSPORTER);

    public static final BlockTypeTile<TileEntityRestrictiveTransporter> RESTRICTIVE_TRANSPORTER = BlockTileBuilder.createBlock(() -> MekanismTileEntityTypes.RESTRICTIVE_TRANSPORTER, MekanismLang.DESCRIPTION_TRANSPORTER).build();
    public static final BlockTypeTile<TileEntityDiversionTransporter> DIVERSION_TRANSPORTER = BlockTileBuilder.createBlock(() -> MekanismTileEntityTypes.DIVERSION_TRANSPORTER, MekanismLang.DESCRIPTION_TRANSPORTER).build();

    public static final BlockTypeTile<TileEntityThermodynamicConductor> BASIC_THERMODYNAMIC_CONDUCTOR = createConductor(ConductorTier.BASIC, () -> MekanismTileEntityTypes.BASIC_THERMODYNAMIC_CONDUCTOR);
    public static final BlockTypeTile<TileEntityThermodynamicConductor> ADVANCED_THERMODYNAMIC_CONDUCTOR = createConductor(ConductorTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_THERMODYNAMIC_CONDUCTOR);
    public static final BlockTypeTile<TileEntityThermodynamicConductor> ELITE_THERMODYNAMIC_CONDUCTOR = createConductor(ConductorTier.ELITE, () -> MekanismTileEntityTypes.ELITE_THERMODYNAMIC_CONDUCTOR);
    public static final BlockTypeTile<TileEntityThermodynamicConductor> ULTIMATE_THERMODYNAMIC_CONDUCTOR = createConductor(ConductorTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_THERMODYNAMIC_CONDUCTOR);

    static {
        for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
            for (FactoryType type : EnumUtils.FACTORY_TYPES) {
                FACTORIES.put(tier, type, FactoryBuilder.createFactory(() -> MekanismTileEntityTypes.getFactoryTile(tier, type), type, tier).build());
            }
        }
    }

    public static Factory<?> getFactory(FactoryTier tier, FactoryType type) {
        return FACTORIES.get(tier, type);
    }

    private static <TILE extends TileEntityInductionCell> BlockTypeTile<TILE> createInductionCell(InductionCellTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile) {
        return BlockTileBuilder.createBlock(tile, MekanismLang.DESCRIPTION_INDUCTION_CELL)
              .withEnergyConfig(tier::getMaxEnergy)
              .with(new AttributeTier<>(tier))
              .internalMultiblock()
              .build();
    }

    private static <TILE extends TileEntityInductionProvider> BlockTypeTile<TILE> createInductionProvider(InductionProviderTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile) {
        return BlockTileBuilder.createBlock(tile, MekanismLang.DESCRIPTION_INDUCTION_PROVIDER)
              .with(new AttributeTier<>(tier))
              .internalMultiblock()
              .build();
    }

    private static <TILE extends TileEntityBin> Machine<TILE> createBin(BinTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        return MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_BIN)
              .with(new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock))
              .without(AttributeParticleFX.class, AttributeSecurity.class, AttributeUpgradeSupport.class, AttributeRedstone.class)
              .withComputerSupport(tier, "Bin")
              .build();
    }

    private static <TILE extends TileEntityEnergyCube> Machine<TILE> createEnergyCube(EnergyCubeTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        return MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_ENERGY_CUBE)
              .withGui(() -> MekanismContainerTypes.ENERGY_CUBE)
              .withEnergyConfig(tier::getMaxEnergy)
              .with(new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock), new AttributeStateFacing(BlockStateProperties.FACING))
              .withSideConfig(TransmissionType.ENERGY, TransmissionType.ITEM)
              .without(AttributeParticleFX.class, AttributeStateActive.class, AttributeUpgradeSupport.class)
              .withComputerSupport(tier, "EnergyCube")
              .build();
    }

    private static <TILE extends TileEntityFluidTank> Machine<TILE> createFluidTank(FluidTankTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        return MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_FLUID_TANK)
              .withGui(() -> MekanismContainerTypes.FLUID_TANK)
              .withCustomShape(BlockShapes.FLUID_TANK)
              .with(new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock))
              .without(AttributeParticleFX.class, AttributeStateFacing.class, AttributeRedstone.class, AttributeUpgradeSupport.class)
              .withComputerSupport(tier, "FluidTank")
              .build();
    }

    private static <TILE extends TileEntityChemicalTank> Machine<TILE> createChemicalTank(ChemicalTankTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        return MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_CHEMICAL_TANK)
              .withGui(() -> MekanismContainerTypes.CHEMICAL_TANK)
              .withCustomShape(BlockShapes.CHEMICAL_TANK)
              .with(new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock))
              .withSideConfig(TransmissionType.CHEMICAL, TransmissionType.ITEM)
              .without(AttributeParticleFX.class, AttributeStateActive.class, AttributeUpgradeSupport.class)
              .withComputerSupport(tier, "ChemicalTank")
              .build();
    }

    private static BlockTypeTile<TileEntityUniversalCable> createCable(CableTier tier, Supplier<TileEntityTypeRegistryObject<TileEntityUniversalCable>> tile) {
        return createTransmitter(tier, tile, MekanismLang.DESCRIPTION_CABLE);
    }

    private static BlockTypeTile<TileEntityMechanicalPipe> createPipe(PipeTier tier, Supplier<TileEntityTypeRegistryObject<TileEntityMechanicalPipe>> tile) {
        return createTransmitter(tier, tile, MekanismLang.DESCRIPTION_PIPE);
    }

    private static BlockTypeTile<TileEntityPressurizedTube> createTube(TubeTier tier, Supplier<TileEntityTypeRegistryObject<TileEntityPressurizedTube>> tile) {
        return createTransmitter(tier, tile, MekanismLang.DESCRIPTION_TUBE);
    }

    private static BlockTypeTile<TileEntityLogisticalTransporter> createTransporter(TransporterTier tier, Supplier<TileEntityTypeRegistryObject<TileEntityLogisticalTransporter>> tile) {
        return createTransmitter(tier, tile, MekanismLang.DESCRIPTION_TRANSPORTER);
    }

    private static BlockTypeTile<TileEntityThermodynamicConductor> createConductor(ConductorTier tier, Supplier<TileEntityTypeRegistryObject<TileEntityThermodynamicConductor>> tile) {
        return createTransmitter(tier, tile, MekanismLang.DESCRIPTION_CONDUCTOR);
    }

    private static <TILE extends TileEntityTransmitter> BlockTypeTile<TILE> createTransmitter(ITier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, ILangEntry description) {
        return BlockTileBuilder.createBlock(tile, description)
              .with(new AttributeTier<>(tier))
              .build();
    }
}
