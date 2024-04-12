package mekanism.common.registries;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.tier.ITier;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.BlockIndustrialAlarm;
import mekanism.common.block.BlockOre;
import mekanism.common.block.BlockPersonalBarrel;
import mekanism.common.block.BlockPersonalChest;
import mekanism.common.block.BlockQIOComponent;
import mekanism.common.block.BlockRadioactiveWasteBarrel;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.block.basic.BlockChargepad;
import mekanism.common.block.basic.BlockFluidTank;
import mekanism.common.block.basic.BlockLogisticalSorter;
import mekanism.common.block.basic.BlockResource;
import mekanism.common.block.basic.BlockStructuralGlass;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.prefab.BlockBase;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.block.prefab.BlockFactoryMachine;
import mekanism.common.block.prefab.BlockFactoryMachine.BlockFactory;
import mekanism.common.block.prefab.BlockFactoryMachine.BlockFactoryMachineModel;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.block.transmitter.BlockSmallTransmitter;
import mekanism.common.capabilities.chemical.variable.RateLimitGasTank;
import mekanism.common.capabilities.chemical.variable.RateLimitInfusionTank;
import mekanism.common.capabilities.chemical.variable.RateLimitPigmentTank;
import mekanism.common.capabilities.chemical.variable.RateLimitSlurryTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.FluidTankRateLimitFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.Factory;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.content.gear.IModuleItem;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.FormulaicCraftingSlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.InternalInventorySlot;
import mekanism.common.inventory.slot.ItemSlotsBuilder;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.inventory.slot.SecurityInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.inventory.slot.chemical.MergedChemicalInventorySlot;
import mekanism.common.inventory.slot.chemical.PigmentInventorySlot;
import mekanism.common.inventory.slot.chemical.SlurryInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.item.block.ItemBlockChemicalTank;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.item.block.ItemBlockInductionCell;
import mekanism.common.item.block.ItemBlockInductionProvider;
import mekanism.common.item.block.ItemBlockLaserAmplifier;
import mekanism.common.item.block.ItemBlockMekanism;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.item.block.ItemBlockRadioactiveWasteBarrel;
import mekanism.common.item.block.ItemBlockSecurityDesk;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.item.block.machine.ItemBlockLaserTractorBeam;
import mekanism.common.item.block.machine.ItemBlockQIOComponent;
import mekanism.common.item.block.machine.ItemBlockQuantumEntangloporter;
import mekanism.common.item.block.machine.ItemBlockResistiveHeater;
import mekanism.common.item.block.machine.ItemBlockTeleporter;
import mekanism.common.item.block.transmitter.ItemBlockLogisticalTransporter;
import mekanism.common.item.block.transmitter.ItemBlockMechanicalPipe;
import mekanism.common.item.block.transmitter.ItemBlockPressurizedTube;
import mekanism.common.item.block.transmitter.ItemBlockThermodynamicConductor;
import mekanism.common.item.block.transmitter.ItemBlockTransporter;
import mekanism.common.item.block.transmitter.ItemBlockUniversalCable;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.DoubleItem;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemFluidChemical;
import mekanism.common.recipe.lookup.cache.SingleInputRecipeCache;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.resource.IResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityChemicalTank;
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
import mekanism.common.util.EnumUtils;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class MekanismBlocks {

    private MekanismBlocks() {
    }

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(Mekanism.MODID);

    public static final Map<IResource, BlockRegistryObject<?, ?>> PROCESSED_RESOURCE_BLOCKS = new LinkedHashMap<>();
    public static final Map<OreType, OreBlockType> ORES = new LinkedHashMap<>();

    private static final Table<FactoryTier, FactoryType, BlockRegistryObject<BlockFactory<?>, ItemBlockFactory>> FACTORIES = HashBasedTable.create();

    static {
        // factories
        for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
            for (FactoryType type : EnumUtils.FACTORY_TYPES) {
                FACTORIES.put(tier, type, registerFactory(MekanismBlockTypes.getFactory(tier, type)));
            }
        }
        // resource blocks
        for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
            if (resource.getResourceBlockInfo() != null) {
                PROCESSED_RESOURCE_BLOCKS.put(resource, registerResourceBlock(resource.getResourceBlockInfo()));
            }
            BlockResourceInfo rawResource = resource.getRawResourceBlockInfo();
            if (rawResource != null) {
                PROCESSED_RESOURCE_BLOCKS.put(rawResource, registerResourceBlock(rawResource));
            }
        }
        // ores
        for (OreType ore : EnumUtils.ORE_TYPES) {
            ORES.put(ore, registerOre(ore));
        }
    }

    public static final BlockRegistryObject<BlockResource, ItemBlockMekanism<BlockResource>> BRONZE_BLOCK = registerResourceBlock(BlockResourceInfo.BRONZE);
    public static final BlockRegistryObject<BlockResource, ItemBlockMekanism<BlockResource>> REFINED_OBSIDIAN_BLOCK = registerResourceBlock(BlockResourceInfo.REFINED_OBSIDIAN);
    public static final BlockRegistryObject<BlockResource, ItemBlockMekanism<BlockResource>> CHARCOAL_BLOCK = registerResourceBlock(BlockResourceInfo.CHARCOAL);
    public static final BlockRegistryObject<BlockResource, ItemBlockMekanism<BlockResource>> REFINED_GLOWSTONE_BLOCK = registerResourceBlock(BlockResourceInfo.REFINED_GLOWSTONE);
    public static final BlockRegistryObject<BlockResource, ItemBlockMekanism<BlockResource>> STEEL_BLOCK = registerResourceBlock(BlockResourceInfo.STEEL);
    public static final BlockRegistryObject<BlockResource, ItemBlockMekanism<BlockResource>> FLUORITE_BLOCK = registerResourceBlock(BlockResourceInfo.FLUORITE);

    public static final BlockRegistryObject<BlockBin, ItemBlockBin> BASIC_BIN = registerBin(MekanismBlockTypes.BASIC_BIN);
    public static final BlockRegistryObject<BlockBin, ItemBlockBin> ADVANCED_BIN = registerBin(MekanismBlockTypes.ADVANCED_BIN);
    public static final BlockRegistryObject<BlockBin, ItemBlockBin> ELITE_BIN = registerBin(MekanismBlockTypes.ELITE_BIN);
    public static final BlockRegistryObject<BlockBin, ItemBlockBin> ULTIMATE_BIN = registerBin(MekanismBlockTypes.ULTIMATE_BIN);
    public static final BlockRegistryObject<BlockBin, ItemBlockBin> CREATIVE_BIN = registerBin(MekanismBlockTypes.CREATIVE_BIN);

    public static final BlockRegistryObject<BlockBase<BlockType>, ItemBlockTooltip<BlockBase<BlockType>>> TELEPORTER_FRAME = registerBlock("teleporter_frame", () -> new BlockBase<>(MekanismBlockTypes.TELEPORTER_FRAME, properties -> properties.strength(5, 6).mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockBase<BlockType>, ItemBlockTooltip<BlockBase<BlockType>>> STEEL_CASING = registerBlock("steel_casing", () -> new BlockBase<>(MekanismBlockTypes.STEEL_CASING, properties -> properties.strength(3.5F, 9).mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityDynamicTank>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityDynamicTank>>> DYNAMIC_TANK = registerBlock("dynamic_tank", () -> new BlockBasicMultiblock<>(MekanismBlockTypes.DYNAMIC_TANK, properties -> properties.mapColor(MapColor.COLOR_GRAY)));
    public static final BlockRegistryObject<BlockStructuralGlass<TileEntityStructuralGlass>, ItemBlockTooltip<BlockStructuralGlass<TileEntityStructuralGlass>>> STRUCTURAL_GLASS = registerBlock("structural_glass", () -> new BlockStructuralGlass<>(MekanismBlockTypes.STRUCTURAL_GLASS));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityDynamicValve>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityDynamicValve>>> DYNAMIC_VALVE = registerBlock("dynamic_valve", () -> new BlockBasicMultiblock<>(MekanismBlockTypes.DYNAMIC_VALVE, properties -> properties.mapColor(MapColor.COLOR_GRAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityThermalEvaporationController>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityThermalEvaporationController>>> THERMAL_EVAPORATION_CONTROLLER = registerBlock("thermal_evaporation_controller", () -> new BlockBasicMultiblock<>(MekanismBlockTypes.THERMAL_EVAPORATION_CONTROLLER, properties -> properties.mapColor(BlockResourceInfo.BRONZE.getMapColor())));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityThermalEvaporationValve>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityThermalEvaporationValve>>> THERMAL_EVAPORATION_VALVE = registerBlock("thermal_evaporation_valve", () -> new BlockBasicMultiblock<>(MekanismBlockTypes.THERMAL_EVAPORATION_VALVE, properties -> properties.mapColor(BlockResourceInfo.BRONZE.getMapColor())));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityThermalEvaporationBlock>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityThermalEvaporationBlock>>> THERMAL_EVAPORATION_BLOCK = registerBlock("thermal_evaporation_block", () -> new BlockBasicMultiblock<>(MekanismBlockTypes.THERMAL_EVAPORATION_BLOCK, properties -> properties.mapColor(BlockResourceInfo.BRONZE.getMapColor())));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityInductionCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityInductionCasing>>> INDUCTION_CASING = registerBlock("induction_casing", () -> new BlockBasicMultiblock<>(MekanismBlockTypes.INDUCTION_CASING, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityInductionPort>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityInductionPort>>> INDUCTION_PORT = registerBlock("induction_port", () -> new BlockBasicMultiblock<>(MekanismBlockTypes.INDUCTION_PORT, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY)));

    public static final BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> BASIC_INDUCTION_CELL = registerInductionCell(MekanismBlockTypes.BASIC_INDUCTION_CELL);
    public static final BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> ADVANCED_INDUCTION_CELL = registerInductionCell(MekanismBlockTypes.ADVANCED_INDUCTION_CELL);
    public static final BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> ELITE_INDUCTION_CELL = registerInductionCell(MekanismBlockTypes.ELITE_INDUCTION_CELL);
    public static final BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> ULTIMATE_INDUCTION_CELL = registerInductionCell(MekanismBlockTypes.ULTIMATE_INDUCTION_CELL);

    public static final BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, ItemBlockInductionProvider> BASIC_INDUCTION_PROVIDER = registerInductionProvider(MekanismBlockTypes.BASIC_INDUCTION_PROVIDER);
    public static final BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, ItemBlockInductionProvider> ADVANCED_INDUCTION_PROVIDER = registerInductionProvider(MekanismBlockTypes.ADVANCED_INDUCTION_PROVIDER);
    public static final BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, ItemBlockInductionProvider> ELITE_INDUCTION_PROVIDER = registerInductionProvider(MekanismBlockTypes.ELITE_INDUCTION_PROVIDER);
    public static final BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, ItemBlockInductionProvider> ULTIMATE_INDUCTION_PROVIDER = registerInductionProvider(MekanismBlockTypes.ULTIMATE_INDUCTION_PROVIDER);

    public static final BlockRegistryObject<BlockTile<TileEntitySuperheatingElement, BlockTypeTile<TileEntitySuperheatingElement>>, ItemBlockTooltip<BlockTile<TileEntitySuperheatingElement, BlockTypeTile<TileEntitySuperheatingElement>>>> SUPERHEATING_ELEMENT = registerBlock("superheating_element", () -> new BlockTile<>(MekanismBlockTypes.SUPERHEATING_ELEMENT, properties -> properties.mapColor(MapColor.COLOR_GRAY)));
    public static final BlockRegistryObject<BlockTile<TileEntityPressureDisperser, BlockTypeTile<TileEntityPressureDisperser>>, ItemBlockTooltip<BlockTile<TileEntityPressureDisperser, BlockTypeTile<TileEntityPressureDisperser>>>> PRESSURE_DISPERSER = registerBlock("pressure_disperser", () -> new BlockTile<>(MekanismBlockTypes.PRESSURE_DISPERSER, properties -> properties.mapColor(MapColor.DEEPSLATE)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityBoilerCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityBoilerCasing>>> BOILER_CASING = registerBlock("boiler_casing", () -> new BlockBasicMultiblock<>(MekanismBlockTypes.BOILER_CASING, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityBoilerValve>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityBoilerValve>>> BOILER_VALVE = registerBlock("boiler_valve", () -> new BlockBasicMultiblock<>(MekanismBlockTypes.BOILER_VALVE, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockTileModel<TileEntitySecurityDesk, BlockTypeTile<TileEntitySecurityDesk>>, ItemBlockSecurityDesk> SECURITY_DESK = BLOCKS.register("security_desk", () -> new BlockTileModel<>(MekanismBlockTypes.SECURITY_DESK, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockSecurityDesk::new)
          .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                .addSlot((listener, x, y) -> SecurityInventorySlot.unlock(() -> IItemSecurityUtils.INSTANCE.getOwnerUUID(stack), listener, x, y))
                .addSlot(SecurityInventorySlot::lock)
                .build()
          ));
    public static final BlockRegistryObject<BlockRadioactiveWasteBarrel, ItemBlockRadioactiveWasteBarrel> RADIOACTIVE_WASTE_BARREL = BLOCKS.registerDefaultProperties("radioactive_waste_barrel", BlockRadioactiveWasteBarrel::new, ItemBlockRadioactiveWasteBarrel::new);
    public static final BlockRegistryObject<BlockIndustrialAlarm, ItemBlockTooltip<BlockIndustrialAlarm>> INDUSTRIAL_ALARM = BLOCKS.register("industrial_alarm", BlockIndustrialAlarm::new, ItemBlockTooltip::new);

    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityEnrichmentChamber, FactoryMachine<TileEntityEnrichmentChamber>>, ItemBlockTooltip<BlockFactoryMachine<TileEntityEnrichmentChamber, FactoryMachine<TileEntityEnrichmentChamber>>>> ENRICHMENT_CHAMBER =
          BLOCKS.register("enrichment_chamber", () -> new BlockFactoryMachine<>(MekanismBlockTypes.ENRICHMENT_CHAMBER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                      .addInput(MekanismRecipeType.ENRICHING, SingleInputRecipeCache::containsInput)
                      .addOutput()
                      .addEnergy()
                      .build()
                ));
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityOsmiumCompressor, FactoryMachine<TileEntityOsmiumCompressor>>, ItemBlockTooltip<BlockFactoryMachine<TileEntityOsmiumCompressor, FactoryMachine<TileEntityOsmiumCompressor>>>> OSMIUM_COMPRESSOR =
          BLOCKS.register("osmium_compressor", () -> new BlockFactoryMachine<>(MekanismBlockTypes.OSMIUM_COMPRESSOR, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.GAS, stack -> RateLimitGasTank.createBasicItem(TileEntityAdvancedElectricMachine.MAX_GAS,
                            ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                            gas -> MekanismRecipeType.COMPRESSING.getInputCache().containsInputB(null, gas.getStack(1))
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addInput(MekanismRecipeType.COMPRESSING, ItemChemical::containsInputA)
                            .addGasSlotWithConversion(0)
                            .addOutput()
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityCombiner, FactoryMachine<TileEntityCombiner>>, ItemBlockTooltip<BlockFactoryMachine<TileEntityCombiner, FactoryMachine<TileEntityCombiner>>>> COMBINER =
          BLOCKS.register("combiner", () -> new BlockFactoryMachine<>(MekanismBlockTypes.COMBINER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                      .addInput(MekanismRecipeType.COMBINING, DoubleItem::containsInputA)
                      .addInput(MekanismRecipeType.COMBINING, DoubleItem::containsInputB)
                      .addOutput()
                      .addEnergy()
                      .build()
                ));
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityCrusher, FactoryMachine<TileEntityCrusher>>, ItemBlockTooltip<BlockFactoryMachine<TileEntityCrusher, FactoryMachine<TileEntityCrusher>>>> CRUSHER =
          BLOCKS.register("crusher", () -> new BlockFactoryMachine<>(MekanismBlockTypes.CRUSHER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                      .addInput(MekanismRecipeType.CRUSHING, SingleInputRecipeCache::containsInput)
                      .addOutput()
                      .addEnergy()
                      .build()
                ));
    public static final BlockRegistryObject<BlockTileModel<TileEntityDigitalMiner, Machine<TileEntityDigitalMiner>>, ItemBlockTooltip<BlockTileModel<TileEntityDigitalMiner, Machine<TileEntityDigitalMiner>>>> DIGITAL_MINER =
          BLOCKS.register("digital_miner", () -> new BlockTileModel<>(MekanismBlockTypes.DIGITAL_MINER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> {
                    BiPredicate<ItemStack, AutomationType> canInsert = (s, automationType) -> automationType != AutomationType.EXTERNAL || TileEntityDigitalMiner.isSavedReplaceTarget(stack, s.getItem());
                    //Allow extraction if it is manual or for internal usage, or if it is not a replace stack
                    //Note: We don't currently use internal for extraction anywhere here as we just shrink replace stacks directly
                    BiPredicate<ItemStack, AutomationType> canExtract = (s, automationType) -> automationType != AutomationType.EXTERNAL || !TileEntityDigitalMiner.isSavedReplaceTarget(stack, s.getItem());
                    return ItemSlotsBuilder.builder(stack)
                          .addSlots(3 * 9, (listener, x, y) -> BasicInventorySlot.at(canExtract, canInsert, listener, x, y))
                          .addEnergy()
                          .build();
                }));

    public static final BlockRegistryObject<BlockFactoryMachineModel<TileEntityMetallurgicInfuser, FactoryMachine<TileEntityMetallurgicInfuser>>, ItemBlockTooltip<BlockFactoryMachineModel<TileEntityMetallurgicInfuser, FactoryMachine<TileEntityMetallurgicInfuser>>>> METALLURGIC_INFUSER =
          BLOCKS.register("metallurgic_infuser", () -> new BlockFactoryMachineModel<>(MekanismBlockTypes.METALLURGIC_INFUSER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.INFUSION, stack -> RateLimitInfusionTank.createBasicItem(TileEntityMetallurgicInfuser.MAX_INFUSE,
                            ChemicalTankBuilder.INFUSION.manualOnly, ChemicalTankBuilder.INFUSION.alwaysTrueBi,
                            infuseType -> MekanismRecipeType.METALLURGIC_INFUSING.getInputCache().containsInputB(null, infuseType.getStack(1))
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addInfusionSlotWithConversion(0)
                            .addInput(MekanismRecipeType.METALLURGIC_INFUSING, ItemChemical::containsInputA)
                            .addOutput()
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityPurificationChamber, FactoryMachine<TileEntityPurificationChamber>>, ItemBlockTooltip<BlockFactoryMachine<TileEntityPurificationChamber, FactoryMachine<TileEntityPurificationChamber>>>> PURIFICATION_CHAMBER =
          BLOCKS.register("purification_chamber", () -> new BlockFactoryMachine<>(MekanismBlockTypes.PURIFICATION_CHAMBER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.GAS, stack -> RateLimitGasTank.createBasicItem(TileEntityAdvancedElectricMachine.MAX_GAS,
                            ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                            gas -> MekanismRecipeType.PURIFYING.getInputCache().containsInputB(null, gas.getStack(1))
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addInput(MekanismRecipeType.PURIFYING, ItemChemical::containsInputA)
                            .addGasSlotWithConversion(0)
                            .addOutput()
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityEnergizedSmelter, FactoryMachine<TileEntityEnergizedSmelter>>, ItemBlockTooltip<BlockFactoryMachine<TileEntityEnergizedSmelter, FactoryMachine<TileEntityEnergizedSmelter>>>> ENERGIZED_SMELTER =
          BLOCKS.register("energized_smelter", () -> new BlockFactoryMachine<>(MekanismBlockTypes.ENERGIZED_SMELTER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                      .addInput(MekanismRecipeType.SMELTING, SingleInputRecipeCache::containsInput)
                      .addOutput()
                      .addEnergy()
                      .build()
                ));
    public static final BlockRegistryObject<BlockTile<TileEntityTeleporter, Machine<TileEntityTeleporter>>, ItemBlockTeleporter> TELEPORTER = BLOCKS.register("teleporter", () -> new BlockTile<>(MekanismBlockTypes.TELEPORTER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTeleporter::new)
          .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack).addEnergy().build()));
    public static final BlockRegistryObject<BlockTileModel<TileEntityElectricPump, Machine<TileEntityElectricPump>>, ItemBlockTooltip<BlockTileModel<TileEntityElectricPump, Machine<TileEntityElectricPump>>>> ELECTRIC_PUMP =
          BLOCKS.register("electric_pump", () -> new BlockTileModel<>(MekanismBlockTypes.ELECTRIC_PUMP, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.FLUID, stack -> RateLimitFluidTank.createBasicItem(TileEntityElectricPump.MAX_FLUID,
                            BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addFluidSlot(0, FluidInventorySlot::drain)
                            .addOutput()
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockPersonalBarrel, ItemBlockPersonalStorage<BlockPersonalBarrel>> PERSONAL_BARREL = BLOCKS.register("personal_barrel", BlockPersonalBarrel::new, block -> new ItemBlockPersonalStorage<>(block, Stats.OPEN_BARREL));
    public static final BlockRegistryObject<BlockPersonalChest, ItemBlockPersonalStorage<BlockPersonalChest>> PERSONAL_CHEST = BLOCKS.register("personal_chest", BlockPersonalChest::new, block -> new ItemBlockPersonalStorage<>(block, Stats.OPEN_CHEST));
    public static final BlockRegistryObject<BlockChargepad, ItemBlockTooltip<BlockChargepad>> CHARGEPAD = BLOCKS.register("chargepad", BlockChargepad::new, ItemBlockTooltip::new);
    public static final BlockRegistryObject<BlockLogisticalSorter, ItemBlockTooltip<BlockLogisticalSorter>> LOGISTICAL_SORTER = BLOCKS.register("logistical_sorter", BlockLogisticalSorter::new, ItemBlockTooltip::new);
    public static final BlockRegistryObject<BlockTileModel<TileEntityRotaryCondensentrator, Machine<TileEntityRotaryCondensentrator>>, ItemBlockTooltip<BlockTileModel<TileEntityRotaryCondensentrator, Machine<TileEntityRotaryCondensentrator>>>> ROTARY_CONDENSENTRATOR =
          BLOCKS.register("rotary_condensentrator", () -> new BlockTileModel<>(MekanismBlockTypes.ROTARY_CONDENSENTRATOR, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.FLUID, stack -> RateLimitFluidTank.createBasicItem(TileEntityRotaryCondensentrator.CAPACITY,
                            BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi,
                            fluid -> MekanismRecipeType.ROTARY.getInputCache().containsInput(null, fluid)
                      )).addAttachmentOnlyContainer(ContainerType.GAS, stack -> RateLimitGasTank.createBasicItem(TileEntityRotaryCondensentrator.CAPACITY,
                            ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                            gas -> MekanismRecipeType.ROTARY.getInputCache().containsInput(null, gas.getStack(1))
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> {
                          BooleanSupplier modeSupplier = () -> stack.getData(MekanismAttachmentTypes.ROTARY_MODE);
                          return ItemSlotsBuilder.builder(stack)
                                .addGasSlot(0, (tank, listener, x, y) -> GasInventorySlot.rotaryDrain(tank, modeSupplier, listener, x, y))
                                .addGasSlot(0, (tank, listener, x, y) -> GasInventorySlot.rotaryDrain(tank, modeSupplier, listener, x, y))
                                .addFluidSlot(0, (tank, listener, x, y) -> FluidInventorySlot.rotary(tank, modeSupplier, listener, x, y))
                                .addOutput()
                                .addEnergy()
                                .build();
                      })
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalOxidizer, Machine<TileEntityChemicalOxidizer>>, ItemBlockTooltip<BlockTileModel<TileEntityChemicalOxidizer, Machine<TileEntityChemicalOxidizer>>>> CHEMICAL_OXIDIZER =
          BLOCKS.register("chemical_oxidizer", () -> new BlockTileModel<>(MekanismBlockTypes.CHEMICAL_OXIDIZER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.GAS, stack -> RateLimitGasTank.createBasicItem(TileEntityChemicalOxidizer.MAX_GAS,
                            ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addInput(MekanismRecipeType.OXIDIZING, SingleInputRecipeCache::containsInput)
                            .addGasSlot(0, GasInventorySlot::drain)
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalInfuser, Machine<TileEntityChemicalInfuser>>, ItemBlockTooltip<BlockTileModel<TileEntityChemicalInfuser, Machine<TileEntityChemicalInfuser>>>> CHEMICAL_INFUSER =
          BLOCKS.register("chemical_infuser", () -> new BlockTileModel<>(MekanismBlockTypes.CHEMICAL_INFUSER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.GAS, stack -> List.of(
                            RateLimitGasTank.createBasicItem(TileEntityChemicalInfuser.MAX_GAS,
                                  ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                                  gas -> MekanismRecipeType.CHEMICAL_INFUSING.getInputCache().containsInput(null, gas.getStack(1))
                            ),
                            RateLimitGasTank.createBasicItem(TileEntityChemicalInfuser.MAX_GAS,
                                  ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                                  gas -> MekanismRecipeType.CHEMICAL_INFUSING.getInputCache().containsInput(null, gas.getStack(1))
                            ),
                            RateLimitGasTank.createBasicItem(TileEntityChemicalInfuser.MAX_GAS,
                                  ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue
                            )
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addGasSlot(0, GasInventorySlot::fill)
                            .addGasSlot(1, GasInventorySlot::fill)
                            .addGasSlot(2, GasInventorySlot::drain)
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityChemicalInjectionChamber, FactoryMachine<TileEntityChemicalInjectionChamber>>, ItemBlockTooltip<BlockFactoryMachine<TileEntityChemicalInjectionChamber, FactoryMachine<TileEntityChemicalInjectionChamber>>>> CHEMICAL_INJECTION_CHAMBER =
          BLOCKS.register("chemical_injection_chamber", () -> new BlockFactoryMachine<>(MekanismBlockTypes.CHEMICAL_INJECTION_CHAMBER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.GAS, stack -> RateLimitGasTank.createBasicItem(TileEntityAdvancedElectricMachine.MAX_GAS,
                            ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                            gas -> MekanismRecipeType.INJECTING.getInputCache().containsInputB(null, gas.getStack(1))
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addInput(MekanismRecipeType.INJECTING, ItemChemical::containsInputA)
                            .addGasSlotWithConversion(0)
                            .addOutput()
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityElectrolyticSeparator, Machine<TileEntityElectrolyticSeparator>>, ItemBlockTooltip<BlockTileModel<TileEntityElectrolyticSeparator, Machine<TileEntityElectrolyticSeparator>>>> ELECTROLYTIC_SEPARATOR =
          BLOCKS.register("electrolytic_separator", () -> new BlockTileModel<>(MekanismBlockTypes.ELECTROLYTIC_SEPARATOR, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.FLUID, stack -> RateLimitFluidTank.createBasicItem(TileEntityElectrolyticSeparator.MAX_FLUID,
                            BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi,
                            fluid -> MekanismRecipeType.SEPARATING.getInputCache().containsInput(null, fluid)
                      )).addAttachmentOnlyContainers(ContainerType.GAS, stack -> List.of(
                            RateLimitGasTank.createBasicItem(TileEntityElectrolyticSeparator.MAX_GAS,
                                  ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue
                            ),
                            RateLimitGasTank.createBasicItem(TileEntityElectrolyticSeparator.MAX_GAS,
                                  ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue
                            )
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addFluidSlot(0, FluidInventorySlot::fill)
                            .addGasSlot(0, GasInventorySlot::drain)
                            .addGasSlot(1, GasInventorySlot::drain)
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityPrecisionSawmill, FactoryMachine<TileEntityPrecisionSawmill>>, ItemBlockTooltip<BlockFactoryMachine<TileEntityPrecisionSawmill, FactoryMachine<TileEntityPrecisionSawmill>>>> PRECISION_SAWMILL =
          BLOCKS.register("precision_sawmill", () -> new BlockFactoryMachine<>(MekanismBlockTypes.PRECISION_SAWMILL, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                      .addInput(MekanismRecipeType.SAWING, SingleInputRecipeCache::containsInput)
                      .addOutput()
                      .addOutput()//Secondary output
                      .addEnergy()
                      .build()
                ));
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalDissolutionChamber, Machine<TileEntityChemicalDissolutionChamber>>, ItemBlockTooltip<BlockTileModel<TileEntityChemicalDissolutionChamber, Machine<TileEntityChemicalDissolutionChamber>>>> CHEMICAL_DISSOLUTION_CHAMBER =
          BLOCKS.register("chemical_dissolution_chamber", () -> new BlockTileModel<>(MekanismBlockTypes.CHEMICAL_DISSOLUTION_CHAMBER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.GAS, stack -> List.of(
                            RateLimitGasTank.createBasicItem(TileEntityChemicalDissolutionChamber.MAX_CHEMICAL,
                                  ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                                  gas -> MekanismRecipeType.DISSOLUTION.getInputCache().containsInputB(null, gas.getStack(1))
                            ),
                            stack.getData(MekanismAttachmentTypes.CDC_CONTENTS_HANDLER).getGasTank()
                      )).addMissingMergedTanks(MekanismAttachmentTypes.CDC_CONTENTS_HANDLER, false, false)
                      .addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addGasSlotWithConversion(0)
                            .addInput(MekanismRecipeType.DISSOLUTION, ItemChemical::containsInputA)
                            .addContainerSlot(stack.getData(MekanismAttachmentTypes.CDC_CONTENTS_HANDLER), MergedChemicalInventorySlot::drain)
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalWasher, Machine<TileEntityChemicalWasher>>, ItemBlockTooltip<BlockTileModel<TileEntityChemicalWasher, Machine<TileEntityChemicalWasher>>>> CHEMICAL_WASHER =
          BLOCKS.register("chemical_washer", () -> new BlockTileModel<>(MekanismBlockTypes.CHEMICAL_WASHER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.FLUID, stack -> RateLimitFluidTank.createBasicItem(TileEntityChemicalWasher.MAX_FLUID,
                            BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi,
                            fluid -> MekanismRecipeType.WASHING.getInputCache().containsInputA(null, fluid)
                      )).addAttachmentOnlyContainers(ContainerType.SLURRY, stack -> List.of(
                            RateLimitSlurryTank.createBasicItem(TileEntityChemicalWasher.MAX_SLURRY,
                                  ChemicalTankBuilder.SLURRY.manualOnly, ChemicalTankBuilder.SLURRY.alwaysTrueBi,
                                  slurry -> MekanismRecipeType.WASHING.getInputCache().containsInputB(null, slurry.getStack(1))
                            ),
                            RateLimitSlurryTank.createBasicItem(TileEntityChemicalWasher.MAX_SLURRY,
                                  ChemicalTankBuilder.SLURRY.manualOnly, ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrue
                            )
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addFluidSlot(0, FluidInventorySlot::fill)
                            .addOutput()
                            .addSlurrySlot(1, SlurryInventorySlot::drain)
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalCrystallizer, Machine<TileEntityChemicalCrystallizer>>, ItemBlockTooltip<BlockTileModel<TileEntityChemicalCrystallizer, Machine<TileEntityChemicalCrystallizer>>>> CHEMICAL_CRYSTALLIZER =
          BLOCKS.register("chemical_crystallizer", () -> new BlockTileModel<>(MekanismBlockTypes.CHEMICAL_CRYSTALLIZER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addMissingMergedTanks(MekanismAttachmentTypes.CRYSTALLIZER_CONTENTS_HANDLER, false, false)
                      .addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addContainerSlot(stack.getData(MekanismAttachmentTypes.CRYSTALLIZER_CONTENTS_HANDLER), MergedChemicalInventorySlot::fill)
                            .addOutput()
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntitySeismicVibrator, Machine<TileEntitySeismicVibrator>>, ItemBlockTooltip<BlockTileModel<TileEntitySeismicVibrator, Machine<TileEntitySeismicVibrator>>>> SEISMIC_VIBRATOR =
          BLOCKS.register("seismic_vibrator", () -> new BlockTileModel<>(MekanismBlockTypes.SEISMIC_VIBRATOR, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack).addEnergy().build()));
    public static final BlockRegistryObject<BlockTileModel<TileEntityPressurizedReactionChamber, Machine<TileEntityPressurizedReactionChamber>>, ItemBlockTooltip<BlockTileModel<TileEntityPressurizedReactionChamber, Machine<TileEntityPressurizedReactionChamber>>>> PRESSURIZED_REACTION_CHAMBER =
          BLOCKS.register("pressurized_reaction_chamber", () -> new BlockTileModel<>(MekanismBlockTypes.PRESSURIZED_REACTION_CHAMBER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.FLUID, stack -> RateLimitFluidTank.createBasicItem(TileEntityPressurizedReactionChamber.MAX_FLUID,
                            BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi,
                            fluid -> MekanismRecipeType.REACTION.getInputCache().containsInputB(null, fluid)
                      )).addAttachmentOnlyContainers(ContainerType.GAS, stack -> List.of(
                            RateLimitGasTank.createBasicItem(TileEntityPressurizedReactionChamber.MAX_GAS,
                                  ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                                  gas -> MekanismRecipeType.REACTION.getInputCache().containsInputC(null, gas.getStack(1))
                            ),
                            RateLimitGasTank.createBasicItem(TileEntityPressurizedReactionChamber.MAX_GAS,
                                  ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue
                            )
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addInput(MekanismRecipeType.REACTION, ItemFluidChemical::containsInputA)
                            .addOutput()
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityIsotopicCentrifuge, Machine<TileEntityIsotopicCentrifuge>>, ItemBlockTooltip<BlockTileModel<TileEntityIsotopicCentrifuge, Machine<TileEntityIsotopicCentrifuge>>>> ISOTOPIC_CENTRIFUGE =
          BLOCKS.register("isotopic_centrifuge", () -> new BlockTileModel<>(MekanismBlockTypes.ISOTOPIC_CENTRIFUGE, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.GAS, stack -> List.of(
                            RateLimitGasTank.createBasicItem(TileEntityIsotopicCentrifuge.MAX_GAS,
                                  ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                                  gas -> MekanismRecipeType.CENTRIFUGING.getInputCache().containsInput(null, gas.getStack(1))
                            ),
                            RateLimitGasTank.createBasicItem(TileEntityIsotopicCentrifuge.MAX_GAS,
                                  ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue
                            )
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addGasSlot(0, GasInventorySlot::fill)
                            .addGasSlot(1, GasInventorySlot::drain)
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTile<TileEntityNutritionalLiquifier, Machine<TileEntityNutritionalLiquifier>>, ItemBlockTooltip<BlockTile<TileEntityNutritionalLiquifier, Machine<TileEntityNutritionalLiquifier>>>> NUTRITIONAL_LIQUIFIER =
          BLOCKS.register("nutritional_liquifier", () -> new BlockTile<>(MekanismBlockTypes.NUTRITIONAL_LIQUIFIER, properties -> properties.noOcclusion().mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.FLUID, stack -> RateLimitFluidTank.createBasicItem(TileEntityNutritionalLiquifier.MAX_FLUID,
                            BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addInput(TileEntityNutritionalLiquifier::isValidInput)
                            .addFluidSlot(0, FluidInventorySlot::drain)
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

    public static final BlockRegistryObject<BlockTileModel<TileEntityFluidicPlenisher, Machine<TileEntityFluidicPlenisher>>, ItemBlockTooltip<BlockTileModel<TileEntityFluidicPlenisher, Machine<TileEntityFluidicPlenisher>>>> FLUIDIC_PLENISHER =
          BLOCKS.register("fluidic_plenisher", () -> new BlockTileModel<>(MekanismBlockTypes.FLUIDIC_PLENISHER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.FLUID, stack -> RateLimitFluidTank.createBasicItem(TileEntityFluidicPlenisher.MAX_FLUID,
                            BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addFluidSlot(0, FluidInventorySlot::fill)
                            .addOutput()
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityLaser, BlockTypeTile<TileEntityLaser>>, ItemBlockTooltip<BlockTileModel<TileEntityLaser, BlockTypeTile<TileEntityLaser>>>> LASER = BLOCKS.register("laser", () -> new BlockTileModel<>(MekanismBlockTypes.LASER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new);
    public static final BlockRegistryObject<BlockTileModel<TileEntityLaserAmplifier, BlockTypeTile<TileEntityLaserAmplifier>>, ItemBlockLaserAmplifier> LASER_AMPLIFIER = BLOCKS.register("laser_amplifier", () -> new BlockTileModel<>(MekanismBlockTypes.LASER_AMPLIFIER, properties -> properties.mapColor(MapColor.COLOR_GRAY)), ItemBlockLaserAmplifier::new);
    public static final BlockRegistryObject<BlockTileModel<TileEntityLaserTractorBeam, BlockTypeTile<TileEntityLaserTractorBeam>>, ItemBlockLaserTractorBeam> LASER_TRACTOR_BEAM = BLOCKS.register("laser_tractor_beam", () -> new BlockTileModel<>(MekanismBlockTypes.LASER_TRACTOR_BEAM, properties -> properties.mapColor(MapColor.COLOR_GRAY)), ItemBlockLaserTractorBeam::new)
          .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack).addSlots(3 * 9, OutputInventorySlot::at).build()));
    public static final BlockRegistryObject<BlockTileModel<TileEntityQuantumEntangloporter, BlockTypeTile<TileEntityQuantumEntangloporter>>, ItemBlockQuantumEntangloporter> QUANTUM_ENTANGLOPORTER = BLOCKS.register("quantum_entangloporter", () -> new BlockTileModel<>(MekanismBlockTypes.QUANTUM_ENTANGLOPORTER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockQuantumEntangloporter::new);
    public static final BlockRegistryObject<BlockTileModel<TileEntitySolarNeutronActivator, Machine<TileEntitySolarNeutronActivator>>, ItemBlockTooltip<BlockTileModel<TileEntitySolarNeutronActivator, Machine<TileEntitySolarNeutronActivator>>>> SOLAR_NEUTRON_ACTIVATOR =
          BLOCKS.register("solar_neutron_activator", () -> new BlockTileModel<>(MekanismBlockTypes.SOLAR_NEUTRON_ACTIVATOR, properties -> properties.mapColor(MapColor.COLOR_BLUE)), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.GAS, stack -> List.of(
                            RateLimitGasTank.createBasicItem(TileEntitySolarNeutronActivator.MAX_GAS,
                                  ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                                  gas -> MekanismRecipeType.ACTIVATING.getInputCache().containsInput(null, gas.getStack(1))
                            ),
                            RateLimitGasTank.createBasicItem(TileEntitySolarNeutronActivator.MAX_GAS,
                                  ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue
                            )
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addGasSlot(0, GasInventorySlot::fill)
                            .addGasSlot(1, GasInventorySlot::drain)
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTile<TileEntityOredictionificator, BlockTypeTile<TileEntityOredictionificator>>, ItemBlockTooltip<BlockTile<TileEntityOredictionificator, BlockTypeTile<TileEntityOredictionificator>>>> OREDICTIONIFICATOR =
          BLOCKS.register("oredictionificator", () -> new BlockTile<>(MekanismBlockTypes.OREDICTIONIFICATOR, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                      .addInput(s -> TileEntityOredictionificator.hasResult(stack.getData(MekanismAttachmentTypes.FILTER_AWARE).getEnabled(OredictionificatorItemFilter.class), s))
                      .addOutput()
                      .build()
                ));
    public static final BlockRegistryObject<BlockTileModel<TileEntityResistiveHeater, Machine<TileEntityResistiveHeater>>, ItemBlockResistiveHeater> RESISTIVE_HEATER = BLOCKS.register("resistive_heater", () -> new BlockTileModel<>(MekanismBlockTypes.RESISTIVE_HEATER, properties -> properties.mapColor(MapColor.METAL)), ItemBlockResistiveHeater::new)
          .forItemHolder(holder -> holder
                .addAttachmentOnlyContainer(ContainerType.HEAT, stack -> BasicHeatCapacitor.createBasicItem(TileEntityResistiveHeater.HEAT_CAPACITY,
                      TileEntityResistiveHeater.INVERSE_CONDUCTION_COEFFICIENT, TileEntityResistiveHeater.INVERSE_INSULATION_COEFFICIENT
                )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack).addEnergy().build())
          );
    public static final BlockRegistryObject<BlockTile<TileEntityFormulaicAssemblicator, Machine<TileEntityFormulaicAssemblicator>>, ItemBlockTooltip<BlockTile<TileEntityFormulaicAssemblicator, Machine<TileEntityFormulaicAssemblicator>>>> FORMULAIC_ASSEMBLICATOR =
          BLOCKS.register("formulaic_assemblicator", () -> new BlockTile<>(MekanismBlockTypes.FORMULAIC_ASSEMBLICATOR, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> {
                    BooleanSupplier autoMode = () -> stack.getData(MekanismAttachmentTypes.AUTO);
                    return ItemSlotsBuilder.builder(stack)
                          .addSlot((listener, x, y) -> BasicInventorySlot.at(TileEntityFormulaicAssemblicator.FORMULA_SLOT_VALIDATOR, listener, x, y))
                          //Note: We skip making the extra checks based on the formula and just allow all items
                          .addSlots(2 * 9, InputInventorySlot::at)
                          .addSlots(3 * 3, (listener, x, y) -> FormulaicCraftingSlot.at(autoMode, listener, x, y))
                          .addSlots(3 * 2, OutputInventorySlot::at)
                          .addEnergy()
                          .build();
                }));
    public static final BlockRegistryObject<BlockTile<TileEntityFuelwoodHeater, BlockTypeTile<TileEntityFuelwoodHeater>>, ItemBlockTooltip<BlockTile<TileEntityFuelwoodHeater, BlockTypeTile<TileEntityFuelwoodHeater>>>> FUELWOOD_HEATER =
          BLOCKS.register("fuelwood_heater", () -> new BlockTile<>(MekanismBlockTypes.FUELWOOD_HEATER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.HEAT, stack -> BasicHeatCapacitor.createBasicItem(TileEntityFuelwoodHeater.HEAT_CAPACITY,
                            TileEntityFuelwoodHeater.INVERSE_CONDUCTION_COEFFICIENT, TileEntityFuelwoodHeater.INVERSE_INSULATION_COEFFICIENT
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack).addFuelSlot().build())
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityModificationStation, BlockTypeTile<TileEntityModificationStation>>, ItemBlockTooltip<BlockTileModel<TileEntityModificationStation, BlockTypeTile<TileEntityModificationStation>>>> MODIFICATION_STATION =
          BLOCKS.register("modification_station", () -> new BlockTileModel<>(MekanismBlockTypes.MODIFICATION_STATION, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                      .addInput(s -> s.getItem() instanceof IModuleItem)
                      .addInput(IModuleHelper.INSTANCE::isModuleContainer)
                      .addEnergy()
                      .build()
                ));
    public static final BlockRegistryObject<BlockTileModel<TileEntityAntiprotonicNucleosynthesizer, Machine<TileEntityAntiprotonicNucleosynthesizer>>, ItemBlockTooltip<BlockTileModel<TileEntityAntiprotonicNucleosynthesizer, Machine<TileEntityAntiprotonicNucleosynthesizer>>>> ANTIPROTONIC_NUCLEOSYNTHESIZER =
          BLOCKS.register("antiprotonic_nucleosynthesizer", () -> new BlockTileModel<>(MekanismBlockTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER, properties -> properties.mapColor(MapColor.METAL)), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.GAS, stack -> RateLimitGasTank.createBasicItem(TileEntityAntiprotonicNucleosynthesizer.MAX_GAS,
                            ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                            gas -> MekanismRecipeType.NUCLEOSYNTHESIZING.getInputCache().containsInputB(null, gas.getStack(1))
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addGasSlotWithConversion(0)
                            .addInput(MekanismRecipeType.NUCLEOSYNTHESIZING, ItemChemical::containsInputA)
                            .addOutput()
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTile<TileEntityPigmentExtractor, Machine<TileEntityPigmentExtractor>>, ItemBlockTooltip<BlockTile<TileEntityPigmentExtractor, Machine<TileEntityPigmentExtractor>>>> PIGMENT_EXTRACTOR =
          BLOCKS.register("pigment_extractor", () -> new BlockTile<>(MekanismBlockTypes.PIGMENT_EXTRACTOR, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.PIGMENT, stack -> RateLimitPigmentTank.createBasicItem(TileEntityPigmentExtractor.MAX_PIGMENT,
                            ChemicalTankBuilder.PIGMENT.manualOnly, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrue
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addInput(MekanismRecipeType.PIGMENT_EXTRACTING, SingleInputRecipeCache::containsInput)
                            .addPigmentSlot(0, PigmentInventorySlot::drain)
                            .addEnergy()
                            .build()
                      )
                );
    //Note: Bottom of the mixer block has no model, so it uses the normal BlockTile instead of BlockTileModel
    public static final BlockRegistryObject<BlockTile<TileEntityPigmentMixer, Machine<TileEntityPigmentMixer>>, ItemBlockTooltip<BlockTile<TileEntityPigmentMixer, Machine<TileEntityPigmentMixer>>>> PIGMENT_MIXER =
          BLOCKS.register("pigment_mixer", () -> new BlockTile<>(MekanismBlockTypes.PIGMENT_MIXER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.PIGMENT, stack -> List.of(
                            RateLimitPigmentTank.createBasicItem(TileEntityPigmentMixer.MAX_INPUT_PIGMENT,
                                  ChemicalTankBuilder.PIGMENT.manualOnly, ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
                                  pigment -> MekanismRecipeType.PIGMENT_MIXING.getInputCache().containsInput(null, pigment.getStack(1))
                            ),
                            RateLimitPigmentTank.createBasicItem(TileEntityPigmentMixer.MAX_INPUT_PIGMENT,
                                  ChemicalTankBuilder.PIGMENT.manualOnly, ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
                                  pigment -> MekanismRecipeType.PIGMENT_MIXING.getInputCache().containsInput(null, pigment.getStack(1))
                            ),
                            RateLimitPigmentTank.createBasicItem(TileEntityPigmentMixer.MAX_OUTPUT_PIGMENT,
                                  ChemicalTankBuilder.PIGMENT.manualOnly, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrue
                            )
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addPigmentSlot(0, PigmentInventorySlot::fill)
                            .addPigmentSlot(1, PigmentInventorySlot::fill)
                            .addPigmentSlot(2, PigmentInventorySlot::drain)
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTile<TileEntityPaintingMachine, Machine<TileEntityPaintingMachine>>, ItemBlockTooltip<BlockTile<TileEntityPaintingMachine, Machine<TileEntityPaintingMachine>>>> PAINTING_MACHINE =
          BLOCKS.register("painting_machine", () -> new BlockTile<>(MekanismBlockTypes.PAINTING_MACHINE, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.PIGMENT, stack -> RateLimitPigmentTank.createBasicItem(TileEntityPaintingMachine.MAX_PIGMENT,
                            ChemicalTankBuilder.PIGMENT.manualOnly, ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
                            pigment -> MekanismRecipeType.PAINTING.getInputCache().containsInputB(null, pigment.getStack(1))
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addPigmentSlot(0, PigmentInventorySlot::fill)
                            //TODO - 1.20.4: add this comment to more methods
                            //Note: We don't bother with the insertion check based on what pigments are currently stored
                            .addInput(MekanismRecipeType.PAINTING, ItemChemical::containsInputA)
                            .addOutput()
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntitySPSCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntitySPSCasing>>> SPS_CASING = registerBlock("sps_casing", () -> new BlockBasicMultiblock<>(MekanismBlockTypes.SPS_CASING, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY)), Rarity.EPIC);
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntitySPSPort>, ItemBlockTooltip<BlockBasicMultiblock<TileEntitySPSPort>>> SPS_PORT = registerBlock("sps_port", () -> new BlockBasicMultiblock<>(MekanismBlockTypes.SPS_PORT, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY)), Rarity.EPIC);
    public static final BlockRegistryObject<BlockTileModel<TileEntitySuperchargedCoil, BlockTypeTile<TileEntitySuperchargedCoil>>, ItemBlockTooltip<BlockTileModel<TileEntitySuperchargedCoil, BlockTypeTile<TileEntitySuperchargedCoil>>>> SUPERCHARGED_COIL = registerBlock("supercharged_coil", () -> new BlockTileModel<>(MekanismBlockTypes.SUPERCHARGED_COIL, properties -> properties.mapColor(MapColor.COLOR_ORANGE)), Rarity.EPIC);
    public static final BlockRegistryObject<BlockTile<TileEntityDimensionalStabilizer, Machine<TileEntityDimensionalStabilizer>>, ItemBlockTooltip<BlockTile<TileEntityDimensionalStabilizer, Machine<TileEntityDimensionalStabilizer>>>> DIMENSIONAL_STABILIZER =
          BLOCKS.register("dimensional_stabilizer", () -> new BlockTile<>(MekanismBlockTypes.DIMENSIONAL_STABILIZER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack).addEnergy().build()));

    public static final BlockRegistryObject<BlockQIOComponent<TileEntityQIODriveArray, BlockTypeTile<TileEntityQIODriveArray>>, ItemBlockQIOComponent> QIO_DRIVE_ARRAY = BLOCKS.register("qio_drive_array", () -> new BlockQIOComponent<>(MekanismBlockTypes.QIO_DRIVE_ARRAY, properties -> properties.mapColor(MapColor.METAL)), ItemBlockQIOComponent::new)
          .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                //Note: As we don't have to update the presence of a drive or remove it from the frequency we can make do with just using a basic slot
                //TODO - 1.20.4: Evaluate if copy the notExternal is correct or do we want this to have some other checks
                .addSlots(2 * 6, (listener, x, y) -> BasicInventorySlot.at(BasicInventorySlot.notExternal, BasicInventorySlot.notExternal, QIODriveSlot.IS_QIO_ITEM, listener, x, y))
                .build()
          ));
    public static final BlockRegistryObject<BlockQIOComponent<TileEntityQIODashboard, BlockTypeTile<TileEntityQIODashboard>>, ItemBlockQIOComponent> QIO_DASHBOARD = BLOCKS.register("qio_dashboard", () -> new BlockQIOComponent<>(MekanismBlockTypes.QIO_DASHBOARD, properties -> properties.mapColor(MapColor.COLOR_GRAY)), ItemBlockQIOComponent::new)
          //Note: While the attachment is mainly used for the portable dashboard, it is a convenient way to also handle window construction
          // and setting up the proper predicates for the actual dashboard block
          .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> stack.getData(MekanismAttachmentTypes.QIO_DASHBOARD).getSlots()));
    public static final BlockRegistryObject<BlockQIOComponent<TileEntityQIOImporter, BlockTypeTile<TileEntityQIOImporter>>, ItemBlockQIOComponent> QIO_IMPORTER = BLOCKS.register("qio_importer", () -> new BlockQIOComponent<>(MekanismBlockTypes.QIO_IMPORTER, properties -> properties.mapColor(MapColor.COLOR_GRAY)), ItemBlockQIOComponent::new);
    public static final BlockRegistryObject<BlockQIOComponent<TileEntityQIOExporter, BlockTypeTile<TileEntityQIOExporter>>, ItemBlockQIOComponent> QIO_EXPORTER = BLOCKS.register("qio_exporter", () -> new BlockQIOComponent<>(MekanismBlockTypes.QIO_EXPORTER, properties -> properties.mapColor(MapColor.COLOR_GRAY)), ItemBlockQIOComponent::new);
    public static final BlockRegistryObject<BlockQIOComponent<TileEntityQIORedstoneAdapter, BlockTypeTile<TileEntityQIORedstoneAdapter>>, ItemBlockQIOComponent> QIO_REDSTONE_ADAPTER = BLOCKS.register("qio_redstone_adapter", () -> new BlockQIOComponent<>(MekanismBlockTypes.QIO_REDSTONE_ADAPTER, properties -> properties.mapColor(MapColor.COLOR_GRAY)), ItemBlockQIOComponent::new);

    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> BASIC_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.BASIC_ENERGY_CUBE);
    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> ADVANCED_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.ADVANCED_ENERGY_CUBE);
    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> ELITE_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.ELITE_ENERGY_CUBE);
    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> ULTIMATE_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.ULTIMATE_ENERGY_CUBE);
    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> CREATIVE_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.CREATIVE_ENERGY_CUBE);

    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityUniversalCable>, ItemBlockUniversalCable> BASIC_UNIVERSAL_CABLE = registerUniversalCable(MekanismBlockTypes.BASIC_UNIVERSAL_CABLE);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityUniversalCable>, ItemBlockUniversalCable> ADVANCED_UNIVERSAL_CABLE = registerUniversalCable(MekanismBlockTypes.ADVANCED_UNIVERSAL_CABLE);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityUniversalCable>, ItemBlockUniversalCable> ELITE_UNIVERSAL_CABLE = registerUniversalCable(MekanismBlockTypes.ELITE_UNIVERSAL_CABLE);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityUniversalCable>, ItemBlockUniversalCable> ULTIMATE_UNIVERSAL_CABLE = registerUniversalCable(MekanismBlockTypes.ULTIMATE_UNIVERSAL_CABLE);

    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityMechanicalPipe>, ItemBlockMechanicalPipe> BASIC_MECHANICAL_PIPE = registerMechanicalPipe(MekanismBlockTypes.BASIC_MECHANICAL_PIPE);
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityMechanicalPipe>, ItemBlockMechanicalPipe> ADVANCED_MECHANICAL_PIPE = registerMechanicalPipe(MekanismBlockTypes.ADVANCED_MECHANICAL_PIPE);
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityMechanicalPipe>, ItemBlockMechanicalPipe> ELITE_MECHANICAL_PIPE = registerMechanicalPipe(MekanismBlockTypes.ELITE_MECHANICAL_PIPE);
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityMechanicalPipe>, ItemBlockMechanicalPipe> ULTIMATE_MECHANICAL_PIPE = registerMechanicalPipe(MekanismBlockTypes.ULTIMATE_MECHANICAL_PIPE);

    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityPressurizedTube>, ItemBlockPressurizedTube> BASIC_PRESSURIZED_TUBE = registerPressurizedTube(MekanismBlockTypes.BASIC_PRESSURIZED_TUBE);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityPressurizedTube>, ItemBlockPressurizedTube> ADVANCED_PRESSURIZED_TUBE = registerPressurizedTube(MekanismBlockTypes.ADVANCED_PRESSURIZED_TUBE);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityPressurizedTube>, ItemBlockPressurizedTube> ELITE_PRESSURIZED_TUBE = registerPressurizedTube(MekanismBlockTypes.ELITE_PRESSURIZED_TUBE);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityPressurizedTube>, ItemBlockPressurizedTube> ULTIMATE_PRESSURIZED_TUBE = registerPressurizedTube(MekanismBlockTypes.ULTIMATE_PRESSURIZED_TUBE);

    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityLogisticalTransporter>, ItemBlockLogisticalTransporter> BASIC_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(MekanismBlockTypes.BASIC_LOGISTICAL_TRANSPORTER);
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityLogisticalTransporter>, ItemBlockLogisticalTransporter> ADVANCED_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(MekanismBlockTypes.ADVANCED_LOGISTICAL_TRANSPORTER);
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityLogisticalTransporter>, ItemBlockLogisticalTransporter> ELITE_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(MekanismBlockTypes.ELITE_LOGISTICAL_TRANSPORTER);
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityLogisticalTransporter>, ItemBlockLogisticalTransporter> ULTIMATE_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(MekanismBlockTypes.ULTIMATE_LOGISTICAL_TRANSPORTER);

    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityRestrictiveTransporter>, ItemBlockTransporter<TileEntityRestrictiveTransporter>> RESTRICTIVE_TRANSPORTER = BLOCKS.register("restrictive_transporter", () -> new BlockLargeTransmitter<>(MekanismBlockTypes.RESTRICTIVE_TRANSPORTER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), block -> new ItemBlockTransporter<>(block, MekanismLang.DESCRIPTION_RESTRICTIVE));
    public static final BlockRegistryObject<BlockLargeTransmitter<TileEntityDiversionTransporter>, ItemBlockTransporter<TileEntityDiversionTransporter>> DIVERSION_TRANSPORTER = BLOCKS.register("diversion_transporter", () -> new BlockLargeTransmitter<>(MekanismBlockTypes.DIVERSION_TRANSPORTER, properties -> properties.mapColor(MapColor.COLOR_ORANGE)), block -> new ItemBlockTransporter<>(block, MekanismLang.DESCRIPTION_DIVERSION));

    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityThermodynamicConductor>, ItemBlockThermodynamicConductor> BASIC_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(MekanismBlockTypes.BASIC_THERMODYNAMIC_CONDUCTOR);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityThermodynamicConductor>, ItemBlockThermodynamicConductor> ADVANCED_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(MekanismBlockTypes.ADVANCED_THERMODYNAMIC_CONDUCTOR);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityThermodynamicConductor>, ItemBlockThermodynamicConductor> ELITE_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(MekanismBlockTypes.ELITE_THERMODYNAMIC_CONDUCTOR);
    public static final BlockRegistryObject<BlockSmallTransmitter<TileEntityThermodynamicConductor>, ItemBlockThermodynamicConductor> ULTIMATE_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(MekanismBlockTypes.ULTIMATE_THERMODYNAMIC_CONDUCTOR);

    public static final BlockRegistryObject<BlockBounding, BlockItem> BOUNDING_BLOCK = BLOCKS.register("bounding_block", BlockBounding::new);

    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> BASIC_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.BASIC_CHEMICAL_TANK);
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> ADVANCED_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.ADVANCED_CHEMICAL_TANK);
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> ELITE_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.ELITE_CHEMICAL_TANK);
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> ULTIMATE_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.ULTIMATE_CHEMICAL_TANK);
    public static final BlockRegistryObject<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> CREATIVE_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.CREATIVE_CHEMICAL_TANK);

    public static final BlockRegistryObject<BlockCardboardBox, ItemBlockCardboardBox> CARDBOARD_BOX = BLOCKS.register("cardboard_box", BlockCardboardBox::new, ItemBlockCardboardBox::new);
    public static final BlockRegistryObject<Block, BlockItem> SALT_BLOCK = BLOCKS.register("block_salt", Properties.of().strength(0.5F).sound(SoundType.SAND).instrument(NoteBlockInstrument.SNARE));
    public static final BlockRegistryObject<Block, BlockItem> BIO_FUEL_BLOCK = BLOCKS.register("block_bio_fuel", Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.5F).sound(SoundType.GRASS).instrument(NoteBlockInstrument.BANJO));

    private static BlockRegistryObject<BlockResource, ItemBlockMekanism<BlockResource>> registerResourceBlock(BlockResourceInfo resource) {
        return BLOCKS.registerDefaultProperties("block_" + resource.getRegistrySuffix(), () -> new BlockResource(resource), (block, properties) -> {
            if (!block.getResourceInfo().burnsInFire()) {
                properties = properties.fireResistant();
            }
            return new ItemBlockMekanism<>(block, properties);
        });
    }

    private static BlockRegistryObject<BlockBin, ItemBlockBin> registerBin(BlockTypeTile<TileEntityBin> type) {
        BinTier tier = (BinTier) type.get(AttributeTier.class).tier();
        return registerTieredBlock(tier, "_bin", color -> new BlockBin(type, properties -> properties.mapColor(color)), ItemBlockBin::new)
              .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> List.of(BinInventorySlot.create(null, tier))));
    }

    private static BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> registerInductionCell(BlockTypeTile<TileEntityInductionCell> type) {
        return registerTieredBlock(type, "_induction_cell", color -> new BlockTile<>(type, properties -> properties.mapColor(color)), ItemBlockInductionCell::new);
    }

    private static BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, ItemBlockInductionProvider> registerInductionProvider(BlockTypeTile<TileEntityInductionProvider> type) {
        return registerTieredBlock(type, "_induction_provider", color -> new BlockTile<>(type, properties -> properties.mapColor(color)), ItemBlockInductionProvider::new);
    }

    private static BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> registerFluidTank(Machine<TileEntityFluidTank> type) {
        FluidTankTier tier = (FluidTankTier) type.get(AttributeTier.class).tier();
        return registerTieredBlock(tier, "_fluid_tank", () -> new BlockFluidTank(type), ItemBlockFluidTank::new)
              .forItemHolder(holder -> holder
                    .addAttachedContainerCapability(ContainerType.FLUID, stack -> FluidTankRateLimitFluidTank.create(tier))
                    .addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                          .addFluidSlot(0, FluidInventorySlot::input)
                          .addOutput()
                          .build()
                    )
              );
    }

    private static BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> registerEnergyCube(Machine<TileEntityEnergyCube> type) {
        return registerTieredBlock(type, "_energy_cube", () -> new BlockEnergyCube(type), ItemBlockEnergyCube::new)
              .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                    .addEnergy()
                    .addEnergySlot(0, EnergyInventorySlot::drain)
                    .build()
              ));
    }

    private static BlockRegistryObject<BlockSmallTransmitter<TileEntityUniversalCable>, ItemBlockUniversalCable> registerUniversalCable(
          BlockTypeTile<TileEntityUniversalCable> type) {
        return registerTieredBlock(type, "_universal_cable", () -> new BlockSmallTransmitter<>(type), ItemBlockUniversalCable::new);
    }

    private static BlockRegistryObject<BlockLargeTransmitter<TileEntityMechanicalPipe>, ItemBlockMechanicalPipe> registerMechanicalPipe(
          BlockTypeTile<TileEntityMechanicalPipe> type) {
        return registerTieredBlock(type, "_mechanical_pipe", () -> new BlockLargeTransmitter<>(type), ItemBlockMechanicalPipe::new);
    }

    private static BlockRegistryObject<BlockSmallTransmitter<TileEntityPressurizedTube>, ItemBlockPressurizedTube> registerPressurizedTube(
          BlockTypeTile<TileEntityPressurizedTube> type) {
        return registerTieredBlock(type, "_pressurized_tube", () -> new BlockSmallTransmitter<>(type), ItemBlockPressurizedTube::new);
    }

    private static BlockRegistryObject<BlockLargeTransmitter<TileEntityLogisticalTransporter>, ItemBlockLogisticalTransporter> registerLogisticalTransporter(
          BlockTypeTile<TileEntityLogisticalTransporter> type) {
        return registerTieredBlock(type, "_logistical_transporter", () -> new BlockLargeTransmitter<>(type), ItemBlockLogisticalTransporter::new);
    }

    private static BlockRegistryObject<BlockSmallTransmitter<TileEntityThermodynamicConductor>, ItemBlockThermodynamicConductor> registerThermodynamicConductor(
          BlockTypeTile<TileEntityThermodynamicConductor> type) {
        return registerTieredBlock(type, "_thermodynamic_conductor", () -> new BlockSmallTransmitter<>(type), ItemBlockThermodynamicConductor::new);
    }

    private static BlockRegistryObject<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> registerChemicalTank(
          Machine<TileEntityChemicalTank> type) {
        return registerTieredBlock(type, "_chemical_tank", color -> new BlockTileModel<>(type, properties -> properties.mapColor(color)), ItemBlockChemicalTank::new)
              .forItemHolder(holder -> holder
                    .addMissingMergedTanks(MekanismAttachmentTypes.CHEMICAL_TANK_CONTENTS_HANDLER, false, true)
                    .addAttachmentOnlyContainers(ContainerType.ITEM, stack -> {
                        MergedChemicalTank tank = stack.getData(MekanismAttachmentTypes.CHEMICAL_TANK_CONTENTS_HANDLER);
                        return ItemSlotsBuilder.builder(stack)
                              .addContainerSlot(tank, MergedChemicalInventorySlot::drain)
                              .addContainerSlot(tank, MergedChemicalInventorySlot::fill)
                              .build();
                    })
              );
    }

    private static <TILE extends TileEntityFactory<?>> BlockRegistryObject<BlockFactory<?>, ItemBlockFactory> registerFactory(Factory<TILE> type) {
        FactoryTier tier = (FactoryTier) type.get(AttributeTier.class).tier();
        BlockRegistryObject<BlockFactory<?>, ItemBlockFactory> factory = registerTieredBlock(tier, "_" + type.getFactoryType().getRegistryNameComponent() + "_factory", () -> new BlockFactory<>(type), ItemBlockFactory::new);
        factory.forItemHolder(holder -> {
            int processes = tier.processes;
            Predicate<ItemStack> recipeInputPredicate = switch (type.getFactoryType()) {
                case SMELTING -> s -> MekanismRecipeType.SMELTING.getInputCache().containsInput(null, s);
                case ENRICHING -> s -> MekanismRecipeType.ENRICHING.getInputCache().containsInput(null, s);
                case CRUSHING -> s -> MekanismRecipeType.CRUSHING.getInputCache().containsInput(null, s);
                case COMPRESSING -> s -> MekanismRecipeType.COMPRESSING.getInputCache().containsInputA(null, s);
                case COMBINING -> s -> MekanismRecipeType.COMBINING.getInputCache().containsInputA(null, s);
                case PURIFYING -> s -> MekanismRecipeType.PURIFYING.getInputCache().containsInputA(null, s);
                case INJECTING -> s -> MekanismRecipeType.INJECTING.getInputCache().containsInputA(null, s);
                case INFUSING -> s -> MekanismRecipeType.METALLURGIC_INFUSING.getInputCache().containsInputA(null, s);
                case SAWING -> s -> MekanismRecipeType.SAWING.getInputCache().containsInput(null, s);
            };
            switch (type.getFactoryType()) {
                case SMELTING, ENRICHING, CRUSHING -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                      .addBasicFactorySlots(processes, recipeInputPredicate)
                      .addEnergy()
                      .build()
                );
                case COMPRESSING, INJECTING, PURIFYING -> {
                    Predicate<GasStack> secondaryInputPredicate = switch (type.getFactoryType()) {
                        case COMPRESSING -> gas -> MekanismRecipeType.COMPRESSING.getInputCache().containsInputB(null, gas);
                        case INJECTING -> gas -> MekanismRecipeType.INJECTING.getInputCache().containsInputB(null, gas);
                        case PURIFYING -> gas -> MekanismRecipeType.PURIFYING.getInputCache().containsInputB(null, gas);
                        default -> throw new IllegalStateException("Factory type doesn't have a known gas recipe");
                    };
                    holder.addAttachmentOnlyContainer(ContainerType.GAS, stack -> RateLimitGasTank.createBasicItem(TileEntityAdvancedElectricMachine.MAX_GAS * processes,
                          ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                          gas -> secondaryInputPredicate.test(gas.getStack(1))
                    )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                          .addBasicFactorySlots(processes, recipeInputPredicate)
                          .addGasSlotWithConversion(0)
                          .addEnergy()
                          .build()
                    );
                }
                case COMBINING -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                      .addBasicFactorySlots(processes, recipeInputPredicate)
                      .addInput(MekanismRecipeType.COMBINING, DoubleItem::containsInputB)
                      .addEnergy()
                      .build()
                );
                case INFUSING -> holder
                      .addAttachmentOnlyContainer(ContainerType.INFUSION, stack -> RateLimitInfusionTank.createBasicItem(TileEntityMetallurgicInfuser.MAX_INFUSE * processes,
                            ChemicalTankBuilder.INFUSION.manualOnly, ChemicalTankBuilder.INFUSION.alwaysTrueBi,
                            infuseType -> MekanismRecipeType.METALLURGIC_INFUSING.getInputCache().containsInputB(null, infuseType.getStack(1))
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addBasicFactorySlots(processes, recipeInputPredicate)
                            .addInfusionSlotWithConversion(0)
                            .addEnergy()
                            .build()
                      );
                case SAWING -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                      .addBasicFactorySlots(processes, recipeInputPredicate, true)
                      .addEnergy()
                      .build()
                );
            }

        });
        return factory;
    }

    private static <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> registerTieredBlock(BlockType type, String suffix,
          Function<MapColor, ? extends BLOCK> blockSupplier, Function<BLOCK, ITEM> itemCreator) {
        return registerTieredBlock(type.get(AttributeTier.class).tier(), suffix, blockSupplier, itemCreator);
    }

    private static <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> registerTieredBlock(ITier tier, String suffix,
          Function<MapColor, ? extends BLOCK> blockSupplier, Function<BLOCK, ITEM> itemCreator) {
        return registerTieredBlock(tier, suffix, () -> blockSupplier.apply(tier.getBaseTier().getMapColor()), itemCreator);
    }

    private static <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> registerTieredBlock(BlockType type, String suffix,
          Supplier<? extends BLOCK> blockSupplier, Function<BLOCK, ITEM> itemCreator) {
        return registerTieredBlock(type.get(AttributeTier.class).tier(), suffix, blockSupplier, itemCreator);
    }

    private static <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> registerTieredBlock(ITier tier, String suffix,
          Supplier<? extends BLOCK> blockSupplier, Function<BLOCK, ITEM> itemCreator) {
        return BLOCKS.register(tier.getBaseTier().getLowerName() + suffix, blockSupplier, itemCreator);
    }

    private static OreBlockType registerOre(OreType ore) {
        String name = ore.getResource().getRegistrySuffix() + "_ore";
        BlockRegistryObject<BlockOre, ItemBlockTooltip<BlockOre>> stoneOre = registerBlock(name, () -> new BlockOre(ore));
        BlockRegistryObject<BlockOre, ItemBlockTooltip<BlockOre>> deepslateOre = BLOCKS.registerDefaultProperties("deepslate_" + name,
              () -> new BlockOre(ore, Properties.ofLegacyCopy(stoneOre.getBlock()).mapColor(MapColor.DEEPSLATE)
                    .strength(4.5F, 3).sound(SoundType.DEEPSLATE)), ItemBlockTooltip::new);
        return new OreBlockType(stoneOre, deepslateOre);
    }

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerBlock(String name,
          Supplier<? extends BLOCK> blockSupplier) {
        return BLOCKS.registerDefaultProperties(name, blockSupplier, ItemBlockTooltip::new);
    }

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerBlock(String name,
          Supplier<? extends BLOCK> blockSupplier, Rarity rarity) {
        return BLOCKS.registerDefaultProperties(name, blockSupplier, (block, props) -> new ItemBlockTooltip<>(block, props.rarity(rarity)));
    }

    /**
     * Retrieves a Factory with a defined tier and recipe type.
     *
     * @param tier - tier to add to the Factory
     * @param type - recipe type to add to the Factory
     *
     * @return factory with defined tier and recipe type
     */
    public static BlockRegistryObject<BlockFactory<?>, ItemBlockFactory> getFactory(@NotNull FactoryTier tier, @NotNull FactoryType type) {
        return FACTORIES.get(tier, type);
    }

    @SuppressWarnings("unchecked")
    public static BlockRegistryObject<BlockFactory<?>, ItemBlockFactory>[] getFactoryBlocks() {
        return FACTORIES.values().toArray(new BlockRegistryObject[0]);
    }
}