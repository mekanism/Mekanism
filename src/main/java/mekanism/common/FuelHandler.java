package mekanism.common;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IGasProvider;
import net.minecraft.tags.Tag;

public class FuelHandler {

    public static final FuelGas EMPTY_FUEL = new FuelGas(0, FloatingLong.ZERO);
    private static Map<Tag<Gas>, FuelGas> tagFuels = new Object2ObjectOpenHashMap<>();
    private static Map<Gas, FuelGas> fuels = new Object2ObjectOpenHashMap<>();

    public static void addGas(Tag<Gas> gasTag, int burnTicks, FloatingLong energyPerMilliBucket) {
        tagFuels.put(gasTag, new FuelGas(burnTicks, energyPerMilliBucket));
    }

    public static void addGas(IGasProvider gas, int burnTicks, FloatingLong energyPerMilliBucket) {
        fuels.put(gas.getGas(), new FuelGas(burnTicks, energyPerMilliBucket));
    }

    @Nonnull
    public static FuelGas getFuel(@Nonnull Gas gas) {
        //TODO: Try to optimize this, maybe use gas.getTags()
        for (Entry<Gas, FuelGas> entry : fuels.entrySet()) {
            if (gas == entry.getKey()) {
                return entry.getValue();
            }
        }
        for (Entry<Tag<Gas>, FuelGas> entry : tagFuels.entrySet()) {
            if (gas.isIn(entry.getKey())) {
                return entry.getValue();
            }
        }
        return EMPTY_FUEL;
    }

    public static class FuelGas {

        public int burnTicks;
        public FloatingLong energyPerTick;

        public FuelGas(int duration, FloatingLong energyDensity) {
            burnTicks = duration;
            energyPerTick = duration == 0 ? energyDensity : energyDensity.divide(duration);
        }

        public boolean isEmpty() {
            //TODO: Make a better check than this
            return this == EMPTY_FUEL;
        }
    }
}