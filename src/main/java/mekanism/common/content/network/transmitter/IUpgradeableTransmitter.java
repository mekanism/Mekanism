package mekanism.common.content.network.transmitter;

import mekanism.api.tier.IAlloyTier;
import mekanism.api.tier.ITier;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import org.jetbrains.annotations.NotNull;

public interface IUpgradeableTransmitter<DATA extends TransmitterUpgradeData> {

    DATA getUpgradeData();

    boolean dataTypeMatches(@NotNull TransmitterUpgradeData data);

    void parseUpgradeData(@NotNull DATA data);

    ITier getTier();

    default boolean canUpgrade(IAlloyTier alloyTier) {
        return alloyTier.getBaseTierLevel() == getTier().getBaseTierLevel() + 1;
    }
}