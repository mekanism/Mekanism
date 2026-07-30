package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import net.minecraft.network.chat.TextColor;

public interface IStorageTier extends ITier {

    long getCapacity();

    int getTransferRate();

    default boolean isCreative() {
        return getBaseTier() == BaseTier.CREATIVE;
    }

    default TextColor getTextColor() {
        return getBaseTier().getTextColor();
    }
}