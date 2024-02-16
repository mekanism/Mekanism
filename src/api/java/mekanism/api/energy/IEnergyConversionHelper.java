package mekanism.api.energy;

import java.util.ServiceLoader;

/**
 * Helper that provides access to Mekanism's configured energy conversion rates.
 *
 * @since 10.4.0
 */
public interface IEnergyConversionHelper {

    /**
     * Provides access to Mekanism's implementation of {@link IEnergyConversionHelper}.
     */
    IEnergyConversionHelper INSTANCE = ServiceLoader.load(IEnergyConversionHelper.class).findFirst().orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IEnergyConversionHelper found"));

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