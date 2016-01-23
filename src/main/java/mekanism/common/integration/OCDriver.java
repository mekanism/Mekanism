package mekanism.common.integration;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.DriverTileEntity;
import li.cil.oc.api.prefab.ManagedEnvironment;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Locale;

/**
 * Created by aidancbrady on 7/20/15.
 */
public class OCDriver extends DriverTileEntity
{
    @Override
    public Class<?> getTileEntityClass()
    {
        return IComputerIntegration.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, int x, int y, int z)
    {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));

        if(tile instanceof IComputerIntegration)
        {
            return new OCManagedEnvironment((IComputerIntegration)tile);
        }

        return null;
    }

    public class OCManagedEnvironment extends ManagedEnvironment implements NamedBlock, ManagedPeripheral
    {
        public IComputerIntegration computerTile;

        public String name;

        public OCManagedEnvironment(IComputerIntegration tile)
        {
            computerTile = tile;
            name = tile.getName().toLowerCase(Locale.ENGLISH).replace(" ", "_");

            setNode(Network.newNode(this, Visibility.Network).withComponent(name, Visibility.Network).create());
        }

        @Override
        public String[] methods()
        {
            return computerTile.getMethods();
        }

        @Override
        public Object[] invoke(String method, Context context, Arguments args) throws Exception
        {
            return computerTile.invoke(Arrays.asList(methods()).indexOf(method), args.toArray());
        }

        @Override
        public int priority()
        {
            return 4;
        }

        @Override
        public String preferredName()
        {
            return name;
        }
    }
}