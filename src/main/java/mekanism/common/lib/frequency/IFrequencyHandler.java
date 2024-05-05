package mekanism.common.lib.frequency;

import java.util.List;
import java.util.UUID;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;

public interface IFrequencyHandler {

    TileComponentFrequency getFrequencyComponent();

    default <FREQ extends Frequency> FREQ getFrequency(FrequencyType<FREQ> type) {
        return getFrequencyComponent().getFrequency(type);
    }

    /**
     * Sets or creates a frequency from the data using the given player as the frequency owner.
     */
    default void setFrequency(FrequencyType<?> type, FrequencyIdentity data, UUID player) {
        getFrequencyComponent().setFrequencyFromData(type, data, player);
    }

    /**
     * Removes a frequency of the given type if it exists and the given player is the frequency owner.
     */
    default void removeFrequency(FrequencyType<?> type, FrequencyIdentity data, UUID player) {
        getFrequencyComponent().removeFrequencyFromData(type, data, player);
    }

    default <FREQ extends Frequency> List<FREQ> getPublicCache(FrequencyType<FREQ> type) {
        return getFrequencyComponent().getPublicCache(type);
    }

    default <FREQ extends Frequency> List<FREQ> getTrustedCache(FrequencyType<FREQ> type) {
        return getFrequencyComponent().getTrustedCache(type);
    }

    default <FREQ extends Frequency> List<FREQ> getPrivateCache(FrequencyType<FREQ> type) {
        return getFrequencyComponent().getPrivateCache(type);
    }
}