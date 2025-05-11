package mekanism.api.text;

import mekanism.api.MekanismAPI;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Interface mostly meant as a way to provide us a way to access some internal formatting helpers for some tooltips that are defined in the API. These methods are
 * intentionally not documented as they should not really be relied on and may change at any time.
 */
@Internal
public interface ITooltipHelper {

    ITooltipHelper INSTANCE = MekanismAPI.getService(ITooltipHelper.class);

    Component getEnergyPerMBDisplayShort(long energy);

    Component getRadioactivityDisplayShort(double radioactivity);

    Component getTemperatureDisplayShort(double temperature);

    String getFormattedNumber(long number);

    Component getPercent(double ratio);

    Component getEnergyDisplay(long joules, boolean perTick);

    Component getFluidDisplay(long joules, boolean perTick);
}