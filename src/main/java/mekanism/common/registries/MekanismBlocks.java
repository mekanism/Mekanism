package mekanism.common.registries;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.tier.ITier;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.BlockIndustrialAlarm;
import mekanism.common.block.BlockOre;
import mekanism.common.block.BlockPersonalBarrel;
import mekanism.common.block.BlockPersonalChest;
import mekanism.common.block.BlockQIOComponent;
import mekanism.common.block.BlockRadioactiveWasteBarrel;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.block.basic.BlockChargepad;
import mekanism.common.block.basic.BlockFluidTank;
import mekanism.common.block.basic.BlockLogisticalSorter;
import mekanism.common.block.basic.BlockResource;
import mekanism.common.block.basic.BlockStructuralGlass;
import mekanism.common.block.prefab.BlockBase;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.block.prefab.BlockFactoryMachine;
import mekanism.common.block.prefab.BlockFactoryMachine.BlockFactory;
import mekanism.common.block.prefab.BlockFactoryMachine.BlockFactoryMachineModel;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.block.transmitter.BlockSmallTransmitter;
import mekanism.common.component.FilterAware;
import mekanism.common.component.OverflowAware;
import mekanism.common.component.SpecializedTransporter;
import mekanism.common.component.StabilizedChunks;
import mekanism.common.component.component.AttachedEjector;
import mekanism.common.component.component.AttachedSideConfig;
import mekanism.common.component.component.AttachedSideConfig.LightConfigInfo;
import mekanism.common.component.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.component.containers.chemical.ComponentBackedChemicalTankTank;
import mekanism.common.component.containers.fluid.ComponentBackedFluidTankFluidTank;
import mekanism.common.component.containers.fluid.FluidTanksBuilder;
import mekanism.common.component.containers.heat.HeatCapacitorBuilder;
import mekanism.common.component.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.component.containers.item.ItemSlotsBuilder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.Factory;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.item.block.ItemBlockChemicalTank;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.item.block.ItemBlockInductionCell;
import mekanism.common.item.block.ItemBlockLaserAmplifier;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.item.block.machine.ItemBlockLaserTractorBeam;
import mekanism.common.item.block.machine.ItemBlockQIOComponent;
import mekanism.common.item.block.machine.ItemBlockQuantumEntangloporter;
import mekanism.common.item.block.machine.ItemBlockResistiveHeater;
import mekanism.common.item.block.machine.ItemBlockTeleporter;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.DoubleItem;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.EitherSideChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.FluidChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemFluidChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleFluid;
import mekanism.common.recipe.lookup.cache.RotaryInputRecipeCache;
import mekanism.common.recipe.lookup.cache.SingleInputRecipeCache;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tier.CableTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tier.PipeTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.factory.TileEntityFactory;
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
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
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
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class MekanismBlocks {

    private MekanismBlocks() {
    }

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(Mekanism.MODID);

    public static final Map<BlockResourceInfo, BlockRegistryObject<?, ?>> PROCESSED_RESOURCE_BLOCKS = new EnumMap<>(BlockResourceInfo.class);
    public static final Map<OreType, OreBlockType> ORES = new LinkedHashMap<>();

    private static final Table<FactoryTier, FactoryType, BlockRegistryObject<BlockFactory<?>, ItemBlockTooltip>> FACTORIES = HashBasedTable.create();

    static {
        // factories
        for (FactoryTier tier : FactoryTier.VALUES) {
            for (FactoryType type : FactoryType.VALUES) {
                FACTORIES.put(tier, type, registerFactory(MekanismBlockTypes.getFactory(tier, type)));
            }
        }
        // resource blocks
        for (PrimaryResource resource : PrimaryResource.VALUES) {
            BlockResourceInfo resourceInfo = resource.getResourceBlockInfo();
            if (resourceInfo != null) {
                PROCESSED_RESOURCE_BLOCKS.put(resourceInfo, registerResourceBlock(resourceInfo));
            }
            BlockResourceInfo rawResource = resource.getRawResourceBlockInfo();
            if (rawResource != null) {
                PROCESSED_RESOURCE_BLOCKS.put(rawResource, registerResourceBlock(rawResource));
            }
        }
        // ores
        for (OreType ore : OreType.VALUES) {
            ORES.put(ore, registerOre(ore));
        }
    }

    public static final BlockRegistryObject<BlockResource, BlockItem> CHARCOAL_BLOCK = registerResourceBlock(BlockResourceInfo.CHARCOAL);
    public static final BlockRegistryObject<BlockResource, BlockItem> BRONZE_BLOCK = registerResourceBlock(BlockResourceInfo.BRONZE);
    public static final BlockRegistryObject<BlockResource, BlockItem> STEEL_BLOCK = registerResourceBlock(BlockResourceInfo.STEEL);
    public static final BlockRegistryObject<BlockResource, BlockItem> FLUORITE_BLOCK = registerResourceBlock(BlockResourceInfo.FLUORITE);
    public static final BlockRegistryObject<BlockResource, BlockItem> REFINED_OBSIDIAN_BLOCK = registerResourceBlock(BlockResourceInfo.REFINED_OBSIDIAN);
    public static final BlockRegistryObject<BlockResource, BlockItem> REFINED_GLOWSTONE_BLOCK = registerResourceBlock(BlockResourceInfo.REFINED_GLOWSTONE);

    public static final BlockRegistryObject<BlockBin, ItemBlockBin> BASIC_BIN = registerBin(MekanismBlockTypes.BASIC_BIN);
    public static final BlockRegistryObject<BlockBin, ItemBlockBin> ADVANCED_BIN = registerBin(MekanismBlockTypes.ADVANCED_BIN);
    public static final BlockRegistryObject<BlockBin, ItemBlockBin> ELITE_BIN = registerBin(MekanismBlockTypes.ELITE_BIN);
    public static final BlockRegistryObject<BlockBin, ItemBlockBin> ULTIMATE_BIN = registerBin(MekanismBlockTypes.ULTIMATE_BIN);
    public static final BlockRegistryObject<BlockBin, ItemBlockBin> CREATIVE_BIN = registerBin(MekanismBlockTypes.CREATIVE_BIN);

    public static final BlockRegistryObject<BlockBase<BlockType>, BlockItem> TELEPORTER_FRAME = BLOCKS.register("teleporter_frame", properties -> new BlockBase<>(MekanismBlockTypes.TELEPORTER_FRAME, properties.requiresCorrectToolForDrops().strength(5, 6).mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockBase<BlockType>, BlockItem> STEEL_CASING = BLOCKS.register("steel_casing", properties -> new BlockBase<>(MekanismBlockTypes.STEEL_CASING, properties.requiresCorrectToolForDrops().strength(3.5F, 9).mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityDynamicTank>, BlockItem> DYNAMIC_TANK = BLOCKS.register("dynamic_tank", properties -> new BlockBasicMultiblock<>(MekanismBlockTypes.DYNAMIC_TANK, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.COLOR_GRAY)));
    public static final BlockRegistryObject<BlockStructuralGlass<TileEntityStructuralGlass>, BlockItem> STRUCTURAL_GLASS = BLOCKS.register("structural_glass", properties -> new BlockStructuralGlass<>(MekanismBlockTypes.STRUCTURAL_GLASS, properties));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityDynamicValve>, BlockItem> DYNAMIC_VALVE = BLOCKS.register("dynamic_valve", properties -> new BlockBasicMultiblock<>(MekanismBlockTypes.DYNAMIC_VALVE, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.COLOR_GRAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityThermalEvaporationController>, BlockItem> THERMAL_EVAPORATION_CONTROLLER = BLOCKS.register("thermal_evaporation_controller", properties -> new BlockBasicMultiblock<>(MekanismBlockTypes.THERMAL_EVAPORATION_CONTROLLER, BlockBasicMultiblock.defaultProperties(properties).mapColor(BlockResourceInfo.BRONZE.getMapColor())));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityThermalEvaporationValve>, BlockItem> THERMAL_EVAPORATION_VALVE = BLOCKS.register("thermal_evaporation_valve", properties -> new BlockBasicMultiblock<>(MekanismBlockTypes.THERMAL_EVAPORATION_VALVE, BlockBasicMultiblock.defaultProperties(properties).mapColor(BlockResourceInfo.BRONZE.getMapColor())));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityThermalEvaporationBlock>, BlockItem> THERMAL_EVAPORATION_BLOCK = BLOCKS.register("thermal_evaporation_block", properties -> new BlockBasicMultiblock<>(MekanismBlockTypes.THERMAL_EVAPORATION_BLOCK, BlockBasicMultiblock.defaultProperties(properties).mapColor(BlockResourceInfo.BRONZE.getMapColor())));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityInductionCasing>, BlockItem> INDUCTION_CASING = BLOCKS.register("induction_casing", properties -> new BlockBasicMultiblock<>(MekanismBlockTypes.INDUCTION_CASING, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityInductionPort>, BlockItem> INDUCTION_PORT = BLOCKS.register("induction_port", properties -> new BlockBasicMultiblock<>(MekanismBlockTypes.INDUCTION_PORT, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.COLOR_LIGHT_GRAY)));

    public static final BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> BASIC_INDUCTION_CELL = registerInductionCell(MekanismBlockTypes.BASIC_INDUCTION_CELL);
    public static final BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> ADVANCED_INDUCTION_CELL = registerInductionCell(MekanismBlockTypes.ADVANCED_INDUCTION_CELL);
    public static final BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> ELITE_INDUCTION_CELL = registerInductionCell(MekanismBlockTypes.ELITE_INDUCTION_CELL);
    public static final BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> ULTIMATE_INDUCTION_CELL = registerInductionCell(MekanismBlockTypes.ULTIMATE_INDUCTION_CELL);

    public static final BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, BlockItem> BASIC_INDUCTION_PROVIDER = registerInductionProvider(MekanismBlockTypes.BASIC_INDUCTION_PROVIDER);
    public static final BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, BlockItem> ADVANCED_INDUCTION_PROVIDER = registerInductionProvider(MekanismBlockTypes.ADVANCED_INDUCTION_PROVIDER);
    public static final BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, BlockItem> ELITE_INDUCTION_PROVIDER = registerInductionProvider(MekanismBlockTypes.ELITE_INDUCTION_PROVIDER);
    public static final BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, BlockItem> ULTIMATE_INDUCTION_PROVIDER = registerInductionProvider(MekanismBlockTypes.ULTIMATE_INDUCTION_PROVIDER);

    public static final BlockRegistryObject<BlockTile<TileEntitySuperheatingElement, BlockTypeTile<TileEntitySuperheatingElement>>, BlockItem> SUPERHEATING_ELEMENT = BLOCKS.register("superheating_element", properties -> new BlockTile<>(MekanismBlockTypes.SUPERHEATING_ELEMENT, BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_GRAY)));
    public static final BlockRegistryObject<BlockTile<TileEntityPressureDisperser, BlockTypeTile<TileEntityPressureDisperser>>, BlockItem> PRESSURE_DISPERSER = BLOCKS.register("pressure_disperser", properties -> new BlockTile<>(MekanismBlockTypes.PRESSURE_DISPERSER, BlockTile.defaultProperties(properties).mapColor(MapColor.DEEPSLATE)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityBoilerCasing>, BlockItem> BOILER_CASING = BLOCKS.register("boiler_casing", properties -> new BlockBasicMultiblock<>(MekanismBlockTypes.BOILER_CASING, BlockBasicMultiblock.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityBoilerValve>, BlockItem> BOILER_VALVE = BLOCKS.register("boiler_valve", properties -> new BlockBasicMultiblock<>(MekanismBlockTypes.BOILER_VALVE, BlockBasicMultiblock.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockTileModel<TileEntitySecurityDesk, BlockTypeTile<TileEntitySecurityDesk>>, ItemBlockTooltip> SECURITY_DESK =
          BLOCKS.registerDetails("security_desk", properties -> new BlockTileModel<>(MekanismBlockTypes.SECURITY_DESK,
                BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor()))
          ).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                .addUnlockSlot()
                .addLockSlot()
                .build()
          ));
    public static final BlockRegistryObject<BlockRadioactiveWasteBarrel, BlockItem> RADIOACTIVE_WASTE_BARREL = BLOCKS.registerSimpleItem("radioactive_waste_barrel", BlockRadioactiveWasteBarrel::new,
          properties -> properties.component(MekanismDataComponents.WASTE_DECAY, Unit.INSTANCE));
    public static final BlockRegistryObject<BlockIndustrialAlarm, BlockItem> INDUSTRIAL_ALARM = BLOCKS.registerSimpleItem("industrial_alarm", BlockIndustrialAlarm::new,
          properties -> properties.component(MekanismDataComponents.DETAILS, Unit.INSTANCE));

    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityEnrichmentChamber, FactoryMachine<TileEntityEnrichmentChamber>>, ItemBlockTooltip> ENRICHMENT_CHAMBER =
          BLOCKS.register("enrichment_chamber", properties -> new BlockFactoryMachine<>(MekanismBlockTypes.ENRICHMENT_CHAMBER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.FACTORY_TYPE, FactoryType.ENRICHING)
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.ELECTRIC_MACHINE)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                .addInput(MekanismRecipeType.ENRICHING, SingleInputRecipeCache::containsInput)
                .addOutput()
                .addEnergy()
                .build()
          ));
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityOsmiumCompressor, FactoryMachine<TileEntityOsmiumCompressor>>, ItemBlockTooltip> OSMIUM_COMPRESSOR =
          BLOCKS.register("osmium_compressor", properties -> new BlockFactoryMachine<>(MekanismBlockTypes.OSMIUM_COMPRESSOR, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.FACTORY_TYPE, FactoryType.COMPRESSING)
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.ADVANCED_MACHINE)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityAdvancedElectricMachine.MAX_GAS, MekanismRecipeType.COMPRESSING, ItemChemical::containsInputB)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addInput(MekanismRecipeType.COMPRESSING, ItemChemical::containsInputA)
                      .addChemicalFillOrConvertSlot(0)
                      .addOutput()
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityCombiner, FactoryMachine<TileEntityCombiner>>, ItemBlockTooltip> COMBINER =
          BLOCKS.register("combiner", properties -> new BlockFactoryMachine<>(MekanismBlockTypes.COMBINER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.FACTORY_TYPE, FactoryType.COMBINING)
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.EXTRA_MACHINE)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                .addInput(MekanismRecipeType.COMBINING, DoubleItem::containsInputA)
                .addInput(MekanismRecipeType.COMBINING, DoubleItem::containsInputB)
                .addOutput()
                .addEnergy()
                .build()
          ));
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityCrusher, FactoryMachine<TileEntityCrusher>>, ItemBlockTooltip> CRUSHER =
          BLOCKS.register("crusher", properties -> new BlockFactoryMachine<>(MekanismBlockTypes.CRUSHER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.FACTORY_TYPE, FactoryType.CRUSHING)
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.ELECTRIC_MACHINE)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                .addInput(MekanismRecipeType.CRUSHING, SingleInputRecipeCache::containsInput)
                .addOutput()
                .addEnergy()
                .build()
          ));
    public static final BlockRegistryObject<BlockTileModel<TileEntityDigitalMiner, Machine<TileEntityDigitalMiner>>, ItemBlockTooltip> DIGITAL_MINER =
          BLOCKS.register("digital_miner", properties -> new BlockTileModel<>(MekanismBlockTypes.DIGITAL_MINER,
                BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())), (block, properties) -> new ItemBlockTooltip(block, properties
                .component(MekanismDataComponents.EJECT, false)
                .component(MekanismDataComponents.PULL, false)
                .component(MekanismDataComponents.SILK_TOUCH, false)
                .component(MekanismDataComponents.INVERSE, false)
                .component(MekanismDataComponents.INVERSE_REQUIRES_REPLACE, false)
                .component(MekanismDataComponents.RADIUS, TileEntityDigitalMiner.DEFAULT_RADIUS)
                .component(MekanismDataComponents.MIN_Y, 0)
                .component(MekanismDataComponents.MAX_Y, TileEntityDigitalMiner.DEFAULT_HEIGHT_RANGE)
                .component(MekanismDataComponents.REPLACE_STACK, Items.AIR)
                .component(MekanismDataComponents.OVERFLOW_AWARE, OverflowAware.EMPTY)
                .component(MekanismDataComponents.FILTER_AWARE, FilterAware.EMPTY)
                .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
          )).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                .addMinerSlots(3 * 9)
                .addEnergy()
                .build())
          );

    public static final BlockRegistryObject<BlockFactoryMachineModel<TileEntityMetallurgicInfuser, FactoryMachine<TileEntityMetallurgicInfuser>>, ItemBlockTooltip> METALLURGIC_INFUSER =
          BLOCKS.register("metallurgic_infuser", properties -> new BlockFactoryMachineModel<>(MekanismBlockTypes.METALLURGIC_INFUSER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.FACTORY_TYPE, FactoryType.INFUSING)
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.ADVANCED_MACHINE)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityMetallurgicInfuser.MAX_INFUSE, MekanismRecipeType.METALLURGIC_INFUSING, ItemChemical::containsInputB)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addChemicalFillOrConvertSlot(0)
                      .addInput(MekanismRecipeType.METALLURGIC_INFUSING, ItemChemical::containsInputA)
                      .addOutput()
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityPurificationChamber, FactoryMachine<TileEntityPurificationChamber>>, ItemBlockTooltip> PURIFICATION_CHAMBER =
          BLOCKS.register("purification_chamber", properties -> new BlockFactoryMachine<>(MekanismBlockTypes.PURIFICATION_CHAMBER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.FACTORY_TYPE, FactoryType.PURIFYING)
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.ADVANCED_MACHINE_INPUT_ONLY)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityAdvancedElectricMachine.MAX_GAS, MekanismRecipeType.PURIFYING, ItemChemical::containsInputB)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addInput(MekanismRecipeType.PURIFYING, ItemChemical::containsInputA)
                      .addChemicalFillOrConvertSlot(0)
                      .addOutput()
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityEnergizedSmelter, FactoryMachine<TileEntityEnergizedSmelter>>, ItemBlockTooltip> ENERGIZED_SMELTER =
          BLOCKS.register("energized_smelter", properties -> new BlockFactoryMachine<>(MekanismBlockTypes.ENERGIZED_SMELTER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.FACTORY_TYPE, FactoryType.SMELTING)
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.ELECTRIC_MACHINE)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                .addInput(MekanismRecipeType.SMELTING, SingleInputRecipeCache::containsInput)
                .addOutput()
                .addEnergy()
                .build()
          ));
    public static final BlockRegistryObject<BlockTile<TileEntityTeleporter, Machine<TileEntityTeleporter>>, ItemBlockTeleporter> TELEPORTER =
          BLOCKS.register("teleporter", properties -> new BlockTile<>(MekanismBlockTypes.TELEPORTER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTeleporter::new
          ).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));
    public static final BlockRegistryObject<BlockTileModel<TileEntityElectricPump, Machine<TileEntityElectricPump>>, ItemBlockTooltip> ELECTRIC_PUMP =
          BLOCKS.registerDetails("electric_pump", properties -> new BlockTileModel<>(MekanismBlockTypes.ELECTRIC_PUMP, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())))
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                            .addBasic(TileEntityElectricPump.MAX_FLUID)
                            .build()
                      ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addFluidDrainSlot(0)
                            .addOutput()
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockPersonalBarrel, ItemBlockPersonalStorage> PERSONAL_BARREL = BLOCKS.register("personal_barrel", BlockPersonalBarrel::new, (block, properties) -> new ItemBlockPersonalStorage(block, properties, Stats.OPEN_BARREL));
    public static final BlockRegistryObject<BlockPersonalChest, ItemBlockPersonalStorage> PERSONAL_CHEST = BLOCKS.register("personal_chest", BlockPersonalChest::new, (block, properties) -> new ItemBlockPersonalStorage(block, properties, Stats.OPEN_CHEST));
    public static final BlockRegistryObject<BlockChargepad, ItemBlockTooltip> CHARGEPAD = BLOCKS.registerDetails("chargepad", BlockChargepad::new);
    public static final BlockRegistryObject<BlockLogisticalSorter, ItemBlockTooltip> LOGISTICAL_SORTER = BLOCKS.register("logistical_sorter", BlockLogisticalSorter::new,
          (block, properties) -> new ItemBlockTooltip(block, properties
                .component(MekanismDataComponents.EJECT, false)
                .component(MekanismDataComponents.ROUND_ROBIN, false)
                .component(MekanismDataComponents.SINGLE_ITEM, false)
                .component(MekanismDataComponents.FILTER_AWARE, FilterAware.EMPTY)
                .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
          ));
    public static final BlockRegistryObject<BlockTileModel<TileEntityRotaryCondensentrator, Machine<TileEntityRotaryCondensentrator>>, ItemBlockTooltip> ROTARY_CONDENSENTRATOR =
          BLOCKS.register("rotary_condensentrator", properties -> new BlockTileModel<>(MekanismBlockTypes.ROTARY_CONDENSENTRATOR,
                BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())), (block, properties) -> new ItemBlockTooltip(block, properties
                .component(MekanismDataComponents.ROTARY_MODE, false)
                .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.ROTARY)
                .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
          )).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                      .addBasic(TileEntityRotaryCondensentrator.CAPACITY, MekanismRecipeType.ROTARY, RotaryInputRecipeCache::containsInputFluid)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityRotaryCondensentrator.CAPACITY, MekanismRecipeType.ROTARY, RotaryInputRecipeCache::containsInputChemical)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addChemicalRotarySlot(0)
                      .addOutput()
                      .addFluidRotarySlot(0)
                      .addOutput()
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalOxidizer, Machine<TileEntityChemicalOxidizer>>, ItemBlockTooltip> CHEMICAL_OXIDIZER =
          BLOCKS.register("chemical_oxidizer", properties -> new BlockTileModel<>(MekanismBlockTypes.CHEMICAL_OXIDIZER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.CHEMICAL_OUT_MACHINE)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityChemicalOxidizer.MAX_GAS)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addInput(MekanismRecipeType.OXIDIZING, SingleInputRecipeCache::containsInput)
                      .addChemicalDrainSlot(0)
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalInfuser, Machine<TileEntityChemicalInfuser>>, ItemBlockTooltip> CHEMICAL_INFUSER =
          BLOCKS.register("chemical_infuser", properties -> new BlockTileModel<>(MekanismBlockTypes.CHEMICAL_INFUSER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.CHEMICAL_INFUSING)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityChemicalInfuser.MAX_GAS, MekanismRecipeType.CHEMICAL_INFUSING, EitherSideChemical::containsInput)
                      .addBasic(TileEntityChemicalInfuser.MAX_GAS, MekanismRecipeType.CHEMICAL_INFUSING, EitherSideChemical::containsInput)
                      .addBasic(TileEntityChemicalInfuser.MAX_GAS)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addChemicalFillSlot(0)
                      .addChemicalFillSlot(1)
                      .addChemicalDrainSlot(2)
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityChemicalInjectionChamber, FactoryMachine<TileEntityChemicalInjectionChamber>>, ItemBlockTooltip> CHEMICAL_INJECTION_CHAMBER =
          BLOCKS.register("chemical_injection_chamber", properties -> new BlockFactoryMachine<>(MekanismBlockTypes.CHEMICAL_INJECTION_CHAMBER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.FACTORY_TYPE, FactoryType.INJECTING)
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.ADVANCED_MACHINE_INPUT_ONLY)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityAdvancedElectricMachine.MAX_GAS, MekanismRecipeType.INJECTING, ItemChemical::containsInputB)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addInput(MekanismRecipeType.INJECTING, ItemChemical::containsInputA)
                      .addChemicalFillOrConvertSlot(0)
                      .addOutput()
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockTileModel<TileEntityElectrolyticSeparator, Machine<TileEntityElectrolyticSeparator>>, ItemBlockTooltip> ELECTROLYTIC_SEPARATOR =
          BLOCKS.register("electrolytic_separator", properties -> new BlockTileModel<>(MekanismBlockTypes.ELECTROLYTIC_SEPARATOR, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.DUMP_MODE, GasMode.IDLE)
                      .component(MekanismDataComponents.SECONDARY_DUMP_MODE, GasMode.IDLE)
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.SEPARATOR)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                      .addBasic(TileEntityElectrolyticSeparator.MAX_FLUID, MekanismRecipeType.SEPARATING, SingleFluid::containsInput)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityElectrolyticSeparator.MAX_GAS)
                      .addBasic(TileEntityElectrolyticSeparator.MAX_GAS)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addFluidFillSlot(0)
                      .addChemicalDrainSlot(0)
                      .addChemicalDrainSlot(1)
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityPrecisionSawmill, FactoryMachine<TileEntityPrecisionSawmill>>, ItemBlockTooltip> PRECISION_SAWMILL =
          BLOCKS.register("precision_sawmill", properties -> new BlockFactoryMachine<>(MekanismBlockTypes.PRECISION_SAWMILL, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.FACTORY_TYPE, FactoryType.SAWING)
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.ELECTRIC_MACHINE)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                .addInput(MekanismRecipeType.SAWING, SingleInputRecipeCache::containsInput)
                .addOutput()
                .addOutput()//Secondary output
                .addEnergy()
                .build()
          ));
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalDissolutionChamber, Machine<TileEntityChemicalDissolutionChamber>>, ItemBlockTooltip> CHEMICAL_DISSOLUTION_CHAMBER =
          BLOCKS.register("chemical_dissolution_chamber", properties -> new BlockTileModel<>(MekanismBlockTypes.CHEMICAL_DISSOLUTION_CHAMBER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.DISSOLUTION)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                .addBasic(TileEntityChemicalDissolutionChamber.MAX_CHEMICAL, MekanismRecipeType.DISSOLUTION, ItemChemical::containsInputB)
                .addBasic(() -> TileEntityChemicalDissolutionChamber.MAX_CHEMICAL)
                .build()
          ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                .addChemicalFillOrConvertSlot(0)
                .addInput(MekanismRecipeType.DISSOLUTION, ItemChemical::containsInputA)
                .addChemicalDrainSlot(1)
                .addEnergy()
                .build()
          ));
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalWasher, Machine<TileEntityChemicalWasher>>, ItemBlockTooltip> CHEMICAL_WASHER =
          BLOCKS.register("chemical_washer", properties -> new BlockTileModel<>(MekanismBlockTypes.CHEMICAL_WASHER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.WASHER)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                      .addBasic(TileEntityChemicalWasher.MAX_FLUID, MekanismRecipeType.WASHING, FluidChemical::containsInputA)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityChemicalWasher.MAX_SLURRY, MekanismRecipeType.WASHING, FluidChemical::containsInputB)
                      .addBasic(TileEntityChemicalWasher.MAX_SLURRY)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addFluidFillSlot(0)
                      .addOutput()
                      .addChemicalDrainSlot(1)
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalCrystallizer, Machine<TileEntityChemicalCrystallizer>>, ItemBlockTooltip> CHEMICAL_CRYSTALLIZER =
          BLOCKS.register("chemical_crystallizer", properties -> new BlockTileModel<>(MekanismBlockTypes.CHEMICAL_CRYSTALLIZER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.CRYSTALLIZER)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                .addBasic(TileEntityChemicalCrystallizer.MAX_CHEMICAL, MekanismRecipeType.CRYSTALLIZING, SingleChemical::containsInput)
                .build()
          ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                .addChemicalFillSlot(0)
                .addOutput()
                .addEnergy()
                .build()
          ));
    public static final BlockRegistryObject<BlockTileModel<TileEntitySeismicVibrator, Machine<TileEntitySeismicVibrator>>, ItemBlockTooltip> SEISMIC_VIBRATOR =
          BLOCKS.registerDetails("seismic_vibrator", properties -> new BlockTileModel<>(MekanismBlockTypes.SEISMIC_VIBRATOR, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())))
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));
    public static final BlockRegistryObject<BlockTileModel<TileEntityPressurizedReactionChamber, Machine<TileEntityPressurizedReactionChamber>>, ItemBlockTooltip> PRESSURIZED_REACTION_CHAMBER =
          BLOCKS.register("pressurized_reaction_chamber", properties -> new BlockTileModel<>(MekanismBlockTypes.PRESSURIZED_REACTION_CHAMBER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.REACTION)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                      .addBasic(TileEntityPressurizedReactionChamber.MAX_FLUID, MekanismRecipeType.REACTION, ItemFluidChemical::containsInputB)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityPressurizedReactionChamber.MAX_GAS, MekanismRecipeType.REACTION, ItemFluidChemical::containsInputC)
                      .addBasic(TileEntityPressurizedReactionChamber.MAX_GAS)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addInput(MekanismRecipeType.REACTION, ItemFluidChemical::containsInputA)
                      .addOutput()
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockTileModel<TileEntityIsotopicCentrifuge, Machine<TileEntityIsotopicCentrifuge>>, ItemBlockTooltip> ISOTOPIC_CENTRIFUGE =
          BLOCKS.register("isotopic_centrifuge", properties -> new BlockTileModel<>(MekanismBlockTypes.ISOTOPIC_CENTRIFUGE, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.CENTRIFUGE)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityIsotopicCentrifuge.MAX_GAS, MekanismRecipeType.CENTRIFUGING, SingleChemical::containsInput)
                      .addBasic(TileEntityIsotopicCentrifuge.MAX_GAS)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addChemicalFillSlot(0)
                      .addChemicalDrainSlot(1)
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockTile<TileEntityNutritionalLiquifier, Machine<TileEntityNutritionalLiquifier>>, ItemBlockTooltip> NUTRITIONAL_LIQUIFIER =
          BLOCKS.register("nutritional_liquifier", properties -> new BlockTile<>(MekanismBlockTypes.NUTRITIONAL_LIQUIFIER, BlockTile.defaultProperties(properties).noOcclusion().mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.LIQUIFIER)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                      .addBasic(TileEntityNutritionalLiquifier.MAX_FLUID)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addInput(TileEntityNutritionalLiquifier::isValidInput)
                      .addOutput()
                      .addFluidDrainSlot(0)
                      .addOutput()
                      .addEnergy()
                      .build()
                )
          );

    public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> BASIC_FLUID_TANK = registerFluidTank(MekanismBlockTypes.BASIC_FLUID_TANK);
    public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> ADVANCED_FLUID_TANK = registerFluidTank(MekanismBlockTypes.ADVANCED_FLUID_TANK);
    public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> ELITE_FLUID_TANK = registerFluidTank(MekanismBlockTypes.ELITE_FLUID_TANK);
    public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> ULTIMATE_FLUID_TANK = registerFluidTank(MekanismBlockTypes.ULTIMATE_FLUID_TANK);
    public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> CREATIVE_FLUID_TANK = registerFluidTank(MekanismBlockTypes.CREATIVE_FLUID_TANK);

    public static final BlockRegistryObject<BlockTileModel<TileEntityFluidicPlenisher, Machine<TileEntityFluidicPlenisher>>, ItemBlockTooltip> FLUIDIC_PLENISHER =
          BLOCKS.registerDetails("fluidic_plenisher", properties -> new BlockTileModel<>(MekanismBlockTypes.FLUIDIC_PLENISHER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())))
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                            .addBasic(TileEntityFluidicPlenisher.MAX_FLUID)
                            .build()
                      ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addFluidFillSlot(0)
                            .addOutput()
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityLaser, BlockTypeTile<TileEntityLaser>>, ItemBlockTooltip> LASER =
          BLOCKS.registerDetails("laser", properties -> new BlockTileModel<>(MekanismBlockTypes.LASER,
                BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockTileModel<TileEntityLaserAmplifier, BlockTypeTile<TileEntityLaserAmplifier>>, ItemBlockLaserAmplifier> LASER_AMPLIFIER =
          BLOCKS.register("laser_amplifier", properties -> new BlockTileModel<>(MekanismBlockTypes.LASER_AMPLIFIER,
                BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_GRAY)), ItemBlockLaserAmplifier::new);
    public static final BlockRegistryObject<BlockTileModel<TileEntityLaserTractorBeam, BlockTypeTile<TileEntityLaserTractorBeam>>, ItemBlockLaserTractorBeam> LASER_TRACTOR_BEAM =
          BLOCKS.register("laser_tractor_beam", properties -> new BlockTileModel<>(MekanismBlockTypes.LASER_TRACTOR_BEAM,
                BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_GRAY)), ItemBlockLaserTractorBeam::new
          ).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addOutput(3 * 9).build()));
    public static final BlockRegistryObject<BlockTileModel<TileEntityQuantumEntangloporter, BlockTypeTile<TileEntityQuantumEntangloporter>>, ItemBlockQuantumEntangloporter> QUANTUM_ENTANGLOPORTER =
          BLOCKS.register("quantum_entangloporter", properties -> new BlockTileModel<>(MekanismBlockTypes.QUANTUM_ENTANGLOPORTER,
                BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockQuantumEntangloporter::new);
    public static final BlockRegistryObject<BlockTileModel<TileEntitySolarNeutronActivator, Machine<TileEntitySolarNeutronActivator>>, ItemBlockTooltip> SOLAR_NEUTRON_ACTIVATOR =
          BLOCKS.register("solar_neutron_activator", properties -> new BlockTileModel<>(MekanismBlockTypes.SOLAR_NEUTRON_ACTIVATOR, BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_BLUE)),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.SNA)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntitySolarNeutronActivator.MAX_GAS, MekanismRecipeType.ACTIVATING, SingleChemical::containsInput)
                      .addBasic(TileEntitySolarNeutronActivator.MAX_GAS)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addChemicalFillSlot(0)
                      .addChemicalDrainSlot(1)
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockTile<TileEntityOredictionificator, BlockTypeTile<TileEntityOredictionificator>>, ItemBlockTooltip> OREDICTIONIFICATOR =
          BLOCKS.register("oredictionificator", properties -> new BlockTile<>(MekanismBlockTypes.OREDICTIONIFICATOR, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.FILTER_AWARE, FilterAware.EMPTY)
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, new AttachedSideConfig(Map.of(TransmissionType.ITEM, LightConfigInfo.OUT_NO_EJECT)))
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                .addOredictionificatorInput()
                .addOutput()
                .build()
          ));
    public static final BlockRegistryObject<BlockTileModel<TileEntityResistiveHeater, Machine<TileEntityResistiveHeater>>, ItemBlockResistiveHeater> RESISTIVE_HEATER =
          BLOCKS.register("resistive_heater", properties -> new BlockTileModel<>(MekanismBlockTypes.RESISTIVE_HEATER,
                BlockTile.defaultProperties(properties).mapColor(MapColor.METAL)), ItemBlockResistiveHeater::new
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.HEAT, () -> HeatCapacitorBuilder.basicCreator(
                      TileEntityResistiveHeater.HEAT_CAPACITY, TileEntityResistiveHeater.INVERSE_CONDUCTION_COEFFICIENT, TileEntityResistiveHeater.INVERSE_INSULATION_COEFFICIENT
                )).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build())
          );
    public static final BlockRegistryObject<BlockTile<TileEntityFormulaicAssemblicator, Machine<TileEntityFormulaicAssemblicator>>, ItemBlockTooltip> FORMULAIC_ASSEMBLICATOR =
          BLOCKS.register("formulaic_assemblicator", properties -> new BlockTile<>(MekanismBlockTypes.FORMULAIC_ASSEMBLICATOR, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.EXTRA_MACHINE)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                .addFormulaSlot()
                .addInput(2 * 9)
                .addFormulaCraftingSlot(3 * 3)
                .addOutput(3 * 2)
                .addEnergy()
                .build())
          );
    public static final BlockRegistryObject<BlockTile<TileEntityFuelwoodHeater, BlockTypeTile<TileEntityFuelwoodHeater>>, ItemBlockTooltip> FUELWOOD_HEATER =
          BLOCKS.registerDetails("fuelwood_heater", properties -> new BlockTile<>(MekanismBlockTypes.FUELWOOD_HEATER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())))
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.HEAT, () -> HeatCapacitorBuilder.basicCreator(
                            TileEntityFuelwoodHeater.HEAT_CAPACITY, TileEntityFuelwoodHeater.INVERSE_CONDUCTION_COEFFICIENT, TileEntityFuelwoodHeater.INVERSE_INSULATION_COEFFICIENT
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addFuelSlot().build())
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityModificationStation, BlockTypeTile<TileEntityModificationStation>>, ItemBlockTooltip> MODIFICATION_STATION =
          BLOCKS.registerDetails("modification_station", properties -> new BlockTileModel<>(MekanismBlockTypes.MODIFICATION_STATION, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())))
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addInput(itemType -> itemType.has(IModuleHelper.INSTANCE.dataComponent()))
                      .addInput(IModuleHelper.INSTANCE::isModuleContainer)
                      .addEnergy()
                      .build()
                ));
    public static final BlockRegistryObject<BlockTileModel<TileEntityAntiprotonicNucleosynthesizer, Machine<TileEntityAntiprotonicNucleosynthesizer>>, ItemBlockTooltip> ANTIPROTONIC_NUCLEOSYNTHESIZER =
          BLOCKS.register("antiprotonic_nucleosynthesizer", properties -> new BlockTileModel<>(MekanismBlockTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER, BlockTile.defaultProperties(properties).mapColor(MapColor.METAL)),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.ADVANCED_MACHINE_INPUT_ONLY)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityAntiprotonicNucleosynthesizer.MAX_GAS, MekanismRecipeType.NUCLEOSYNTHESIZING, ItemChemical::containsInputB)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addChemicalFillOrConvertSlot(0)
                      .addInput(MekanismRecipeType.NUCLEOSYNTHESIZING, ItemChemical::containsInputA)
                      .addOutput()
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockTile<TileEntityPigmentExtractor, Machine<TileEntityPigmentExtractor>>, ItemBlockTooltip> PIGMENT_EXTRACTOR =
          BLOCKS.register("pigment_extractor", properties -> new BlockTile<>(MekanismBlockTypes.PIGMENT_EXTRACTOR, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.CHEMICAL_OUT_MACHINE)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityPigmentExtractor.MAX_PIGMENT)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addInput(MekanismRecipeType.PIGMENT_EXTRACTING, SingleInputRecipeCache::containsInput)
                      .addChemicalDrainSlot(0)
                      .addEnergy()
                      .build()
                )
          );
    //Note: Bottom of the mixer block has no model, so it uses the normal BlockTile instead of BlockTileModel
    public static final BlockRegistryObject<BlockTile<TileEntityPigmentMixer, Machine<TileEntityPigmentMixer>>, ItemBlockTooltip> PIGMENT_MIXER =
          BLOCKS.register("pigment_mixer", properties -> new BlockTile<>(MekanismBlockTypes.PIGMENT_MIXER, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.PIGMENT_MIXER)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityPigmentMixer.MAX_INPUT_PIGMENT, MekanismRecipeType.PIGMENT_MIXING, EitherSideChemical::containsInput)
                      .addBasic(TileEntityPigmentMixer.MAX_INPUT_PIGMENT, MekanismRecipeType.PIGMENT_MIXING, EitherSideChemical::containsInput)
                      .addBasic(TileEntityPigmentMixer.MAX_OUTPUT_PIGMENT)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addChemicalFillSlot(0)
                      .addChemicalFillSlot(1)
                      .addChemicalDrainSlot(2)
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockTile<TileEntityPaintingMachine, Machine<TileEntityPaintingMachine>>, ItemBlockTooltip> PAINTING_MACHINE =
          BLOCKS.register("painting_machine", properties -> new BlockTile<>(MekanismBlockTypes.PAINTING_MACHINE, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                      .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.PAINTING)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )
          ).forItemHolder(holder -> holder
                .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                      .addBasic(TileEntityPaintingMachine.MAX_PIGMENT, MekanismRecipeType.PAINTING, ItemChemical::containsInputB)
                      .build()
                ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addChemicalFillSlot(0)
                      //TODO - 1.20.4: add this comment to more methods
                      //Note: We don't bother with the insertion check based on what pigments are currently stored
                      .addInput(MekanismRecipeType.PAINTING, ItemChemical::containsInputA)
                      .addOutput()
                      .addEnergy()
                      .build()
                )
          );
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntitySPSCasing>, BlockItem> SPS_CASING = BLOCKS.registerSimpleItem("sps_casing",
          properties -> new BlockBasicMultiblock<>(MekanismBlockTypes.SPS_CASING, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.COLOR_LIGHT_GRAY)),
          properties -> properties.rarity(Rarity.EPIC));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntitySPSPort>, ItemBlockTooltip> SPS_PORT = BLOCKS.register("sps_port",
          properties -> new BlockBasicMultiblock<>(MekanismBlockTypes.SPS_PORT, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.COLOR_LIGHT_GRAY)),
          (block, props) -> new ItemBlockTooltip(block, props.rarity(Rarity.EPIC)));
    public static final BlockRegistryObject<BlockTileModel<TileEntitySuperchargedCoil, BlockTypeTile<TileEntitySuperchargedCoil>>, BlockItem> SUPERCHARGED_COIL = BLOCKS.registerSimpleItem("supercharged_coil",
          properties -> new BlockTileModel<>(MekanismBlockTypes.SUPERCHARGED_COIL, BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_ORANGE)),
          properties -> properties.rarity(Rarity.EPIC));
    public static final BlockRegistryObject<BlockTile<TileEntityDimensionalStabilizer, Machine<TileEntityDimensionalStabilizer>>, ItemBlockTooltip> DIMENSIONAL_STABILIZER =
          BLOCKS.register("dimensional_stabilizer", properties -> new BlockTile<>(MekanismBlockTypes.DIMENSIONAL_STABILIZER,
                      BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())),
                (block, properties) -> new ItemBlockTooltip(block, properties
                      .component(MekanismDataComponents.STABILIZER_CHUNKS, StabilizedChunks.NONE)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                )).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));

    public static final BlockRegistryObject<BlockQIOComponent<TileEntityQIODriveArray, BlockTypeTile<TileEntityQIODriveArray>>, ItemBlockQIOComponent> QIO_DRIVE_ARRAY =
          BLOCKS.register("qio_drive_array", properties -> new BlockQIOComponent<>(MekanismBlockTypes.QIO_DRIVE_ARRAY,
                BlockTile.defaultProperties(properties).mapColor(MapColor.METAL)), (block, properties) -> new ItemBlockQIOComponent(block, properties
                .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
          )).forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                .addQIODriveSlots(2 * 6)
                .build()
          ));
    public static final BlockRegistryObject<BlockQIOComponent<TileEntityQIODashboard, BlockTypeTile<TileEntityQIODashboard>>, ItemBlockQIOComponent> QIO_DASHBOARD =
          BLOCKS.register("qio_dashboard", properties -> new BlockQIOComponent<>(MekanismBlockTypes.QIO_DASHBOARD,
                      BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_GRAY)), (block, properties) -> new ItemBlockQIOComponent(block, properties
                      .component(MekanismDataComponents.INSERT_INTO_FREQUENCY, true)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                ))
                //Note: While the attachment is mainly used for the portable dashboard, it is a convenient way to also handle window construction
                // and setting up the proper predicates for the actual dashboard block
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addQIODashboardSlots().build()));
    public static final BlockRegistryObject<BlockQIOComponent<TileEntityQIOImporter, BlockTypeTile<TileEntityQIOImporter>>, ItemBlockQIOComponent> QIO_IMPORTER =
          BLOCKS.register("qio_importer", properties -> new BlockQIOComponent<>(MekanismBlockTypes.QIO_IMPORTER, BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_GRAY)),
                (block, properties) -> new ItemBlockQIOComponent(block, properties
                      .component(MekanismDataComponents.AUTO, false)
                      .component(MekanismDataComponents.FILTER_AWARE, FilterAware.EMPTY)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                ));
    public static final BlockRegistryObject<BlockQIOComponent<TileEntityQIOExporter, BlockTypeTile<TileEntityQIOExporter>>, ItemBlockQIOComponent> QIO_EXPORTER =
          BLOCKS.register("qio_exporter", properties -> new BlockQIOComponent<>(MekanismBlockTypes.QIO_EXPORTER, BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_GRAY)),
                (block, properties) -> new ItemBlockQIOComponent(block, properties
                      .component(MekanismDataComponents.AUTO, false)
                      .component(MekanismDataComponents.ROUND_ROBIN, false)
                      .component(MekanismDataComponents.FILTER_AWARE, FilterAware.EMPTY)
                      .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
                ));
    public static final BlockRegistryObject<BlockQIOComponent<TileEntityQIORedstoneAdapter, BlockTypeTile<TileEntityQIORedstoneAdapter>>, ItemBlockQIOComponent> QIO_REDSTONE_ADAPTER =
          BLOCKS.register("qio_redstone_adapter", properties -> new BlockQIOComponent<>(MekanismBlockTypes.QIO_REDSTONE_ADAPTER,
                BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_GRAY)), (block, properties) -> new ItemBlockQIOComponent(block, properties
                .component(MekanismDataComponents.FUZZY, false)
                .component(MekanismDataComponents.INVERSE, false)
                .component(MekanismDataComponents.LONG_AMOUNT, 0L)
                .component(MekanismDataComponents.ITEM_TARGET, ItemResource.EMPTY)
                .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
          ));

    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> BASIC_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.BASIC_ENERGY_CUBE);
    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> ADVANCED_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.ADVANCED_ENERGY_CUBE);
    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> ELITE_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.ELITE_ENERGY_CUBE);
    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> ULTIMATE_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.ULTIMATE_ENERGY_CUBE);
    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> CREATIVE_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.CREATIVE_ENERGY_CUBE);

    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityUniversalCable>, BlockItem> BASIC_UNIVERSAL_CABLE = registerUniversalCable(MekanismBlockTypes.BASIC_UNIVERSAL_CABLE);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityUniversalCable>, BlockItem> ADVANCED_UNIVERSAL_CABLE = registerUniversalCable(MekanismBlockTypes.ADVANCED_UNIVERSAL_CABLE);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityUniversalCable>, BlockItem> ELITE_UNIVERSAL_CABLE = registerUniversalCable(MekanismBlockTypes.ELITE_UNIVERSAL_CABLE);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityUniversalCable>, BlockItem> ULTIMATE_UNIVERSAL_CABLE = registerUniversalCable(MekanismBlockTypes.ULTIMATE_UNIVERSAL_CABLE);

    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityMechanicalPipe>, BlockItem> BASIC_MECHANICAL_PIPE = registerMechanicalPipe(MekanismBlockTypes.BASIC_MECHANICAL_PIPE);
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityMechanicalPipe>, BlockItem> ADVANCED_MECHANICAL_PIPE = registerMechanicalPipe(MekanismBlockTypes.ADVANCED_MECHANICAL_PIPE);
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityMechanicalPipe>, BlockItem> ELITE_MECHANICAL_PIPE = registerMechanicalPipe(MekanismBlockTypes.ELITE_MECHANICAL_PIPE);
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityMechanicalPipe>, BlockItem> ULTIMATE_MECHANICAL_PIPE = registerMechanicalPipe(MekanismBlockTypes.ULTIMATE_MECHANICAL_PIPE);

    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityPressurizedTube>, BlockItem> BASIC_PRESSURIZED_TUBE = registerPressurizedTube(MekanismBlockTypes.BASIC_PRESSURIZED_TUBE);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityPressurizedTube>, BlockItem> ADVANCED_PRESSURIZED_TUBE = registerPressurizedTube(MekanismBlockTypes.ADVANCED_PRESSURIZED_TUBE);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityPressurizedTube>, BlockItem> ELITE_PRESSURIZED_TUBE = registerPressurizedTube(MekanismBlockTypes.ELITE_PRESSURIZED_TUBE);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityPressurizedTube>, BlockItem> ULTIMATE_PRESSURIZED_TUBE = registerPressurizedTube(MekanismBlockTypes.ULTIMATE_PRESSURIZED_TUBE);

    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityLogisticalTransporter>, BlockItem> BASIC_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(MekanismBlockTypes.BASIC_LOGISTICAL_TRANSPORTER);
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityLogisticalTransporter>, BlockItem> ADVANCED_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(MekanismBlockTypes.ADVANCED_LOGISTICAL_TRANSPORTER);
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityLogisticalTransporter>, BlockItem> ELITE_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(MekanismBlockTypes.ELITE_LOGISTICAL_TRANSPORTER);
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityLogisticalTransporter>, BlockItem> ULTIMATE_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(MekanismBlockTypes.ULTIMATE_LOGISTICAL_TRANSPORTER);

    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityRestrictiveTransporter>, BlockItem> RESTRICTIVE_TRANSPORTER = BLOCKS.registerSimpleItem("restrictive_transporter",
          properties -> new BlockLargeTransmitter<>(MekanismBlockTypes.RESTRICTIVE_TRANSPORTER, properties, BlockResourceInfo.STEEL.getMapColor()),
          properties -> properties.component(MekanismDataComponents.SPECIALIZED_TRANSPORTER, new SpecializedTransporter(MekanismLang.DESCRIPTION_RESTRICTIVE))
    );
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityDiversionTransporter>, BlockItem> DIVERSION_TRANSPORTER = BLOCKS.registerSimpleItem("diversion_transporter",
          properties -> new BlockLargeTransmitter<>(MekanismBlockTypes.DIVERSION_TRANSPORTER, properties, MapColor.COLOR_ORANGE),
          properties -> properties.component(MekanismDataComponents.SPECIALIZED_TRANSPORTER, new SpecializedTransporter(MekanismLang.DESCRIPTION_DIVERSION))
    );

    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityThermodynamicConductor>, BlockItem> BASIC_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(MekanismBlockTypes.BASIC_THERMODYNAMIC_CONDUCTOR);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityThermodynamicConductor>, BlockItem> ADVANCED_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(MekanismBlockTypes.ADVANCED_THERMODYNAMIC_CONDUCTOR);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityThermodynamicConductor>, BlockItem> ELITE_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(MekanismBlockTypes.ELITE_THERMODYNAMIC_CONDUCTOR);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityThermodynamicConductor>, BlockItem> ULTIMATE_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(MekanismBlockTypes.ULTIMATE_THERMODYNAMIC_CONDUCTOR);

    public static final DeferredHolder<Block, BlockBounding> BOUNDING_BLOCK = BLOCKS.registerBlockOnly("bounding_block", BlockBounding::new);

    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> BASIC_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.BASIC_CHEMICAL_TANK);
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> ADVANCED_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.ADVANCED_CHEMICAL_TANK);
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> ELITE_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.ELITE_CHEMICAL_TANK);
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> ULTIMATE_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.ULTIMATE_CHEMICAL_TANK);
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> CREATIVE_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.CREATIVE_CHEMICAL_TANK);

    public static final BlockRegistryObject<BlockCardboardBox, ItemBlockCardboardBox> CARDBOARD_BOX = BLOCKS.register("cardboard_box", BlockCardboardBox::new, ItemBlockCardboardBox::new);
    public static final BlockRegistryObject<Block, BlockItem> SALT_BLOCK = BLOCKS.registerSimple("block_salt", properties -> properties.strength(0.5F).sound(SoundType.SAND).instrument(NoteBlockInstrument.SNARE));
    public static final BlockRegistryObject<Block, BlockItem> BIO_FUEL_BLOCK = BLOCKS.registerSimple("block_bio_fuel", properties -> properties.mapColor(MapColor.COLOR_BROWN).strength(0.5F).sound(SoundType.GRASS).instrument(NoteBlockInstrument.BANJO),
          properties -> properties.cookingFuel(MekanismContextIntProviders.COOKING_TIME_BIO_FUEL_BLOCK));

    private static BlockRegistryObject<BlockResource, BlockItem> registerResourceBlock(BlockResourceInfo resource) {
        return BLOCKS.register("block_" + resource.getRegistrySuffix(), properties -> new BlockResource(properties, resource), (block, properties) -> {
            if (!block.getResourceInfo().burnsInFire()) {
                properties = properties.fireResistant();
            }
            ResourceKey<ContextIntProvider> cookingTime = block.getResourceInfo().cookingTime();
            if (cookingTime != null) {
                properties = properties.cookingFuel(cookingTime);
            }
            return new BlockItem(block, properties);
        });
    }

    private static BlockRegistryObject<BlockBin, ItemBlockBin> registerBin(BlockTypeTile<TileEntityBin> type) {
        return registerTieredBlock(type, "_bin", (properties, color) -> new BlockBin(type, BlockTile.defaultProperties(properties).mapColor(color)), ItemBlockBin::new)
              .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                    .addSlot(ComponentBackedBinInventorySlot::create)
                    .build()
              ));
    }

    private static BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> registerInductionCell(BlockTypeTile<TileEntityInductionCell> type) {
        return registerTieredBlock(type, "_induction_cell", (properties, color) -> new BlockTile<>(type, BlockTile.defaultProperties(properties).mapColor(color)), ItemBlockInductionCell::new);
    }

    private static BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, BlockItem>
    registerInductionProvider(BlockTypeTile<TileEntityInductionProvider> type) {
        return registerTieredBlock(type, "_induction_provider", (properties, color) -> new BlockTile<>(type, BlockTile.defaultProperties(properties).mapColor(color)),
              (block, properties) -> new BlockItem(block, properties
                    .component(MekanismDataComponents.INDUCTION_PROVIDER_TIER, Attribute.getTierNN(block, InductionProviderTier.class))
              ));
    }

    private static BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> registerFluidTank(Machine<TileEntityFluidTank> type) {
        return registerTieredBlock(type, "_fluid_tank", properties -> new BlockFluidTank(type, properties), ItemBlockFluidTank::new)
              .forItemHolder(holder -> holder
                    .addAttachedContainerCapabilities(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                          .addContainer(ComponentBackedFluidTankFluidTank::create)
                          .build()
                    ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                          .addFluidInputSlot(0)
                          .addOutput()
                          .build()
                    )
              );
    }

    private static BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> registerEnergyCube(Machine<TileEntityEnergyCube> type) {
        return registerTieredBlock(type, "_energy_cube", properties -> new BlockEnergyCube(type, properties), ItemBlockEnergyCube::new)
              .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                    .addEnergy()
                    .addDrainEnergy()
                    .build()
              ));
    }

    private static BlockRegistryObject<BlockSmallTransmitter<TileEntityUniversalCable>, BlockItem> registerUniversalCable(
          BlockTypeTile<TileEntityUniversalCable> type) {
        return registerTieredBlock(type, "_universal_cable", properties -> new BlockSmallTransmitter<>(type, properties),
              (block, properties) -> new BlockItem(block, properties
                    .component(MekanismDataComponents.CABLE_TIER, Attribute.getTierNN(block, CableTier.class))
              ));
    }

    private static BlockRegistryObject<BlockLargeTransmitter<TileEntityMechanicalPipe>, BlockItem> registerMechanicalPipe(
          BlockTypeTile<TileEntityMechanicalPipe> type) {
        return registerTieredBlock(type, "_mechanical_pipe", properties -> new BlockLargeTransmitter<>(type, properties),
              (block, properties) -> new BlockItem(block, properties
                    .component(MekanismDataComponents.PIPE_TIER, Attribute.getTierNN(block, PipeTier.class))
              ));
    }

    private static BlockRegistryObject<BlockSmallTransmitter<TileEntityPressurizedTube>, BlockItem> registerPressurizedTube(
          BlockTypeTile<TileEntityPressurizedTube> type) {
        return registerTieredBlock(type, "_pressurized_tube", properties -> new BlockSmallTransmitter<>(type, properties),
              (block, properties) -> new BlockItem(block, properties
                    .component(MekanismDataComponents.TUBE_TIER, Attribute.getTierNN(block, TubeTier.class))
              ));
    }

    private static BlockRegistryObject<BlockLargeTransmitter<TileEntityLogisticalTransporter>, BlockItem> registerLogisticalTransporter(
          BlockTypeTile<TileEntityLogisticalTransporter> type) {
        return registerTieredBlock(type, "_logistical_transporter", properties -> new BlockLargeTransmitter<>(type, properties),
              (block, properties) -> new BlockItem(block, properties
                    .component(MekanismDataComponents.TRANSPORTER_TIER, Attribute.getTierNN(block, TransporterTier.class))
              ));
    }

    private static BlockRegistryObject<BlockSmallTransmitter<TileEntityThermodynamicConductor>, BlockItem> registerThermodynamicConductor(
          BlockTypeTile<TileEntityThermodynamicConductor> type) {
        return registerTieredBlock(type, "_thermodynamic_conductor", properties -> new BlockSmallTransmitter<>(type, properties),
              (block, properties) -> new BlockItem(block, properties
                    .component(MekanismDataComponents.CONDUCTOR_TIER, Attribute.getTierNN(block, ConductorTier.class))
              ));
    }

    private static BlockRegistryObject<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> registerChemicalTank(
          Machine<TileEntityChemicalTank> type) {
        return registerTieredBlock(type, "_chemical_tank", (properties, color) -> new BlockTileModel<>(type, BlockTile.defaultProperties(properties).mapColor(color)), ItemBlockChemicalTank::new)
              .forItemHolder(holder -> holder
                    .addAttachedContainerCapabilities(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                          .addContainer(ComponentBackedChemicalTankTank::create)
                          .build()
                    ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                          .addChemicalDrainSlot(0)
                          .addChemicalFillSlot(0)
                          .build()
                    )
              );
    }

    private static <TILE extends TileEntityFactory<?>> BlockRegistryObject<BlockFactory<?>, ItemBlockTooltip> registerFactory(Factory<TILE> type) {
        FactoryTier tier = (FactoryTier) type.getOrThrow(AttributeTier.class).tier();
        FactoryType factoryType = type.getOrThrow(AttributeFactoryType.class).getFactoryType();
        AttachedSideConfig sideConfig = switch (factoryType) {
            case SMELTING, ENRICHING, CRUSHING, SAWING -> AttachedSideConfig.ELECTRIC_MACHINE;
            case COMPRESSING, INFUSING -> AttachedSideConfig.ADVANCED_MACHINE;
            case COMBINING -> AttachedSideConfig.EXTRA_MACHINE;
            case PURIFYING, INJECTING -> AttachedSideConfig.ADVANCED_MACHINE_INPUT_ONLY;
        };
        BlockRegistryObject<BlockFactory<?>, ItemBlockTooltip> factory = registerTieredBlock(tier, "_" + factoryType.getSerializedName() + "_factory", properties -> new BlockFactory<>(type, properties),
              (block, properties) -> new ItemBlockTooltip(block, properties
                    .component(MekanismDataComponents.FACTORY_TIER, tier)
                    .component(MekanismDataComponents.FACTORY_TYPE, factoryType)
                    .component(MekanismDataComponents.SORTING, false)
                    .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                    .component(MekanismDataComponents.SIDE_CONFIG, sideConfig)
                    .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
              ));
        factory.forItemHolder(holder -> {
            int processes = tier.processes;
            Predicate<ItemResource> recipeInputPredicate = switch (factoryType) {
                case SMELTING -> itemType -> MekanismRecipeType.SMELTING.getInputCache().containsInput(null, itemType);
                case ENRICHING -> itemType -> MekanismRecipeType.ENRICHING.getInputCache().containsInput(null, itemType);
                case CRUSHING -> itemType -> MekanismRecipeType.CRUSHING.getInputCache().containsInput(null, itemType);
                case COMPRESSING -> itemType -> MekanismRecipeType.COMPRESSING.getInputCache().containsInputA(null, itemType);
                case COMBINING -> itemType -> MekanismRecipeType.COMBINING.getInputCache().containsInputA(null, itemType);
                case PURIFYING -> itemType -> MekanismRecipeType.PURIFYING.getInputCache().containsInputA(null, itemType);
                case INJECTING -> itemType -> MekanismRecipeType.INJECTING.getInputCache().containsInputA(null, itemType);
                case INFUSING -> itemType -> MekanismRecipeType.METALLURGIC_INFUSING.getInputCache().containsInputA(null, itemType);
                case SAWING -> itemType -> MekanismRecipeType.SAWING.getInputCache().containsInput(null, itemType);
            };
            switch (factoryType) {
                case SMELTING, ENRICHING, CRUSHING -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addBasicFactorySlots(processes, recipeInputPredicate)
                      .addEnergy()
                      .build()
                );
                case COMPRESSING, INJECTING, PURIFYING -> holder
                      .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                            .addBasic(TileEntityAdvancedElectricMachine.MAX_GAS * processes, switch (factoryType) {
                                case COMPRESSING -> MekanismRecipeType.COMPRESSING;
                                case INJECTING -> MekanismRecipeType.INJECTING;
                                case PURIFYING -> MekanismRecipeType.PURIFYING;
                                default -> throw new IllegalStateException("Factory type doesn't have a known gas recipe");
                            }, ItemChemical::containsInputB)
                            .build()
                      ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addBasicFactorySlots(processes, recipeInputPredicate)
                            .addChemicalFillOrConvertSlot(0)
                            .addEnergy()
                            .build()
                      );
                case COMBINING -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addBasicFactorySlots(processes, recipeInputPredicate)
                      .addInput(MekanismRecipeType.COMBINING, DoubleItem::containsInputB)
                      .addEnergy()
                      .build()
                );
                case INFUSING -> holder
                      .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                            .addBasic(TileEntityMetallurgicInfuser.MAX_INFUSE * processes, MekanismRecipeType.METALLURGIC_INFUSING, ItemChemical::containsInputB)
                            .build()
                      ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addBasicFactorySlots(processes, recipeInputPredicate)
                            .addChemicalFillOrConvertSlot(0)
                            .addEnergy()
                            .build()
                      );
                case SAWING -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addBasicFactorySlots(processes, recipeInputPredicate, true)
                      .addEnergy()
                      .build()
                );
            }

        });
        return factory;
    }

    private static <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> registerTieredBlock(BlockType type, String suffix,
          BiFunction<BlockBehaviour.Properties, MapColor, ? extends BLOCK> blockCreator, BiFunction<BLOCK, Item.Properties, ITEM> itemCreator) {
        ITier tier = type.getOrThrow(AttributeTier.class).tier();
        return registerTieredBlock(tier, suffix, properties -> blockCreator.apply(properties, tier.getBaseTier().getMapColor()), itemCreator);
    }

    private static <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> registerTieredBlock(BlockType type, String suffix,
          Function<BlockBehaviour.Properties, ? extends BLOCK> blockCreator, BiFunction<BLOCK, Item.Properties, ITEM> itemCreator) {
        return registerTieredBlock(type.getOrThrow(AttributeTier.class).tier(), suffix, blockCreator, itemCreator);
    }

    private static <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> registerTieredBlock(ITier tier, String suffix,
          Function<BlockBehaviour.Properties, ? extends BLOCK> blockSupplier, BiFunction<BLOCK, Item.Properties, ITEM> itemCreator) {
        return BLOCKS.register(tier.getBaseTier().getLowerName() + suffix, blockSupplier, itemCreator);
    }

    private static OreBlockType registerOre(OreType ore) {
        String name = ore.getResource().getRegistrySuffix() + "_ore";
        BlockRegistryObject<BlockOre, BlockItem> stoneOre = BLOCKS.register(name, properties -> new BlockOre(ore,
              BlockStateHelper.applyLightLevelAdjustments(properties)
                    .strength(3, 3)
                    .requiresCorrectToolForDrops()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)));
        BlockRegistryObject<BlockOre, BlockItem> deepslateOre = BLOCKS.register("deepslate_" + name, () -> Properties.ofLegacyCopy(stoneOre.value()),
              properties -> new BlockOre(ore, properties.mapColor(MapColor.DEEPSLATE)
                    .strength(4.5F, 3)
                    .sound(SoundType.DEEPSLATE)), BlockItem::new);
        return new OreBlockType(stoneOre, deepslateOre);
    }

    /// Retrieves a Factory with a defined tier and recipe type.
    ///
    /// @param tier tier to add to the Factory
    /// @param type recipe type to add to the Factory
    ///
    /// @return factory with defined tier and recipe type
    public static BlockRegistryObject<BlockFactory<?>, ItemBlockTooltip> getFactory(FactoryTier tier, FactoryType type) {
        return Objects.requireNonNull(FACTORIES.get(tier, type));
    }

    public static Collection<? extends BlockRegistryObject<BlockFactory<?>, ?>> getFactoryBlocks(FactoryType type) {
        return FACTORIES.column(type).values();
    }
}