package mekanism.common.integration.buildcraft;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjPassiveProvider;
import javax.annotation.Nonnull;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;

@Interface(iface = "buildcraft.api.mj.IMjPassiveProvider", modid = MekanismHooks.BUILDCRAFT_MOD_ID)
public class MjCableIntegration implements IMjPassiveProvider {

    public TileEntityUniversalCable tileEntity;

    public EnumFacing side;

    public MjCableIntegration(TileEntityUniversalCable tile, EnumFacing facing) {
        tileEntity = tile;
        side = facing;
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public long extractPower(long min, long max, boolean simulate) {
        long toDraw = MjIntegration.toMj(tileEntity.acceptEnergy(side, MjIntegration.fromMj(max), true));
        if (toDraw < min) {
            return 0;
        }
        if (simulate) {
            //We already simulated it so we can just use that value instead of recalculating it
            return toDraw;
        }
        //Draw it for real this time
        return MjIntegration.toMj(tileEntity.acceptEnergy(side, MjIntegration.fromMj(toDraw), false));
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public boolean canConnect(@Nonnull IMjConnector other) {
        return tileEntity.canConnect(side);
    }
}