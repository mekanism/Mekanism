package mekanism.generators.common.registries;

import java.util.function.Supplier;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.machine.prefab.BlockTile;
import mekanism.common.block.machine.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.generators.client.render.item.GeneratorsISTERProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.reactor.BlockLaserFocusMatrix;
import mekanism.generators.common.block.reactor.BlockReactorGlass;
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
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorFrame;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorPort;
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
    public static final BlockRegistryObject<BlockTile<TileEntityTurbineCasing, BlockTypeTile<TileEntityTurbineCasing>>, ItemBlockTooltip<BlockTile<TileEntityTurbineCasing, BlockTypeTile<TileEntityTurbineCasing>>>> TURBINE_CASING = registerTooltipBlock("turbine_casing", () -> new BlockTile<>(GeneratorsBlockTypes.TURBINE_CASING));
    public static final BlockRegistryObject<BlockTile<TileEntityTurbineValve, BlockTypeTile<TileEntityTurbineValve>>, ItemBlockTooltip<BlockTile<TileEntityTurbineValve, BlockTypeTile<TileEntityTurbineValve>>>> TURBINE_VALVE = registerTooltipBlock("turbine_valve", () -> new BlockTile<>(GeneratorsBlockTypes.TURBINE_VALVE));
    public static final BlockRegistryObject<BlockTile<TileEntityTurbineVent, BlockTypeTile<TileEntityTurbineVent>>, ItemBlockTooltip<BlockTile<TileEntityTurbineVent, BlockTypeTile<TileEntityTurbineVent>>>> TURBINE_VENT = registerTooltipBlock("turbine_vent", () -> new BlockTile<>(GeneratorsBlockTypes.TURBINE_VENT));
    public static final BlockRegistryObject<BlockTile<TileEntitySaturatingCondenser, BlockTypeTile<TileEntitySaturatingCondenser>>, ItemBlockTooltip<BlockTile<TileEntitySaturatingCondenser, BlockTypeTile<TileEntitySaturatingCondenser>>>> SATURATING_CONDENSER = registerTooltipBlock("saturating_condenser", () -> new BlockTile<>(GeneratorsBlockTypes.SATURATING_CONDENSER));
    public static final BlockRegistryObject<BlockTile<TileEntityReactorController, BlockTypeTile<TileEntityReactorController>>, ItemBlockTooltip<BlockTile<TileEntityReactorController, BlockTypeTile<TileEntityReactorController>>>> REACTOR_CONTROLLER = registerTooltipBlock("reactor_controller", () -> new BlockTile<>(GeneratorsBlockTypes.REACTOR_CONTROLLER));
    public static final BlockRegistryObject<BlockTile<TileEntityReactorFrame, BlockTypeTile<TileEntityReactorFrame>>, ItemBlockTooltip<BlockTile<TileEntityReactorFrame, BlockTypeTile<TileEntityReactorFrame>>>> REACTOR_FRAME = registerTooltipBlock("reactor_frame", () -> new BlockTile<>(GeneratorsBlockTypes.REACTOR_FRAME));
    public static final BlockRegistryObject<BlockTile<TileEntityReactorPort, BlockTypeTile<TileEntityReactorPort>>, ItemBlockTooltip<BlockTile<TileEntityReactorPort, BlockTypeTile<TileEntityReactorPort>>>> REACTOR_PORT = registerTooltipBlock("reactor_port", () -> new BlockTile<>(GeneratorsBlockTypes.REACTOR_PORT));
    public static final BlockRegistryObject<BlockTile<TileEntityReactorLogicAdapter, BlockTypeTile<TileEntityReactorLogicAdapter>>, ItemBlockTooltip<BlockTile<TileEntityReactorLogicAdapter, BlockTypeTile<TileEntityReactorLogicAdapter>>>> REACTOR_LOGIC_ADAPTER = registerTooltipBlock("reactor_logic_adapter", () -> new BlockTile<>(GeneratorsBlockTypes.REACTOR_LOGIC_ADAPTER));
    public static final BlockRegistryObject<BlockReactorGlass, ItemBlockTooltip<BlockReactorGlass>> REACTOR_GLASS = registerTooltipBlock("reactor_glass", BlockReactorGlass::new);
    public static final BlockRegistryObject<BlockLaserFocusMatrix, ItemBlockTooltip<BlockLaserFocusMatrix>> LASER_FOCUS_MATRIX = registerTooltipBlock("laser_focus_matrix", BlockLaserFocusMatrix::new);

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerTooltipBlock(String name, Supplier<BLOCK> blockCreator) {
        return BLOCKS.register(name, blockCreator, ItemBlockTooltip::new);
    }
}