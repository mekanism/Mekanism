package mekanism.generators.common.registries;

import java.util.function.Supplier;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.fluid.FluidTanksBuilder;
import mekanism.common.attachments.containers.heat.HeatCapacitorsBuilder;
import mekanism.common.attachments.containers.item.ItemSlotsBuilder;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.BlockReactorGlass;
import mekanism.generators.common.block.fusion.BlockLaserFocusMatrix;
import mekanism.generators.common.block.turbine.BlockTurbineRotor;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.blocktype.Generator;
import mekanism.generators.common.item.ItemBlockFissionLogicAdapter;
import mekanism.generators.common.item.ItemBlockFusionLogicAdapter;
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
import mekanism.generators.common.tile.fission.TileEntityFissionReactorPort;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorBlock;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorPort;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public class GeneratorsBlocks {

    private GeneratorsBlocks() {
    }

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismGenerators.MODID);

    public static final BlockRegistryObject<BlockTileModel<TileEntityHeatGenerator, Generator<TileEntityHeatGenerator>>, ItemBlockTooltip<BlockTileModel<TileEntityHeatGenerator, Generator<TileEntityHeatGenerator>>>> HEAT_GENERATOR =
          BLOCKS.registerDetails("heat_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.HEAT_GENERATOR, properties -> properties.mapColor(MapColor.METAL)))
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                            .addBasic(MekanismGeneratorsConfig.generators.heatTankCapacity, fluid -> fluid.is(FluidTags.LAVA))
                            .build()
                      ).addAttachmentOnlyContainers(ContainerType.HEAT, () -> HeatCapacitorsBuilder.builder()
                            .addBasic(TileEntityHeatGenerator.HEAT_CAPACITY, TileEntityHeatGenerator.INVERSE_CONDUCTION_COEFFICIENT, TileEntityHeatGenerator.INVERSE_INSULATION_COEFFICIENT)
                            .build()
                      ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addFluidFuelSlot(0, s -> s.getBurnTime(null) != 0)
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntitySolarGenerator, Generator<TileEntitySolarGenerator>>, ItemBlockTooltip<BlockTileModel<TileEntitySolarGenerator, Generator<TileEntitySolarGenerator>>>> SOLAR_GENERATOR =
          BLOCKS.registerDetails("solar_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.SOLAR_GENERATOR, properties -> properties.mapColor(MapColor.COLOR_BLUE)))
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));
    public static final BlockRegistryObject<BlockTileModel<TileEntityGasGenerator, Generator<TileEntityGasGenerator>>, ItemBlockTooltip<BlockTileModel<TileEntityGasGenerator, Generator<TileEntityGasGenerator>>>> GAS_BURNING_GENERATOR =
          BLOCKS.registerDetails("gas_burning_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.GAS_BURNING_GENERATOR, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())))
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                            .addBasic(MekanismGeneratorsConfig.generators.gbgTankCapacity, TileEntityGasGenerator.HAS_FUEL)
                            .build()
                      ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addChemicalFillSlot(0)
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityBioGenerator, Generator<TileEntityBioGenerator>>, ItemBlockTooltip<BlockTileModel<TileEntityBioGenerator, Generator<TileEntityBioGenerator>>>> BIO_GENERATOR =
          BLOCKS.registerDetails("bio_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.BIO_GENERATOR, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())))
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                            .addBasic(MekanismGeneratorsConfig.generators.bioTankCapacity, fluid -> fluid.is(GeneratorTags.Fluids.BIOETHANOL))
                            .build()
                      ).addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addFluidFuelSlot(0, s -> s.is(MekanismTags.Items.FUELS_BIO) || s.is(MekanismTags.Items.FUELS_BLOCK_BIO))
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityAdvancedSolarGenerator, Generator<TileEntityAdvancedSolarGenerator>>, ItemBlockTooltip<BlockTileModel<TileEntityAdvancedSolarGenerator, Generator<TileEntityAdvancedSolarGenerator>>>> ADVANCED_SOLAR_GENERATOR =
          BLOCKS.registerDetails("advanced_solar_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.ADVANCED_SOLAR_GENERATOR, properties -> properties.mapColor(MapColor.COLOR_BLUE)))
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));
    public static final BlockRegistryObject<BlockTileModel<TileEntityWindGenerator, Generator<TileEntityWindGenerator>>, ItemBlockTooltip<BlockTileModel<TileEntityWindGenerator, Generator<TileEntityWindGenerator>>>> WIND_GENERATOR = BLOCKS.registerDetails("wind_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.WIND_GENERATOR, properties -> properties.mapColor(MapColor.METAL)))
          .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));

    public static final BlockRegistryObject<BlockTurbineRotor, ItemBlockTooltip<BlockTurbineRotor>> TURBINE_ROTOR = registerTooltipBlock("turbine_rotor", BlockTurbineRotor::new);
    public static final BlockRegistryObject<BlockTile<TileEntityRotationalComplex, BlockTypeTile<TileEntityRotationalComplex>>, ItemBlockTooltip<BlockTile<TileEntityRotationalComplex, BlockTypeTile<TileEntityRotationalComplex>>>> ROTATIONAL_COMPLEX = registerTooltipBlock("rotational_complex", () -> new BlockTile<>(GeneratorsBlockTypes.ROTATIONAL_COMPLEX, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockTile<TileEntityElectromagneticCoil, BlockTypeTile<TileEntityElectromagneticCoil>>, ItemBlockTooltip<BlockTile<TileEntityElectromagneticCoil, BlockTypeTile<TileEntityElectromagneticCoil>>>> ELECTROMAGNETIC_COIL = registerTooltipBlock("electromagnetic_coil", () -> new BlockTile<>(GeneratorsBlockTypes.ELECTROMAGNETIC_COIL, properties -> properties.mapColor(MapColor.COLOR_BLACK)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityTurbineCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityTurbineCasing>>> TURBINE_CASING = registerTooltipBlock("turbine_casing", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.TURBINE_CASING, properties -> properties.mapColor(MapColor.CLAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityTurbineValve>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityTurbineValve>>> TURBINE_VALVE = registerTooltipBlock("turbine_valve", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.TURBINE_VALVE, properties -> properties.mapColor(MapColor.CLAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityTurbineVent>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityTurbineVent>>> TURBINE_VENT = registerTooltipBlock("turbine_vent", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.TURBINE_VENT, properties -> properties.mapColor(MapColor.COLOR_GRAY)));
    public static final BlockRegistryObject<BlockTile<TileEntitySaturatingCondenser, BlockTypeTile<TileEntitySaturatingCondenser>>, ItemBlockTooltip<BlockTile<TileEntitySaturatingCondenser, BlockTypeTile<TileEntitySaturatingCondenser>>>> SATURATING_CONDENSER = registerTooltipBlock("saturating_condenser", () -> new BlockTile<>(GeneratorsBlockTypes.SATURATING_CONDENSER, properties -> properties.mapColor(MapColor.COLOR_GRAY)));

    public static final BlockRegistryObject<BlockReactorGlass<TileEntityReactorGlass>, ItemBlockTooltip<BlockReactorGlass<TileEntityReactorGlass>>> REACTOR_GLASS = registerTooltipBlock("reactor_glass", () -> new BlockReactorGlass<>(GeneratorsBlockTypes.REACTOR_GLASS));

    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFissionReactorCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFissionReactorCasing>>> FISSION_REACTOR_CASING = registerTooltipBlock("fission_reactor_casing", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FISSION_REACTOR_CASING, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFissionReactorPort>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFissionReactorPort>>> FISSION_REACTOR_PORT = registerTooltipBlock("fission_reactor_port", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FISSION_REACTOR_PORT, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFissionReactorLogicAdapter>, ItemBlockFissionLogicAdapter> FISSION_REACTOR_LOGIC_ADAPTER = BLOCKS.register("fission_reactor_logic_adapter", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FISSION_REACTOR_LOGIC_ADAPTER, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY)), ItemBlockFissionLogicAdapter::new);
    public static final BlockRegistryObject<BlockTileModel<TileEntityFissionFuelAssembly, BlockTypeTile<TileEntityFissionFuelAssembly>>, ItemBlockTooltip<BlockTileModel<TileEntityFissionFuelAssembly, BlockTypeTile<TileEntityFissionFuelAssembly>>>> FISSION_FUEL_ASSEMBLY = registerTooltipBlock("fission_fuel_assembly", () -> new BlockTileModel<>(GeneratorsBlockTypes.FISSION_FUEL_ASSEMBLY, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockTileModel<TileEntityControlRodAssembly, BlockTypeTile<TileEntityControlRodAssembly>>, ItemBlockTooltip<BlockTileModel<TileEntityControlRodAssembly, BlockTypeTile<TileEntityControlRodAssembly>>>> CONTROL_ROD_ASSEMBLY = registerTooltipBlock("control_rod_assembly", () -> new BlockTileModel<>(GeneratorsBlockTypes.CONTROL_ROD_ASSEMBLY, properties -> properties.mapColor(MapColor.METAL)));

    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFusionReactorController>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFusionReactorController>>> FUSION_REACTOR_CONTROLLER = registerTooltipBlock("fusion_reactor_controller", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FUSION_REACTOR_CONTROLLER, properties -> properties.mapColor(MapColor.COLOR_ORANGE)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFusionReactorBlock>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFusionReactorBlock>>> FUSION_REACTOR_FRAME = registerTooltipBlock("fusion_reactor_frame", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FUSION_REACTOR_FRAME, properties -> properties.mapColor(MapColor.TERRACOTTA_BROWN)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFusionReactorPort>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFusionReactorPort>>> FUSION_REACTOR_PORT = registerTooltipBlock("fusion_reactor_port", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FUSION_REACTOR_PORT, properties -> properties.mapColor(MapColor.TERRACOTTA_BROWN)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFusionReactorLogicAdapter>, ItemBlockFusionLogicAdapter> FUSION_REACTOR_LOGIC_ADAPTER = BLOCKS.register("fusion_reactor_logic_adapter", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FUSION_REACTOR_LOGIC_ADAPTER, properties -> properties.mapColor(MapColor.TERRACOTTA_BROWN)), ItemBlockFusionLogicAdapter::new);
    public static final BlockRegistryObject<BlockLaserFocusMatrix, ItemBlockTooltip<BlockLaserFocusMatrix>> LASER_FOCUS_MATRIX = registerTooltipBlock("laser_focus_matrix", BlockLaserFocusMatrix::new);

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerTooltipBlock(String name, Supplier<BLOCK> blockCreator) {
        return BLOCKS.register(name, blockCreator, ItemBlockTooltip::new);
    }
}