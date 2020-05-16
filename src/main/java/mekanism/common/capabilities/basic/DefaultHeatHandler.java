package mekanism.common.capabilities.basic;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DefaultHeatHandler implements IHeatHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(IHeatHandler.class, new NullStorage<>(), DefaultHeatHandler::new);
    }

    @Override
    public int getHeatCapacitorCount() {
        return 0;
    }

    @Override
    public double getTemperature(int capacitor) {
        return HeatAPI.AMBIENT_TEMP;
    }

    @Override
    public double getInverseConduction(int capacitor) {
        return HeatAPI.DEFAULT_INVERSE_CONDUCTION;
    }

    @Override
    public double getHeatCapacity(int capacitor) {
        return HeatAPI.DEFAULT_HEAT_CAPACITY;
    }

    @Override
    public void handleHeat(int capacitor, double transfer) {
    }
}