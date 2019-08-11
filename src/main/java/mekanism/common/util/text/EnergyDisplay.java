package mekanism.common.util.text;

import mekanism.api.text.IHasTextComponent;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.text.ITextComponent;

public class EnergyDisplay implements IHasTextComponent {

    private final double energy;
    private final double max;

    private EnergyDisplay(double energy, double max) {
        this.energy = energy;
        this.max = max;
    }

    //TODO: Wrapper for getting this from itemstack
    public static EnergyDisplay of(double energy, double max) {
        return new EnergyDisplay(energy, max);
    }

    public static EnergyDisplay of(double energy) {
        return of(energy, 0);
    }

    @Override
    public ITextComponent getTextComponent() {
        if (energy == Double.MAX_VALUE) {
            return TextComponentUtil.build(Translation.of("mekanism.gui.infinite"));
        }
        if (max == 0) {
            return TextComponentUtil.build(MekanismUtils.getEnergyDisplayShort(energy));
        }
        //Pass max back as a new Energy Display so that if we have 0/infinite it shows that properly without us having to add extra handling
        return TextComponentUtil.build(MekanismUtils.getEnergyDisplayShort(energy), "/", of(max));
    }
}