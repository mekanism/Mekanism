package mekanism.common.capabilities;

import mekanism.api.IHeatTransfer;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultHeatTransfer implements IHeatTransfer {

    public static void register() {
        CapabilityManager.INSTANCE.register(IHeatTransfer.class, new NullStorage<>(), DefaultHeatTransfer::new);
    }

    @Override
    public double getTemp() {
        return 0;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return 0;
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        return 0;
    }

    @Override
    public void transferHeatTo(double heat) {
    }

    @Override
    public double[] simulateHeat() {
        return new double[]{0, 0};
    }

    @Override
    public double applyTemperatureChange() {
        return 0;
    }
}