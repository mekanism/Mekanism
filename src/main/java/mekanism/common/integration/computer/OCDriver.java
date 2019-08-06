package mekanism.common.integration.computer;

import java.util.Arrays;
import java.util.Locale;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by aidancbrady on 7/20/15.
 */
public class OCDriver extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return IComputerIntegration.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, Direction side) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IComputerIntegration) {
            return new OCManagedEnvironment((IComputerIntegration) tile);
        }
        return null;
    }

    public static class OCManagedEnvironment extends AbstractManagedEnvironment implements NamedBlock, ManagedPeripheral {

        public IComputerIntegration computerTile;

        public String name;

        public OCManagedEnvironment(IComputerIntegration tile) {
            computerTile = tile;
            name = tile.getName().toLowerCase(Locale.ENGLISH).replace(" ", "_");
            setNode(Network.newNode(this, Visibility.Network).withComponent(name, Visibility.Network).create());
        }

        @Override
        public String[] methods() {
            return computerTile.getMethods();
        }

        @Override
        public Object[] invoke(String method, Context context, Arguments args) throws Exception {
            return computerTile.invoke(Arrays.asList(methods()).indexOf(method), args.toArray());
        }

        @Override
        public int priority() {
            return 4;
        }

        @Override
        public String preferredName() {
            return name;
        }
    }
}