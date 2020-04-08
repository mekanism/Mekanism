package mekanism.common.capabilities.basic;

import mekanism.api.heat.HeatPacket;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultHeatHandler implements IHeatHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(IHeatHandler.class, new NullStorage<>(), DefaultHeatHandler::new);
    }

    @Override
    public int getHeatCapacitorCount() {
        return 0;
    }

    @Override
    public FloatingLong getTemperature(int capacitor) {
        return FloatingLong.ZERO;
    }

    @Override
    public FloatingLong getInverseConductionCoefficient(int capacitor) {
        return FloatingLong.ZERO;
    }

    @Override
    public FloatingLong getInsulationCoefficient(int capacitor) {
        return FloatingLong.ZERO;
    }

    @Override
    public FloatingLong getHeatCapacity(int capacitor) {
        return FloatingLong.ZERO;
    }

    @Override
    public void handleHeat(int capacitor, HeatPacket transfer) {}
}