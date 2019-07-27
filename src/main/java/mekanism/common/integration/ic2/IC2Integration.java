package mekanism.common.integration.ic2;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Method;

public class IC2Integration {

    public static double toEU(double joules) {
        return joules * MekanismConfig.current().general.TO_IC2.val();
    }

    public static int toEUAsInt(double joules) {
        return MekanismUtils.clampToInt(toEU(joules));
    }

    public static double fromEU(double eu) {
        return eu * MekanismConfig.current().general.FROM_IC2.val();
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public static boolean isOutputter(TileEntity tileEntity, EnumFacing side) {
        IEnergyTile tile = EnergyNet.instance.getSubTile(tileEntity.getWorld(), tileEntity.getPos());
        return tile instanceof IEnergySource && ((IEnergySource) tile).emitsEnergyTo(null, side.getOpposite());

    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public static boolean isAcceptor(TileEntity tileEntity, EnumFacing side) {
        IEnergyTile tile = EnergyNet.instance.getSubTile(tileEntity.getWorld(), tileEntity.getPos());
        if (tile instanceof IEnergySink) {
            return ((IEnergySink) tile).acceptsEnergyFrom(null, side.getOpposite());
        }
        return false;
    }
}