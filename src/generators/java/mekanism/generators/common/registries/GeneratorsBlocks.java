package mekanism.generators.common.registries;

import java.util.function.Supplier;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.machine.prefab.BlockBase;
import mekanism.common.block.machine.prefab.BlockBase.BlockMachineModel;
import mekanism.common.content.blocktype.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.generators.client.render.item.GeneratorsISTERProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.reactor.BlockLaserFocusMatrix;
import mekanism.generators.common.block.reactor.BlockReactorGlass;
import mekanism.generators.common.block.reactor.BlockReactorLogicAdapter;
import mekanism.generators.common.block.turbine.BlockElectromagneticCoil;
import mekanism.generators.common.block.turbine.BlockRotationalComplex;
import mekanism.generators.common.block.turbine.BlockSaturatingCondenser;
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
import mekanism.generators.common.tile.reactor.TileEntityReactorPort;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.block.Block;

public class GeneratorsBlocks {

    //TODO: Lang files had a string for a Neutron Capture Plate with the description:
    // A block that can be used to both block Fusion Reactor radiation and assist in the production of Tritium.
    public static BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismGenerators.MODID);

    public static final BlockRegistryObject<BlockMachineModel<TileEntityHeatGenerator, Generator<TileEntityHeatGenerator>>, ItemBlockMachine> HEAT_GENERATOR = BLOCKS.register("heat_generator", () -> new BlockMachineModel<>(GeneratorsBlockTypes.HEAT_GENERATOR), (block) -> new ItemBlockMachine(block, GeneratorsISTERProvider::heat));
    public static final BlockRegistryObject<BlockMachineModel<TileEntitySolarGenerator, Generator<TileEntitySolarGenerator>>, ItemBlockMachine> SOLAR_GENERATOR = BLOCKS.register("solar_generator", () -> new BlockMachineModel<>(GeneratorsBlockTypes.SOLAR_GENERATOR), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockMachineModel<TileEntityGasGenerator, Generator<TileEntityGasGenerator>>, ItemBlockMachine> GAS_BURNING_GENERATOR = BLOCKS.register("gas_burning_generator", () -> new BlockMachineModel<>(GeneratorsBlockTypes.GAS_BURNING_GENERATOR), (block) -> new ItemBlockMachine(block, GeneratorsISTERProvider::gasBurning));
    public static final BlockRegistryObject<BlockMachineModel<TileEntityBioGenerator, Generator<TileEntityBioGenerator>>, ItemBlockMachine> BIO_GENERATOR = BLOCKS.register("bio_generator", () -> new BlockMachineModel<>(GeneratorsBlockTypes.BIO_GENERATOR), (block) -> new ItemBlockMachine(block, GeneratorsISTERProvider::bio));
    public static final BlockRegistryObject<BlockMachineModel<TileEntityAdvancedSolarGenerator, Generator<TileEntityAdvancedSolarGenerator>>, ItemBlockAdvancedSolarGenerator> ADVANCED_SOLAR_GENERATOR = BLOCKS.register("advanced_solar_generator", () -> new BlockMachineModel<>(GeneratorsBlockTypes.ADVANCED_SOLAR_GENERATOR), ItemBlockAdvancedSolarGenerator::new);
    public static final BlockRegistryObject<BlockMachineModel<TileEntityWindGenerator, Generator<TileEntityWindGenerator>>, ItemBlockWindGenerator> WIND_GENERATOR = BLOCKS.register("wind_generator", () -> new BlockMachineModel<>(GeneratorsBlockTypes.WIND_GENERATOR), ItemBlockWindGenerator::new);
    public static final BlockRegistryObject<BlockTurbineRotor, ItemBlockTooltip<BlockTurbineRotor>> TURBINE_ROTOR = registerTooltipBlock("turbine_rotor", BlockTurbineRotor::new);
    public static final BlockRegistryObject<BlockRotationalComplex, ItemBlockTooltip<BlockRotationalComplex>> ROTATIONAL_COMPLEX = registerTooltipBlock("rotational_complex", BlockRotationalComplex::new);
    public static final BlockRegistryObject<BlockElectromagneticCoil, ItemBlockTooltip<BlockElectromagneticCoil>> ELECTROMAGNETIC_COIL = registerTooltipBlock("electromagnetic_coil", BlockElectromagneticCoil::new);
    public static final BlockRegistryObject<BlockBase<TileEntityTurbineCasing, BlockTile<TileEntityTurbineCasing>>, ItemBlockTooltip<BlockBase<TileEntityTurbineCasing, BlockTile<TileEntityTurbineCasing>>>> TURBINE_CASING = registerTooltipBlock("turbine_casing", () -> new BlockBase<>(GeneratorsBlockTypes.TURBINE_CASING));
    public static final BlockRegistryObject<BlockBase<TileEntityTurbineValve, BlockTile<TileEntityTurbineValve>>, ItemBlockTooltip<BlockBase<TileEntityTurbineValve, BlockTile<TileEntityTurbineValve>>>> TURBINE_VALVE = registerTooltipBlock("turbine_valve", () -> new BlockBase<>(GeneratorsBlockTypes.TURBINE_VALVE));
    public static final BlockRegistryObject<BlockBase<TileEntityTurbineVent, BlockTile<TileEntityTurbineVent>>, ItemBlockTooltip<BlockBase<TileEntityTurbineVent, BlockTile<TileEntityTurbineVent>>>> TURBINE_VENT = registerTooltipBlock("turbine_vent", () -> new BlockBase<>(GeneratorsBlockTypes.TURBINE_VENT));
    public static final BlockRegistryObject<BlockSaturatingCondenser, ItemBlockTooltip<BlockSaturatingCondenser>> SATURATING_CONDENSER = registerTooltipBlock("saturating_condenser", BlockSaturatingCondenser::new);
    public static final BlockRegistryObject<BlockBase<TileEntityReactorController, BlockTile<TileEntityReactorController>>, ItemBlockTooltip<BlockBase<TileEntityReactorController, BlockTile<TileEntityReactorController>>>> REACTOR_CONTROLLER = registerTooltipBlock("reactor_controller", () -> new BlockBase<>(GeneratorsBlockTypes.REACTOR_CONTROLLER));
    public static final BlockRegistryObject<BlockBase<TileEntityReactorFrame, BlockTile<TileEntityReactorFrame>>, ItemBlockTooltip<BlockBase<TileEntityReactorFrame, BlockTile<TileEntityReactorFrame>>>> REACTOR_FRAME = registerTooltipBlock("reactor_frame", () -> new BlockBase<>(GeneratorsBlockTypes.REACTOR_FRAME));
    public static final BlockRegistryObject<BlockBase<TileEntityReactorPort, BlockTile<TileEntityReactorPort>>, ItemBlockTooltip<BlockBase<TileEntityReactorPort, BlockTile<TileEntityReactorPort>>>> REACTOR_PORT = registerTooltipBlock("reactor_port", () -> new BlockBase<>(GeneratorsBlockTypes.REACTOR_PORT));
    public static final BlockRegistryObject<BlockReactorLogicAdapter, ItemBlockTooltip<BlockReactorLogicAdapter>> REACTOR_LOGIC_ADAPTER = registerTooltipBlock("reactor_logic_adapter", BlockReactorLogicAdapter::new);
    public static final BlockRegistryObject<BlockReactorGlass, ItemBlockTooltip<BlockReactorGlass>> REACTOR_GLASS = registerTooltipBlock("reactor_glass", BlockReactorGlass::new);
    public static final BlockRegistryObject<BlockLaserFocusMatrix, ItemBlockTooltip<BlockLaserFocusMatrix>> LASER_FOCUS_MATRIX = registerTooltipBlock("laser_focus_matrix", BlockLaserFocusMatrix::new);

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerTooltipBlock(String name, Supplier<BLOCK> blockCreator) {
        return BLOCKS.register(name, blockCreator, ItemBlockTooltip::new);
    }
}