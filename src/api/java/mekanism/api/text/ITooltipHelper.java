package mekanism.api.text;

import mekanism.api.math.FloatingLong;
import net.minecraft.network.chat.Component;

/**
 * Interface mostly meant as a way to provide us a way to access some internal formatting helpers for some tooltips that are defined in the API. These methods are
 * intentionally not documented as they should not really be relied on and may change at any time.
 */
public interface ITooltipHelper {

    Component getEnergyPerMBDisplayShort(FloatingLong energy);

    Component getRadioactivityDisplayShort(double radioactivity);

    String getFormattedNumber(long number);

    Component getPercent(double ratio);
}