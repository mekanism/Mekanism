package mekanism.generators.common;

import mekanism.common.Mekanism;
import mekanism.common.base.IGuiProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.base.TileEntityContainer;
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
        MekanismConfig.local().generators.load(Mekanism.configuration);
        if (Mekanism.configuration.hasChanged()) {
            Mekanism.configuration.save();
        }
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
            case 15:
                return new ContainerNull(player, (TileEntityContainer) tileEntity);
        }

        return null;
    }
}