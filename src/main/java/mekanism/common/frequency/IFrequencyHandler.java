package mekanism.common.frequency;

import java.util.List;
import mekanism.common.frequency.Frequency.FrequencyIdentity;
import mekanism.common.security.ISecurityTile;

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