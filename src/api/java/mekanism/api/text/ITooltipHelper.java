package mekanism.api.text;

import mekanism.api.math.FloatingLong;
import net.minecraft.util.text.ITextComponent;

/**
 * Interface mostly meant as a way to provide us a way to access some internal formatting helpers for some tooltips that are defined in the API. These methods are
 * intentionally not documented as they should not really be relied on and may change at any time.
 */
public interface ITooltipHelper {

    ITextComponent getEnergyPerMBDisplayShort(FloatingLong energy);

    ITextComponent getRadioactivityDisplayShort(double radioactivity);

    String getFormattedNumber(long number);

    ITextComponent getPercent(double ratio);
}