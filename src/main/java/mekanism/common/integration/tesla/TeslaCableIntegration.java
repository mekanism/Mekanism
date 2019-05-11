package mekanism.common.integration.tesla;

import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;

@Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = MekanismHooks.TESLA_MOD_ID)
public class TeslaCableIntegration implements ITeslaConsumer {

    public TileEntityUniversalCable tileEntity;

    public EnumFacing side;

    public TeslaCableIntegration(TileEntityUniversalCable tile, EnumFacing facing) {
        tileEntity = tile;
        side = facing;
    }

    @Override
    @Method(modid = MekanismHooks.TESLA_MOD_ID)
    public long givePower(long power, boolean simulated) {
        return Math.round(
              tileEntity.acceptEnergy(side, power * MekanismConfig.current().general.FROM_TESLA.val(), simulated)
              * MekanismConfig.current().general.TO_TESLA.val());
    }
}
