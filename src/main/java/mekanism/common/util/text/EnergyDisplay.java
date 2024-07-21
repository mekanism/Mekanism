package mekanism.common.util.text;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;

@NothingNullByDefault
public class EnergyDisplay implements IHasTextComponent {

    public static final EnergyDisplay ZERO = of(0L);

    private final long energy;
    private final long max;

    private EnergyDisplay(long energy, long max) {
        this.energy = Math.max(0, energy);
        this.max = Math.max(0, max);
    }

    public static EnergyDisplay of(IEnergyContainer container) {
        return of(container.getEnergy(), container.getMaxEnergy());
    }

    public static EnergyDisplay of(long energy, long max) {
        return new EnergyDisplay(energy, max);
    }

    public static EnergyDisplay of(long energy) {
        return of(energy, 0L);
    }

    @Override
    public Component getTextComponent() {
        if (energy == Long.MAX_VALUE) {
            return MekanismLang.INFINITE.translate();
        } else if (max == 0L) {
            return MekanismUtils.getEnergyDisplayShort(energy);
        }
        //Pass max back as a new Energy Display so that if we have 0/infinite it shows that properly without us having to add extra handling
        return MekanismLang.GENERIC_FRACTION.translate(MekanismUtils.getEnergyDisplayShort(energy), of(max));
    }
}