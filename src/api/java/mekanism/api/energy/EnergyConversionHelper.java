package mekanism.api.energy;

import com.mojang.logging.LogUtils;
import mekanism.api.MekanismAPI;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Helper that provides access to Mekanism's configured energy conversion rates.
 *
 * @since 10.3.4
 */
public class EnergyConversionHelper {

    private EnergyConversionHelper() {
    }

    private static IEnergyConversion[] energyConversions;

    /**
     * Retrieves and caches all the internal energy unit objects for Mekanism.
     */
    @VisibleForTesting
    static IEnergyConversion[] getEnergyConversions() {
        if (energyConversions == null) {//Harmless race
            try {
                Class<?> clazz = Class.forName("mekanism.common.util.UnitDisplayUtils$EnergyUnit");
                if (clazz.getEnumConstants() instanceof IEnergyConversion[] conversions) {
                    energyConversions = conversions;
                } else {
                    MekanismAPI.logger.error(LogUtils.FATAL_MARKER, "Error Energy Units are of the wrong type, Mekanism may be absent, damaged, or outdated.");
                }
            } catch (ReflectiveOperationException ex) {
                MekanismAPI.logger.error(LogUtils.FATAL_MARKER, "Error retrieving Energy Units, Mekanism may be absent, damaged, or outdated.");
            }
        }
        return energyConversions;
    }

    /**
     * @return The conversion rate config between Joules and Joules.
     *
     * @implNote This will always be 1:1, so likely isn't much use except for getting the translation key.
     */
    public static IEnergyConversion jouleConversion() {
        return EnergyConversions.JOULES.getInternal();
    }

    /**
     * @return The conversion rate config between Joules and Forge Energy.
     */
    public static IEnergyConversion feConversion() {
        return EnergyConversions.FORGE_ENERGY.getInternal();
    }

    /**
     * @return The conversion rate config between Joules and IC2's EU.
     */
    public static IEnergyConversion euConversion() {
        return EnergyConversions.ELECTRICAL_UNITS.getInternal();
    }

    /**
     * Helper to make it easier to cache the corresponding energy unit to a specific variable while making the magic number order slightly more behind the scenes.
     */
    @VisibleForTesting
    enum EnergyConversions {
        JOULES,
        FORGE_ENERGY,
        ELECTRICAL_UNITS;

        private IEnergyConversion internal;

        private IEnergyConversion getInternal() {
            if (internal == null) {//Harmless race
                IEnergyConversion[] energyUnits = getEnergyConversions();
                if (energyUnits != null) {
                    //Note: We don't need to log an error if we failed to get the energy units as that will be handled in the getEnergyUnits method
                    int index = ordinal();
                    if (index < energyUnits.length) {
                        internal = energyUnits[index];
                    } else {
                        MekanismAPI.logger.error(LogUtils.FATAL_MARKER, "Error retrieving energy conversion {}, Mekanism may be absent, damaged, or outdated.", name());
                    }
                }
            }
            return internal;
        }
    }
}