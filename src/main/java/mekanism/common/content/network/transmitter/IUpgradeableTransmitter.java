package mekanism.common.content.network.transmitter;

import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.ITier;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import org.jetbrains.annotations.NotNull;

public interface IUpgradeableTransmitter<DATA extends TransmitterUpgradeData> {

    DATA getUpgradeData();

    boolean dataTypeMatches(@NotNull TransmitterUpgradeData data);

    void parseUpgradeData(@NotNull DATA data);

    ITier getTier();

    default boolean canUpgrade(AlloyTier alloyTier) {
        return alloyTier.getBaseTier().ordinal() == getTier().getBaseTier().ordinal() + 1;
    }
}