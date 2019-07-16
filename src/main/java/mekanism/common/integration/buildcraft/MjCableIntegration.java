package mekanism.common.integration.buildcraft;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import javax.annotation.Nonnull;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;

@Interface(iface = "buildcraft.api.mj.IMjReceiver", modid = MekanismHooks.BUILDCRAFT_MOD_ID)
public class MjCableIntegration implements IMjReceiver {

    public TileEntityUniversalCable tileEntity;

    public EnumFacing side;

    public MjCableIntegration(TileEntityUniversalCable tile, EnumFacing facing) {
        tileEntity = tile;
        side = facing;
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public boolean canConnect(@Nonnull IMjConnector other) {
        return tileEntity.canConnect(side);
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long getPowerRequested() {
        return MjIntegration.toMj(tileEntity.getMaxEnergy() - tileEntity.getEnergy());
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long receivePower(long microJoules, boolean simulate) {
        return Math.max(0, microJoules - MjIntegration.toMj(tileEntity.acceptEnergy(side, MjIntegration.fromMj(microJoules), simulate)));
    }
}