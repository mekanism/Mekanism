package mekanism.generators.common.registries;

import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.component.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.component.containers.fluid.FluidTanksBuilder;
import mekanism.common.component.containers.heat.HeatCapacitorBuilder;
import mekanism.common.component.containers.item.ItemSlotsBuilder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.BlockReactorGlass;
import mekanism.generators.common.block.fusion.BlockLaserFocusMatrix;
import mekanism.generators.common.block.turbine.BlockTurbineRotor;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.blocktype.Generator;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntityReactorGlass;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import mekanism.generators.common.tile.fission.TileEntityControlRodAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionFuelAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorPort;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorBlock;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter.FusionReactorLogic;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorPort;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Unit;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.material.MapColor;

public class GeneratorsBlocks {

    private GeneratorsBlocks() {
    }

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismGenerators.MODID);

    public static final BlockRegistryObject<BlockTileModel<TileEntityHeatGenerator, Generator<TileEntityHeatGenerator>>, ItemBlockTooltip> HEAT_GENERATOR =
          BLOCKS.registerDetails("heat_generator", properties -> new BlockTileModel<>(GeneratorsBlockTypes.HEAT_GENERATOR, BlockTile.defaultProperties(properties).mapColor(MapColor.METAL)))
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                            .addBasic(MekanismGeneratorsConfig.generators.heatTankCapacity, fluid -> fluid.is(FluidTags.LAVA))
                            .build()
                      ).addAttachmentOnlyContainers(ContainerType.HEAT, () -> HeatCapacitorBuilder.basicCreator(
                            TileEntityHeatGenerator.HEAT_CAPACITY, TileEntityHeatGenerator.INVERSE_CONDUCTION_COEFFICIENT, TileEntityHeatGenerator.INVERSE_INSULATION_COEFFICIENT
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addBasic(1)//TODO - 26.3: is this really needed? .addFluidFuelSlot(0, itemType -> itemType.toStack().getBurnTime(null) != 0)
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntitySolarGenerator, Generator<TileEntitySolarGenerator>>, ItemBlockTooltip> SOLAR_GENERATOR =
          BLOCKS.registerDetails("solar_generator", properties -> new BlockTileModel<>(GeneratorsBlockTypes.SOLAR_GENERATOR, BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_BLUE)))
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));
    public static final BlockRegistryObject<BlockTileModel<TileEntityGasGenerator, Generator<TileEntityGasGenerator>>, ItemBlockTooltip> GAS_BURNING_GENERATOR =
          BLOCKS.registerDetails("gas_burning_generator", properties -> new BlockTileModel<>(GeneratorsBlockTypes.GAS_BURNING_GENERATOR, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())))
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                            .addBasic(MekanismGeneratorsConfig.generators.gbgTankCapacity, chemical -> chemical.getFuel(null) != null)
                            .build()
                      ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addChemicalFillSlot(0)
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityBioGenerator, Generator<TileEntityBioGenerator>>, ItemBlockTooltip> BIO_GENERATOR =
          BLOCKS.registerDetails("bio_generator", properties -> new BlockTileModel<>(GeneratorsBlockTypes.BIO_GENERATOR, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())))
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                            .addBasic(MekanismGeneratorsConfig.generators.bioTankCapacity, fluid -> fluid.is(GeneratorTags.Fluids.BIOETHANOL))
                            .build()
                      ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addFluidFuelSlot(0, itemType -> itemType.is(MekanismTags.Items.FUELS_BIO) || itemType.is(MekanismTags.Items.FUELS_BLOCK_BIO))
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityAdvancedSolarGenerator, Generator<TileEntityAdvancedSolarGenerator>>, ItemBlockTooltip> ADVANCED_SOLAR_GENERATOR =
          BLOCKS.registerDetails("advanced_solar_generator", properties -> new BlockTileModel<>(GeneratorsBlockTypes.ADVANCED_SOLAR_GENERATOR, BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_BLUE)))
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));
    public static final BlockRegistryObject<BlockTileModel<TileEntityWindGenerator, Generator<TileEntityWindGenerator>>, ItemBlockTooltip> WIND_GENERATOR = BLOCKS.registerDetails("wind_generator", properties -> new BlockTileModel<>(GeneratorsBlockTypes.WIND_GENERATOR, BlockTile.defaultProperties(properties).mapColor(MapColor.METAL)))
          .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));

    public static final BlockRegistryObject<BlockTurbineRotor, BlockItem> TURBINE_ROTOR = BLOCKS.register("turbine_rotor", BlockTurbineRotor::new);
    public static final BlockRegistryObject<BlockTile<TileEntityRotationalComplex, BlockTypeTile<TileEntityRotationalComplex>>, BlockItem> ROTATIONAL_COMPLEX = BLOCKS.register("rotational_complex", properties -> new BlockTile<>(GeneratorsBlockTypes.ROTATIONAL_COMPLEX, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockTile<TileEntityElectromagneticCoil, BlockTypeTile<TileEntityElectromagneticCoil>>, BlockItem> ELECTROMAGNETIC_COIL = BLOCKS.register("electromagnetic_coil", properties -> new BlockTile<>(GeneratorsBlockTypes.ELECTROMAGNETIC_COIL, BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_BLACK)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityTurbineCasing>, BlockItem> TURBINE_CASING = BLOCKS.register("turbine_casing", properties -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.TURBINE_CASING, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.CLAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityTurbineValve>, BlockItem> TURBINE_VALVE = BLOCKS.register("turbine_valve", properties -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.TURBINE_VALVE, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.CLAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityTurbineVent>, BlockItem> TURBINE_VENT = BLOCKS.register("turbine_vent", properties -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.TURBINE_VENT, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.COLOR_GRAY)));
    public static final BlockRegistryObject<BlockTile<TileEntitySaturatingCondenser, BlockTypeTile<TileEntitySaturatingCondenser>>, BlockItem> SATURATING_CONDENSER = BLOCKS.register("saturating_condenser", properties -> new BlockTile<>(GeneratorsBlockTypes.SATURATING_CONDENSER, BlockTile.defaultProperties(properties).mapColor(MapColor.COLOR_GRAY)));

    public static final BlockRegistryObject<BlockReactorGlass<TileEntityReactorGlass>, BlockItem> REACTOR_GLASS = BLOCKS.register("reactor_glass", properties -> new BlockReactorGlass<>(GeneratorsBlockTypes.REACTOR_GLASS, properties));

    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFissionReactorCasing>, BlockItem> FISSION_REACTOR_CASING = BLOCKS.register("fission_reactor_casing",
          properties -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FISSION_REACTOR_CASING, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFissionReactorPort>, BlockItem> FISSION_REACTOR_PORT = BLOCKS.register("fission_reactor_port",
          properties -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FISSION_REACTOR_PORT, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFissionReactorLogicAdapter>, BlockItem> FISSION_REACTOR_LOGIC_ADAPTER = BLOCKS.register("fission_reactor_logic_adapter",
          properties -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FISSION_REACTOR_LOGIC_ADAPTER, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.COLOR_LIGHT_GRAY)),
          (block, properties) -> new BlockItem(block, properties
                .component(GeneratorsDataComponents.FISSION_LOGIC_TYPE, FissionReactorLogic.DISABLED)
                .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
          ));
    public static final BlockRegistryObject<BlockTileModel<TileEntityFissionFuelAssembly, BlockTypeTile<TileEntityFissionFuelAssembly>>, BlockItem> FISSION_FUEL_ASSEMBLY = BLOCKS.register("fission_fuel_assembly", properties -> new BlockTileModel<>(GeneratorsBlockTypes.FISSION_FUEL_ASSEMBLY, BlockTile.defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockTileModel<TileEntityControlRodAssembly, BlockTypeTile<TileEntityControlRodAssembly>>, BlockItem> CONTROL_ROD_ASSEMBLY = BLOCKS.register("control_rod_assembly", properties -> new BlockTileModel<>(GeneratorsBlockTypes.CONTROL_ROD_ASSEMBLY, BlockTile.defaultProperties(properties).mapColor(MapColor.METAL)));

    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFusionReactorController>, BlockItem> FUSION_REACTOR_CONTROLLER = BLOCKS.register("fusion_reactor_controller", properties -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FUSION_REACTOR_CONTROLLER, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.COLOR_ORANGE)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFusionReactorBlock>, BlockItem> FUSION_REACTOR_FRAME = BLOCKS.register("fusion_reactor_frame", properties -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FUSION_REACTOR_FRAME, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.TERRACOTTA_BROWN)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFusionReactorPort>, BlockItem> FUSION_REACTOR_PORT = BLOCKS.register("fusion_reactor_port", properties -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FUSION_REACTOR_PORT, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.TERRACOTTA_BROWN)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFusionReactorLogicAdapter>, BlockItem> FUSION_REACTOR_LOGIC_ADAPTER = BLOCKS.register("fusion_reactor_logic_adapter",
          properties -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FUSION_REACTOR_LOGIC_ADAPTER, BlockBasicMultiblock.defaultProperties(properties).mapColor(MapColor.TERRACOTTA_BROWN)),
          (block, properties) -> new BlockItem(block, properties
                .component(GeneratorsDataComponents.FUSION_LOGIC_TYPE, FusionReactorLogic.DISABLED)
                .component(GeneratorsDataComponents.ACTIVE_COOLED, false)
                .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
          ));
    public static final BlockRegistryObject<BlockLaserFocusMatrix, BlockItem> LASER_FOCUS_MATRIX = BLOCKS.register("laser_focus_matrix", BlockLaserFocusMatrix::new);
}