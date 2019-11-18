package mekanism.generators.common;

import java.util.function.Supplier;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.generators.common.block.BlockAdvancedSolarGenerator;
import mekanism.generators.common.block.BlockBioGenerator;
import mekanism.generators.common.block.BlockGasBurningGenerator;
import mekanism.generators.common.block.BlockHeatGenerator;
import mekanism.generators.common.block.BlockSolarGenerator;
import mekanism.generators.common.block.BlockWindGenerator;
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
import mekanism.generators.common.item.generator.ItemBlockBioGenerator;
import mekanism.generators.common.item.generator.ItemBlockGasBurningGenerator;
import mekanism.generators.common.item.generator.ItemBlockHeatGenerator;
import mekanism.generators.common.item.generator.ItemBlockSolarGenerator;
import mekanism.generators.common.item.generator.ItemBlockTurbineCasing;
import mekanism.generators.common.item.generator.ItemBlockTurbineValve;
import mekanism.generators.common.item.generator.ItemBlockTurbineVent;
import mekanism.generators.common.item.generator.ItemBlockWindGenerator;
import net.minecraft.block.Block;

public class GeneratorsBlock {

    public static BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismGenerators.MODID);

    public static final BlockRegistryObject<BlockHeatGenerator, ItemBlockHeatGenerator> HEAT_GENERATOR = BLOCKS.register("heat_generator", BlockHeatGenerator::new, ItemBlockHeatGenerator::new);
    public static final BlockRegistryObject<BlockSolarGenerator, ItemBlockSolarGenerator> SOLAR_GENERATOR = BLOCKS.register("solar_generator", BlockSolarGenerator::new, ItemBlockSolarGenerator::new);
    public static final BlockRegistryObject<BlockGasBurningGenerator, ItemBlockGasBurningGenerator> GAS_BURNING_GENERATOR = BLOCKS.register("gas_burning_generator", BlockGasBurningGenerator::new, ItemBlockGasBurningGenerator::new);
    public static final BlockRegistryObject<BlockBioGenerator, ItemBlockBioGenerator> BIO_GENERATOR = BLOCKS.register("bio_generator", BlockBioGenerator::new, ItemBlockBioGenerator::new);
    public static final BlockRegistryObject<BlockAdvancedSolarGenerator, ItemBlockAdvancedSolarGenerator> ADVANCED_SOLAR_GENERATOR = BLOCKS.register("advanced_solar_generator", BlockAdvancedSolarGenerator::new, ItemBlockAdvancedSolarGenerator::new);
    public static final BlockRegistryObject<BlockWindGenerator, ItemBlockWindGenerator> WIND_GENERATOR = BLOCKS.register("wind_generator", BlockWindGenerator::new, ItemBlockWindGenerator::new);
    public static final BlockRegistryObject<BlockTurbineRotor, ItemBlockTooltip> TURBINE_ROTOR = registerTooltipBlock("turbine_rotor", BlockTurbineRotor::new);
    public static final BlockRegistryObject<BlockRotationalComplex, ItemBlockTooltip> ROTATIONAL_COMPLEX = registerTooltipBlock("rotational_complex", BlockRotationalComplex::new);
    public static final BlockRegistryObject<BlockElectromagneticCoil, ItemBlockTooltip> ELECTROMAGNETIC_COIL = registerTooltipBlock("electromagnetic_coil", BlockElectromagneticCoil::new);
    public static final BlockRegistryObject<BlockTurbineCasing, ItemBlockTurbineCasing> TURBINE_CASING = BLOCKS.register("turbine_casing", BlockTurbineCasing::new, ItemBlockTurbineCasing::new);
    public static final BlockRegistryObject<BlockTurbineValve, ItemBlockTurbineValve> TURBINE_VALVE = BLOCKS.register("turbine_valve", BlockTurbineValve::new, ItemBlockTurbineValve::new);
    public static final BlockRegistryObject<BlockTurbineVent, ItemBlockTurbineVent> TURBINE_VENT = BLOCKS.register("turbine_vent", BlockTurbineVent::new, ItemBlockTurbineVent::new);
    public static final BlockRegistryObject<BlockSaturatingCondenser, ItemBlockTooltip> SATURATING_CONDENSER = registerTooltipBlock("saturating_condenser", BlockSaturatingCondenser::new);
    public static final BlockRegistryObject<BlockReactorController, ItemBlockTooltip> REACTOR_CONTROLLER = registerTooltipBlock("reactor_controller", BlockReactorController::new);
    public static final BlockRegistryObject<BlockReactorFrame, ItemBlockTooltip> REACTOR_FRAME = registerTooltipBlock("reactor_frame", BlockReactorFrame::new);
    public static final BlockRegistryObject<BlockReactorPort, ItemBlockTooltip> REACTOR_PORT = registerTooltipBlock("reactor_port", BlockReactorPort::new);
    public static final BlockRegistryObject<BlockReactorLogicAdapter, ItemBlockTooltip> REACTOR_LOGIC_ADAPTER = registerTooltipBlock("reactor_logic_adapter", BlockReactorLogicAdapter::new);
    public static final BlockRegistryObject<BlockReactorGlass, ItemBlockTooltip> REACTOR_GLASS = registerTooltipBlock("reactor_glass", BlockReactorGlass::new);
    public static final BlockRegistryObject<BlockLaserFocusMatrix, ItemBlockTooltip> LASER_FOCUS_MATRIX = registerTooltipBlock("laser_focus_matrix", BlockLaserFocusMatrix::new);

    private static <BLOCK extends Block> BlockRegistryObject<BLOCK, ItemBlockTooltip> registerTooltipBlock(String name, Supplier<BLOCK> blockCreator) {
        return BLOCKS.register(name, blockCreator, ItemBlockTooltip::new);
    }
}