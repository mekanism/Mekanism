package mekanism.common.integration.tesla;

import mekanism.common.base.IEnergyWrapper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.MekanismHooks;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@InterfaceList({
      @Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = MekanismHooks.TESLA_MOD_ID),
      @Interface(iface = "net.darkhax.tesla.api.ITeslaProducer", modid = MekanismHooks.TESLA_MOD_ID),
      @Interface(iface = "net.darkhax.tesla.api.ITeslaHolder", modid = MekanismHooks.TESLA_MOD_ID)
})
public class TeslaIntegration implements ITeslaHolder, ITeslaConsumer, ITeslaProducer {

    public IEnergyWrapper tileEntity;

    public EnumFacing side;

    public TeslaIntegration(IEnergyWrapper tile, EnumFacing facing) {
        tileEntity = tile;
        side = facing;
    }

    public static long toTesla(double joules) {
        return Math.round(joules * MekanismConfig.current().general.TO_TESLA.val());
    }

    public static double fromTesla(long tesla) {
        return tesla * MekanismConfig.current().general.FROM_TESLA.val();
    }

    @Override
    @Method(modid = MekanismHooks.TESLA_MOD_ID)
    public long getStoredPower() {
        return toTesla(tileEntity.getEnergy());
    }

    @Override
    @Method(modid = MekanismHooks.TESLA_MOD_ID)
    public long getCapacity() {
        return toTesla(tileEntity.getMaxEnergy());
    }

    @Override
    @Method(modid = MekanismHooks.TESLA_MOD_ID)
    public long takePower(long power, boolean simulate) {
        return toTesla(tileEntity.pullEnergy(side, fromTesla(power), simulate));
    }

    @Override
    @Method(modid = MekanismHooks.TESLA_MOD_ID)
    public long givePower(long power, boolean simulate) {
        return toTesla(tileEntity.acceptEnergy(side, fromTesla(power), simulate));
    }
}