package mekanism.common.content.network.transmitter;

import javax.annotation.Nonnull;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.ITier;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;

public interface IUpgradeableTransmitter<DATA extends TransmitterUpgradeData> {

    DATA getUpgradeData();

    boolean dataTypeMatches(@Nonnull TransmitterUpgradeData data);

    void parseUpgradeData(@Nonnull DATA data);

    ITier getTier();

    default boolean canUpgrade(AlloyTier alloyTier) {
        return alloyTier.getBaseTier().ordinal() == getTier().getBaseTier().ordinal() + 1;
    }
}