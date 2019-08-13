package mekanism.common.integration.ic2;

import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;

public class IC2Integration {

    public static double toEU(double joules) {
        return joules * MekanismConfig.general.TO_IC2.get();
    }

    public static int toEUAsInt(double joules) {
        return MekanismUtils.clampToInt(toEU(joules));
    }

    public static double fromEU(double eu) {
        return eu * MekanismConfig.general.FROM_IC2.get();
    }

    //TODO: IC2
    /*@Method(modid = MekanismHooks.IC2_MOD_ID)
    public static boolean isOutputter(TileEntity tileEntity, Direction side) {
        IEnergyTile tile = EnergyNet.instance.getSubTile(tileEntity.getWorld(), tileEntity.getPos());
        return tile instanceof IEnergySource && ((IEnergySource) tile).emitsEnergyTo(null, side.getOpposite());

    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public static boolean isAcceptor(TileEntity tileEntity, Direction side) {
        IEnergyTile tile = EnergyNet.instance.getSubTile(tileEntity.getWorld(), tileEntity.getPos());
        if (tile instanceof IEnergySink) {
            return ((IEnergySink) tile).acceptsEnergyFrom(null, side.getOpposite());
        }
        return false;
    }*/
}