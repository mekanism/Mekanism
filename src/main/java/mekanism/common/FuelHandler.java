package mekanism.common;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.gas.Gas;
import mekanism.api.providers.IGasProvider;
import net.minecraft.util.ResourceLocation;

public class FuelHandler {

    //TODO: Tags
    public static Map<ResourceLocation, FuelGas> fuels = new HashMap<>();

    public static void addGas(IGasProvider gasProvider, int burnTicks, double energyPerMilliBucket) {
        //TODO: use tag instead
        fuels.put(gasProvider.getRegistryName(), new FuelGas(burnTicks, energyPerMilliBucket));
    }

    public static FuelGas getFuel(Gas gas) {
        //TODO: use tag instead
        if (fuels.containsKey(gas.getRegistryName())) {
            return fuels.get(gas.getRegistryName());
        }
        //TODO: BuildCraft
        /*if (BCPresent() && gas.hasFluid() && BuildcraftFuelRegistry.fuel != null) {
            IFuel bcFuel = BuildcraftFuelRegistry.fuel.getFuel(new FluidStack(gas.getFluid(), 1));
            if (bcFuel != null) {
                FuelGas fuel = new FuelGas(bcFuel);
                fuels.put(gas.getName(), fuel);
                return fuel;
            }
        }*/
        return null;
    }

    public static class FuelGas {

        public int burnTicks;
        public double energyPerTick;

        public FuelGas(int duration, double energyDensity) {
            burnTicks = duration;
            energyPerTick = energyDensity / duration;
        }

        //TODO: BuildCraft
        /*public FuelGas(IFuel bcFuel) {
            burnTicks = bcFuel.getTotalBurningTime() / FluidAttributes.BUCKET_VOLUME;

            // getPowerPerCycle returns value in 1 BuildCraft micro MJ
            // 1 BuildCraft MJ equals 20 RF
            energyPerTick = ForgeEnergyIntegration.fromForge(bcFuel.getPowerPerCycle() / (double) MjAPI.MJ * 20);
        }*/
    }
}