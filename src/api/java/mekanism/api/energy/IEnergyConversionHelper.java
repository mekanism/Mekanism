package mekanism.api.energy;

import mekanism.api.MekanismAPI;

/**
 * Helper that provides access to Mekanism's configured energy conversion rates.
 *
 * @since 10.4.0
 */
public interface IEnergyConversionHelper {

    /**
     * Provides access to Mekanism's implementation of {@link IEnergyConversionHelper}.
     */
    IEnergyConversionHelper INSTANCE = MekanismAPI.getService(IEnergyConversionHelper.class);

    /**
     * @return The conversion rate config between Joules and Joules.
     *
     * @implNote This will always be 1:1, so likely isn't much use except for getting the translation key.
     */
    IEnergyConversion jouleConversion();

    /**
     * @return The conversion rate config between Joules and Forge Energy.
     */
    IEnergyConversion feConversion();
}