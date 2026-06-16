package mekanism.common.tier;

import mekanism.api.tier.ITier;

public interface IStorageTier extends ITier {

    long getCapacity();

    int getTransferRate();

    default boolean isCreative() {
        return false;
    }
}