package mekanism.common.integration.buildcraft;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjPassiveProvider;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.IMjReceiver;
import javax.annotation.Nonnull;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.MekanismHooks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@InterfaceList({
      @Interface(iface = "buildcraft.api.mj.IMjPassiveProvider", modid = MekanismHooks.BUILDCRAFT_MOD_ID),
      @Interface(iface = "buildcraft.api.mj.IMjReceiver", modid = MekanismHooks.BUILDCRAFT_MOD_ID),
      @Interface(iface = "buildcraft.api.mj.IMjReadable", modid = MekanismHooks.BUILDCRAFT_MOD_ID)
})
public class MjIntegration implements IMjReadable, IMjPassiveProvider, IMjReceiver {

    public IEnergyWrapper tileEntity;

    public EnumFacing side;

    public MjIntegration(IEnergyWrapper tile, EnumFacing facing) {
        tileEntity = tile;
        side = facing;
    }

    public static long toMj(double joules) {
        return Math.round(joules * MekanismConfig.current().general.TO_MJ.val());
    }

    public static double fromMj(long mj) {
        return mj * MekanismConfig.current().general.FROM_MJ.val();
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long extractPower(long min, long max, boolean simulate) {
        long toDraw = toMj(tileEntity.acceptEnergy(side, fromMj(max), true));
        if (toDraw < min) {
            return 0;
        }
        if (simulate) {
            //We already simulated it so we can just use that value instead of recalculating it
            return toDraw;
        }
        //Draw it for real this time
        return toMj(tileEntity.acceptEnergy(side, fromMj(toDraw), false));
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long getPowerRequested() {
        return toMj(tileEntity.getMaxEnergy() - tileEntity.getEnergy());
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long receivePower(long microJoules, boolean simulate) {
        return Math.max(0, microJoules - toMj(tileEntity.pullEnergy(side, fromMj(microJoules), simulate)));
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public boolean canConnect(@Nonnull IMjConnector other) {
        //TODO:
        return true;
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long getStored() {
        return toMj(tileEntity.getEnergy());
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long getCapacity() {
        return toMj(tileEntity.getMaxEnergy());
    }
}