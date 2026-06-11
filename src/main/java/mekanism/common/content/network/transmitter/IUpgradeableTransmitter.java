package mekanism.common.content.network.transmitter;

import mekanism.api.tier.IAlloyTier;
import mekanism.api.tier.ITier;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public interface IUpgradeableTransmitter<DATA extends TransmitterUpgradeData> {

    @Nullable
    DATA getUpgradeData();

    boolean dataTypeMatches(TransmitterUpgradeData data);

    void parseUpgradeData(DATA data, TransactionContext transaction);

    ITier getTier();

    default boolean canUpgrade(IAlloyTier alloyTier) {
        return alloyTier.getBaseTierLevel() == getTier().getBaseTierLevel() + 1;
    }
}