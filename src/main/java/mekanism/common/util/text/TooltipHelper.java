package mekanism.common.util.text;

import mekanism.api.math.FloatingLong;
import mekanism.api.text.ITooltipHelper;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import net.minecraft.util.text.ITextComponent;

public class TooltipHelper implements ITooltipHelper {

    public static final TooltipHelper INSTANCE = new TooltipHelper();

    @Override
    public ITextComponent getEnergyPerMBDisplayShort(FloatingLong energy) {
        return MekanismLang.GENERIC_PER_MB.translate(MekanismUtils.getEnergyDisplayShort(energy));
    }

    @Override
    public ITextComponent getRadioactivityDisplayShort(double radioactivity) {
        return UnitDisplayUtils.getDisplayShort(radioactivity, RadiationUnit.SVH, 2);
    }

    @Override
    public String getFormattedNumber(long number) {
        return TextUtils.format(number);
    }

    @Override
    public ITextComponent getPercent(double ratio) {
        return TextUtils.getPercent(ratio);
    }
}