package mekanism.generators.common;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import mekanism.common.Mekanism;
import mekanism.common.base.IGuiProvider;
import mekanism.common.config.MekanismConfig.generators;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorType;
import mekanism.generators.common.inventory.container.ContainerBioGenerator;
import mekanism.generators.common.inventory.container.ContainerGasGenerator;
import mekanism.generators.common.inventory.container.ContainerHeatGenerator;
import mekanism.generators.common.inventory.container.ContainerReactorController;
import mekanism.generators.common.inventory.container.ContainerSolarGenerator;
import mekanism.generators.common.inventory.container.ContainerWindGenerator;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Common proxy for the Mekanism Generators module.
 *
 * @author AidanBrady
 */
public class GeneratorsCommonProxy implements IGuiProvider {

    private static void registerTileEntity(Class<? extends TileEntity> clazz, String name) {
        GameRegistry.registerTileEntity(clazz, new ResourceLocation(MekanismGenerators.MODID, name));
    }

    /**
     * Register normal tile entities
     */
    public void registerTileEntities() {
        registerTileEntity(TileEntityAdvancedSolarGenerator.class, "advanced_solar_generator");
        registerTileEntity(TileEntityBioGenerator.class, "bio_generator");
        registerTileEntity(TileEntityElectromagneticCoil.class, "electromagnetic_coil");
        registerTileEntity(TileEntityGasGenerator.class, "gas_generator");
        registerTileEntity(TileEntityHeatGenerator.class, "heat_generator");
        registerTileEntity(TileEntityReactorController.class, "reactor_controller");
        registerTileEntity(TileEntityReactorFrame.class, "reactor_frame");
        registerTileEntity(TileEntityReactorGlass.class, "reactor_glass");
        registerTileEntity(TileEntityReactorLaserFocusMatrix.class, "reactor_laser_focus");
        registerTileEntity(TileEntityReactorLogicAdapter.class, "reactor_logic_adapter");
        registerTileEntity(TileEntityReactorPort.class, "reactor_port");
        registerTileEntity(TileEntityRotationalComplex.class, "rotational_complex");
        registerTileEntity(TileEntitySaturatingCondenser.class, "saturating_condenser");
        registerTileEntity(TileEntitySolarGenerator.class, "solar_generator");
        registerTileEntity(TileEntityTurbineCasing.class, "turbine_casing");
        registerTileEntity(TileEntityTurbineRotor.class, "turbine_rod");
        registerTileEntity(TileEntityTurbineValve.class, "turbine_valve");
        registerTileEntity(TileEntityTurbineVent.class, "turbine_vent");
        registerTileEntity(TileEntityWindGenerator.class, "wind_turbine");
    }

    /**
     * Register tile entities that have special models. Overwritten in client to register TESRs.
     */
    public void registerTESRs() {
    }

    /**
     * Register and load client-only item render information.
     */
    public void registerItemRenders() {
    }

    /**
     * Register and load client-only block render information.
     */
    public void registerBlockRenders() {
    }

    public void preInit() {
    }

    /**
     * Set and load the mod's common configuration properties.
     */
    public void loadConfiguration() {
        generators.advancedSolarGeneration = Mekanism.configuration.get("generation", "AdvancedSolarGeneration", 300D,
              "Peak output for the Advanced Solar Generator. Note: It can go higher than this value in some extreme environments.")
              .getDouble();
        generators.bioGeneration = Mekanism.configuration.get("generation", "BioGeneration", 350D,
              "Amount of energy in Joules the Bio Generator produces per tick.").getDouble();
        generators.heatGeneration = Mekanism.configuration.get("generation", "HeatGeneration", 150D,
              "Amount of energy in Joules the Heat Generator produces per tick. (heatGenerationLava * heatGenerationLava) + heatGenerationNether")
              .getDouble();
        generators.heatGenerationLava = Mekanism.configuration
              .get("generation", "HeatGenerationLava", 5D, "Multiplier of effectiveness of Lava in the Heat Generator.")
              .getDouble();
        generators.heatGenerationNether = Mekanism.configuration.get("generation", "HeatGenerationNether", 100D,
              "Add this amount of Joules to the energy produced by a heat generator if it is in the Nether.")
              .getDouble();
        generators.solarGeneration = Mekanism.configuration.get("generation", "SolarGeneration", 50D,
              "Peak output for the Solar Generator. Note: It can go higher than this value in some extreme environments.")
              .getDouble();

        loadWindConfiguration();

        generators.turbineBladesPerCoil = Mekanism.configuration.get("generation", "TurbineBladesPerCoil", 4,
              "The number of blades on each turbine coil per blade applied.").getInt();
        generators.turbineVentGasFlow = Mekanism.configuration.get("generation", "TurbineVentGasFlow", 16000D,
              "The rate at which steam is vented into the turbine.").getDouble();
        generators.turbineDisperserGasFlow = Mekanism.configuration.get("generation", "TurbineDisperserGasFlow", 640D,
              "The rate at which steam is dispersed into the turbine.").getDouble();
        generators.condenserRate = Mekanism.configuration.get("generation", "TurbineCondenserFlowRate", 32000,
              "The rate at which steam is condensed in the turbine.").getInt();

        generators.energyPerFusionFuel = Mekanism.configuration.get("generation", "EnergyPerFusionFuel", 5E6D,
              "Affects the Injection Rate, Max Temp, and Ignition Temp.").getDouble();

        for (GeneratorType type : GeneratorType.getGeneratorsForConfig()) {
            generators.generatorsManager.setEntry(type.blockName,
                  Mekanism.configuration.get("generators", type.blockName + "Enabled", true,
                        "Allow " + type.blockName + " to be used/crafted.").getBoolean());
        }

        if (Mekanism.configuration.hasChanged()) {
            Mekanism.configuration.save();
        }
    }

    private void loadWindConfiguration() {
        generators.windGenerationMin = Mekanism.configuration.get("generation", "WindGenerationMin", 60D,
              "Minimum base generation value of the Wind Generator.").getDouble();
        generators.windGenerationMax = Mekanism.configuration.get("generation", "WindGenerationMax", 480D,
              "Maximum base generation value of the Wind Generator.").getDouble();

        //Ensure max > min to avoid division by zero later
        final int minY = Mekanism.configuration.get("generation", "WindGenerationMinY", 24,
              "The minimum Y value that affects the Wind Generators Power generation.").getInt();
        final int maxY = Mekanism.configuration.get("generation", "WindGenerationMaxY", 255,
              "The maximum Y value that affects the Wind Generators Power generation.").getInt();

        generators.windGenerationMinY = minY;
        generators.windGenerationMaxY = Math.max(minY + 1, maxY);

        int[] windGenerationBlacklistDims = Mekanism.configuration
              .get("generation", "WindGenerationDimBlacklist", new int[]{},
                    "The list of dimension ids that the Wind Generator will not generate power in.").getIntList();
        generators.windGenerationDimBlacklist = IntStream.of(windGenerationBlacklistDims).boxed().
              collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public Object getClientGui(int ID, EntityPlayer player, World world, BlockPos pos) {
        return null;
    }

    @Override
    public Container getServerGui(int ID, EntityPlayer player, World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);

        switch (ID) {
            case 0:
                return new ContainerHeatGenerator(player.inventory, (TileEntityHeatGenerator) tileEntity);
            case 1:
                return new ContainerSolarGenerator(player.inventory, (TileEntitySolarGenerator) tileEntity);
            case 3:
                return new ContainerGasGenerator(player.inventory, (TileEntityGasGenerator) tileEntity);
            case 4:
                return new ContainerBioGenerator(player.inventory, (TileEntityBioGenerator) tileEntity);
            case 5:
                return new ContainerWindGenerator(player.inventory, (TileEntityWindGenerator) tileEntity);
            case 6:
                return new ContainerFilter(player.inventory, (TileEntityTurbineCasing) tileEntity);
            case 7:
                return new ContainerNull(player, (TileEntityTurbineCasing) tileEntity);
            case 10:
                return new ContainerReactorController(player.inventory, (TileEntityReactorController) tileEntity);
            case 11:
            case 12:
            case 13:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 15:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
        }

        return null;
    }
}
