package mekanism.common.lib.frequency;

import java.util.List;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.security.ISecurityTile;

public interface IFrequencyHandler extends ISecurityTile {

    TileComponentFrequency getFrequencyComponent();

    default <FREQ extends Frequency> FREQ getFrequency(FrequencyType<FREQ> type) {
        return getFrequencyComponent().getFrequency(type);
    }

    default void setFrequency(FrequencyType<?> type, FrequencyIdentity data) {
        getFrequencyComponent().setFrequencyFromData(type, data);
    }

    default void removeFrequency(FrequencyType<?> type, FrequencyIdentity data) {
        getFrequencyComponent().removeFrequencyFromData(type, data);
    }

    default <FREQ extends Frequency> List<FREQ> getPublicCache(FrequencyType<FREQ> type) {
        return getFrequencyComponent().getPublicCache(type);
    }

    default <FREQ extends Frequency> List<FREQ> getPrivateCache(FrequencyType<FREQ> type) {
        return getFrequencyComponent().getPrivateCache(type);
    }
}