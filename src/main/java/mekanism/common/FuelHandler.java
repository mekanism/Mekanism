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
        return EMPTY_FUEL;
    }

    public static class FuelGas {

        public int burnTicks;
        public double energyPerTick;

        public FuelGas(int duration, double energyDensity) {
            burnTicks = duration;
            energyPerTick = energyDensity / duration;
        }

        public boolean isEmpty() {
            //TODO: Make a better check than this
            return this == EMPTY_FUEL;
        }
    }
}