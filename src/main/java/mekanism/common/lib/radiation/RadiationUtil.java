package mekanism.common.lib.radiation;

import mekanism.api.radiation.IRadiationSource;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.common.config.MekanismConfig;
import net.minecraft.SharedConstants;

public class RadiationUtil {

    /**
     * Calculates approximately how long in ticks radiation will take to decay
     *
     * @param magnitude Magnitude
     * @param source    {@code true} for if it is a {@link IRadiationSource} or an {@link IRadiationEntity} decaying
     */
    public static long getDecayTime(double magnitude, boolean source) {
        double decayRate = source ? MekanismConfig.general.radiationSourceDecayRate.get() : MekanismConfig.general.radiationTargetDecayRate.get();
        long ticks = 0;
        double localMagnitude = magnitude;
        while (localMagnitude > RadiationManager.get().minRadiationMagnitude()) {
            localMagnitude *= decayRate;
            ticks += SharedConstants.TICKS_PER_SECOND;
        }
        return ticks;
    }
}
