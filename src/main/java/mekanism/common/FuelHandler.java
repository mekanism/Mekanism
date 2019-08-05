package mekanism.common;

import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.IFuel;
import buildcraft.api.mj.MjAPI;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.gas.Gas;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ModAPIManager;

public class FuelHandler {

    public static Map<String, FuelGas> fuels = new HashMap<>();

    public static void addGas(Gas gas, int burnTicks, double energyPerMilliBucket) {
        fuels.put(gas.getName(), new FuelGas(burnTicks, energyPerMilliBucket));
    }

    public static FuelGas getFuel(Gas gas) {
        if (fuels.containsKey(gas.getName())) {
            return fuels.get(gas.getName());
        }
        if (BCPresent() && gas.hasFluid() && BuildcraftFuelRegistry.fuel != null) {
            IFuel bcFuel = BuildcraftFuelRegistry.fuel.getFuel(new FluidStack(gas.getFluid(), 1));
            if (bcFuel != null) {
                FuelGas fuel = new FuelGas(bcFuel);
                fuels.put(gas.getName(), fuel);
                return fuel;
            }
        }
        return null;
    }

    public static boolean BCPresent() {
        return ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|fuels") && MekanismUtils.classExists("buildcraft.api.fuels.BuildcraftFuelRegistry") &&
               MekanismUtils.classExists("buildcraft.api.fuels.IFuel");
    }

    public static class FuelGas {

        public int burnTicks;
        public double energyPerTick;

        public FuelGas(int duration, double energyDensity) {
            burnTicks = duration;
            energyPerTick = energyDensity / duration;
        }

        public FuelGas(IFuel bcFuel) {
            burnTicks = bcFuel.getTotalBurningTime() / Fluid.BUCKET_VOLUME;

            // getPowerPerCycle returns value in 1 BuildCraft micro MJ
            // 1 BuildCraft MJ equals 20 RF
            energyPerTick = ForgeEnergyIntegration.fromForge(bcFuel.getPowerPerCycle() / (double) MjAPI.MJ * 20);
        }
    }
}