package mekanism.common.util.text;

import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

public class EnergyDisplay implements IHasTextComponent {

    public static final EnergyDisplay ZERO = of(0L);

    private final long energy;
    private final long max;

    private EnergyDisplay(long energy, long max) {
        this.energy = Math.max(0, energy);
        this.max = Math.max(0, max);
    }

    public static EnergyDisplay of(EnergyHandler energyHandler) {
        return of(energyHandler.getAmountAsLong(), energyHandler.getCapacityAsLong());
    }

    public static EnergyDisplay of(long energy, long max) {
        return new EnergyDisplay(energy, max);
    }

    public static EnergyDisplay of(long energy) {
        return of(energy, 0);
    }

    @Override
    public Component getTextComponent() {
        if (energy == Long.MAX_VALUE) {
            return MekanismLang.INFINITE.translate();
        }
        Component energyDisplay = UnitDisplayUtils.getEnergyDisplayShort(energy);
        if (max == 0) {
            return energyDisplay;
        }
        return MekanismLang.GENERIC_FRACTION.translate(energyDisplay, max == Long.MAX_VALUE ? MekanismLang.INFINITE : UnitDisplayUtils.getEnergyDisplayShort(max));
    }
}