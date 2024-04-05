package mekanism.generators.common.registries;

import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.attribute.GasAttributes.Fuel;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.basic.BlockStructuralGlass;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.capabilities.chemical.variable.RateLimitGasTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.inventory.slot.ItemSlotsBuilder;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.fusion.BlockLaserFocusMatrix;
import mekanism.generators.common.block.turbine.BlockTurbineRotor;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.blocktype.Generator;
import mekanism.generators.common.item.ItemBlockFissionLogicAdapter;
import mekanism.generators.common.item.ItemBlockFusionLogicAdapter;
import mekanism.generators.common.item.generator.ItemBlockWindGenerator;
import mekanism.generators.common.slot.FluidFuelInventorySlot;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.FluidStack;

public class GeneratorsBlocks {

    private GeneratorsBlocks() {
    }

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismGenerators.MODID);

    public static final BlockRegistryObject<BlockTileModel<TileEntityHeatGenerator, Generator<TileEntityHeatGenerator>>, ItemBlockTooltip<BlockTileModel<TileEntityHeatGenerator, Generator<TileEntityHeatGenerator>>>> HEAT_GENERATOR =
          BLOCKS.register("heat_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.HEAT_GENERATOR, properties -> properties.mapColor(MapColor.METAL)), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.FLUID, stack -> RateLimitFluidTank.createBasicItem(MekanismGeneratorsConfig.generators.heatTankCapacity,
                            BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi,
                            fluidStack -> fluidStack.is(FluidTags.LAVA)
                      )).addAttachmentOnlyContainer(ContainerType.HEAT, stack -> BasicHeatCapacitor.createBasicItem(TileEntityHeatGenerator.HEAT_CAPACITY,
                            TileEntityHeatGenerator.INVERSE_CONDUCTION_COEFFICIENT, TileEntityHeatGenerator.INVERSE_INSULATION_COEFFICIENT
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addFluidSlot(0, (tank, listener, x, y) -> FluidFuelInventorySlot.forFuel(tank,
                                  s -> s.getBurnTime(null) / 20,
                                  size -> new FluidStack(Fluids.LAVA, size),
                                  listener, x, y)
                            ).addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntitySolarGenerator, Generator<TileEntitySolarGenerator>>, ItemBlockTooltip<BlockTileModel<TileEntitySolarGenerator, Generator<TileEntitySolarGenerator>>>> SOLAR_GENERATOR =
          BLOCKS.register("solar_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.SOLAR_GENERATOR, properties -> properties.mapColor(MapColor.COLOR_BLUE)), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack).addEnergy().build()));
    public static final BlockRegistryObject<BlockTileModel<TileEntityGasGenerator, Generator<TileEntityGasGenerator>>, ItemBlockTooltip<BlockTileModel<TileEntityGasGenerator, Generator<TileEntityGasGenerator>>>> GAS_BURNING_GENERATOR =
          BLOCKS.register("gas_burning_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.GAS_BURNING_GENERATOR, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.GAS, stack -> RateLimitGasTank.createBasicItem(MekanismGeneratorsConfig.generators.gbgTankCapacity,
                            ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                            gas -> gas.has(Fuel.class)
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addGasSlot(0, GasInventorySlot::fill)
                            .addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityBioGenerator, Generator<TileEntityBioGenerator>>, ItemBlockTooltip<BlockTileModel<TileEntityBioGenerator, Generator<TileEntityBioGenerator>>>> BIO_GENERATOR =
          BLOCKS.register("bio_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.BIO_GENERATOR, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder
                      .addAttachmentOnlyContainer(ContainerType.FLUID, stack -> RateLimitFluidTank.createBasicItem(MekanismGeneratorsConfig.generators.bioTankCapacity,
                            BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi,
                            fluidStack -> fluidStack.is(GeneratorTags.Fluids.BIOETHANOL)
                      )).addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack)
                            .addFluidSlot(0, (tank, listener, x, y) -> FluidFuelInventorySlot.forFuel(tank, s -> s.is(MekanismTags.Items.FUELS_BIO) ? 200 : s.is(MekanismTags.Items.FUELS_BLOCK_BIO) ? 200 * 9 : 0,
                                  GeneratorsFluids.BIOETHANOL::getFluidStack,
                                  listener, x, y)
                            ).addEnergy()
                            .build()
                      )
                );
    public static final BlockRegistryObject<BlockTileModel<TileEntityAdvancedSolarGenerator, Generator<TileEntityAdvancedSolarGenerator>>, ItemBlockTooltip<BlockTileModel<TileEntityAdvancedSolarGenerator, Generator<TileEntityAdvancedSolarGenerator>>>> ADVANCED_SOLAR_GENERATOR =
          BLOCKS.register("advanced_solar_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.ADVANCED_SOLAR_GENERATOR, properties -> properties.mapColor(MapColor.COLOR_BLUE)), ItemBlockTooltip::new)
                .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack).addEnergy().build()));
    public static final BlockRegistryObject<BlockTileModel<TileEntityWindGenerator, Generator<TileEntityWindGenerator>>, ItemBlockWindGenerator> WIND_GENERATOR = BLOCKS.register("wind_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.WIND_GENERATOR, properties -> properties.mapColor(MapColor.METAL)), ItemBlockWindGenerator::new)
          .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, stack -> ItemSlotsBuilder.builder(stack).addEnergy().build()));

    public static final BlockRegistryObject<BlockTurbineRotor, ItemBlockTooltip<BlockTurbineRotor>> TURBINE_ROTOR = registerTooltipBlock("turbine_rotor", BlockTurbineRotor::new);
    public static final BlockRegistryObject<BlockTile<TileEntityRotationalComplex, BlockTypeTile<TileEntityRotationalComplex>>, ItemBlockTooltip<BlockTile<TileEntityRotationalComplex, BlockTypeTile<TileEntityRotationalComplex>>>> ROTATIONAL_COMPLEX = registerTooltipBlock("rotational_complex", () -> new BlockTile<>(GeneratorsBlockTypes.ROTATIONAL_COMPLEX, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockTile<TileEntityElectromagneticCoil, BlockTypeTile<TileEntityElectromagneticCoil>>, ItemBlockTooltip<BlockTile<TileEntityElectromagneticCoil, BlockTypeTile<TileEntityElectromagneticCoil>>>> ELECTROMAGNETIC_COIL = registerTooltipBlock("electromagnetic_coil", () -> new BlockTile<>(GeneratorsBlockTypes.ELECTROMAGNETIC_COIL, properties -> properties.mapColor(MapColor.COLOR_BLACK)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityTurbineCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityTurbineCasing>>> TURBINE_CASING = registerTooltipBlock("turbine_casing", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.TURBINE_CASING, properties -> properties.mapColor(MapColor.CLAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityTurbineValve>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityTurbineValve>>> TURBINE_VALVE = registerTooltipBlock("turbine_valve", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.TURBINE_VALVE, properties -> properties.mapColor(MapColor.CLAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityTurbineVent>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityTurbineVent>>> TURBINE_VENT = registerTooltipBlock("turbine_vent", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.TURBINE_VENT, properties -> properties.mapColor(MapColor.COLOR_GRAY)));
    public static final BlockRegistryObject<BlockTile<TileEntitySaturatingCondenser, BlockTypeTile<TileEntitySaturatingCondenser>>, ItemBlockTooltip<BlockTile<TileEntitySaturatingCondenser, BlockTypeTile<TileEntitySaturatingCondenser>>>> SATURATING_CONDENSER = registerTooltipBlock("saturating_condenser", () -> new BlockTile<>(GeneratorsBlockTypes.SATURATING_CONDENSER, properties -> properties.mapColor(MapColor.COLOR_GRAY)));

    public static final BlockRegistryObject<BlockStructuralGlass<TileEntityReactorGlass>, ItemBlockTooltip<BlockStructuralGlass<TileEntityReactorGlass>>> REACTOR_GLASS = registerTooltipBlock("reactor_glass", () -> new BlockStructuralGlass<>(GeneratorsBlockTypes.REACTOR_GLASS));

    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFissionReactorCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFissionReactorCasing>>> FISSION_REACTOR_CASING = registerTooltipBlock("fission_reactor_casing", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FISSION_REACTOR_CASING, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFissionReactorPort>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFissionReactorPort>>> FISSION_REACTOR_PORT = registerTooltipBlock("fission_reactor_port", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FISSION_REACTOR_PORT, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFissionReactorLogicAdapter>, ItemBlockFissionLogicAdapter> FISSION_REACTOR_LOGIC_ADAPTER = BLOCKS.registerDefaultProperties("fission_reactor_logic_adapter", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FISSION_REACTOR_LOGIC_ADAPTER, properties -> properties.mapColor(MapColor.COLOR_LIGHT_GRAY)), ItemBlockFissionLogicAdapter::new);
    public static final BlockRegistryObject<BlockTileModel<TileEntityFissionFuelAssembly, BlockTypeTile<TileEntityFissionFuelAssembly>>, ItemBlockTooltip<BlockTileModel<TileEntityFissionFuelAssembly, BlockTypeTile<TileEntityFissionFuelAssembly>>>> FISSION_FUEL_ASSEMBLY = registerTooltipBlock("fission_fuel_assembly", () -> new BlockTileModel<>(GeneratorsBlockTypes.FISSION_FUEL_ASSEMBLY, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor())));
    public static final BlockRegistryObject<BlockTileModel<TileEntityControlRodAssembly, BlockTypeTile<TileEntityControlRodAssembly>>, ItemBlockTooltip<BlockTileModel<TileEntityControlRodAssembly, BlockTypeTile<TileEntityControlRodAssembly>>>> CONTROL_ROD_ASSEMBLY = registerTooltipBlock("control_rod_assembly", () -> new BlockTileModel<>(GeneratorsBlockTypes.CONTROL_ROD_ASSEMBLY, properties -> properties.mapColor(MapColor.METAL)));

    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFusionReactorController>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFusionReactorController>>> FUSION_REACTOR_CONTROLLER = registerTooltipBlock("fusion_reactor_controller", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FUSION_REACTOR_CONTROLLER, properties -> properties.mapColor(MapColor.COLOR_ORANGE)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFusionReactorBlock>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFusionReactorBlock>>> FUSION_REACTOR_FRAME = registerTooltipBlock("fusion_reactor_frame", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FUSION_REACTOR_FRAME, properties -> properties.mapColor(MapColor.TERRACOTTA_BROWN)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFusionReactorPort>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFusionReactorPort>>> FUSION_REACTOR_PORT = registerTooltipBlock("fusion_reactor_port", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FUSION_REACTOR_PORT, properties -> properties.mapColor(MapColor.TERRACOTTA_BROWN)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFusionReactorLogicAdapter>, ItemBlockFusionLogicAdapter> FUSION_REACTOR_LOGIC_ADAPTER = BLOCKS.registerDefaultProperties("fusion_reactor_logic_adapter", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FUSION_REACTOR_LOGIC_ADAPTER, properties -> properties.mapColor(MapColor.TERRACOTTA_BROWN)), ItemBlockFusionLogicAdapter::new);
    public static final BlockRegistryObject<BlockLaserFocusMatrix, ItemBlockTooltip<BlockLaserFocusMatrix>> LASER_FOCUS_MATRIX = registerTooltipBlock("laser_focus_matrix", BlockLaserFocusMatrix::new);

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerTooltipBlock(String name, Supplier<BLOCK> blockCreator) {
        return BLOCKS.registerDefaultProperties(name, blockCreator, ItemBlockTooltip::new);
    }
}