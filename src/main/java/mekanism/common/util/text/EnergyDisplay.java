package mekanism.common.util.text;

import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

public class EnergyDisplay implements IHasTextComponent {

    public static final EnergyDisplay ZERO = of(0L, false);

    private final boolean fullEnergyDisplay;
    private final long energy;
    private final long max;

    private EnergyDisplay(long energy, long max, boolean fullEnergyDisplay) {
        if (max > 0 && energy > max && !FMLEnvironment.isProduction()) {
            //Mask edge cases when there might be more energy stored than max energy except in dev
            //Note: We skip doing this for when the max energy is zero as that is used to represent only the energy value should be displayed
            max = energy;
        }
        this.energy = Math.max(0, energy);
        this.max = Math.max(0, max);
        this.fullEnergyDisplay = fullEnergyDisplay;
    }

    public static EnergyDisplay of(EnergyHandler energyHandler, boolean fullEnergyDisplay) {
        return of(energyHandler.getAmountAsLong(), energyHandler.getCapacityAsLong(), fullEnergyDisplay);
    }

    public static EnergyDisplay of(long energy, long max, boolean fullEnergyDisplay) {
        return new EnergyDisplay(energy, max, fullEnergyDisplay);
    }

    public static EnergyDisplay of(long energy, boolean fullEnergyDisplay) {
        return of(energy, 0, fullEnergyDisplay);
    }

    @Override
    public Component getTextComponent() {
        if (energy == Long.MAX_VALUE) {
            return MekanismLang.INFINITE.translate();
        }
        Component energyDisplay = UnitDisplayUtils.getEnergyDisplay(energy, fullEnergyDisplay);
        if (max == 0) {
            return energyDisplay;
        }
        return MekanismLang.GENERIC_FRACTION.translate(energyDisplay, max == Long.MAX_VALUE ? MekanismLang.INFINITE : UnitDisplayUtils.getEnergyDisplay(max, fullEnergyDisplay));
    }
}