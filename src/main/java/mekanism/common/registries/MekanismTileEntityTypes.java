package mekanism.common.registries;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.block.basic.BlockFluidTank;
import mekanism.common.block.prefab.BlockFactoryMachine.BlockFactory;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.integration.computer.ComputerCapabilityHelper;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.item.block.ItemBlockChemicalTank;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityCardboardBox;
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
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityCombiningFactory;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntityItemStackChemicalToItemStackFactory;
import mekanism.common.tile.factory.TileEntityItemStackToItemStackFactory;
import mekanism.common.tile.factory.TileEntitySawingFactory;
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
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityRestrictiveTransporter;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MekanismTileEntityTypes {

    private MekanismTileEntityTypes() {
    }

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(Mekanism.MODID);

    private static final Table<FactoryTier, FactoryType, TileEntityTypeRegistryObject<? extends TileEntityFactory<?>>> FACTORIES = HashBasedTable.create();

    static {
        for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
            registerFactory(tier, FactoryType.COMBINING, TileEntityCombiningFactory::new);
            registerFactory(tier, FactoryType.COMPRESSING, TileEntityItemStackChemicalToItemStackFactory::new);
            registerFactory(tier, FactoryType.CRUSHING, TileEntityItemStackToItemStackFactory::new);
            registerFactory(tier, FactoryType.ENRICHING, TileEntityItemStackToItemStackFactory::new);
            registerFactory(tier, FactoryType.INFUSING, TileEntityItemStackChemicalToItemStackFactory::new);
            registerFactory(tier, FactoryType.INJECTING, TileEntityItemStackChemicalToItemStackFactory::new);
            registerFactory(tier, FactoryType.PURIFYING, TileEntityItemStackChemicalToItemStackFactory::new);
            registerFactory(tier, FactoryType.SAWING, TileEntitySawingFactory::new);
            registerFactory(tier, FactoryType.SMELTING, TileEntityItemStackToItemStackFactory::new);
        }
    }

    private static void registerFactory(FactoryTier tier, FactoryType type, BlockEntityFactory<? extends TileEntityFactory<?>> factoryConstructor) {
        BlockRegistryObject<BlockFactory<?>, ItemBlockFactory> block = MekanismBlocks.getFactory(tier, type);
        TileEntityTypeRegistryObject<? extends TileEntityFactory<?>> tileRO = TILE_ENTITY_TYPES.mekBuilder(block, (pos, state) -> factoryConstructor.create(block, pos, state))
              .clientTicker(TileEntityMekanism::tickClient)
              .serverTicker(TileEntityMekanism::tickServer)
              .withSimple(Capabilities.CONFIG_CARD)
              .build();
        FACTORIES.put(tier, type, tileRO);
    }

    public static final TileEntityTypeRegistryObject<TileEntityBoundingBlock> BOUNDING_BLOCK = TILE_ENTITY_TYPES.builder(MekanismBlocks.BOUNDING_BLOCK, TileEntityBoundingBlock::new).build();

    //Regular Tiles
    public static final TileEntityTypeRegistryObject<TileEntityBoilerCasing> BOILER_CASING = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.BOILER_CASING, TileEntityBoilerCasing::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityBoilerValve> BOILER_VALVE = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.BOILER_VALVE, TileEntityBoilerValve::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityCardboardBox> CARDBOARD_BOX = TILE_ENTITY_TYPES.builder(MekanismBlocks.CARDBOARD_BOX, TileEntityCardboardBox::new).build();
    public static final TileEntityTypeRegistryObject<TileEntityChargepad> CHARGEPAD = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.CHARGEPAD, TileEntityChargepad::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityChemicalCrystallizer> CHEMICAL_CRYSTALLIZER = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.CHEMICAL_CRYSTALLIZER, TileEntityChemicalCrystallizer::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityChemicalDissolutionChamber> CHEMICAL_DISSOLUTION_CHAMBER = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, TileEntityChemicalDissolutionChamber::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityChemicalInfuser> CHEMICAL_INFUSER = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.CHEMICAL_INFUSER, TileEntityChemicalInfuser::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityChemicalInjectionChamber> CHEMICAL_INJECTION_CHAMBER = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, TileEntityChemicalInjectionChamber::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityChemicalOxidizer> CHEMICAL_OXIDIZER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.CHEMICAL_OXIDIZER, TileEntityChemicalOxidizer::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityChemicalWasher> CHEMICAL_WASHER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.CHEMICAL_WASHER, TileEntityChemicalWasher::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityCombiner> COMBINER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.COMBINER, TileEntityCombiner::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityCrusher> CRUSHER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.CRUSHER, TileEntityCrusher::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityDigitalMiner> DIGITAL_MINER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.DIGITAL_MINER, TileEntityDigitalMiner::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          //Item capabilities are handled only via offset capabilities
          .without(Capabilities.ITEM.block())
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityDynamicTank> DYNAMIC_TANK = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.DYNAMIC_TANK, TileEntityDynamicTank::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          //Disable item handler caps if we are the dynamic tank (but not the valve)
          .without(Capabilities.ITEM.block())
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityDynamicValve> DYNAMIC_VALVE = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.DYNAMIC_VALVE, TileEntityDynamicValve::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityElectricPump> ELECTRIC_PUMP = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.ELECTRIC_PUMP, TileEntityElectricPump::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityElectrolyticSeparator> ELECTROLYTIC_SEPARATOR = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.ELECTROLYTIC_SEPARATOR, TileEntityElectrolyticSeparator::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityEnergizedSmelter> ENERGIZED_SMELTER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.ENERGIZED_SMELTER, TileEntityEnergizedSmelter::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityEnrichmentChamber> ENRICHMENT_CHAMBER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.ENRICHMENT_CHAMBER, TileEntityEnrichmentChamber::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityFluidicPlenisher> FLUIDIC_PLENISHER = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.FLUIDIC_PLENISHER, TileEntityFluidicPlenisher::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityFormulaicAssemblicator> FORMULAIC_ASSEMBLICATOR = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, TileEntityFormulaicAssemblicator::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityFuelwoodHeater> FUELWOOD_HEATER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.FUELWOOD_HEATER, TileEntityFuelwoodHeater::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityInductionCasing> INDUCTION_CASING = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.INDUCTION_CASING, TileEntityInductionCasing::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          //Disable item handler caps if we are the induction casing (but not the port)
          .without(Capabilities.ITEM.block())
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityInductionPort> INDUCTION_PORT = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.INDUCTION_PORT, TileEntityInductionPort::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityLaser> LASER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.LASER, TileEntityLaser::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityLaserAmplifier> LASER_AMPLIFIER = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.LASER_AMPLIFIER, TileEntityLaserAmplifier::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .withSimple(Capabilities.LASER_RECEPTOR)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityLaserTractorBeam> LASER_TRACTOR_BEAM = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.LASER_TRACTOR_BEAM, TileEntityLaserTractorBeam::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.LASER_RECEPTOR)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalSorter> LOGISTICAL_SORTER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.LOGISTICAL_SORTER, TileEntityLogisticalSorter::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityMetallurgicInfuser> METALLURGIC_INFUSER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.METALLURGIC_INFUSER, TileEntityMetallurgicInfuser::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityOredictionificator> OREDICTIONIFICATOR = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.OREDICTIONIFICATOR, TileEntityOredictionificator::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityOsmiumCompressor> OSMIUM_COMPRESSOR = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.OSMIUM_COMPRESSOR, TileEntityOsmiumCompressor::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityPersonalBarrel> PERSONAL_BARREL = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.PERSONAL_BARREL, TileEntityPersonalBarrel::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityPersonalChest> PERSONAL_CHEST = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.PERSONAL_CHEST, TileEntityPersonalChest::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityPrecisionSawmill> PRECISION_SAWMILL = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.PRECISION_SAWMILL, TileEntityPrecisionSawmill::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityPressureDisperser> PRESSURE_DISPERSER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.PRESSURE_DISPERSER, TileEntityPressureDisperser::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedReactionChamber> PRESSURIZED_REACTION_CHAMBER = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, TileEntityPressurizedReactionChamber::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityPurificationChamber> PURIFICATION_CHAMBER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.PURIFICATION_CHAMBER, TileEntityPurificationChamber::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityQuantumEntangloporter> QUANTUM_ENTANGLOPORTER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.QUANTUM_ENTANGLOPORTER, TileEntityQuantumEntangloporter::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityResistiveHeater> RESISTIVE_HEATER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.RESISTIVE_HEATER, TileEntityResistiveHeater::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityModificationStation> MODIFICATION_STATION = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.MODIFICATION_STATION, TileEntityModificationStation::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityIsotopicCentrifuge> ISOTOPIC_CENTRIFUGE = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.ISOTOPIC_CENTRIFUGE, TileEntityIsotopicCentrifuge::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityNutritionalLiquifier> NUTRITIONAL_LIQUIFIER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.NUTRITIONAL_LIQUIFIER, TileEntityNutritionalLiquifier::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityRotaryCondensentrator> ROTARY_CONDENSENTRATOR = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.ROTARY_CONDENSENTRATOR, TileEntityRotaryCondensentrator::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntitySecurityDesk> SECURITY_DESK = TILE_ENTITY_TYPES.builder(MekanismBlocks.SECURITY_DESK, TileEntitySecurityDesk::new)
          .serverTicker(TileEntityMekanism::tickServer)
          //Even though there are inventory slots make this return none as accessible by automation, as then people could lock items to other
          // people unintentionally. We only provide access to the security desk as an "owner object" which means that all access checks will be handled as requiring the owner
          .withSimple(IBlockSecurityUtils.INSTANCE.ownerCapability())
          .build();
    public static final TileEntityTypeRegistryObject<TileEntitySeismicVibrator> SEISMIC_VIBRATOR = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.SEISMIC_VIBRATOR, TileEntitySeismicVibrator::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntitySolarNeutronActivator> SOLAR_NEUTRON_ACTIVATOR = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, TileEntitySolarNeutronActivator::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityStructuralGlass> STRUCTURAL_GLASS = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.STRUCTURAL_GLASS, TileEntityStructuralGlass::new)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntitySuperheatingElement> SUPERHEATING_ELEMENT = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.SUPERHEATING_ELEMENT, TileEntitySuperheatingElement::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityTeleporter> TELEPORTER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.TELEPORTER, TileEntityTeleporter::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityThermalEvaporationBlock> THERMAL_EVAPORATION_BLOCK = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.THERMAL_EVAPORATION_BLOCK, TileEntityThermalEvaporationBlock::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityThermalEvaporationController> THERMAL_EVAPORATION_CONTROLLER = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, TileEntityThermalEvaporationController::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityThermalEvaporationValve> THERMAL_EVAPORATION_VALVE = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.THERMAL_EVAPORATION_VALVE, TileEntityThermalEvaporationValve::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityRadioactiveWasteBarrel> RADIOACTIVE_WASTE_BARREL = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.RADIOACTIVE_WASTE_BARREL, TileEntityRadioactiveWasteBarrel::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityIndustrialAlarm> INDUSTRIAL_ALARM = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.INDUSTRIAL_ALARM, TileEntityIndustrialAlarm::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityAntiprotonicNucleosynthesizer> ANTIPROTONIC_NUCLEOSYNTHESIZER = TILE_ENTITY_TYPES
          .mekBuilder(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, TileEntityAntiprotonicNucleosynthesizer::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityPigmentExtractor> PIGMENT_EXTRACTOR = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.PIGMENT_EXTRACTOR, TileEntityPigmentExtractor::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityPigmentMixer> PIGMENT_MIXER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.PIGMENT_MIXER, TileEntityPigmentMixer::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityPaintingMachine> PAINTING_MACHINE = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.PAINTING_MACHINE, TileEntityPaintingMachine::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntitySPSCasing> SPS_CASING = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.SPS_CASING, TileEntitySPSCasing::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntitySPSPort> SPS_PORT = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.SPS_PORT, TileEntitySPSPort::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntitySuperchargedCoil> SUPERCHARGED_COIL = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.SUPERCHARGED_COIL, TileEntitySuperchargedCoil::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityDimensionalStabilizer> DIMENSIONAL_STABILIZER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.DIMENSIONAL_STABILIZER, TileEntityDimensionalStabilizer::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();

    public static final TileEntityTypeRegistryObject<TileEntityQIODriveArray> QIO_DRIVE_ARRAY = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.QIO_DRIVE_ARRAY, TileEntityQIODriveArray::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityQIODashboard> QIO_DASHBOARD = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.QIO_DASHBOARD, TileEntityQIODashboard::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityQIOImporter> QIO_IMPORTER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.QIO_IMPORTER, TileEntityQIOImporter::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityQIOExporter> QIO_EXPORTER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.QIO_EXPORTER, TileEntityQIOExporter::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityQIORedstoneAdapter> QIO_REDSTONE_ADAPTER = TILE_ENTITY_TYPES.mekBuilder(MekanismBlocks.QIO_REDSTONE_ADAPTER, TileEntityQIORedstoneAdapter::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();

    //Transmitters
    public static final TileEntityTypeRegistryObject<TileEntityDiversionTransporter> DIVERSION_TRANSPORTER = registerDiversionTransporter();
    public static final TileEntityTypeRegistryObject<TileEntityRestrictiveTransporter> RESTRICTIVE_TRANSPORTER = registerTransporter(MekanismBlocks.RESTRICTIVE_TRANSPORTER, TileEntityRestrictiveTransporter::new);
    //Logistic Transporters
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> BASIC_LOGISTICAL_TRANSPORTER = registerTransporter(MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, TileEntityLogisticalTransporter::new);
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> ADVANCED_LOGISTICAL_TRANSPORTER = registerTransporter(MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, TileEntityLogisticalTransporter::new);
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> ELITE_LOGISTICAL_TRANSPORTER = registerTransporter(MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER, TileEntityLogisticalTransporter::new);
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> ULTIMATE_LOGISTICAL_TRANSPORTER = registerTransporter(MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER, TileEntityLogisticalTransporter::new);
    //Mechanical Pipes
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalPipe> BASIC_MECHANICAL_PIPE = registerPipe(MekanismBlocks.BASIC_MECHANICAL_PIPE);
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalPipe> ADVANCED_MECHANICAL_PIPE = registerPipe(MekanismBlocks.ADVANCED_MECHANICAL_PIPE);
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalPipe> ELITE_MECHANICAL_PIPE = registerPipe(MekanismBlocks.ELITE_MECHANICAL_PIPE);
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalPipe> ULTIMATE_MECHANICAL_PIPE = registerPipe(MekanismBlocks.ULTIMATE_MECHANICAL_PIPE);
    //Pressurized Tubes
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedTube> BASIC_PRESSURIZED_TUBE = registerTube(MekanismBlocks.BASIC_PRESSURIZED_TUBE);
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedTube> ADVANCED_PRESSURIZED_TUBE = registerTube(MekanismBlocks.ADVANCED_PRESSURIZED_TUBE);
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedTube> ELITE_PRESSURIZED_TUBE = registerTube(MekanismBlocks.ELITE_PRESSURIZED_TUBE);
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedTube> ULTIMATE_PRESSURIZED_TUBE = registerTube(MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE);
    //Thermodynamic Conductors
    public static final TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> BASIC_THERMODYNAMIC_CONDUCTOR = registerConductor(MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR);
    public static final TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> ADVANCED_THERMODYNAMIC_CONDUCTOR = registerConductor(MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR);
    public static final TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> ELITE_THERMODYNAMIC_CONDUCTOR = registerConductor(MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR);
    public static final TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> ULTIMATE_THERMODYNAMIC_CONDUCTOR = registerConductor(MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR);
    //Universal Cables
    public static final TileEntityTypeRegistryObject<TileEntityUniversalCable> BASIC_UNIVERSAL_CABLE = registerCable(MekanismBlocks.BASIC_UNIVERSAL_CABLE);
    public static final TileEntityTypeRegistryObject<TileEntityUniversalCable> ADVANCED_UNIVERSAL_CABLE = registerCable(MekanismBlocks.ADVANCED_UNIVERSAL_CABLE);
    public static final TileEntityTypeRegistryObject<TileEntityUniversalCable> ELITE_UNIVERSAL_CABLE = registerCable(MekanismBlocks.ELITE_UNIVERSAL_CABLE);
    public static final TileEntityTypeRegistryObject<TileEntityUniversalCable> ULTIMATE_UNIVERSAL_CABLE = registerCable(MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE);

    private static TileEntityTypeRegistryObject<TileEntityDiversionTransporter> registerDiversionTransporter() {
        BlockEntityTypeBuilder<TileEntityDiversionTransporter> builder = transporterBuilder(MekanismBlocks.DIVERSION_TRANSPORTER, TileEntityDiversionTransporter::new);
        if (Mekanism.hooks.computerCompatEnabled()) {
            ComputerCapabilityHelper.addComputerCapabilities(builder, ConstantPredicates.ALWAYS_TRUE);
        }
        return builder.build();
    }

    private static <BE extends TileEntityLogisticalTransporterBase> TileEntityTypeRegistryObject<BE> registerTransporter(DeferredHolder<Block, ?> block, BlockEntityFactory<BE> factory) {
        return transporterBuilder(block, factory).build();
    }

    private static <BE extends TileEntityLogisticalTransporterBase> BlockEntityTypeBuilder<BE> transporterBuilder(DeferredHolder<Block, ?> block, BlockEntityFactory<BE> factory) {
        return transmitterBuilder(block, factory)
              .clientTicker(TileEntityLogisticalTransporterBase::tickClient)
              .with(Capabilities.ITEM.block(), CapabilityTileEntity.ITEM_HANDLER_PROVIDER);
    }

    private static TileEntityTypeRegistryObject<TileEntityMechanicalPipe> registerPipe(DeferredHolder<Block, ?> block) {
        BlockEntityTypeBuilder<TileEntityMechanicalPipe> builder = transmitterBuilder(block, TileEntityMechanicalPipe::new)
              .with(Capabilities.FLUID.block(), CapabilityTileEntity.FLUID_HANDLER_PROVIDER);
        if (Mekanism.hooks.computerCompatEnabled()) {
            ComputerCapabilityHelper.addComputerCapabilities(builder, ConstantPredicates.ALWAYS_TRUE);
        }
        return builder.build();
    }

    private static TileEntityTypeRegistryObject<TileEntityPressurizedTube> registerTube(DeferredHolder<Block, ?> block) {
        BlockEntityTypeBuilder<TileEntityPressurizedTube> builder = transmitterBuilder(block, TileEntityPressurizedTube::new)
              .with(Capabilities.CHEMICAL.block(), CapabilityTileEntity.CHEMICAL_HANDLER_PROVIDER);
        if (Mekanism.hooks.computerCompatEnabled()) {
            ComputerCapabilityHelper.addComputerCapabilities(builder, ConstantPredicates.ALWAYS_TRUE);
        }
        return builder.build();
    }

    private static TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> registerConductor(DeferredHolder<Block, ?> block) {
        return transmitterBuilder(block, TileEntityThermodynamicConductor::new)
              .with(Capabilities.HEAT, CapabilityTileEntity.HEAT_HANDLER_PROVIDER)
              .build();
    }

    private static TileEntityTypeRegistryObject<TileEntityUniversalCable> registerCable(DeferredHolder<Block, ?> block) {
        BlockEntityTypeBuilder<TileEntityUniversalCable> builder = transmitterBuilder(block, TileEntityUniversalCable::new);
        EnergyCompatUtils.addBlockCapabilities(builder);
        if (Mekanism.hooks.computerCompatEnabled()) {
            ComputerCapabilityHelper.addComputerCapabilities(builder, ConstantPredicates.ALWAYS_TRUE);
        }
        return builder.build();
    }

    private static <BE extends TileEntityTransmitter> BlockEntityTypeBuilder<BE> transmitterBuilder(DeferredHolder<Block, ?> block, BlockEntityFactory<BE> factory) {
        return TILE_ENTITY_TYPES.builder(block, (pos, state) -> factory.create(block, pos, state))
              .serverTicker(TileEntityTransmitter::tickServer)
              .withSimple(Capabilities.ALLOY_INTERACTION)
              .with(Capabilities.CONFIGURABLE, TileEntityTransmitter.CONFIGURABLE_PROVIDER);
    }

    //Tiered Tiles
    //Energy Cubes
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> BASIC_ENERGY_CUBE = registerEnergyCube(MekanismBlocks.BASIC_ENERGY_CUBE);
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> ADVANCED_ENERGY_CUBE = registerEnergyCube(MekanismBlocks.ADVANCED_ENERGY_CUBE);
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> ELITE_ENERGY_CUBE = registerEnergyCube(MekanismBlocks.ELITE_ENERGY_CUBE);
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> ULTIMATE_ENERGY_CUBE = registerEnergyCube(MekanismBlocks.ULTIMATE_ENERGY_CUBE);
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> CREATIVE_ENERGY_CUBE = registerEnergyCube(MekanismBlocks.CREATIVE_ENERGY_CUBE);

    private static TileEntityTypeRegistryObject<TileEntityEnergyCube> registerEnergyCube(DeferredHolder<Block, BlockEnergyCube> block) {
        return TILE_ENTITY_TYPES.mekBuilder(block, (pos, state) -> new TileEntityEnergyCube(block, pos, state))
              .serverTicker(TileEntityMekanism::tickServer)
              .withSimple(Capabilities.CONFIG_CARD)
              .build();
    }

    //Chemical Tanks
    public static final TileEntityTypeRegistryObject<TileEntityChemicalTank> BASIC_CHEMICAL_TANK = registerChemicalTank(MekanismBlocks.BASIC_CHEMICAL_TANK);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalTank> ADVANCED_CHEMICAL_TANK = registerChemicalTank(MekanismBlocks.ADVANCED_CHEMICAL_TANK);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalTank> ELITE_CHEMICAL_TANK = registerChemicalTank(MekanismBlocks.ELITE_CHEMICAL_TANK);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalTank> ULTIMATE_CHEMICAL_TANK = registerChemicalTank(MekanismBlocks.ULTIMATE_CHEMICAL_TANK);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalTank> CREATIVE_CHEMICAL_TANK = registerChemicalTank(MekanismBlocks.CREATIVE_CHEMICAL_TANK);

    private static TileEntityTypeRegistryObject<TileEntityChemicalTank> registerChemicalTank(BlockRegistryObject<?, ItemBlockChemicalTank> block) {
        return TILE_ENTITY_TYPES.mekBuilder(block, (pos, state) -> new TileEntityChemicalTank(block, pos, state))
              .serverTicker(TileEntityMekanism::tickServer)
              .withSimple(Capabilities.CONFIG_CARD)
              .build();
    }

    //Fluid Tanks
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> BASIC_FLUID_TANK = registerFluidTank(MekanismBlocks.BASIC_FLUID_TANK);
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> ADVANCED_FLUID_TANK = registerFluidTank(MekanismBlocks.ADVANCED_FLUID_TANK);
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> ELITE_FLUID_TANK = registerFluidTank(MekanismBlocks.ELITE_FLUID_TANK);
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> ULTIMATE_FLUID_TANK = registerFluidTank(MekanismBlocks.ULTIMATE_FLUID_TANK);
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> CREATIVE_FLUID_TANK = registerFluidTank(MekanismBlocks.CREATIVE_FLUID_TANK);

    private static TileEntityTypeRegistryObject<TileEntityFluidTank> registerFluidTank(DeferredHolder<Block, BlockFluidTank> block) {
        return TILE_ENTITY_TYPES.mekBuilder(block, (pos, state) -> new TileEntityFluidTank(block, pos, state))
              .clientTicker(TileEntityMekanism::tickClient)
              .serverTicker(TileEntityMekanism::tickServer)
              .withSimple(Capabilities.CONFIG_CARD)
              .withSimple(Capabilities.CONFIGURABLE)
              .build();
    }

    //Bins
    public static final TileEntityTypeRegistryObject<TileEntityBin> BASIC_BIN = registerBin(MekanismBlocks.BASIC_BIN);
    public static final TileEntityTypeRegistryObject<TileEntityBin> ADVANCED_BIN = registerBin(MekanismBlocks.ADVANCED_BIN);
    public static final TileEntityTypeRegistryObject<TileEntityBin> ELITE_BIN = registerBin(MekanismBlocks.ELITE_BIN);
    public static final TileEntityTypeRegistryObject<TileEntityBin> ULTIMATE_BIN = registerBin(MekanismBlocks.ULTIMATE_BIN);
    public static final TileEntityTypeRegistryObject<TileEntityBin> CREATIVE_BIN = registerBin(MekanismBlocks.CREATIVE_BIN);

    private static TileEntityTypeRegistryObject<TileEntityBin> registerBin(DeferredHolder<Block, BlockBin> block) {
        return TILE_ENTITY_TYPES.mekBuilder(block, (pos, state) -> new TileEntityBin(block, pos, state))
              .serverTicker(TileEntityMekanism::tickServer)
              .withSimple(Capabilities.CONFIGURABLE)
              .build();
    }

    //Induction Cells
    //Note: We don't expose any caps or have induction cells or providers tick, so we just call the basic builder and don't add anything
    // we don't need to specify without for the cells as we don't add it when using the basic non mekBuilder method
    public static final TileEntityTypeRegistryObject<TileEntityInductionCell> BASIC_INDUCTION_CELL = TILE_ENTITY_TYPES.builder(MekanismBlocks.BASIC_INDUCTION_CELL, (pos, state) -> new TileEntityInductionCell(MekanismBlocks.BASIC_INDUCTION_CELL, pos, state)).build();
    public static final TileEntityTypeRegistryObject<TileEntityInductionCell> ADVANCED_INDUCTION_CELL = TILE_ENTITY_TYPES.builder(MekanismBlocks.ADVANCED_INDUCTION_CELL, (pos, state) -> new TileEntityInductionCell(MekanismBlocks.ADVANCED_INDUCTION_CELL, pos, state)).build();
    public static final TileEntityTypeRegistryObject<TileEntityInductionCell> ELITE_INDUCTION_CELL = TILE_ENTITY_TYPES.builder(MekanismBlocks.ELITE_INDUCTION_CELL, (pos, state) -> new TileEntityInductionCell(MekanismBlocks.ELITE_INDUCTION_CELL, pos, state)).build();
    public static final TileEntityTypeRegistryObject<TileEntityInductionCell> ULTIMATE_INDUCTION_CELL = TILE_ENTITY_TYPES.builder(MekanismBlocks.ULTIMATE_INDUCTION_CELL, (pos, state) -> new TileEntityInductionCell(MekanismBlocks.ULTIMATE_INDUCTION_CELL, pos, state)).build();
    //Induction Providers
    public static final TileEntityTypeRegistryObject<TileEntityInductionProvider> BASIC_INDUCTION_PROVIDER = TILE_ENTITY_TYPES.builder(MekanismBlocks.BASIC_INDUCTION_PROVIDER, (pos, state) -> new TileEntityInductionProvider(MekanismBlocks.BASIC_INDUCTION_PROVIDER, pos, state)).build();
    public static final TileEntityTypeRegistryObject<TileEntityInductionProvider> ADVANCED_INDUCTION_PROVIDER = TILE_ENTITY_TYPES.builder(MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, (pos, state) -> new TileEntityInductionProvider(MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, pos, state)).build();
    public static final TileEntityTypeRegistryObject<TileEntityInductionProvider> ELITE_INDUCTION_PROVIDER = TILE_ENTITY_TYPES.builder(MekanismBlocks.ELITE_INDUCTION_PROVIDER, (pos, state) -> new TileEntityInductionProvider(MekanismBlocks.ELITE_INDUCTION_PROVIDER, pos, state)).build();
    public static final TileEntityTypeRegistryObject<TileEntityInductionProvider> ULTIMATE_INDUCTION_PROVIDER = TILE_ENTITY_TYPES.builder(MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER, (pos, state) -> new TileEntityInductionProvider(MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER, pos, state)).build();

    public static TileEntityTypeRegistryObject<? extends TileEntityFactory<?>> getFactoryTile(FactoryTier tier, FactoryType type) {
        return FACTORIES.get(tier, type);
    }

    @SuppressWarnings("unchecked")
    public static TileEntityTypeRegistryObject<? extends TileEntityFactory<?>>[] getFactoryTiles() {
        return FACTORIES.values().toArray(new TileEntityTypeRegistryObject[0]);
    }

    @FunctionalInterface
    private interface BlockEntityFactory<BE extends BlockEntity> {

        BE create(Holder<Block> block, BlockPos pos, BlockState state);
    }
}