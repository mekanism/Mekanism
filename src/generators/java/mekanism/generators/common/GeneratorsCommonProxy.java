package mekanism.generators.common;

import mekanism.common.Mekanism;
import mekanism.common.base.IGuiProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.generators.common.inventory.container.ContainerBioGenerator;
import mekanism.generators.common.inventory.container.ContainerGasGenerator;
import mekanism.generators.common.inventory.container.ContainerHeatGenerator;
import mekanism.generators.common.inventory.container.ContainerReactorController;
import mekanism.generators.common.inventory.container.ContainerSolarGenerator;
import mekanism.generators.common.inventory.container.ContainerWindGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Common proxy for the Mekanism Generators module.
 *
 * @author AidanBrady
 */
public class GeneratorsCommonProxy implements IGuiProvider {

    /**
     * Register normal tile entities
     */
    public void registerTileEntities() {
        Mekanism.registerTileEntities(GeneratorsBlock.values());
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
    public Object getClientGui(int ID, PlayerEntity player, World world, BlockPos pos) {
        return null;
    }

    @Override
    public Container getServerGui(int ID, PlayerEntity player, World world, BlockPos pos) {
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
                return new ContainerNull(player, (TileEntityMekanism) tileEntity);
        }

        return null;
    }
}