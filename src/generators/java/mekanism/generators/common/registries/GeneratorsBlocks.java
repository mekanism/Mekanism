package mekanism.generators.common.registries;

import java.util.function.Supplier;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.machine.prefab.BlockBasicMultiblock;
import mekanism.common.block.machine.prefab.BlockTile;
import mekanism.common.block.machine.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.generators.client.render.item.GeneratorsISTERProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.fusion.BlockFusionReactorGlass;
import mekanism.generators.common.block.fusion.BlockLaserFocusMatrix;
import mekanism.generators.common.block.turbine.BlockTurbineRotor;
import mekanism.generators.common.content.blocktype.Generator;
import mekanism.generators.common.item.generator.ItemBlockAdvancedSolarGenerator;
import mekanism.generators.common.item.generator.ItemBlockWindGenerator;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import mekanism.generators.common.tile.fission.TileEntityControlRodAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionFuelAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorPort;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorFrame;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorPort;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.block.Block;

public class GeneratorsBlocks {

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismGenerators.MODID);

    public static final BlockRegistryObject<BlockTileModel<TileEntityHeatGenerator, Generator<TileEntityHeatGenerator>>, ItemBlockMachine> HEAT_GENERATOR = BLOCKS.register("heat_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.HEAT_GENERATOR), (block) -> new ItemBlockMachine(block, GeneratorsISTERProvider::heat));
    public static final BlockRegistryObject<BlockTileModel<TileEntitySolarGenerator, Generator<TileEntitySolarGenerator>>, ItemBlockMachine> SOLAR_GENERATOR = BLOCKS.register("solar_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.SOLAR_GENERATOR), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockTileModel<TileEntityGasGenerator, Generator<TileEntityGasGenerator>>, ItemBlockMachine> GAS_BURNING_GENERATOR = BLOCKS.register("gas_burning_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.GAS_BURNING_GENERATOR), (block) -> new ItemBlockMachine(block, GeneratorsISTERProvider::gasBurning));
    public static final BlockRegistryObject<BlockTileModel<TileEntityBioGenerator, Generator<TileEntityBioGenerator>>, ItemBlockMachine> BIO_GENERATOR = BLOCKS.register("bio_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.BIO_GENERATOR), (block) -> new ItemBlockMachine(block, GeneratorsISTERProvider::bio));
    public static final BlockRegistryObject<BlockTileModel<TileEntityAdvancedSolarGenerator, Generator<TileEntityAdvancedSolarGenerator>>, ItemBlockAdvancedSolarGenerator> ADVANCED_SOLAR_GENERATOR = BLOCKS.register("advanced_solar_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.ADVANCED_SOLAR_GENERATOR), ItemBlockAdvancedSolarGenerator::new);
    public static final BlockRegistryObject<BlockTileModel<TileEntityWindGenerator, Generator<TileEntityWindGenerator>>, ItemBlockWindGenerator> WIND_GENERATOR = BLOCKS.register("wind_generator", () -> new BlockTileModel<>(GeneratorsBlockTypes.WIND_GENERATOR), ItemBlockWindGenerator::new);

    public static final BlockRegistryObject<BlockTurbineRotor, ItemBlockTooltip<BlockTurbineRotor>> TURBINE_ROTOR = registerTooltipBlock("turbine_rotor", BlockTurbineRotor::new);
    public static final BlockRegistryObject<BlockTile<TileEntityRotationalComplex, BlockTypeTile<TileEntityRotationalComplex>>, ItemBlockTooltip<BlockTile<TileEntityRotationalComplex, BlockTypeTile<TileEntityRotationalComplex>>>> ROTATIONAL_COMPLEX = registerTooltipBlock("rotational_complex", () -> new BlockTile<>(GeneratorsBlockTypes.ROTATIONAL_COMPLEX));
    public static final BlockRegistryObject<BlockTile<TileEntityElectromagneticCoil, BlockTypeTile<TileEntityElectromagneticCoil>>, ItemBlockTooltip<BlockTile<TileEntityElectromagneticCoil, BlockTypeTile<TileEntityElectromagneticCoil>>>> ELECTROMAGNETIC_COIL = registerTooltipBlock("electromagnetic_coil", () -> new BlockTile<>(GeneratorsBlockTypes.ELECTROMAGNETIC_COIL));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityTurbineCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityTurbineCasing>>> TURBINE_CASING = registerTooltipBlock("turbine_casing", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.TURBINE_CASING));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityTurbineValve>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityTurbineValve>>> TURBINE_VALVE = registerTooltipBlock("turbine_valve", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.TURBINE_VALVE));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityTurbineVent>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityTurbineVent>>> TURBINE_VENT = registerTooltipBlock("turbine_vent", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.TURBINE_VENT));
    public static final BlockRegistryObject<BlockTile<TileEntitySaturatingCondenser, BlockTypeTile<TileEntitySaturatingCondenser>>, ItemBlockTooltip<BlockTile<TileEntitySaturatingCondenser, BlockTypeTile<TileEntitySaturatingCondenser>>>> SATURATING_CONDENSER = registerTooltipBlock("saturating_condenser", () -> new BlockTile<>(GeneratorsBlockTypes.SATURATING_CONDENSER));

    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFissionReactorCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFissionReactorCasing>>> FISSION_REACTOR_CASING = registerTooltipBlock("fission_reactor_casing", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FISSION_REACTOR_CASING));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFissionReactorPort>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFissionReactorPort>>> FISSION_REACTOR_PORT = registerTooltipBlock("fission_reactor_port", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FISSION_REACTOR_PORT));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityFissionReactorLogicAdapter>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityFissionReactorLogicAdapter>>> FISSION_REACTOR_LOGIC_ADAPTER = registerTooltipBlock("fission_reactor_logic_adapter", () -> new BlockBasicMultiblock<>(GeneratorsBlockTypes.FISSION_REACTOR_LOGIC_ADAPTER));
    public static final BlockRegistryObject<BlockTileModel<TileEntityFissionFuelAssembly, BlockTypeTile<TileEntityFissionFuelAssembly>>, ItemBlockTooltip<BlockTileModel<TileEntityFissionFuelAssembly, BlockTypeTile<TileEntityFissionFuelAssembly>>>> FISSION_FUEL_ASSEMBLY = registerTooltipBlock("fission_fuel_assembly", () -> new BlockTileModel<>(GeneratorsBlockTypes.FISSION_FUEL_ASSEMBLY));
    public static final BlockRegistryObject<BlockTileModel<TileEntityControlRodAssembly, BlockTypeTile<TileEntityControlRodAssembly>>, ItemBlockTooltip<BlockTileModel<TileEntityControlRodAssembly, BlockTypeTile<TileEntityControlRodAssembly>>>> CONTROL_ROD_ASSEMBLY = registerTooltipBlock("control_rod_assembly", () -> new BlockTileModel<>(GeneratorsBlockTypes.CONTROL_ROD_ASSEMBLY));

    public static final BlockRegistryObject<BlockTile<TileEntityFusionReactorController, BlockTypeTile<TileEntityFusionReactorController>>, ItemBlockTooltip<BlockTile<TileEntityFusionReactorController, BlockTypeTile<TileEntityFusionReactorController>>>> FUSION_REACTOR_CONTROLLER = registerTooltipBlock("fusion_reactor_controller", () -> new BlockTile<>(GeneratorsBlockTypes.FUSION_REACTOR_CONTROLLER));
    public static final BlockRegistryObject<BlockTile<TileEntityFusionReactorFrame, BlockTypeTile<TileEntityFusionReactorFrame>>, ItemBlockTooltip<BlockTile<TileEntityFusionReactorFrame, BlockTypeTile<TileEntityFusionReactorFrame>>>> FUSION_REACTOR_FRAME = registerTooltipBlock("fusion_reactor_frame", () -> new BlockTile<>(GeneratorsBlockTypes.FUSION_REACTOR_FRAME));
    public static final BlockRegistryObject<BlockTile<TileEntityFusionReactorPort, BlockTypeTile<TileEntityFusionReactorPort>>, ItemBlockTooltip<BlockTile<TileEntityFusionReactorPort, BlockTypeTile<TileEntityFusionReactorPort>>>> FUSION_REACTOR_PORT = registerTooltipBlock("fusion_reactor_port", () -> new BlockTile<>(GeneratorsBlockTypes.FUSION_REACTOR_PORT));
    public static final BlockRegistryObject<BlockTile<TileEntityFusionReactorLogicAdapter, BlockTypeTile<TileEntityFusionReactorLogicAdapter>>, ItemBlockTooltip<BlockTile<TileEntityFusionReactorLogicAdapter, BlockTypeTile<TileEntityFusionReactorLogicAdapter>>>> FUSION_REACTOR_LOGIC_ADAPTER = registerTooltipBlock("fusion_reactor_logic_adapter", () -> new BlockTile<>(GeneratorsBlockTypes.FUSION_REACTOR_LOGIC_ADAPTER));
    public static final BlockRegistryObject<BlockFusionReactorGlass, ItemBlockTooltip<BlockFusionReactorGlass>> FUSION_REACTOR_GLASS = registerTooltipBlock("fusion_reactor_glass", BlockFusionReactorGlass::new);
    public static final BlockRegistryObject<BlockLaserFocusMatrix, ItemBlockTooltip<BlockLaserFocusMatrix>> LASER_FOCUS_MATRIX = registerTooltipBlock("laser_focus_matrix", BlockLaserFocusMatrix::new);

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerTooltipBlock(String name, Supplier<BLOCK> blockCreator) {
        return BLOCKS.registerDefaultProperties(name, blockCreator, ItemBlockTooltip::new);
    }
}