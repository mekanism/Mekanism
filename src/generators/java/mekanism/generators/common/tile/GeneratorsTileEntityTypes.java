package mekanism.generators.common.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.IBlockProvider;
import mekanism.generators.common.GeneratorsBlock;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorFrame;
import mekanism.generators.common.tile.reactor.TileEntityReactorGlass;
import mekanism.generators.common.tile.reactor.TileEntityReactorLaserFocusMatrix;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorPort;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.IForgeRegistry;

public class GeneratorsTileEntityTypes {

    private static final List<TileEntityType<?>> types = new ArrayList<>();

    //Generators
    public static final TileEntityType<TileEntityAdvancedSolarGenerator> ADVANCED_SOLAR_GENERATOR = create(GeneratorsBlock.ADVANCED_SOLAR_GENERATOR, TileEntityAdvancedSolarGenerator::new);
    public static final TileEntityType<TileEntityBioGenerator> BIO_GENERATOR = create(GeneratorsBlock.BIO_GENERATOR, TileEntityBioGenerator::new);
    public static final TileEntityType<TileEntityGasGenerator> GAS_BURNING_GENERATOR = create(GeneratorsBlock.GAS_BURNING_GENERATOR, TileEntityGasGenerator::new);
    public static final TileEntityType<TileEntityHeatGenerator> HEAT_GENERATOR = create(GeneratorsBlock.HEAT_GENERATOR, TileEntityHeatGenerator::new);
    public static final TileEntityType<TileEntitySolarGenerator> SOLAR_GENERATOR = create(GeneratorsBlock.SOLAR_GENERATOR, TileEntitySolarGenerator::new);
    public static final TileEntityType<TileEntityWindGenerator> WIND_GENERATOR = create(GeneratorsBlock.WIND_GENERATOR, TileEntityWindGenerator::new);
    //Reactor
    public static final TileEntityType<TileEntityReactorController> REACTOR_CONTROLLER = create(GeneratorsBlock.REACTOR_CONTROLLER, TileEntityReactorController::new);
    public static final TileEntityType<TileEntityReactorFrame> REACTOR_FRAME = create(GeneratorsBlock.REACTOR_FRAME, TileEntityReactorFrame::new);
    public static final TileEntityType<TileEntityReactorGlass> REACTOR_GLASS = create(GeneratorsBlock.REACTOR_GLASS, TileEntityReactorGlass::new);
    public static final TileEntityType<TileEntityReactorLaserFocusMatrix> LASER_FOCUS_MATRIX = create(GeneratorsBlock.LASER_FOCUS_MATRIX, TileEntityReactorLaserFocusMatrix::new);
    public static final TileEntityType<TileEntityReactorLogicAdapter> REACTOR_LOGIC_ADAPTER = create(GeneratorsBlock.REACTOR_LOGIC_ADAPTER, TileEntityReactorLogicAdapter::new);
    public static final TileEntityType<TileEntityReactorPort> REACTOR_PORT = create(GeneratorsBlock.REACTOR_PORT, TileEntityReactorPort::new);
    //Turbine
    public static final TileEntityType<TileEntityElectromagneticCoil> ELECTROMAGNETIC_COIL = create(GeneratorsBlock.ELECTROMAGNETIC_COIL, TileEntityElectromagneticCoil::new);
    public static final TileEntityType<TileEntityRotationalComplex> ROTATIONAL_COMPLEX = create(GeneratorsBlock.ROTATIONAL_COMPLEX, TileEntityRotationalComplex::new);
    public static final TileEntityType<TileEntitySaturatingCondenser> SATURATING_CONDENSER = create(GeneratorsBlock.SATURATING_CONDENSER, TileEntitySaturatingCondenser::new);
    public static final TileEntityType<TileEntityTurbineCasing> TURBINE_CASING = create(GeneratorsBlock.TURBINE_CASING, TileEntityTurbineCasing::new);
    public static final TileEntityType<TileEntityTurbineRotor> TURBINE_ROTOR = create(GeneratorsBlock.TURBINE_ROTOR, TileEntityTurbineRotor::new);
    public static final TileEntityType<TileEntityTurbineValve> TURBINE_VALVE = create(GeneratorsBlock.TURBINE_VALVE, TileEntityTurbineValve::new);
    public static final TileEntityType<TileEntityTurbineVent> TURBINE_VENT = create(GeneratorsBlock.TURBINE_VENT, TileEntityTurbineVent::new);

    private static <T extends TileEntity> TileEntityType<T> create(IBlockProvider provider, Supplier<? extends T> factory) {
        TileEntityType.Builder<T> builder = TileEntityType.Builder.create(factory, provider.getBlock());
        //fixerType = DataFixesManager.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getVersion().getWorldVersion())).getChoiceType(TypeReferences.BLOCK_ENTITY, registryName.getPath());
        //TODO: I don't believe we have a data fixer type for our stuff so it is technically null not the above thing which is taken from TileEntityTypes#register
        // Note: If above is needed, we should add the try catch that TileEntityTypes#register includes
        TileEntityType<T> type = builder.build(null);
        type.setRegistryName(provider.getRegistryName());
        types.add(type);
        return type;
    }

    public static void registerTileEntities(IForgeRegistry<TileEntityType<?>> registry) {
        types.forEach(registry::register);
        //TODO: Should the list be cleared afterwards as it isn't really needed anymore after registration
    }
}