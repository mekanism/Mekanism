package mekanism.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import net.minecraft.tags.Tag;

public class FuelHandler {

    public static final FuelGas EMPTY_FUEL = new FuelGas(0, 0);
    private static Map<Tag<Gas>, FuelGas> fuels = new HashMap<>();

    public static void addGas(Tag<Gas> gasTag, int burnTicks, double energyPerMilliBucket) {
        fuels.put(gasTag, new FuelGas(burnTicks, energyPerMilliBucket));
    }

    @Nonnull
    public static FuelGas getFuel(@Nonnull Gas gas) {
        //TODO: Try to optimize this, maybe use gas.getTags()
        for (Entry<Tag<Gas>, FuelGas> entry : fuels.entrySet()) {
            if (gas.isIn(entry.getKey())) {
                return entry.getValue();
            }
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
        return EMPTY_FUEL;
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

        public boolean isEmpty() {
            //TODO: Make a better check than this
            return this == EMPTY_FUEL;
        }
    }
}