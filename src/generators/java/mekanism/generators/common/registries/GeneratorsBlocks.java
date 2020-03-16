package mekanism.generators.common.registries;

import java.util.function.Supplier;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.generators.client.render.item.GeneratorsISTERProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.BlockGenerator;
import mekanism.generators.common.block.reactor.BlockLaserFocusMatrix;
import mekanism.generators.common.block.reactor.BlockReactorController;
import mekanism.generators.common.block.reactor.BlockReactorFrame;
import mekanism.generators.common.block.reactor.BlockReactorGlass;
import mekanism.generators.common.block.reactor.BlockReactorLogicAdapter;
import mekanism.generators.common.block.reactor.BlockReactorPort;
import mekanism.generators.common.block.turbine.BlockElectromagneticCoil;
import mekanism.generators.common.block.turbine.BlockRotationalComplex;
import mekanism.generators.common.block.turbine.BlockSaturatingCondenser;
import mekanism.generators.common.block.turbine.BlockTurbineCasing;
import mekanism.generators.common.block.turbine.BlockTurbineRotor;
import mekanism.generators.common.block.turbine.BlockTurbineValve;
import mekanism.generators.common.block.turbine.BlockTurbineVent;
import mekanism.generators.common.item.generator.ItemBlockAdvancedSolarGenerator;
import mekanism.generators.common.item.generator.ItemBlockGenerator;
import mekanism.generators.common.item.generator.ItemBlockTurbineCasing;
import mekanism.generators.common.item.generator.ItemBlockTurbineValve;
import mekanism.generators.common.item.generator.ItemBlockTurbineVent;
import mekanism.generators.common.item.generator.ItemBlockWindGenerator;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.block.Block;

public class GeneratorsBlocks {

    //TODO: Lang files had a string for a Neutron Capture Plate with the description:
    // A block that can be used to both block Fusion Reactor radiation and assist in the production of Tritium.
    public static BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismGenerators.MODID);

    public static final BlockRegistryObject<BlockGenerator<TileEntityHeatGenerator>, ItemBlockGenerator> HEAT_GENERATOR = BLOCKS.register("heat_generator", () -> new BlockGenerator<>(GeneratorsBlockTypes.HEAT_GENERATOR), (block) -> new ItemBlockGenerator(block, GeneratorsISTERProvider::heat));
    public static final BlockRegistryObject<BlockGenerator<TileEntitySolarGenerator>, ItemBlockGenerator> SOLAR_GENERATOR = BLOCKS.register("solar_generator", () -> new BlockGenerator<>(GeneratorsBlockTypes.SOLAR_GENERATOR), ItemBlockGenerator::new);
    public static final BlockRegistryObject<BlockGenerator<TileEntityGasGenerator>, ItemBlockGenerator> GAS_BURNING_GENERATOR = BLOCKS.register("gas_burning_generator", () -> new BlockGenerator<>(GeneratorsBlockTypes.GAS_BURNING_GENERATOR), (block) -> new ItemBlockGenerator(block, GeneratorsISTERProvider::gasBurning));
    public static final BlockRegistryObject<BlockGenerator<TileEntityBioGenerator>, ItemBlockGenerator> BIO_GENERATOR = BLOCKS.register("bio_generator", () -> new BlockGenerator<>(GeneratorsBlockTypes.BIO_GENERATOR), (block) -> new ItemBlockGenerator(block, GeneratorsISTERProvider::bio));
    public static final BlockRegistryObject<BlockGenerator<TileEntityAdvancedSolarGenerator>, ItemBlockAdvancedSolarGenerator> ADVANCED_SOLAR_GENERATOR = BLOCKS.register("advanced_solar_generator", () -> new BlockGenerator<>(GeneratorsBlockTypes.ADVANCED_SOLAR_GENERATOR), ItemBlockAdvancedSolarGenerator::new);
    public static final BlockRegistryObject<BlockGenerator<TileEntityWindGenerator>, ItemBlockWindGenerator> WIND_GENERATOR = BLOCKS.register("wind_generator", () -> new BlockGenerator<>(GeneratorsBlockTypes.WIND_GENERATOR), ItemBlockWindGenerator::new);
    public static final BlockRegistryObject<BlockTurbineRotor, ItemBlockTooltip<BlockTurbineRotor>> TURBINE_ROTOR = registerTooltipBlock("turbine_rotor", BlockTurbineRotor::new);
    public static final BlockRegistryObject<BlockRotationalComplex, ItemBlockTooltip<BlockRotationalComplex>> ROTATIONAL_COMPLEX = registerTooltipBlock("rotational_complex", BlockRotationalComplex::new);
    public static final BlockRegistryObject<BlockElectromagneticCoil, ItemBlockTooltip<BlockElectromagneticCoil>> ELECTROMAGNETIC_COIL = registerTooltipBlock("electromagnetic_coil", BlockElectromagneticCoil::new);
    public static final BlockRegistryObject<BlockTurbineCasing, ItemBlockTurbineCasing> TURBINE_CASING = BLOCKS.register("turbine_casing", BlockTurbineCasing::new, ItemBlockTurbineCasing::new);
    public static final BlockRegistryObject<BlockTurbineValve, ItemBlockTurbineValve> TURBINE_VALVE = BLOCKS.register("turbine_valve", BlockTurbineValve::new, ItemBlockTurbineValve::new);
    public static final BlockRegistryObject<BlockTurbineVent, ItemBlockTurbineVent> TURBINE_VENT = BLOCKS.register("turbine_vent", BlockTurbineVent::new, ItemBlockTurbineVent::new);
    public static final BlockRegistryObject<BlockSaturatingCondenser, ItemBlockTooltip<BlockSaturatingCondenser>> SATURATING_CONDENSER = registerTooltipBlock("saturating_condenser", BlockSaturatingCondenser::new);
    public static final BlockRegistryObject<BlockReactorController, ItemBlockTooltip<BlockReactorController>> REACTOR_CONTROLLER = registerTooltipBlock("reactor_controller", BlockReactorController::new);
    public static final BlockRegistryObject<BlockReactorFrame, ItemBlockTooltip<BlockReactorFrame>> REACTOR_FRAME = registerTooltipBlock("reactor_frame", BlockReactorFrame::new);
    public static final BlockRegistryObject<BlockReactorPort, ItemBlockTooltip<BlockReactorPort>> REACTOR_PORT = registerTooltipBlock("reactor_port", BlockReactorPort::new);
    public static final BlockRegistryObject<BlockReactorLogicAdapter, ItemBlockTooltip<BlockReactorLogicAdapter>> REACTOR_LOGIC_ADAPTER = registerTooltipBlock("reactor_logic_adapter", BlockReactorLogicAdapter::new);
    public static final BlockRegistryObject<BlockReactorGlass, ItemBlockTooltip<BlockReactorGlass>> REACTOR_GLASS = registerTooltipBlock("reactor_glass", BlockReactorGlass::new);
    public static final BlockRegistryObject<BlockLaserFocusMatrix, ItemBlockTooltip<BlockLaserFocusMatrix>> LASER_FOCUS_MATRIX = registerTooltipBlock("laser_focus_matrix", BlockLaserFocusMatrix::new);

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerTooltipBlock(String name, Supplier<BLOCK> blockCreator) {
        return BLOCKS.register(name, blockCreator, ItemBlockTooltip::new);
    }
}